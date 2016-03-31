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
package com.github.marcosalis.kraken.cache.bitmap.threading;

import android.support.annotation.NonNull;

import com.google.common.annotations.Beta;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Implementation of {@link BitmapThreadingPolicy} that allows setting custom threading policies for
 * bitmap caches.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@ThreadSafe
public final class CustomBitmapThreadingPolicy implements BitmapThreadingPolicy {

    private final ThreadPoolExecutor mBitmapDiskExecutor;
    private final ThreadPoolExecutor mDownloaderExecutor;

    /**
     * Set custom thread pool sizes and thread priorities for the default executors.
     *
     * The priority values must one of the thread priority specified in the {@link Process} class.
     *
     * @param executorThreadSize
     * @param executorThreadPriority
     * @param downloaderThreadSize
     * @param downloaderThreadPriority
     */
    public CustomBitmapThreadingPolicy(int executorThreadSize, int executorThreadPriority,
                                       int downloaderThreadSize, int downloaderThreadPriority) {
        mBitmapDiskExecutor = DefaultBitmapThreadingPolicy.buildDefaultDiskExecutor(
                executorThreadSize, executorThreadPriority);
        mDownloaderExecutor = DefaultBitmapThreadingPolicy.buildDefaultDownloader(
                downloaderThreadSize, downloaderThreadPriority);
    }

    /**
     * Set two custom executors for the bitmap caches.
     *
     * @param diskExecutor The custom bitmap disk {@link ThreadPoolExecutor}
     * @param downloader   The custom bitmap downloader {@link ThreadPoolExecutor}
     */
    public CustomBitmapThreadingPolicy(@NonNull ThreadPoolExecutor diskExecutor,
                                       @NonNull ThreadPoolExecutor downloader) {
        mBitmapDiskExecutor = diskExecutor;
        mDownloaderExecutor = downloader;
    }

    @NonNull
    @Override
    public ThreadPoolExecutor getBitmapDiskExecutor() {
        return mBitmapDiskExecutor;
    }

    @NonNull
    @Override
    public ThreadPoolExecutor getBitmapDownloader() {
        return mDownloaderExecutor;
    }

}