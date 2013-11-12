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
package com.github.marcosalis.kraken.cache.bitmap.threading;

import java.util.concurrent.ThreadPoolExecutor;

import android.os.Process;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.github.marcosalis.kraken.utils.concurrent.ReorderingThreadPoolExecutor;

/**
 * Unit tests for the {@link DefaultBitmapThreadingPolicy} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DefaultBitmapThreadingPolicyTest extends AndroidTestCase {

	private DefaultBitmapThreadingPolicy mPolicy;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mPolicy = new DefaultBitmapThreadingPolicy();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetBitmapDiskExecutor() {
		final ThreadPoolExecutor executor = mPolicy.getBitmapDiskExecutor();
		assertNotNull(executor);
		assertEquals(DefaultBitmapThreadingPolicy.getDefaultDiskExecutorSize(),
				executor.getCorePoolSize());
	}

	public void testGetBitmapDownloader() {
		final ThreadPoolExecutor executor = mPolicy.getBitmapDownloader();
		assertNotNull(executor);
		assertTrue(executor instanceof ReorderingThreadPoolExecutor);
		assertEquals(DefaultBitmapThreadingPolicy.getDefaultDownloaderSize(),
				executor.getCorePoolSize());
	}

	public void testBuildDefaultDiskExecutor() {
		final int expectedSize = 2;
		final int expectedPriority = Process.THREAD_PRIORITY_LOWEST;
		final ThreadPoolExecutor executor = DefaultBitmapThreadingPolicy.buildDefaultDiskExecutor(
				expectedSize, expectedPriority);
		assertEquals(expectedSize, executor.getCorePoolSize());
	}

	public void testBuildDefaultDownloader() {
		final int expectedSize = 4;
		final int expectedPriority = Process.THREAD_PRIORITY_LOWEST;
		final ThreadPoolExecutor executor = DefaultBitmapThreadingPolicy.buildDefaultDownloader(
				expectedSize, expectedPriority);
		assertTrue(executor instanceof ReorderingThreadPoolExecutor);
		assertEquals(expectedSize, executor.getCorePoolSize());
	}

}