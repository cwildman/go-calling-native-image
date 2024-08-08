package com.chriswildman;

import com.oracle.svm.core.Uninterruptible;
import com.oracle.svm.core.c.function.CEntryPointActions;
import com.oracle.svm.core.c.function.CEntryPointOptions;
import com.oracle.svm.core.util.VMError;
import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.c.function.CEntryPoint;

import java.util.concurrent.atomic.AtomicInteger;

public class Native {
    public static AtomicInteger COUNT = new AtomicInteger(0);

    @Uninterruptible(reason = "unsafe state in case of failure", calleeMustBe = false)
    @CEntryPoint(name = "increment")
    @CEntryPointOptions(prologue = CEntryPointOptions.NoPrologue.class, epilogue = CEntryPointOptions.NoEpilogue.class)
    public static int increment(final Isolate isolate) {
        int status = getOrAttachAndEnter(isolate);
        if (status != 0) {
            return status;
        }

        COUNT.addAndGet(1);

        return CEntryPointActions.leave();
    }

    @Uninterruptible(reason = "unsafe state in case of failure", calleeMustBe = false)
    @CEntryPoint(name = "get")
    @CEntryPointOptions(prologue = CEntryPointOptions.NoPrologue.class, epilogue = CEntryPointOptions.NoEpilogue.class)
    public static int get(final Isolate isolate) {
        int status = getOrAttachAndEnter(isolate);
        if (status != 0) {
            return status;
        }

        int rv = COUNT.get();
        CEntryPointActions.leave();
        return rv;
    }

    @Uninterruptible(reason = "unsafe state in case of failure", calleeMustBe = false)
    private static int getOrAttachAndEnter(final Isolate isolate) {
        try {
            final boolean attached = CEntryPointActions.isCurrentThreadAttachedTo(isolate);
            int status;
            if (!attached) {
                status = CEntryPointActions.enterAttachThread(isolate, false, true);
            } else {
                status = CEntryPointActions.enterByIsolate(isolate);
            }
            return status;
        } catch (final Throwable t) {
            throw VMError.shouldNotReachHere(t);
        }
    }
}