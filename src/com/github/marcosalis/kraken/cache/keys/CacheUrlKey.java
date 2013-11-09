/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.cache.keys;

import javax.annotation.Nonnull;

import android.annotation.SuppressLint;
import android.os.Parcelable;

import com.github.marcosalis.kraken.utils.HashUtils.CacheKey;
import com.google.common.annotations.Beta;

/**
 * Interface for objects that hold either a String key and a URL representing
 * the resource (or value), to be used as cache key entries.
 * 
 * Implementations <b>must</b> implement {@link Parcelable}
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@SuppressLint("ParcelCreator")
public interface CacheUrlKey extends CacheKey, Parcelable {

	/**
	 * Gets the hash string used as a key
	 */
	@Nonnull
	public String hash();

	/**
	 * Gets the string URL hold by this {@link CacheUrlKey}
	 */
	@Nonnull
	public String getUrl();

}