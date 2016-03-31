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
package com.github.marcosalis.kraken.cache.bitmap.internal;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.ContentCache.CacheSource;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache.OnBitmapRetrievalListener;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCacheBase;
import com.github.marcosalis.kraken.cache.bitmap.BitmapDecoder;
import com.github.marcosalis.kraken.cache.bitmap.disk.BitmapDiskCache;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapLruCache;
import com.github.marcosalis.kraken.cache.bitmap.memory.BitmapMemoryCache;
import com.github.marcosalis.kraken.cache.keys.CacheUrlKey;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.github.marcosalis.kraken.utils.http.ByteArrayDownloader;
import com.google.api.client.http.HttpRequestFactory;
import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * General loader for a {@link Bitmap} from a {@link BitmapCacheBase}.<br> If the mode set for the
 * request is {@link AccessPolicy#PRE_FETCH}, the retrieved image is only downloaded and put in the
 * memory cache if necessary.
 *
 * FIXME: some tests showed that, very rarely, a race condition occurs for which two threads can get
 * to download the same bitmap twice: investigate this
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@NotThreadSafe
public class BitmapLoader implements Callable<Bitmap> {

    /**
     * Configuration class that holds all the external components that a {@link BitmapLoader} needs
     * to access.
     */
    @Immutable
    static class Config {
        public final Memoizer<String, Bitmap> downloadsCache;
        public final BitmapMemoryCache<String> memoryCache;
        public final BitmapDiskCache diskCache;
        public final HttpRequestFactory requestFactory;
        public final BitmapDecoder bitmapDecoder;

        /**
         * Creates a {@link BitmapLoader} immutable configuration.
         *
         * @param downloads The {@link CacheMemoizer} used to retrieve cached items
         * @param cache     The {@link BitmapLruCache} where bitmaps in memory are stored
         * @param diskCache The (optional) {@link BitmapDiskCache} where bitmaps saved on disk are
         *                  handled
         * @param factory   The {@link HttpRequestFactory} to download the bitmap
         * @param decoder   The {@link BitmapDecoder} to use for decoding
         */
        public Config(@NonNull Memoizer<String, Bitmap> downloadsCache,
                      @NonNull BitmapMemoryCache<String> memoryCache,
                      @Nullable BitmapDiskCache diskCache, @NonNull HttpRequestFactory requestFactory,
                      @NonNull BitmapDecoder decoder) {
            this.downloadsCache = downloadsCache;
            this.memoryCache = memoryCache;
            this.diskCache = diskCache;
            this.requestFactory = requestFactory;
            this.bitmapDecoder = decoder;
        }
    }

    static {
        final int concurrency = DroidUtils.getCpuBoundPoolSize();
        final ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<String, Boolean>(128,
                0.75f, concurrency);
        // addAll() is NOT thread-safe in this set
        loaderDownloadedItems = Sets.newSetFromMap(map);
    }

    private static final String TAG = BitmapLoader.class.getSimpleName();

    static final Set<String> loaderDownloadedItems;
    // for logging purposes only
    static final AtomicLong downloaderTimer = new AtomicLong();
    static final AtomicInteger downloaderCounter = new AtomicInteger();
    static final AtomicInteger failuresCounter = new AtomicInteger();
    // for logging purposes only

    private final BitmapLoader.Config mLoaderConfig;
    private final CacheUrlKey mKey;
    private final AccessPolicy mPolicy;
    private OnBitmapRetrievalListener mBitmapCallback;

    /**
     * Instantiates a {@link BitmapLoader}.
     *
     * @param config   The {@link BitmapLoader#Config} for this loader
     * @param key      The {@link CacheUrlKey} to retrieve the bitmap
     * @param policy   The {@link AccessPolicy} to load the bitmap
     * @param callback {@link OnBitmapRetrievalListener} for the image (can be null)
     */
    BitmapLoader(@NonNull BitmapLoader.Config config, @NonNull CacheUrlKey key,
                 @NonNull AccessPolicy policy, @Nullable OnBitmapRetrievalListener callback) {
        mLoaderConfig = config;
        mKey = key;
        mPolicy = policy;
        mBitmapCallback = callback;
    }

    @Override
    @Nullable
    public Bitmap call() {
        // TODO: refactor this
        final BitmapMemoryCache<String> memoryCache = mLoaderConfig.memoryCache;
        final BitmapDiskCache diskCache = mLoaderConfig.diskCache;
        final String key = mKey.hash();

        Bitmap bitmap = null;
        CacheSource source = null;

        try {
            if (mPolicy != AccessPolicy.REFRESH) {
                // 1- check memory cache again
                if ((bitmap = memoryCache.get(key)) != null) {
                    // memory cache hit
                    source = CacheSource.MEMORY;
                    return bitmap;
                }

                // 2- check disk cache
                if (diskCache != null) {
                    if ((bitmap = diskCache.get(key)) != null) {
                        // disk cache hit, put it into memory cache
                        source = CacheSource.DISK;
                        // use put(key, bitmap) for debugging
                        memoryCache.putIfAbsent(key, bitmap);
                        return bitmap;
                    }
                }
            }
            /*
			 * 3- Memory and disk cache miss, execute GET request to retrieve
			 * image.
			 * 
			 * We delegate the task to another, separated executor to download
			 * images to avoid blocking delivery of cached images to the UI
			 */
            if (mPolicy != AccessPolicy.CACHE_ONLY) {
                executeDownload(mLoaderConfig, mKey, mPolicy, mBitmapCallback);
            }
			/*
			 * FIXME: returning null here means that bitmaps coming from the
			 * network don't get returned in the Future representing this task
			 * completion.
			 */
            return null;
        } finally {
            if (mBitmapCallback != null) {
                // notify success or failure to callback
                if (bitmap != null) {
                    mBitmapCallback.onBitmapRetrieved(mKey, bitmap, source);
                } else if (mPolicy == AccessPolicy.CACHE_ONLY) {
                    mBitmapCallback.onBitmapRetrievalFailed(mKey, null);
                }
                mBitmapCallback = null; // avoid leaks
            }
        }
    }

    @NonNull
    static Future<Bitmap> executeDownload(@NonNull BitmapLoader.Config config,
                                          @NonNull CacheUrlKey key, @NonNull AccessPolicy policy,
                                          @Nullable OnBitmapRetrievalListener callback) {
        final MemoizerCallable memoizer = new MemoizerCallable(config, key, policy, callback);
        final String hash = key.hash();
        // attempt prioritizing the download task if already in queue
        BitmapCacheBase.moveDownloadToFront(hash);
        // submit new memoizer task to downloder executor
        return BitmapCacheBase.submitInDownloader(hash, memoizer);
    }

    /**
     * Memoizer-related task. It uses the {@link CacheMemoizer} item to check whether there is
     * another running and unfinished task for the Bitmap we want to download from the server.
     *
     * This computation gets executed through the {@link BitmapCacheBase#submitInDownloader(String,
     * Callable)} method.
     */
    @NotThreadSafe
    private static class MemoizerCallable implements Callable<Bitmap> {

        private final BitmapLoader.Config mLoaderConfig;
        private final CacheUrlKey mKey;
        private final AccessPolicy mPolicy;
        private OnBitmapRetrievalListener mBitmapCallback;

        public MemoizerCallable(@NonNull BitmapLoader.Config config, @NonNull CacheUrlKey key,
                                @NonNull AccessPolicy policy, @Nullable OnBitmapRetrievalListener callback) {
            mLoaderConfig = config;
            mKey = key;
            mPolicy = policy;
            mBitmapCallback = callback;
        }

        @Override
        @Nullable
        public Bitmap call() throws InterruptedException {
            final BitmapMemoryCache<String> memoryCache = mLoaderConfig.memoryCache;
            final BitmapDiskCache diskCache = mLoaderConfig.diskCache;
            final String key = mKey.hash();

            Bitmap bitmap = null;
            CacheSource source = null;
            Exception exception = null;

            try {
                if (loaderDownloadedItems.contains(key) && mPolicy != AccessPolicy.REFRESH) {
					/*
					 * Bitmap already downloaded, we need to check the disk
					 * cache again. This is a race condition due to the fact
					 * that we don't block on the FutureTask that checks the
					 * memory/disk cache at first. This caveat only occurs in
					 * the unlikely circumnstances that a bitmap is requested
					 * many times before the memoizer has been called.
					 */
                    if (diskCache != null) {
                        if ((bitmap = diskCache.get(key)) != null) {
                            if (DroidConfig.DEBUG) {
                                Log.w(TAG, "MemoizerCallable: restored bitmap from disk! - " + key);
                            }
                            // disk cache hit, put it into memory cache
                            memoryCache.putIfAbsent(key, bitmap);
                            // and call back to the listener
                            source = CacheSource.DISK;
                            return bitmap;
                        }
                    }
                }

                final DownloaderCallable downloader = new DownloaderCallable(mLoaderConfig, mKey);
                bitmap = mLoaderConfig.downloadsCache.execute(key, downloader);
                source = CacheSource.NETWORK;
                return bitmap;
            } catch (InterruptedException e) {
                exception = e;
                throw e;
            } catch (Exception e) {
                exception = e;
                LogUtils.logException(e);
                return null; // something unexpected happened, can do nothing
            } finally {
                if (mBitmapCallback != null) {
                    // notify success or failure to callback
                    if (bitmap != null) {
                        mBitmapCallback.onBitmapRetrieved(mKey, bitmap, source);
                    } else {
                        mBitmapCallback.onBitmapRetrievalFailed(mKey, exception);
                    }
                    mBitmapCallback = null; // avoid leaks
                }
            }
        }
    }

    /**
     * Bitmap cache task that effectively downloads a Bitmap from the network when needed. When
     * debugging is active, it also handles some instrumentation and logs some statistics about the
     * download.
     */
    @Immutable
    private static class DownloaderCallable implements Callable<Bitmap> {

        private final BitmapLoader.Config mLoaderConfig;
        private final CacheUrlKey mKey;

        public DownloaderCallable(@NonNull BitmapLoader.Config config, @NonNull CacheUrlKey key) {
            mLoaderConfig = config;
            mKey = key;
        }

        @Override
        @Nullable
        public Bitmap call() throws IOException {
            final BitmapMemoryCache<String> memoryCache = mLoaderConfig.memoryCache;
            final BitmapDiskCache diskCache = mLoaderConfig.diskCache;
            final String key = mKey.hash();
            final String url = mKey.getUrl();
            Bitmap bitmap = null;

            long startDownload = 0;
            if (DroidConfig.DEBUG) {
                startDownload = System.currentTimeMillis();
            }

            final HttpRequestFactory factory = mLoaderConfig.requestFactory;
            final byte[] imageBytes = ByteArrayDownloader.downloadByteArray(factory, url);

            long endDownload = 0;
            if (DroidConfig.DEBUG) {
                endDownload = System.currentTimeMillis();
            }
            if (imageBytes != null) { // download successful
                // TODO: pass bitmap options here
                bitmap = mLoaderConfig.bitmapDecoder.decode(imageBytes, null);

                if (bitmap != null) { // decoding successful

                    if (!loaderDownloadedItems.add(key)) {
                        // bitmap was already downloaded, maybe refresh action?
                        LogUtils.log(Log.WARN, TAG, "Downloading " + key + " bitmap twice: " + url);
                    }

                    if (DroidConfig.DEBUG) { // debugging
                        final long endDecoding = System.currentTimeMillis();
                        // logging download statistics
                        final long downloadTime = endDownload - startDownload;
                        downloaderTimer.addAndGet(downloadTime);
                        downloaderCounter.incrementAndGet();
                        Log.d(TAG, key + " download took ms " + downloadTime);
                        Log.v(TAG, key + " decoding took ms " + (endDecoding - endDownload));
                    } // end debugging

                    // save downloaded bitmap in caches
                    memoryCache.put(key, bitmap);

					/*
					 * Note: the bitmap retrieval callback is not executed
					 * before saving to disk cache anymore. Verify if this can
					 * have significant performance impacts.
					 */
                    saveIntoDiskCache(diskCache, key, imageBytes);
                }
            } else { // download failed
                if (DroidConfig.DEBUG) {
                    failuresCounter.incrementAndGet();
                }
            }
            return bitmap;
        }
    }

    // for debugging purposes only
    public static void clearStatsLog() {
        final AtomicLong timer = downloaderTimer;
        final AtomicInteger counter = downloaderCounter;
        final AtomicInteger failures = failuresCounter;
        final int counterInt = counter.get();
        final int failuresInt = failures.get();
        final long averageMs = (counterInt != 0) ? timer.get() / counterInt : 0;
        final int failuresPerc = (counterInt != 0) ? (int) (100f / counterInt) * failuresInt : 0;
        Log.i(TAG, counterInt + " bitmaps downloaded in average ms " + averageMs + " - "
                + failuresPerc + "% failures");
        // reset stats
        loaderDownloadedItems.clear();
        timer.set(0);
        counter.set(0);
        failures.set(0);
    }

    private static void saveIntoDiskCache(@Nullable BitmapDiskCache diskCache, @NonNull String key,
                                          @NonNull byte[] data) {

        if (diskCache != null) {
            long startSave = 0;
            if (DroidConfig.DEBUG) { // profiling
                startSave = System.currentTimeMillis();
            }

            diskCache.put(key, data);

            if (DroidConfig.DEBUG) { // profiling
                final long endSave = System.currentTimeMillis();
                Log.v(TAG, key + " disk save took ms " + (endSave - startSave));
            }
        }
    }

}