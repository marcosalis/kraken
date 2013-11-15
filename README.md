## Release... the Kraken!

*Kraken* is an easy to use, powerful and fast Android bitmaps/data loading and caching framework, based and refactored from my original open source project <b>droid_utils</b> @Luluvise (which can be found at https://github.com/Luluvise/droid-utils).

It can be used from Android versions **2.2** upwards, and it is based on Google's **Guava** and **google-http-java-client** libraries, and **Jackson** for JSON data processing.

With *Kraken*, creating a global, multithreaded, two-level bitmap cache with default settings can be as easy as:
``` java
BitmapCache cache = new BitmapCacheBuilder(context).diskCacheDirectoryName("bitmaps").build();
```

and setting a bitmap into an *ImageView* asynchronously when retrieved is just:
``` java
CacheUrlKey cacheKey = new SimpleCacheUrlKey("https://www.google.co.uk/images/srpr/logo11w.png");
cache.setBitmapAsync(cacheKey, imageView);
```

## Quick reference

### Current version
*Kraken* current version is **1.0 beta 1**

### Dependencies
*Kraken* is based and depends on the following open source (see licenses below) third-party libraries:

* **guava-libraries-sdk5** v. 13 (https://code.google.com/p/guava-libraries/ - *Apache 2.0*)
* **google-http-java-client** v. 1.15.0rc (https://code.google.com/p/google-http-java-client/ - *Apache 2.0*)
* **jackson-json-processor** v. 2.2.2 (http://jackson.codehaus.org/  - *multiple licenses available*)
* **android-support-v4** v. 19 (http://developer.android.com/tools/support-library/  - *Apache 2.0*)

### Setup
To use *Kraken*, clone this repository:
<pre><code>git clone https://github.com/marcosalis/kraken.git</code></pre>
and add the *Eclipse* project as an Android library to your application project.

*Kraken* has two running configurations:
* **debug**, which logs useful debug messages and statistics to the *LogCat*
* **release**, which disables all logging and must be used when releasing the application

In order to be able to compile the project, or switch between configurations, run one of this two *Ant* targets:
<pre><code>ant kraken-debug-config</code></pre>
or
<pre><code>ant kraken-release-config</code></pre>
(*fear not, Gradle builds support is coming soon*).

### Demo application
The folder */kraken_demo* contains a demo application project that demostrates how to use *Kraken* for bitmap caching. It implements very long *ListView*s and *GridView*s of bitmaps downloaded from the network so that you can see how the library performs in the most performance-critical scenario.

### Bitmap loading and caching
Efficiently load images from the network and cache them, as well as being able to set them asynchronously into image views, is one of the most common problems in Android: it's really easy to overuse the UI thread or cause memory leaks in the attempt of improving the performances, especially when dealing with adapters and *ListView*s.
*Kraken* reliefs the programmer from the burden of managing all this. It holds a configurable memory and disk cache where bitmaps are stored after the download, and provides methods to set the bitmaps inside image views after they're loaded, seamlessly handling the case of recycled or destroyed views. Images are never downloaded twice in the case simultaneous requests (i.e. when scrolling a list back and forth).

#### Memory cache
*Kraken* uses Android's **LruCache** to provide a limited size memory cache to hold the recently used bitmaps, evicting the old ones with a LRU policy. The memory cache size can be set in terms of maximum bytes or percentage of the available application memory in the current device. Multiple bitmap caches can be built and their memory occupation sums up: it's not recommended to set above 20-25% of the total application memory for caching or the risk of *OutOfMemoryError*s would increase, unless your application only caches bitmaps (and you really know what you're doing).

It is important that you be nice to Android and clear the memory caches when receiving the <code>onLowMemory()</code> and <code>onTrimMemory()</code> (with critical state) callbacks. See the *demo application* for an example of how to do that.

#### Disk cache
The encoded version of the downloaded bitmaps are saved in the device's SD card (or internal flash memory as a fallback). An expiration time can be set, to make sure all old images are deleted when calling <code>BitmapCache.clearDiskCache(DiskCacheClearMode.EVICT_OLD)</code>.

#### HTTP transport
*Kraken* uses the **google-http-java-client** library for Android as an HTTP abstraction layer. This means you can easily override the default used <code>HttpTransport</code> to provide another implementation or write your own by passing the built <code>HttpRequestFactory</code> to the bitmap cache builder and provide your own customization of network-layer policies and failover such as <code>HttpRequestInitializer</code>, <code>BackOff</code>, <code>BackOffRequired</code> and <code>HttpUnsuccessfulResponseHandler</code>.

#### Threading policies
Image downloading is multithreaded to ensure maximum performances. *Kraken* automatically sets the best combination of thread pool sizes depending on the number of available CPU cores. A custom policy can be set by calling the static method <code>BitmapCacheBase.setThreadingPolicy()</code> with a <code>BitmapThreadingPolicy</code> instance.
The set policy and thread pools are shared among all bitmap caches, so that it's possible to create many (with different size, location and purpose) without spawning too many threads.

#### Access policy
With <code>AccessPolicy</code>, you can decide how to access the data inside the cache. Along with the <code>NORMAL</code> access mode (memory/disk/network), you can choose to refresh the item in cache from the network, only pre-fetch it into caches for future use, or retrieve it only if it's already in cache.

#### Usage
##### Create and reference a bitmap cache
The best way to initialize the caches is the <code>onCreate()</code> method of the <code>Application</code> class. *Kraken* provides a custom subclass called <code>DroidApplication</code> that provides some utility and debugging methods, check its documentation on how to use it.
The <code>Application</code> instance is never GC'd when the application process is alive. Moreover, its natural *singleton* behavior makes it the perfect place to store the global instance(s) of cache(s). When not in foreground, the application process, along with the whole app stack (and consequently memory caches), will be killed by Android whenever it requires memory.
See the <code>CachesManager</code> interface and its base implementation <code>BaseCachesManager</code> for a convenient way to group and access multiple caches.<br />
Using the code below, you can build a bitmap cache (with debugging name *"Profile bitmaps cache"*) that occupies the 15% of the total max app memory heap and stores the images in the external storage application cache subfolder *profile_bitmaps* with an expiration time of 1 day. See the <code>BitmapCacheBuilder</code> documentation for the full list of configurable parameters.
``` java
 BitmapCache cache = new BitmapCacheBuilder(context)
 	.maxMemoryCachePercentage(15)
 	.memoryCacheLogName("Profile bitmaps cache")
 	.diskCacheDirectoryName("profile_bitmaps")
 	.diskCachePurgeableAfter(DroidUtils.DAY)
 	.build();
```

##### Set a bitmap into an ImageView
The <code>BitmapCache</code> interface offers methods to prefetch, load and set bitmaps into an *ImageView*.
Here is the code that allows full customization (you can also use <code>AnimatedBitmapAsyncSetter</code> to control how to fade-in the bitmap when set into the view):
``` java
CacheUrlKey cacheKey = new SimpleCacheUrlKey("https://www.google.co.uk/images/srpr/logo11w.png");
OnBitmapSetListener listener = new OnBitmapSetListener() {
			@Override
			public void onSetIntoImageView(CacheUrlKey url, final Bitmap bitmap, BitmapSource source) {
			  // called when the bitmap is set
			}
		};
BitmapAsyncSetter callback = new BitmapAsyncSetter(cacheKey, imageView, listener);
cache.setBitmapAsync(cacheKey, callback);
```

### POJO and DTO loading, (de)serialization and caching
**Note** *The public classes and interfaces for this feature are currently in ALPHA version, and backwards compatibility in future releases of Kraken is not guaranteed. Please be patient as a stabler interface is being developed.*

Many Android applications require data for presentation to be downloaded from a remote server with REST. The purpose of *Kraken* is to allow easy caching this data models (no matter what the input format is) "as they are" in their serialized format, and deserialize them back as **POJO** (Plain Old Java Object - http://en.wikipedia.org/wiki/Plain_Old_Java_Object) or **DTO** (*Data Transfer Object* - http://en.wikipedia.org/wiki/Data_Transfer_Object) for use in client/UI code.

The structure of a <code>ModelContentProxy</code> is similar to a <code>BitmapCache</code>: it's implemented as a two-layers cache and allows the caller use a concrete implementation of <code>CacheableRequest</code> to fully customize the *HTTP(S)* request wrapper that will be executed and provides the cache key for the requested data.

See the following classes documentation for further information on how to subclass and create a <code>ContentProxy</code> for *JSON* models using the JSON processor **Jackson** (the only one supported at the moment):
* <code>ContentProxyBase</code>
* <code>ModelContentProxy</code>
* <code>AbstractDiskModelContentProxy</code>
* <code>ContentLruCache</code>
* <code>ModelDiskCache</code>

### Coming soon
* Android Studio / Gradle integration and distribution JAR
* More examples of use in the GitHub Wiki documentation
* Bitmaps: save into caches a resampled/resized version of a bitmap
* Bitmaps: allow custom pre/post processing of the downloaded bitmap
* Allow selection and use of other disk/memory cache policies (LFU?)
* Support for pluggable JSON/XML/other (de)serializers for model data
* Tasks: allow cancellation and priority setting
* Effective automatic disk cache purge policy implementation


## Other stuff

### Annotations and FindBugs™
I strongly believe in Java annotations as an effortless way to improve code quality and readability. That's why you'll find that the vast majority of *Kraken* source code is annotated with thread-safety (<code>@Immutable</code>, 
<code>@ThreadSafe</code>, <code>@NotThreadSafe</code>, <code>@NotForUiThread</code>) and parameter/fields consistency (<code>@Nonnull</code>, <code>@Nullable</code>) information.<br />
I also make frequent use of the static analyzer **FindBugs** (http://findbugs.sourceforge.net/) and I consider it a very powerful tool that every Java programmer shouldn't live without. Check it out if you still haven't.

### Issues reporting and tests
A (hopefully enough) comprehensive suite of unit/functional tests for the library are provided as Android test project in the *kraken_tests* subfolder. Bug reports and feature requests are more then welcome, and the best way of submitting them is using the *Issues* feature in GitHub. Pull requests are more than welcome, too!

### Alternatives to Kraken
<p>There are many other valid (and well known) open source alternatives to *Kraken*, which may be more suitable for you. Here is a few ones:
<ul>
<li><b>Volley</b> (https://developers.google.com/events/io/sessions/325304728)</li>
<li><b>Picasso</b> (http://square.github.io/picasso/)</li>
<li><b>Universal Image Loader</b> (https://github.com/nostra13/Android-Universal-Image-Loader)</li>
</ul>
</p>

### License
You are free to use, modify, redistribute *Kraken* in any way permitted by the **Apache 2.0** license. If you like *Kraken* and you are using it inside your Android application, please let me know by sending an email to *fast3r(at)gmail.com*.

> <pre>
> Copyright 2013 Marco Salis - fast3r(at)gmail.com
>
> Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
