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
package com.github.marcosalis.kraken.cache.bitmap.memory;

import android.graphics.Bitmap;

import com.github.marcosalis.kraken.cache.MemoryCache;
import com.google.common.annotations.Beta;

/**
 * Public interface for a 1st level, memory based cache for bitmaps. It holds no
 * additional methods at the moment.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public interface BitmapMemoryCache<K> extends MemoryCache<K, Bitmap> {

	/**
	 * Default maximum percentage of the application memory to use when
	 * configuring a bitmap memory cache.
	 */
	public static final float DEFAULT_MAX_MEMORY_PERCENTAGE = 10f;

}