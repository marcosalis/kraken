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
package com.github.marcosalis.kraken.cache.bitmap.internal;

import java.util.concurrent.Future;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCacheBase;
import com.github.marcosalis.kraken.cache.bitmap.disk.BitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapMemoryCache;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter.BitmapSource;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.utils.concurrent.SettableFutureTask;
import com.google.api.client.http.HttpRequestFactory;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Concrete default implementation for a {@link BitmapCache}.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
class BitmapCacheImpl extends BitmapCacheBase {

	private final BitmapMemoryCache<String> mMemoryCache;
	@CheckForNull
	private final BitmapDiskCache mDiskCache;
	private final BitmapLoader.Config mLoaderConfig;

	BitmapCacheImpl(@Nonnull BitmapMemoryCache<String> cache, @Nullable BitmapDiskCache diskCache,
			@Nonnull HttpRequestFactory factory) {
		mMemoryCache = cache;
		cache.setOnEntryRemovedListener(this);
		mDiskCache = diskCache;
		mLoaderConfig = new BitmapLoader.Config(getMemoizer(), mMemoryCache, mDiskCache, factory);
	}

	@Nonnull
	@Override
	public void getBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nonnull OnSuccessfulBitmapRetrievalListener listener) {
		Preconditions.checkArgument(policy != AccessPolicy.PRE_FETCH, "Can't prefetch here");
		final boolean isRefresh = policy == AccessPolicy.REFRESH;

		if (isRefresh) {
			BitmapLoader.executeDownload(mLoaderConfig, key, listener);
		} else {
			final Future<Bitmap> future = getBitmapFromMemory(key, listener);
			if (future != null) {
				// cache hit at memory level, we can avoid further overhead of
				// executing tasks as an optimization
			} else {
				final BitmapLoader loader = new BitmapLoader(mLoaderConfig, key, policy, listener);
				BitmapCacheBase.submitInExecutor(loader);
			}
		}
	}

	@Override
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	public void preloadBitmap(@Nonnull CacheUrlKey key) {
		final BitmapLoader loader = new BitmapLoader(mLoaderConfig, key, AccessPolicy.PRE_FETCH,
				null);
		BitmapCacheBase.submitInExecutor(loader);
	}

	@Nonnull
	@Override
	public void setBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull ImageView view) {
		final BitmapAsyncSetter setter = new BitmapAsyncSetter(key, view);
		getBitmap(key, AccessPolicy.NORMAL, setter, null);
	}

	@Nonnull
	@Override
	public void setBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nonnull BitmapAsyncSetter setter, @Nullable Drawable placeholder) {
		getBitmap(key, policy, setter, placeholder);
	}

	/**
	 * Get a bitmap content from the {@link BitmapCache} and set it into an
	 * ImageView using the passed {@link BitmapAsyncSetter}.
	 * 
	 * <b>This needs to be called from the UI thread</b>, as the image setting
	 * is asynchronous except in the case we already have the image available in
	 * the memory cache.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} of the image to retrieve
	 * @param action
	 *            The {@link AccessPolicy} to use, can be one of
	 *            {@link AccessPolicy#NORMAL}, {@link AccessPolicy#CACHE_ONLY}
	 *            or {@link AccessPolicy#REFRESH}
	 * @param setter
	 *            The {@link BitmapAsyncSetter} to set the bitmap in an
	 *            {@link ImageView}
	 * @param placeholder
	 *            An (optional) {@link Drawable} temporary placeholder, only set
	 *            if the bitmap is not in the memory cache
	 * @return The {@link Future} that holds the Bitmap loading
	 */
	@Nonnull
	protected final Future<Bitmap> getBitmap(@Nonnull CacheUrlKey key,
			@Nonnull AccessPolicy policy, @Nonnull BitmapAsyncSetter setter,
			@Nullable Drawable placeholder) {
		Preconditions.checkArgument(policy != AccessPolicy.PRE_FETCH, "Can't prefetch here");
		final boolean isRefresh = policy == AccessPolicy.REFRESH;

		final Future<Bitmap> future;
		if (!isRefresh && (future = getBitmapFromMemory(key, setter)) != null) {
			// cache hit at the very first attempt, no other actions needed
			return future;
		} else {
			// set temporary placeholder
			if (placeholder != null) {
				setter.setPlaceholderSync(placeholder);
			}
			if (!isRefresh) {
				final BitmapLoader loader = new BitmapLoader(mLoaderConfig, key, policy, setter);
				return BitmapCacheBase.submitInExecutor(loader);
			} else {
				return BitmapLoader.executeDownload(mLoaderConfig, key, setter);
			}
		}
	}

	@CheckForNull
	private Future<Bitmap> getBitmapFromMemory(@Nonnull CacheUrlKey key,
			@Nonnull OnBitmapRetrievalListener listener) {
		Bitmap bitmap = null;
		if ((bitmap = mMemoryCache.get(key.hash())) != null) {
			if (listener instanceof BitmapAsyncSetter) {
				((BitmapAsyncSetter) listener).setBitmapSync(bitmap);
			} else {
				listener.onBitmapRetrieved(key, bitmap, BitmapSource.MEMORY);
			}
			return SettableFutureTask.fromResult(bitmap);
		}
		return null;
	}

	@Override
	public void clearMemoryCache() {
		super.clearMemoryCache();
		mMemoryCache.clear();
	}

	@Override
	public void clearDiskCache(ClearMode mode) {
		if (mDiskCache != null) {
			mDiskCache.clear(mode);
		}
	}

	@Override
	public void scheduleClearDiskCache() {
		if (mDiskCache != null) {
			mDiskCache.scheduleClear();
		}
	}

}