package com.lld.im.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ClassName: ShareThreadPool
 * Package: com.lld.im.service.utils
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/5 上午10:08
 * Version 1.0
 */
@Service
public class ShareThreadPool {

    private Logger logger = LoggerFactory.getLogger(ShareThreadPool.class);

    private final ThreadPoolExecutor threadPoolExecutor;

    // 初始化代码块，类加载时执行
    {
        final AtomicInteger tNum = new AtomicInteger(0); // 用于生成线程名的计数器

        // 创建线程池实例
        threadPoolExecutor = new ThreadPoolExecutor(
                8,  // 核心线程数：始终保持的线程数量
                8,  // 最大线程数：核心线程满了之后，最多再创建的线程数（这里和核心线程数相同，说明是固定大小的线程池）
                120,  // 空闲线程存活时间：超过核心线程数的线程，空闲120秒后会被销毁
                TimeUnit.SECONDS,  // 时间单位：秒
                new LinkedBlockingQueue<>(2 << 20),  // 任务队列：容量为 2^21（约200万），用于存放等待执行的任务
                new ThreadFactory() {  // 线程工厂：用于创建线程
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setDaemon(true);  // 设置为守护线程（进程结束时自动销毁，不阻塞程序退出）
                        t.setName("SHARE-Processor-" + tNum.getAndIncrement());  // 线程名：带自增编号，方便日志追踪
                        return t;
                    }
                }
        );
    }
    private AtomicLong ind = new AtomicLong(0);

    public void submit(Runnable r) {
        // 获取当前线程的调用栈（用于记录任务来源，方便排查慢任务）
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        ind.incrementAndGet();  // 原子计数器：记录当前正在执行的任务数（+1）

        // 向线程池提交任务
        threadPoolExecutor.submit(() -> {
            long start = System.currentTimeMillis();  // 记录任务开始时间
            try {
                r.run();  // 执行任务（r是外部传入的业务逻辑）
            } catch (Exception e) {
                logger.error("ShareThreadPool_ERROR", e);  // 捕获任务执行中的异常
            } finally {
                long end = System.currentTimeMillis();
                long dur = end - start;  // 计算任务执行耗时
                long remanent = ind.decrementAndGet();  // 任务完成，计数器-1，得到剩余任务数

                // 根据耗时打印不同级别日志，监控慢任务
                if (dur > 1000) {  // 耗时>1秒：严重警告
                    logger.warn("慢任务警告，剩余任务数={}，耗时={}ms，调用栈={}", remanent, dur, stackTrace);
                } else if (dur > 300) {  // 耗时>300ms：普通警告
                    logger.warn("慢任务提醒，剩余任务数={}，任务={}，耗时={}ms", remanent, r, dur);
                } else {  // 正常耗时：调试日志
                    logger.debug("任务完成，剩余任务数={}", remanent);
                }
            }
        });
    }

}
