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
package com.github.marcosalis.kraken.cache.bitmap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.github.marcosalis.kraken.cache.DiskCache;
import com.github.marcosalis.kraken.utils.BitmapUtils;

/**
 * Unit tests for the {@link BitmapDiskCache} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class BitmapDiskCacheTest extends AndroidTestCase {

	private BitmapDiskCache mDiskCache;
	private Bitmap mTestBitmap;
	private Bitmap mTestBitmapCropped;

	protected void setUp() throws Exception {
		super.setUp();
		mDiskCache = new BitmapDiskCache(getContext(), "bitmap_test", DiskCache.MIN_EXPIRE_IN_SEC);
		final InputStream is = getContext().getAssets().open("droid.jpg");
		mTestBitmap = BitmapFactory.decodeStream(is);
		mTestBitmapCropped = Bitmap.createBitmap(mTestBitmap, 0, 0, 20, 20);
		is.close();
	}

	protected void tearDown() throws Exception {
		mDiskCache.clear();
		mTestBitmap.recycle();
		mTestBitmapCropped.recycle();
		super.tearDown();
	}

	public void testGetBitmap() {
		assertNull(mDiskCache.getBitmap("test_bitmap"));

		mDiskCache.put("test_bitmap", mTestBitmap);
		final Bitmap bitmap = mDiskCache.getBitmap("test_bitmap");
		assertNotNull(bitmap);
		assertSameSize(mTestBitmap, bitmap);
	}

	public void testPutBitmap_ByteArray() {
		// test first insertion
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mTestBitmap.compress(CompressFormat.PNG, 0, bos);
		byte[] bitmapData = bos.toByteArray();
		mDiskCache.put("test_put_bytearray", bitmapData);
		final Bitmap bitmap = mDiskCache.getBitmap("test_put_bytearray");
		assertNotNull(bitmap);
		assertSameSize(mTestBitmap, bitmap);

		// test overwrite
		bos = new ByteArrayOutputStream();
		mTestBitmapCropped.compress(CompressFormat.PNG, 0, bos);
		bitmapData = bos.toByteArray();
		mDiskCache.put("test_put_bytearray", bitmapData);
		final Bitmap bitmapCropped = mDiskCache.getBitmap("test_put_bytearray");
		assertNotNull(bitmapCropped);
		assertSameSize(mTestBitmapCropped, bitmapCropped);
	}

	public void testPutBitmapStringBitmap() {
		// test first insertion
		mDiskCache.put("test_put_bitmap", mTestBitmap);
		final Bitmap bitmap = mDiskCache.getBitmap("test_put_bitmap");
		assertNotNull(bitmap);
		assertSameSize(mTestBitmap, bitmap);

		// test overwrite
		mDiskCache.put("test_put_bitmap", mTestBitmapCropped);
		final Bitmap bitmapCropped = mDiskCache.getBitmap("test_put_bitmap");
		assertNotNull(bitmapCropped);
		assertSameSize(mTestBitmapCropped, bitmapCropped);
	}

	private static void assertSameSize(Bitmap expected, Bitmap actual) {
		assertEquals(BitmapUtils.getSize(expected), BitmapUtils.getSize(actual));
	}

}