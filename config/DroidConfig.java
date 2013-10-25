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
package com.github.marcosalis.kraken;

import android.os.StrictMode;

/**
 * Kraken library configuration global constants.
 * 
 * <b>Do NOT modify this file manually: all changes will be erased</b>
 * 
 * Run one of the following Ant scripts instead:
 * 
 * <code>ant kraken-debug-config</code> for debug configuration
 * <code>ant kraken-release-config</code> for release configuration
 * 
 * @since 1.0
 * @author Marco Salis
 */
public class DroidConfig {

	// AUTOMATICALLY GENERATED
	
	/**
	 * Global flag to enable library's LogCat logging messages and other debug
	 * configuration settings.
	 * 
	 * <p>
	 * <b>Warning:</b> STRICT_MODE <u>must always</u> be disabled when releasing
	 * as it can affect performances and show unwanted data/feedback to the
	 * users!
	 * </p>
	 */
	public static final boolean DEBUG = @droid.debug@;
	
	/**
	 * Global flag to enable the strictest {@link StrictMode} on the
	 * application, using invasive penalties to notify the developer. Note that
	 * when the {@link #DEBUG} mode is active, some logging strict mode features
	 * are already enabled.
	 * 
	 * <p>
	 * <b>Warning:</b> STRICT_MODE <u>must always</u> be disabled when releasing
	 * as it can affect performances and show unwanted data/feedback to the
	 * users!
	 * </p>
	 */
	public static final boolean STRICT_MODE = @droid.strict_mode@;

}