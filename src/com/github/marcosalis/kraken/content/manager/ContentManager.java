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
package com.github.marcosalis.kraken.content.manager;

import com.github.marcosalis.kraken.cache.DiskCache.DiskCacheClearMode;
import com.github.marcosalis.kraken.content.ContentProxy;
import com.google.common.annotations.Beta;

/**
 * General interface for an application global contents manager.<br>
 * Provides methods to put and get global content managers from a single
 * endpoint, and utility functionalities to reduce, clear and manage their
 * caches.
 * 
 * To be used to allow dependencies injection when using a content manager.
 * 
 * @param <E>
 *            The content identificator
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface ContentManager<E> {

	/**
	 * Register a cache into the manager.<br>
	 * This should only be done once, and application global level managed.
	 * 
	 * @param contentId
	 *            The content identificator object (preferably a String or an
	 *            enum type)
	 * @param content
	 *            The content proxy instance
	 * @return true if the cache was successfully added, false if another cache
	 *         is already registered with the same cacheId
	 */
	public boolean registerContent(E contentId, ContentProxy content);

	/**
	 * Get a global cache from the manager.
	 * 
	 * @param contentId
	 *            The cache identification string
	 * @return The required cache instance, null if it doesn't exist
	 */
	public ContentProxy getContent(E contentId);

	/**
	 * Clears all the registered caches contents.
	 */
	public void clearAllCaches();

	/**
	 * Clears all the registered memory caches contents.<br>
	 * <strong>Warning:</strong> use this only on extreme low memory situations.
	 */
	public void clearMemoryCaches();

	/**
	 * Clears all the registered disk caches contents.<br>
	 * <strong>Warning:</strong> use this only on extreme low disk space
	 * situations.
	 */
	public void scheduleClearDiskCaches();

	/**
	 * Synchronously clears all the registered disk caches contents using the
	 * passed {@link DiskCacheClearMode}.
	 */
	public void clearDiskCaches(DiskCacheClearMode mode);

}