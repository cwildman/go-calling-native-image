package com.chriswildman;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;

public class Native {
    public static int COUNT = 0;

    @CEntryPoint(name = "do_stuff")
    public static int doStuff(final IsolateThread thread) {
        return ++COUNT;
    }
}