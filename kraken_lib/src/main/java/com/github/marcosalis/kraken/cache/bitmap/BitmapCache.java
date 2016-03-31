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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.ContentCache.CacheSource;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.proxies.ContentProxy;
import com.google.common.annotations.Beta;

/**
 * Public interface to access a {@link Bitmap}s cache.
 *
 * All the methods implementations are asynchronous and must be executed from the UI thread.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
public interface BitmapCache extends ContentProxy {

    /**
     * Callback interface to retrieve the result of a Bitmap loading from a {@link BitmapCache}.
     *
     * The callback methods can be executed in an internal pool thread, not in the caller thread.
     * Make sure you are handling this when dealing with UI and non-thread safe code.
     */
    @Beta
    public interface OnBitmapRetrievalListener {

        /**
         * Called when a Bitmap has been retrieved from the cache.
         *
         * @param key    The {@link CacheUrlKey} of the bitmap
         * @param bitmap The retrieved Bitmap
         * @param source The {@link CacheSource} from where the bitmap has been retrieved
         */
        public void onBitmapRetrieved(@NonNull CacheUrlKey key, @NonNull Bitmap bitmap,
                                      @NonNull CacheSource source);

        /**
         * Called when a bitmap could not be retrieved.
         *
         * @param key The {@link CacheUrlKey} of the bitmap
         * @param e   The exception that caused the error (if any)
         */
        public void onBitmapRetrievalFailed(@NonNull CacheUrlKey key, @Nullable Exception e);
    }

    /**
     * Extension of {@link OnBitmapRetrievalListener} with methods related to setting a {@link
     * Bitmap} inside an {@link ImageView}.
     *
     * @since 1.0.2
     */
    @Beta
    public interface BitmapSetter extends OnBitmapRetrievalListener {

        /**
         * Sets a temporary {@link Drawable} placeholder to the image view or resets the image
         * drawable in the image view by passing null.
         *
         * Note: implementations must set the drawable synchronously, and the method should be
         * called from the UI thread.
         *
         * @param drawable The drawable placeholder (can be null)
         */
        public void setPlaceholder(@Nullable Drawable drawable);
    }

    /**
     * Simple {@link OnBitmapRetrievalListener} implementation to retrieve a successful retrieval of
     * a bitmap.
     */
    @Beta
    public static abstract class OnSuccessfulBitmapRetrievalListener implements
            OnBitmapRetrievalListener {
        @Override
        public final void onBitmapRetrievalFailed(@NonNull CacheUrlKey key, @Nullable Exception e) {
            // does nothing
        }
    }

    /**
     * Callback interface to be used when the caller needs to know if and when the bitmap has
     * actually been set into the image view.
     *
     * @author Marco Salis
     * @since 1.0
     */
    @Beta
    public interface OnBitmapSetListener {

        /**
         * Called from the UI thread when the retrieved bitmap image has been set into the {@link
         * ImageView}
         *
         * @param key    The {@link CacheUrlKey} of the bitmap
         * @param bitmap The set {@link Bitmap}
         * @param source The {@link CacheSource} of the bitmap
         */
        public void onBitmapSet(@NonNull CacheUrlKey key, @NonNull Bitmap bitmap,
                                @NonNull CacheSource source);
    }

    /**
     * <p> Builds a new instance of {@link BitmapSetterBuilder} for this cache. Retain this
     * reference for an activity, component or list view, to optimize bitmap retrieval and setting
     * into views.
     *
     * <p> See {@link BitmapSetterBuilder} documentation for more information.
     *
     * @param allowReuse true to create a {@link BitmapSetterBuilder} that can be reused locally and
     *                   holds caches to improve performances, false to create a one-off builder
     * @return The built instance
     */
    @NonNull
    public BitmapSetterBuilder newBitmapSetterBuilder(boolean allowReuse);

    /**
     * Retrieves a bitmap asynchronously with the specified {@link AccessPolicy}
     *
     * Use {@link #preloadBitmap(CacheUrlKey)} for {@link AccessPolicy#PRE_FETCH}.
     *
     * @param key      The {@link CacheUrlKey} of the bitmap
     * @param policy   The {@link AccessPolicy} to use
     * @param listener {@link OnBitmapRetrievalListener} to get the bitmap if successfully
     *                 retrieved
     * @throws IllegalArgumentException if policy is {@link AccessPolicy#PRE_FETCH}
     */
    public void getBitmapAsync(@NonNull CacheUrlKey key, @NonNull AccessPolicy policy,
                               @NonNull OnBitmapRetrievalListener listener);

    /**
     * Preloads a bitmap into the cache for future use. Does nothing if the bitmap is already in one
     * of the caches.
     *
     * @param key The {@link CacheUrlKey} of the bitmap
     */
    public void preloadBitmap(@NonNull CacheUrlKey key);

    /**
     * Asynchronously sets the retrieved bitmap into the passed image view.
     *
     * @param url  The string URL of the bitmap
     * @param view The {@link ImageView} to set the bitmap into
     */
    public void setBitmapAsync(@NonNull String url, @NonNull ImageView view);

    /**
     * Asynchronously sets the retrieved bitmap into the passed image view.
     *
     * @param key  The {@link CacheUrlKey} of the bitmap
     * @param view The {@link ImageView} to set the bitmap into
     */
    public void setBitmapAsync(@NonNull CacheUrlKey key, @NonNull ImageView view);

    /**
     * <strong>Note:</strong> this method is not officially part of the public interface, and its
     * direct use is not recommended as it is subject to backwards incompatible variations. Call
     * {@link #newBitmapSetterBuilder(boolean)} to get a builder to configure a personalized bitmap
     * setter.
     *
     * Asynchronously sets the retrieved bitmap into an image view with the passed custom
     * parameters.
     *
     * Use {@link #setBitmapAsync(CacheUrlKey, ImageView)} if custom parameters are not needed.
     *
     * @param key         The {@link CacheUrlKey} of the bitmap
     * @param policy      The {@link AccessPolicy} to use
     * @param setter      A {@link BitmapSetter} for the image view
     * @param placeholder A (optional) placeholder {@link Drawable} to set inside the image view
     *                    when the bitmap is not available in memory
     */
    public void setBitmapAsync(@NonNull CacheUrlKey key, @NonNull AccessPolicy policy,
                               @NonNull BitmapSetter setter, @Nullable Drawable placeholder);

}