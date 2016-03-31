/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.cache.keys;

import java.net.URI;

import android.support.annotation.NonNull;
import javax.annotation.concurrent.Immutable;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.marcosalis.kraken.utils.HashUtils;
import com.google.common.annotations.Beta;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Simple class that holds either an URI of a resource and generates a shorter
 * file system friendly String key to be used for memory and disk caches.
 * 
 * The main purpose of this implementation of {@link CacheUrlKey} is to allow
 * URLs with expiration parameters (as the Amazon AWS ones can be) to be trimmed
 * and used as cache keys, by simply using the only path to generate the key
 * hash itself.
 * 
 * Use {@link SimpleCacheUrlKey} when the URL doesn't change for better
 * performances and memory consumption (an {@link URI} object is created for
 * every instance of this class).
 * 
 * It implements {@link Parcelable}, so that it can be passed into Bundles.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class URICacheUrlKey implements CacheUrlKey, Parcelable {

	/**
	 * Minimum allowed source path length for a {@link URICacheUrlKey}
	 */
	protected static final int MIN_KEY_SOURCE = 5;

	/**
	 * Currently used hash function (murmur3 128 bit algorithm)
	 */
	protected static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();

	@NonNull
	private final String mKey;
	@NonNull
	private final URI mUri;

	/**
	 * Generate a unique key from the passed URL, ignoring any query parameters
	 * or URL fragments.
	 * 
	 * @param url
	 *            The URL of the resource to be cached
	 * @throws IllegalArgumentException
	 *             if the passed URL is not a valid {@link URI} or is not
	 *             suitable for being converted to a cache key
	 */
	public URICacheUrlKey(@NonNull String url) throws IllegalArgumentException {
		mUri = URI.create(url);
		// process URI
		mKey = hashUri();
	}

	/**
	 * Override this constructor to provide a custom implementation of the
	 * hashed cache key.
	 * 
	 * @param url
	 *            The URL of the resource to be cached
	 * @param key
	 *            The generated cache key
	 * @throws IllegalArgumentException
	 *             if the passed URL is not a valid {@link URI} or is not
	 *             suitable for being converted to a cache key
	 */
	protected URICacheUrlKey(@NonNull String url, @NonNull String key)
			throws IllegalArgumentException {
		mUri = URI.create(url);
		mKey = key;
	}

	/*
	 * Needed for the Parcelable functionalities
	 */

	public URICacheUrlKey(Parcel source) throws IllegalArgumentException {
		// Reconstruct from the Parcel
		this(source.readString());
	}

	public static final Parcelable.Creator<URICacheUrlKey> CREATOR = new Parcelable.Creator<URICacheUrlKey>() {
		@Override
		public URICacheUrlKey createFromParcel(Parcel source) {
			return new URICacheUrlKey(source);
		}

		@Override
		public URICacheUrlKey[] newArray(int size) {
			return new URICacheUrlKey[size];
		}
	};

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUri.toString());
	}

	/**
	 * Gets the generated key for this object
	 */
	@Override
	public String hash() {
		return mKey;
	}

	/**
	 * Gets the full URL for GET requests
	 */
	@Override
	public String getUrl() {
		return mUri.toString();
	}

	/**
	 * Generate an unique identifier for the cache key from the path part of the
	 * given URI.
	 */
	private String hashUri() throws IllegalArgumentException {
		String path = mUri.getPath();
		if (path == null || path.length() < MIN_KEY_SOURCE) {
			throw new IllegalArgumentException("Unsuitable URI");
		}
		return HashUtils.getHash(HASH_FUNCTION, path);
	}

}