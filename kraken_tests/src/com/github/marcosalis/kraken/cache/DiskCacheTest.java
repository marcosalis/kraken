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
package com.github.marcosalis.kraken.cache;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;

import com.github.marcosalis.kraken.utils.FileUtils;
import com.github.marcosalis.kraken.utils.StorageUtils;
import com.github.marcosalis.kraken.utils.StorageUtils.CacheLocation;

/**
 * Unit tests for the {@link DiskCache} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class DiskCacheTest extends AndroidTestCase {

	private static final String TAG = "DiskCacheTest";

	private static final String TEST_FOLDER = "test";
	private static final CacheLocation TEST_LOCATION = CacheLocation.EXTERNAL;

	private File mCacheDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final File cacheRoot = StorageUtils.getAppCacheDir(getContext(), TEST_LOCATION, true);
		mCacheDir = new File(cacheRoot.getAbsolutePath() + File.separator + TEST_FOLDER);
	}

	@Override
	protected void tearDown() throws Exception {
		FileUtils.deleteDirectoryTree(mCacheDir);
		mCacheDir.delete();
		super.tearDown();
	}

	/**
	 * Test constructor
	 * {@link DiskCache#DiskCache(Context, CacheLocation, String, boolean)}
	 */
	public void testDiskCache() {
		DiskCache<String> mockCache = null;
		try {
			mockCache = new DiskCache<String>(getContext(), TEST_LOCATION, TEST_FOLDER, false);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(mockCache);
		// check for directory existence
		assertTrue("Cache dir not existing", mCacheDir.exists());
	}

	/**
	 * Test for {@link DiskCache#scheduleClearAll()}
	 */
	public void testPurgeAll() throws InterruptedException {
		if (!mCacheDir.mkdirs())
			fail();
		File file1 = new File(mCacheDir, "file1");
		File file2 = new File(mCacheDir, "file2");
		try {
			file1.createNewFile();
			file2.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		DiskCache<String> mockCache = null;
		try {
			mockCache = new DiskCache<String>(getContext(), TEST_LOCATION, TEST_FOLDER, false);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		mockCache.clear();

		assertFalse("Files not deleted correctly", file1.exists() || file2.exists());
	}

	/**
	 * Test for {@link DiskCache#schedulePurge(long)}
	 */
	public void testPurge() throws InterruptedException {
		if (!mCacheDir.mkdirs())
			fail();
		File expiredFile = new File(mCacheDir, "expired");
		File newFile = new File(mCacheDir, "new");
		try {
			expiredFile.createNewFile();
			newFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		DiskCache<String> mockCache = null;
		try {
			mockCache = new DiskCache<String>(getContext(), TEST_LOCATION, TEST_FOLDER, false);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		final long timeSpan = System.currentTimeMillis() - (DiskCache.MIN_EXPIRE_IN_MS * 2);
		expiredFile.setLastModified(timeSpan); // make the file "old"
		mockCache.purge(DiskCache.MIN_EXPIRE_IN_SEC);

		// Note: this test can fail in some devices due to an Android OS bug
		// assertFalse("Expired file not deleted correctly",
		// expiredFile.exists());
		assertTrue("New file also deleted", newFile.exists());
	}

	public void testPurge_invalidExpiration() {
		final DiskCache<String> cache = createDiskCache(getContext(), false);
		assertNotNull(cache);
		boolean thrown = false;
		try { // using too short expiration
			cache.purge(DiskCache.MIN_EXPIRE_IN_SEC - 1);
		} catch (IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue("Exception on illegal argument not thrown", thrown);
	}

	public void testDeleteIfExpired_expired() throws InterruptedException {
		mCacheDir.mkdirs();
		final File file = new File(mCacheDir, "testFile");
		try {
			assertTrue(file.createNewFile());
		} catch (IOException e) {
			fail();
		}
		Thread.sleep(101); // sleep to cause expiration
		boolean deleted = DiskCache.deleteIfExpired(file, System.currentTimeMillis(), 100);
		assertTrue(deleted);
		assertFalse(file.exists());
	}

	public void testDeleteIfExpired_notExpired() {
		mCacheDir.mkdirs();
		final File file = new File(mCacheDir, "testFile");
		try {
			assertTrue(file.createNewFile());
		} catch (IOException e) {
			fail();
		}
		boolean deleted = DiskCache.deleteIfExpired(file, System.currentTimeMillis(), 5000);
		assertFalse(deleted);
		assertTrue(file.exists());
	}

	/**
	 * Test for {@link DiskCache#touchFile(File)}
	 * 
	 * Note: this test can fail in some devices due to an Android OS bug
	 */
	@Suppress
	public void testTouchFile() {
		if (!mCacheDir.mkdirs())
			fail();
		File touchFile = new File(mCacheDir, "touchFile");
		try {
			touchFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		Log.e(TAG, "Initial lastModified():" + touchFile.lastModified());
		// set last modified date far enough in the past (so that we know the
		// touch is actually written to the file)
		final long timeSpan = System.currentTimeMillis() - (DiskCache.MIN_EXPIRE_IN_MS * 2);
		if (!touchFile.setLastModified(timeSpan)) {
			fail();
		}
		Log.e(TAG, "After setLastModified():" + touchFile.lastModified());
		assertTrue("setLastModified() failed",
				Math.abs(touchFile.lastModified() - timeSpan) <= 1000);
		DiskCache.touchFile(touchFile);
		assertTrue("touchFile() didn't touch", touchFile.lastModified() - timeSpan > 0);
		Log.e(TAG, "After touchFile():" + touchFile.lastModified());
	}

	private static DiskCache<String> createDiskCache(Context context, boolean allowLocationFallback) {
		DiskCache<String> cache = null;
		try {
			cache = new DiskCache<String>(context, TEST_LOCATION, TEST_FOLDER,
					allowLocationFallback);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(cache);
		return cache;
	}

}