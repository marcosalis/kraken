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

import javax.annotation.concurrent.Immutable;

import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.Sleeper;
import com.google.common.annotations.Beta;

/**
 * Default, general simple {@link HttpIOExceptionHandler} implementation (as
 * suggested in docs) that uses the {@link DefaultLinearBackOff} to handle
 * request IO exceptions and, opposite to the library
 * {@link HttpBackOffIOExceptionHandler}, can be used for multiple requests as
 * it is stateless.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public final class DefaultHttpIOExceptionHandler implements HttpIOExceptionHandler {

	private static final String TAG = DefaultHttpIOExceptionHandler.class.getSimpleName();

	private static final BackOff BACKOFF = new DefaultLinearBackOff();

	@Override
	public boolean handleIOException(HttpRequest request, boolean supportsRetry) throws IOException {
		if (!supportsRetry) {
			// just return if retry is not supported
			return false;
		}
		try {
			if (DroidConfig.DEBUG) {
				Log.v(TAG, "Retrying with backoff: " + request.getUrl());
			}
			return BackOffUtils.next(Sleeper.DEFAULT, BACKOFF);
		} catch (InterruptedException exception) {
			return false;
		}
	}

}