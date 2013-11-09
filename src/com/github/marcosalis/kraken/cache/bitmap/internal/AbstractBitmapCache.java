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
package com.github.marcosalis.kraken.cache.bitmap.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.ContentCache.OnEntryRemovedListener;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapMemoryCache;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.loaders.AccessPolicy;
import com.github.marcosalis.kraken.content.AbstractContentProxy;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.github.marcosalis.kraken.utils.concurrent.PriorityThreadFactory;
import com.github.marcosalis.kraken.utils.concurrent.ReorderingThreadPoolExecutor;
import com.github.marcosalis.kraken.utils.concurrent.SettableFutureTask;
import com.google.common.annotations.Beta;

/**
 * Abstract base class for a {@link Bitmap} content proxy. Every
 * {@link AbstractBitmapCache} subclass can hold one or more (in order to be
 * able to fine-tune the size of each of them) memory caches and a disk cache,
 * which are managed separately.
 * 
 * Every {@link AbstractBitmapCache} shares the same executors, one for querying
 * the cache for a Bitmap (memory and disk I/O can be required) and another for
 * executing image download requests without preventing the other cache requests
 * to block. The executor maximum thread pool size varies depending on the
 * number of CPU cores in the device.
 * 
 * The only {@link AccessPolicy} allowed for Bitmap content proxies are:<br>
 * {@link AccessPolicy#NORMAL}, {@link AccessPolicy#PRE_FETCH}
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public abstract class AbstractBitmapCache extends AbstractContentProxy implements BitmapCache,
		OnEntryRemovedListener<String, Bitmap> {

	static {
		// here we query memory and disk caches and set bitmaps into views
		final int executorSize = DroidUtils.getCpuBoundPoolSize() + 1;
		// here we either execute a network request or we wait for it
		final int dwExecutorSize = DroidUtils.getIOBoundPoolSize();

		// prepare bitmaps main executor
		final LinkedBlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
		final PriorityThreadFactory executorFactory = new PriorityThreadFactory(
				"BitmapProxy executor thread", Process.THREAD_PRIORITY_BACKGROUND);
		final ThreadPoolExecutor bitmapExecutor = new ThreadPoolExecutor(executorSize,
				executorSize, 0L, TimeUnit.MILLISECONDS, executorQueue, executorFactory);
		BITMAP_EXECUTOR = bitmapExecutor;

		// prepare bitmaps downloader executor
		final BlockingQueue<Runnable> downloaderQueue = ReorderingThreadPoolExecutor
				.createBlockingQueue();
		final PriorityThreadFactory downloaderFactory = new PriorityThreadFactory(
				"BitmapProxy downloader executor thread", Process.THREAD_PRIORITY_DEFAULT);
		final ReorderingThreadPoolExecutor<String> downloaderExecutor = new ReorderingThreadPoolExecutor<String>(
				dwExecutorSize, dwExecutorSize, 0L, TimeUnit.MILLISECONDS, downloaderQueue,
				downloaderFactory);
		DOWNLOADER_EXECUTOR = downloaderExecutor;

		BITMAP_EXECUTOR_Q = executorQueue;
		DOWNLOADER_EXECUTOR_Q = downloaderQueue;
	}

	private static final String TAG = AbstractBitmapCache.class.getSimpleName();

	private static final ThreadPoolExecutor BITMAP_EXECUTOR;
	private static final ReorderingThreadPoolExecutor<String> DOWNLOADER_EXECUTOR;

	/* private executors blocking queues */
	private static final LinkedBlockingQueue<Runnable> BITMAP_EXECUTOR_Q;
	private static final BlockingQueue<Runnable> DOWNLOADER_EXECUTOR_Q;

	/**
	 * Executes a runnable task in the bitmap downloader thread pool.
	 * 
	 * @param runnable
	 *            The {@link Runnable} to execute (must be non null)
	 */
	static synchronized final void executeInDownloader(@Nonnull Runnable runnable) {
		DOWNLOADER_EXECUTOR.execute(runnable);
	}

	/**
	 * Executes a callable task in the bitmap downloader thread pool.
	 * 
	 * @param callable
	 *            The {@link Callable} to execute (must be non null)
	 */
	static synchronized final Future<Bitmap> submitInDownloader(@Nonnull Callable<Bitmap> callable) {
		return DOWNLOADER_EXECUTOR.submit(callable);
	}

	/**
	 * Executes a callable task in the bitmap downloader thread pool.
	 * 
	 * @param key
	 *            The key to associate with the submitted task
	 * @param callable
	 *            The {@link Callable} to execute (must be non null)
	 */
	static synchronized final Future<Bitmap> submitInDownloader(@Nonnull String key,
			@Nonnull Callable<Bitmap> callable) {
		return DOWNLOADER_EXECUTOR.submitWithKey(key, callable);
	}

	/**
	 * Attempts to prioritize a bitmap download by moving to the top of the
	 * executor queue the task with the passed key, if it exists.
	 * 
	 * @param key
	 *            The string key corresponding to the bitmap
	 */
	@NotForUIThread
	static synchronized final void moveDownloadToFront(@Nonnull String key) {
		DOWNLOADER_EXECUTOR.moveToFront(key);
	}

	/**
	 * Remove all not-running tasks from all static bitmap executors.
	 */
	public static synchronized final void clearBitmapExecutors() {
		BITMAP_EXECUTOR_Q.clear();
		DOWNLOADER_EXECUTOR_Q.clear();
		DOWNLOADER_EXECUTOR.clearKeysMap();

		if (DroidConfig.DEBUG) {
			BitmapLoader.clearStatsLog();
			Log.d(TAG, "Bitmap executors tasks cleared");
			Log.v(TAG, "Bitmap executor tasks: " + BITMAP_EXECUTOR.getTaskCount() + ", completed: "
					+ BITMAP_EXECUTOR.getCompletedTaskCount());
			Log.v(TAG, "Bitmap downloader executor tasks: " + DOWNLOADER_EXECUTOR.getTaskCount()
					+ ", completed: " + DOWNLOADER_EXECUTOR.getCompletedTaskCount());
		}
	}

	/**
	 * {@link Memoizer} used for loading Bitmaps from the cache.
	 */
	private final Memoizer<String, Bitmap> mBitmapMemoizer;

	protected AbstractBitmapCache() {
		final int concurrencyLevel = DOWNLOADER_EXECUTOR.getMaximumPoolSize();
		mBitmapMemoizer = new Memoizer<String, Bitmap>(concurrencyLevel);
	}

	@Override
	public void onEntryRemoved(boolean evicted, String key, Bitmap value) {
		// remove evicted bitmaps from the downloads memoizer to allow GC
		mBitmapMemoizer.remove(key);
	}

	/**
	 * Method to be called by subclasses to get a bitmap content from any
	 * {@link BitmapCache} by passing the main request parameters and type of
	 * actions.
	 * 
	 * <b>This needs to be called from the UI thread except when using
	 * {@link AccessPolicy#PRE_FETCH} mode</b>, as the image setting is
	 * asynchronous except in the case we already have the image available in
	 * the memory cache.
	 * 
	 * @param cache
	 *            The {@link BitmapMemoryCache} memory cache to use
	 * @param diskCache
	 *            The {@link BitmapDiskCache} to use
	 * @param url
	 *            The {@link CacheUrlKey} of the image to retrieve
	 * @param action
	 *            The {@link AccessPolicy} to perform, one of
	 *            {@link AccessPolicy#NORMAL} or {@link AccessPolicy#PRE_FETCH}
	 * @param setter
	 *            The {@link BitmapAsyncSetter} to set the bitmap in an
	 *            {@link ImageView}. Can be null with
	 *            {@link AccessPolicy#PRE_FETCH}
	 * @param placeholder
	 *            An (optional) {@link Drawable} temporary placeholder, only set
	 *            if the bitmap is not in the memory cache
	 * @return The {@link Future} that holds the Bitmap loading
	 */
	@CheckForNull
	protected final Future<Bitmap> getBitmap(@Nonnull BitmapMemoryCache<String> cache,
			@Nullable BitmapDiskCache diskCache, @Nonnull CacheUrlKey url,
			@Nullable AccessPolicy action, @Nullable BitmapAsyncSetter setter,
			@CheckForNull Drawable placeholder) {
		final boolean preFetch = (action == AccessPolicy.PRE_FETCH);
		Bitmap bitmap;

		if ((bitmap = cache.get(url.hash())) != null) {
			// cache hit at the very first attempt, no other actions needed
			if (!preFetch && setter != null) {
				// set Bitmap if we are not just pre-fetching
				/*
				 * This is supposed to be called from the UI thread and be
				 * synchronous.
				 */
				setter.setBitmapSync(url, bitmap);
			}
			return SettableFutureTask.fromResult(bitmap);
		} else {
			if (!preFetch) {
				// set temporary placeholder if we are not just pre-fetching
				if (placeholder != null) {
					setter.setPlaceholderSync(placeholder);
				}
			} else {
				setter = null; // make sure there's no callback
			}
			return BITMAP_EXECUTOR.submit(new BitmapLoader(mBitmapMemoizer, cache, diskCache, url,
					setter));
		}
	}

	/**
	 * See
	 * {@link #getBitmap(BitmapMemoryCache, BitmapDiskCache, CacheUrlKey, AccessPolicy, BitmapAsyncSetter, Drawable)}
	 * with null placeholder.
	 */
	@CheckForNull
	protected final Future<Bitmap> getBitmap(@Nonnull BitmapMemoryCache<String> cache,
			@Nullable BitmapDiskCache diskCache, @Nonnull CacheUrlKey url,
			@Nullable AccessPolicy action, @Nullable BitmapAsyncSetter callback) {
		return getBitmap(cache, diskCache, url, action, callback, null);
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void clearMemoryCache() {
		super.clearCache();
		mBitmapMemoizer.clear();
	}

}