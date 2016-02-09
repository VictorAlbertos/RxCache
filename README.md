[![Build Status](https://travis-ci.org/VictorAlbertos/RxCache.svg?branch=master)](https://travis-ci.org/VictorAlbertos/RxCache)

[![Android Gems](http://www.android-gems.com/badge/VictorAlbertos/RxCache.svg?branch=master)](http://www.android-gems.com/lib/VictorAlbertos/RxCache)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxCache-green.svg?style=true)](https://android-arsenal.com/details/1/3016)

RxCache
=======

Inspired by [Retrofit](http://square.github.io/retrofit/) api, RxCache is a reactive caching library for Android and Java which turns your caching needs into an interface. 

Every method acts as a provider for RxCache, and all of them are managed through observables; they are the fundamental contract 
between the library and its clients. 

When supplying an observable which contains the data provided by an expensive task -probably a http connection, RxCache determines if it is needed 
to subscribe to it or instead fetch the data previously cached. This decision is made based on the providers configuration.

So, when supplying an observable you get your observable cached back, and next time you will retrieve it without the time cost associated with its underlying task. 
 
```java
Observable<List<Mock>> getMocks(@Loader Observable<List<Mock>> mocks);
```

Setup
=======

Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:
```gradle
dependencies {
    compile "com.github.VictorAlbertos:RxCache:0.4.8"
    compile "io.reactivex:rxjava:1.1.0"
}
```

Usage
=======

Define an interface with as much methods as needed to create the caching providers:

```java
interface Providers {
    Observable<List<Mock>> getMocks(@Loader Observable<List<Mock>> mocks);
    
    @LifeCache(duration = 5, timeUnit = TimeUnit.MINUTES)
    Observable<List<Mock>> getMocksWith5MinutesLifeTime(@Loader Observable<List<Mock>> mocks);
    
    Observable<List<Mock>> getMocksInvalidateCache(@Loader Observable<List<Mock>> mocks, @InvalidateCache Invalidator invalidator);
    
    Observable<List<Mock>> getMocksPaginate(@Loader Observable<List<Mock>> mocks, @DynamicKey int page);
    
    Observable<List<Mock>> getMocksPaginateInvalidateCachePerPage(@Loader Observable<List<Mock>> mocks, @DynamicKey int page,
                                                                        @InvalidateCache InvalidatorDynamicKey invalidatorPerPage);
}
```


RxCache provides a set of annotations to indicate how a provider will be handled:

* [@Loader](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Loader.java) is the only annotation required to create a provider. The associated object must be an observable which parameterized type must be equal to the one specified by the returning value of the provider. 
* [@LifeCache](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/LifeCache.java) sets the amount of time before the data would be evicted. If @LifeCache is not supplied, the data will be never evicted unless it is required explicitly using an [Invalidator](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Invalidator.java).
* [@InvalidateCache](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/InvalidateCache.java) allows to explicitly evict the data passing an implementation of Invalidator interface. 
* [@DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/DynamicKey.java) provides a runtime possibility to generate multiple variants for the same provider, allowing to support pagination or filtering. 
    If you need to invalidate the data associated with one particular key, use @InvalidateCache, but instead of providing an Invalidator, you must provide an implementation of [InvalidatorDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/InvalidatorDynamicKey.java), which requires to be specified the key of the data to be evicted.


Build an instance of Providers and use it
-----------
Finally, instantiate the Providers interface using RxCache.Builder and supplying a valid file system which allows RxCache writes on disk. 

```java
File cacheDir = getFilesDir();
Providers providers = new RxCache.Builder()
                            .persistence(cacheDir)
                            .using(Providers.class);
```

Putting It All Together
-----------
```java
interface Providers {        
    Observable<List<Mock>> getMocksInvalidateCache(@Loader Observable<List<Mock>> mocks, @InvalidateCache Invalidator invalidator);
        
    Observable<List<Mock>> getMocksPaginateInvalidateCachePerPage(@Loader Observable<List<Mock>> mocks, @DynamicKey int page,
                                                                        @InvalidateCache InvalidatorDynamicKey invalidatorPerPage);
}
```

```java
    public class Repository {
        private final Providers providers;

        public Repository(File cacheDir) {
            providers = new RxCache.Builder()
                    .persistence(cacheDir)
                    .using(Providers.class);
        }

        public Observable<List<Mock>> getMocks(final boolean update) {
            return providers.getMocksInvalidateCache(getExpensiveMocks(), new Invalidator() {
                @Override public boolean invalidate() {
                    return update;
                }
            });
        }

        public Observable<List<Mock>> getMocksPaginate(final int page, final boolean update) {
            return providers.getMocksPaginateInvalidateCachePerPage(getExpensiveMocks(), page, new InvalidatorDynamicKey() {
                @Override public Object dynamicKey() {
                    return page;
                }

                @Override public boolean invalidate() {
                    return update;
                }
            });
        }

        //In a real use case, here is when you build your observable with the expensive operation.
        //Or if you are making http calls you can use Retrofit to get it out of the box.
        private Observable<List<Mock>> getExpensiveMocks() {
            return Observable.just(new Arrays.asList(new Mock()));
        }
    }
```

Nevertheless, there are complete examples for [Android and Java projects](https://github.com/VictorAlbertos/RxCacheSamples).

Configure general behaviour
---------------------------
RxCache allows to set certain parameters when building the providers instance:

PolicyHeapCache
---------------
PolicyHeapCache sets the percentage to be used for the the in memory cache layer, based on the total heap memory available.

```java
public enum PolicyHeapCache {
        CONSERVATIVE(.40), MODERATE(.60), AGGRESSIVE(.80);
}
```

The memory cache will use as much memory as resulting of this percentage, regardless the current memory allocated by other resources. 

So you may incur in out of memory errors if you allocates chunks of memory not managed by this library.
```java
new RxCache.Builder()
            .withPolicyCache(PolicyHeapCache.MODERATE)
            .persistence(cacheDir)
            .using(Providers.class);
```

If not PolicyHeapCache is specified, PolicyHeapCache.Conservative would be set as default. 


Use expired data if loader not available
----------------------------------------
By default, RxCache will throw a RuntimeException if the cached data has expired and the data returned by the observable loader is null, 
preventing this way serving data which has been marked as evicted.

You can modify this behaviour, allowing RxCache serving evicted data when the loader has returned null values, by setting as true the value of useExpiredDataIfLoaderNotAvailable

```java
new RxCache.Builder()
            .useExpiredDataIfLoaderNotAvailable(true)
            .persistence(cacheDir)
            .using(Providers.class);
```


Changing the mechanism for the persistence layer
------------------------------------------------
RxCache uses Gson for serialize and deserialize objects in order to save them in disk. This is the mechanism provided by RxCache for the persistence layer. 

But you can supply your own persistence mechanism implementing a Persistence interface and passing it when building, as follow:  

```java
new RxCache.Builder()
            .persistence(new Persistence() {
                @Override public void saveRecord(String key, Record record) {
                    
                }

                @Override public void delete(String key) {

                }

                @Override public <T> Record<T> retrieveRecord(String key) {
                    return null;
                }
            })
            .using(Providers.class);
```                  

Android considerations
-----------
To build an instance of the interface used as provides by RxCache, you need to supply a reference to a file system. On Android, you can get the File reference calling getFilesDir() from the [Android Application](http://developer.android.com/intl/es/reference/android/app/Application.html) class.

Also, it is recommended to use this Android Application class to provide a unique instance of RxCache for the entire life cycle of your application.

In order execute the Observable on a new thread, and emit results through onNext on the main UI thread, you should use the built in methods provided by [RxAndroid](https://github.com/ReactiveX/RxAndroid).

Check the [Android example](https://github.com/VictorAlbertos/RxCacheSamples/tree/master/sample_android)

Retrofit
-----------
RxCache is the perfect match for Retrofit to create a repository of auto-managed-caching data pointing to endpoints. 
You can check an [example](https://github.com/VictorAlbertos/RxCacheSamples/blob/master/sample_data/src/main/java/sample_data/Repository.java) of RxCache and Retrofit working together.

Internals
---------
RxCache serves the data from one of its three layers:

* A memory layer -> Powered by [Guava LoadingCache](https://github.com/google/guava/wiki/CachesExplained).
* A persisting layer -> RxCache uses internally [Gson](https://github.com/google/gson) for serialize and deserialize objects.
* A loader layer (the observable supplied by the client library)

The policy is very simple: 

* If the data requested is in memory, and It has not been expired, get it from memory.
* Else if the data requested is in persistence layer, and It has not been expired, get it from persistence.
* Else get it from the loader layer. 
