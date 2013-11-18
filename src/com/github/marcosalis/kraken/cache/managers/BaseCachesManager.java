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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import android.app.Application;

import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.proxies.ContentProxy;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.android.DroidApplication;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.google.common.annotations.Beta;

/**
 * <p>
 * Base implementation of an application {@link CachesManager}.
 * 
 * <p>
 * You should preferably create the instance in the
 * {@link Application#onCreate()} method and retain it in the
 * {@link Application} class. See {@link DroidApplication} for documentation on
 * how to use a custom application class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class BaseCachesManager<E> implements CachesManager<E> {

	@Nonnull
	private final ConcurrentMap<E, ContentProxy> mContents;

	public BaseCachesManager(int initSize) {
		mContents = new ConcurrentHashMap<E, ContentProxy>(initSize, 0.75f,
				DroidUtils.getCpuBoundPoolSize());
	}

	@Override
	public boolean registerContent(@Nonnull E contentId, @Nonnull ContentProxy content) {
		return (mContents.putIfAbsent(contentId, content) == null);
	}

	@Override
	public ContentProxy getContent(@Nonnull E contentId) {
		return mContents.get(contentId);
	}

	/**
	 * Removes the content with the passed value
	 * 
	 * @param contentId
	 *            The identificator of the content to remove
	 */
	protected void removeContent(@Nonnull E contentId) {
		mContents.remove(contentId);
	}

	@Override
	public void clearAllCaches() {
		clearMemoryCaches();
		scheduleClearDiskCaches();
	}

	@Override
	public void clearMemoryCaches() {
		for (ContentProxy content : mContents.values()) {
			content.clearMemoryCache();
		}
	}

	@Override
	public void scheduleClearDiskCaches() {
		for (ContentProxy content : mContents.values()) {
			content.scheduleClearDiskCache();
		}
	}

	@Override
	@NotForUIThread
	public void clearDiskCaches(ClearMode mode) {
		for (ContentProxy content : mContents.values()) {
			content.clearDiskCache(mode);
		}
	}

}