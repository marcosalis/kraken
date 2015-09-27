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
package com.github.marcosalis.kraken.utils;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * Unit tests for the {@link BitmapUtils} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SmallTest
public class BitmapUtilsTest extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetSizeIntInt() {
		int actualSize = BitmapUtils.getSize(200, 200);
		final int expectedSize = 200 * 200 * 4;
		assertEquals(expectedSize, actualSize);
	}

	public void testGetSizeBitmap() {
		InputStream is = null;
		try {
			is = getContext().getAssets().open("droid.jpg");
		} catch (IOException e) {
			fail("Can't retrieve bitmap asset");
		}
		final Bitmap droid = BitmapFactory.decodeStream(is);
		final int actualSize = BitmapUtils.getSize(droid);
		final int expectedSize = 500 * droid.getRowBytes();
		assertEquals(expectedSize, actualSize);
	}

	/**
	 * Test for
	 * {@link BitmapUtils#calculateInSampleSize(android.graphics.BitmapFactory.Options, int, int)}
	 */
	public void testCalculateInSampleSize() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// test with squared image
		options.outWidth = 600;
		options.outHeight = 600;
		int sampleSize = BitmapUtils.calculateInSampleSize(options, 300, 300);
		assertEquals(2, sampleSize);
		// test with rectangular image
		options.outWidth = 300;
		options.outHeight = 600;
		sampleSize = BitmapUtils.calculateInSampleSize(options, 300, 300);
		assertEquals(1, sampleSize);
		// test with very large image
		options.outWidth = 3200;
		options.outHeight = 2600;
		sampleSize = BitmapUtils.calculateInSampleSize(options, 300, 300);
		assertEquals(8, sampleSize);
		// test with a too small image
		options.outWidth = 280;
		options.outHeight = 150;
		sampleSize = BitmapUtils.calculateInSampleSize(options, 300, 300);
		assertEquals(1, sampleSize);
	}

	public void testIsPowerOfTwo() {
		assertTrue(BitmapUtils.isPowerOfTwo(1));
		assertTrue(BitmapUtils.isPowerOfTwo(2));
		assertFalse(BitmapUtils.isPowerOfTwo(3));
		assertTrue(BitmapUtils.isPowerOfTwo(4));
		assertTrue(BitmapUtils.isPowerOfTwo(8));
		assertFalse(BitmapUtils.isPowerOfTwo(Integer.MAX_VALUE));
	}

	public void testGetNextLowerTwoPow() {
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(0) == 0);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(1) == 1);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(2) == 2);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(3) == 2);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(4) == 4);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(5) == 4);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(15) == 8);
		assertTrue("Wrong power of 2", BitmapUtils.getNextLowerTwoPow(66) == 64);
	}

}