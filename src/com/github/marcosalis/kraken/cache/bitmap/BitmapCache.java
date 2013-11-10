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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.cache.loaders.AccessPolicy;
import com.github.marcosalis.kraken.content.ContentProxy;
import com.google.common.annotations.Beta;

/**
 * Public interface to access a {@link Bitmap} cache.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface BitmapCache extends ContentProxy {

	@CheckForNull
	public Future<Bitmap> getBitmapAsync(@Nonnull CacheUrlKey key,
			@Nullable BitmapAsyncSetter setter);

	@CheckForNull
	public Future<Bitmap> getBitmapAsync(@Nonnull CacheUrlKey key, @Nullable AccessPolicy policy,
			@Nullable BitmapAsyncSetter setter, @CheckForNull Drawable placeholder);

}