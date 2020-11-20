package com.mrxu;

import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DirectMemoryReporter {
    private static final int _1K = 1024;
    private static final String BUSINESS_KEY = "netty_direct_memory";

    private AtomicLong directMemory;

    private DirectMemoryReporter() {
        init();
    }

    private static DirectMemoryReporter directMemoryReporter;

    public static DirectMemoryReporter getInstance() {
        if (directMemoryReporter == null) {
            synchronized (DirectMemoryReporter.class) {
                if (directMemoryReporter == null) {
                    directMemoryReporter = new DirectMemoryReporter();
                }
            }
        }
        return directMemoryReporter;
    }

    private void init() {
        Field field = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER");
        field.setAccessible(true);

        try {
            directMemory = (AtomicLong) field.get(PlatformDependent.class);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void startReport() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::doReport, 0, 5, TimeUnit.SECONDS);
    }

    private void doReport() {
        int memoryInkb = (int) (directMemory.get() / _1K);
        log.info("{}: {} k", BUSINESS_KEY, memoryInkb);
    }

    public long getDirectMemory() {
        return directMemory.get() / _1K;
    }

}