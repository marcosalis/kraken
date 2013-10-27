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
package com.github.marcosalis.kraken.utils;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests for the {@link DroidUtils} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DroidUtilsTest extends InstrumentationTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetCpuBoundPoolSize() {
		assertTrue("CPU bound pool size is zero", DroidUtils.getCpuBoundPoolSize() > 0);
	}

	public void testGetIOBoundPoolSize() {
		assertTrue("IO bound pool size minor or equal to the CPU bound one",
				DroidUtils.getIOBoundPoolSize() > DroidUtils.getCpuBoundPoolSize());
	}

	public void testGetDeviceUniqueIdentificator() {
		String id = DroidUtils.getDeviceUniqueIdentificator(
				getInstrumentation().getTargetContext(), "mock_id");
		assertNotNull("ID null", id);
	}

	public void testBuildEmailUri() {
		Uri emailUri = DroidUtils.buildEmailUri("me@me.com", "hello", "Hi!");
		assertNotNull("Uri null", emailUri);
	}

	public void testGetApplicationMarketPage() {
		final Intent viewUrl = DroidUtils.getApplicationMarketPage("com.google.android.apps.maps");
		assertNotNull("Null intent", viewUrl);
		assertEquals("Wrong action", Intent.ACTION_VIEW, viewUrl.getAction());
		final Uri uri = viewUrl.getData();
		assertEquals("market", uri.getScheme());
	}

	public void testGetViewUrlIntent() {
		Intent viewUrl = DroidUtils.getViewUrlIntent("http://www.github.com");
		assertNotNull("Null intent", viewUrl);
		assertEquals("Wrong action", Intent.ACTION_VIEW, viewUrl.getAction());
	}

	/**
	 * Check for the availability of a temporary directory
	 */
	public void testGetTempFolder() {
		final File tempFolder = DroidUtils.getTempFolder(getInstrumentation().getTargetContext());
		assertNotNull("Null temp File", tempFolder);
		assertTrue("Temporary path doesn't exist", tempFolder.exists());
		assertTrue("Can't write on temporary path", tempFolder.canWrite());
	}

}