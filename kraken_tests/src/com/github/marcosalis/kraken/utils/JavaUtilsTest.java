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
package com.github.marcosalis.kraken.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the {@link JavaUtils} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class JavaUtilsTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testStartsWithIgnoreCase() {
		// edge cases
		assertTrue(JavaUtils.startsWithIgnoreCase(null, null));
		assertFalse(JavaUtils.startsWithIgnoreCase("string", null));
		assertFalse(JavaUtils.startsWithIgnoreCase(null, "string"));

		assertTrue(JavaUtils.startsWithIgnoreCase("string", "str"));
		assertTrue(JavaUtils.startsWithIgnoreCase("string", "Str"));
		assertTrue(JavaUtils.startsWithIgnoreCase("string", "STRING"));

		assertFalse(JavaUtils.startsWithIgnoreCase("string", "tring"));
	}

	public void testGetRandomEArray() {
		assertNull(JavaUtils.getRandom(new Integer[0]));
		// single item = same random result
		assertEquals(Integer.valueOf(3), JavaUtils.getRandom(new Integer[] { 3 }));

		for (int i = 0; i < 5; i++) {
			final Integer[] items = new Integer[] { 0, 2, 4, 3, 1 };
			final Integer randomItem = JavaUtils.getRandom(items);
			Arrays.sort(items); // sort items, so that the array index = value
			assertTrue(Arrays.binarySearch(items, randomItem) == randomItem);
		}
	}

	public void testGetRandomListOfE() {
		assertNull(JavaUtils.getRandom(ImmutableList.of()));
		// single item = same random result
		assertEquals(Integer.valueOf(3), JavaUtils.getRandom(ImmutableList.of(3)));

		for (int i = 0; i < 5; i++) {
			final List<Integer> items = ImmutableList.of(0, 2, 4, 3, 1);
			final Integer randomItem = JavaUtils.getRandom(items);
			// sort items, so that the list index = value
			ArrayList<Integer> sortedItems = new ArrayList<Integer>(items);
			Collections.sort(sortedItems);
			assertTrue(Collections.binarySearch(sortedItems, randomItem) == randomItem);
		}
	}

}