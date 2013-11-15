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
package com.github.marcosalis.kraken.cache.bitmap.threading;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nonnull;

import com.github.marcosalis.kraken.cache.bitmap.BitmapCache;

/**
 * <p>
 * Public interface to access a threading policy for the bitmap caches.
 * 
 * <p>
 * Classes implementing it should provide a set of {@link ThreadPoolExecutor}s
 * that will be shared among all created {@link BitmapCache}s instances inside
 * the application. In most cases the policy provided by
 * {@link DefaultBitmapThreadingPolicy} is optimal and should be preferred to
 * custom implementations.
 * 
 * The default bitmap caches implementations use two different executors:
 * <ol>
 * <li>A <b>bitmap disk executor</b>, where the caches are accessed and bitmaps
 * are decoded from the disk cache if needed. The tasks executed here are mostly
 * CPU/(disk)IO bound and the pool size should be therefore limited.</li>
 * <li>A <b>bitmap downloader executor</b>, in charge of executing the tasks
 * that effectively download the bitmaps and decode the response to a Bitmap
 * instance. Performance-wise, the pool size should be bigger than the previous
 * as the tasks executed here are mostly network-bound.</li>
 * </ol>
 * 
 * @since 1.0
 * @author Marco Salis
 */
public interface BitmapThreadingPolicy {

	@Nonnull
	public ThreadPoolExecutor getBitmapDiskExecutor();

	@Nonnull
	public ThreadPoolExecutor getBitmapDownloader();

}