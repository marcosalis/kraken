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

import java.io.InputStream;
import java.util.concurrent.Semaphore;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.github.marcosalis.kraken.cache.bitmap.BitmapDecoder;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

/**
 * Default implementation of {@link BitmapDecoder} that decodes bitmaps using
 * the {@link BitmapFactory} method.
 * 
 * All the concurrent decodings are controlled by a {@link Semaphore} and
 * limited depending on the number of device CPU cores to avoid overloading the
 * CPU and block the main thread.
 * 
 * @since 1.0.1
 * @author Marco Salis
 */
@Beta
public final class DefaultBitmapDecoder implements BitmapDecoder {

	static {
		DECODE_SEMAPHORE = new Semaphore(calcMaxDecodingCores(), true);
	}

	private static final Semaphore DECODE_SEMAPHORE;

	@Override
	@CheckForNull
	public Bitmap decode(@Nonnull byte[] data, @Nullable Options options) {
		if (acquirePermit()) {
			try {
				return BitmapFactory.decodeByteArray(data, 0, data.length, options);
			} finally {
				releasePermit();
			}
		}
		return null;
	}

	@Override
	@CheckForNull
	public Bitmap decode(@Nonnull InputStream stream, @Nullable Options options) {
		if (acquirePermit()) {
			try {
				return BitmapFactory.decodeStream(stream, null, options);
			} finally {
				releasePermit();
			}
		}
		return null;
	}

	@Override
	@CheckForNull
	public Bitmap decode(@Nonnull String pathName, @Nullable Options options) {
		if (acquirePermit()) {
			try {
				return BitmapFactory.decodeFile(pathName, options);
			} finally {
				releasePermit();
			}
		}
		return null;
	}

	@VisibleForTesting
	static int calcMaxDecodingCores() {
		final int cores = DroidUtils.CPU_CORES;
		// max concurrent decodings: (cores - 1)
		return cores > 1 ? cores - 1 : 1;
	}

	@VisibleForTesting
	static int getAvailablePermits() {
		return DECODE_SEMAPHORE.availablePermits();
	}

	private static boolean acquirePermit() {
		try {
			DECODE_SEMAPHORE.acquire();
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	private static void releasePermit() {
		DECODE_SEMAPHORE.release();
	}

}