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
package com.github.marcosalis.kraken.utils.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests for the abstract {@link SettableFutureTask} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class SettableFutureTaskTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSetE_callable() throws Exception {
		final SettableFutureTask<String> task = new SettableFutureTask<String>(
				new Callable<String>() {
					@Override
					public String call() throws Exception {
						return "first_string";
					}
				});
		task.set("set_string");
		assertEquals("set_string", task.get());
	}

	public void testSetE_runnable() throws Exception {
		final SettableFutureTask<String> task = new SettableFutureTask<String>(new Runnable() {
			@Override
			public void run() {
			}
		}, "first_string");
		task.set("set_string");
		assertEquals("set_string", task.get());
	}

	public void testFromResult() throws Exception {
		final FutureTask<String> result = SettableFutureTask.fromResult("set_string");
		assertEquals("set_string", result.get());

		final FutureTask<String> nullResult = SettableFutureTask.fromResult(null);
		assertNull(nullResult.get());
	}

}