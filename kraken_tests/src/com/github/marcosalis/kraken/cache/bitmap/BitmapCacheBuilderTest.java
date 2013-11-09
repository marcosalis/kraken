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
package com.github.marcosalis.kraken.cache.bitmap;

import java.io.IOException;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.github.marcosalis.kraken.cache.DiskCache;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter.BitmapSource;
import com.github.marcosalis.kraken.utils.DroidUtils;

/**
 * Unit tests for the {@link BitmapCacheBuilder} class.
 * 
 * TODO: test access to bitmaps and check {@link BitmapSource} in callback.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class BitmapCacheBuilderTest extends AndroidTestCase {

	private BitmapCacheBuilder mBuilder;

	protected void setUp() throws Exception {
		super.setUp();
		mBuilder = new BitmapCacheBuilder(getContext());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#BitmapCacheBuilder(Context)}.
	 */
	public void testBitmapCacheBuilder() {
		final Context context = getContext();
		final BitmapCacheBuilder builder = new BitmapCacheBuilder(context);
		assertEquals(context, builder.context);
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#disableMemoryCache()}.
	 */
	public void testDisableMemoryCache() {
		mBuilder.disableMemoryCache();
		assertFalse(mBuilder.memoryCacheEnabled);
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#maxMemoryCacheBytes(int)}.
	 */
	public void testMaxMemoryCacheBytes() {
		final int maxBytes = 2048;
		mBuilder.maxMemoryCacheBytes(maxBytes);
		assertTrue(mBuilder.memoryCacheEnabled);
		assertEquals(maxBytes, mBuilder.memoryCacheMaxBytes);

		boolean thrownOnFailure = false;
		try {
			mBuilder.maxMemoryCacheBytes(0);
		} catch (IllegalArgumentException e) {
			thrownOnFailure = true;
		}
		assertTrue(thrownOnFailure);
	}

	/**
	 * Test method for
	 * {@link BitmapCacheBuilder#maxMemoryCachePercentage(float)}.
	 */
	public void testMaxMemoryCachePercentage() {
		final float percentage = 15f;
		mBuilder.maxMemoryCachePercentage(percentage);
		assertTrue(mBuilder.memoryCacheEnabled);
		final int expectedBytes = getMaxMemoryPercentageBytes(percentage);
		assertEquals(expectedBytes, mBuilder.memoryCacheMaxBytes);

		boolean thrownOnFailure = false;
		try {
			mBuilder.maxMemoryCachePercentage(0);
		} catch (IllegalArgumentException e) {
			thrownOnFailure = true;
		}
		assertTrue(thrownOnFailure);
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#memoryCacheLogName(String)}.
	 */
	public void testMemoryCacheLogName() {
		mBuilder.memoryCacheLogName("test_name");
		assertEquals("test_name", mBuilder.memoryCacheLogName);
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#disableDiskCache()}.
	 */
	public void testDisableDiskCache() {
		mBuilder.disableDiskCache();
		assertFalse(mBuilder.diskCacheEnabled);
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#diskCacheDirectoryName(String)}
	 */
	public void testDiskCacheDirectoryName() {
		mBuilder.diskCacheDirectoryName("test_dir");
		assertEquals("test_dir", mBuilder.diskCacheDirectory);
	}

	/**
	 * Test method for {@link BitmapCacheBuilder#diskCachePurgeableAfter(long)}.
	 */
	public void testDiskCachePurgeableAfter() {
		final long purgeAfter = BitmapDiskCache.DEFAULT_PURGE_AFTER + 100;
		mBuilder.diskCachePurgeableAfter(purgeAfter);
		assertEquals(purgeAfter, mBuilder.purgeableAfterSeconds);

		boolean thrownOnFailure = false;
		try {
			mBuilder.diskCachePurgeableAfter(DiskCache.MIN_EXPIRE_IN_SEC - 1);
		} catch (IllegalArgumentException e) {
			thrownOnFailure = true;
		}
		assertTrue(thrownOnFailure);
	}

	/*
	 * Test methods for {@link BitmapCacheBuilder#build()}.
	 */

	public void testBuild_empty() throws IOException {
		boolean thrownOnFailure = false;
		try {
			mBuilder.build();
		} catch (IllegalArgumentException e) {
			thrownOnFailure = true;
		}
		assertTrue(thrownOnFailure);
	}

	public void testBuild_nocaches() throws IOException {
		mBuilder.disableMemoryCache();
		mBuilder.disableDiskCache();
		final BitmapCache cache = mBuilder.build();
		assertNotNull(cache);
	}

	public void testBuild_noDiskCache() throws IOException {
		mBuilder.disableDiskCache();
		final BitmapCache cache = mBuilder.build();
		// test setting default memory occupation
		final int expectedBytes = getMaxMemoryPercentageBytes(BitmapMemoryCache.DEFAULT_MAX_MEMORY_PERCENTAGE);
		assertEquals(expectedBytes, mBuilder.memoryCacheMaxBytes);
		assertNotNull(cache);
	}

	public void testBuild_noMemoryCache() throws IOException {
		mBuilder.disableMemoryCache();
		mBuilder.diskCacheDirectoryName("test_dir");
		final BitmapCache cache = mBuilder.build();
		assertNotNull(cache);
	}

	private int getMaxMemoryPercentageBytes(float percentage) {
		final int maxMem = DroidUtils.getApplicationMemoryClass(getContext());
		return (int) ((maxMem / 100f) * percentage);
	}

}