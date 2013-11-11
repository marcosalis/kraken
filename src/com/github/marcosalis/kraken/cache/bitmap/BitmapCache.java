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

import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter.BitmapSource;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.loaders.AccessPolicy;
import com.github.marcosalis.kraken.content.ContentProxy;
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
		 *            The bitmap's {@link CacheUrlKey}
		 * @param bitmap
		 *            The retrieved Bitmap
		 * @param source
		 *            The {@link BitmapSource} from where the bitmap has been
		 *            retrieved
		 */
		public void onBitmapRetrieved(@Nonnull CacheUrlKey key, @Nonnull Bitmap bitmap,
				@Nonnull BitmapSource source);
	}

	@Nonnull
	public Future<Bitmap> getBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nonnull OnBitmapRetrievalListener listener);

	public void preloadBitmap(@Nonnull CacheUrlKey key);

	@Nonnull
	public Future<Bitmap> setBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull BitmapAsyncSetter setter);

	@Nonnull
	public Future<Bitmap> setBitmapAsync(@Nonnull CacheUrlKey key, @Nonnull AccessPolicy policy,
			@Nonnull BitmapAsyncSetter setter, @Nullable Drawable placeholder);

}