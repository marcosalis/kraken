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
package com.github.marcosalis.kraken.cache.proxies;

import android.os.Process;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.concurrent.PriorityThreadFactory;
import com.google.common.annotations.Beta;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

/**
 * <p> Abstract base implementation of {@link ContentProxy}. <p> Just provides some library static
 * utility methods to execute content-retrieval related tasks.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@ThreadSafe
public abstract class ContentProxyBase implements ContentProxy {

    private static final String TAG = ContentProxyBase.class.getSimpleName();

    static {
        final int executorSize = DroidUtils.getIOBoundPoolSize();
        final int prefetchSize = (int) Math.ceil((double) executorSize / 2);

        // prepare I/O bound, default priority proxy thread pool
        final LinkedBlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
        final PriorityThreadFactory executorFactory = new PriorityThreadFactory("Proxy executor");
        final ThreadPoolExecutor proxyExecutor = new ThreadPoolExecutor(executorSize, executorSize,
                0L, TimeUnit.MILLISECONDS, executorQueue, executorFactory);
        PROXY_EXECUTOR = proxyExecutor;

        // prepare low priority pre-fetch thread pool
        final LinkedBlockingQueue<Runnable> prefetchQueue = new LinkedBlockingQueue<Runnable>();
        final PriorityThreadFactory prefetchFactory = new PriorityThreadFactory("Proxy pre-fetch",
                Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE);
        final ThreadPoolExecutor prefetchExecutor = new ThreadPoolExecutor(prefetchSize,
                prefetchSize, 0L, TimeUnit.MILLISECONDS, prefetchQueue, prefetchFactory);
        PRE_FETCH_EXECUTOR = prefetchExecutor;

        // prepare lowest priority, single threaded pool
        final LinkedBlockingQueue<Runnable> lowPriorityQueue = new LinkedBlockingQueue<Runnable>();
        final PriorityThreadFactory lowPriorityFactory = new PriorityThreadFactory(
                "Low priority executor", Process.THREAD_PRIORITY_LOWEST);
        final ThreadPoolExecutor lowPriorityExecutor = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, lowPriorityQueue, lowPriorityFactory);
        LOW_PRIORITY_EXECUTOR = lowPriorityExecutor;
    }

    /**
     * Executor to be used by all content proxies to make requests.
     */
    private static final ThreadPoolExecutor PROXY_EXECUTOR;

    /**
     * Low priority executor to be used by subclasses when pre-fetching content.
     */
    private static final ThreadPoolExecutor PRE_FETCH_EXECUTOR;

    /**
     * Low priority, single thread executor to be used for network requests or other tasks that
     * don't immediately affect the user experience and therefore don't necessarily need to be
     * completed in a short time.
     */
    private static final ThreadPoolExecutor LOW_PRIORITY_EXECUTOR;

    /**
     * Executes a task in the main, standard priority common thread pool.
     *
     * @param runnable The {@link Runnable} to execute (must be non null)
     */
    protected static synchronized final void execute(@NonNull Runnable runnable) {
        PROXY_EXECUTOR.execute(runnable);
    }

    /**
     * Executes a non time-critical {@link Runnable} task using the common low priority thread
     * pool.
     *
     * @param runnable The {@link Runnable} to execute (must be non null)
     */
    protected static synchronized final void executeLowPriority(@NonNull Runnable runnable) {
        LOW_PRIORITY_EXECUTOR.execute(runnable);
    }

    /**
     * Executes a {@link Runnable} to pre-fetch generic data inside the content proxies common
     * pre-fetch thread pool.
     *
     * @param runnable The {@link Runnable} to execute (must be non null)
     */
    public static synchronized final void prefetch(@NonNull Runnable runnable) {
        PRE_FETCH_EXECUTOR.execute(runnable);
    }

    /**
     * Remove all not-running tasks from all static executors. Note that this method doesn't cancel
     * tasks that are already in execution nor terminates the executors.
     */
    public static synchronized final void clearExecutors() {
        PROXY_EXECUTOR.getQueue().clear();
        PRE_FETCH_EXECUTOR.getQueue().clear();
        LOW_PRIORITY_EXECUTOR.getQueue().clear();

        if (DroidConfig.DEBUG) { // logging some stats
            Log.d(TAG, "Executors tasks cleared");
            Log.v(TAG, "Proxy executor tasks: " + PROXY_EXECUTOR.getTaskCount() + ", completed: "
                    + PROXY_EXECUTOR.getCompletedTaskCount());
            Log.v(TAG, "Low-priority executor tasks: " + LOW_PRIORITY_EXECUTOR.getTaskCount()
                    + ", completed: " + LOW_PRIORITY_EXECUTOR.getCompletedTaskCount());
            Log.v(TAG, "Pre-fetch executor tasks: " + PRE_FETCH_EXECUTOR.getTaskCount()
                    + ", completed: " + PRE_FETCH_EXECUTOR.getCompletedTaskCount());
        }
    }

    @Override
    @CallSuper
    public void clearCache() {
        clearMemoryCache();
        clearDiskCache(ClearMode.ALL);
    }

}