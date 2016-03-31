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

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler.BackOffRequired;
import com.google.api.client.http.HttpResponse;
import com.google.common.annotations.Beta;

import org.apache.http.HttpStatus;

import javax.annotation.concurrent.Immutable;

/**
 * Default, general simple {@link BackOffRequired} implementation with the
 * library's default policy for requests retry backoff.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class DefaultBackOffRequired implements BackOffRequired {

	private static final String TAG = DefaultBackOffRequired.class.getSimpleName();

	@Override
	public boolean isRequired(@NonNull HttpResponse response) {
		final int statusCode = response.getStatusCode();
		switch (statusCode) {
		case 0:
			// No HTTP response (may be caused by a Google's library fault)
			logBackoffRequired("Backoff required for '0' status code", response);
			return true;
		case HttpStatus.SC_CONFLICT:
			// server-side conflict, retry with backoff
			logBackoffRequired("Backoff required for '409 - Conflict' status code", response);
			return true;
		case HttpStatus.SC_BAD_GATEWAY:
			// server unavailable, or too busy, retry with backoff
			logBackoffRequired("Handle '502 - Bad Gateway' status code", response);
			return true;
		default:
			return false;
		}
	}

	private void logBackoffRequired(@NonNull String message, @NonNull HttpResponse response) {
		if (DroidConfig.DEBUG) {
			Log.w(TAG, message + ": " + response.getRequest().getUrl());
		}
	}

}