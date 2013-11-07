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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

import com.github.marcosalis.kraken.cache.DiskCache.DiskCacheClearMode;
import com.github.marcosalis.kraken.content.ContentProxy;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.google.common.annotations.Beta;

/**
 * Base implementation of an application global contents manager.<br>
 * Provides methods to put and get global content managers from a single
 * endpoint, and utility functionalities to reduce, clear and manage their
 * caches.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class BaseContentManager<E> implements ContentManager<E> {

	private final ConcurrentMap<E, ContentProxy> mContents;

	public BaseContentManager(int initSize) {
		mContents = new ConcurrentHashMap<E, ContentProxy>(initSize, 0.75f,
				DroidUtils.getCpuBoundPoolSize());
	}

	@Override
	public boolean registerContent(E contentId, ContentProxy content) {
		return (mContents.putIfAbsent(contentId, content) == null);
	}

	@Override
	public ContentProxy getContent(E contentId) {
		return mContents.get(contentId);
	}

	/**
	 * Removes the content with the passed value
	 * 
	 * @param contentId
	 *            The identificator of the content to remove
	 */
	protected void removeContent(E contentId) {
		mContents.remove(contentId);
	}

	/**
	 * @see ContentManager#clearAllCaches()
	 */
	@Override
	public void clearAllCaches() {
		clearMemoryCaches();
		scheduleClearDiskCaches();
	}

	/**
	 * @see ContentManager#clearMemoryCaches()
	 */
	@Override
	public void clearMemoryCaches() {
		for (ContentProxy content : mContents.values()) {
			content.clearMemoryCache();
		}
	}

	/**
	 * @see ContentManager#scheduleClearDiskCaches()
	 */
	@Override
	public void scheduleClearDiskCaches() {
		for (ContentProxy content : mContents.values()) {
			content.scheduleClearDiskCache();
		}
	}

	/**
	 * @see ContentManager#clearDiskCaches(DiskCacheClearMode)
	 */
	@Override
	@NotForUIThread
	public void clearDiskCaches(DiskCacheClearMode mode) {
		for (ContentProxy content : mContents.values()) {
			content.clearDiskCache(mode);
		}
	}

}