/*
 * Copyright 2013 Luluvise Ltd
 * Copyright 2013 Marco Salis - fast3r(at)gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.marcosalis.kraken.utils.concurrent;

import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;

import com.google.common.annotations.Beta;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Extension of {@link Thread} that allows setting a Linux thread priority to this thread by calling
 * {@link android.os.Process#setThreadPriority(int)} in its {@link #run()} method.
 *
 * Part of the implementation is taken from Android {@link HandlerThread}.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@NotThreadSafe
public class PrioritizableThread extends Thread {

    protected final int mPriority;
    private volatile int mTid = -1;

    /**
     * Instantiate a thread with priority {@link Process#THREAD_PRIORITY_DEFAULT}
     */
    public PrioritizableThread(@NonNull Runnable runnable, @NonNull String threadName) {
        this(runnable, threadName, Process.THREAD_PRIORITY_DEFAULT);
    }

    /**
     * Instantiate a thread with the specified priority.
     */
    public PrioritizableThread(@NonNull Runnable runnable, @NonNull String threadName, int priority) {
        super(runnable, threadName);
        mPriority = priority;
    }

    @Override
    public void run() {
        mTid = Process.myTid();
        Process.setThreadPriority(mPriority);
        super.run();
    }

    /**
     * Returns the identifier of this thread. Call this only after the thread has been started.
     *
     * @return The thread ID or -1 if the thread hasn't been started
     * @see {@link Process#myTid()}
     */
    public int getThreadId() {
        return mTid;
    }

}