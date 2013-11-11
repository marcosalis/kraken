/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.cache.bitmap.utils;

import java.lang.ref.SoftReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnBitmapRetrievalListener;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.google.common.annotations.Beta;

/**
 * <p>
 * Callback class to use with a {@link BitmapCache} to set the bitmap to an
 * {@link ImageView} if this is still existing and attached to an Activity,
 * either synchronously from the UI thread or asynchronously after querying a
 * disk cache or the network from another thread.
 * </p>
 * 
 * <p>
 * In order to ensure that the ImageView still refers to the requested bitmap (=
 * it hasn't been recycled, for example), the setter constructors set the tag of
 * the view ({@link ImageView#setTag(Object)}) to the {@link CacheUrlKey} hash.
 * Do not reset the tag or the bitmap won't be set.
 * </p>
 * 
 * <p>
 * The references to the passed {@link ImageView} and listener are
 * {@link SoftReference}, so that it's possible to safely pass an object that
 * retains a {@link Context} to this object constructors.
 * </p>
 * 
 * TODO: handle placeholder setting when the bitmap loading fails
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class BitmapAsyncSetter implements OnBitmapRetrievalListener {

	/**
	 * Callback interface to be used when the caller needs to know if and when
	 * the bitmap has actually been set into the image view.
	 */
	public interface OnBitmapSetListener {
		/**
		 * Called when the retrieved bitmap image has been set into the
		 * {@link ImageView}
		 * 
		 * @param key
		 *            The {@link CacheUrlKey} of the bitmap
		 * @param bitmap
		 *            The set {@link Bitmap}
		 * @param source
		 *            The {@link BitmapSource} of the bitmap
		 */
		public void onSetIntoImageView(@Nonnull CacheUrlKey key, @Nonnull Bitmap bitmap,
				@Nonnull BitmapSource source);
	}

	/**
	 * Possible origin of a cache item TODO: move from here
	 */
	public enum BitmapSource {
		MEMORY,
		DISK,
		NETWORK;
	}

	/**
	 * Low-level debug mode for bitmap debugging (disabled by default).
	 */
	protected static final boolean BITMAP_DEBUG = DroidConfig.DEBUG && false;

	private static final String TAG = BitmapAsyncSetter.class.getSimpleName();

	/**
	 * Private UI-thread created handler used to post to image views
	 */
	private static final Handler mHandler = new Handler(Looper.getMainLooper());

	private final CacheUrlKey mCacheKey;
	/* we only use soft references to avoid possible memory leaks */
	private final SoftReference<ImageView> mImageView;
	private final SoftReference<OnBitmapSetListener> mListener;

	/**
	 * Creates a new {@link BitmapAsyncSetter} with no listener.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} for the bitmap
	 * @param imgView
	 *            The {@link ImageView} to set the bitmap into
	 */
	public BitmapAsyncSetter(@Nonnull CacheUrlKey key, @Nonnull ImageView imgView) {
		this(key, imgView, null);
	}

	/**
	 * Creates a new {@link BitmapAsyncSetter}.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} for the bitmap
	 * @param imgView
	 *            The {@link ImageView} to set the bitmap into
	 * @param listener
	 *            The listener to be called when the image gets set (can be
	 *            null)
	 */
	public BitmapAsyncSetter(@Nonnull CacheUrlKey key, @Nonnull ImageView imgView,
			@Nullable OnBitmapSetListener listener) {
		mCacheKey = key;
		imgView.setTag(key.hash()); // set view tag for identification
		mImageView = new SoftReference<ImageView>(imgView);
		if (listener != null) {
			mListener = new SoftReference<OnBitmapSetListener>(listener);
		} else {
			mListener = null;
		}
	}

	/**
	 * Sets a temporary {@link Drawable} placeholder to the image view.
	 * 
	 * Note: only call this from the UI thread.
	 * 
	 * @param drawable
	 *            The drawable placeholder (can be null)
	 */
	public void setPlaceholderSync(@Nullable Drawable drawable) {
		final ImageView view = mImageView.get();
		if (view != null) {
			view.setImageDrawable(drawable);
		}
	}

	/**
	 * Note: only call this from the UI thread.
	 * 
	 * @param bitmap
	 *            The {@link Bitmap} image to set
	 */
	@OverridingMethodsMustInvokeSuper
	public void setBitmapSync(@Nonnull Bitmap bitmap) {
		final ImageView view = mImageView.get();
		if (view != null) {
			setImageBitmap(view, bitmap, BitmapSource.MEMORY);
			if (mListener != null) { // notify caller
				final OnBitmapSetListener listener = mListener.get();
				if (listener != null) {
					listener.onSetIntoImageView(mCacheKey, bitmap, BitmapSource.MEMORY);
				}
			}
			mImageView.clear();
		} else if (BITMAP_DEBUG) { // debugging
			Log.w(TAG, "Trying to access synchronously null image view!");
		}
	}

	@Override
	public final void onBitmapRetrieved(@Nonnull CacheUrlKey key, @Nonnull Bitmap bitmap,
			@Nonnull BitmapSource source) {
		setBitmapAsync(bitmap, source);
	}

	/**
	 * Asynchronous callback to use from other threads for asynchronously
	 * attempting to set a {@link Bitmap} to an {@link ImageView} if this is not
	 * null and image tags match.
	 * 
	 * @param bitmap
	 *            The {@link Bitmap} image to set
	 * @param source
	 *            The {@link BitmapSource} of the bitmap
	 */
	@NotForUIThread
	@OverridingMethodsMustInvokeSuper
	protected synchronized void setBitmapAsync(@Nonnull Bitmap bitmap,
			@Nonnull final BitmapSource source) {
		// do not pass this reference to the runnable
		final ImageView view = mImageView.get();
		if (view != null) {
			final SoftReference<Bitmap> bitmapRef = new SoftReference<Bitmap>(bitmap);

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					final ImageView innerViewRef = mImageView.get();
					// clears soft reference to avoid this being called twice
					mImageView.clear();
					final Bitmap bitmap = bitmapRef.get();
					if (innerViewRef != null) { // context still valid
						if (bitmap != null) { // bitmap not GC'd yet
							final Object tag = innerViewRef.getTag();
							if (tag != null && tag.equals(mCacheKey.hash())) {
								setImageBitmap(innerViewRef, bitmap, source);
								if (mListener != null) { // notify caller
									final OnBitmapSetListener listener = mListener.get();
									if (listener != null) {
										listener.onSetIntoImageView(mCacheKey, bitmap, source);
										mListener.clear();
									}
								}
							} else if (BITMAP_DEBUG) { // debugging
								Log.v(TAG, "Runnable: view tag not matching: " + mCacheKey.getUrl());
							}
						} else if (BITMAP_DEBUG) { // debugging
							Log.v(TAG, "Runnable: null bitmap reference: " + mCacheKey.getUrl());
						}
					} else if (BITMAP_DEBUG) { // debugging
						Log.d(TAG, "Runnable: null image view: " + mCacheKey.getUrl());
					}
					// remove runnable from the handler queue
					mHandler.removeCallbacks(this);
				}
			});
		} else if (BITMAP_DEBUG) { // debugging
			Log.d(TAG, "Async: null image view: " + mCacheKey.getUrl());
		}
	}

	/**
	 * Method that effectively sets a bitmap image for the passed
	 * {@link ImageView}. The default implementation just calls
	 * {@link ImageView#setImageBitmap(Bitmap)}, override to provide custom
	 * behavior or animations when setting the bitmap.
	 * 
	 * @param imageView
	 *            The {@link ImageView} to set the bitmap into
	 * @param bitmap
	 *            The {@link Bitmap} to set
	 * @param source
	 *            The origin {@link BitmapSource} of the bitmap
	 */
	protected void setImageBitmap(@Nonnull ImageView imageView, @Nonnull Bitmap bitmap,
			@Nonnull BitmapSource source) {
		imageView.setImageBitmap(bitmap);
	}

}