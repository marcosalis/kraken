/*
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

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.test.ApplicationTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;

import com.github.marcosalis.kraken.utils.DroidUtils;
import com.github.marcosalis.kraken.utils.android.DroidApplication.DroidStrictMode;

/**
 * ApplicationTestCase for the {@link DroidApplication} class.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@MediumTest
public class DroidApplicationTest extends ApplicationTestCase<DroidApplication> {

	private DroidApplication mApplication;

	public DroidApplicationTest() {
		super(DroidApplication.class);
	}

	protected void setUp() throws Exception {
		super.setUp();

		createApplication();
		mApplication = getApplication();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsDebuggable() {
		assertTrue("Debuggable application with wrong flag", mApplication.isDebuggable());
	}

	public void testGetAppVersion() throws NameNotFoundException {
		final PackageInfo packageInfo = mApplication.getPackageManager().getPackageInfo(
				mApplication.getPackageName(), 0);
		assertEquals(packageInfo.versionName, mApplication.getAppVersion());
	}

	public void testGetAppVersionCode() throws NameNotFoundException {
		final PackageInfo packageInfo = mApplication.getPackageManager().getPackageInfo(
				mApplication.getPackageName(), 0);
		assertEquals(packageInfo.versionCode, mApplication.getAppVersionCode());
	}

	public void testGetMemoryClass() {
		assertTrue(mApplication.getMemoryClass() > 0);
	}

	@UiThreadTest
	public void testIsApplicationOnForeground() {
		assertFalse(mApplication.isApplicationOnForeground());

		mApplication.incrementVisibleActivitiesStack();
		assertTrue(mApplication.isApplicationOnForeground());
		mApplication.decrementVisibleActivitiesStack();

		assertFalse(mApplication.isApplicationOnForeground());
	}

	@UiThreadTest
	public void testGetDisplayMetrics() {
		assertNotNull(mApplication.getDisplayMetrics());
	}

	@UiThreadTest
	@SuppressLint("NewApi")
	public void testSetStrictMode() {
		if (Build.VERSION.SDK_INT >= 9) {
			// we compare string rep as the policies mask is not accessible
			mApplication.setStrictMode(DroidStrictMode.NONE);
			assertEquals(StrictMode.getThreadPolicy().toString(), ThreadPolicy.LAX.toString());
			assertEquals(StrictMode.getVmPolicy().toString(), VmPolicy.LAX.toString());

			mApplication.setStrictMode(DroidStrictMode.DEFAULT);
			assertNotSame(StrictMode.getThreadPolicy(), ThreadPolicy.LAX);
			assertNotSame(StrictMode.getVmPolicy(), VmPolicy.LAX);

			mApplication.setStrictMode(DroidStrictMode.NONE);
			mApplication.setStrictMode(DroidStrictMode.DEBUG);
			assertEquals(StrictMode.getThreadPolicy().toString(), mApplication
					.getDebugCustomThreadPolicy().toString());
			assertEquals(StrictMode.getVmPolicy().toString(), mApplication.getDebugCustomVmPolicy()
					.toString());

			mApplication.setStrictMode(DroidStrictMode.NONE);
			mApplication.setStrictMode(DroidStrictMode.STRICT);
			assertEquals(StrictMode.getThreadPolicy().toString(), mApplication
					.getStrictCustomThreadPolicy().toString());
			assertEquals(StrictMode.getVmPolicy().toString(), mApplication
					.getStrictCustomVmPolicy().toString());
		}
	}

	@UiThreadTest
	public void testGetDebugCustomThreadPolicy() {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			assertNotNull(mApplication.getDebugCustomThreadPolicy());
		}
	}

	@UiThreadTest
	public void testGetStrictCustomThreadPolicy() {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			assertNotNull(mApplication.getStrictCustomThreadPolicy());
		}
	}

	@UiThreadTest
	public void testGetDebugCustomVmPolicy() {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			assertNotNull(mApplication.getDebugCustomVmPolicy());
		}
	}

	@UiThreadTest
	public void testGetStrictCustomVmPolicy() {
		if (DroidUtils.isMinimumSdkLevel(9)) {
			assertNotNull(mApplication.getStrictCustomVmPolicy());
		}
	}

}