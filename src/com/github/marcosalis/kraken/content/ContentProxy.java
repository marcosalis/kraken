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
package com.github.marcosalis.kraken.content;

import com.github.marcosalis.kraken.cache.DiskCache;
import com.github.marcosalis.kraken.cache.loaders.ContentLoader;
import com.google.common.annotations.Beta;

/**
 * Interface for general content proxies.
 * 
 * The purpose of a content proxy (or content provider) is to ensure a level of
 * abstraction for clients of the proxy, in order to avoid the need for them to
 * manage the way a content is retrieved: the calls to the proxy should be the
 * same whether the content is available in a cache or needs to be retrieved
 * from a server. A {@link ContentLoader} is used internally to effectively
 * retrieve the requested content.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface ContentProxy {

	/**
	 * Completely clears the memory cache of this content manager.
	 */
	public void clearMemoryCache();

	/**
	 * Synchronously clears permanent "disk" (DB or flash storage) cache items
	 * of this content manager according to the passed
	 * {@link DiskCache.DiskCacheClearMode}
	 */
	public void clearDiskCache(DiskCache.DiskCacheClearMode mode);

	/**
	 * Completely wipes any permanent "disk" (DB or flash storage) cache of this
	 * content manager.
	 * 
	 * Note: implementations must be asynchronous and non-blocking.
	 */
	public void scheduleClearDiskCache();

	/**
	 * Completely wipe any kind of cache of this content manager.
	 * 
	 * Note: implementations must be asynchronous and non-blocking.
	 */
	public void clearCache();

}