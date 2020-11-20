package com.mrxu.netty.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.mrxu.netty.prop.ProxySearchProperties.WORKER_GROUP_SIZE;

public class ProxyThreadPool {
    private static final int defaultQueueSize = 100;
    private static final int defaultKeepAliveTime = 60;
    private static final BlockingQueue queue = new LinkedBlockingQueue(defaultQueueSize);
    //使用线程池将请求放入线程池中处理，服务器规格16c32g，则16<<6=1024
    private static final ThreadPoolExecutor ThreadPool = new ThreadPoolExecutor(
            WORKER_GROUP_SIZE << 2,
            WORKER_GROUP_SIZE << 4,
            defaultKeepAliveTime,
            TimeUnit.SECONDS,
            queue,
            ProxyThreadFactory.create("proxy", true));
    static {
        ThreadPool.allowCoreThreadTimeOut(true);
    }

    public static void submit(Runnable runnable){
        ThreadPool.submit(runnable);
    }
}
