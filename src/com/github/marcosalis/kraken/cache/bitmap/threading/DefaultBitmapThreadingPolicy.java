/*
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
package com.github.marcosalis.kraken.cache.bitmap.threading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

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
public final class DefaultBitmapThreadingPolicy implements BitmapThreadingPolicy {

	private final ThreadPoolExecutor mBitmapDiskExecutor;
	private final ReorderingThreadPoolExecutor<String> mDownloaderExecutor;

	public DefaultBitmapThreadingPolicy() {
		mBitmapDiskExecutor = buildDiskExecutor();
		mDownloaderExecutor = buildDownloader();
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

	@Nonnull
	private ThreadPoolExecutor buildDiskExecutor() {
		// here we query memory and disk caches and decode bitmaps
		final int executorSize = DroidUtils.getCpuBoundPoolSize() + 1;

		final LinkedBlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
		// priority here is less than default to face decoding overhead
		final PriorityThreadFactory executorFactory = new PriorityThreadFactory(
				"Bitmap caches disk executor thread", Process.THREAD_PRIORITY_BACKGROUND);
		final ThreadPoolExecutor diskExecutor = new ThreadPoolExecutor(executorSize, executorSize,
				0L, TimeUnit.MILLISECONDS, executorQueue, executorFactory);

		return diskExecutor;
	}

	@Nonnull
	private ReorderingThreadPoolExecutor<String> buildDownloader() {
		// here we either execute a network request or we wait for it
		final int dwExecutorSize = DroidUtils.getIOBoundPoolSize();

		final BlockingQueue<Runnable> downloaderQueue = ReorderingThreadPoolExecutor
				.createBlockingQueue();
		final PriorityThreadFactory downloaderFactory = new PriorityThreadFactory(
				"Bitmap caches downloader executor thread", Process.THREAD_PRIORITY_DEFAULT);
		final ReorderingThreadPoolExecutor<String> downloaderExecutor = new ReorderingThreadPoolExecutor<String>(
				dwExecutorSize, dwExecutorSize, 0L, TimeUnit.MILLISECONDS, downloaderQueue,
				downloaderFactory);

		return downloaderExecutor;
	}

}