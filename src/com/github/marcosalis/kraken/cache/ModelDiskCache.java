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
package com.github.marcosalis.kraken.cache;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.utils.StorageUtils.CacheLocation;
import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.google.api.client.util.ObjectParser;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Disk cache that stores a POJO/DTO object into its JSON representation in text
 * files.
 * 
 * TODO: use {@link JsonFactory} instead of Jackson's {@link ObjectMapper}
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@NotThreadSafe
public class ModelDiskCache<V> extends SimpleDiskCache<V> {

	private static final String TAG = ModelDiskCache.class.getSimpleName();

	private static final String PATH = "model";

	private static final long PURGE_AFTER = MIN_EXPIRE_IN_SEC * 2;

	private final Class<V> mModelClass;
	private final ObjectMapper mObjectMapper;

	/**
	 * Builds a {@link ModelDiskCache} that stores POJO/DTO objects as (plain)
	 * text into the passed sub-folder, using the passed {@link ObjectParser}.
	 * 
	 * Note that the {@link CacheLocation#INTERNAL} cache is always used. If no
	 * internal caches are present, it falls back to the external one.
	 * 
	 * TODO: allow choosing whether to store in ext storage for failover?
	 * 
	 * @param context
	 *            The {@link Context} to use
	 * @param mapper
	 *            The {@link ObjectMapper} to use for model de/serialization
	 * @param subFolder
	 *            The relative path to the cache folder where to store the cache
	 *            (if it doesn't exist, the folder is created)
	 * @param modelClass
	 *            The POJO object to store class type
	 * @throws IOException
	 */
	public ModelDiskCache(@Nonnull Context context, @Nonnull ObjectMapper mapper,
			@Nonnull String subFolder, @Nonnull Class<V> modelClass) throws IOException {
		super(context, CacheLocation.INTERNAL, PATH + File.separator + subFolder, true);
		mModelClass = modelClass;
		mObjectMapper = mapper;
		if (DroidConfig.DEBUG) {
			Log.d(TAG, "Model disk cache created at: " + mCacheLocation.getAbsolutePath());
		}
	}

	/**
	 * Gets an item from the disk cache
	 * 
	 * @param key
	 *            The cache item key (must be not null)
	 * @return The cached item, null if not present or an error occurred
	 */
	@CheckForNull
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	public V get(@Nonnull String key) {
		File jsonFile = new File(mCacheLocation, key);
		if (!jsonFile.exists()) {
			return null;
		}
		try {
			return mObjectMapper.readValue(jsonFile, mModelClass);
		} catch (IOException e) {
			LogUtils.logException(TAG, "Exception when reading " + key, e);
			jsonFile.delete(); // try to delete damaged file
			return null;
		}
	}

	/**
	 * Gets an item from the disk cache only if it is not expired.
	 * 
	 * <b>Note on expiration:</b> This method uses the
	 * {@link File#lastModified()} method to check if the cache item is expired.
	 * Sometimes in Android this value is rounded to the second, so using very
	 * small expiration times can result in a cache miss even if the item would
	 * be valid.
	 * 
	 * @param key
	 *            The cache item key (must be not null)
	 * @param expiration
	 *            The validity of the item from its modification in
	 *            milliseconds, or {@link Long#MAX_VALUE} for no expiration
	 * @return The cached item, null if not present, expired or an error
	 *         occurred
	 */
	@CheckForNull
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	public V get(@Nonnull String key, long expiration) {
		File jsonFile = new File(mCacheLocation, key);
		if (!jsonFile.exists()) {
			return null;
		}
		final boolean noExpire = expiration == Long.MAX_VALUE;
		if (noExpire || (jsonFile.lastModified() + expiration) > System.currentTimeMillis()) {
			try { // the item is still valid, try parsing it
				return mObjectMapper.readValue(jsonFile, mModelClass);
			} catch (IOException e) { // something wrong happened
				LogUtils.logException(TAG, "Exception when reading " + key, e);
				jsonFile.delete(); // try to delete damaged file
				return null;
			}
		}
		return null;
	}

	/**
	 * Puts an item into the disk cache.
	 * 
	 * @param key
	 *            The cache item key (must be not null)
	 * @param model
	 *            The value to put (must be not null)
	 * @return true if successful, false otherwise (IO error while saving the
	 *         stream)
	 */
	public boolean put(@Nonnull String key, @Nonnull V model) {
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(model);
		// don't care if the file already exist, it will be replaced
		File jsonFile = new File(mCacheLocation, key);
		try {
			mObjectMapper.writeValue(jsonFile, model);
		} catch (IOException e) {
			LogUtils.logException(TAG, "Exception when writing " + key, e);
			return false;
		}
		return true;
	}

	/**
	 * Deletes an item from the disk cache.
	 * 
	 * @param key
	 *            The cache item key to remove (must be not null)
	 * @return true if successful, false otherwise
	 */
	public boolean remove(@Nonnull String key) {
		Preconditions.checkNotNull(key);
		File jsonFile = new File(mCacheLocation, key);
		return jsonFile.delete();
	}

	@Override
	@NotForUIThread
	public final void clearOld() {
		purge(PURGE_AFTER);
	}

}