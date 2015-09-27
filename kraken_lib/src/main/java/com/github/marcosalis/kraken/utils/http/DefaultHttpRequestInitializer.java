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
package com.github.marcosalis.kraken.utils.http;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

/**
 * Default implementation of {@link HttpRequestInitializer}. Every request is
 * initialized with default timeouts, number of retries, exception handler and
 * back off policies using the constants in {@link NetworkConstants}.
 */
public class DefaultHttpRequestInitializer implements HttpRequestInitializer {

	@Override
	@OverridingMethodsMustInvokeSuper
	public void initialize(@Nonnull HttpRequest request) throws IOException {
		setDefaultRequestParams(request);
	}

	/**
	 * Sets common parameters to every request made through this manager, which
	 * will be applied through the {@link HttpRequestInitializer}.
	 * 
	 * @param request
	 *            The request to set the parameters in
	 */
	protected static void setDefaultRequestParams(@Nonnull HttpRequest request) {
		request.setConnectTimeout(NetworkConstants.DEFAULT_CONN_TIMEOUT);
		request.setReadTimeout(NetworkConstants.DEFAULT_READ_TIMEOUT);
		request.setNumberOfRetries(NetworkConstants.REQUEST_RETRIES);

		// use global default response handler to avoid excessive GC
		request.setUnsuccessfulResponseHandler(NetworkConstants.DEFAULT_RESPONSE_HANDLER);

		request.setIOExceptionHandler(NetworkConstants.IO_EXCEPTION_HANDLER);
		request.setThrowExceptionOnExecuteError(false);

		// enable logging only when in debug mode
		request.setLoggingEnabled(DroidConfig.DEBUG);
		request.setCurlLoggingEnabled(DroidConfig.DEBUG);
	}
}