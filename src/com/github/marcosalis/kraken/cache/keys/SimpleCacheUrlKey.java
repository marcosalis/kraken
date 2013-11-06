/*
 * Copyright 2013 Luluvise Ltd
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
import javax.annotation.concurrent.Immutable;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.marcosalis.kraken.utils.HashUtils;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Simple class that holds either a URL string and generates a shorter file
 * system friendly String key to be used for memory and disk caches.
 * 
 * Prefer to {@link URICacheUrlKey} when there is no URL parameter striping
 * needed.
 * 
 * It implements {@link Parcelable}, so that it can be passed into Bundles.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class SimpleCacheUrlKey implements CacheUrlKey {

	/**
	 * Currently used hash function (murmur3 128 bit algorithm)
	 */
	protected static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();

	@Nonnull
	private final String mKey;
	@Nonnull
	private final String mUrl;

	/**
	 * Generate a unique key from the passed URL, ignoring any query parameters
	 * or URL fragments.
	 * 
	 * @param url
	 *            The URL to be cached
	 * @throws NullPointerException
	 *             if the passed string URL is null
	 */
	public SimpleCacheUrlKey(@Nonnull String url) {
		Preconditions.checkNotNull(url);
		mUrl = url;
		// process URI
		mKey = HashUtils.getHash(HASH_FUNCTION, url);
	}

	public SimpleCacheUrlKey(Parcel source) {
		// reconstruct from the Parcel
		mUrl = source.readString();
		mKey = source.readString();
	}

	/*
	 * Needed for the Parcelable functionalities
	 */

	public static final Parcelable.Creator<SimpleCacheUrlKey> CREATOR = new Parcelable.Creator<SimpleCacheUrlKey>() {
		@Override
		public SimpleCacheUrlKey createFromParcel(Parcel source) {
			return new SimpleCacheUrlKey(source);
		}

		@Override
		public SimpleCacheUrlKey[] newArray(int size) {
			return new SimpleCacheUrlKey[size];
		}
	};

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUrl);
		dest.writeString(mKey);
	}

	/**
	 * Gets the generated key for this object
	 */
	@Nonnull
	@Override
	public String hash() {
		return mKey;
	}

	/**
	 * Gets the full URL for GET requests
	 */
	@Nonnull
	@Override
	public String getUrl() {
		return mUrl;
	}

}