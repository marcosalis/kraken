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
package com.github.marcosalis.kraken.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.api.client.extensions.android.AndroidUtils;
import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;

/**
 * Helper class containing general static utility methods and shortcuts to
 * Android core functionalities.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
@Immutable
public class DroidUtils {

	public static final String SMS_BODY_EXTRA = "sms_body";

	/* Time utils: conversions from seconds to other time units */
	public static final int MINUTE = 60;
	public static final int HOUR = 60 * MINUTE;
	public static final int DAY = 24 * HOUR;
	public static final int WEEK = 7 * DAY;
	public static final int MONTH = 30 * DAY;
	public static final int YEAR = 12 * MONTH;

	/**
	 * Statically initialized constant that represents the number of total CPU
	 * cores in the device. This number can be higher than the one returned by
	 * {@link Runtime.getRuntime().availableProcessors()}, as the latter returns
	 * the number of active processors that can be used by the current virtual
	 * machine (and it's used as a fallback method when it's impossible to
	 * access the <i>/sys/devices/system/cpu</i> folder, where this information
	 * is retrieved).
	 * 
	 * See {@link http
	 * ://makingmoneywithandroid.com/forum/showthread.php?tid=298&pid=1663} for
	 * more information on the method used.
	 */
	public static final int CPU_CORES;

	static {
		CPU_CORES = getDeviceCpuCores();
	}

	private DroidUtils() {
		// hidden constructor, no instantiation needed
	}

	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to access the filesystem at "/sys/devices/system/cpu"
	 * 
	 * @return The number of cores, or the value of {@link
	 *         Runtime.getRuntime().availableProcessors()} if failed to get
	 *         result
	 */
	private static int getDeviceCpuCores() {
		// private class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}
		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			final int cpuCount = files.length;
			if (cpuCount > 0) {
				return cpuCount;
			}
		} catch (Exception e) {
			// falls back to Runtime.getRuntime().availableProcessors()
		}
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Returns a recommended size for thread pools that execute CPU-bound
	 * operations. The current implementation just returns the value of the
	 * runtime method {@code Runtime.getRuntime().availableProcessors()},
	 * avoiding to add the common <i>+ 1</i> to avoid putting too much overhead
	 * on the Android Dalvik threads scheduling system.
	 */
	public static int getCpuBoundPoolSize() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Returns a recommended size for thread pools that execute IO-bound
	 * operations (mostly network requests, rather than disk accesses).
	 * 
	 * You are guaranteed that this method will always return more than 1 and at
	 * least 1 more than the {@link #getCpuBoundPoolSize()}.
	 * 
	 * This trivial implementation just doubles the number returned by
	 * {@link #getCpuBoundPoolSize()}.
	 */
	public static int getIOBoundPoolSize() {
		return (getCpuBoundPoolSize() * 2);
	}

	/**
	 * Returns the upper memory usage limit in bytes for the current application
	 * retrieved from {@link ActivityManager#getMemoryClass()}. Using a
	 * percentage of this value is a reliable way to limit memory caches.
	 * 
	 * @param context
	 *            The {@link Context} to retrieve the {@link ActivityManager}
	 * @return The application memory class in bytes
	 */
	public static int getApplicationMemoryClass(@Nonnull Context context) {
		final ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		return manager.getMemoryClass() * 1024 * 1024; // to bytes
	}

	/**
	 * Retrieves the {@link DisplayMetrics} for the device's default display.
	 * 
	 * @param context
	 *            The {@link Context} to retrieve the {@link WindowManager}
	 * @return The retrieved {@link DisplayMetrics}
	 */
	@Nonnull
	public static DisplayMetrics getDefaultDisplayMetrics(@Nonnull Context context) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		final DisplayMetrics metrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}

	/**
	 * Retrieves the screen size category for the device's default display from
	 * the {@link Configuration} values.
	 * 
	 * @param context
	 *            The {@link Context} to use to retrieve the
	 *            {@link WindowManager}
	 * @return The retrieved screen size configuration, can be one of
	 *         {@link Configuration#SCREENLAYOUT_SIZE_SMALL},
	 *         {@link Configuration#SCREENLAYOUT_SIZE_NORMAL},
	 *         {@link Configuration#SCREENLAYOUT_SIZE_LARGE},
	 *         {@link Configuration#SCREENLAYOUT_SIZE_XLARGE} (<b>use the int 4
	 *         for this configuration, constant available only from API 11</b>)
	 */
	public static int getScreenSize(@Nonnull Context context) {
		final Configuration conf = context.getResources().getConfiguration();
		return conf.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
	}

	/**
	 * Returns whether the SDK version is the given level or higher.
	 * 
	 * @see android.os.Build.VERSION_CODES
	 * @see {@link AndroidUtils#isMinimumSdkLevel(int)}
	 */
	public static boolean isMinimumSdkLevel(int minimumSdkLevel) {
		return Build.VERSION.SDK_INT >= minimumSdkLevel;
	}

	/**
	 * Generates an unique, stable UID that identifies the device where the user
	 * is currently logged.
	 * 
	 * Requires the <code>READ_PHONE_STATE</code> permission in the Manifest.
	 * 
	 * @param context
	 *            A {@link Context} to retrieve data from the
	 *            {@link TelephonyManager} and {@link ContentResolver}
	 * @param appId
	 *            An unique and possibly secret app ID to "secure" the generated
	 *            value
	 * @return An alphanumeric (+ hyphens) {@link UUID} string
	 */
	@Nonnull
	public static String getDeviceUniqueIdentificator(@Nonnull Context context,
			@Nonnull String appId) {
		String telephonyId = null;
		String androidId = null;

		if (hasTelephony(context)) {
			final TelephonyManager telephony = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			telephonyId = telephony.getDeviceId();
		}
		if (telephonyId == null) {
			telephonyId = "";
		}

		if (Build.VERSION.SDK_INT >= 9) { // Build.VERSION_CODES.GINGERBREAD
			androidId = getAndroidBuildSerial();
		} else {
			androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID);
		}
		// if ("9774d56d682e549c".equals(androidId)) { // broken Android ID
		// FIXME: fallback
		// }
		if (androidId == null) {
			androidId = "";
		}

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long) telephonyId.hashCode() << 32)
				| appId.hashCode());

		return deviceUuid.toString();
	}

	/**
	 * Returns the {@link #android.os.Build.SERIAL} string.<br>
	 * Not guaranteed to be unique and not null, only available from API 9.
	 */
	@TargetApi(9)
	private static String getAndroidBuildSerial() {
		return android.os.Build.SERIAL;
	}

	/**
	 * Checks if the device has the {@link PackageManager#FEATURE_TELEPHONY}.
	 * 
	 * @param context
	 * @return true if the telephony is available, false otherwise
	 */
	public static boolean hasTelephony(@Nonnull Context context) {
		final PackageManager manager = context.getPackageManager();
		return manager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	}

	/**
	 * Sends an SMS message to the given number with the passed text.
	 * 
	 * FIXME: SMS text is not displayed in Motorola devices
	 * 
	 * Note: if the passed {@link Context} is not an activity, the flag
	 * {@link Intent#FLAG_ACTIVITY_NEW_TASK} will automatically be set to avoid
	 * an Android runtime exception.
	 * 
	 * @param context
	 *            The {@link Context} to start the intent with
	 * @param number
	 *            The phone number to send the SMS to
	 * @param text
	 *            The preset text for the SMS
	 * @return true if the SMS composer has been opened, false if telephony was
	 *         not available on the device or the phone number wasn't in the
	 *         accepted format by
	 *         {@link PhoneNumberUtils#isWellFormedSmsAddress(String)}. In the
	 *         latter case, the SMS composer is shown anyway with no recipients.
	 */
	public static boolean sendSms(@Nonnull Context context, @CheckForNull String number,
			@Nullable String text) {
		boolean success = true;
		if (hasTelephony(context)) {
			if (number == null || !PhoneNumberUtils.isWellFormedSmsAddress(number)) {
				number = ""; // show empty recipient
				success = false;
			}
			// we can send the sms
			Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + number));
			smsIntent.putExtra(SMS_BODY_EXTRA, text);
			// Intent.EXTRA_TEXT added only as a fallback
			smsIntent.putExtra(Intent.EXTRA_TEXT, text);

			if (!(context instanceof Activity)) {
				smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(smsIntent);
			return true;
		} else {
			success = false;
		}
		return success;
	}

	/**
	 * FIXME: SMS text is not displayed in Motorola devices
	 * 
	 * As {@link #sendSms(Context, String, String)} but allows to use multiple
	 * recipients for the SMS message.
	 * 
	 * @param context
	 *            The {@link Context} to start the intent with
	 * @param numbers
	 *            An array of recipients phone numbers
	 * @param text
	 *            The preset text for the SMS
	 * @return true if the SMS composer has been opened, false otherwise (for
	 *         example, because the device has no telephony)
	 */
	public static boolean sendSmsToMany(@Nonnull Context context, @Nonnull String[] numbers,
			@Nullable String text) {
		if (numbers.length == 1) { // one recipient only, call sendSms()
			return sendSms(context, numbers[0], text);
		} // more than one recipient
		if (hasTelephony(context)) {
			final List<String> filteredNumbers = new ArrayList<String>(numbers.length);
			for (String number : numbers) {
				if (number != null && PhoneNumberUtils.isWellFormedSmsAddress(number)) {
					filteredNumbers.add(number);
				}
			}
			// we can send the SMS
			Joiner joiner = Joiner.on(getMultipleSmsJoinOn()).skipNulls();
			Uri smsUri = Uri.parse("sms:" + joiner.join(filteredNumbers));
			Intent smsIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
			smsIntent.putExtra(SMS_BODY_EXTRA, text);
			// Intent.EXTRA_TEXT added only as a fallback
			smsIntent.putExtra(Intent.EXTRA_TEXT, text);

			if (!(context instanceof Activity)) {
				smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(smsIntent);
			return true;
		}
		return false;
	}

	@Nonnull
	private static String getMultipleSmsJoinOn() {
		final String manufacturer = android.os.Build.MANUFACTURER;
		if (manufacturer.toLowerCase(Locale.US).contains("samsung")) {
			// Samsung requires comma-separated numbers
			return ",";
		} else {
			return ";";
		}
	}

	/**
	 * To be used with {@link android.content.Intent.ACTION_SENDTO} to send
	 * email, either in plain text or HTML.
	 * 
	 * @param email
	 *            The email address to send the message to
	 * @param subject
	 *            The email subject
	 * @param body
	 *            The email body
	 * @return The {@link Uri} for the intent to send the email
	 */
	@Nonnull
	public static Uri buildEmailUri(String email, String subject, CharSequence body) {
		StringBuilder builder = new StringBuilder();
		builder.append("mailto:").append(email);
		builder.append("?subject=").append(subject);
		builder.append("&body=").append(body);
		String uriText = builder.toString().replace(" ", "%20");
		return Uri.parse(uriText);
	}

	/**
	 * Create a chooser intent to open the email composer with the specified
	 * single email recipient, subject and message.
	 * 
	 * @param context
	 *            The {@link Context} to start the chooser intent with
	 * @param chooserMessage
	 *            The (optional) message to display in the intent chooser
	 * @param recipient
	 *            The email address to send the message to
	 * @param subject
	 *            The subject of the message
	 * @param message
	 *            The email message
	 */
	public static void sendEmail(@Nonnull Context context, @Nullable String chooserMessage,
			@Nullable String recipient, @Nullable String subject, @Nullable String message) {
		sendEmail(context, chooserMessage, new String[] { recipient }, subject, message);
	}

	/**
	 * Create a chooser intent to open the email composer with the specified
	 * email recipients, subject and message.
	 * 
	 * Note: if the passed {@link Context} is not an activity, the flag
	 * {@link Intent#FLAG_ACTIVITY_NEW_TASK} will automatically be set to avoid
	 * an Android runtime exception.
	 * 
	 * @param context
	 *            The {@link Context} to start the chooser intent with
	 * @param chooserMessage
	 *            The (optional) message to display in the intent chooser
	 * @param recipients
	 *            The email addresses array to send the message to
	 * @param subject
	 *            The subject of the message
	 * @param message
	 *            The email message
	 */
	public static void sendEmail(@Nonnull Context context, @Nullable String chooserMessage,
			@Nonnull String[] recipients, @Nullable String subject, @Nullable String message) {
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
		emailIntent.setType("plain/text");
		final Intent intent = Intent.createChooser(emailIntent, chooserMessage);
		if (!(context instanceof Activity)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}

	/**
	 * Returns an {@link Intent} with action {@link Intent#ACTION_VIEW} to open
	 * the Google Play page for the passed package name.
	 * 
	 * @param packageName
	 *            A full, valid Google Play application package name
	 */
	@Nonnull
	public static Intent getApplicationMarketPage(@Nonnull String packageName) {
		return getViewUrlIntent("market://details?id=" + packageName);
	}

	/**
	 * Creates an {@link Intent} to open the passed URI with the default
	 * application that handles {@link Intent#ACTION_VIEW} for that content.
	 * 
	 * @param uri
	 *            The URI string (must be non null)
	 * @return The created intent
	 */
	@Nonnull
	public static Intent getViewUrlIntent(@Nonnull String uri) {
		return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
	}

	/**
	 * Checks whether an action has a matching Intent in the current device
	 * 
	 * @param context
	 *            A Context
	 * @param action
	 *            The action to check (see {@link Intent} docs
	 * @return true if an Intent is available, false otherwise
	 */
	public static boolean isIntentAvailable(@Nonnull Context context, @Nonnull String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		final List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * Returns the {@link InputStream} of the given path file using the passed
	 * context to retrieve the {@link AssetManager}.
	 * 
	 * @param context
	 *            The {@link Context} to use to retrieve the assets
	 * @param path
	 *            The path, relative to the assets root, of the file to retrieve
	 * @return The {@link InputStream} of the requested file, or null if an
	 *         error occurred
	 * @throws IOException
	 */
	@CheckForNull
	public static InputStream getAsset(@Nonnull Context context, @Nonnull String path)
			throws IOException {
		return context.getAssets().open(path);
	}

}