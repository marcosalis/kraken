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
package com.github.marcosalis.kraken.cache.internal.loaders;

import com.github.marcosalis.kraken.cache.ContentLruCache;
import com.github.marcosalis.kraken.cache.ModelDiskCache;
import com.github.marcosalis.kraken.cache.internal.AbstractDiskModelContentProxy;
import com.github.marcosalis.kraken.cache.requests.CacheableRequest;
import com.github.marcosalis.kraken.utils.concurrent.ExpirableFutureTask;
import com.github.marcosalis.kraken.utils.json.JsonModel;
import com.google.common.annotations.Beta;

/**
 * Generic interface to for a factory class that builds instances of a
 * {@link ContentLoader} to be used inside an
 * {@link AbstractDiskModelContentProxy}.
 * 
 * @param <R>
 *            Requests extending {@link CacheableRequest}
 * @param <M>
 *            Models extending {@link JsonModel}
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface ModelDiskContentLoaderFactory<R extends CacheableRequest<M>, M extends JsonModel> {

	/**
	 * Builds a {@link ContentLoader}.
	 * 
	 * @param memoryCache
	 *            The {@link ContentLruCache} that the content loader will use.
	 * @param diskCache
	 *            The {@link ModelDiskCache} that the content loader will use.
	 * @return The built {@link ContentLoader}
	 */
	public ContentLoader<R, M> getContentLoader(
			ContentLruCache<String, ExpirableFutureTask<M>> memoryCache, ModelDiskCache<M> diskCache);

}