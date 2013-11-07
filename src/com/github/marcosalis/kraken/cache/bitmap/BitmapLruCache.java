/*
 * Copyright 2013 Luluvise Ltd
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

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.ContentCache;
import com.github.marcosalis.kraken.utils.BitmapUtils;
import com.google.common.annotations.Beta;
import com.google.common.cache.Cache;

/**
 * Abstract class that exposes basic functionalities implemented in any
 * hard-referenced Bitmap cache. The size of the cache is strictly limited by
 * the memory occupation of the contained Bitmaps to avoid OutOfMemoryError.
 * 
 * The actual cache is implemented on top of an {@code LruCache<K, Bitmap>}
 * instance.
 * 
 * TODO: use Guava's {@link Cache} instead? It's really concurrent (backed by a
 * ConcurrentHashMap implementation, whereas LruCache operations block on the
 * whole structure) and automatically handles blocking load with Future's get().
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class BitmapLruCache<K> extends LruCache<K, Bitmap> implements ContentCache<K, Bitmap> {

	private static final String TAG = BitmapLruCache.class.getSimpleName();

	@CheckForNull
	private final String mLogName;
	@CheckForNull
	private final OnEntryRemovedListener<K, Bitmap> mEntryRemovedListener;

	/**
	 * Constructor for a BitmapCache.<br>
	 * Call {@link ActivityManager#getMemoryClass()} to properly size this cache
	 * depending on the maximum available application memory heap.
	 * 
	 * @param cacheLogName
	 *            The (optional) name of the cache (for logging purposes)
	 * @param maxSize
	 *            The max memory occupation, in bytes, that the cache will ever
	 *            occupy when full.
	 * @param listener
	 *            An (optional) {@link OnEntryRemovedListener} to allow Bitmap
	 *            entries to be removed when using another component that keeps
	 *            references to them (such as a {@link CacheMemoizer} to
	 *            populate the cache), in order to avoid memory leaks and OOM.
	 */
	public BitmapLruCache(@Nullable String cacheLogName, @Nonnegative int maxSize,
			@Nullable OnEntryRemovedListener<K, Bitmap> listener) {
		super(maxSize);
		mLogName = cacheLogName;
		mEntryRemovedListener = listener;
		if (DroidConfig.DEBUG) {
			Log.i(TAG, mLogName + ": max cache size is set to " + maxSize + " bytes");
		}
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
	 * If the specified key is not already associated with a value, associate it
	 * with the given value.
	 * 
	 * <b>Note:</b> this method doesn't respect the LRU policy, as an already
	 * inserted element isn't put at the top of the queue as the
	 * {@link LruCache#put(Object, Object)} method would do.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return the previous value associated with the specified key, or null if
	 *         there was no mapping for the key. (A null return can also
	 *         indicate that the map previously associated null with the key, if
	 *         the implementation supports null values.)
	 */
	@CheckForNull
	public synchronized Bitmap putIfAbsent(@Nonnull K key, Bitmap value) {
		Bitmap old = null;
		if ((old = get(key)) == null) {
			return put(key, value);
		} else {
			return old;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		if (DroidConfig.DEBUG) {
			Log.i(TAG, mLogName + " session stats: hits " + hitCount() + ", miss " + missCount());
		}
		evictAll();
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
		if (mEntryRemovedListener != null) {
			mEntryRemovedListener.onEntryRemoved(evicted, key, oldValue);
		}

		if (DroidConfig.DEBUG) {
			if (oldValue != null && newValue != null) {
				Log.w(TAG, mLogName + ": item " + key + " replaced: this should never happen!");
			}
			Log.v(TAG, mLogName + ": item removed, cache size is now " + size() + " bytes");
		}
	}

}