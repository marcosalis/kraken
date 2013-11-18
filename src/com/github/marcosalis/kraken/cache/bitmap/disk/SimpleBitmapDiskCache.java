/*
 * Copyright 2013 Luluvise Ltd
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.SimpleDiskCache;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.StorageUtils.CacheLocation;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * Base implementation of a {@link Bitmap} disk cache. The bitmaps are stored in
 * the disk as byte streams and decoded back to a {@link Bitmap} object when
 * they are retrieved from the cache.
 * 
 * No size constraints are imposed, the purge methods are in charge of keeping
 * the cache size reasonable, make sure to call them frequently enough and
 * always when the device storage is running out of space.
 * 
 * In order not to degrade UI performances when decoding a {@link Bitmap} from
 * the disk, only one simultaneous decoding is permitted.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public class SimpleBitmapDiskCache extends SimpleDiskCache<Bitmap> implements BitmapDiskCache {

	private static final String TAG = SimpleBitmapDiskCache.class.getSimpleName();

	private static final String PATH = "bitmap";

	public static final long DEFAULT_PURGE_AFTER = DroidUtils.DAY * 2;
	private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 85;

	/**
	 * {@link ReentrantLock} used to guarantee that only one bitmap gets decoded
	 * at any time (it's a heavy CPU-bound operation and we don't want it to
	 * degrade the UI performances at any cost).
	 */
	private static final ReentrantLock DECODE_LOCK = new ReentrantLock();

	private final long mBitmapExpirationSec;

	/**
	 * Builds a {@link SimpleBitmapDiskCache} in the passed sub-folder. Note
	 * that the {@link CacheLocation#EXTERNAL} cache is always used. If no
	 * external caches are present, it falls back to the internal one.
	 * 
	 * @param context
	 *            The context to retrieve the cache location
	 * @param subFolder
	 *            The relative path to the cache folder where to store the cache
	 *            (the folder is created if it doesn't exist)
	 * @param purgeAfterSec
	 *            Expiration time, in seconds, for the items in disk cache (must
	 *            be >= {@link #MIN_EXPIRE_IN_SEC})
	 * @throws IOException
	 *             if the cache cannot be created
	 */
	public SimpleBitmapDiskCache(@Nonnull Context context, @Nonnull String subFolder,
			@Nonnegative long purgeAfterSec) throws IOException {
		super(context, CacheLocation.EXTERNAL, PATH + File.separator + subFolder, true);
		Preconditions.checkArgument(purgeAfterSec >= MIN_EXPIRE_IN_SEC);
		mBitmapExpirationSec = purgeAfterSec;
		if (DroidConfig.DEBUG) {
			Log.d(TAG, "Disk cache created at: " + mCacheLocation.getAbsolutePath());
		}
	}

	@Override
	@CheckForNull
	public Bitmap get(@Nonnull String key) {
		return getBitmap(key);
	}

	@Override
	@NotForUIThread
	public boolean put(@Nonnull String key, @Nonnull byte[] image) {
		return putBitmap(key, image);
	}

	@Override
	@NotForUIThread
	public boolean put(@Nonnull String key, @Nonnull Bitmap bitmap) {
		return putBitmap(key, bitmap);
	}

	@Override
	public boolean remove(@Nonnull String key) {
		final File bitmapFile = new File(mCacheLocation, key);
		if (bitmapFile.exists()) { // existing cache item
			return bitmapFile.delete();
		}
		return true;
	}

	@Override
	@NotForUIThread
	public final void clearOld() {
		purge(mBitmapExpirationSec);
	}

	/**
	 * Loads a Bitmap from cache reading and decoding it from the file system if
	 * present.
	 * 
	 * @param fileName
	 * @return The loaded {@link Bitmap}, null if not found or an error occurred
	 *         while decoding the bitmap
	 * @throws IllegalArgumentException
	 *             if fileName is null
	 */
	@CheckForNull
	@NotForUIThread
	protected final Bitmap getBitmap(@Nonnull String fileName) {
		Preconditions.checkNotNull(fileName);
		final File bitmapFile = new File(mCacheLocation, fileName);
		if (bitmapFile.exists()) { // existing cache item
			Bitmap bitmap = null;
			// decode file content into a Bitmap
			DECODE_LOCK.lock();
			try {
				bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());
			} finally {
				DECODE_LOCK.unlock();
			}
			if (bitmap == null) { // file is damaged, delete it
				if (!bitmapFile.delete()) {
					if (DroidConfig.DEBUG) {
						Log.w(TAG, "Damaged cache entry: " + fileName);
					}
				}
			} else {
				/*
				 * This is a cache hit, we don't "touch" the file as the
				 * download date defines the expiration.
				 */
			}
			return bitmap;
		} else {
			return null; // cache miss or errors while decoding the file
		}
	}

	/**
	 * Save a byte array containing an image into the file system putting it in
	 * the cache.
	 * 
	 * @param fileName
	 *            The name of the file to store
	 * @param image
	 *            The byte array containing an image
	 * @return true if successful, false otherwise
	 * @throws IllegalArgumentException
	 *             if fileName or image are null
	 */
	@NotForUIThread
	protected final boolean putBitmap(@Nonnull String fileName, @Nonnull byte[] image) {
		Preconditions.checkNotNull(fileName);
		Preconditions.checkNotNull(image);
		try {
			File bitmapFile = new File(mCacheLocation, fileName);
			// if the cache entry already exists, replace it
			if (bitmapFile.exists() && !bitmapFile.delete()) {
				return false; // cannot delete old file
			}
			Files.write(image, bitmapFile);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Compress and save a Bitmap into the file system putting it in the cache.
	 * 
	 * <b>Note:</b> saving a file from a Bitmap requires a new compression,
	 * avoid it directly storing the original byte array with
	 * {@link #putBitmap(String, byte[])} whenever possible.
	 * 
	 * @param fileName
	 *            The name of the file to store
	 * @param bitmap
	 *            The Bitmap to store
	 * @return true if successful, false otherwise
	 */
	protected final boolean putBitmap(@Nonnull String fileName, @Nonnull Bitmap bitmap) {
		Preconditions.checkNotNull(fileName);
		Preconditions.checkNotNull(bitmap);
		try {
			final File bitmapFile = new File(mCacheLocation, fileName);
			// if the cache entry already exists, replace it
			if (bitmapFile.exists() && !bitmapFile.delete()) {
				return false;
			}
			final FileOutputStream fos = new FileOutputStream(bitmapFile);
			bitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}