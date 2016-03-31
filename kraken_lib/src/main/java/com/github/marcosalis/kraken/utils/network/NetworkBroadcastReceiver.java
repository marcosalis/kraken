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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.common.annotations.Beta;

/**
 * {@link BroadcastReceiver} for notifying application components about network connection
 * availability changes on the device, such as a loss of data connectivity or the user disabling the
 * active wireless.
 *
 * <b>Note:</b> clients should not use this class directly.<br> See the {@link NetworkReceiver}
 * documentation instead.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
class NetworkBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION_NETWORK_ACTIVE = "com.github.luluvise.droid_utils.action.NETWORK_ACTIVE";
    static final String ACTION_NETWORK_GONE = "com.github.luluvise.droid_utils.action.NETWORK_GONE";

    private static final NetworkIntent NETWORK_INTENT_GONE = new NetworkIntent(ACTION_NETWORK_GONE);

    private static final String TAG = NetworkBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        logNetworkInfo(intent);

        // use EXTRA_NO_CONNECTIVITY to check if there is no connection
        if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
            notifyConnectionGone(context);
            return;
        }

        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();

        // send local broadcast to notify all registered receivers
        if (netInfo != null && netInfo.isConnected()) { // connection active
            notifyConnectionActive(context, netInfo.getType());
        } else {
            // connection has gone
            notifyConnectionGone(context);
        }
    }

    private void notifyConnectionActive(@NonNull Context context, int type) {
        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcastSync(new NetworkIntent(ACTION_NETWORK_ACTIVE, type));
        if (DroidConfig.DEBUG) {
            Log.d(TAG, "Network connection is back, type: " + type);
        }
    }

    private void notifyConnectionGone(@NonNull Context context) {
        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcastSync(NETWORK_INTENT_GONE);
        if (DroidConfig.DEBUG) {
            Log.d(TAG, "Network connection is gone");
        }
    }

    private void logNetworkInfo(@NonNull Intent intent) {
        if (DroidConfig.DEBUG) { // debugging network info
            final NetworkInfo otherNetworkInfo = (NetworkInfo) intent
                    .getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            final String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            final boolean failover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER,
                    false);
            Log.i(TAG, "Network info: " + " otherNetworkInfo = "
                    + (otherNetworkInfo == null ? "[none]" : otherNetworkInfo) + ", failover="
                    + failover + ", reason=" + reason);
        }
    }

    /**
     * Simple extension of the Intent class used by NetworkReceiver and client classes to handle
     * intents about a network connection change notification.
     */
    static class NetworkIntent extends Intent {

        private static final String NETWORK_TYPE = "NETWORK_TYPE";

        /**
         * {@inheritDoc}
         */
        NetworkIntent(String action) {
            super(action);
        }

        /**
         * @param type The type of an active network connection
         * @see Intent#Intent(String)
         */
        NetworkIntent(String action, int type) {
            super(action);
            setNetworkType(type);
        }

        /**
         * Get the intent's connection type, if set. Can be one of those listed in {@link
         * ConnectionManager} or -1 if not set
         */
        int getNetworkType() {
            return getIntExtra(NETWORK_TYPE, -1);
        }

        private void setNetworkType(int type) {
            putExtra(NETWORK_TYPE, type);
        }
    }

}