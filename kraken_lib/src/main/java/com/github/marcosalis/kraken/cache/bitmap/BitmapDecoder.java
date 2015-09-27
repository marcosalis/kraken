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
package com.github.marcosalis.kraken.cache.bitmap;

import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.annotations.Beta;

/**
 * Public interface that defines objects used to decode bitmaps from
 * network/disk with a specific policy in a {@link BitmapCache}.
 * 
 * Concrete implementations will likely use the corresponding static methods in
 * {@link BitmapFactory} to decode the bitmaps in the required format and
 * configuration, limiting the concurrent decodings if necessary.
 * 
 * The optional passed {@link BitmapFactory.Options} instance can also be used
 * to impose size limits to the decoded bitmaps or decode a scaled version of
 * the bitmap.
 * 
 * All implementations must be thread-safe.
 * 
 * @since 1.0.1
 * @author Marco Salis
 */
@Beta
public interface BitmapDecoder {

	/**
	 * Decodes a Bitmap from the passed byte array.
	 * 
	 * @param data
	 *            The byte array containing the bitmap to decode
	 * @param options
	 *            Optional {@link BitmapFactory.Options} passed to the
	 *            {@link BitmapFactory}
	 * @return The decoded Bitmap or null if there was an error
	 */
	@CheckForNull
	public Bitmap decode(@Nonnull byte[] data, @Nullable BitmapFactory.Options options);

	/**
	 * Decodes a Bitmap from the passed {@link InputStream}.
	 * 
	 * @param stream
	 *            The input stream of the bitmap to decode
	 * @param options
	 *            Optional {@link BitmapFactory.Options} passed to the
	 *            {@link BitmapFactory}
	 * @return The decoded Bitmap or null if there was an error
	 */
	@CheckForNull
	public Bitmap decode(@Nonnull InputStream stream, @Nullable BitmapFactory.Options options);

	/**
	 * Decode a file path into a Bitmap.
	 * 
	 * @param pathName
	 *            Complete path name for the file to be decoded
	 * @param options
	 *            Optional {@link BitmapFactory.Options} passed to the
	 *            {@link BitmapFactory}
	 * @return The decoded Bitmap or null if there was an error
	 */
	@CheckForNull
	public Bitmap decode(@Nonnull String pathName, @Nullable BitmapFactory.Options options);

}