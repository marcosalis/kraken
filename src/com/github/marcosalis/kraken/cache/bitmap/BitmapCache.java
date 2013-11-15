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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter.BitmapSource;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.proxies.ContentProxy;
import com.google.common.annotations.Beta;

/**
 * Public interface to access a {@link Bitmap}s cache.
 * 
 * All the methods implementations are asynchronous and must be executed from
 * the UI thread.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface BitmapCache extends ContentProxy {

	/**
	 * Callback interface to retrieve the result of a Bitmap loading from a
	 * {@link BitmapCache}.
	 * 
	 * The callback methods can be executed in an internal pool thread, not in
	 * the caller thread. Make sure you are handling this when dealing with UI
	 * and non-thread safe code.
	 */
	@Beta
	public interface OnBitmapRetrievalListener {

		/**
		 * Called when a Bitmap has been retrieved from the cache.
		 * 
		 * @param key
		 *            The {@link CacheUrlKey} of the bitmap
		 * @param bitmap
		 *            The retrieved Bitmap
		 * @param source
		 *            The {@link BitmapSource} from where the bitmap has been
		 *            retrieved
		 */
		public void onBitmapRetrieved(@Nonnull CacheUrlKey key, @Nonnull Bitmap bitmap,
				@Nonnull BitmapSource source);

		/**
		 * Called when a bitmap could not be retrieved.
		 * 
		 * @param key
		 *            The {@link CacheUrlKey} of the bitmap
		 * @param e
		 *            The exception that caused the error (if any)
		 */
		public void onBitmapRetrievalFailed(@Nonnull CacheUrlKey key, @Nullable Exception e);
	}

	/**
	 * Simple {@link OnBitmapRetrievalListener} implementation to retrieve a
	 * successful retrieval of a bitmap.
	 */
	@Beta
	public static abstract class OnSuccessfulBitmapRetrievalListener implements
			OnBitmapRetrievalListener {
		@Override
		public final void onBitmapRetrievalFailed(@Nonnull CacheUrlKey key, @Nullable Exception e) {
			// does nothing
		}
	}

	/**
	 * Retrieves a bitmap asynchronously with the specified {@link AccessPolicy}
	 * 
	 * Use {@link #preloadBitmap(CacheUrlKey)} for
	 * {@link AccessPolicy#PRE_FETCH}.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} of the bitmap
	 * @param policy
	 *            The {@link AccessPolicy} to use
	 * @param listener
	 *            {@link OnSuccessfulBitmapRetrievalListener} to get the bitmap
	 *            if successfully retrieved
	 * @throws IllegalArgumentException
	 *             if policy is {@link AccessPolicy#PRE_FETCH}
	 */
	@Nonnull
	public void getBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nonnull OnSuccessfulBitmapRetrievalListener listener);

	/**
	 * Preloads a bitmap into the cache for future use. Does nothing if the
	 * bitmap is already in one of the caches.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} of the bitmap
	 */
	public void preloadBitmap(@Nonnull CacheUrlKey key);

	/**
	 * Asynchronously sets the retrieved bitmap into the passed image view.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} of the bitmap
	 * @param view
	 *            The {@link ImageView} to set the bitmap into
	 */
	@Nonnull
	public void setBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull ImageView view);

	/**
	 * Asynchronously sets the retrieved bitmap into an image view with the
	 * passed custom parameters.
	 * 
	 * Use {@link #setBitmapAsync(CacheUrlKey, ImageView)} if custom parameters
	 * are not needed.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} of the bitmap
	 * @param policy
	 *            The {@link AccessPolicy} to use
	 * @param setter
	 *            A {@link BitmapAsyncSetter} holding the image view
	 * @param placeholder
	 *            A (optional) placeholder {@link Drawable} to set inside the
	 *            image view when the bitmap is not available in memory
	 */
	@Nonnull
	public void setBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nonnull BitmapAsyncSetter setter, @Nullable Drawable placeholder);

}