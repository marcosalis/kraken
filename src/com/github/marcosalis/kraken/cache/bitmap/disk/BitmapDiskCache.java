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
package com.github.marcosalis.kraken.cache.bitmap.disk;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import android.graphics.Bitmap;

import com.github.marcosalis.kraken.cache.SecondLevelCache;

/**
 * Public interface for a disk cache of bitmaps. Include operations to retrieve
 * a bitmap, store it by passing a byte array or Bitmap and remove items.
 * 
 * @since 1.0.1
 * @author Marco Salis
 */
public interface BitmapDiskCache extends SecondLevelCache<String, Bitmap> {

	/**
	 * Gets a {@link Bitmap} from the disk cache.
	 * 
	 * @param key
	 *            The cache item key
	 * @return The cached bitmap, null if not present or an error occurred
	 */
	@CheckForNull
	public Bitmap get(@Nonnull String key);

	/**
	 * Puts a byte array representing a bitmap into the disk cache.
	 * 
	 * <b>Note:</b> to ensure optimal performances in terms of writing I/O and
	 * space consumption, the byte array should possibly be in a compressed
	 * format (JPG or PNG).
	 * 
	 * @param key
	 *            The cache item key
	 * @param image
	 *            The byte array containing the image
	 * @return true if successful, false otherwise (I/O error while storing)
	 * @throws IllegalArgumentException
	 *             if key or image are null
	 */
	public boolean put(@Nonnull String key, @Nonnull byte[] image);

	/**
	 * Compresses a {@link Bitmap} and puts it into the disk cache.
	 * 
	 * <b>Note:</b> saving a file from a Bitmap requires a new compression,
	 * avoid it directly storing the original byte array with
	 * {@link #put(String, byte[])} whenever possible.
	 * 
	 * @param key
	 *            The cache item key
	 * @param image
	 *            The byte array containing the image
	 * @return true if successful, false otherwise (I/O error while storing)
	 * @throws IllegalArgumentException
	 *             if key or image are null
	 */
	public boolean put(@Nonnull String key, @Nonnull Bitmap bitmap);

	/**
	 * Removes an item from the disk cache by deleting the corresponding file in
	 * the disk cache.
	 * 
	 * @param key
	 *            The cache item key
	 * @return true if the item was successfully removed, false otherwise
	 */
	public boolean remove(@Nonnull String key);

}