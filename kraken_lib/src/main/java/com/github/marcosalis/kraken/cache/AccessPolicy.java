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

/**
 * Enum that defines the supported access policies to a content proxy's cache levels.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
public enum AccessPolicy {

    /**
     * Normal content fetching: passes through all levels of cache, if there is a cache miss,
     * download the content from the server.
     */
    NORMAL,

    /**
     * Attempt a cache only content fetching.<br> If there is a total cache miss, do nothing.
     */
    CACHE_ONLY,

    /**
     * Pre-fetch data that could be needed soon.<br> If the data is not available in cache, schedule
     * a content download.
     */
    PRE_FETCH,

    /**
     * Refresh the cache data, and return the updated content if any.
     */
    REFRESH;

}