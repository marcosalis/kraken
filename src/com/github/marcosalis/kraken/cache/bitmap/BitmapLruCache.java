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
package com.github.marcosalis.kraken.cache.bitmap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.ContentLruCache;
import com.github.marcosalis.kraken.utils.BitmapUtils;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.google.common.annotations.Beta;

/**
 * Abstract class that exposes basic functionalities implemented in any
 * hard-referenced Bitmap cache. The size of the cache is strictly limited by
 * the memory occupation of the contained Bitmaps to avoid OutOfMemoryError.
 * 
 * The actual cache is implemented on top of an {@code LruCache<K, Bitmap>}
 * instance.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class BitmapLruCache<K> extends ContentLruCache<K, Bitmap> {

	private static final String TAG = BitmapLruCache.class.getSimpleName();

	/**
	 * Default maximum percentage of the application memory to use when
	 * configuring the LRU cache.
	 */
	public static final float DEFAULT_MAX_MEMORY_PERCENTAGE = 10f;

	@CheckForNull
	private final String mLogName;
	@CheckForNull
	private volatile OnEntryRemovedListener<K, Bitmap> mEntryRemovedListener;

	/**
	 * Constructor for a {@link BitmapLruCache}.<br>
	 * Call {@link DroidUtils#getApplicationMemoryClass(Context)} to properly
	 * size this cache depending on the maximum available app memory heap.
	 * 
	 * @param maxSize
	 *            The max memory occupation, in bytes, that the cache will ever
	 *            occupy when full
	 * @param cacheLogName
	 *            The (optional) name of the cache (for logging purposes)
	 */
	public BitmapLruCache(@Nonnegative int maxSize, @Nullable String cacheLogName) {
		super(maxSize);
		mLogName = cacheLogName;
		if (DroidConfig.DEBUG) {
			Log.i(TAG, mLogName + ": max cache size is set to " + maxSize + " bytes");
		}
	}

	/**
	 * Sets an (optional) {@link OnEntryRemovedListener} to allow Bitmap entries
	 * to be removed when using another component that keeps references to them
	 * (such as a {@link Memoizer} to populate the cache), in order to avoid
	 * memory leaks and OOM.
	 * 
	 * @param listener
	 *            The listener to set or null to unset it
	 */
	public void setOnEntryRemovedListener(@Nullable OnEntryRemovedListener<K, Bitmap> listener) {
		mEntryRemovedListener = listener;
	}

	/**
	 * The cache items size is measured in terms of the Bitmap's size in bytes
	 * (see {@link BitmapUtils#getSize(Bitmap)}.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected final int sizeOf(K key, @Nonnull Bitmap bitmap) {
		return BitmapUtils.getSize(bitmap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		if (DroidConfig.DEBUG) {
			Log.i(TAG, mLogName + " session stats: hits " + hitCount() + ", miss " + missCount());
		}
		super.clear();
	}

	/**
	 * Do NOT EVER attempt to recycle a Bitmap here, it still could be actively
	 * used in layouts or drawables even if evicted from the cache.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
		super.entryRemoved(evicted, key, oldValue, newValue);
		// remove evicted Bitmap task from the downloads cache if exists
		final OnEntryRemovedListener<K, Bitmap> listener = mEntryRemovedListener;
		if (listener != null) {
			listener.onEntryRemoved(evicted, key, oldValue);
		}

		if (DroidConfig.DEBUG) {
			if (oldValue != null && newValue != null) {
				Log.w(TAG, mLogName + ": item " + key + " replaced: this should never happen!");
			}
			Log.v(TAG, mLogName + ": item removed, cache size is now " + size() + " bytes");
		}
	}

}