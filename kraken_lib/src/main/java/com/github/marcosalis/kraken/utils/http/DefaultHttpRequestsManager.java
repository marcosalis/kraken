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

import android.app.Application;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.api.client.util.Preconditions;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Default implementation of {@link HttpRequestsManager}, a decorator around the
 * Google library {@link HttpRequestFactory} to build requests using the same
 * transport and optimizing reusable components. It just takes care of using the
 * same {@link HttpTransport} for any connection and uses an
 * {@link HttpRequestFactory} initialised with default connection parameters,
 * user agent and timeouts.
 *
 * The global instance, suitable for most uses, can be retrieved by calling
 * {@link #get()}, and {@link #initialize(ConnectionKeepAliveStrategy)} must be
 * called before accessing the request facory.
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
public class DefaultHttpRequestsManager implements HttpRequestsManager {

	private static final String TAG = DefaultHttpRequestsManager.class.getSimpleName();

	/**
	 * Globally accessible instance of the default HTTP connection manager.
	 */
	private static final DefaultHttpRequestsManager INSTANCE = new DefaultHttpRequestsManager();

	private volatile HttpRequestFactory mDefaultRequestFactory;

	/**
	 * Shortcut method to return the {@link DefaultHttpRequestsManager} global
	 * instance.
	 */
	public static DefaultHttpRequestsManager get() {
		return INSTANCE;
	}

	private DefaultHttpRequestsManager() {
		final Level logLevel = DroidConfig.DEBUG ? Level.CONFIG : Level.OFF;
		Logger.getLogger(HttpTransport.class.getName()).setLevel(logLevel);
	}

	/**
	 * Initializes the {@link DefaultHttpRequestsManager}. Call this preferably
	 * from the {@link Application#onCreate()} method.
	 *
	 * @param strategy
	 *            The {@link ConnectionKeepAliveStrategy} if
	 *            {@link ApacheHttpTransport} is used.
	 */
	@CallSuper
	public synchronized void initialize(@NonNull ConnectionKeepAliveStrategy strategy) {

		if (DroidConfig.DEBUG) { // logging system properties values
			Log.d(TAG, "http.maxConnections: " + System.getProperty("http.maxConnections"));
			Log.d(TAG, "http.keepAlive: " + System.getProperty("http.keepAlive"));
		}
		/*
		 * Get the best HTTP client for the current Android version, mimicking
		 * the behavior of the method AndroidHttp.newCompatibleTransport(). As
		 * of now, ApacheHttpTransport appears to be much faster and less
		 * CPU-consuming than NetHttpTransport on Gingerbread, so we use the
		 * latter only for API >= 11
		 */
		if (AndroidUtils.isMinimumSdkLevel(11)) {
			// use NetHttpTransport as default connection transport
			mDefaultRequestFactory = createRequestFactory(new NetHttpTransport());
		} else {
			/* Use custom DefaultHttpClient to set the keep alive strategy */
			final DefaultHttpClient httpClient = ApacheHttpTransport.newDefaultHttpClient();
			if (strategy != null) {
				httpClient.setKeepAliveStrategy(strategy);
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
			mDefaultRequestFactory = createRequestFactory(new ApacheHttpTransport(httpClient));
		}
	}

	/**
	 * Same as {@link #initialize(ConnectionKeepAliveStrategy)} with a
	 * {@link DefaultConnectionKeepAliveStrategy}.
	 */
	public synchronized void initialize() {
		initialize(new DefaultConnectionKeepAliveStrategy());
	}

	/**
	 * <b>Only for testing purposes.</b><br>
	 * Inject a custom {@link HttpTransport} inside the manager
	 *
	 * @param transport
	 *            The {@link HttpTransport} to inject
	 */
	@VisibleForTesting
	synchronized void injectTransport(@NonNull HttpTransport transport) {
		mDefaultRequestFactory = createRequestFactory(transport);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The HttpRequest objects created with this factory don't throw exceptions
	 * if the request is not successful (response < 200 or >299), so you have to
	 * check the HTTP result code within the response.
	 */
	@NonNull
	@Override
	public HttpRequestFactory getRequestFactory() {
		Preconditions.checkArgument(mDefaultRequestFactory != null, "initialize() not called");
		return mDefaultRequestFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public HttpRequestFactory createRequestFactory(@NonNull HttpTransport transport) {
		return transport.createRequestFactory(new DefaultHttpRequestInitializer());
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull
	@Override
	public HttpRequest buildRequest(@NonNull String method, @NonNull String urlString,
			@Nullable HttpContent content) throws IOException {
		Preconditions.checkArgument(mDefaultRequestFactory != null, "initialize() not called");
		return mDefaultRequestFactory.buildRequest(method, new GenericUrl(urlString), content);
	}

}