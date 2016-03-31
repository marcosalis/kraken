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
package com.github.marcosalis.kraken.cache;

import com.google.common.annotations.Beta;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable decorator class to hold a memory cache value which has an expiration time.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@Immutable
public class ExpirableCacheItem<E> {

    /**
     * Content field, direct access to slightly improve performances.
     */
    public final E content;
    private final long mExpiration;

    /**
     * Instantiate an {@link ExpirableCacheItem}
     *
     * @param content    The content value to cache
     * @param expiration The expiration time, in milliseconds
     */
    public ExpirableCacheItem(E content, long expiration) {
        this.content = content;
        mExpiration = System.currentTimeMillis() + expiration;
    }

    /**
     * Returns whether the held cache item is expired or not.
     *
     * @return true if the item is expired, false otherwise
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > mExpiration;
    }

}
