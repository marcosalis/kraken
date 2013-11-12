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
package com.github.marcosalis.kraken.cache.internal.loaders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.ContentLruCache;
import com.github.marcosalis.kraken.cache.ModelDiskCache;
import com.github.marcosalis.kraken.cache.internal.loaders.ContentLoader.ContentUpdateCallback;
import com.github.marcosalis.kraken.cache.internal.loaders.ContentLoader.RequestHandler;
import com.github.marcosalis.kraken.cache.json.JsonModel;
import com.github.marcosalis.kraken.cache.requests.BaseCacheableRequest;
import com.github.marcosalis.kraken.cache.requests.CacheableRequest;
import com.github.marcosalis.kraken.testing.mock.MockConnectionMonitor;
import com.github.marcosalis.kraken.testing.mock.MockModelDiskCache;
import com.github.marcosalis.kraken.utils.concurrent.ExpirableFutureTask;
import com.github.marcosalis.kraken.utils.http.HttpRequestsManager;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ObjectParser;

/**
 * Unit tests for the {@link DiskContentLoader} class
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DiskContentLoaderTest extends AndroidTestCase {

	private static final int MOCK_CACHE_EXP = 50;

	public static final MockJsonModel MOCK_MODEL = new MockJsonModel();

	private DiskContentLoader<MockJsonModel> mContentLoader;

	private ContentLruCache<String, ExpirableFutureTask<MockJsonModel>> mMemCache;
	private ModelDiskCache<MockJsonModel> mDiskCache;

	protected void setUp() throws Exception {
		super.setUp();

		mMemCache = new MockContentLruCache(10);
		mDiskCache = new TestModelDiskCache(getContext(), "test", MockJsonModel.class);
		RequestHandler reqHandler = new MockRequestHandler();
		mContentLoader = new DiskContentLoader<MockJsonModel>(mMemCache, mDiskCache,
				MOCK_CACHE_EXP, reqHandler, new MockConnectionMonitor());
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		mContentLoader = null;
		mMemCache = null;
		mDiskCache = null;
	}

	/**
	 * Test
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#NORMAL}
	 * 
	 * @throws InterruptedException
	 */
	public void testLoad_AccessPolicy_Normal() throws InterruptedException {
		/** test model not in cache (coming from the request) */
		MockJsonModel model = loadWithNormalRequest(AccessPolicy.NORMAL);
		assertNotNull("Null model from content loader request", model);

		/** test model in cache */
		// inject model into memory cache
		mMemCache.put("mock_hash", buildExpiringFuture());
		MockJsonModel cacheModel = loadWithEmptyRequest(AccessPolicy.NORMAL);
		assertNotNull("Null model from content loader cache", cacheModel);

		/** test expired model nullity */
		Thread.sleep(MOCK_CACHE_EXP * 2); // expire cache entry
		mDiskCache.remove("mock_hash"); // remove expired model from disk cache
		MockJsonModel expiredModel = loadWithEmptyRequest(AccessPolicy.NORMAL);
		assertNull("Not null expired model", expiredModel);
	}

	/**
	 * Test disk cache content
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#NORMAL}
	 */
	public void testLoadFromDisk_AccessPolicy_Normal() {
		/** test model in disk cache */
		// inject model into disk cache
		mDiskCache.put("mock_hash", MOCK_MODEL);
		MockJsonModel diskModel = loadWithEmptyRequest(AccessPolicy.NORMAL);
		assertNotNull("Null model from disk cache", diskModel);
	}

	/**
	 * Test
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#CACHE_ONLY}
	 * 
	 * @throws InterruptedException
	 */
	public void testLoad_AccessPolicy_CacheOnly() throws InterruptedException {
		/** test null model not in cache */
		MockJsonModel model = loadWithNormalRequest(AccessPolicy.CACHE_ONLY);
		assertNull("Not null model from content loader request in CACHE_ONLY", model);

		/** test model in cache */
		// inject model into memory cache
		mMemCache.put("mock_hash", buildExpiringFuture());
		MockJsonModel cacheModel = loadWithEmptyRequest(AccessPolicy.CACHE_ONLY);
		assertNotNull("Null model from content loader cache in CACHE_ONLY", cacheModel);

		/** test expired model non-nullity */
		Thread.sleep(MOCK_CACHE_EXP * 2); // expire cache entry
		mDiskCache.remove("mock_hash"); // remove expired model from disk cache
		MockJsonModel expiredModel = loadWithEmptyRequest(AccessPolicy.CACHE_ONLY);
		assertNotNull("Null expired model in CACHE_ONLY", expiredModel);
	}

	/**
	 * Test disk cache content
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#CACHE_ONLY}
	 */
	public void testLoadFromDisk_AccessPolicy_CacheOnly() {
		/** test model in disk cache */
		// inject model into disk cache
		mDiskCache.put("mock_hash", MOCK_MODEL);
		MockJsonModel diskModel = loadWithEmptyRequest(AccessPolicy.CACHE_ONLY);
		assertNotNull("Null model from disk cache", diskModel);
	}

	/**
	 * Test
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#PRE_FETCH}
	 * 
	 * @throws InterruptedException
	 */
	public void testLoad_AccessPolicy_PreFetch() throws InterruptedException {
		/** test null model not in cache */
		MockJsonModel model = loadWithNormalRequest(AccessPolicy.PRE_FETCH);
		assertNotNull("Null model from content loader request in PRE_FETCH", model);

		CacheableRequest<MockJsonModel> newModelRequest = new MockCacheableRequest() {
			@Override
			public MockJsonModel execute() throws Exception {
				return new MockJsonModel();
			}
		};

		/** test model in cache used */
		// inject model into memory cache
		mMemCache.put("mock_hash", buildExpiringFuture());
		MockJsonModel cacheModel = loadWithCustomRequest(AccessPolicy.PRE_FETCH, newModelRequest);
		assertNotNull("Null model from content loader cache in PRE_FETCH", cacheModel);
		assertEquals("Valid item in cache not used with PRE_FETCH", MOCK_MODEL, cacheModel);

		/** test expired model not used */
		Thread.sleep(MOCK_CACHE_EXP * 2); // expire cache entry
		mDiskCache.remove("mock_hash"); // remove expired model from disk cache
		MockJsonModel fetchedModel = loadWithCustomRequest(AccessPolicy.PRE_FETCH, newModelRequest);
		assertNotNull("Null fetched model retrieved in PRE_FETCH", fetchedModel);
		assertNotSame("Expired model used in PRE_FETCH", MOCK_MODEL, fetchedModel);
	}

	/**
	 * Test disk cache content
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#PRE_FETCH}
	 */
	public void testLoadFromDisk_AccessPolicy_PreFetch() {
		/** test model in disk cache */
		// inject model into disk cache
		mDiskCache.put("mock_hash", MOCK_MODEL);
		MockJsonModel diskModel = loadWithEmptyRequest(AccessPolicy.PRE_FETCH);
		assertNotNull("Null model from disk cache", diskModel);
	}

	/**
	 * Test
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#REFRESH}
	 * 
	 * @throws InterruptedException
	 */
	public void testLoad_AccessPolicy_Refresh() throws InterruptedException {
		/** test null model not in cache */
		MockJsonModel model = loadWithNormalRequest(AccessPolicy.REFRESH);
		assertNotNull("Null model from content loader request in REFRESH", model);

		CacheableRequest<MockJsonModel> newModelRequest = new MockCacheableRequest() {
			@Override
			public MockJsonModel execute() throws Exception {
				return new MockJsonModel();
			}
		};

		/** test model in cache refreshed */
		// inject model into memory cache
		mMemCache.put("mock_hash", buildExpiringFuture());
		MockJsonModel cacheModel = loadWithCustomRequest(AccessPolicy.REFRESH, newModelRequest);
		assertNotNull("Null model from content loader cache in REFRESH", cacheModel);
		assertNotSame("Item in cache used with REFRESH", MOCK_MODEL, cacheModel);

		/** test expired model not used */
		Thread.sleep(MOCK_CACHE_EXP * 2); // expire cache entry
		mDiskCache.remove("mock_hash"); // remove expired model from disk cache
		MockJsonModel fetchedModel = loadWithCustomRequest(AccessPolicy.REFRESH, newModelRequest);
		assertNotNull("Null fetched model retrieved in REFRESH", fetchedModel);
		assertNotSame("Expired model used in REFRESH", MOCK_MODEL, fetchedModel);
	}

	/**
	 * Test disk cache content
	 * {@link DiskContentLoader#load(AccessPolicy, CacheableRequest, ContentUpdateCallback)}
	 * with {@link AccessPolicy#REFRESH}
	 */
	public void testLoadFromDisk_AccessPolicy_Refresh() {
		/** test model in disk cache */
		// inject model into disk cache
		mDiskCache.put("mock_hash", MOCK_MODEL);
		MockJsonModel diskModel = loadWithEmptyRequest(AccessPolicy.REFRESH);
		assertNull("Not null model from disk cache when refreshing", diskModel);
	}

	/*
	 * Test utility methods
	 */

	private MockJsonModel loadWithNormalRequest(AccessPolicy action) {
		CacheableRequest<MockJsonModel> request = new MockCacheableRequest();
		try {
			return mContentLoader.load(action, request, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception when calling load()");
		}
		return null;
	}

	private MockJsonModel loadWithEmptyRequest(AccessPolicy action) {
		CacheableRequest<MockJsonModel> nullRequest = new MockCacheableRequest() {
			@Override
			public MockJsonModel execute() throws Exception {
				return null;
			}
		};
		try {
			return mContentLoader.load(action, nullRequest, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception when calling load() with empty request");
		}
		return null;
	}

	private MockJsonModel loadWithCustomRequest(AccessPolicy action,
			CacheableRequest<MockJsonModel> request) {
		try {
			return mContentLoader.load(action, request, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception when calling load() with custom request");
		}
		return null;
	}

	private static ExpirableFutureTask<MockJsonModel> buildExpiringFuture() {
		ExpirableFutureTask<MockJsonModel> task = new ExpirableFutureTask<MockJsonModel>(
				new Callable<MockJsonModel>() {
					@Override
					public MockJsonModel call() throws Exception {
						return MOCK_MODEL;
					}
				}, MOCK_CACHE_EXP);
		task.run(); // set value into future task
		return task;
	}

	/*
	 * Private static inner classes for mocking dependencies
	 */

	private static class MockJsonModel extends JsonModel {

	}

	private static class MockContentLruCache extends
			ContentLruCache<String, ExpirableFutureTask<MockJsonModel>> {

		public MockContentLruCache(int maxSize) {
			super(maxSize);
		}
	}

	private static class TestModelDiskCache extends MockModelDiskCache<MockJsonModel> {

		private final Map<String, MockJsonModel> mockDiskCache;

		public TestModelDiskCache(Context context, String subFolder, Class<MockJsonModel> modelClass)
				throws IOException {
			super(context, subFolder, modelClass);
			mockDiskCache = new HashMap<String, MockJsonModel>();
		}

		@Override
		public MockJsonModel get(String key) {
			return mockDiskCache.get(key);
		}

		@Override
		public MockJsonModel get(String key, long expiration) {
			// never expires
			return mockDiskCache.get(key);
		}

		@Override
		public boolean put(String key, MockJsonModel model) {
			mockDiskCache.put(key, model);
			return true;
		}

		@Override
		public boolean remove(String key) {
			mockDiskCache.remove(key);
			return true;
		}
	}

	private static class MockRequestHandler implements RequestHandler {
		@Override
		public boolean validateRequest(CacheableRequest<?> request) {
			return false;
		}

		@Override
		public JsonModel execRequest(CacheableRequest<?> request) throws Exception {
			return (JsonModel) request.execute(); // does nothing more
		}
	}

	private static class MockCacheableRequest extends BaseCacheableRequest<MockJsonModel> {

		public MockCacheableRequest() {
			super(HttpMethods.GET, "mock_url");
		}

		@Override
		public synchronized String hash() {
			return "mock_hash";
		}

		@Override
		public MockJsonModel execute() throws Exception {
			return MOCK_MODEL;
		}

		@Override
		public MockJsonModel execute(HttpRequestsManager connManager) throws Exception {
			return MOCK_MODEL;
		}

		@Override
		protected void configRequest(HttpRequest request) {
			// do nothing here
		}

		@Override
		protected MockJsonModel parseResponse(HttpResponse response) throws IOException,
				IllegalArgumentException {
			return MOCK_MODEL;
		}

		@Override
		protected String getTag() {
			return "mock";
		}

		@Override
		protected ObjectParser getObjectParser() {
			return new JsonObjectParser(new JacksonFactory());
		}
	}

}