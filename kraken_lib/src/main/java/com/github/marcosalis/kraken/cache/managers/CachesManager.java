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
package com.github.marcosalis.kraken.cache.managers;

import android.support.annotation.NonNull;

import android.app.Application;

import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.proxies.ContentProxy;
import com.google.common.annotations.Beta;

/**
 * General interface for an application global contents manager.<br>
 * Provides methods to put and get global content managers from a single
 * endpoint, and utility functionalities to clear and manage their caches.
 * 
 * Can be used to allow dependencies injection when accessing a content manager.
 * 
 * @param <E>
 *            The content identificator
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface CachesManager<E> {

	/**
	 * Register a content proxy into the manager.<br>
	 * This should only be done once, preferably in the
	 * {@link Application#onCreate()} method.
	 * 
	 * @param contentId
	 *            The content proxy identificator object (preferably a String or
	 *            an enum type)
	 * @param content
	 *            The content proxy instance
	 * @return true if the content proxy was successfully added, false if
	 *         another cache is already registered with the same ID
	 */
	public boolean registerContent(@NonNull E contentId, @NonNull ContentProxy content);

	/**
	 * Gets a previously registered content proxy from the manager.
	 * 
	 * @param contentId
	 *            The content proxy identification string
	 * @return The required content proxy instance, or null if it doesn't exist
	 */
	public ContentProxy getContent(@NonNull E contentId);

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
	 * Clears all the registered disk caches contents.
	 */
	public void scheduleClearDiskCaches();

	/**
	 * Synchronously clears all the registered disk caches contents using the
	 * passed {@link ClearMode}.
	 */
	public void clearDiskCaches(@NonNull ClearMode mode);

}