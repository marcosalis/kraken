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

import java.util.HashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.BitmapSetter;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnBitmapSetListener;
import com.github.marcosalis.kraken.cache.bitmap.internal.BitmapAnimatedAsyncSetter;
import com.github.marcosalis.kraken.cache.bitmap.internal.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.keys.SimpleCacheUrlKey;
import com.google.api.client.util.Preconditions;
import com.google.common.annotations.Beta;

/**
 * <p>
 * Default component to access a {@link BitmapCache} from client code to set
 * bitmaps into views and build a {@link BitmapSetter} for the cache.
 * 
 * <p>
 * It allows detailed configuration of how a bitmap is set, while allowing
 * methods chaining in a "builder" style in order to make the cache accesses
 * code short and clean.
 * 
 * <p>
 * The {@link BitmapSetterBuilder} can work in two different modes, one-off and
 * local. When working in <i>local</i> mode (by calling
 * {@link BitmapCache#newBitmapSetterBuilder(true)} and retaining the instance),
 * a single instance of the builder can be re-used within a "context" (such an
 * activity) or UI component (as a fragment): it holds local caches for internal
 * elements (which can be cleared by calling {@link #clearLocalCaches()} when
 * the context is closed/destroyed or the data source changes) and it avoids as
 * much as possible the creation of objects, to avoid GC, in
 * performance-critical scenarios, such as a list view scrolling.<br>
 * A <i>one-off</i> instance (obtained by calling
 * {@link BitmapCache#newBitmapSetterBuilder(false)}) allows using the builder
 * for a single bitmap setting, which is recommended when only a few bitmaps
 * need to be set.
 * 
 * <p>
 * <strong>Example usage</strong><br>
 * Retrieve a new instance of the builder by calling
 * {@link BitmapCache#newBitmapSetterBuilder(boolean)} then chain calls on the
 * builder to start the asynchronous request: <code><pre>
 * builder.setAsync("http://www.example.com/image.jpg")
 * 	.placeholder(placeholderDrawable)
 * 	.policy(AccessPolicy.NORMAL)
 * 	.animate(AnimationMode.NOT_IN_MEMORY)
 * 	.listener(new OnBitmapSetListener() {
 * 		public void onBitmapSet(CacheUrlKey key, Bitmap bitmap, CacheSource source) {
 * 			// called when the bitmap is set
 * 		}
 * 	})
 * 	.into(imageView);
 * </pre></code>
 * 
 * @since 1.0.2
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public final class BitmapSetterBuilder {

	private final BitmapCache mCache;
	private final boolean mCachingEnabled;

	@CheckForNull
	private HashMap<String, CacheUrlKey> mKeysCache;

	private CacheUrlKey mKey;
	private AccessPolicy mPolicy = AccessPolicy.NORMAL;
	private AnimationMode mAnimationMode = AnimationMode.NEVER;
	private Drawable mPlaceholder;
	private OnBitmapSetListener mListener;

	/**
	 * Do not use this constructor directly, call
	 * {@link BitmapCache#newBitmapSetterBuilder(boolean)}.
	 * 
	 * @param cache
	 */
	public BitmapSetterBuilder(@Nonnull BitmapCache cache) {
		this(cache, true);
	}

	/**
	 * Do not use this constructor directly, call
	 * {@link BitmapCache#newBitmapSetterBuilder(boolean)}.
	 * 
	 * @param cache
	 * @param reusable
	 */
	public BitmapSetterBuilder(@Nonnull BitmapCache cache, boolean reusable) {
		mCache = cache;
		mCachingEnabled = reusable;
	}

	/**
	 * Sets the download URL for the bitmap.
	 * 
	 * @param url
	 *            The URL string
	 * @return This builder for call chaining
	 */
	@Nonnull
	public BitmapSetterBuilder setAsync(@Nonnull String url) {
		mKey = getKeyFromCacheOrCreate(url);
		return this;
	}

	private CacheUrlKey getKeyFromCacheOrCreate(@Nonnull String url) {
		if (mCachingEnabled) {
			if (mKeysCache == null) { // lazily initialized
				mKeysCache = new HashMap<String, CacheUrlKey>();
			}
			CacheUrlKey key;
			if ((key = mKeysCache.get(url)) == null) {
				key = new SimpleCacheUrlKey(url);
				mKeysCache.put(url, key);
			}
			return key;
		} else {
			return new SimpleCacheUrlKey(url);
		}
	}

	/**
	 * Sets the {@link CacheUrlKey} for the bitmap.
	 * 
	 * <strong>Note: </strong> calling this method instead of
	 * {@link #setAsync(String)} would prevent any internal key cache from
	 * saving on objects instantiation. Only use to provide a
	 * {@link CacheUrlKey} customized behavior.
	 * 
	 * @param key
	 *            The {@link CacheUrlKey} for the bitmap
	 * @return This builder for call chaining
	 */
	@Nonnull
	public BitmapSetterBuilder setAsync(@Nonnull CacheUrlKey key) {
		mKey = key;
		return this;
	}

	/**
	 * Sets a temporary placeholder to be set into the view when the requested
	 * bitmap is not in the memory cache.
	 * 
	 * If not set, the image view drawable will be just set to null.
	 * 
	 * @param placeholder
	 *            The placeholder {@link Drawable}
	 * @return This builder for call chaining
	 */
	@Nonnull
	public BitmapSetterBuilder placeholder(@Nullable Drawable placeholder) {
		mPlaceholder = placeholder;
		return this;
	}

	/**
	 * Sets the bitmap retrieval to the passed {@link AccessPolicy}. Defaults to
	 * {@link AccessPolicy#NORMAL} if not called.
	 * 
	 * @param policy
	 *            The access policy to retrieve the bitmap
	 * @return This builder for call chaining
	 */
	@Nonnull
	public BitmapSetterBuilder policy(@Nonnull AccessPolicy policy) {
		mPolicy = policy;
		return this;
	}

	/**
	 * Sets the animation mode when setting the bitmap into the view. Defaults
	 * to {@link AnimationMode#NEVER}.
	 * 
	 * @param mode
	 *            The {@link AnimationMode} for the bitmap
	 * @return This builder for call chaining
	 */
	@Nonnull
	public BitmapSetterBuilder animate(@Nonnull AnimationMode mode) {
		mAnimationMode = mode;
		return this;
	}

	/**
	 * Sets an {@link OnBitmapSetListener} to be notified when the bitmap is set
	 * into the view.
	 * 
	 * @param listener
	 *            The listener (can be safely a context, it won't be leaked)
	 * @return This builder for call chaining
	 */
	@Nonnull
	public BitmapSetterBuilder listener(@Nonnull OnBitmapSetListener listener) {
		mListener = listener;
		return this;
	}

	/**
	 * Starts the asynchronous bitmap retrieval by calling the
	 * {@link BitmapCache}.
	 * 
	 * After this call, all the parameters added to the builder are reset to the
	 * default state.
	 * 
	 * @param view
	 *            The {@link ImageView} to set the bitmap into
	 * @throws NullPointerException
	 *             if any of the mandatory parameters were not set
	 */
	public void into(@Nonnull ImageView view) {
		checkParameters();

		// build setter
		final BitmapSetter setter = getBitmapSetter(view);

		// start the asynchronous retrieval
		mCache.setBitmapAsync(mKey, mPolicy, setter, mPlaceholder);
		reset();
	}

	/**
	 * @param view
	 * @throws UnsupportedOperationException
	 */
	public void asBackground(@Nonnull View view) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	/**
	 * Resets the builder to its default parameters. This method does
	 * *<i>not</i>* clear the builder local caches.
	 */
	public void reset() {
		mKey = null;
		mPolicy = AccessPolicy.NORMAL;
		mAnimationMode = AnimationMode.NEVER;
		mPlaceholder = null;
		mListener = null;
	}

	/**
	 * Clears the builder local caches. Call this when the "context" where the
	 * builder is used is not valid or the dataset changes.
	 */
	public void clearLocalCaches() {
		if (mKeysCache != null) {
			mKeysCache.clear();
		}
	}

	private void checkParameters() {
		Preconditions.checkNotNull(mKey);
		Preconditions.checkNotNull(mPolicy);
		Preconditions.checkNotNull(mAnimationMode);
	}

	@Nonnull
	private BitmapSetter getBitmapSetter(@Nonnull ImageView view) {
		if (mAnimationMode == AnimationMode.NEVER) {
			return new BitmapAsyncSetter(mKey, view, mListener);
		} else {
			return new BitmapAnimatedAsyncSetter(mKey, view, mAnimationMode, mListener, -1);
		}
	}

}