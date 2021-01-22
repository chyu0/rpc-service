package com.cy.rpc.register.test;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author chenyu3
 * 共享锁测试
 *
 * 共享锁：读取不互斥，写时互斥
 */
public class ShareLockTest {

    private final ServiceCuratorFramework framework;

    private static final String path = "/shareLock";

    private String currentPath;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    public ShareLockTest(ServiceCuratorFramework framework) {
        this.framework = framework;
    }

    /**
     * 加锁
     * @param read 是否是读
     * @return
     * @throws Exception
     */
    private void lock(boolean read) throws Exception {
        String node = framework.getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path + "/" + read);
        currentPath = node.substring(path.length() + 1);
    }

    /**
     * 获取锁
     */
    private void acquired() {
        try {
            lock.lock();

            List<String> nodes = framework.getChildren(path, null);
            System.out.println(nodes);
            String writeNode;
            //如果是第一个节点，直接返回
            int index = nodes.indexOf(currentPath);
            if(index == 0) {
                return ;
            }

            List<String> writes;
            //表示节点是读节点
            if(currentPath.contains("true")) {
                //当前节点之前的最后一个写节点
                writes = nodes.stream().limit(index).filter(item -> item.contains("false")).collect(Collectors.toList());
            }else {
                //当前节点之前的最后一个写节点
                writes = nodes.stream().limit(index).collect(Collectors.toList());
            }

            //获取最后一个节点
            if (!CollectionUtils.isEmpty(writes)) {
                writeNode = writes.get(writes.size() - 1);
            }else {
                return ;
            }

            //等待
            boolean exist = framework.existed(path + "/" + writeNode, watchedEvent -> {
                lock.lock();
                if(watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                    condition.signal();
                }
                lock.unlock();
            });

            if(exist) {
                condition.await();
            }
        }catch (Exception e){
            System.out.println("系统异常");
        }finally {
            lock.unlock();
        }
    }

    //释放锁
    private void release() {
        framework.remove(path + "/" + currentPath);
    }


    //测试
    public static void main(String[] args) throws InterruptedException {
        ZookeeperClientFactory.init(new RpcServiceZookeeperProperties());
        ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();

        for(int i = 0; i < 10; i++) {
            new Thread(() -> {
                ShareLockTest test = new ShareLockTest(framework);
                try{
                    long sleep = new Random().nextInt(1000);
                    boolean read = sleep % 2 == 0;
                    test.lock(read);
                    test.acquired();
                    System.out.println(System.currentTimeMillis() + "----->" + test.currentPath + "开始执行"+ read +"！" + sleep + ", ");
                    Thread.sleep(sleep);
                }catch (Exception e) {
                    System.out.println("系统异常");
                }finally {
                    test.release();
                }

            }).start();
        }

        Thread.sleep(100000000);
    }

}
