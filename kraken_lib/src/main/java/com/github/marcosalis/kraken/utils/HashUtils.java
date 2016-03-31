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
package com.github.marcosalis.kraken.utils;

import android.support.annotation.NonNull;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Helper class containing static utility methods for hashing objects.
 *
 * @author Marco Salis
 * @since 1.0
 */
public final class HashUtils {

    private static final HashFunction DEFAULT_HASH = Hashing.murmur3_128();

    private HashUtils() {
        // hidden constructor, no instantiation needed
    }

    /**
     * Gets a String hash generated using the default hashing algorithm with the passed strings as
     * input.
     */
    @NonNull
    public static String getDefaultHash(@NonNull String... strings) {
        return getHash(DEFAULT_HASH, strings);
    }

    /**
     * Gets a String hash generated using the MD5 hashing algorithm with the passed strings as
     * input.
     */
    @NonNull
    public static String getMD5Hash(@NonNull String... strings) {
        return getHash(Hashing.md5(), strings);
    }

    /**
     * Gets a String hash generated using the passed hashing algorithm.
     */
    @NonNull
    public static String getHash(@NonNull HashFunction hash, @NonNull String... strings) {
        final Hasher hasher = hash.newHasher();
        for (String input : strings) {
            hasher.putString(input);
        }
        return hasher.hash().toString();
    }

    /**
     * Interface that represent any suitable object that can be used as a String cache key. The key
     * itself retrieved by calling the hash() method.
     *
     * TODO: move this
     */
    public interface CacheKey {

        public String hash();
    }

}