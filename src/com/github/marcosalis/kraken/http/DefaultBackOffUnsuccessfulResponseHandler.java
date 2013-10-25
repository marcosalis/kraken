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
package com.github.marcosalis.kraken.http;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler.BackOffRequired;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sleeper;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

/**
 * {@link HttpUnsuccessfulResponseHandler} that mimics the behavior of the
 * library {@link HttpBackOffUnsuccessfulResponseHandler} but allows overriding
 * the {@link #handleResponse(HttpRequest, HttpResponse, boolean)} to subclasses
 * in order to allow custom handling of specific HTTP status codes.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class DefaultBackOffUnsuccessfulResponseHandler implements HttpUnsuccessfulResponseHandler {

	private static final DefaultBackOffRequired DEFAULT_BACK_OFF_REQUIRED = new DefaultBackOffRequired();
	private static final DefaultLinearBackOff DEFAULT_BACK_OFF = new DefaultLinearBackOff();

	protected final BackOff mBackOff;
	protected final BackOffRequired mBackOffRequired;

	private volatile Sleeper mSleeper = Sleeper.DEFAULT;

	/**
	 * Constructs a new instance using the default {@link BackOffRequired} and a
	 * linear {@link BackOff}.
	 * 
	 * An instance built using this constructor can be reused among different
	 * requests as it's immutable.
	 */
	public DefaultBackOffUnsuccessfulResponseHandler() {
		this(DEFAULT_BACK_OFF_REQUIRED, DEFAULT_BACK_OFF);
	}

	/**
	 * Constructs a new instance from a {@link BackOffRequired} and a
	 * {@link BackOff}.
	 * 
	 * When using this constructor, be aware that {@link BackOff} should be used
	 * for only one request as the Google library doesn't reset it anymore. The
	 * only exception is when either {@link BackOff} and {@link BackOffRequired}
	 * are stateless.
	 * 
	 * @param backOffRequired
	 *            The {@link BackOffRequired}
	 * @param backOff
	 *            back-off policy
	 */
	public DefaultBackOffUnsuccessfulResponseHandler(@Nonnull BackOffRequired backOffRequired,
			@Nonnull BackOff backOff) {
		mBackOffRequired = Preconditions.checkNotNull(backOffRequired);
		mBackOff = Preconditions.checkNotNull(backOff);
	}

	@Nonnull
	@VisibleForTesting
	public final Sleeper getSleeper() {
		return mSleeper;
	}

	/**
	 * Sets the sleeper.
	 * 
	 * <p>
	 * The default value is {@link Sleeper#DEFAULT}.
	 * </p>
	 */
	@VisibleForTesting
	public final void setSleeper(@Nonnull Sleeper sleeper) {
		mSleeper = Preconditions.checkNotNull(sleeper);
	}

	@Override
	public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry)
			throws IOException {
		// return false if retry is not supported
		if (!supportsRetry) {
			return false;
		}

		// check if back-off is required for this response
		if (mBackOffRequired.isRequired(response)) {
			try {
				return BackOffUtils.next(mSleeper, mBackOff);
			} catch (InterruptedException exception) {
				// ignore
			}
		}
		return false;
	}

}