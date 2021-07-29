package com.mrxu.netty.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static com.mrxu.common.Constants.APPLICATION_NAME;
import static com.mrxu.common.Constants.SHORT_LINE;

public class DefaultThreadFactory implements ThreadFactory {

    private static final ThreadGroup threadGroup = new ThreadGroup(APPLICATION_NAME);

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final String namePrefix;

    private final boolean daemon;

    public static ThreadFactory create(String namePrefix, boolean daemon) {
        return new DefaultThreadFactory(namePrefix, daemon);
    }
    private DefaultThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup,r,threadGroup.getName() + SHORT_LINE + namePrefix + SHORT_LINE + threadNumber.getAndIncrement());
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        thread.setDaemon(daemon);
        return thread;
    }
}
