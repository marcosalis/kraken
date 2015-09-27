/*
 * Copyright 2013 Luluvise Ltd
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.ContentCache.CacheSource;
import com.github.marcosalis.kraken.cache.bitmap.AnimationMode;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnBitmapSetListener;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.google.common.annotations.Beta;

/**
 * Extension of {@link BitmapAsyncSetter} that starts a fade-in animation right
 * after setting the image bitmap when this is loaded from the disk or network.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class BitmapAnimatedAsyncSetter extends BitmapAsyncSetter {

	private static final String TAG = BitmapAnimatedAsyncSetter.class.getSimpleName();

	protected static final int DEFAULT_ANIM_ID = android.R.anim.fade_in;

	protected final AnimationMode mAnimationMode; // defaults to NOT_IN_MEMORY
	protected final int mCustomAnimationId;

	/* Constructors from superclass */

	public BitmapAnimatedAsyncSetter(@Nonnull CacheUrlKey key, @Nonnull ImageView imgView) {
		this(key, imgView, AnimationMode.NOT_IN_MEMORY, null, -1);
	}

	public BitmapAnimatedAsyncSetter(@Nonnull CacheUrlKey key, @Nonnull ImageView imgView,
			@Nullable OnBitmapSetListener listener) {
		this(key, imgView, AnimationMode.NOT_IN_MEMORY, listener, -1);
	}

	/**
	 * @see BitmapAsyncSetter#BitmapAsyncSetter(ImageView,
	 *      BitmapAsyncSetter#OnBitmapImageSetListener)
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} for the bitmap
	 * @param mode
	 *            The {@link AnimationMode} to use
	 * @param customAnimationId
	 *            The ID of a custom animation to load, or -1 to use the default
	 *            Android fade-in animation.
	 */
	public BitmapAnimatedAsyncSetter(@Nonnull CacheUrlKey key, @Nonnull ImageView imgView,
			@Nonnull AnimationMode mode, @Nullable OnBitmapSetListener listener,
			int customAnimationId) {
		super(key, imgView, listener);
		mAnimationMode = mode;
		mCustomAnimationId = customAnimationId;
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
	 *            The {@link CacheSource} from where the bitmap was loaded
	 */
	protected void setImageBitmap(@Nonnull final ImageView imageView, @Nonnull Bitmap bitmap,
			@Nonnull CacheSource source) {
		// only animate when the bitmap source is compatible with the set mode
		if (shouldAnimate(source, mAnimationMode)) {
			final Animation animation = imageView.getAnimation();
			if (animation != null) { // reuse animation
				if (animation.hasEnded()) {
					// we don't want to animate if we're just setting the same
					// bitmap again in the same view (can happen when scrolling
					// back and forth in a long list)
					final Drawable drawable = imageView.getDrawable();
					if (drawable instanceof BitmapDrawable) {
						final Bitmap oldBitmap = ((BitmapDrawable) drawable).getBitmap();
						if (!bitmap.equals(oldBitmap)) { // different bitmap
							animation.startNow();
							if (BITMAP_DEBUG) { // debugging
								Log.w(TAG,
										"Reusing ended animation in reused view"
												+ imageView.hashCode());
							}
						} else {
							if (BITMAP_DEBUG) { // debugging
								Log.w(TAG, "Same bitmap, do not animate " + imageView.hashCode());
							}
						}
					} else {
						animation.startNow();
						if (BITMAP_DEBUG) { // debugging
							Log.w(TAG,
									"Reusing ended animation in empty view" + imageView.hashCode());
						}
					}
				} else {
					if (BITMAP_DEBUG) { // debugging
						Log.d(TAG, "Old animation still in progress " + imageView.hashCode());
					}
				}
			} else { // create new animation
				int animationId = (mCustomAnimationId != -1) ? mCustomAnimationId : DEFAULT_ANIM_ID;
				final Animation newAnimation = AnimationUtils.loadAnimation(imageView.getContext(),
						animationId);
				newAnimation.setFillAfter(true);
				imageView.startAnimation(newAnimation);
				if (BITMAP_DEBUG) { // debugging
					Log.i(TAG, "Starting new image view animation " + imageView.hashCode());
				}
			}
		}
		super.setImageBitmap(imageView, bitmap, source); // set image bitmap
	}

	/**
	 * Returns whether the bitmap setting should be animate depending on the
	 * current {@link CacheSource} and {@link AnimationMode}.
	 */
	protected static final boolean shouldAnimate(@Nonnull CacheSource source,
			@Nonnull AnimationMode mode) {
		switch (mode) {
		case ALWAYS:
			return true;
		case NOT_IN_MEMORY:
			return source == CacheSource.NETWORK || source == CacheSource.DISK;
		case FROM_NETWORK:
			return source == CacheSource.NETWORK;
		case NEVER:
			return false;
		default:
			throw new IllegalArgumentException("Unsupported animation mode");
		}
	}

}