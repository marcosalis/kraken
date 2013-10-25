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

import com.google.api.client.util.BackOff;
import com.google.common.annotations.Beta;

/**
 * Default, simple {@link BackOff} implementation that deals with retries on a
 * failed request by using a linear back off policy with a delay of
 * {@link #DEFAULT_LINEAR_BACKOFF} milliseconds.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class DefaultLinearBackOff implements BackOff {

	/**
	 * Default short linear back off time in milliseconds. Can be used to avoid
	 * polluting with consecutive requests the connection library, giving some
	 * time to the socket to be opened or temporary network/server issues to be
	 * fixed.
	 */
	public static final long DEFAULT_LINEAR_BACKOFF = 80;

	@SuppressWarnings("unused")
	private static final String TAG = DefaultLinearBackOff.class.getSimpleName();

	@Override
	public long nextBackOffMillis() throws IOException {
		return DEFAULT_LINEAR_BACKOFF;
	}

	@Override
	public void reset() {
		// does nothing, the policy is stateless
	}

}