/*
 * Copyright 2013 Luluvise Ltd
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
package com.github.marcosalis.kraken.utils.concurrent;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import android.annotation.TargetApi;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.annotations.NotForUIThread;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.google.common.annotations.Beta;

/**
 * Thread pool based on {@link ThreadPoolExecutor} to manage the runnables queue
 * in order to be able to reorder the tasks by moving them to its front when
 * needed and prioritize their execution.
 * 
 * This is useful, for example, when loading many bitmaps from the network in a
 * long list, and we want to be able to re-arrange the downloads priorities when
 * the user scrolls down.
 * 
 * The executor is backed by a data structure that maps keys to the executor's
 * queue runnables in order to be able to retrieve them and move them along the
 * queue itself. It is recommended to use a lightweight object as a key type,
 * such as String.
 * 
 * {@link #submitWithKey(String, Callable)} and {@link #moveToFront(String)} are
 * effective only from API >= 9 as they use {@link LinkedBlockingDeque}. With
 * API 8 the behavior is exactly the same as a normal executor and
 * {@link #moveToFront(String)} is a no-op.
 * 
 * Note that this executor is specifically developed to be used when task
 * reordering is crucial for performances. In all other cases, expecially in
 * case of very long task queues, {@link ReorderingThreadPoolExecutor} can be
 * significantly slower than its superclass. Furthermore, always remember to
 * explicitly call {@link #purge()} from time to time when cancelling
 * {@link Future}s for tasks that have been submitted to this executor in order
 * to not pollute the internal map with cancelled tasks.
 * 
 * @since 1.0
 * @author Marco Salis
 * 
 * @param <K>
 *            The key type for matching tasks
 */
@Beta
public class ReorderingThreadPoolExecutor<K> extends ThreadPoolExecutor {

	private static final String TAG = ReorderingThreadPoolExecutor.class.getSimpleName();

	private final BlockingQueue<Runnable> mQueueRef;
	@GuardedBy("mMapLock")
	private final ConcurrentHashMap<K, KeyHoldingFutureTask<K, ?>> mRunnablesMap;
	private final ReentrantReadWriteLock mMapLock;

	/**
	 * Creates a {@link ReorderingThreadPoolExecutor}.
	 * 
	 * The passed {@link BlockingQueue} should be a {@link LinkedBlockingDeque}.
	 * Use the static method {@link #createBlockingQueue()} to retrieve a
	 * compatible queue depending on the API level.
	 * 
	 * @see {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue, ThreadFactory)}
	 */
	// superclass constructor
	public ReorderingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		mQueueRef = workQueue;
		mRunnablesMap = new ConcurrentHashMap<K, KeyHoldingFutureTask<K, ?>>(maximumPoolSize,
				0.75f, corePoolSize);
		mMapLock = new ReentrantReadWriteLock();
	}

	@Nonnull
	@TargetApi(9)
	@NotForUIThread
	public <T> Future<T> submitWithKey(@Nonnull K key, @Nonnull Callable<T> callable) {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			// mimics newTaskFor() behavior to provide a custom RunnableFuture
			final KeyHoldingFutureTask<K, T> runnable = new KeyHoldingFutureTask<K, T>(key,
					callable);
			mMapLock.readLock().lock(); // read lock
			try {
				mRunnablesMap.put(key, runnable); // O(1)
			} finally {
				mMapLock.readLock().unlock();
			}
			execute(runnable);
			return runnable;
		} else {
			return submit(callable);
		}
	}

	@TargetApi(9)
	@NotForUIThread
	public void moveToFront(@Nonnull K key) {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			final Runnable runnable;
			mMapLock.readLock().lock(); // read lock
			try {
				runnable = mRunnablesMap.get(key); // O(1)
			} finally {
				mMapLock.readLock().unlock();
			}
			if (runnable != null) {
				if (mQueueRef instanceof LinkedBlockingDeque) {
					final LinkedBlockingDeque<Runnable> blockingDeque = (LinkedBlockingDeque<Runnable>) mQueueRef;
					/*
					 * the Runnable is removed from the executor queue so it's
					 * safe to add it back: we don't risk double running it.
					 * removeLastOccurrence() has linear complexity, however we
					 * assume that the advantages of bringing the runnable on
					 * top of the queue overtake this drawback in a reasonably
					 * small queue.
					 */
					if (blockingDeque.removeLastOccurrence(runnable)) { // O(n)
						blockingDeque.offerFirst(runnable); // O(1)
						if (DroidConfig.DEBUG) {
							Log.v(TAG, "Bringing bitmap task to front for: " + key);
						}
					}
				}
			}
		}
	}

	@NotForUIThread
	@OverridingMethodsMustInvokeSuper
	public void clearKeysMap() {
		if (DroidConfig.DEBUG) {
			Log.d(TAG, "Clearing runnables key map, contains " + mRunnablesMap.size() + " keys");
		}
		mMapLock.writeLock().lock();
		try {
			// we add a write lock here as we don't want other threads to add
			// tasks here while the map is cleared
			mRunnablesMap.clear();
		} finally {
			mMapLock.writeLock().unlock();
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (r instanceof KeyHoldingFutureTask) {
			mMapLock.readLock().lock(); // read lock
			try {
				mRunnablesMap.remove(((KeyHoldingFutureTask<?, ?>) r).key, r);
			} finally {
				mMapLock.readLock().unlock();
			}
		}
	}

	@Override
	@NotForUIThread
	@OverridingMethodsMustInvokeSuper
	public void purge() {
		mMapLock.writeLock().lock(); // write lock
		try {
			// remove cancelled runnables from the keys map
			final Collection<KeyHoldingFutureTask<K, ?>> runnables = mRunnablesMap.values();
			for (KeyHoldingFutureTask<K, ?> r : runnables) {
				if (r.isCancelled()) {
					runnables.remove(r); // supposedly O(1)
				}
			}
		} finally {
			mMapLock.writeLock().unlock();
		}
		super.purge();
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	protected void terminated() {
		clearKeysMap(); // clear keys map when terminated
		super.terminated();
	}

	/**
	 * Factory method that creates a {@link LinkedBlockingDeque}, if the API
	 * level is >= 9, or falls back to a {@link LinkedBlockingQueue}.
	 */
	@Nonnull
	public static BlockingQueue<Runnable> createBlockingQueue() {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			return getBlockingDeque();
		} else {
			return new LinkedBlockingQueue<Runnable>();
		}
	}

	@TargetApi(9)
	private static LinkedBlockingDeque<Runnable> getBlockingDeque() {
		return new LinkedBlockingDeque<Runnable>();
	}

	/**
	 * Extension of {@link FutureTask} which just allows setting a key
	 */
	@ThreadSafe
	private static class KeyHoldingFutureTask<K, V> extends FutureTask<V> {

		public final K key;

		public KeyHoldingFutureTask(@Nonnull K key, @Nonnull Callable<V> callable) {
			super(callable);
			this.key = key;
		}
	}

}