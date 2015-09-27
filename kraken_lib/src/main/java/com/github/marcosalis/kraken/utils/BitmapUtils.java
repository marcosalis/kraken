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
package com.github.marcosalis.kraken.utils;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Helper class containing static utility methods for handling bitmaps.<br>
 * 
 * Documentation references:
 * 
 * <pre>
 * <ul>
 * <li>{@link http://developer.android.com/reference/android/graphics/Bitmap.html}</li>
 * <li>{@link http://developer.android.com/reference/android/graphics/BitmapFactory.html}</li>
 * <li>{@link http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html}</li>
 * <li>{@link http://developer.android.com/training/displaying-bitmaps/index.html}</li>
 * </ul>
 * </pre>
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class BitmapUtils {

	@SuppressWarnings("unused")
	private static final String TAG = BitmapUtils.class.getSimpleName();

	private BitmapUtils() {
		// hidden constructor, no instantiation needed
	}

	/**
	 * Calculates the expected memory byte occupation of a bitmap with the given
	 * width and height. Use {@link BitmapUtils#getSize(Bitmap)} if you hold a
	 * reference of the Bitmap to get a more accurate measurement.
	 * 
	 * The current implementation just calculates {@code width * height * 4}
	 * 
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 * @return The actual size in bytes
	 */
	@Nonnegative
	public static int getSize(@Nonnegative int width, @Nonnegative int height) {
		return width * height * 4;
	}

	/**
	 * Calculates the byte occupation of the passed Bitmap, in a
	 * backwards-compatible fashion ({@link Bitmap#getByteCount()} is available
	 * from API 12 onwards).
	 * 
	 * @param bitmap
	 * @return The actual size in bytes
	 */
	@Nonnegative
	@SuppressLint("NewApi")
	public static int getSize(@Nonnull Bitmap bitmap) {
		final int sdkInt = Build.VERSION.SDK_INT;
		if (sdkInt < 12) {
			// getBytesCount() on API 12 does exactly the same
			return bitmap.getRowBytes() * bitmap.getHeight();
		} else if (sdkInt < 19) {
			return bitmap.getByteCount();
		} else { // needed from API 19
			return bitmap.getAllocationByteCount();
		}
	}

	/**
	 * Calculates the inSampleSize option from {@link BitmapFactory.Options()}
	 * for decoding a Bitmap which is big enough to fit the required passed
	 * size.
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return The calculated inSampleSize
	 */
	@Nonnegative
	public static int calculateInSampleSize(@Nonnull BitmapFactory.Options options,
			@Nonnegative int reqWidth, @Nonnegative int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}

		if (inSampleSize > 3 && !isPowerOfTwo(inSampleSize)) {
			// calculate next lower power of two from the number
			inSampleSize = getNextLowerTwoPow(inSampleSize);
		}

		return inSampleSize;
	}

	/**
	 * Calculates the inSampleSize option from {@link BitmapFactory.Options()}
	 * for decoding a Bitmap whose maximum side is always less the passed pixel
	 * value.
	 * 
	 * This is useful when we're dealing with very big bitmaps and we don't want
	 * in any case them to be bigger than the specified value. The inSampleSize
	 * calculated is not necessarily a power of two and can result in an image a
	 * lot smaller than the size we wanted.
	 * 
	 * @param options
	 *            The {@link BitmapFactory.Options()} to use
	 * @param maxSide
	 *            The max size, in pixel, the resulting Bitmap must have
	 * @return The calculated inSampleSize
	 */
	public static int calculateMaxInSampleSize(@Nonnull BitmapFactory.Options options,
			@Nonnegative int maxSide) {
		// raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > maxSide || width > maxSide) {
			if (width > height) {
				inSampleSize = (int) Math.ceil(width / (double) maxSide);
			} else {
				inSampleSize = (int) Math.ceil(height / (double) maxSide);
			}
		}

		return inSampleSize;
	}

	@VisibleForTesting
	static boolean isPowerOfTwo(@Nonnegative @Nonnull int number) {
		Preconditions.checkArgument(number > 0);
		return (number & -number) == number;
	}

	/**
	 * Calculates the next lower power of 2 of a given positive number.
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code number < 0}
	 */
	@VisibleForTesting
	static int getNextLowerTwoPow(int number) {
		Preconditions.checkArgument(number >= 0);
		return (int) Math.pow(2, Math.floor(Math.log(number) / Math.log(2)));
	}

	/**
	 * Decodes a Bitmap from resources which is big enough to fit the required
	 * passed size.
	 * 
	 * @param res
	 *            The {@link Resources} to get the drawable from
	 * @param resId
	 *            The drawable resource ID
	 * @param reqWidth
	 * @param reqHeight
	 * @return The decoded Bitmap or null if something went wrong
	 */
	@CheckForNull
	public static Bitmap decodeSampledBitmapFromResource(@Nonnull Resources res, int resId,
			@Nonnegative int reqWidth, @Nonnegative int reqHeight) {

		// First decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	private static final ImmutableList<String> MEDIA_COLUMNS = ImmutableList.of(
			MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION);

	/**
	 * Returns the available columns for the media content provider for images.
	 * 
	 * Only available from API >= 16:<br>
	 * MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT
	 */
	@Nonnull
	public static final String[] getImagesMediaColumns() {
		return MEDIA_COLUMNS.toArray(new String[MEDIA_COLUMNS.size()]);
	}

	/**
	 * Loads a Bitmap from an URI using the passed ContentResolver, which is
	 * scaled to match at least the required width and height.
	 * 
	 * @param cr
	 *            The {@link ContentResolver} to use
	 * @param picUri
	 *            The picture Uri
	 * @param reqWidth
	 *            The minimum required width
	 * @param reqHeight
	 *            The minimum required height
	 * @return The Bitmap, or null if the URI didn't match any resource
	 */
	@CheckForNull
	public static Bitmap loadBitmapFromUri(@Nonnull ContentResolver cr, @Nonnull Uri picUri,
			@Nonnegative int reqWidth, @Nonnegative int reqHeight) {
		// TODO: handle bitmap orientation
		final String[] mediaColumns = getImagesMediaColumns();
		final Cursor cursor = cr.query(picUri, mediaColumns, null, null, null);
		if (cursor != null && cursor.moveToFirst()) { // we found the image
			final String picturePath = cursor.getString(cursor.getColumnIndex(mediaColumns[0]));
			cursor.close();
			return loadBitmapFromPath(picturePath, reqWidth, reqHeight);
		} else {
			return null;
		}
	}

	/**
	 * See {@link #loadBitmapFromPath(String, int, int, boolean)}.
	 * 
	 * Rotate flag set to true by default.
	 */
	@CheckForNull
	public static Bitmap loadBitmapFromPath(@Nonnull String picturePath, @Nonnegative int reqWidth,
			@Nonnegative int reqHeight) {
		return loadBitmapFromPath(picturePath, reqWidth, reqHeight, true);
	}

	/**
	 * Loads an immutable Bitmap object from the given path.
	 * 
	 * @param picturePath
	 * @param reqWidth
	 *            The minimum required width
	 * @param reqHeight
	 *            The minimum required height
	 * @param rotate
	 *            true to check for the EXIF orientation tag (if existing) and
	 *            rotate the Bitmap accordingly if necessary, false to leave the
	 *            default orientation
	 * @return The Bitmap, or null if the path wasn't valid
	 */
	@CheckForNull
	public static Bitmap loadBitmapFromPath(@Nonnull String picturePath, int reqWidth,
			int reqHeight, boolean rotate) {
		int rotateValue = 0;
		if (rotate) { // check for orientation
			int orientation = getExifOrientation(picturePath);
			rotateValue = getRotationAngleFromOrientation(orientation);
			// invert width and height if the image flips
			if (rotateValue == 90 || rotateValue == 270) {
				int oldW = reqWidth;
				reqWidth = reqHeight;
				reqHeight = oldW;
			}
		}
		// First decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// actually decode the sampled bitmap
		options.inJustDecodeBounds = false;

		Bitmap bitmap;

		if (rotateValue != 0 && DroidUtils.isMinimumSdkLevel((11))) { // HONEYCOMB
			// we can rotate the mutable bitmap in-place without copying it
			bitmap = decodeMutableBitmap(picturePath, options);
			final Canvas canvas = new Canvas(bitmap);
			canvas.rotate(rotateValue);
		} else { // pre-HONEYCOMB or no rotation needed
			bitmap = BitmapFactory.decodeFile(picturePath, options);
			if (rotateValue != 0) {
				// TODO: is there a less memory-consuming way to do this?
				final Matrix matrix = new Matrix();
				matrix.setRotate(rotateValue);
				final Bitmap originalBitmap = bitmap;
				bitmap = Bitmap.createBitmap(originalBitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), matrix, false);
				originalBitmap.recycle();
			}
		}
		return bitmap;
	}

	@TargetApi(11)
	private static Bitmap decodeMutableBitmap(@Nonnull String picturePath,
			@Nonnull BitmapFactory.Options options) {
		options.inMutable = true;
		return BitmapFactory.decodeFile(picturePath, options);
	}

	/**
	 * Gets the orientation of a JPEG file with EXIF attributes
	 * 
	 * @param path
	 *            The path of the JPEG to analyse
	 * @return The orientation constant, see {@link ExifInterface}
	 * @throws IOException
	 */
	public static int getExifOrientation(@Nonnull String path) {
		ExifInterface exif = null;
		final int defOrientation = ExifInterface.ORIENTATION_NORMAL;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e) {
			LogUtils.logException(e);
			return defOrientation;
		}
		return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, defOrientation);
	}

	/**
	 * Gets the rotation to apply to a picture given its EXIF orientation tag.
	 */
	public static int getRotationAngleFromOrientation(int orientation) {
		int rotateValue = 0;
		switch (orientation) {
		// sums up rotation value
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotateValue += 90;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotateValue += 90;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotateValue += 90;
			break;
		}
		return rotateValue;
	}

	/**
	 * Loads and crops an image to be used as a squared picture, mostly to be
	 * used as a "profile" picture of some sort: if the image is in portrait
	 * format, it is cropped from its bottom. If the image is in landscape
	 * format, the image is cropped to its center to make it squared.
	 * 
	 * @param url
	 *            The path where to retrieve the image (must be not null)
	 * @param maxSide
	 *            The maximum required image side (the cropped resulting image
	 *            may be smaller)
	 * @return The cropped {@link Bitmap} or null if an error occurred
	 */
	@CheckForNull
	public static Bitmap cropProfileBitmap(@Nonnull String picturePath, int maxSide) {
		Preconditions.checkArgument(maxSide > 0);

		int rotateValue = 0;
		// check for orientation
		int orientation = getExifOrientation(picturePath);
		rotateValue = getRotationAngleFromOrientation(orientation);

		// First decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);

		// Calculate inSampleSize "memory-defensively"
		options.inSampleSize = calculateMaxInSampleSize(options, maxSide);
		// actually decode the sampled bitmap
		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
		if (bitmap == null) {
			return null;
		}
		// do the actual cropping
		return cropSquaredBitmap(bitmap, rotateValue);
	}

	/**
	 * Crops the passed bitmap to be used as a squared "profile" picture. The
	 * same {@link Bitmap} is returned if already squared.
	 * 
	 * See {@link #cropProfileBitmap(String, int)}.
	 * 
	 * @param bitmap
	 *            The bitmap to crop (must already be resized if necessary)
	 * @return The cropped {@link Bitmap} or null if an error occurred
	 */
	@CheckForNull
	public static Bitmap cropProfileBitmap(@Nonnull Bitmap bitmap) {
		if (bitmap.getWidth() == bitmap.getHeight()) {
			return bitmap; // already squared, just return
		} else {
			return cropSquaredBitmap(bitmap, 0);
		}
	}

	/**
	 * Crops the passed bitmap in the center to make it squared.
	 * 
	 * @param bitmap
	 *            The bitmap to crop (must already be resized if necessary)
	 * @param rotateValue
	 *            The rotate value for the {@link Matrix} if needed
	 * @return The processed {@link Bitmap}
	 */
	@CheckForNull
	private static Bitmap cropSquaredBitmap(@Nonnull Bitmap bitmap, int rotateValue) {
		// cropping calculations
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		boolean portrait = height > width;
		int squaredSize, cropOffset, vertOffset = 0;
		if (portrait) { // crop from the bottom of the picture
			squaredSize = width;
			cropOffset = 0;
			vertOffset = 1; // using 0 here doesn't work
		} else { // landscape: crop to the center
			squaredSize = height;
			cropOffset = (width - squaredSize) / 2;
		}

		final Bitmap originalBitmap = bitmap;
		if (rotateValue == 0 && cropOffset == 0 && width == height) {
			// we don't need to do anything, return the original image
			return originalBitmap;
		} else {
			final Matrix matrix = new Matrix();
			matrix.setRotate(rotateValue);
			final Bitmap cropped = Bitmap.createBitmap(originalBitmap, cropOffset, vertOffset,
					squaredSize, squaredSize, matrix, true);
			return cropped;
		}
	}

}