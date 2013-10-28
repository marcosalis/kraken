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

import java.io.IOException;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.testing.http.MockHttpContent;
import com.google.api.client.testing.http.MockHttpTransport;

/**
 * Unit tests for the {@link DefaultHttpRequestsManager} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DefaultHttpRequestsManagerTest extends AndroidTestCase {

	private DefaultHttpRequestsManager mManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mManager = DefaultHttpRequestsManager.get();
		mManager.initialize(null);
	}

	@Override
	protected void tearDown() throws Exception {
		mManager = null;
		super.tearDown();
	}

	public void testGetRequestFactory() {
		final HttpRequestFactory factory = mManager.getRequestFactory();
		assertNotNull("Null HttpRequestFactory", factory);
		assertTrue("Wrong default initializer",
				factory.getInitializer() instanceof DefaultHttpRequestInitializer);
	}

	public void testGetCustomRequestFactory() {
		final HttpRequestFactory factory = mManager.createRequestFactory(new MockHttpTransport());
		assertNotNull("Null custom HttpRequestFactory", factory);
		assertTrue("Custom transport not set", factory.getTransport() instanceof MockHttpTransport);
	}

	public void testBuildRequest() throws IOException {
		final MockHttpTransport mockTransport = new MockHttpTransport();
		mManager.injectTransport(mockTransport);

		final MockHttpContent content = new MockHttpContent();
		final String urlString = "http://www.google.com";
		final HttpRequest request = mManager.buildRequest(HttpMethods.GET, urlString, content);
		assertEquals("Wrong request transport", mockTransport, request.getTransport());
		assertEquals("Wrong request URL", urlString, request.getUrl().build());
		assertEquals("Wrong request content", content, request.getContent());
	}

}