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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.BitmapSetter;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnBitmapSetListener;
import com.github.marcosalis.kraken.cache.internal.BitmapAnimatedAsyncSetter;
import com.github.marcosalis.kraken.cache.internal.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.keys.SimpleCacheUrlKey;
import com.google.api.client.util.Preconditions;
import com.google.common.annotations.Beta;

/**
 * <p>
 * Main component to access a {@link BitmapCache} from client code to set
 * bitmaps into views.
 * 
 * <p>
 * It allows detailed configuration of how a bitmap is set, while allowing
 * methods chaining in a "builder" style.
 * 
 * <p>
 * The {@link BitmapSetterLocalFactory} is local is the sense that a single
 * instance should be used for a "context" (such an activity) or UI component
 * (as a fragment): it holds a keys cache (which can be cleared by calling
 * {@link #clearLocalCaches()} when the context is closed/destroyed) and it
 * avoids as much as possible the creation of objects, to avoid GC, in
 * performance-critical scenarios, such as a list view scrolling.
 * 
 * <p>
 * Example usage: TODO
 * 
 * 
 * @since 1.0.2
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public final class BitmapSetterLocalFactory {

	private final BitmapCache mCache;
	private final HashMap<String, CacheUrlKey> mKeysCache;

	private CacheUrlKey mKey;
	private AccessPolicy mPolicy = AccessPolicy.NORMAL;
	private AnimationMode mAnimationMode = AnimationMode.NEVER;
	private Drawable mPlaceholder;
	private OnBitmapSetListener mListener;

	public BitmapSetterLocalFactory(@Nonnull BitmapCache cache) {
		mCache = cache;
		mKeysCache = new HashMap<String, CacheUrlKey>();
	}

	@Nonnull
	public BitmapSetterLocalFactory setAsync(@Nonnull String url) {
		if ((mKey = mKeysCache.get(url)) == null) {
			mKey = new SimpleCacheUrlKey(url);
		}
		return this;
	}

	@Nonnull
	public BitmapSetterLocalFactory placeholder(@Nonnull Drawable placeholder) {
		mPlaceholder = placeholder;
		return this;
	}

	@Nonnull
	public BitmapSetterLocalFactory policy(@Nonnull AccessPolicy policy) {
		mPolicy = policy;
		return this;
	}

	@Nonnull
	public BitmapSetterLocalFactory animate(@Nonnull AnimationMode mode) {
		mAnimationMode = mode;
		return this;
	}

	@Nonnull
	public BitmapSetterLocalFactory listener(@Nonnull OnBitmapSetListener listener) {
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

	public void reset() {
		mKey = null;
		mPolicy = AccessPolicy.NORMAL;
		mAnimationMode = AnimationMode.NEVER;
		mPlaceholder = null;
		mListener = null;
	}

	public void clearLocalCaches() {
		mKeysCache.clear();
	}

	private void checkParameters() {
		Preconditions.checkNotNull(mKey);
		Preconditions.checkNotNull(mPolicy);
		Preconditions.checkNotNull(mAnimationMode);
	}

	private BitmapSetter getBitmapSetter(@Nonnull ImageView view) {
		if (mAnimationMode == AnimationMode.NEVER) {
			return new BitmapAsyncSetter(mKey, view, mListener);
		} else {
			return new BitmapAnimatedAsyncSetter(mKey, view, mAnimationMode, mListener, -1);
		}
	}

}