/*
 * Copyright 2013 Luluvise Ltd
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

import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.common.annotations.Beta;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link ThreadFactory} implementation that allow callers to set a custom
 * thread Linux priority by calling
 * {@link android.os.Process#setThreadPriority(int)} on the created threads.
 * Check the {@link android.os.Process} documentation to find out which
 * priorities can be used.
 * 
 * It also allows to set a human-readable concise description of the Thread
 * being created (for example, the thread pool or executor name). The
 * incremental number of built threads is appended to the passed string.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class PriorityThreadFactory implements ThreadFactory {

	private static final String TAG = PriorityThreadFactory.class.getSimpleName();

	private final String mThreadsName;
	private final AtomicInteger mThreadCount = new AtomicInteger();

	private final int mPriority;

	/**
	 * Constructor that uses priority {@link Process#THREAD_PRIORITY_DEFAULT}
	 */
	public PriorityThreadFactory(@NonNull String threadName) {
		this(threadName, Process.THREAD_PRIORITY_DEFAULT);
	}

	/**
	 * Costructor that allows setting a custom priority to the created threads.
	 * 
	 * @param threadsName
	 *            Common string that identifies part of the name for all threads
	 *            created by the factory
	 * @param priority
	 *            The thread priority (must be one of the constants in
	 *            {@link android.os.Process})
	 */
	public PriorityThreadFactory(@NonNull String threadsName, int priority) {
		mThreadsName = threadsName;
		mPriority = priority;
	}

	/**
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(@NonNull Runnable r) {
		String name = mThreadsName + " #" + mThreadCount.incrementAndGet();
		final PrioritizableThread thread = new PrioritizableThread(r, name, mPriority);

		if (DroidConfig.DEBUG) {
			Log.v(TAG, "Creating thread: " + name + " with priority " + mPriority);
		}

		return thread;
	}

}