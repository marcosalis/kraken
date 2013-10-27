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
package com.github.marcosalis.kraken.utils.http;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.api.client.extensions.android.AndroidUtils;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

/**
 * Library level class to handle network connections using the same transport
 * and optimizing reusable components. This default implementation provides
 * basic functionalities, and it just takes care of using the same
 * {@link HttpTransport} for any connection and can be used to retrieve an
 * {@link HttpRequestFactory} initialised with default connection parameters,
 * user agent and timeouts.
 * 
 * The global instance, suitable for most uses, can be retrieved by calling
 * {@link #get()}.
 * 
 * Please note that no limits are put on the number of concurrent connection at
 * this abstraction level. Clients must implement their own pooling mechanism to
 * limit it or just rely on the wrapped connection library pooling policies.
 * 
 * To debug network connections executed from the {@link HttpTransport} library
 * in a device or emulator do the following:
 * 
 * <ul>
 * <li>set the DEBUG flag to true in the {@link DroidConfig} class</li>
 * <li>add
 * <i>Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.CONFIG
 * );</i> before the execute call in your code</li>
 * <li>type <i>adb shell setprop log.tag.HttpTransport DEBUG</i> in a terminal
 * to enable debug logging for the transport class in the connected device or
 * emulator</li>
 * </ul>
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@ThreadSafe
public class DefaultHttpConnectionManager implements HttpConnectionManager {

	private static final String TAG = DefaultHttpConnectionManager.class.getSimpleName();

	/**
	 * Globally accessible instance of the default HTTP connection manager.
	 */
	private static final DefaultHttpConnectionManager INSTANCE = new DefaultHttpConnectionManager();

	private volatile HttpTransport mDefaultHttpTransport;
	private volatile HttpRequestFactory mDefaultRequestFactory;

	/**
	 * Shortcut method to return the {@link DefaultHttpConnectionManager} global
	 * instance.
	 */
	public static DefaultHttpConnectionManager get() {
		return INSTANCE;
	}

	private DefaultHttpConnectionManager() {
		final Level logLevel = DroidConfig.DEBUG ? Level.CONFIG : Level.OFF;
		Logger.getLogger(HttpTransport.class.getName()).setLevel(logLevel);
	}

	/**
	 * Initializes the {@link DefaultHttpConnectionManager}
	 * 
	 * @param keepAliveStrategy
	 *            The {@link ConnectionKeepAliveStrategy} if
	 *            {@link ApacheHttpTransport} is used.
	 */
	@OverridingMethodsMustInvokeSuper
	public synchronized void initialize(@Nullable ConnectionKeepAliveStrategy keepAliveStrategy) {

		if (DroidConfig.DEBUG) { // logging system properties values
			Log.d(TAG, "http.maxConnections: " + System.getProperty("http.maxConnections"));
			Log.d(TAG, "http.keepAlive: " + System.getProperty("http.keepAlive"));
		}
		/*
		 * Get the best HTTP client for the current Android version, mimicking
		 * the behavior of the method AndroidHttp.newCompatibleTransport(). As
		 * of now, ApacheHttpTransport appears to be much less CPU-consuming
		 * than NetHttpTransport on Gingerbread, so we use the latter only for
		 * API >= 11
		 */
		if (AndroidUtils.isMinimumSdkLevel(11)) {
			// use HttpURLConnection as default connection transport
			mDefaultHttpTransport = new NetHttpTransport();
		} else {
			/* Use custom DefaultHttpClient to set the keep alive strategy */
			final DefaultHttpClient httpClient = ApacheHttpTransport.newDefaultHttpClient();
			if (keepAliveStrategy != null) {
				httpClient.setKeepAliveStrategy(keepAliveStrategy);
			}
			/**
			 * Android has a known issue that causes the generation of unsafe
			 * {@link SecureRandom} values which can affect secure connections
			 * with the Apache http library. See the link below for more
			 * information.
			 * 
			 * <pre>
			 * http://android-developers.blogspot.com.au/2013/08/some-securerandom-thoughts.html
			 * </pre>
			 */
			mDefaultHttpTransport = new ApacheHttpTransport(httpClient);
		}

		mDefaultRequestFactory = createStandardRequestFactory(mDefaultHttpTransport);
	}

	/**
	 * ONLY FOR TESTING PURPOSES<br>
	 * Inject a custom {@link HttpTransport} inside the manager
	 * 
	 * @param transport
	 *            The {@link HttpTransport} to inject
	 */
	@VisibleForTesting
	synchronized void injectTransport(@Nonnull HttpTransport transport) {
		mDefaultHttpTransport = transport;
		mDefaultRequestFactory = createStandardRequestFactory(transport);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The HttpRequest objects created with this factory don't throw exceptions
	 * if the request is not successful (response < 200 or >299), so you have to
	 * check the HTTP result code within the response.
	 */
	@Nonnull
	@Override
	public HttpRequestFactory getRequestFactory() {
		return mDefaultRequestFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public HttpRequestFactory createRequestFactory(@Nonnull HttpTransport transport) {
		return createStandardRequestFactory(transport);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public HttpRequest buildRequest(@Nonnull String method, @Nonnull String urlString,
			@Nullable HttpContent content) throws IOException {
		return mDefaultRequestFactory.buildRequest(method, new GenericUrl(urlString), content);
	}

	/**
	 * Returns the default {@link HttpTransport} used by the manager.
	 */
	@Nonnull
	public HttpTransport getDefaultHttpTransport() {
		return mDefaultHttpTransport;
	}

	/**
	 * Initialize here {@link HttpRequest}'s parameters for the request factory
	 * to other servers
	 * 
	 * @param transport
	 *            The {@link HttpTransport} used to create requests
	 * @return The created {@link HttpRequestFactory}
	 */
	@Nonnull
	private HttpRequestFactory createStandardRequestFactory(HttpTransport transport) {
		return transport.createRequestFactory(new DefaultHttpRequestInitializer());
	}

}