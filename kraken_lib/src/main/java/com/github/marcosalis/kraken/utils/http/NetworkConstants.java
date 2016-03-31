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

import android.support.annotation.NonNull;

import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler.BackOffRequired;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.common.annotations.Beta;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;

import javax.annotation.concurrent.Immutable;

/**
 * Simple class holding default constants related to network connections, such as connection
 * settings (timeouts, keep-alive, back-offs) and so on.
 *
 * @author Marco Salis
 * @since 1.0
 */
@Beta
@Immutable
public class NetworkConstants {

    /**
     * Default connection keep-alive (in milliseconds)
     */
    public static final int DEFAULT_KEEP_ALIVE = 20 * 1000;

    /**
     * Default connection socket timeout (in milliseconds)
     */
    public static final int DEFAULT_CONN_TIMEOUT = 20 * 1000;

    /**
     * Default socket read timeout (in milliseconds)
     */
    public static final int DEFAULT_READ_TIMEOUT = 30 * 1000;

    /**
     * Number of retries allowed for a failed request that allows retry.
     */
    public static final int REQUEST_RETRIES = 4;

    /**
     * Default, immutable {@link BackOff} to be used for HTTP requests
     */
    public static final BackOff DEFAULT_BACKOFF = new DefaultLinearBackOff();

    /**
     * Default, immutable {@link BackOffRequired} to be used for HTTP requests
     */
    public static final BackOffRequired DEFAULT_BACKOFF_REQUIRED = new DefaultBackOffRequired();

    /**
     * Default, immutable {@link HttpUnsuccessfulResponseHandler} to be used for HTTP requests.
     */
    public static final HttpUnsuccessfulResponseHandler DEFAULT_RESPONSE_HANDLER = new DefaultBackOffUnsuccessfulResponseHandler();

    /**
     * Default, immutable {@link HttpIOExceptionHandler} to be used for HTTP requests.
     *
     * TODO: create a {@link HttpBackOffIOExceptionHandler} factory to be able to use an exponential
     * back off policy.
     */
    public static final HttpIOExceptionHandler IO_EXCEPTION_HANDLER = new DefaultHttpIOExceptionHandler();

    /**
     * Attempts to retrieve a "Keep-Alive" header from the passed {@link HttpResponse}.
     *
     * @return The keep alive time or -1 if not found
     */
    public static long getKeepAliveHeader(@NonNull HttpResponse response) {
        HeaderElementIterator it = new BasicHeaderElementIterator(
                response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase("timeout")) {
                try {
                    return Long.parseLong(value) * 1000;
                } catch (NumberFormatException ignore) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private NetworkConstants() {
        // hidden constructor, no instantiation needed
    }

}