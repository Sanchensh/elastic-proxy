package com.mrxu.netty.timer;

import com.mrxu.netty.thread.DefaultThreadFactory;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.mrxu.common.Constants.APPLICATION_NAME;
import static com.mrxu.common.Constants.DEFAULT_TICK_DURATION;

@Slf4j
public class TimerHolder {
    private final static ConcurrentMap<String, Timeout> scheduledFutures = PlatformDependent.newConcurrentHashMap();

    private static class DefaultInstance {
        static final Timer INSTANCE = new HashedWheelTimer(DefaultThreadFactory.create(APPLICATION_NAME , true), DEFAULT_TICK_DURATION, TimeUnit.MILLISECONDS);
    }

    private TimerHolder() {
    }

    /**
     * Get a singleton instance of {@link Timer}. <br>
     * The tick duration is DEFAULT_TICK_DURATION
     *
     * @return Timer
     */
    private static Timer getTimer() {
        return DefaultInstance.INSTANCE;
    }

    public static void schedule(final String key, final Runnable runnable, long delay, TimeUnit unit) {
        Timeout timeout = getTimer().newTimeout(time -> {
            try {
                runnable.run();
            } finally {
                scheduledFutures.remove(key);
            }
        }, delay, unit);
        replaceScheduledFuture(key, timeout);
    }

    public static void stop(String key) {
        Timeout timeout = scheduledFutures.get(key);
        if (Objects.nonNull(timeout)) {
            timeout.cancel();
        }
        scheduledFutures.remove(key);
    }

    private static void replaceScheduledFuture(final String key, final Timeout newTimeout) {
        final Timeout oldTimeout;
        if (newTimeout.isExpired()) {
            // no need to put already expired timeout to scheduledFutures map.
            // simply remove old timeout
            oldTimeout = scheduledFutures.remove(key);
        } else {
            oldTimeout = scheduledFutures.put(key, newTimeout);
        }
        // if there was old timeout, cancel it
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }
}