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
package com.github.marcosalis.kraken.utils.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.github.marcosalis.kraken.utils.annotations.NotForUIThread;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.common.annotations.Beta;
import com.google.common.io.ByteStreams;

/**
 * Simple {@link Callable} task to download raw data through a GET request to
 * the given URL and converts the resulting stream to a byte array.
 * 
 * When performance is critical and a non-UI thread is available, prefer using
 * the {@link #downloadByteArray(HttpRequestFactory, String)} static method to
 * save in object instantiation.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public final class ByteArrayDownloader implements Callable<byte[]> {

	private static final String TAG = ByteArrayDownloader.class.getSimpleName();

	private final HttpRequestFactory mRequestFactory;
	private final String mUrl;

	/**
	 * Uses the default {@link HttpRequestFactory}
	 */
	public ByteArrayDownloader(@Nonnull String url) {
		this(DefaultHttpRequestsManager.get().getRequestFactory(), url);
	}

	public ByteArrayDownloader(@Nonnull HttpRequestFactory factory, @Nonnull String url) {
		mRequestFactory = factory;
		mUrl = url;
	}

	@Override
	public byte[] call() throws IOException, IllegalArgumentException {
		return downloadByteArray(mRequestFactory, mUrl);
	}

	/**
	 * Directly downloads the byte array.
	 * 
	 * @param factory
	 *            The {@link HttpRequestFactory}
	 * @param url
	 *            The string URL to download from
	 * @return The byte array from the stream or null if an error occurred
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	@CheckForNull
	@NotForUIThread
	public static byte[] downloadByteArray(@Nonnull HttpRequestFactory factory, @Nonnull String url)
			throws IOException, IllegalArgumentException {
		HttpRequest request = null;
		HttpResponse response = null;
		byte[] bytes = null;

		if (DroidConfig.DEBUG) {
			Log.d(TAG, "Executing GET request to: " + url);
		}

		try {
			request = factory.buildRequest(HttpMethods.GET, new GenericUrl(url), null);
			response = request.execute();

			if (response.isSuccessStatusCode()) {
				// get input stream and converts it to byte array
				InputStream stream = new BufferedInputStream(response.getContent());
				bytes = ByteStreams.toByteArray(stream);

				if (DroidConfig.DEBUG && bytes != null) {
					Log.v(TAG, "GET request successful to: " + url);
				}
			}
		} finally {
			if (response != null) {
				try {
					response.disconnect();
				} catch (IOException e) { // just an attempt to close the stream
					LogUtils.logException(e);
				}
			}
		}
		return bytes;
	}

}