package com.mrxu.sniff;

import com.mrxu.netty.thread.DefaultThreadFactory;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultScheduler implements Scheduler {
    private static final String sniffer_thread_name = "es_rest_client_sniffer"; //线程池名称
    final ScheduledExecutorService executor;
    public DefaultScheduler() {
        this.executor = initScheduledExecutorService();
    }

    private static ScheduledExecutorService initScheduledExecutorService() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, DefaultThreadFactory.create(sniffer_thread_name,true));
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    @Override
    public Future<?> schedule(Runnable task, long delayMillis) {
        return executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                return;
            }
            executor.shutdownNow();
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}