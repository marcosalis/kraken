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
package com.github.marcosalis.kraken.cache.bitmap;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import android.content.Context;
import android.graphics.Bitmap;

import com.github.marcosalis.kraken.cache.EmptyMemoryCache;
import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.SimpleDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.disk.SimpleBitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.internal.BitmapCacheFactory;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapLruCache;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapMemoryCache;
import com.github.marcosalis.kraken.cache.bitmap.threading.BitmapThreadingPolicy;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.http.DefaultHttpRequestsManager;
import com.google.api.client.http.HttpRequestFactory;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

/**
 * Public builder to customize and initialize a {@link BitmapCache}. See
 * {@link BitmapLruCache} and {@link SimpleBitmapDiskCache} for the default
 * memory and disk caches specifications.
 * 
 * <p>
 * <b>Example usage:</b>
 * 
 * <p>
 * <code><pre>
 * BitmapCache cache = new BitmapCacheBuilder(context)
 * 	.maxMemoryCachePercentage(15)
 * 	.memoryCacheLogName("Profile bitmaps cache")
 * 	.diskCacheDirectoryName("profile_bitmaps")
 * 	.diskCachePurgeableAfter(DroidUtils.DAY)
 * 	.build();
 * </pre></code>
 * 
 * <p>
 * The threading policies and executors are common to all bitmap caches, so that
 * callers can use separate caches with different settings and limit the thread
 * count constant. The default (recommended) threading policy, that sizes
 * executors depending on the number of device CPU cores, can be overridden by
 * calling
 * <code>{@link BitmapCacheBase#setThreadingPolicy(BitmapThreadingPolicy)}</code>
 * 
 * <p>
 * <b>TODO list:</b>
 * <ul>
 * <li>Save into caches a resampled/resized version of a Bitmap</li>
 * <li>Allow selection and use of other disk/memory cache policies (LFU?)</li>
 * <li>Effective automatic disk cache purge policy implementation</li>
 * <li>Allow custom pre/post processing of the downloaded bitmap</li>
 * </ul>
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public class BitmapCacheBuilder {

	final Context context;

	// first level cache config
	boolean memoryCacheEnabled = true;
	int memoryCacheMaxBytes;
	String memoryCacheLogName = "BitmapCache";

	// disk cache config
	boolean diskCacheEnabled = true;
	String diskCacheDirectory;
	long purgeableAfterSeconds;

	// other config
	HttpRequestFactory requestFactory;

	public BitmapCacheBuilder(@Nonnull Context context) {
		this.context = context;
	}

	@Nonnull
	public BitmapCacheBuilder disableMemoryCache() {
		memoryCacheEnabled = false;
		return this;
	}

	/**
	 * Sets the memory cache occupation limit in bytes. Use of
	 * {@link #maxMemoryCachePercentage(float)} is recommended as it enforces a
	 * proportional limit on the application memory class.
	 * 
	 * Calling this method automatically enables the memory cache.
	 * 
	 * @param maxBytes
	 * @return This builder
	 */
	@Nonnull
	public BitmapCacheBuilder maxMemoryCacheBytes(@Nonnegative int maxBytes) {
		Preconditions.checkArgument(maxBytes > 0);
		memoryCacheEnabled = true;
		memoryCacheMaxBytes = maxBytes;
		return this;
	}

	/**
	 * Sets the maximum percentage of application memory that the built cache
	 * can use.
	 * 
	 * Defaults to {@link BitmapLruCache#DEFAULT_MAX_MEMORY_PERCENTAGE}.
	 * Percentages above 30% are not recommended for a typical application.
	 * 
	 * Calling this method automatically enables the memory cache.
	 * 
	 * @param percentage
	 *            A percentage value (0 < percentage <= 100)
	 * @return This builder
	 */
	@Nonnull
	public BitmapCacheBuilder maxMemoryCachePercentage(@Nonnegative float percentage) {
		Preconditions.checkArgument(percentage > 0f && percentage <= 100f);
		memoryCacheEnabled = true;
		final int appMemoryClass = DroidUtils.getApplicationMemoryClass(context);
		memoryCacheMaxBytes = (int) ((appMemoryClass / 100f) * percentage); // %
		return this;
	}

	/**
	 * Sets an optional cache logging name. Only useful for debugging and cache
	 * statistics.
	 * 
	 * @param cacheName
	 * @return This builder
	 */
	@Nonnull
	public BitmapCacheBuilder memoryCacheLogName(@Nonnull String cacheName) {
		memoryCacheLogName = cacheName;
		return this;
	}

	@Nonnull
	public BitmapCacheBuilder disableDiskCache() {
		diskCacheEnabled = false;
		return this;
	}

	/**
	 * Sets the <b>mandatory</b> disk cache directory name. Must be a valid
	 * Linux file name. Keep this consistent at each cache instantiation or the
	 * disk cache will be lost.
	 * 
	 * Calling this method automatically enables the disk cache.
	 * 
	 * @param cacheDirectory
	 *            The cache directory (with no trailing slashes)
	 * @return This builder
	 */
	@Nonnull
	public BitmapCacheBuilder diskCacheDirectoryName(@Nonnull String cacheDirectory) {
		diskCacheEnabled = true;
		diskCacheDirectory = cacheDirectory;
		return this;
	}

	/**
	 * Sets the disk cache items expiration time in seconds. This is just a
	 * hint: it's not guaranteed that the actual implementation of the disk
	 * cache will automatically perform cache eviction at all. Call
	 * {@link BitmapCache#clearDiskCache(ClearMode)} to manually purge old
	 * bitmaps from disk.
	 * 
	 * Defaults to {@link SimpleBitmapDiskCache#DEFAULT_PURGE_AFTER} if not set.
	 * 
	 * Calling this method automatically enables the disk cache.
	 * 
	 * @param seconds
	 *            The cache items expiration. Must be >=
	 *            {@link SimpleDiskCache#MIN_EXPIRE_IN_SEC}
	 * @return This builder
	 */
	@Nonnull
	public BitmapCacheBuilder diskCachePurgeableAfter(long seconds) {
		Preconditions.checkArgument(seconds >= SimpleDiskCache.MIN_EXPIRE_IN_SEC);
		diskCacheEnabled = true;
		purgeableAfterSeconds = seconds;
		return this;
	}

	/**
	 * Sets a custom {@link HttpRequestFactory} for downloading the bitmaps.
	 * 
	 * Defaults to {@link DefaultHttpRequestsManager#getRequestFactory()} if not
	 * set.
	 * 
	 * @param factory
	 *            The request factory to use
	 * @return This builder
	 */
	@Nonnull
	public BitmapCacheBuilder httpRequestFactory(@Nonnull HttpRequestFactory factory) {
		requestFactory = factory;
		return this;
	}

	/**
	 * Builds the configured bitmap cache.
	 * 
	 * @return The built {@link BitmapCache} instance
	 * @throws IOException
	 *             If the disk cache creation failed
	 * @throws IllegalArgumentException
	 *             If one of the mandatory configuration parameters were not set
	 */
	@Nonnull
	public BitmapCache build() throws IOException {
		checkMandatoryValuesConsistency();
		setConfigDefaults();

		final BitmapMemoryCache<String> memoryCache = buildMemoryCache();
		final SimpleBitmapDiskCache diskCache = buildDiskCache();
		final HttpRequestFactory factory = getRequestFactory();
		return BitmapCacheFactory.buildDefaultBitmapCache(memoryCache, diskCache, factory);
	}

	private void checkMandatoryValuesConsistency() {
		if (diskCacheEnabled) {
			Preconditions.checkArgument(diskCacheDirectory != null, "Disk cache directory not set");
		}
	}

	private void setConfigDefaults() {
		if (memoryCacheEnabled && memoryCacheMaxBytes == 0) {
			maxMemoryCachePercentage(BitmapMemoryCache.DEFAULT_MAX_MEMORY_PERCENTAGE);
		}
		if (diskCacheEnabled && purgeableAfterSeconds == 0) {
			diskCachePurgeableAfter(SimpleBitmapDiskCache.DEFAULT_PURGE_AFTER);
		}
	}

	@Nonnull
	private BitmapMemoryCache<String> buildMemoryCache() {
		if (memoryCacheEnabled) {
			return new BitmapLruCache<String>(memoryCacheMaxBytes, memoryCacheLogName);
		} else {
			return new EmptyBitmapMemoryCache(memoryCacheLogName);
		}
	}

	@CheckForNull
	private SimpleBitmapDiskCache buildDiskCache() throws IOException {
		if (diskCacheEnabled) {
			return new SimpleBitmapDiskCache(context, diskCacheDirectory, purgeableAfterSeconds);
		}
		return null;
	}

	@Nonnull
	private HttpRequestFactory getRequestFactory() {
		if (requestFactory != null) {
			return requestFactory;
		} else {
			return DefaultHttpRequestsManager.get().getRequestFactory();
		}
	}

	/**
	 * {@link EmptyMemoryCache} implementation for bitmap memory caches.
	 */
	private static class EmptyBitmapMemoryCache extends EmptyMemoryCache<String, Bitmap> implements
			BitmapMemoryCache<String> {
		public EmptyBitmapMemoryCache(String cacheLogName) {
			super(cacheLogName);
		}
	}

}