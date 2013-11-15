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

import java.util.concurrent.CountDownLatch;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.github.marcosalis.kraken.cache.ContentCache.OnEntryRemovedListener;

/**
 * Unit tests for the {@link EmptyMemoryCache} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class EmptyMemoryCacheTest extends AndroidTestCase {

	private EmptyMemoryCache<String, String> mCache;

	protected void setUp() throws Exception {
		super.setUp();

		mCache = new EmptyMemoryCache<String, String>("test_empty_cache");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGet() {
		assertNull(mCache.get("key"));
		mCache.put("key", "value");
		assertNull(mCache.get("key"));
	}

	public void testPut() throws InterruptedException {
		mCache.put("key", "value");

		final CountDownLatch latch = new CountDownLatch(1);
		mCache.setOnEntryRemovedListener(new OnEntryRemovedListener<String, String>() {
			@Override
			public void onEntryRemoved(boolean evicted, String key, String value) {
				assertEquals("key", key);
				assertEquals("value", value);
				latch.countDown();
			}
		});
		assertNull(mCache.put("key", "value"));
		latch.await();
	}

	public void testRemove() {
		assertNull(mCache.remove("key"));
		mCache.put("key", "value");
		assertNull(mCache.remove("key"));
	}

	public void testClear() {
		// no-op
	}

}