package com.otn.service.utils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 用于service层DML批量操作的一些接口
 * 自适应多线程
 */
public class BatchDMLUtils {

    private final static int MULTI_THREAD_LIMIT = 2000;

    /**
     * 批量插入函数
     * 根据给定的action执行批量操作，自动选择是否使用多线程模式
     * 返回值为执行的条目数量
     * 注意这个函数一定会等待到所有条目执行完再返回
     * 目的只是单纯的多线程执行
     *
     * @param list
     * @param action
     * @param <K>
     * @return
     */
    static public <K> int batchDMLAction(final List<K> list, final Consumer<K> action) throws InterruptedException {
        if (list.size() <= MULTI_THREAD_LIMIT) {
            list.forEach(action::accept);
        } else {
            final ExecutorService pool = Executors.newWorkStealingPool(Runtime.getRuntime()
                    .availableProcessors() * 3);
            CountDownLatch count = new CountDownLatch(list.size());
            list.parallelStream().forEach((item) -> pool.execute(() -> {
                action.accept(item);
                count.countDown();
            }));
            count.await();
        }
        return list.size();
    }
}
