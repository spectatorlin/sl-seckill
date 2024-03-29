package com.seckill;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.*;

/**
 * @author 邹松林
 * @version 1.0
 * @Title: ThreadPoolDemo
 * @Description: TODO
 * @date 2023/10/29 15:21
 */
public class ThreadPoolDemo {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);
        ExecutorService executorService2 = Executors.newCachedThreadPool();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 20,
                5, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(10),
                new DefaultThreadFactory("sl"),
                new ThreadPoolExecutor.DiscardPolicy());
    }
}
