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
package com.github.marcosalis.kraken.utils.concurrent;

import java.util.concurrent.Callable;

import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * Unit tests for the abstract {@link ExpirableFutureTask} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class ExpirableFutureTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test for {@link ExpirableFutureTask#isExpired()}
	 * 
	 * @throws InterruptedException
	 */
	public void testIsExpired() throws InterruptedException {
		final long expiration = 100; // ms

		// test future expiration
		ExpirableFutureTask<JSONObject> future = new ExpirableFutureTask<JSONObject>(
				new Callable<JSONObject>() {
					@Override
					public JSONObject call() throws Exception {
						return null;
					}
				}, expiration);
		// sleep enough time for the future to expire
		Thread.sleep(expiration + 1);
		assertTrue("Future should be expired", future.isExpired());

		// test future validity
		ExpirableFutureTask<JSONObject> validFuture = new ExpirableFutureTask<JSONObject>(
				new Callable<JSONObject>() {
					@Override
					public JSONObject call() throws Exception {
						return null;
					}
				}, expiration * 2);
		// sleep less time than expiration period
		Thread.sleep(expiration);
		assertFalse("Future should not be expired", validFuture.isExpired());
	}

}