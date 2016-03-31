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
package com.github.marcosalis.kraken.utils.network;

import android.support.annotation.NonNull;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.annotations.Beta;

/**
 * Helper class that contains static methods to check the network connection
 * state and other network-related properties.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class NetworkUtils {

	private NetworkUtils() {
		// hidden constructor, no instantiation needed
	}

	/**
	 * Check whether there is *any* active network connection available.
	 * 
	 * Requires the <i>android.permission.ACCESS_NETWORK_STATE</i> permission.
	 * 
	 * @param context
	 *            The application or activity context
	 * @return true if a connection is currently active, false otherwise
	 */
	public static boolean isConnectionEnabled(@NonNull Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return netInfo != null && netInfo.isConnected();
	}

}