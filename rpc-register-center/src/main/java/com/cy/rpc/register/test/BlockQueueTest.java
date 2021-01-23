package com.cy.rpc.register.test;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.curator.ServiceCuratorFramework;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import com.cy.rpc.register.utils.ByteUtils;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.springframework.data.util.CastUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenyu3
 * 阻塞队列测试
 * @param <T>
 */
public class BlockQueueTest<T> {

    private final ServiceCuratorFramework framework;

    //阻塞队列的根节点
    private static final String path = "/block-queue";

    //阻塞队列的元素节点目录，临时有序节点存队列数据
    private static final String nodes = path + "/nodes";

    //临时有序节点前缀，如/block-queue/nodes/test000000001
    private static final String prefix = "/test";

    //为保证线程安全，/offer为临时唯一节点，只有创建成功，才有资格进行offer写数据，创建不成功，一律阻塞，直到有资格可以存为止
    private static final String offBefore = path + "/offer";

    //为保证线程安全，/poll为临时唯一节点，只有创建成功，才有资格进行poll取数据，创建不成功，一律阻塞，直到有资格可以取为止
    private static final String pollBefore = path + "/poll";

    //队列的最大大小，超过这个大小，一律阻塞
    private static final Integer maxQueueSize = 2;

    public BlockQueueTest(ServiceCuratorFramework framework) {
        this.framework = framework;
    }

    /**
     * 先创建临时节点，如果临时节点创建成功，表示轮到它写入数据
     * @return
     */
    private boolean offBefore() {
        try {
            framework.getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(offBefore, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写入队列
     * @param t
     * @throws Exception
     */
    public void offer(T t){
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        try {
            lock.lock();
            if(offBefore()) {
                List<String> children = framework.getChildren(nodes, null);
                if(children != null && children.size() >= maxQueueSize) {
                    //检测第一个节点是否删除，如果已经删除，则通知节点释放资源
                    framework.existed(nodes + "/" + children.get(0), watchedEvent -> {
                        if (watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                            lock.lock();
                            condition.signal();
                            lock.unlock();
                        }
                    });
                    //等待，如果第一个节点被删掉了，就马上激活，并创建节点，然后移除off节点，唤醒所有等待的offer线程
                    condition.await();
                }

                //创建子节点
                framework.getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground((curatorFramework, curatorEvent) -> {
                    if(curatorEvent.getType() == CuratorEventType.CREATE) {
                        curatorFramework.delete().forPath(offBefore);
                    }
                }).forPath(nodes  + prefix, ByteUtils.toByteArray(t));
                return;
            }

            //监听节点删除
            framework.existed(offBefore, watchedEvent -> {
                if(watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                    lock.lock();
                    condition.signal();
                    lock.unlock();
                }
            });
            condition.await();
            //回调，重复写
            offer(t);
        }catch (Exception e) {
            System.out.println("系统异常");
        }finally {
            lock.unlock();
        }
    }

    /**
     * 先创建临时节点，如果临时节点创建成功，表示轮到它写入数据
     * @return
     */
    private boolean pollBefore() {
        try {
            framework.getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(pollBefore, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取数据，等time长时间
     * @return
     * @throws Exception
     */
    public T poll() {
        //定义内部锁和事件通知
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        try {
            lock.lock();
            if(pollBefore()) {
                List<String> children = framework.getChildren(nodes, null);

                final List<String> resultChildren;
                if(children == null || children.size() == 0) {
                    //检测到子节点变更，此时一定是新增了节点，需要唤醒，释放调锁
                    framework.getChildren(nodes, watchedEvent -> {
                        if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                            lock.lock();
                            condition.signal();
                            lock.unlock();
                        }
                    });
                    //等待，如果创建了节点，就马上激活，并创建节点，然后移除poll，唤醒所有等待的poll的线程
                    condition.await();
                    resultChildren = framework.getChildren(nodes, null);
                }else {
                    resultChildren = children;
                }

                //返回结果
                T result = CastUtils.cast(ByteUtils.toObject(framework.getClient().getData().forPath(nodes + "/" + resultChildren.get(0))));
                //删除第一个节点，并返回
                framework.remove(nodes + "/" + resultChildren.get(0));
                framework.remove(pollBefore);

                return result;
            }

            framework.existed(pollBefore, watchedEvent -> {
                if(watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                    lock.lock();
                    condition.signal();
                    lock.unlock();
                }
            });

            //等待
            condition.await();
            return poll();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("系统异常");
            return null;
        }finally {
            lock.unlock();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        ZookeeperClientFactory.init(new RpcServiceZookeeperProperties());
        ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();

        BlockQueueTest<String> test = new BlockQueueTest<>(framework);
        for(int i = 0; i < 10; i++) {
            new Thread(() -> {
                test.offer(String.valueOf(new Random().nextInt(100)));
            }).start();
        }

        for(int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(test.poll());
            }).start();
        }

        Thread.sleep(1000000);
    }
}
