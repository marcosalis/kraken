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
package com.github.marcosalis.kraken.utils.http;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;

/**
 * Unit tests for the {@link DefaultResponseHandlerFactory} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DefaultResponseHandlerFactoryTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreateHttpUnsuccessfulResponseHandler() {
		final HttpUnsuccessfulResponseHandler handler = DefaultResponseHandlerFactory
				.createHttpUnsuccessfulResponseHandler();
		assertTrue(handler instanceof DefaultBackOffUnsuccessfulResponseHandler);

		final BackOff backOff = ((DefaultBackOffUnsuccessfulResponseHandler) handler).getBackOff();
		assertEquals(NetworkConstants.DEFAULT_BACKOFF, backOff);
	}

	public void testCreateExponentialBackOffResponseHandler() {
		final HttpUnsuccessfulResponseHandler handler = DefaultResponseHandlerFactory
				.createExponentialBackOffResponseHandler();
		assertTrue(handler instanceof HttpBackOffUnsuccessfulResponseHandler);

		final BackOff backOff = ((HttpBackOffUnsuccessfulResponseHandler) handler).getBackOff();
		assertTrue(backOff instanceof ExponentialBackOff);
	}

	public void testCreateHttpIOExceptionHandler() {
		final HttpIOExceptionHandler handler = DefaultResponseHandlerFactory
				.createHttpIOExceptionHandler();
		assertTrue(handler instanceof DefaultHttpIOExceptionHandler);
	}

}