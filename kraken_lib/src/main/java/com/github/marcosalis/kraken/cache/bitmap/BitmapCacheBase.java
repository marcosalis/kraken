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
package com.github.marcosalis.kraken.cache.bitmap;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.ContentCache.OnEntryRemovedListener;
import com.github.marcosalis.kraken.cache.bitmap.internal.BitmapLoader;
import com.github.marcosalis.kraken.cache.bitmap.threading.BitmapThreadingPolicy;
import com.github.marcosalis.kraken.cache.bitmap.threading.DefaultBitmapThreadingPolicy;
import com.github.marcosalis.kraken.cache.proxies.ContentProxyBase;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.github.marcosalis.kraken.utils.concurrent.ReorderingThreadPoolExecutor;
import com.google.common.annotations.Beta;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.concurrent.Immutable;

/**
 * <p> Abstract base class for a {@link Bitmap} content cache. Every {@link BitmapCacheBase}
 * subclass can hold one or more (in order to be able to fine-tune the size of each of them) memory
 * caches and a disk cache, which are managed separately.
 *
 * <p> Every {@link BitmapCacheBase} shares the same executors, one for querying the cache for a
 * Bitmap (memory and disk I/O can be required) and another for executing image download requests
 * without preventing the other cache requests to block. The default executors maximum thread pool
 * size varies depending on the number of CPU cores in the device. The {@link BitmapThreadingPolicy}
 * shared among all bitmap caches can be overridden by calling {@link
 * #setThreadingPolicy(BitmapThreadingPolicy)}. This class also contains other static methods to
 * manage the execution tasks and the executors lifecycle.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@Immutable
public abstract class BitmapCacheBase extends ContentProxyBase implements BitmapCache,
        OnEntryRemovedListener<String, Bitmap> {

    private static final String TAG = BitmapCacheBase.class.getSimpleName();

    private static volatile BitmapThreadingPolicy mThreadingPolicy = new DefaultBitmapThreadingPolicy();

    /**
     * Sets a custom common static {@link BitmapThreadingPolicy} for the bitmap caches, overriding
     * the {@link DefaultBitmapThreadingPolicy} default implementation, which is recommended for
     * most common uses.
     *
     * Call this before initializing any bitmap cache, preferably in the {@link
     * Application#onCreate()} method.
     *
     * @param policy The bitmap threading policy to set
     */
    public static final synchronized void setThreadingPolicy(@NonNull BitmapThreadingPolicy policy) {
        mThreadingPolicy = policy;
    }

    /**
     * Executes a callable task in the bitmap disk executor thread pool.
     *
     * @param callable The {@link Callable} to execute
     */
    @NonNull
    public static synchronized final Future<Bitmap> submitInExecutor(
            @NonNull Callable<Bitmap> callable) {
        return mThreadingPolicy.getBitmapDiskExecutor().submit(callable);
    }

    /**
     * Executes a runnable task in the bitmap downloader thread pool.
     *
     * @param runnable The {@link Runnable} to execute
     */
    public static synchronized final void executeInDownloader(@NonNull Runnable runnable) {
        mThreadingPolicy.getBitmapDownloader().execute(runnable);
    }

    /**
     * Executes a callable task in the bitmap downloader thread pool.
     *
     * @param callable The {@link Callable} to execute
     */
    @NonNull
    public static synchronized final Future<Bitmap> submitInDownloader(
            @NonNull Callable<Bitmap> callable) {
        return mThreadingPolicy.getBitmapDownloader().submit(callable);
    }

    /**
     * Executes a callable task in the bitmap downloader thread pool.
     *
     * @param key      The key associated to the submitted task
     * @param callable The {@link Callable} to execute
     */
    @SuppressWarnings("unchecked")
    public static synchronized final Future<Bitmap> submitInDownloader(@NonNull String key,
                                                                       @NonNull Callable<Bitmap> callable) {
        final ThreadPoolExecutor executor = mThreadingPolicy.getBitmapDownloader();
        if (executor instanceof ReorderingThreadPoolExecutor) {
            return ((ReorderingThreadPoolExecutor<String>) executor).submitWithKey(key, callable);
        } else {
            if (DroidConfig.DEBUG) {
                Log.v(TAG, "Not using instance of ReorderingThreadPoolExecutor for downloader");
            }
            return executor.submit(callable);
        }
    }

    /**
     * Attempts to prioritize a bitmap download by moving to the top of the executor queue the task
     * with the passed key, if it exists.
     *
     * @param key The string key corresponding to the bitmap
     */
    @NotForUIThread
    @SuppressWarnings("unchecked")
    public static synchronized final void moveDownloadToFront(@NonNull String key) {
        final ThreadPoolExecutor executor = mThreadingPolicy.getBitmapDownloader();
        if (executor instanceof ReorderingThreadPoolExecutor) {
            ((ReorderingThreadPoolExecutor<String>) executor).moveToFront(key);
        } else {
            if (DroidConfig.DEBUG) {
                Log.v(TAG, "Not using instance of ReorderingThreadPoolExecutor for downloader");
            }
        }
    }

    /**
     * Remove all not-running tasks from all static bitmap executors.
     */
    @NotForUIThread
    public static synchronized final void clearBitmapExecutors() {
        final ThreadPoolExecutor diskExecutor = mThreadingPolicy.getBitmapDiskExecutor();
        final ThreadPoolExecutor bitmapDownloader = mThreadingPolicy.getBitmapDownloader();
        diskExecutor.getQueue().clear();
        bitmapDownloader.getQueue().clear();
        if (bitmapDownloader instanceof ReorderingThreadPoolExecutor) {
            ((ReorderingThreadPoolExecutor<?>) bitmapDownloader).clearKeysMap();
        }

        if (DroidConfig.DEBUG) {
            BitmapLoader.clearStatsLog();
            Log.d(TAG, "Bitmap executors tasks cleared");
            Log.v(TAG, "Bitmap executor tasks: " + diskExecutor.getTaskCount() + ", completed: "
                    + diskExecutor.getCompletedTaskCount());
            Log.v(TAG, "Bitmap downloader executor tasks: " + bitmapDownloader.getTaskCount()
                    + ", completed: " + bitmapDownloader.getCompletedTaskCount());
        }
    }

    /**
     * {@link Memoizer} used for loading Bitmaps from the cache.
     */
    private final Memoizer<String, Bitmap> mBitmapMemoizer;

    protected BitmapCacheBase() {
        final int concurrencyLevel = mThreadingPolicy.getBitmapDownloader().getCorePoolSize();
        mBitmapMemoizer = new Memoizer<String, Bitmap>(concurrencyLevel);
    }

    @Override
    public void onEntryRemoved(boolean evicted, String key, Bitmap value) {
        // remove evicted bitmaps from the downloads memoizer to allow GC
        mBitmapMemoizer.remove(key);
    }

    @NonNull
    protected final Memoizer<String, Bitmap> getMemoizer() {
        return mBitmapMemoizer;
    }

    @Override
    @CallSuper
    public void clearMemoryCache() {
        mBitmapMemoizer.clear();
    }

}