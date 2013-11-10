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
package com.github.marcosalis.kraken.cache.bitmap;

import java.util.concurrent.Future;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.github.marcosalis.kraken.cache.DiskCache.DiskCacheClearMode;
import com.github.marcosalis.kraken.cache.bitmap.internal.AbstractBitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.internal.BitmapLoader;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.loaders.AccessPolicy;
import com.google.api.client.http.HttpRequestFactory;
import com.google.common.annotations.Beta;

/**
 * Concrete default implementation for a {@link BitmapCache}.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
class BitmapCacheImpl extends AbstractBitmapCache {

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

	@Override
	@CheckForNull
	public Future<Bitmap> getBitmapAsync(@Nonnull CacheUrlKey key,
			@Nullable BitmapAsyncSetter setter) {
		return getBitmap(mMemoryCache, mDiskCache, key, AccessPolicy.NORMAL, setter);
	}

	@Override
	@CheckForNull
	public Future<Bitmap> getBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nullable BitmapAsyncSetter setter, @Nullable Drawable placeholder) {
		return getBitmap(mMemoryCache, mDiskCache, key, policy, setter, placeholder);
	}

	@Override
	public void clearMemoryCache() {
		super.clearMemoryCache();
		mMemoryCache.clear();
	}

	@Override
	public void clearDiskCache(DiskCacheClearMode mode) {
		if (mDiskCache != null) {
			mDiskCache.clear(mode);
		}
	}

	@Override
	public void scheduleClearDiskCache() {
		if (mDiskCache != null) {
			mDiskCache.scheduleClearAll();
		}
	}

	@Nonnull
	@Override
	protected BitmapLoader.Config getLoaderConfig() {
		return mLoaderConfig;
	}

}