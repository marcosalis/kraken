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
package com.github.marcosalis.kraken.cache.bitmap.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.BitmapDecoder;
import com.github.marcosalis.kraken.cache.bitmap.disk.BitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapMemoryCache;
import com.google.api.client.http.HttpRequestFactory;
import com.google.common.annotations.Beta;

/**
 * Internal utility class containing static methods to build the default implementations of {@link
 * BitmapCache}s and their components.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
public class BitmapCacheFactory {

    private BitmapCacheFactory() {
        // no instantiation needed
    }

    /**
     * Builds an instance of the default {@link BitmapCache} implementation given the passed
     * components.
     */
    @NonNull
    public static BitmapCache buildDefaultBitmapCache(@NonNull BitmapMemoryCache<String> cache,
                                                      @Nullable BitmapDiskCache diskCache, @NonNull HttpRequestFactory factory,
                                                      @NonNull BitmapDecoder decoder) {
        return new BitmapCacheImpl(cache, diskCache, factory, decoder);
    }

}