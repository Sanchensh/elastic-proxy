package com.mrxu.netty.thread;

import com.mrxu.netty.property.PropertiesUtil;

import java.util.concurrent.*;

import static com.mrxu.common.Constants.*;


public class ProxyThreadPool {
    private static final ProxyRejectHandler handler = new ProxyRejectHandler();
    private static final BlockingQueue queue = new LinkedBlockingQueue(DEFAULT_QUEUE_SIZE);
    //使用线程池将请求放入线程池中处理，服务器规格16c32g，则16<<6=1024
    public static final ThreadPoolExecutor ThreadPool = new ThreadPoolExecutor(
            PropertiesUtil.properties.getCoreThread(),
            PropertiesUtil.properties.getMaxThread(),
            DEFAULT_KEEPALIVE_TIME,
            TimeUnit.SECONDS,
            queue,
            DefaultThreadFactory.create(APPLICATION_NAME, true), handler);

    static {
        ThreadPool.allowCoreThreadTimeOut(true);
    }

    private static class ProxyRejectHandler implements RejectedExecutionHandler {
        //重新执行
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            r.run();
        }
    }
}
