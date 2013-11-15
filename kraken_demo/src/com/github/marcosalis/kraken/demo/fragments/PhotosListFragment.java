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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.marcosalis.kraken.cache.AccessPolicy;
import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAnimatedAsyncSetter;
import com.github.marcosalis.kraken.cache.bitmap.utils.BitmapAsyncSetter;
import com.github.marcosalis.kraken.cache.keys.SimpleCacheUrlKey;
import com.github.marcosalis.kraken.demo.KrakenDemoApplication;
import com.github.marcosalis.kraken.demo.KrakenDemoApplication.CacheId;
import com.github.marcosalis.kraken.demo.R;
import com.github.marcosalis.kraken.demo.models.Photo;
import com.github.marcosalis.kraken.demo.models.PhotosList;
import com.github.marcosalis.kraken.utils.android.LogUtils;
import com.github.marcosalis.kraken.utils.android.ParallelAsyncTask;
import com.google.common.collect.ImmutableList;

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

	private PhotosSize mPhotosSize;
	private ArrayAdapter<Photo> mAdapter;
	private PhotosListTask mPhotosTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPhotosSize = (PhotosSize) getArguments().getSerializable(ARGS_PHOTOS_SIZE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// TODO: use best photos size for screen size
		switch (mPhotosSize) {
		case SMALL:
			mPhotosTask = new PhotosListTask("photos_130x130.json");
			break;
		case FULL_SCREEN:
			mPhotosTask = new PhotosListTask("photos_720x720.json");
			break;
		}
		mPhotosTask.parallelExec(getActivity().getAssets());
	}

	@Override
	public void onDestroyView() {
		mPhotosTask.cancel(true);
		super.onDestroyView();
	}

	@Nonnull
	private ArrayAdapter<Photo> buildAdapter(List<Photo> photos) {
		switch (mPhotosSize) {
		case SMALL:
			mAdapter = new SmallPhotosAdapter(getActivity(), photos);
			break;
		case FULL_SCREEN:
			mAdapter = new FullScreenPhotosAdapter(getActivity(), photos);
			break;
		}
		return mAdapter;
	}

	private class PhotosListTask extends ParallelAsyncTask<AssetManager, Void, PhotosList> {

		private final String mJsonFile;

		public PhotosListTask(String jsonFile) {
			mJsonFile = jsonFile;
		}

		@Override
		protected PhotosList doInBackground(AssetManager... params) {
			try {
				final InputStream stream = params[0].open(mJsonFile);
				final ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(stream, PhotosList.class);
			} catch (IOException e) {
				LogUtils.logException(e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(PhotosList result) {
			final ImmutableList<Photo> list;
			if (result != null && (list = result.getPhotos()) != null) {
				final ArrayAdapter<Photo> adapter = buildAdapter(list);
				setListAdapter(adapter);
			} else {
				setListShown(true);
				setEmptyText("Error while retrieving the items");
			}
		}
	}

	private static class PhotosAdapter extends ArrayAdapter<Photo> {

		protected LayoutInflater mInflater;
		protected BitmapCache mBitmapCache;
		protected Drawable mPlaceholder;

		public PhotosAdapter(Context context, int resource, List<Photo> objects) {
			super(context, resource, objects);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		protected void setViewSquaredHeight(@Nonnull View parent, @Nonnull ImageView imgView) {
			final LayoutParams params = imgView.getLayoutParams();
			params.height = parent.getWidth();
		}

		protected void setBitmapAnimated(@Nonnull ViewHolder holder, @Nonnull Photo photo) {
			final String photoUrl = photo.getPhotoUrl();
			if (photoUrl != null) {
				final SimpleCacheUrlKey key = new SimpleCacheUrlKey(photoUrl);
				final BitmapAsyncSetter setter = new BitmapAnimatedAsyncSetter(key,
						holder.imageView);
				mBitmapCache.setBitmapAsync(key, AccessPolicy.NORMAL, setter, mPlaceholder);
			} else {
				holder.imageView.setImageDrawable(mPlaceholder);
			}
		}

		protected static class ViewHolder {
			public ImageView imageView;
			public TextView textView;
		}
	}

	private static class SmallPhotosAdapter extends PhotosAdapter {

		public SmallPhotosAdapter(Context context, List<Photo> objects) {
			super(context, -1, objects);
			mBitmapCache = (BitmapCache) KrakenDemoApplication.get().getCache(CacheId.BITMAPS_130);
			mPlaceholder = context.getResources().getDrawable(R.drawable.ic_launcher);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Photo photo = getItem(position);
			final View view;
			final ViewHolder holder;

			// view holder pattern to retrieve views references
			if (convertView == null) {
				view = mInflater.inflate(R.layout.adapter_listview_small, null);
				holder = new ViewHolder();
				holder.imageView = (ImageView) view.findViewById(R.id.image_view);
				holder.textView = (TextView) view.findViewById(R.id.text_view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) convertView.getTag();
			}

			// populate data
			setBitmapAnimated(holder, photo);
			holder.textView.setText("Image n. " + position);

			return view;
		}
	}

	private static class FullScreenPhotosAdapter extends PhotosAdapter {

		public FullScreenPhotosAdapter(Context context, List<Photo> objects) {
			super(context, -1, objects);
			mBitmapCache = (BitmapCache) KrakenDemoApplication.get()
					.getCache(CacheId.BITMAPS_LARGE);
			mPlaceholder = context.getResources().getDrawable(R.drawable.ic_launcher);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Photo photo = getItem(position);
			final View view;
			final ViewHolder holder;

			// view holder pattern to retrieve views references
			if (convertView == null) {
				view = mInflater.inflate(R.layout.adapter_listview_fullscreen, null);
				holder = new ViewHolder();
				holder.imageView = (ImageView) view.findViewById(R.id.image_view);
				holder.textView = (TextView) view.findViewById(R.id.text_view);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) convertView.getTag();
			}

			// populate data
			setViewSquaredHeight(parent, holder.imageView);
			setBitmapAnimated(holder, photo);
			holder.textView.setText("Image n. " + position);

			return view;
		}
	}

}