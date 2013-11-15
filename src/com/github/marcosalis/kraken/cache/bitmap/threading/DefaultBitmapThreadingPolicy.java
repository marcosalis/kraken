/*
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
package com.github.marcosalis.kraken.cache.bitmap.threading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import android.os.Process;

import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.concurrent.PriorityThreadFactory;
import com.github.marcosalis.kraken.utils.concurrent.ReorderingThreadPoolExecutor;

/**
 * Default recommended implementation of {@link BitmapThreadingPolicy}.
 * 
 * The thread pools are sized depending on the number of physical cores of the
 * current device.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@ThreadSafe
public final class DefaultBitmapThreadingPolicy implements BitmapThreadingPolicy {

	private final ThreadPoolExecutor mBitmapDiskExecutor;
	private final ReorderingThreadPoolExecutor<String> mDownloaderExecutor;

	public DefaultBitmapThreadingPolicy() {
		mBitmapDiskExecutor = buildDefaultDiskExecutor(getDefaultDiskExecutorSize(),
				Process.THREAD_PRIORITY_BACKGROUND);
		mDownloaderExecutor = buildDefaultDownloader(getDefaultDownloaderSize(),
				Process.THREAD_PRIORITY_DEFAULT);
	}

	@Nonnull
	@Override
	public ThreadPoolExecutor getBitmapDiskExecutor() {
		return mBitmapDiskExecutor;
	}

	@Nonnull
	@Override
	public ThreadPoolExecutor getBitmapDownloader() {
		return mDownloaderExecutor;
	}

	public static final int getDefaultDiskExecutorSize() {
		// here we query memory and disk caches and decode bitmaps
		return DroidUtils.getCpuBoundPoolSize() + 1;
	}

	public static final int getDefaultDownloaderSize() {
		// here we either execute a network request or we wait for it
		return DroidUtils.getIOBoundPoolSize();
	}

	@Nonnull
	static final ThreadPoolExecutor buildDefaultDiskExecutor(int executorSize, int priority) {
		final LinkedBlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
		// priority here is less than default to face decoding overhead
		final PriorityThreadFactory executorFactory = new PriorityThreadFactory(
				"Bitmap caches disk executor thread", priority);
		final ThreadPoolExecutor diskExecutor = new ThreadPoolExecutor(executorSize, executorSize,
				0L, TimeUnit.MILLISECONDS, executorQueue, executorFactory);

		return diskExecutor;
	}

	@Nonnull
	static final ReorderingThreadPoolExecutor<String> buildDefaultDownloader(int executorSize,
			int priority) {
		final BlockingQueue<Runnable> downloaderQueue = ReorderingThreadPoolExecutor
				.createBlockingQueue();
		final PriorityThreadFactory downloaderFactory = new PriorityThreadFactory(
				"Bitmap caches downloader executor thread", priority);
		final ReorderingThreadPoolExecutor<String> downloaderExecutor = new ReorderingThreadPoolExecutor<String>(
				executorSize, executorSize, 0L, TimeUnit.MILLISECONDS, downloaderQueue,
				downloaderFactory);

		return downloaderExecutor;
	}

}