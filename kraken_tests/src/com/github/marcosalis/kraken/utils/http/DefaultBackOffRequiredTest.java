/*
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

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

/**
 * Unit tests for the {@link DefaultBackOffRequired} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DefaultBackOffRequiredTest extends TestCase {

	private DefaultBackOffRequired mBackOffRequired;

	protected void setUp() throws Exception {
		super.setUp();
		mBackOffRequired = new DefaultBackOffRequired();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsRequired_200() throws IOException {
		final HttpResponse response200 = generateMockHttpResponse(HttpStatusCodes.STATUS_CODE_OK);
		assertFalse(mBackOffRequired.isRequired(response200));
	}

	public void testIsRequired_404() throws IOException {
		final HttpResponse response404 = generateMockHttpResponse(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
		assertFalse(mBackOffRequired.isRequired(response404));
	}

	public void testIsRequired_0() throws IOException { // no http response
		final HttpResponse response0 = generateMockHttpResponse(0);
		assertTrue(mBackOffRequired.isRequired(response0));
	}

	public void testIsRequired_409() throws IOException {
		final HttpResponse response409 = generateMockHttpResponse(409);
		assertTrue(mBackOffRequired.isRequired(response409));
	}

	public void testIsRequired_502() throws IOException {
		final HttpResponse response502 = generateMockHttpResponse(502);
		assertTrue(mBackOffRequired.isRequired(response502));
	}

	private static HttpResponse generateMockHttpResponse(final int statusCode) throws IOException {
		HttpTransport transport = new MockHttpTransport() {
			@Override
			public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
				return new MockLowLevelHttpRequest() {
					@Override
					public LowLevelHttpResponse execute() throws IOException {
						return new MockLowLevelHttpResponse().setStatusCode(statusCode);
					}
				};
			}
		};
		HttpRequest request = transport.createRequestFactory().buildGetRequest(
				HttpTesting.SIMPLE_GENERIC_URL);
		request.setThrowExceptionOnExecuteError(false);
		return request.execute();
	}

}