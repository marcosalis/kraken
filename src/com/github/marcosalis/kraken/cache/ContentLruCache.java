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
package com.github.marcosalis.kraken.cache;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import android.support.v4.util.LruCache;

import com.google.common.annotations.Beta;

/**
 * Extension of the Android's {@link LruCache} to support some of the methods of
 * a {@link ConcurrentMap}.
 * 
 * TODO: unit tests
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class ContentLruCache<K, V> extends LruCache<K, V> implements ContentCache<K, V> {

	@SuppressWarnings("unused")
	private static final String TAG = ContentLruCache.class.getSimpleName();

	public ContentLruCache(@Nonnegative int maxSize) {
		super(maxSize);
	}

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
	@CheckForNull
	public synchronized V putIfAbsent(@Nonnull K key, V value) {
		V old = null;
		if ((old = get(key)) == null) {
			return put(key, value);
		} else {
			return old;
		}
	}

	/**
	 * Remove a cache entry if the key is currently associated with the passed
	 * object.
	 * 
	 * @param key
	 * @param value
	 * @return true if the element was removed, false otherwise
	 */
	public boolean remove(@Nonnull K key, V value) {
		if (value == null) {
			return false;
		}
		synchronized (this) {
			V oldValue = get(key);
			if (oldValue != null && oldValue.equals(value)) {
				remove(key);
				return true;
			}
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		evictAll();
	}

}