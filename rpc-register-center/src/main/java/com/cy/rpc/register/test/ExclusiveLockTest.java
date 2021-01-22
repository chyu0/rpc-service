package com.cy.rpc.register.test;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenyu3
 * 排他锁测试
 *
 * 排他锁：所有任务互斥，也叫写锁
 */
public class ExclusiveLockTest {

    private final ServiceCuratorFramework framework;

    private static final String path = "/exclusiveLock";

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    public ExclusiveLockTest(ServiceCuratorFramework framework) {
        this.framework = framework;
    }


    private boolean lock() {
        try {
            framework.getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            return true;
        }catch (Exception e){
            //加节点失败，需要阻塞
            return false;
        }
    }

    /**
     * 获取锁
     */
    private void acquired() {
        try {
            lock.lock();

            boolean exist = framework.existed(path, watchedEvent -> {
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

    /**
     *     释放锁
     */
    private void release() {
        framework.remove(path);
    }


    /**
     * 测试
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        ZookeeperClientFactory.init(new RpcServiceZookeeperProperties());
        ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();

        for(int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(()  -> {
                ExclusiveLockTest test = new ExclusiveLockTest(framework);
                try {
                    long sleep = new Random().nextInt(1000);
                    //互斥
                    while (!test.lock()) {
                        test.acquired();
                    }
                    System.out.println(System.currentTimeMillis() + "-----> "+ finalI +" 开始执行！" + sleep + ", ");
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
