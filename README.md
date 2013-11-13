## Release... the Kraken!

*Kraken* is an easy to use, powerful and fast Android bitmaps and data caching framework, based and refactored from my original open source <b>droid_utils</b> @Luluvise (which can be found at https://github.com/Luluvise/droid-utils).

It can be used from Android versions **2.2** upwards, and it is based on Google's **Guava** and **google-http-java-client** libraries, and **Jackson** for JSON data processing.

With *Kraken*, creating a global, multithreaded, two-level bitmap cache with default settings can be as easy as:
``` java
BitmapCache cache = new BitmapCacheBuilder(context).diskCacheDirectoryName("bitmaps").build();
```

and setting a bitmap into an ImageView asynchronously is just:
``` java
CacheUrlKey cacheKey = new SimpleCacheUrlKey("https://www.google.co.uk/images/srpr/logo11w.png");
BitmapAsyncSetter callback = new BitmapAsyncSetter(cacheKey, imageView);
cache.setBitmapAsync(cacheKey, callback);
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

### Bitmap loading and caching
TODO

### POJO and DTO loading, (de)serialization and caching
TODO

### Coming soon
* Android Studio / Gradle integration
* Bitmaps: save into caches a resampled/resized version of a bitmap
* Bitmaps: allow custom pre/post processing of the downloaded bitmap
* Allow selection and use of other disk/memory cache policies (LFU?)
* Effective automatic disk cache purge policy implementation


## Other

### Alternatives to Kraken
<p>There are many other valid (and well known) open source alternatives to Kraken, which may be more suitable for you. Here is a few ones:
<ul>
<li><b>Volley</b> (https://developers.google.com/events/io/sessions/325304728)</li>
<li><b>Picasso</b> (http://square.github.io/picasso/)</li>
<li><b>Universal Image Loader</b> (https://github.com/nostra13/Android-Universal-Image-Loader)</li>
</ul>
</p>

### License
You are free to use, modify, redistribute Kraken in any way permitted by the <i>Apache 2.0</i> license. If you like Kraken and you are using it inside your Android application, please let me know by sending an email to fast3r(at)gmail.com.

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
