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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.StorageUtils;

/**
 * Unit tests for the {@link DefaultBitmapDecoder} class.
 * 
 * @since 1.0.1
 * @author Marco Salis
 */
public class DefaultBitmapDecoderTest extends AndroidTestCase {

	private DefaultBitmapDecoder mDecoder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mDecoder = new DefaultBitmapDecoder();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDecodeByteArrayOptions() throws IOException {
		final Context context = getContext();
		final InputStream is = context.getAssets().open("droid.jpg");
		final Bitmap bitmap = BitmapFactory.decodeStream(is);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, bos);
		final byte[] bitmapData = bos.toByteArray();
		assertNotNull(mDecoder.decode(bitmapData, null));
		assertAvailablePermits();
	}

	public void testDecodeInputStreamOptions() throws IOException {
		final Context context = getContext();
		final InputStream is = context.getAssets().open("droid.jpg");
		assertNotNull(mDecoder.decode(is, null));
		assertAvailablePermits();
	}

	public void testDecodeStringOptions() throws IOException {
		final Context context = getContext();
		final InputStream is = context.getAssets().open("droid.jpg");
		final Bitmap bitmap = BitmapFactory.decodeStream(is);
		File temp = null;
		try {
			temp = new File(StorageUtils.getTempFolder(context), "droid.jpg");
			bitmap.compress(CompressFormat.PNG, 0, new FileOutputStream(temp));
			assertNotNull(mDecoder.decode(temp.getAbsolutePath(), null));
		} finally {
			if (temp != null) {
				temp.delete();
			}
		}
		assertAvailablePermits();
	}

	public void testCalcMaxDecodingCores() {
		final int decodingCores = DefaultBitmapDecoder.calcMaxDecodingCores();
		assertTrue(decodingCores > 0);
		assertTrue(decodingCores <= DroidUtils.CPU_CORES);
	}

	private static void assertAvailablePermits() {
		assertEquals(DefaultBitmapDecoder.calcMaxDecodingCores(),
				DefaultBitmapDecoder.getAvailablePermits());
	}

}