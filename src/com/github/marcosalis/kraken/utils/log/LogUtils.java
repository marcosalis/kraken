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
package com.github.marcosalis.kraken.utils.log;

import android.util.Log;

import com.github.marcosalis.kraken.DroidConfig;
import com.google.common.annotations.Beta;

/**
 * Helper class containing static utility methods for logging.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class LogUtils {

	@SuppressWarnings("unused")
	private static final String TAG = LogUtils.class.getSimpleName();

	private LogUtils() {
		// hidden constructor, no instantiation needed
	}

	/**
	 * Logs a message in LogCat, only if library debugging is active.
	 * 
	 * @param logLevel
	 *            The log level to use: must be one of the level constants
	 *            provided by the {@link Log} class.
	 * @param logTag
	 * @param logMessage
	 */
	public static void log(int logLevel, String logTag, String logMessage) {
		if (DroidConfig.DEBUG) {
			switch (logLevel) {
			case Log.ASSERT:
			case Log.VERBOSE:
				Log.v(logTag, logMessage);
				break;
			case Log.DEBUG:
				Log.d(logTag, logMessage);
				break;
			case Log.INFO:
				Log.i(logTag, logMessage);
				break;
			case Log.WARN:
				Log.w(logTag, logMessage);
				break;
			case Log.ERROR:
				Log.e(logTag, logMessage);
				break;
			}
		}
	}

	/**
	 * Prints on LogCat a stack track of an exception, only if library debugging
	 * is active.
	 * 
	 * @param t
	 *            The {@link Throwable} to get the stack trace from
	 */
	public static void logException(Throwable t) {
		if (DroidConfig.DEBUG) {
			if (t != null) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Prints on LogCat a log warning message with the exception stack, only if
	 * library debugging is active.
	 * 
	 * @param logTag
	 * @param logMessage
	 * @param t
	 *            The {@link Throwable} to get the stack trace from
	 */
	public static void logException(String logTag, String logMessage, Throwable t) {
		if (DroidConfig.DEBUG) {
			if (t != null) {
				Log.w(logTag, logMessage, t);
			}
		}
	}

	/**
	 * Prints on LogCat a log warning message with the exception's message
	 * string, only if library debugging is active.
	 * 
	 * @param t
	 *            The {@link Throwable} to get the message from
	 */
	public static void logExceptionMessage(String logTag, String logMessage, Throwable t) {
		if (DroidConfig.DEBUG) {
			if (t != null) {
				Log.w(logTag,
						logMessage + "\n" + t.getClass().getSimpleName() + " message: "
								+ t.getMessage());
			}
		}
	}

}