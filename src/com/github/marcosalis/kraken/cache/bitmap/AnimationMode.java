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

import android.widget.ImageView;

import com.google.common.annotations.Beta;

/**
 * Enumerates all possible animation policies for setting a bitmap into an
 * {@link ImageView}, depending on the bitmap loading source.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public enum AnimationMode {
	/**
	 * Use no animation (same as using a normal {@link BitmapAsyncSetter})
	 */
	NEVER,
	/**
	 * Animate only if the bitmap is not already in the memory caches
	 */
	NOT_IN_MEMORY,
	/**
	 * Animate only if the bitmap was loaded from network
	 */
	FROM_NETWORK,
	/**
	 * Always animate (use with care: the performance impact can be
	 * noticeable when scrolling long lists of bitmaps)
	 */
	ALWAYS;
}