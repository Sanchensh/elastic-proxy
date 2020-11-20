package com.mrxu.netty.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.mrxu.netty.prop.ProxySearchProperties.WORKER_GROUP_SIZE;

public class TimerThreadPool {
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(WORKER_GROUP_SIZE << 2,
            ProxyThreadFactory.create("proxy", true));

    public static ScheduledFuture submit(Runnable runnable, long time, TimeUnit timeUnit) {
        return service.schedule(runnable, time, timeUnit);
    }
}
