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
package com.github.marcosalis.kraken.cache;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import android.content.Context;
import android.os.Process;

import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.StorageUtils;
import com.github.marcosalis.kraken.utils.StorageUtils.CacheLocation;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.github.marcosalis.kraken.utils.concurrent.PriorityThreadFactory;
import com.google.common.annotations.Beta;

/**
 * Abstract prototype class of a file-system based cache, either on internal or
 * external storage. Common disk caching policies are implemented here.
 * 
 * <strong>Recommended disk caching policy:</strong><br>
 * <strong>Images (JPG):</strong> stored in external cache (fallback to internal
 * cache if no external storage device is available).<br>
 * <strong>Other data (sensible data in particular):</strong> stored in internal
 * cache.
 * 
 * <strong>Purge policy:</strong><br>
 * Purge elements with access date older than a set value (1 week or less to
 * avoid filling the device memory up). Files read for a cache hit are modified
 * in their last modified date to implement a raw LRU file cache and avoid
 * deleting recently used items.<br>
 * 
 * <b>Notes:</b><br>
 * - {@link File#setLastModified(long)} doesn't work properly on all Android
 * devices, so the purge policy should not delete items with a strict policy.<br>
 * - File I/O is not thread safe in Java. Attempts to perform any operation on a
 * file/folder from different threads at the same may result in undetermined
 * behavior and race conditions. Keep this in mind and always implement
 * fail-safe accesses to the disk caches.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public abstract class DiskCache<V> implements ContentCache<String, V> {

	/**
	 * Defines the possible policies to clear a disk cache.
	 */
	public enum DiskCacheClearMode {
		ALL,
		EVICT_OLD
	}

	/**
	 * Minimum expiration that can be set to a disk cache entry before it can
	 * get deleted (used to speed up setting of last modification date by
	 * avoiding calling I/O write OS methods when unnecessary).
	 */
	public static final long MIN_EXPIRE_IN_SEC = DroidUtils.DAY;
	public static final long MIN_EXPIRE_IN_MS = MIN_EXPIRE_IN_SEC * 1000;
	/**
	 * Default expiration time for the {@link DiskCache} subclasses (in seconds)
	 */
	public static final long DEFAULT_EXPIRE_IN_SEC = DroidUtils.DAY * 5;

	protected static final ExecutorService PURGE_EXECUTOR = Executors
			.newSingleThreadExecutor(new PriorityThreadFactory("DiskCache purge executor thread",
					Process.THREAD_PRIORITY_BACKGROUND));

	protected final File mCacheLocation;

	/**
	 * Basic setup operation for a disk cache.<br>
	 * Subclasses must handle the failure creating the cache or inform callers.
	 * 
	 * @param location
	 *            The preferred cache location
	 * @param subFolder
	 *            The relative path to the cache folder where to store the cache
	 *            (if it doesn't exist, the folder is created)
	 * @param canChange
	 *            Whether the disk cache can fallback to another location if the
	 *            selected one is not available
	 * @throws IOException
	 *             if the cache cannot be created
	 */
	protected DiskCache(@Nonnull Context context, @Nonnull CacheLocation location,
			@Nonnull String subFolder, boolean canChange) throws IOException {
		File cacheRoot = StorageUtils.getAppCacheDir(context, location, true);
		if (cacheRoot != null) {
			mCacheLocation = new File(cacheRoot.getAbsolutePath() + File.separator + subFolder);
			// setup cache directory
			if (!mCacheLocation.exists() && !mCacheLocation.mkdirs()) {
				throw new IOException("Disk cache location cannot be created");
			}
		} else {
			throw new IOException("Disk cache location cannot be found");
		}
	}

	@NotForUIThread
	public void clear(@Nonnull DiskCacheClearMode mode) {
		switch (mode) {
		case ALL:
			clearAll();
			break;
		case EVICT_OLD:
			clearOld();
			break;
		}
	}

	/**
	 * Executes a purge of all contents on this disk cache.
	 */
	@NotForUIThread
	protected void clearAll() {
		cleanCacheDir();
	}

	/**
	 * Purge old elements from this disk cache, according to the expiration
	 * policy of the implementation.
	 * 
	 * The definition of "old" must be specified by subclasses with an
	 * expiration time in seconds. Then they can simply call
	 * {@link #cleanCacheDir(long)} and pass the time value.
	 */
	protected abstract void clearOld();

	/**
	 * Asynchronously executes a purge of all contents on this disk cache.
	 * 
	 * TODO: use a more lightweight looper thread instead?
	 */
	public void scheduleClearAll() {
		PURGE_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				cleanCacheDir();
			}
		});
	}

	/**
	 * Execute a purge of all contents on this disk cache which are older than
	 * the passed value.
	 * 
	 * @param olderThan
	 *            The maximum "age", in seconds, of the elements to keep in
	 *            cache. This value must be, in any case, equals or higher than
	 *            {@value #MIN_EXPIRE_IN_SEC}
	 * @throws IllegalArgumentException
	 *             if olderThan is less than {@value #MIN_EXPIRE_IN_SEC}
	 */
	@NotForUIThread
	protected void purge(final long olderThan) {
		if (olderThan < MIN_EXPIRE_IN_SEC) {
			throw new IllegalArgumentException("olderThan too short");
		} else {
			cleanCacheDir(olderThan);
		}
	}

	/**
	 * Asynchronously execute a purge of all contents on this disk cache which
	 * are older than the passed value.
	 * 
	 * @param olderThan
	 *            The maximum "age", in seconds, of the elements to keep in
	 *            cache. This value must be, in any case, equals or higher than
	 *            {@value #MIN_EXPIRE_IN_SEC}
	 * @throws IllegalArgumentException
	 *             if olderThan is less than {@value #MIN_EXPIRE_IN_SEC}
	 */
	protected void schedulePurge(final long olderThan) {
		if (olderThan < MIN_EXPIRE_IN_SEC) {
			throw new IllegalArgumentException("olderThan too short");
		} else {
			PURGE_EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					cleanCacheDir(olderThan);
				}
			});
		}
	}

	/**
	 * Delete all files in the given directory (ignoring sub-directories).<br>
	 * Do NOT call from the UI thread.
	 * 
	 * @param dir
	 *            The directory to clean up
	 */
	@NotForUIThread
	protected final void cleanCacheDir() {
		if (mCacheLocation.exists()) {
			final File[] files = mCacheLocation.listFiles();
			if (files != null) {
				// iterate over files in the directory
				for (File f : files) {
					if (f.isFile()) { // ignore sub dirs
						f.delete();
					}
				}
			}
		}
	}

	/**
	 * Delete files older than the given parameter in the given directory
	 * (ignoring sub-directories).
	 * 
	 * Note: this operation is time-consuming and should be done from a
	 * dedicated thread, if possible when the application is not running.
	 * 
	 * @param dir
	 *            The directory to clean
	 * @param olderThan
	 *            The max "age" from the current time for the file to be kept
	 *            (in seconds)
	 */
	@NotForUIThread
	protected final void cleanCacheDir(@Nonnegative long olderThan) {
		final long now = System.currentTimeMillis();
		final long expire = olderThan * 1000;
		if (mCacheLocation.exists()) {
			final File[] files = mCacheLocation.listFiles();
			if (files != null) {
				for (File f : files) { // iterate over files
					long lastMod;
					if (f.isFile()) { // ignore sub dirs
						lastMod = f.lastModified();
						if (lastMod < (now - expire)) {
							// delete if older than max
							f.delete();
						}
					}
				}
			}
		}
	}

	/**
	 * Deletes the cache location directory. To be used only for testing
	 * purposes.
	 * 
	 * @return true if the directory no longer exists, false otherwise
	 */
	@NotForUIThread
	protected final boolean deleteCacheDir() {
		if (mCacheLocation.exists()) {
			return mCacheLocation.delete();
		}
		return true;
	}

	/**
	 * Implements the current policy for marking a file to avoid purging it. The
	 * method currently "touches" the file by setting its modification date only
	 * if the file is older than the MIN_EXPIRE_IN value (it is useless and time
	 * consuming to set it at every access).
	 * 
	 * See the note regarding {@link File#setLastModified(long)} in the class
	 * documentation.
	 * 
	 * @param file
	 *            The {@link File} to "touch" if necessary
	 */
	@NotForUIThread
	protected static final void touchFile(@Nonnull File file) {
		final long now = System.currentTimeMillis();
		if (file.lastModified() < (now - MIN_EXPIRE_IN_MS)) {
			file.setLastModified(now);
		}
	}

}