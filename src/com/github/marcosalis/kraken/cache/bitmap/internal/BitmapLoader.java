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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.bitmap.BitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapLruCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapMemoryCache;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter.BitmapSource;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.loaders.AccessPolicy;
import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.github.marcosalis.kraken.utils.http.ByteArrayDownloader;
import com.google.api.client.http.HttpRequestFactory;
import com.google.common.annotations.Beta;

/**
 * General loader for a {@link Bitmap} from a {@link AbstractBitmapCache}.<br>
 * If the mode set for the request is {@link AccessPolicy#PRE_FETCH}, the
 * retrieved image is only downloaded and put in the memory cache if necessary.
 * 
 * FIXME: some tests showed that, very rarely, a race condition occurs for which
 * two threads can get to download the same bitmap twice: investigate this
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class BitmapLoader implements Callable<Bitmap> {

	/**
	 * Configuration class that holds all the external components that a
	 * {@link BitmapLoader} needs to access.
	 */
	@Immutable
	public static class Config {
		public final Memoizer<String, Bitmap> downloadsCache;
		public final BitmapMemoryCache<String> memoryCache;
		public final BitmapDiskCache diskCache;
		public final HttpRequestFactory requestFactory;

		/**
		 * Creates a {@link BitmapLoader} immutable configuration.
		 * 
		 * @param downloads
		 *            The {@link CacheMemoizer} used to retrieve cached items
		 * @param cache
		 *            The {@link BitmapLruCache} where bitmaps in memory are
		 *            stored
		 * @param diskCache
		 *            The (optional) {@link BitmapDiskCache} where bitmaps saved
		 *            on disk are handled
		 * @param factory
		 *            The {@link HttpRequestFactory} to download the bitmap
		 */
		public Config(@Nonnull Memoizer<String, Bitmap> downloadsCache,
				@Nonnull BitmapMemoryCache<String> memoryCache,
				@Nullable BitmapDiskCache diskCache, @Nonnull HttpRequestFactory requestFactory) {
			this.downloadsCache = downloadsCache;
			this.memoryCache = memoryCache;
			this.diskCache = diskCache;
			this.requestFactory = requestFactory;
		}
	}

	private static final String TAG = BitmapLoader.class.getSimpleName();

	private final BitmapLoader.Config mLoaderConfig;
	private final CacheUrlKey mUrl;
	private final BitmapAsyncSetter mBitmapCallback;

	/**
	 * Instantiates a {@link BitmapLoader}.
	 * 
	 * @param config
	 *            The {@link BitmapLoader#Config} for this loader
	 * @param key
	 *            The {@link CacheUrlKey} to retrieve the bitmap
	 * @param callback
	 *            {@link BitmapAsyncSetter} for the image (can be null)
	 */
	public BitmapLoader(@Nonnull BitmapLoader.Config config, @Nonnull CacheUrlKey key,
			@Nullable BitmapAsyncSetter callback) {
		mLoaderConfig = config;
		mUrl = key;
		mBitmapCallback = callback;
	}

	@Override
	@CheckForNull
	public Bitmap call() {
		final BitmapMemoryCache<String> memoryCache = mLoaderConfig.memoryCache;
		final BitmapDiskCache diskCache = mLoaderConfig.diskCache;
		final String key = mUrl.hash();
		Bitmap bitmap;

		// 1- check memory cache again
		if ((bitmap = memoryCache.get(key)) != null) { // memory cache hit
			if (mBitmapCallback != null) {
				mBitmapCallback.onBitmapReceived(mUrl, bitmap, BitmapSource.MEMORY);
			}
			return bitmap;
		}

		// 2- check disk cache
		if (diskCache != null) {
			if ((bitmap = diskCache.get(key)) != null) {
				// disk cache hit, load file into Bitmap
				if (mBitmapCallback != null) {
					mBitmapCallback.onBitmapReceived(mUrl, bitmap, BitmapSource.DISK);
				} // and put it into memory cache
				memoryCache.put(key, bitmap);
				return bitmap;
			}
		}
		/*
		 * 3- Memory and disk cache miss, execute GET request to retrieve image.
		 * 
		 * We delegate the task to another, separated executor to download
		 * images to avoid blocking delivery of cached images to the UI
		 */
		final MemoizerCallable memoizer = new MemoizerCallable(mLoaderConfig, mUrl, mBitmapCallback);
		// attempt prioritizing the download task if already in queue
		AbstractBitmapCache.moveDownloadToFront(key);
		// submit new memoizer task to downloder executor
		AbstractBitmapCache.submitInDownloader(key, memoizer);

		return null;
	}

	/**
	 * Memoizer-related task. It uses the {@link CacheMemoizer} item to check
	 * whether there is another running and unfinished task for the Bitmap we
	 * want to download from the server.
	 * 
	 * This computation gets executed through the
	 * {@link AbstractBitmapCache#submitInDownloader(Callable)} method.
	 */
	@Immutable
	private static class MemoizerCallable implements Callable<Bitmap> {

		private final BitmapLoader.Config mLoaderConfig;
		private final CacheUrlKey mUrl;
		private final BitmapAsyncSetter mBitmapCallback;

		public MemoizerCallable(@Nonnull BitmapLoader.Config config, CacheUrlKey url,
				BitmapAsyncSetter callback) {
			mLoaderConfig = config;
			mUrl = url;
			mBitmapCallback = callback;
		}

		@Override
		@CheckForNull
		public Bitmap call() throws InterruptedException {
			try {
				final DownloaderCallable downloader = new DownloaderCallable(mLoaderConfig, mUrl,
						mBitmapCallback);
				final Bitmap bitmap = mLoaderConfig.downloadsCache.execute(mUrl.hash(), downloader);
				if (mBitmapCallback != null) {
					mBitmapCallback.onBitmapReceived(mUrl, bitmap, BitmapSource.NETWORK);
				}
				return bitmap;
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				LogUtils.logException(e);
				return null; // something unexpected happened, can do nothing
			}
		}
	}

	/**
	 * Bitmap cache task that effectively downloads a Bitmap from the network
	 * when needed. When debugging is active, it also handles some
	 * instrumentation and logs some statistics about the download.
	 */
	@Immutable
	private static class DownloaderCallable implements Callable<Bitmap> {

		// for logging purposes only
		private static final Set<String> downloaderStats = Collections
				.synchronizedSet(new HashSet<String>());
		static final AtomicLong downloaderTimer = new AtomicLong();
		static final AtomicInteger downloaderCounter = new AtomicInteger();
		static final AtomicInteger failuresCounter = new AtomicInteger();

		private final BitmapLoader.Config mLoaderConfig;
		private final CacheUrlKey mKey;
		private final BitmapAsyncSetter mBitmapCallback;

		public DownloaderCallable(@Nonnull BitmapLoader.Config config, @Nonnull CacheUrlKey url,
				@Nullable BitmapAsyncSetter callback) {
			mLoaderConfig = config;
			mKey = url;
			mBitmapCallback = callback;
		}

		@Override
		@CheckForNull
		public Bitmap call() throws IOException {
			final String key = mKey.hash();
			final String url = mKey.getUrl();
			Bitmap bitmap = null;

			long startDownload = 0;
			if (DroidConfig.DEBUG) {
				startDownload = System.currentTimeMillis();
			}

			final HttpRequestFactory factory = mLoaderConfig.requestFactory;
			final byte[] imageBytes = ByteArrayDownloader.downloadByteArray(factory, url);

			long endDownload = 0;
			if (DroidConfig.DEBUG) {
				endDownload = System.currentTimeMillis();
			}
			if (imageBytes != null) { // download successful
				bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				if (bitmap != null) { // decoding successful

					if (DroidConfig.DEBUG) { // debugging
						final long endDecoding = System.currentTimeMillis();
						// logging download statistics
						final long downloadTime = endDownload - startDownload;
						downloaderTimer.addAndGet(downloadTime);
						downloaderCounter.incrementAndGet();
						Log.d(TAG, key + " download took ms " + downloadTime);
						Log.v(TAG, key + " decoding took ms " + (endDecoding - endDownload));
						if (!downloaderStats.add(key)) {
							// bitmap was already downloaded!
							Log.w(TAG, "Downloading " + key + " bitmap twice: " + url);
						}
					} // end debugging

					final BitmapMemoryCache<String> memoryCache = mLoaderConfig.memoryCache;
					final BitmapDiskCache diskCache = mLoaderConfig.diskCache;

					memoryCache.put(key, bitmap);
					if (mBitmapCallback != null) {
						mBitmapCallback.onBitmapReceived(mKey, bitmap, BitmapSource.NETWORK);
					}
					if (diskCache != null) {
						diskCache.put(key, imageBytes);
					}
				}
			} else { // download failed
				if (DroidConfig.DEBUG) {
					failuresCounter.incrementAndGet();
				}
			}
			return bitmap;
		}
	}

	// for debugging purposes only
	static void clearStatsLog() {
		final AtomicLong timer = DownloaderCallable.downloaderTimer;
		final AtomicInteger counter = DownloaderCallable.downloaderCounter;
		final AtomicInteger failures = DownloaderCallable.failuresCounter;
		final int counterInt = counter.get();
		final int failuresInt = failures.get();
		final long averageMs = (counterInt != 0) ? timer.get() / counterInt : 0;
		final int failuresPerc = (counterInt != 0) ? (int) (100f / counterInt) * failuresInt : 0;
		Log.i(TAG, counterInt + " bitmaps downloaded in average ms " + averageMs + " - "
				+ failuresPerc + "% failures");
		// reset stats
		DownloaderCallable.downloaderStats.clear();
		timer.set(0);
		counter.set(0);
		failures.set(0);
	}

}