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
package com.github.marcosalis.kraken.cache.requests;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.Beta;

/**
 * Generic callback interface to use asynchronous HTTP(S) requests through
 * {@link BaseCacheableRequest}. Mainly useful to execute requests from the UI
 * thread without blocking.
 * 
 * Must be used with the adequate model type as a parameter when passed to a
 * request.
 * 
 * @since 1.0
 * @author Marco Salis
 * 
 * @param <E>
 *            The response object to receive
 */
@Beta
public interface ResponseAsyncCallback<E> {

	/**
	 * Called when the request is successful (2xx response code).
	 * 
	 * @param object
	 *            The model object containing the requested data
	 */
	public void onSuccess(E object);

	/**
	 * Called when the server returned an error status code to the request
	 * 
	 * @param statusCode
	 *            The returned status code
	 * @param statusMessage
	 *            The status message, if any
	 */
	public void onError(int statusCode, @Nullable String statusMessage);

	/**
	 * Called when the request caused an exception: can be an
	 * {@link IOException} (connection error or timeout, error parsing the
	 * response) or any other exception
	 * 
	 * @param ex
	 *            The exception thrown by the request and caught for the caller
	 */
	public void onException(@Nonnull Exception ex);

}