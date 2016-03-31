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
package com.github.marcosalis.kraken.cache.internal;

import android.support.annotation.Nullable;
import android.support.annotation.NonNull;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.internal.loaders.ContentLoader;
import com.github.marcosalis.kraken.cache.internal.loaders.ContentLoader.ContentUpdateCallback;
import com.github.marcosalis.kraken.cache.proxies.ContentProxyBase;
import com.github.marcosalis.kraken.cache.requests.CacheableRequest;
import com.github.marcosalis.kraken.utils.json.JsonModel;
import com.google.common.annotations.Beta;

/**
 * Generic, abstract extension of {@link ContentProxyBase} that defines a common
 * interface for content proxies that handle {@link JsonModel}s and
 * {@link CacheableRequest}.
 * 
 * It provides methods to load and put models from and to the proxy through a
 * {@link ContentLoader}, and perform common maintenance operations to the
 * content proxy caches.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public abstract class ModelContentProxy<MODEL extends JsonModel> extends ContentProxyBase {

	/**
	 * Retrieves a model from the content proxy. See {@link ContentLoader}
	 * 
	 * @param action
	 *            The {@link AccessPolicy} to use
	 * @param request
	 *            The {@link CacheableRequest}
	 * @param callback
	 *            A {@link ContentUpdateCallback} to perform custom operations
	 *            when a model is added
	 * @return The model, or null of unsuccessful
	 * @throws Exception
	 *             if an exception occurred while getting the model
	 */
	@Nullable
	public abstract MODEL getModel(AccessPolicy action, CacheableRequest<MODEL> request,
			ContentUpdateCallback<MODEL> callback) throws Exception;

	/**
	 * Retrieves a model from the content proxy. See {@link ContentLoader}
	 * 
	 * @param action
	 *            The {@link AccessPolicy} to use
	 * @param request
	 *            The {@link CacheableRequest}
	 * @return The model, or null of unsuccessful
	 * @throws Exception
	 *             if an exception occurred while getting the model
	 */
	@Nullable
	public abstract MODEL getModel(AccessPolicy action, @NonNull CacheableRequest<MODEL> request)
			throws Exception;

	/**
	 * Forces a model object to be put into the cache.<br>
	 * This is usually not recommended except for this class internal use, and
	 * must be ONLY used for injection testing purposes or when we receive an
	 * updated model after a change request to the server.
	 * 
	 * @param model
	 *            The model to put into the cache
	 */
	public abstract void putModel(final MODEL model);

	/**
	 * See {@link #putModel(JsonModel)}. When the cache key is not generated
	 * only from the model, you can call this method to directly provide it to
	 * the content proxy.
	 * 
	 * @param key
	 *            The key to use in the cache for this model
	 * @param model
	 *            The model to put into the cache
	 */
	public abstract void putModel(@NonNull final String key, final MODEL model);

}