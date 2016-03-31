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
package com.github.marcosalis.kraken.cache.bitmap.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.ContentCache.CacheSource;
import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.SimpleDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnBitmapSetListener;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnSuccessfulBitmapRetrievalListener;
import com.github.marcosalis.kraken.cache.bitmap.disk.SimpleBitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapLruCache;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.keys.SimpleCacheUrlKey;
import com.github.marcosalis.kraken.testing.framework.TestAssertsWrapper;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.AssertionFailedError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the {@link BitmapCacheImpl} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class BitmapCacheImplTest extends AndroidTestCase {

	private BitmapCacheImpl mCache;
	private Bitmap mTestBitmap;
	private CacheUrlKey mCacheKey;
	private ImageView mImgView;

	protected void setUp() throws Exception {
		super.setUp();
		final Context context = getContext();

		final InputStream is = context.getAssets().open("droid.jpg");
		mTestBitmap = BitmapFactory.decodeStream(is);

		final BitmapLruCache<String> memCache = new BitmapLruCache<String>(
				DroidUtils.getApplicationMemoryClass(mContext) / 10, "test");
		final DefaultBitmapDecoder decoder = new DefaultBitmapDecoder();
		final SimpleBitmapDiskCache diskCache = new SimpleBitmapDiskCache(mContext,
				"bitmapCacheImpl", decoder, SimpleDiskCache.MIN_EXPIRE_IN_SEC);
		final HttpRequestFactory requestFactory = createRequestFactory(mTestBitmap);
		mCache = new BitmapCacheImpl(memCache, diskCache, requestFactory, decoder);
		mCache.clearCache();

		mCacheKey = new SimpleCacheUrlKey("http://www.mymockurl.com/bitmapTest.jpg");
		mImgView = new ImageView(context); // keeps reference alive
	}

	protected void tearDown() throws Exception {
		mCache.clearCache();
		super.tearDown();
	}

	public void testGetBitmapAsyncString() throws InterruptedException {
		// retrieve from network with string URL
		mCache.setBitmapAsync(mCacheKey.getUrl(), mImgView);
		Thread.sleep(500);
		// bitmap should already come from memory
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.MEMORY);
		mCache.clearMemoryCache();
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.DISK);
	}

	public void testGetBitmapAsync_normal() throws InterruptedException {
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.NETWORK);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.MEMORY);
		mCache.clearMemoryCache();
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.DISK);
	}

	public void testGetBitmapAsync_refresh() throws InterruptedException {
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.NETWORK);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.REFRESH, CacheSource.NETWORK);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.REFRESH, CacheSource.NETWORK);
	}

	public void testGetBitmapAsync_cacheOnly() throws InterruptedException {
		assertBitmapNotRetrieved(mCache, mCacheKey, AccessPolicy.CACHE_ONLY, CacheSource.MEMORY);
		assertBitmapNotRetrieved(mCache, mCacheKey, AccessPolicy.CACHE_ONLY, CacheSource.DISK);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.NETWORK);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.CACHE_ONLY, CacheSource.MEMORY);
		mCache.clearMemoryCache();
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.CACHE_ONLY, CacheSource.DISK);
	}

	public void testPreloadBitmap() throws InterruptedException {
		mCache.preloadBitmap(mCacheKey);
		Thread.sleep(500);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.CACHE_ONLY, CacheSource.MEMORY);
	}

	public void testSetBitmapAsync_noPlaceholder() throws InterruptedException {
		assertBitmapSet(mImgView, mCache, mCacheKey, CacheSource.NETWORK);
		assertBitmapSet(mImgView, mCache, mCacheKey, CacheSource.MEMORY);
		mCache.clearMemoryCache();
		assertBitmapSet(mImgView, mCache, mCacheKey, CacheSource.DISK);
	}

	public void testSetBitmapAsync_placeholder() {
		// TODO
	}

	public void testGetBitmap() {
		// test failure on calling prefetch
		boolean thrown = false;
		try {
			mCache.getBitmap(mCacheKey, AccessPolicy.PRE_FETCH, null, null);
		} catch (IllegalArgumentException e) {
			thrown = true;
		}
		assertTrue(thrown);
	}

	public void testClearMemoryCache() throws InterruptedException {
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.NETWORK);
		mCache.clearMemoryCache();
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.DISK);
	}

	public void testClearDiskCache() throws InterruptedException {
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.NETWORK);
		mCache.clearMemoryCache();
		mCache.clearDiskCache(ClearMode.ALL);
		assertBitmapRetrieved(mCache, mCacheKey, AccessPolicy.NORMAL, CacheSource.NETWORK);
	}

	public static void assertBitmapRetrieved(BitmapCache cache, CacheUrlKey key,
			AccessPolicy policy, final CacheSource expectedSource) throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		final TestAssertsWrapper asserts = new TestAssertsWrapper();

		final OnSuccessfulBitmapRetrievalListener listener = new OnSuccessfulBitmapRetrievalListener() {
			@Override
			public void onBitmapRetrieved(@NonNull CacheUrlKey key, @NonNull final Bitmap bitmap,
					@NonNull final CacheSource source) {
				asserts.setAsserts(new Runnable() {
					@Override
					public void run() {
						assertNotNull(bitmap);
						assertEquals(expectedSource, source);
					}
				});
				latch.countDown();
			}
		};
		try {
			cache.getBitmapAsync(key, policy, listener);
		} catch (Exception e) {
			fail("Exception thrown when retrieving bitmap");
		}
		latch.await(1000, TimeUnit.MILLISECONDS);
		asserts.runAsserts();
	}

	public static void assertBitmapNotRetrieved(BitmapCache cache, CacheUrlKey key,
			AccessPolicy policy, final CacheSource expectedSource) throws InterruptedException {
		boolean thrown = false;
		try {
			assertBitmapRetrieved(cache, key, policy, expectedSource);
		} catch (AssertionFailedError e) {
			thrown = true;
		}
		assertTrue(thrown);
	}

	public static void assertBitmapSet(ImageView view, BitmapCache cache, CacheUrlKey key,
			final CacheSource expectedSource) throws InterruptedException {
		final CountDownLatch latchMemory = new CountDownLatch(1);
		final TestAssertsWrapper asserts = new TestAssertsWrapper();

		final OnBitmapSetListener listener = new OnBitmapSetListener() {
			@Override
			public void onBitmapSet(@NonNull CacheUrlKey url, @NonNull final Bitmap bitmap,
					@NonNull final CacheSource source) {
				asserts.setAsserts(new Runnable() {
					@Override
					public void run() {
						assertNotNull(bitmap);
						assertEquals(expectedSource, source);
					}
				});
				latchMemory.countDown();
			}
		};
		try {
			final BitmapAsyncSetter setter = new BitmapAsyncSetter(key, view, listener);
			cache.setBitmapAsync(key, AccessPolicy.NORMAL, setter, null);
		} catch (Exception e) {
			fail("Exception thrown when setting bitmap");
		}
		latchMemory.await(1000, TimeUnit.MILLISECONDS);
		asserts.runAsserts();
	}

	public static HttpRequestFactory createRequestFactory(@NonNull final Bitmap bitmap) {
		return new MockHttpTransport() {
			@Override
			public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
				return new MockLowLevelHttpRequest() {
					@Override
					public LowLevelHttpResponse execute() throws IOException {
						final MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
						response.setStatusCode(200);
						final ByteArrayOutputStream bos = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.PNG, 0, bos);
						final byte[] bitmapData = bos.toByteArray();
						response.setContent(new ByteArrayInputStream(bitmapData));
						return response;
					}
				};
			}
		}.createRequestFactory();
	}

}