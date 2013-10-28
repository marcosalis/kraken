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
package com.github.marcosalis.kraken.cache;

import javax.annotation.Nonnull;

import com.google.common.annotations.Beta;

/**
 * Base, simple interface that every content cache should implement in order to
 * perform some common operations.
 * 
 * TODO: consider implementing {@link com.google.common.cache.Cache}
 * 
 * @since 1.0
 * @author Marco Salis
 * 
 * @param <K>
 *            Cache keys type
 * @param <V>
 *            Cached items type
 */
@Beta
public interface ContentCache<K, V> {

	/**
	 * Interface that implementations can use to notify a client component when
	 * an item in the cache is removed/evicted. This is useful expecially for
	 * space-consuming cache items, in order to avoid other classes holding a
	 * reference to the old cache entry and preventing it from being GCed.
	 * 
	 * @since 1.0
	 */
	@Beta
	public interface OnEntryRemovedListener<K, V> {

		/**
		 * Called when a cache entry has been removed.
		 * 
		 * @param evicted
		 *            true if the item has been removed for space constraints,
		 *            false otherwise (because it's been explicitly removed or
		 *            replaced)
		 * @param key
		 *            the cache key of the removed element
		 * @param value
		 *            the removed element
		 */
		public void onEntryRemoved(boolean evicted, @Nonnull K key, @Nonnull V value);
	}

	/**
	 * Completely clears the cache contents.
	 */
	public void clear();

}