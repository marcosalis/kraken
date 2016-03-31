/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.google.common.annotations.Beta;

/**
 * Public interface for a 1st level, memory based cache.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface MemoryCache<K, V> extends ContentCache<K, V> {

	/**
	 * Sets an (optional) {@link OnEntryRemovedListener} to allow cache entries
	 * to be removed when using another component that keeps references to them
	 * (such as a {@link Memoizer} to populate the cache), in order to avoid
	 * memory leaks and OOM. This is especially crucial when dealing with big
	 * objects in memory such as bitmaps.
	 * 
	 * @param listener
	 *            The listener to set or null to unset it
	 */
	public void setOnEntryRemovedListener(@Nullable OnEntryRemovedListener<K, V> listener);

	/**
	 * Retrieves an element from the memory cache by its cache key.
	 * 
	 * @param key
	 *            The cache key for the requested item
	 * @return The value (or null if not present)
	 */
	@Nullable
	public V get(@NonNull K key);

	/**
	 * Puts an element into the cache.
	 * 
	 * @param key
	 *            The item cache key
	 * @param value
	 *            The cache value or null to unset
	 * @return The old element for the passed key if present
	 */
	@Nullable
	public V put(@NonNull K key, @Nullable V value);

	/**
	 * If the specified key is not already associated with a value, associate it
	 * with the given value.
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
	@Nullable
	public V putIfAbsent(@NonNull K key, V value);

	/**
	 * Removes the entry for the passed key if it exists.
	 * 
	 * @param key
	 * @return the previous value mapped by key
	 */
	@Nullable
	public V remove(K key);

}