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
package com.github.marcosalis.kraken.utils.network;

import com.google.common.annotations.Beta;

/**
 * Interface for a network connection monitor.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
public interface ConnectionMonitor {

    /**
     * Checks if the connection monitor has already been registered.
     *
     * @return true if it is already registered, false otherwise
     */
    public boolean isRegistered();

    /**
     * Checks the current device's network connection state.
     *
     * @return true if there is an active network connection or the monitor hasn't been initialized,
     * false otherwise
     */
    public boolean isNetworkActive();

}