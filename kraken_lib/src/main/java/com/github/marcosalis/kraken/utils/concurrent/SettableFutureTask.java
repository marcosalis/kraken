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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.annotations.Beta;

/**
 * Extension of a {@link FutureTask} that allows callers to directly set the
 * result of the computation.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class SettableFutureTask<E> extends FutureTask<E> {

	/**
	 * @see FutureTask#FutureTask(Runnable)
	 */
	public SettableFutureTask(@Nonnull Callable<E> callable) {
		super(callable);
	}

	/**
	 * @see FutureTask#FutureTask(Runnable, Object)
	 */
	public SettableFutureTask(@Nonnull Runnable runnable, @Nullable E result) {
		super(runnable, result);
	}

	/**
	 * Directly set the result value of this FutureTask. This method just
	 * exposes the {@link FutureTask#set(Object)} method.
	 */
	@Override
	public void set(E v) {
		// we allow callers to directly set the value of the task
		super.set(v);
	}

	/**
	 * Builds a new {@link FutureTask} that wraps the passed computation result.
	 * 
	 * @param wrapped
	 *            The result to wrap
	 * @return The built {@link FutureTask}
	 */
	@Nonnull
	public static <E> FutureTask<E> fromResult(@Nullable final E wrapped) {
		final SettableFutureTask<E> futureTask = new SettableFutureTask<E>(new Callable<E>() {
			@Override
			public E call() throws Exception {
				return wrapped;
			}
		});
		// explicitly set the result or the computation will hang
		// (doesn't happen when using a Runnable)
		futureTask.set(wrapped);
		return futureTask;
	}

}