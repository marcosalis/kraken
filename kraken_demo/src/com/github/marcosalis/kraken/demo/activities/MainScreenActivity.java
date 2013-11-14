/*
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
package com.github.marcosalis.kraken.demo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.marcosalis.kraken.cache.DiskCache.DiskCacheClearMode;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCacheBase;
import com.github.marcosalis.kraken.cache.managers.BaseCachesManager;
import com.github.marcosalis.kraken.demo.KrakenDemoApplication;
import com.github.marcosalis.kraken.demo.KrakenDemoApplication.CacheId;
import com.github.marcosalis.kraken.demo.R;

/**
 * Launcher activity that shows the possible demo options.
 * 
 * @since 1.0
 * @author Marco Salis
 */
public class MainScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final BaseCachesManager<CacheId> caches = KrakenDemoApplication.get().getCachesManager();
		switch (item.getItemId()) {
		case R.id.action_clear_caches:
			caches.clearAllCaches();
			BitmapCacheBase.clearBitmapExecutors(); // clear executors stats
			Toast.makeText(this, "All caches cleared!", Toast.LENGTH_LONG).show();
			break;
		case R.id.action_clear_memory_caches:
			caches.clearMemoryCaches();
			Toast.makeText(this, "Memory caches cleared!", Toast.LENGTH_LONG).show();
			break;
		case R.id.action_clear_disk_caches:
			caches.clearDiskCaches(DiskCacheClearMode.ALL);
			Toast.makeText(this, "Disk caches cleared!", Toast.LENGTH_LONG).show();
			break;
		}
		return true;
	}

}