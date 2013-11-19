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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.annotations.Beta;

/**
 * Public interface that defines objects used to decode bitmaps with a specific
 * policy in a {@link BitmapCache}.
 * 
 * Concrete implementations will likely use the static methods in
 * {@link BitmapFactory} to decode the bitmaps in the required format and
 * configuration.
 * 
 * @since 1.0.1
 * @author Marco Salis
 */
@Beta
public interface BitmapDecoder {

	public Bitmap decode(@Nonnull byte[] data);

	public Bitmap decode(@Nonnull byte[] data, @Nonnull BitmapFactory.Options options);

}