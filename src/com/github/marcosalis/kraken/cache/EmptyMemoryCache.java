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
package com.github.marcosalis.kraken.cache;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.common.annotations.Beta;

/**
 * Implementation of a {@link MemoryCache} that contains no element. Uses the
 * <i>null object</i> pattern for components that require a bitmap memory cache.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class EmptyMemoryCache<K, V> implements MemoryCache<K, V> {

	private static final String TAG = EmptyMemoryCache.class.getSimpleName();

	@CheckForNull
	private volatile OnEntryRemovedListener<K, V> mEntryRemovedListener;

	public EmptyMemoryCache(@Nullable String cacheLogName) {
		if (DroidConfig.DEBUG) {
			Log.i(TAG, "Creating empty memory cache for " + cacheLogName);
		}
	}

	@Override
	public V get(K key) {
		return null; // always null
	}

	@Override
	public V put(K key, V value) {
		final OnEntryRemovedListener<K, V> listener = mEntryRemovedListener;
		if (listener != null) {
			// since the cache is empty, every item is immediately removed
			listener.onEntryRemoved(true, key, value);
		}
		return null;
	}

	@Override
	public V remove(K key) {
		// does nothing
		return null;
	}

	@Override
	public void clear() {
		// does nothing
	}

	@Override
	public void setOnEntryRemovedListener(OnEntryRemovedListener<K, V> listener) {
		mEntryRemovedListener = listener;
	}

}