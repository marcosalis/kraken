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
package com.github.marcosalis.kraken.utils.android;

import android.support.annotation.NonNull;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.github.marcosalis.kraken.BuildConfig;
import com.github.marcosalis.kraken.DroidConfig;
import com.github.marcosalis.kraken.utils.DroidUtils;
import com.google.common.annotations.Beta;

/**
 * Subclass of {@link Application} with some utility methods.
 * 
 * In order to use this class you must create a subclass of it and declare it in
 * the manifest like this:
 * 
 * <code><pre>
 *     &#60;application
 *         android:name="com.mypackage.MyApplication"
 *     ... 
 * </pre></code>
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class DroidApplication extends Application {

	/**
	 * Flag indicating whether the application package is currently built with
	 * the "debuggable" attribute set to true.
	 * 
	 * Do NOT rely on this yet, as it's still incorrect due to an issue in ADT
	 * (v22).
	 */
	static final boolean DEBUGGABLE = BuildConfig.DEBUG;

	private static final String TAG = DroidApplication.class.getSimpleName();

	private volatile String mAppVersion;
	private volatile int mAppVersionCode;
	private volatile int mMemoryClass;
	private volatile boolean mIsDebuggable;

	private volatile int mActivityStack;

	private volatile Display mDefaultDisplay;

	@Override
	public void onCreate() {
		super.onCreate();

		// collect "debuggable" attribute value from application info
		mIsDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		if (DroidConfig.DEBUG) {
			Log.e(TAG, "The application is running in DEBUG mode!");
			Log.i(TAG, "Debuggable flag in Manifest: " + mIsDebuggable);
		}

		// enable strict modes depending on current app configuration
		if (Build.VERSION.SDK_INT >= 9) {
			if (DroidConfig.STRICT_MODE) {
				setStrictMode(DroidStrictMode.STRICT);
			} else if (DroidConfig.DEBUG) {
				setStrictMode(DroidStrictMode.DEBUG);
			} else {
				setStrictMode(DroidStrictMode.NONE);
			}
		}

		try { // set application version
			final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mAppVersion = packageInfo.versionName;
			mAppVersionCode = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// this should never happen
		}

		// max available application heap size
		int memoryClass = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
		mMemoryClass = memoryClass * 1024 * 1024; // convert to bytes
		if (DroidConfig.DEBUG) {
			Log.i(TAG, "App available memory: " + mMemoryClass + " bytes");
			Log.i(TAG, "Runtime max memory: " + Runtime.getRuntime().maxMemory() + " bytes");
			Log.i(TAG, "App total cores: " + DroidUtils.CPU_CORES);
			Log.i(TAG, "App available cores: " + Runtime.getRuntime().availableProcessors());
		}

		// loads the default display
		mDefaultDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

		LogUtils.log(Log.ERROR, TAG, "onLowMemory()");
	}

	@Override
	@TargetApi(14)
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);

		if (DroidConfig.DEBUG) {
			Log.w(TAG, "onTrimMemory() - level: " + level);
		}
	}

	/**
	 * Returns whether the currently running application was built with the
	 * Manifest's "debuggable" attribute set to true (which means it has been
	 * exported and signed with a certificate).
	 * 
	 * Note that this is different from the {@link DroidConfig#DEBUG} flag
	 */
	public boolean isDebuggable() {
		return mIsDebuggable;
	}

	/**
	 * Returns the AndroidManifest application version from the
	 * {@link PackageInfo}
	 */
	public String getAppVersion() {
		return mAppVersion;
	}

	/**
	 * Returns the AndroidManifest application version code from the
	 * {@link PackageInfo}
	 */
	public int getAppVersionCode() {
		return mAppVersionCode;
	}

	/**
	 * Returns the maximum available application heap size, in bytes. This is
	 * useful to set the max size of memory caches within the application.
	 */
	public final int getMemoryClass() {
		return mMemoryClass;
	}

	/*
	 * Getters and setters for the application visible (between onResume() and
	 * onPause()) activity stack, which is used (with some approximation) to
	 * know if the application is effectively on the foreground (for displaying
	 * notifications and other maintenance stuff).
	 * 
	 * Note that there is a small window of time when the stack count can be 0
	 * even if the application is in the foreground. This happens when calling
	 * an activity from the top activity: its onPause() is called a short while
	 * before the new activity's onResume().
	 * 
	 * Call incrementActivityStack() and decrementActivityStack() from within
	 * every base activity class which application activities inherit from.
	 */

	/**
	 * Checks whether the application is currently on the device's foreground.
	 * 
	 * @return true if the app is on foreground, false otherwise
	 */
	public boolean isApplicationOnForeground() {
		return mActivityStack > 0;
	}

	/**
	 * Increments the application activity stack by one. Call this in the
	 * activity {@link Activity#onResume()}
	 * 
	 * @return The current (approximated) activity stack count
	 */
	public int incrementVisibleActivitiesStack() {
		return ++mActivityStack;
	}

	/**
	 * Decrements the application activity stack by one. Call this in the
	 * activity {@link Activity#onPause()}
	 * 
	 * @return The current (approximated) activity stack count
	 */
	public int decrementVisibleActivitiesStack() {
		return --mActivityStack;
	}

	/**
	 * Gets the current display's {@link DisplayMetrics}
	 */
	@NonNull
	public final DisplayMetrics getDisplayMetrics() {
		final DisplayMetrics metrics = new DisplayMetrics();
		mDefaultDisplay.getMetrics(metrics);
		return metrics;
	}

	/* StrictMode management constants and utility methods - API > 8 */

	/**
	 * Enumerate the currently supported {@link StrictMode}s that can be enabled
	 * from within this class.
	 */
	public enum DroidStrictMode {
		/**
		 * No strict mode enabled (Android default). Sets policies to
		 * {@link ThreadPolicy#LAX} and {@link VmPolicy#LAX}.
		 */
		NONE,
		/**
		 * Suggested strict mode returned by {@link StrictMode#enableDefaults()}
		 */
		DEFAULT,
		/**
		 * Debug default mode used when {@link DroidConfig#DEBUG} is enabled
		 */
		DEBUG,
		/**
		 * Stricter mode used when {@link DroidConfig#STRICT_MODE} is enabled
		 */
		STRICT;
	}

	/**
	 * Sets the {@link StrictMode} reporting functionalities by specifying the
	 * level of penalties and logging. This method is useful to temporarily
	 * enable certain features for a specific activity or other components.
	 * 
	 * To know more about {@link StrictMode}:
	 * 
	 * <pre>
	 * {@link http://android-developers.blogspot.co.uk/2010/12/new-gingerbread-api-strictmode.html}
	 * </pre>
	 * 
	 * @param mode
	 *            The {@link DroidStrictMode} to enable
	 */
	@TargetApi(9)
	public final void setStrictMode(@NonNull DroidStrictMode mode) {
		switch (mode) {
		case DEBUG:
			StrictMode.setThreadPolicy(getDebugCustomThreadPolicy());
			StrictMode.setVmPolicy(getDebugCustomVmPolicy());
			break;
		case STRICT:
			StrictMode.setThreadPolicy(getStrictCustomThreadPolicy());
			StrictMode.setVmPolicy(getStrictCustomVmPolicy());
			break;
		case DEFAULT:
			StrictMode.enableDefaults();
			break;
		case NONE:
			StrictMode.setThreadPolicy(ThreadPolicy.LAX);
			StrictMode.setVmPolicy(VmPolicy.LAX);
		}
	}

	/**
	 * Returns the default {@link StrictMode.ThreadPolicy} to be used when
	 * {@link DroidConfig#DEBUG} is active. This policy only logs violations to
	 * the LogCat console.
	 * 
	 * Override this method to provide a custom policy for
	 * {@link DroidStrictMode#DEBUG}.
	 * 
	 * <b>Always call this from the UI thread.</b>
	 */
	@NonNull
	@TargetApi(9)
	protected StrictMode.ThreadPolicy getDebugCustomThreadPolicy() {
		return new ThreadPolicy.Builder() //
				// .detectDiskReads() // too verbose
				// .detectDiskWrites() // too verbose
				.detectNetwork() //
				.penaltyLog() //
				.build();
	}

	/**
	 * Returns the stricter {@link StrictMode.ThreadPolicy} to be used when
	 * {@link DroidConfig#STRICT_MODE} is active. This policy logs violations to
	 * the LogCat console and flashing the screen (API >= 11).
	 * 
	 * Override this method to provide a custom policy for
	 * {@link DroidStrictMode#STRICT}.
	 * 
	 * <b>Always call this from the UI thread.</b>
	 */
	@NonNull
	@TargetApi(9)
	protected StrictMode.ThreadPolicy getStrictCustomThreadPolicy() {
		final Builder builder = new ThreadPolicy.Builder().detectAll().penaltyLog();
		if (Build.VERSION.SDK_INT >= 11) { // Honeycomb or higher
			setPenaltyFlashScreen(builder);
		}
		return builder.build();
	}

	/**
	 * Returns the default {@link StrictMode.VmPolicy} to be used when
	 * {@link DroidConfig#DEBUG} is active. This policy only logs violations to
	 * the LogCat console.
	 * 
	 * Override this method to provide a custom policy for
	 * {@link DroidStrictMode#DEBUG}.
	 */
	@NonNull
	@TargetApi(9)
	protected StrictMode.VmPolicy getDebugCustomVmPolicy() {
		final VmPolicy.Builder builder = new VmPolicy.Builder();
		if (Build.VERSION.SDK_INT >= 11) { // Honeycomb or higher
			setHoneycombVmPolicy(builder);
		}
		if (Build.VERSION.SDK_INT >= 16) { // ICS or higher
			setICSVmPolicy(builder);
		}
		return builder.detectLeakedSqlLiteObjects().penaltyLog().build();
	}

	/**
	 * Returns the stricter {@link StrictMode.VmPolicy} to be used when
	 * {@link DroidConfig#STRICT_MODE} is active. This policy only logs
	 * violations to the LogCat console.
	 * 
	 * Override this method to provide a custom policy for
	 * {@link DroidStrictMode#STRICT}.
	 */
	@NonNull
	@TargetApi(9)
	protected StrictMode.VmPolicy getStrictCustomVmPolicy() {
		return new VmPolicy.Builder().detectAll().penaltyLog().build();
	}

	@TargetApi(11)
	private static final void setHoneycombVmPolicy(@NonNull VmPolicy.Builder builder) {
		builder //
		// .detectActivityLeaks() // buggy
		.detectLeakedClosableObjects();
	}

	@TargetApi(16)
	private static final void setICSVmPolicy(@NonNull VmPolicy.Builder builder) {
		builder.detectLeakedRegistrationObjects();
	}

	@TargetApi(11)
	private static final void setPenaltyFlashScreen(@NonNull ThreadPolicy.Builder builder) {
		builder.penaltyFlashScreen();
	}

}