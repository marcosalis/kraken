## Release... the Kraken!

*Kraken* is an easy to use, powerful and fast Android bitmaps/data loading and caching framework, based and refactored from my original open source project <b>droid_utils</b> @*Luluvise*.

It can be used from Android versions **4.0.3** upwards (API 15), and it is based on Google's **Guava** and **google-http-java-client** libraries, and **Jackson** for JSON data processing.

With *Kraken*, creating a global, multithreaded, two-level bitmap cache with default settings can be as easy as:
``` java
BitmapCache cache = new BitmapCacheBuilder(context).diskCacheDirectoryName("bitmaps").build();
```

and setting a bitmap into an *ImageView* asynchronously when retrieved is just:
``` java
cache.setBitmapAsync("https://www.google.co.uk/images/srpr/logo11w.png", imageView);
```

## Quick reference

### Current version and release notes
*Kraken* current version is **2.0.0 beta**

#### What's new

**2.0.0 beta**
- Added **Gradle** support for compilation
- Kraken finally comes as a fully working *Android Studio* project
- Updated **google-http-java-client** to version 1.16.0rc
- Updated **android-support-v4** and other support libs to version 23
- Updated annotations to use the <code>com.android.support:support-annotations</code> dependency
- Using Android Studio's default code formatter throughout the codebase
- Updated demo project to new compatibility themes and UI

**1.0.2 beta**
- Created <code>BitmapSetterBuilder</code> to simplify customized setting of bitmaps into *ImageView*s
- Hidden concrete implementations of <code>BitmapSetter</code> (**breaking change**)
- <code>JsonModel</code> does not override <code>toString()</code> behavior anymore. Use <code>toJsonString()</code> to get the JSON string representation of the model (**breaking change**)
- <code>BitmapSource</code> renamed to <code>CacheSource</code> and moved to <code>ContentCache</code> interface (**breaking change**)

**1.0.1 beta**
- Added <code>BitmapDecoder</code> to allow using a custom bitmap decoding policy to caches
- Added <code>BitmapDiskCache</code> interface (soon available: set custom disk cache from <code>BitmapCacheBuilder</code>)
- Added build.gradle file to the projects to import them inside *Android Studio* (still **not** working)
- Improved bitmap loading default mechanism, fixed issue where some bitmaps were downloaded multiple times when quickly scrolling a long list of items back and forth

### Dependencies
*Kraken* is based and depends on the following open source (see licenses below) third-party libraries:

* **guava-libraries-sdk5** v. 13 (https://code.google.com/p/guava-libraries/ - *Apache 2.0*)
* **google-http-java-client** v. 1.16.0rc (https://code.google.com/p/google-http-java-client/ - *Apache 2.0*)
* **jackson-json-processor** v. 2.2.2 (http://jackson.codehaus.org/  - *multiple licenses available*)
* **android-support-v4** v. 23 (http://developer.android.com/tools/support-library/  - *Apache 2.0*)

### Setup
To use *Kraken*, clone this repository:
<pre><code>git clone https://github.com/marcosalis/kraken.git</code></pre>
and add the <code>kraken_lib</code> module as a dependency on your application module's *build.gradle*:
<pre><dependencies {
    compile project(':kraken_lib')
}
</pre>

*Kraken* has two Gradle build variants:
- **debug**, which logs useful debug messages and statistics to the *LogCat*
- **release**, which disables all logging and must be used when releasing the application

### Demo application
The folder */kraken_demo* contains a demo application module that demonstrates how to use *Kraken* for bitmap caching. It implements very long *ListView*s and *GridView*s of bitmaps downloaded from the network so that you can see how the library performs in the most performance-critical scenario (*RecyclerView* demo coming soon).

### Bitmap loading and caching
Efficiently load images from the network and cache them, as well as being able to set them asynchronously into image views, is one of the most common problems in Android: it's really easy to overuse the UI thread or cause memory leaks in the attempt of improving the performances, especially when dealing with adapters and *ListView*s.
*Kraken* reliefs the programmer from the burden of managing all this. It holds a configurable memory and disk cache where bitmaps are stored after the download, and provides methods to set the bitmaps inside image views after they're loaded, seamlessly handling the case of recycled or destroyed views. Images are never downloaded twice in the case simultaneous requests (i.e. when scrolling a list back and forth).

#### Memory cache
*Kraken* uses Android's **LruCache** to provide a limited size memory cache to hold the recently used bitmaps, evicting the old ones with a LRU policy. The memory cache size can be set in terms of maximum bytes or percentage of the available application memory in the current device. Multiple bitmap caches can be built and their memory occupation sums up: it's not recommended to set above 20-25% of the total application memory for caching or the risk of *OutOfMemoryError*s would increase, unless your application only caches bitmaps (and you really know what you're doing).

It is important that you be nice to Android and clear the memory caches when receiving the <code>onLowMemory()</code> and <code>onTrimMemory()</code> (with critical state) callbacks. See the *demo application* for an example of how to do that.

#### Disk cache
The encoded version of the downloaded bitmaps are saved in the device's SD card (or internal flash memory as a fallback). An expiration time can be set, to make sure all old images are deleted when calling <code>clearDiskCache(ClearMode.EVICT_OLD)</code> on the <code>BitmapCache</code>.

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
 	.cacheLogName("Profile bitmaps cache")
 	.maxMemoryCachePercentage(15)
 	.diskCacheDirectoryName("profile_bitmaps")
 	.diskCachePurgeableAfter(DroidUtils.DAY)
 	.build();
```

##### Set a bitmap into an ImageView
The <code>BitmapCache</code> interface offers methods to prefetch, load and set bitmaps into an *ImageView*.
To fully customize how to set a bitmap inside the view, use <code>BitmapSetterBuilder</code>. If needed, the same builder can be reused within the same context (such as a long list view) to improve performances and save on object instantiation in performance critical situations (see the class documentation for more info).
This is the sample code to set a bitmap inside an *ImageView* with an animation:
``` java
BitmapSetterBuilder builder = cache.newBitmapSetterBuilder(true); // the builder can be reused
builder.setAsync("https://www.google.co.uk/images/srpr/logo11w.png")
	.placeholder(placeholderDrawable)
	.policy(AccessPolicy.NORMAL)
	.animate(AnimationMode.NOT_IN_MEMORY)
	.listener(new OnBitmapSetListener() {
		public void onBitmapSet(CacheUrlKey key, Bitmap bitmap, CacheSource source) {
			// called when the bitmap is set
		}
	})
	.into(imageView);
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
* More examples of use in the GitHub Wiki documentation
* Bitmaps: save into caches a resampled/resized version of a bitmap
* Bitmaps: allow custom pre/post processing of the downloaded bitmap
* Allow selection and use of other disk/memory cache policies (LFU?)
* Support for pluggable JSON/XML/other (de)serializers for model data
* Tasks: allow cancellation and priority setting
* Effective automatic disk cache purge policy implementation
* Data model: default implementation of a second level SQLLite cache through ContentResolver
* Data model: use of HTTP response cache and cache headers


## Other stuff

### Annotations and FindBugs™
I strongly believe in Java annotations as an effortless way to improve code quality and readability. That's why you'll find that the vast majority of *Kraken* source code is annotated with thread-safety (<code>@Immutable</code>, 
<code>@ThreadSafe</code>, <code>@NotThreadSafe</code>, <code>@NotForUiThread</code>) and parameter/fields consistency (<code>@NonNull</code>, <code>@Nullable</code>) information.<br />
I also make frequent use of the static analyzer **FindBugs** (http://findbugs.sourceforge.net/) and I consider it a very powerful tool that every Java programmer shouldn't live without. Check it out if you still haven't.

***Update (4/16):*** most static analysis capabilities are now by default built into Android Studio's *Analyze* features without the need to install a plugin. What's more, the Android support library now has a module <code>com.android.support:support-annotations</code> that includes many more useful annotations (see http://tools.android.com/tech-docs/support-annotations).
*Kraken* makes use of it throughout the codebase.

### Issues reporting and tests
A (hopefully enough) comprehensive suite of unit/functional tests for the library are provided as Android test project in the *kraken_tests* subfolder. Bug reports and feature requests are more then welcome, and the best way of submitting them is using the *Issues* feature in GitHub. Pull requests are more than welcome, too!

### Alternatives to Kraken
There are many other valid, up to date (and well known) open source alternatives to *Kraken*, which may be more suitable for you. Here is a few ones:
* **Fresco** (http://frescolib.org/)
* **Picasso** (http://square.github.io/picasso/)
* **Volley** (http://developer.android.com/training/volley/index.html)
* **Universal Image Loader** (https://github.com/nostra13/Android-Universal-Image-Loader)
* **ImageLoader** (https://github.com/novoda/ImageLoader)

### License
You are free to use, modify, redistribute *Kraken* in any way permitted by the **Apache 2.0** license. If you like *Kraken* and you are using it inside your Android application, please let me know by sending an email to *fast3r(at)gmail.com*.

> <pre>
> Copyright 2016 Marco Salis - fast3r(at)gmail.com
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
