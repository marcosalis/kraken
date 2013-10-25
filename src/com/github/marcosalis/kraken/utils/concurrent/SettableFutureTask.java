/*
 * Copyright 2013 Luluvise Ltd
 * Copyright 2013 Marco Salis - fast3r@gmail.com
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
	 * Directly set the result value of this FutureTask. This method just makes
	 * the {@link FutureTask#set(Object)} method public.
	 */
	@Override
	public void set(E v) {
		// we allow callers to directly set the value of the task
		super.set(v);
	}

}