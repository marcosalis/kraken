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
package com.github.marcosalis.kraken.utils.android;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.common.annotations.Beta;

/**
 * Simple extension of {@link WebViewClient} that opens any link using the
 * Android {@link Intent#ACTION_VIEW} action.
 * 
 * The superclass opens by default any link inside the {@link WebView}, which is
 * usually not the intended behavior for the user.
 * 
 * @since 1.0
 * @author Marco Salis
 */
@Beta
public class LinkedWebViewClient extends WebViewClient {

	@Override
	@OverridingMethodsMustInvokeSuper
	public boolean shouldOverrideUrlLoading(@Nonnull WebView view, @CheckForNull String url) {
		if (url != null) {
			try {
				final Intent intent;
				// handle manually phone numbers and email addresses
				if (url.startsWith("mailto:") || url.startsWith("tel:")) {
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				} else {
					// open other URLs in the device browser (same intent)
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				}
				view.getContext().startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
				LogUtils.logException(e);
			}
		}
		return super.shouldOverrideUrlLoading(view, url);
	}

}