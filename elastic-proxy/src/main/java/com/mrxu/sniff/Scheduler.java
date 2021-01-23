package com.mrxu.sniff;

import java.util.concurrent.Future;

public interface Scheduler {
        /**
         * Schedules the provided {@link Runnable} to be executed in <code>delayMillis</code> milliseconds
         */
        Future<?> schedule(Runnable task, long delayMillis);

        /**
         * Shuts this scheduler down
         */
        void shutdown();
    }