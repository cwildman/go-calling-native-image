package com.chriswildman;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;

import java.util.concurrent.atomic.AtomicInteger;

public class Native {
    public static AtomicInteger COUNT = new AtomicInteger(0);

    @CEntryPoint(name = "increment")
    public static int increment(final IsolateThread isolate) {
        return COUNT.addAndGet(1);
    }

    @CEntryPoint(name = "get")
    public static int get(final IsolateThread thread) {
        return COUNT.get();
    }
}