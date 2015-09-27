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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.github.marcosalis.kraken.demo.R;
import com.github.marcosalis.kraken.demo.fragments.PhotosListFragment;
import com.github.marcosalis.kraken.demo.fragments.PhotosListFragment.PhotosSize;

/**
 * Demo activity holding a {@link PhotosListFragment}.
 * 
 * @since 1.0
 * @author Marco Salis
 */
public class PhotosListViewActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);

		final PhotosSize size = (PhotosSize) getIntent().getSerializableExtra(
				PhotosListFragment.ARGS_PHOTOS_SIZE);
		final Bundle args = new Bundle();
		args.putSerializable(PhotosListFragment.ARGS_PHOTOS_SIZE, size);

		switch (size) {
		case SMALL:
			setTitle(R.string.title_listview_small);
			break;
		case FULL_SCREEN:
			setTitle(R.string.title_listview_fullscreen);
			break;
		}

		final Fragment fragment = Fragment.instantiate(this,
				"com.github.marcosalis.kraken.demo.fragments.PhotosListFragment", args);
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
				.commit();
	}

}