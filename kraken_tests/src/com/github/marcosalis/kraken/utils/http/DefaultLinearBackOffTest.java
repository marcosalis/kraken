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
package com.github.marcosalis.kraken.utils.http;

import java.io.IOException;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests for the {@link DefaultLinearBackOff} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class DefaultLinearBackOffTest extends TestCase {

	private DefaultLinearBackOff mBackOff;

	protected void setUp() throws Exception {
		super.setUp();
		mBackOff = new DefaultLinearBackOff();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNextBackOffMillis() throws IOException {
		for (int i = 0; i < 4; i++) {
			assertEquals(DefaultLinearBackOff.DEFAULT_LINEAR_BACKOFF, mBackOff.nextBackOffMillis());
		}
		mBackOff.reset(); // should be no-op
		for (int i = 0; i < 4; i++) {
			assertEquals(DefaultLinearBackOff.DEFAULT_LINEAR_BACKOFF, mBackOff.nextBackOffMillis());
		}
	}

}
