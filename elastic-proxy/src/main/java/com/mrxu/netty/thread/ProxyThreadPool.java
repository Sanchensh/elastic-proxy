package com.mrxu.netty.thread;

import com.mrxu.netty.prop.PropertiesUtil;

import java.util.concurrent.*;


public class ProxyThreadPool {
    private static final int defaultQueueSize = 100;
    private static final int defaultKeepAliveTime = 60;
    private static final ProxyRejectHandler handler = new ProxyRejectHandler();
    private static final BlockingQueue queue = new LinkedBlockingQueue(defaultQueueSize);
    //使用线程池将请求放入线程池中处理，服务器规格16c32g，则16<<6=1024
    public static final ThreadPoolExecutor ThreadPool = new ThreadPoolExecutor(
            PropertiesUtil.properties.getCoreThread(),
            PropertiesUtil.properties.getMaxThread(),
            defaultKeepAliveTime,
            TimeUnit.SECONDS,
            queue,
            DefaultThreadFactory.create("proxy", true),handler);
    static {
        ThreadPool.allowCoreThreadTimeOut(true);
    }

    private static class ProxyRejectHandler implements RejectedExecutionHandler{
        //重新执行
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            r.run();
        }
    }
}
