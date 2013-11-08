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
package com.github.marcosalis.kraken.testing.mock;

import java.io.IOException;

import android.content.Context;

import com.github.marcosalis.kraken.cache.ModelDiskCache;
import com.github.marcosalis.kraken.cache.json.JacksonJsonManager;
import com.github.marcosalis.kraken.cache.json.JsonModel;

/**
 * Mock implementation of {@link ModelDiskCache} for testing purposes.
 * 
 * All methods throw {@link UnsupportedOperationException}
 * 
 * @since 1.0
 * @author Marco Salis
 */
@SuppressWarnings("deprecation")
public class MockModelDiskCache<V extends JsonModel> extends ModelDiskCache<V> {

	public MockModelDiskCache(Context context, String subFolder, Class<V> modelClass)
			throws IOException {
		super(context, JacksonJsonManager.getObjectMapper(), subFolder, modelClass);
	}

	@Override
	public V get(String key) {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	public V get(String key, long expiration) {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	public boolean put(String key, V model) {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	public boolean remove(String key) {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	public void clear(DiskCacheClearMode mode) {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	public void scheduleClearAll() {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	protected void purge(long olderThan) {
		throw new UnsupportedOperationException("Mock!");
	}

	@Override
	protected void schedulePurge(long olderThan) {
		throw new UnsupportedOperationException("Mock!");
	}

}
