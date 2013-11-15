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
package com.github.marcosalis.kraken.cache.bitmap;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.DiskCache;
import com.github.marcosalis.kraken.cache.DiskCache.DiskCacheClearMode;
import com.github.marcosalis.kraken.cache.bitmap.internal.BitmapCacheImplTest;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter.BitmapSource;
import com.github.marcosalis.kraken.cache.keys.SimpleCacheUrlKey;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.google.api.client.http.HttpRequestFactory;

/**
 * Unit tests for the {@link BitmapCacheBuilder} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class BitmapCacheBuilderTest extends AndroidTestCase {

	private BitmapCacheBuilder mBuilder;
	private Bitmap mTestBitmap;
	private ImageView mImgView;

	private BitmapCache mCache;

	protected void setUp() throws Exception {
		super.setUp();
		final Context context = getContext();
		mBuilder = new BitmapCacheBuilder(context);
		final InputStream is = context.getAssets().open("droid.jpg");
		mTestBitmap = BitmapFactory.decodeStream(is);
		mImgView = new ImageView(context); // keeps reference alive
	}

	protected void tearDown() throws Exception {
		mImgView = null;
		if (mCache != null) {
			mCache.clearCache();
			mCache = null;
		}
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

	/**
	 * Test method for
	 * {@link BitmapCacheBuilder#httpRequestFactory(HttpRequestFactory)}.
	 */
	public void testHttpRequestFactory() {
		final HttpRequestFactory requestFactory = BitmapCacheImplTest
				.createRequestFactory(mTestBitmap);
		mBuilder.httpRequestFactory(requestFactory);
		assertEquals(requestFactory, mBuilder.requestFactory);
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

	public void testBuild_nocaches() throws IOException, InterruptedException {
		mBuilder.disableMemoryCache();
		mBuilder.disableDiskCache();
		mBuilder.httpRequestFactory(BitmapCacheImplTest.createRequestFactory(mTestBitmap));
		mCache = mBuilder.build();

		assertNotNull(mCache);
		mCache.clearCache();

		final SimpleCacheUrlKey key = new SimpleCacheUrlKey("http://www.mymockurl.com");
		// test HTTP request
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
		// since there are no caches, the bitmap will be downloaded again
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
	}

	public void testBuild_noDiskCache() throws IOException, InterruptedException {
		mBuilder.disableDiskCache();
		mBuilder.httpRequestFactory(BitmapCacheImplTest.createRequestFactory(mTestBitmap));
		mCache = mBuilder.build();
		// test setting default memory occupation
		final int expectedBytes = getMaxMemoryPercentageBytes(BitmapMemoryCache.DEFAULT_MAX_MEMORY_PERCENTAGE);
		assertEquals(expectedBytes, mBuilder.memoryCacheMaxBytes);

		assertNotNull(mCache);
		mCache.clearCache();

		final SimpleCacheUrlKey key = new SimpleCacheUrlKey("http://www.mymockurl.com");
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.MEMORY);
		mCache.clearMemoryCache();
		// no disk cache: image is downloaded again
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
	}

	public void testBuild_noMemoryCache() throws IOException, InterruptedException {
		mBuilder.disableMemoryCache();
		mBuilder.diskCacheDirectoryName("test_dir");
		mBuilder.httpRequestFactory(BitmapCacheImplTest.createRequestFactory(mTestBitmap));
		mCache = mBuilder.build();

		assertNotNull(mCache);
		mCache.clearCache();

		final SimpleCacheUrlKey key = new SimpleCacheUrlKey("http://www.mymockurl.com");
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.DISK);
		mCache.clearDiskCache(DiskCacheClearMode.ALL);
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
	}

	public void testBuild_cacheSteps_all() throws IOException, InterruptedException {
		mBuilder.diskCacheDirectoryName("test_dir");
		mBuilder.httpRequestFactory(BitmapCacheImplTest.createRequestFactory(mTestBitmap));

		mCache = mBuilder.build();
		mCache.clearCache(); // cleanup

		final SimpleCacheUrlKey key = new SimpleCacheUrlKey("http://www.mymockurl.com");
		// test HTTP request
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.NETWORK);
		// memory cache access
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.MEMORY);
		mCache.clearMemoryCache();
		// disk cache access
		BitmapCacheImplTest.assertBitmapSet(mImgView, mCache, key, BitmapSource.DISK);
	}

	// utility methods

	private int getMaxMemoryPercentageBytes(float percentage) {
		final int maxMem = DroidUtils.getApplicationMemoryClass(getContext());
		return (int) ((maxMem / 100f) * percentage);
	}

}