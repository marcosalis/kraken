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
package com.github.marcosalis.kraken.demo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.github.marcosalis.kraken.cache.SecondLevelCache.ClearMode;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCacheBase;
import com.github.marcosalis.kraken.cache.managers.BaseCachesManager;
import com.github.marcosalis.kraken.demo.KrakenDemoApplication;
import com.github.marcosalis.kraken.demo.KrakenDemoApplication.CacheId;
import com.github.marcosalis.kraken.demo.R;
import com.github.marcosalis.kraken.demo.fragments.PhotosListFragment;
import com.github.marcosalis.kraken.demo.fragments.PhotosListFragment.PhotosSize;

/**
 * Launcher activity that shows the possible demo options.
 *
 * TODO: add RecyclerView demo
 *
 * @author Marco Salis
 * @since 1.0
 */
public class MainScreenActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.list_view_small).setOnClickListener(this);
        findViewById(R.id.list_view_fullscreen).setOnClickListener(this);
        findViewById(R.id.grid_view).setOnClickListener(this);
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
                BitmapCacheBase.clearBitmapExecutors(); // clear executors tasks
                Toast.makeText(this, "All caches cleared!", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_clear_memory_caches:
                caches.clearMemoryCaches();
                Toast.makeText(this, "Memory caches cleared!", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_clear_disk_caches:
                caches.clearDiskCaches(ClearMode.ALL);
                Toast.makeText(this, "Disk caches cleared!", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.list_view_small:
                startPhotosListViewActivity(PhotosSize.SMALL);
                break;
            case R.id.list_view_fullscreen:
                startPhotosListViewActivity(PhotosSize.FULL_SCREEN);
                break;
            case R.id.grid_view:
                Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startPhotosListViewActivity(PhotosSize size) {
        final Intent intent = new Intent(this, PhotosListViewActivity.class);
        intent.putExtra(PhotosListFragment.ARGS_PHOTOS_SIZE, size);
        startActivity(intent);
    }

}