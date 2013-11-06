/*
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
package com.github.marcosalis.kraken.cache.requests;

import java.util.concurrent.Callable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.github.marcosalis.kraken.utils.http.HttpRequestsManager;
import com.google.common.annotations.Beta;

/**
 * Interface for an HTTP request which returns a cacheable item and can be used
 * to compute its cache key. It implements {@link Callable} so that in can be
 * directly executed in an executor without wrapping it.
 * 
 * Support for use in caches: a content can be stored in a cache map by using an
 * hashed representation of its URL (or other suitable cache key) by calling
 * {@link #hash()}.
 * 
 * <b>Thread-safety:</b> Implementations must be thread-safe.
 * 
 * @since 1.0
 * @author Marco Salis
 * 
 * @param <E>
 *            The response object type that this request will be returning.
 */
@Beta
public interface CacheableRequest<E> extends Callable<E> {

	/**
	 * Returns the request's URL. Override this in subclasses if the request URL
	 * cannot be generated at object instantiation.
	 */
	@CheckForNull
	public abstract String getRequestUrl();

	/**
	 * Synchronously executes the request.
	 * 
	 * @return The response object if it can be retrieved or null
	 * @throws Exception
	 *             If something went wrong
	 */
	@CheckForNull
	public abstract E execute() throws Exception;

	/**
	 * Synchronously executes the request using the passed
	 * {@link HttpRequestsManager}
	 * 
	 * @see {@link #execute()}
	 */
	@CheckForNull
	public abstract E execute(@Nonnull HttpRequestsManager connManager) throws Exception;

	/**
	 * Returns a 128-bit unique hash code string representation for this request
	 * to be used as a key in caches.
	 * 
	 * @return The String representation of the hash key
	 */
	@Nonnull
	public abstract String hash();

}