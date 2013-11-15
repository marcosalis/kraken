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
package com.github.marcosalis.kraken.demo.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;

import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.demo.models.Photo;

/**
 * ListFragment that displays a list of images from a {@link BitmapCache}.
 * 
 * @since 1.0
 * @author Marco Salis
 */
public class PhotosListFragment extends ListFragment {

	public static final String ARGS_PHOTOS_SIZE = "com.github.marcosalis.kraken.demo.fragments.photos_size";

	public enum PhotosSize {
		SMALL,
		FULL_SCREEN;
	}

	private ArrayAdapter<Photo> mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final PhotosSize size = (PhotosSize) getArguments().getSerializable(ARGS_PHOTOS_SIZE);

		switch (size) {
		case SMALL:
			mAdapter = new SmallPhotosAdapter(getActivity(), new ArrayList<Photo>());
			break;
		case FULL_SCREEN:
			mAdapter = new FullScreenPhotosAdapter(getActivity(), new ArrayList<Photo>());
			break;
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setListAdapter(mAdapter);
	}

	private class SmallPhotosAdapter extends ArrayAdapter<Photo> {

		public SmallPhotosAdapter(Context context, List<Photo> objects) {
			super(context, -1, objects);
		}
	}

	private class FullScreenPhotosAdapter extends ArrayAdapter<Photo> {

		public FullScreenPhotosAdapter(Context context, List<Photo> objects) {
			super(context, -1, objects);
		}
	}

}