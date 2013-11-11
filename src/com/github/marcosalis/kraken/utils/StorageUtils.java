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
package com.github.marcosalis.kraken.utils;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import android.content.Context;
import android.os.Environment;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

/**
 * Helper class containing static methods to retrieve information about the
 * default cache folders and perform operations on the device storage units,
 * either internal or external.
 * 
 * See {@link http://developer.android.com/guide/topics/data/data-storage.html}
 * 
 * Access and write to external storage requires the
 * <code>READ_EXTERNAL_STORAGE</code> and <code>WRITE_EXTERNAL_STORAGE</code>
 * Manifest permissions.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class StorageUtils {

	/**
	 * Application storage caches locations
	 */
	public enum CacheLocation {
		INTERNAL,
		EXTERNAL
	}

	/**
	 * Default external cache storage path for Android apps
	 */
	static final String EXT_CACHE_PATH = "Android/data/%s/cache/";

	private static final String TEMP_FOLDER = File.separator + "temp" + File.separator;

	private StorageUtils() {
		// hidden constructor, no instantiation needed
	}

	/**
	 * Check whether the device external storage is mounted using the
	 * {@link Environment} Android API helper methods.
	 * 
	 * @return true if the external storage is mounted, false otherwise
	 */
	public static boolean isExternalStorageMounted() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * Retrieve the current working application cache directory, selected
	 * depending on the current application caching policy, the Android device
	 * platform version and the state of any existing external storage.
	 * 
	 * It is recommended to use a sub-directory of the returned one to avoid
	 * polluting the application's cache root.<br>
	 * 
	 * Note: this method is usually not suitable when large spaces caches are
	 * required in devices with two cache locations. Use
	 * {@code getAppCacheDir(CacheLocation.EXTERNAL, true)} instead.
	 * 
	 * The cache directory is automatically created if not existing, and the
	 * returned File (if not null) is guaranteed to exist and to be writable.
	 * 
	 * @param context
	 *            A {@link Context} to retrieve the caches location
	 * @return A File for the root directory to use for storing caches, or null
	 *         if an unrecoverable error prevented the method from getting any
	 *         suitable cache location
	 */
	@CheckForNull
	public static File getAppCacheDir(@Nonnull Context context) {
		// failover cache location to use when external storage is not mounted
		File cacheDir = context.getCacheDir();

		// attempt to use external storage cache directory
		File extCacheDir = getExternalAppCacheDir(context);
		if (extCacheDir != null) {
			cacheDir = extCacheDir;
		}

		return cacheDir;
	}

	/**
	 * Retrieve the current working application cache directory, trying to use
	 * the specified cache location.
	 * 
	 * When the fallback flag is set to true, if the preferred location is not
	 * available, the method falls back to the other available location (if
	 * any).
	 * 
	 * See {@link CacheUtils#getAppCacheDir()} for other recommendations.
	 * 
	 * @param context
	 *            A {@link Context} to retrieve the caches location
	 * @param location
	 *            The requested {@link CacheLocation}
	 * @param allowLocationFallback
	 *            Whether the disk cache can fallback to another location if the
	 *            selected one is not available
	 * @return a File containing the location, null if the specified caches are
	 *         not available
	 */
	@CheckForNull
	public static File getAppCacheDir(@Nonnull Context context, @Nonnull CacheLocation location,
			boolean allowLocationFallback) {
		File cacheDir = null;
		switch (location) {
		case INTERNAL:
			cacheDir = getInternalAppCacheDir(context);
			if (cacheDir == null && allowLocationFallback) {
				cacheDir = getExternalAppCacheDir(context);
			}
			break;
		case EXTERNAL:
			cacheDir = getExternalAppCacheDir(context);
			if (cacheDir == null && allowLocationFallback) {
				cacheDir = getInternalAppCacheDir(context);
			}
			break;
		}
		return cacheDir;
	}

	/**
	 * Helper method to retrieve the application internal storage cache
	 * directory and make sure it exists and it's writeable.
	 */
	@CheckForNull
	@VisibleForTesting
	static File getInternalAppCacheDir(@Nonnull Context context) {
		File intCacheDir = context.getCacheDir();
		if (intCacheDir != null && !intCacheDir.exists()) {
			if (!intCacheDir.mkdirs() || !intCacheDir.canWrite()) {
				intCacheDir = null;
			}
		}
		return intCacheDir;
	}

	/**
	 * Helper method to retrieve the application external storage cache
	 * directory and make sure it exists and it's writeable.
	 */
	@CheckForNull
	@VisibleForTesting
	static File getExternalAppCacheDir(@Nonnull Context context) {
		File extCacheDir = null;
		if (isExternalStorageMounted()) { // only works if mounted
			extCacheDir = context.getExternalCacheDir();
			if (extCacheDir != null) {

				// create directory tree if not existing
				if (!FileUtils.createDir(extCacheDir) || !extCacheDir.canWrite()) {
					return null; // can't create or write
				}
			}
		}
		return extCacheDir;
	}

	/**
	 * Returns a writable folder in the external storage (or internal, if not
	 * available) where to write temporary files. Note that these files are not
	 * automatically deleted until application uninstall, delete them manually.
	 * 
	 * @param context
	 *            A {@link Context} to retrieve the temp folder
	 * @return The temp folder {@link File}
	 */
	@CheckForNull
	public static File getTempFolder(@Nonnull Context context) {
		final File cacheDir = getAppCacheDir(context, CacheLocation.EXTERNAL, true);
		File tempDir = null;
		if (cacheDir != null) {
			tempDir = new File(cacheDir.getAbsolutePath() + TEMP_FOLDER);
			if (!tempDir.exists()) {
				if (!tempDir.mkdir()) {
					return null;
				}
			}
		}
		return tempDir;
	}

}