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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.github.marcosalis.kraken.utils.network.NetworkBroadcastReceiver.NetworkIntent;
import com.google.common.annotations.Beta;

/**
 * Network connectivity changes notifier. Clients must register to get
 * connection updates through the application's {@link LocalBroadcastManager}
 * and passing an anonymous extension of this class as shown below:
 * 
 * <pre>
 * LocalBroadcastManager.getInstance(context).registerReceiver(new NetworkReceiver() {
 * 	&#064;Override
 * 	public void onConnectionActive(int type) {
 * 		// do your stuff here
 * 	}
 * 
 * 	&#064;Override
 * 	public void onConnectionGone() {
 * 		// do your stuff here
 * 	}
 * }, NetworkReceiver.getFilter());
 * </pre>
 * 
 * Do <b>NOT</b> forget to unregister the receiver by calling the
 * unregisterReceiver() method of the {@link LocalBroadcastManager}.
 * 
 * For activities, the recommended way to register and unregister the receiver
 * is in the onResume() and onPause() methods.
 * 
 * <b>Note:</b> due to an Android issue, in several cases a connection change
 * gets notified more than once by the BroadcastReceiver. Use your own state
 * control when performing atomic operations inside the receiver callbacks.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public abstract class NetworkReceiver extends BroadcastReceiver {

	/**
	 * Return the {@link IntentFilter} to subscribe to network connectivity
	 * changes
	 */
	public static IntentFilter getFilter() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(NetworkBroadcastReceiver.ACTION_NETWORK_ACTIVE);
		filter.addAction(NetworkBroadcastReceiver.ACTION_NETWORK_GONE);
		return filter;
	}

	@Override
	public final void onReceive(Context context, Intent intent) {
		if (intent instanceof NetworkIntent) {
			final NetworkIntent netIntent = (NetworkIntent) intent;
			if (netIntent.getAction().equals(NetworkBroadcastReceiver.ACTION_NETWORK_ACTIVE)) {
				onConnectionActive(netIntent.getNetworkType());
			} else {
				onConnectionGone();
			}
		}
	}

	/**
	 * Called when a network connection becomes available and active
	 * 
	 * @param type
	 *            The connection type. Can be one of those listed in
	 *            {@link ConnectionManager}
	 */
	public abstract void onConnectionActive(int type);

	/**
	 * Called when a network connection is not available anymore
	 */
	public abstract void onConnectionGone();

}