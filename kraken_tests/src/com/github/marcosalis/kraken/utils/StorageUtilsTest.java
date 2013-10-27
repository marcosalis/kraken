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

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests for the {@link StorageUtils} class
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class StorageUtilsTest extends InstrumentationTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Check for the availability of a cache directory in all testable cases
	 */
	public void testGetAppCacheDir() {
		final Context context = getInstrumentation().getTargetContext();
		File appCache = StorageUtils.getAppCacheDir(context);
		assertNotNull("Null cache File", appCache);
		assertTrue("Cache path doesn't exist", appCache.exists());
		assertTrue("Cannot write on cache path", appCache.canWrite());
	}

	public void testGetInternalAppCacheDir() {
		final Context context = getInstrumentation().getTargetContext();
		final File intCacheDir = context.getCacheDir();
		final File appInternalCache = StorageUtils.getInternalAppCacheDir(context);
		assertEquals("Internal cache dir not matching", intCacheDir, appInternalCache);
		if (appInternalCache != null) {
			assertTrue("Can't write in internal cache", appInternalCache.canWrite());
		}
	}

	public void testGetExternalAppCacheDir() {
		final Context context = getInstrumentation().getTargetContext();
		final File appExternalCache = StorageUtils.getExternalAppCacheDir(context);
		if (StorageUtils.isExternalStorageMounted()) {
			assertNotNull("Null external cache File", appExternalCache);
			assertTrue("Can't write in external cache", appExternalCache.canWrite());
		} else {
			assertNull("Not null external cache File when external storage is not mounted",
					appExternalCache);
		}
	}

	/**
	 * Check for the availability of a temporary directory
	 */
	public void testGetTempFolder() {
		final File tempFolder = StorageUtils.getTempFolder(getInstrumentation().getTargetContext());
		assertNotNull("Null temp File", tempFolder);
		assertTrue("Temporary path doesn't exist", tempFolder.exists());
		assertTrue("Can't write on temporary path", tempFolder.canWrite());
	}

}