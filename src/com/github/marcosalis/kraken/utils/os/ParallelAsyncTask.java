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
package com.github.marcosalis.kraken.utils.os;

import java.util.concurrent.Executor;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import android.annotation.TargetApi;
import android.os.AsyncTask;

import com.google.common.annotations.Beta;

/**
 * Extension of {@link AsyncTask} that forces parallel tasks execution in
 * Honeycomb and higher Android versions by calling
 * {@link #executeOnExecutor(Executor, Object[])} when using API <= 13.
 * 
 * In fact, as specified by AsyncTask documentation, starting from Honeycomb the
 * tasks execution is performed in a single-threaded executor:
 * 
 * <code>
 * <h2>Order of execution</h2>
 * <p>When first introduced, AsyncTasks were executed serially on a single background
 * thread. Starting with {@link android.os.Build.VERSION_CODES#DONUT}, this was changed
 * to a pool of threads allowing multiple tasks to operate in parallel. Starting with
 * {@link android.os.Build.VERSION_CODES#HONEYCOMB}, tasks are executed on a single
 * thread to avoid common application errors caused by parallel execution.</p>
 * <p>If you truly want parallel execution, you can invoke
 * {@link #executeOnExecutor(java.util.concurrent.Executor, Object[])} with
 * {@link #THREAD_POOL_EXECUTOR}.</p>
 * </code>
 * 
 * The client must use the {@link #parallelExec(Object...)} instead of
 * {@link AsyncTask#execute(Object...)} otherwise an exception will be thrown.
 * 
 * @see AsyncTask
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public abstract class ParallelAsyncTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	private static final int SDK_INT = android.os.Build.VERSION.SDK_INT;

	@GuardedBy("this")
	// always accessed in the UI thread
	private boolean mParallelExec;

	/**
	 * Use this instead of {@link AsyncTask#execute(Object...)} to ensure
	 * multiple tasks are executed in the parallel
	 * {@link AsyncTask#THREAD_POOL_EXECUTOR} starting from Honeycomb on.
	 * 
	 * If the task is started with execute, an exception will be thrown in
	 * {@link #onPreExecute()}.
	 */
	public final AsyncTask<Params, Progress, Result> parallelExec(Params... params) {
		mParallelExec = true;
		if (SDK_INT < 11) { // android.os.Build.VERSION_CODES.HONEYCOMB = 11
			return execute(params);
		} else { // Honeycomb and later
			return honeycombExecute(params);
		}
	}

	@TargetApi(11)
	private AsyncTask<Params, Progress, Result> honeycombExecute(Params... params) {
		return executeOnExecutor(THREAD_POOL_EXECUTOR, params);
	}

	/**
	 * Always call back to the superclass when overriding.
	 * 
	 * @throws IllegalArgumentException
	 *             if the caller has used the {@link #execute(Object...)} method
	 *             instead of {@link #parallelExec(Object...)}.
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void onPreExecute() {
		if (!mParallelExec) {
			throw new IllegalStateException("Task not started with parallel executor");
		}
	}

}