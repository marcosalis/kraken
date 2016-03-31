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
package com.github.marcosalis.kraken.utils.android;

import android.os.AsyncTask;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.annotations.Beta;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Small component that allows managing the lifecycle (execution and
 * cancellation) of multiple {@link ParallelAsyncTask}s at the same time.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public class ParallelAsyncTasksManager {

	@NonNull
	private final ArrayList<ParallelAsyncTask<?, ?, ?>> mManagedTasks;

	/**
	 * Constructor for a {@link ParallelAsyncTasksManager}
	 */
	public ParallelAsyncTasksManager() {
		mManagedTasks = new ArrayList<ParallelAsyncTask<?, ?, ?>>();
	}

	/**
	 * Adds a {@link ParallelAsyncTask} to the manager without executing it
	 * 
	 * @param task
	 *            The task to add to the manager
	 */
	public void addTask(@NonNull ParallelAsyncTask<?, ?, ?> task) {
		mManagedTasks.add(task);
	}

	/**
	 * Adds a {@link ParallelAsyncTask} to the manager and executes it.
	 * 
	 * @param task
	 *            The task to add to the manager and execute
	 * @param params
	 *            The params to pass to
	 *            {@link ParallelAsyncTask#parallelExec(Object...)}
	 * @return The executed {@link ParallelAsyncTask}
	 */
	@NonNull
	public <Params> ParallelAsyncTask<Params, ?, ?> addAndExecute(
			@NonNull ParallelAsyncTask<Params, ?, ?> task, @Nullable Params... params) {
		addTask(task);
		return (ParallelAsyncTask<Params, ?, ?>) task.parallelExec(params);
	}

	/**
	 * Adds a collection of {@link ParallelAsyncTask} to the manager without
	 * executing them.
	 * 
	 * @param tasks
	 *            A {@link Collection} of async tasks
	 */
	public void addAllTasks(@NonNull Collection<? extends ParallelAsyncTask<?, ?, ?>> tasks) {
		mManagedTasks.addAll(tasks);
	}

	/**
	 * Cancels all the tasks whose class is the same as the passed one. The
	 * comparison is made by equality with the tasks
	 * {@link ParallelAsyncTask#getClass()}.
	 * 
	 * Note that the tasks are cancelled but not removed from the manager.
	 * {@link #cancelAllTasks(boolean)} has still to be called.
	 * 
	 * @param taskClass
	 *            The {@link Class} of the tasks to cancel
	 * @param mayInterruptIfRunning
	 *            See {@link AsyncTask#cancel(boolean)}
	 * @return The number of tasks cancelled
	 */
	@IntRange(from=0)
	public <C> int cancelTask(@NonNull Class<? extends C> taskClass, boolean mayInterruptIfRunning) {
		int cancelledTasks = 0;
		for (ParallelAsyncTask<?, ?, ?> task : mManagedTasks) {
			if (task.getClass().equals(taskClass)) {
				task.cancel(mayInterruptIfRunning);
				cancelledTasks++;
			}
		}
		return cancelledTasks;
	}

	/**
	 * Cancels and removes all async tasks previously added to the manager.
	 * 
	 * @param mayInterruptIfRunning
	 *            See {@link AsyncTask#cancel(boolean)}
	 */
	public void cancelAllTasks(boolean mayInterruptIfRunning) {
		for (ParallelAsyncTask<?, ?, ?> task : mManagedTasks) {
			if (task != null) {
				task.cancel(mayInterruptIfRunning);
			}
		}
		mManagedTasks.clear();
	}

}