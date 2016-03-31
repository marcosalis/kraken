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

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.google.common.annotations.Beta;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Application global network connection monitor.<br> Useful to easily retrieve the device's current
 * connection status without querying the {@link ConnectivityManager}.
 *
 * Must be initialized by calling {@link #register(Application)} within the {@link
 * Application#onCreate()} method.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@ThreadSafe
public enum ConnectionDefaultMonitor implements ConnectionMonitor {
    INSTANCE;

    /**
     * Shortcut method to return the {@link ConnectionDefaultMonitor} singleton instance.
     */
    public static ConnectionDefaultMonitor get() {
        return INSTANCE;
    }

    private final static String TAG = ConnectionDefaultMonitor.class.getSimpleName();

    private final AtomicBoolean mConnectionActive;
    private final AtomicBoolean mIsRegistered;
    private final NetworkReceiver mNetReceiver = new NetworkReceiver() {
        @Override
        public void onConnectionActive(int type) {
            mConnectionActive.compareAndSet(false, true);
        }

        @Override
        public void onConnectionGone() {
            mConnectionActive.compareAndSet(true, false);
        }
    };

    private ConnectionDefaultMonitor() {
        // defaults to true to avoid issue at initialization
        mConnectionActive = new AtomicBoolean(true);
        mIsRegistered = new AtomicBoolean(false);
    }

    /**
     * Initializes the {@link ConnectionDefaultMonitor}. It registers a {@link NetworkReceiver} in
     * order to be notified about network state changes.
     *
     * @param application The {@link Application} object
     */
    public void register(@NonNull Application application) {
        if (mIsRegistered.compareAndSet(false, true)) {
            // permanently register the listener using the application context
            final IntentFilter filter = NetworkReceiver.getFilter();
            LocalBroadcastManager.getInstance(application).registerReceiver(mNetReceiver, filter);
        } else {
            LogUtils.log(Log.WARN, TAG, "ConnectionMonitor multiple initialization attempt");
        }
    }

    /**
     * Unregisters the {@link NetworkReceiver} to stop being notified about network state changes.
     *
     * @param application The {@link Application} object
     */
    public void unregister(@NonNull Application application) {
        if (mIsRegistered.compareAndSet(true, false)) {
            LocalBroadcastManager.getInstance(application).unregisterReceiver(mNetReceiver);
        }
    }

    @Override
    public boolean isRegistered() {
        return mIsRegistered.get();
    }

    @Override
    public boolean isNetworkActive() {
        return mConnectionActive.get();
    }

}