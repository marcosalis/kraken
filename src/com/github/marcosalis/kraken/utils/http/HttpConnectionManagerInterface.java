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
package com.github.marcosalis.kraken.utils.http;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.common.annotations.Beta;

/**
 * This simple interface declares the basic operations for any custom connection
 * manager to perform standard operations inside other library components that
 * use network requests.
 * 
 * It can also be used to implement a mock connection manager to inject in other
 * components for testing purposes.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface HttpConnectionManagerInterface {

	/**
	 * Returns the default request factory from which you can execute HTTP
	 * requests. Base settings, such as timeouts and user agent, are already set
	 * for you within each {@link HttpRequest} created.
	 * 
	 * The HttpRequest objects created with this factory don't throw exceptions
	 * if the request is not successful (response < 200 or >299), so you have to
	 * check the HTTP result code within the response.
	 */
	public HttpRequestFactory getRequestFactory();

	/**
	 * Returns a new request factory which uses the passed {@link HttpTransport}
	 * and whose generated {@link HttpRequest}s are initialised with default
	 * parameters.
	 * 
	 * @param transport
	 *            The HttpTransport to be used for this factory
	 * @return The created {@link HttpRequestFactory}
	 */
	public HttpRequestFactory createRequestFactory(@Nonnull HttpTransport transport);

	/**
	 * Shortcut for the {@link HttpRequest} builder. See
	 * {@link HttpRequestFactory#buildRequest(String, GenericUrl, HttpContent)}
	 * for more details.
	 * 
	 * @param method
	 *            HTTP request method string as per {@link HttpMethods}
	 * @param urlString
	 *            HTTP request url String
	 * @param content
	 *            HTTP request content or null
	 * @return The built HttpRequest
	 * @throws IOException
	 *             If an exception occurred while building the request
	 * @throws IllegalArgumentException
	 *             If the passed url has a syntax error
	 */
	public HttpRequest buildRequest(@Nonnull String method, @Nonnull String urlString,
			@Nullable HttpContent content) throws IOException;

}