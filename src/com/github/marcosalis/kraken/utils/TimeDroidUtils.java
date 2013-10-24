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
package com.github.marcosalis.kraken.utils;

import com.google.common.annotations.Beta;

/**
 * Helper class containing general static utility methods to handle time,
 * relative time spans and time stamps.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class TimeDroidUtils {

	/* conversions from seconds to other time units */
	public static final int MINUTE = 60;
	public static final int HOUR = 60 * MINUTE;
	public static final int DAY = 24 * HOUR;
	public static final int WEEK = 7 * DAY;
	public static final int MONTH = 30 * DAY;
	public static final int YEAR = 12 * MONTH;

	private TimeDroidUtils() {
		// hidden constructor, no instantiation needed
	}

}