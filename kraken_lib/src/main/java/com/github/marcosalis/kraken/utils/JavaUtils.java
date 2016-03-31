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
import android.support.annotation.Nullable;

import com.google.common.annotations.Beta;

import java.util.List;
import java.util.Random;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Helper class containing general static utility methods of any Java-related kind.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@ThreadSafe
public class JavaUtils {

    /**
     * Single application global {@link Random} generator that can be used for randomizations (do
     * not use for security-sensitive components)
     */
    public static final Random RANDOM = new Random();

    private JavaUtils() {
        // hidden constructor, no instantiation needed
    }

    /**
     * Check if a String starts with a specified prefix (case insensitive).
     *
     * @param str    the String to check, may be null
     * @param prefix the prefix to find, may be null
     * @return <code>true</code> if the String starts with the prefix or both <code>null</code>
     * @see java.lang.String#startsWith(String)
     */
    public static boolean startsWithIgnoreCase(@Nullable String str, @Nullable String prefix) {
        if (str == null || prefix == null) {
            return (str == null && prefix == null);
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Return an object from the passed array using a random generator for the index to get it
     * from.
     *
     * @param array The array to retrieve the item from
     * @return The item at the pseudo-randomly generated index, or null if the array is empty
     */
    @Nullable
    public static final <E> E getRandom(@NonNull E[] array) {
        if (array.length == 0) { // avoids out of bounds exceptions
            return null;
        }
        final int randomIndex = RANDOM.nextInt(array.length);
        return array[randomIndex];
    }

    /**
     * Return an object from the passed {@link List} using a random generator for the index to get
     * it from.
     *
     * @param list The list to retrieve the item from
     * @return The item at the pseudo-randomly generated index, or null if the list is empty
     */
    @Nullable
    public static final <E> E getRandom(@NonNull List<E> list) {
        if (list.size() == 0) {
            return null;
        }
        final int randomIndex = RANDOM.nextInt(list.size());
        return list.get(randomIndex);
    }

}