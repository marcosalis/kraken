/*
 * Copyright 2013 Luluvise Ltd
 * Copyright 2013 Marco Salis - fast3r@gmail.com
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
package com.github.marcosalis.kraken.cache.internal.loaders;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.ContentLruCache;
import com.github.marcosalis.kraken.cache.ModelDiskCache;
import com.github.marcosalis.kraken.cache.requests.CacheableRequest;
import com.github.marcosalis.kraken.utils.concurrent.ExpirableFutureTask;
import com.github.marcosalis.kraken.utils.concurrent.Memoizer;
import com.github.marcosalis.kraken.utils.network.ConnectionMonitor;
import com.google.common.annotations.Beta;

/**
 * 2-level cache (memory-disk) implementation of {@link ContentLoader} that uses
 * a slightly complex version the Memoizer pattern (see {@link CacheMemoizer})
 * that allows handling the different types of cache accesses specified by
 * {@link AccessPolicy}.
 * 
 * In order to allow this, the 100% reliability of the Memoizer in terms of
 * serial retrieval of a single item by multiple threads making concurrent
 * requests is sacrified in favor of a major flexibility.
 * 
 * In case the application {@link ConnectionMonitor} is registered and returns a
 * network connection fail, the set {@link AccessPolicy} is turned into
 * {@link AccessPolicy#CACHE_ONLY} to allow retrieving old data from the caches
 * (if any) by ignoring expiration.
 * 
 * All the actions on a cache, including {@link AccessPolicy#PRE_FETCH}, are
 * blocking for now. TODO: delegate pre-fetching to a separate executor and
 * return to the caller immediately when pre-fetching.<br>
 * 
 * @param <R>
 *            Requests type (extending {@link CacheableRequest})
 * @param <D>
 *            Content data type
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public final class DiskContentLoader<D> implements ContentLoader<CacheableRequest<D>, D> {

	private static final String TAG = DiskContentLoader.class.getSimpleName();

	private final ContentLruCache<String, ExpirableFutureTask<D>> mMemCache;
	@Nullable
	private final ModelDiskCache<D> mDiskCache;
	private final long mExpiration;
	@CheckForNull
	private final RequestHandler mRequestHandler;
	@Nullable
	private final ConnectionMonitor mConnMonitor;

	/**
	 * Instantiates a new {@link DiskContentLoader}.
	 * 
	 * @param memCache
	 *            The {@link ModelLruCache} to use
	 * @param diskCache
	 *            The (optional) {@link ModelDiskCache} to use
	 * @param expiration
	 *            Expiration offset time to use for {@link AccessPolicy}s that
	 *            require it
	 * @param requestHandler
	 *            The (optional) {@link RequestHandler} for this loader
	 * @param connMonitor
	 *            The (optional) {@link ConnectionMonitor} for this loader
	 */
	public DiskContentLoader(@Nonnull ContentLruCache<String, ExpirableFutureTask<D>> memCache,
			@CheckForNull ModelDiskCache<D> diskCache, long expiration,
			@Nullable RequestHandler requestHandler, @Nullable ConnectionMonitor connMonitor) {
		mMemCache = memCache;
		mDiskCache = diskCache;
		mExpiration = expiration;
		mRequestHandler = requestHandler;
		mConnMonitor = connMonitor;
	}

	/**
	 * @see ContentLoader#load(AccessPolicy, CacheableRequest,
	 *      ContentUpdateCallback)
	 */
	@Override
	public D load(@Nullable AccessPolicy action, @Nonnull CacheableRequest<D> request,
			@Nullable ContentUpdateCallback<D> callback) throws Exception {
		// TODO: improve this? It's almost procedural

		// if network is not active, turn every action to CACHE_ONLY
		boolean networkActive = (mConnMonitor != null) ? mConnMonitor.isNetworkActive() : true;
		action = (networkActive) ? ((action != null) ? action : AccessPolicy.NORMAL)
				: AccessPolicy.CACHE_ONLY;

		if (mRequestHandler != null) { // request handler validation
			mRequestHandler.validateRequest(request);
		}

		final String key = request.hash();
		// we try to retrieve item from our task cache
		ExpirableFutureTask<D> oldFutureTask = null;
		ExpirableFutureTask<D> future = oldFutureTask = mMemCache.get(key);
		/*
		 * we try to get a new task either if there is no previous one, if the
		 * action is REFRESH or the previous is expired and this is not a
		 * CACHE_ONLY action.
		 */
		final boolean isExpired = future != null && future.isExpired()
				&& action != AccessPolicy.CACHE_ONLY;

		if (future == null || action == AccessPolicy.REFRESH || isExpired) {

			/** cache debugging */
			if (DroidConfig.DEBUG) {
				final String url = request.getRequestUrl();
				if (future == null) {
					Log.v(TAG, "Memory cache miss for " + url);
				} else if (action == AccessPolicy.REFRESH) {
					Log.v(TAG, "Refreshing cache for " + url);
				} else if (isExpired) {
					Log.v(TAG, "Memory cache expired for " + url);
				}
			}
			/** cache debugging - END */

			// build the new loading task
			final ExpirableFutureTask<D> newFutureTask = new ExpirableFutureTask<D>(
					new IOContentLoader(action, request, callback), mExpiration);

			if (action == AccessPolicy.REFRESH || isExpired) {
				// invalidate cache item if any to start new task
				mMemCache.remove(key, future);
			}

			future = mMemCache.putIfAbsent(key, newFutureTask);
			if (future == null) {
				// no tasks inserted in the meantime, execute it
				future = newFutureTask;
				newFutureTask.run();
			}
		}
		try {
			// wait for the task execution completion
			D model = future.get();
			if (model == null) {
				revertOnFailure(key, oldFutureTask, future, isExpired);
			}
			return model;
		} catch (CancellationException e) {
			revertOnFailure(key, oldFutureTask, future, isExpired);
		} catch (ExecutionException e) {
			// we don't want the cache to be polluted with failed attempts
			revertOnFailure(key, oldFutureTask, future, isExpired);
			throw Memoizer.launderThrowable(e.getCause());
		}
		return null;
	}

	/**
	 * Fallback method to handle the case when a task isn't successful and needs
	 * to be removed from the cache to avoid pollution. For some
	 * {@link AccessPolicy}s, it is also needed to replace the failed new task
	 * with the old one (if any).
	 * 
	 * @param key
	 *            The cache key string
	 * @param oldTask
	 *            The old, removed future task
	 * @param newTask
	 *            The new, failed future task to be removed
	 * @param putOld
	 *            true to synchronously put the old model in the cache again if
	 *            no other new tasks have been added, false otherwise
	 */
	private void revertOnFailure(String key, ExpirableFutureTask<D> oldTask,
			ExpirableFutureTask<D> newTask, boolean putOld) {
		mMemCache.remove(key, newTask);
		if (oldTask != null && putOld) {
			mMemCache.putIfAbsent(key, oldTask);
		}
	}

	/**
	 * Callable that executes loading of data from disk or network
	 */
	private class IOContentLoader implements Callable<D> {

		private final AccessPolicy mAction;
		private final CacheableRequest<D> mRequest;
		@Nullable
		private final ContentUpdateCallback<D> mUpdateCallback;

		public IOContentLoader(@Nonnull AccessPolicy action, @Nonnull CacheableRequest<D> request,
				@Nullable ContentUpdateCallback<D> callback) {
			mAction = action;
			mRequest = request;
			mUpdateCallback = callback;
		}

		@Override
		@SuppressWarnings("unchecked")
		public D call() throws Exception {

			final String key = mRequest.hash();
			D model = null;

			/** Disk cache access */
			if (mDiskCache != null) {
				if (mAction == AccessPolicy.CACHE_ONLY) {
					// do not take care of cache expiration
					model = mDiskCache.get(key);

					/** cache debugging */
					if (DroidConfig.DEBUG) {
						final String url = mRequest.getRequestUrl();
						if (model != null) {
							Log.v(TAG, "CACHE_ONLY: Disk cache hit for " + url);
						} else {
							Log.v(TAG, "CACHE_ONLY: Disk cache miss for " + url);
						}
					}
					/** cache debugging - END */

					// caches retrieval failed, don't attempt a request
					return model;
				} else if (mAction == AccessPolicy.REFRESH) {
					mDiskCache.remove(key);
				} else {
					// check disk cache (and verify item expiration)
					if ((model = mDiskCache.get(key, mExpiration)) != null) {
						return model;
					} else {
						/** cache debugging */
						if (DroidConfig.DEBUG) {
							Log.v(TAG, "Disk cache miss or expired for " + mRequest.getRequestUrl());
						}
						/** cache debugging - END */
					}
				}
			}

			/** execute GET request to the server */
			if (mRequestHandler != null) {
				model = (D) mRequestHandler.execRequest(mRequest);
			} else {
				model = mRequest.execute();
			}
			if (model != null) { // update caches
				if (mDiskCache != null) {
					mDiskCache.put(key, model);
				}
				if (mUpdateCallback != null) {
					mUpdateCallback.onContentUpdated(model);
				}
			} else if (mAction == AccessPolicy.NORMAL) {
				// fallback when request failed
				if (mDiskCache != null) {
					// set model to any stale data we might find
					model = mDiskCache.get(key);
				}
				/** cache debugging */
				if (DroidConfig.DEBUG) {
					final String url = mRequest.getRequestUrl();
					if (model != null) {
						Log.d(TAG, "Fallback: Disk cache hit for " + url);
					} else {
						Log.d(TAG, "Fallback: Disk cache miss for " + url);
					}
				}
				/** cache debugging - END */
			}
			// FIXME: should we throw an exception rather than returning null?
			return model;
		}
	}

}