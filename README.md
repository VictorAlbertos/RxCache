![Downloads](https://jitpack.io/v/VictorAlbertos/RxCache/month.svg)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxCache-green.svg?style=true)](https://android-arsenal.com/details/1/3016)

# RxCache

> [**中文文档**](http://www.jianshu.com/p/b58ef6b0624b)

_For a more reactive approach go [here](https://github.com/VictorAlbertos/ReactiveCache/tree/2.x)_.

The **goal** of this library is simple: **caching your data models like [Picasso](https://github.com/square/picasso) caches your images, with no effort at all.** 

Every Android application is a client application, which means it does not make sense to create and maintain a database just for caching data.

Plus, the fact that you have some sort of legendary database for persisting your data does not solves by itself the real challenge: to be able to configure your caching needs in a flexible and simple way. 

Inspired by [Retrofit](http://square.github.io/retrofit/) api, **RxCache is a reactive caching library for Android and Java which turns your caching needs into an interface.** 

When supplying an **`observable`, `single`, `maybe` or `flowable` (these are the supported Reactive types)** which contains the data provided by an expensive task -probably an http connection, RxCache determines if it is needed 
to subscribe to it or instead fetch the data previously cached. This decision is made based on the providers configuration.
 
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

## Setup

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
    compile "com.github.VictorAlbertos.RxCache:runtime:1.8.3-2.x"
    compile "io.reactivex.rxjava2:rxjava:2.1.6"
}
```

Because RxCache uses internally [Jolyglot](https://github.com/VictorAlbertos/Jolyglot) to serialize and deserialize objects, you need to add one of the next dependency to gradle.
 
```gradle
dependencies {
    // To use Gson 
    compile 'com.github.VictorAlbertos.Jolyglot:gson:0.0.4'
    
    // To use Jackson
    compile 'com.github.VictorAlbertos.Jolyglot:jackson:0.0.4'
    
    // To use Moshi
    compile 'com.github.VictorAlbertos.Jolyglot:moshi:0.0.4'
}
```

## Usage

Define an `interface` with as much methods as needed to create the caching providers:

```java
interface Providers {

        @ProviderKey("mocks")
        Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
    
        @ProviderKey("mocks-5-minute-ttl")
        @LifeCache(duration = 5, timeUnit = TimeUnit.MINUTES)
        Observable<List<Mock>> getMocksWith5MinutesLifeTime(Observable<List<Mock>> oMocks);
    
        @ProviderKey("mocks-evict-provider")
        Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
    
        @ProviderKey("mocks-paginate")
        Observable<List<Mock>> getMocksPaginate(Observable<List<Mock>> oMocks, DynamicKey page);
    
        @ProviderKey("mocks-paginate-evict-per-page")
        Observable<List<Mock>> getMocksPaginateEvictingPerPage(Observable<List<Mock>> oMocks, DynamicKey page, EvictDynamicKey evictPage);
        
        @ProviderKey("mocks-paginate-evict-per-filter")
        Observable<List<Mock>> getMocksPaginateWithFiltersEvictingPerFilter(Observable<List<Mock>> oMocks, DynamicKeyGroup filterPage, EvictDynamicKey evictFilter);
}
```

RxCache exposes `evictAll()` method to evict the entire cache in a row. 

RxCache accepts as argument a set of classes to indicate how the provider needs to handle the cached data:

* A Reactive type is the only object required to create a provider. This Reactive type must be equal to the one specified by the returning value of the provider.
* [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictProvider.java) allows to explicitly evict all the data associated with the provider.
* [@ProviderKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/ProviderKey.java) is an annotation for provider methods that is highly recommended to use and proguard users MUST use this annotation, if not used the method names will be used as provider keys (cache keys) and proguard users will quickly run into problems, please see [Proguard](proguard) for detailed information. Using the annotaiton is also useful when not using Proguard as it makes sure you can change your method names without having to write a migration for old cache files.
* [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKey.java) allows to explicitly evict the data of an specific [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKey.java).
* [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) allows to explicitly evict the data of an specific [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKeyGroup.java).
* [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKey.java) is a wrapper around the key object for those providers which need to handle multiple records, so they need to provide multiple keys, such us endpoints with pagination, ordering or filtering requirements. To evict the data associated with one particular key use `EvictDynamicKey`.
* [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKeyGroup.java) is a wrapper around the key and the group for those providers which need to handle multiple records grouped, so they need to provide multiple keys organized in groups, such us endpoints with filtering AND pagination requirements. To evict the data associated with the key of one particular group, use `EvictDynamicKeyGroup`.

Supported annotations:

* [@LifeCache](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/LifeCache.java) sets the amount of time before the data would be evicted. If `@LifeCache` is not supplied, the data will be never evicted unless it is required explicitly using [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictProvider.java), [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKey.java) or [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) .
* [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Actionable.java) offers an easy way to perform write operations using providers. More details [here](#actionable_section)
* [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/SchemeMigration.java) and [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Migration.java) provides a simple mechanism for handling migrations between releases. More details [here](#migrations_section)
* [@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Expirable.java) determines if that provider will be excluded from the evicting process or not. More details [here](#expirable_section)
* [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java) and [@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java) provides a simple way to encrypt/decrypt the data on persistence layer. More details [here](#encryption_section)

### Build an instance of Providers and use it

Finally, instantiate the Providers `interface` using `RxCache.Builder` and supplying a valid file system path which would allow RxCache to write on disk.

```java
File cacheDir = getFilesDir();
Providers providers = new RxCache.Builder()
                            .persistence(cacheDir, new GsonSpeaker())
                            .using(Providers.class);
```

### Putting It All Together

```java
interface Providers {

    @ProviderKey("mocks-evict-provider")
    Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);

    @ProviderKey("mocks-paginate-evict-per-page")
    Observable<List<Mock>> getMocksPaginateEvictingPerPage(Observable<List<Mock>> oMocks, DynamicKey page, EvictDynamicKey evictPage);

    @ProviderKey("mocks-paginate-evict-per-filter")
    Observable<List<Mock>> getMocksPaginateWithFiltersEvictingPerFilter(Observable<List<Mock>> oMocks, DynamicKeyGroup filterPage, EvictDynamicKey evictFilter);
}
```

```java
public class Repository {
    private final Providers providers;

    public Repository(File cacheDir) {
        providers = new RxCache.Builder()
                .persistence(cacheDir, new GsonSpeaker())
                .using(Providers.class);
    }

    public Observable<List<Mock>> getMocks(final boolean update) {
        return providers.getMocksEvictProvider(getExpensiveMocks(), new EvictProvider(update));
    }

    public Observable<List<Mock>> getMocksPaginate(final int page, final boolean update) {
        return providers.getMocksPaginateEvictingPerPage(getExpensiveMocks(), new DynamicKey(page), new EvictDynamicKey(update));
    }

    public Observable<List<Mock>> getMocksWithFiltersPaginate(final String filter, final int page, final boolean updateFilter) {
        return providers.getMocksPaginateWithFiltersEvictingPerFilter(getExpensiveMocks(), new DynamicKeyGroup(filter, page), new EvictDynamicKey(updateFilter));
    }

    //In a real use case, here is when you build your observable with the expensive operation.
    //Or if you are making http calls you can use Retrofit to get it out of the box.
    private Observable<List<Mock>> getExpensiveMocks() {
        return Observable.just(Arrays.asList(new Mock("")));
    }
}
```

## Use cases
* Using classic API RxCache for read actions with little write needs.
* Using actionable API RxCache, exclusive for write actions.

## Classic API RxCache:

Following use cases illustrate some common scenarios which will help to understand the usage of `DynamicKey` and `DynamicKeyGroup` classes along with evicting scopes.

### List

List without evicting:
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

List evicting:
```java
Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
```

> Runtime usage:

```java
//Hit observable evicting all mocks
getMocksEvictProvider(oMocks, new EvictProvider(true))

//This line throws an IllegalArgumentException: "EvictDynamicKey was provided but not was provided any DynamicKey"
getMocksEvictProvider(oMocks, new EvictDynamicKey(true))
```

### List Filtering

List filtering without evicting:
```java
Observable<List<Mock>> getMocksFiltered(Observable<List<Mock>> oMocks, DynamicKey filter);
```


List filtering evicting:
```java
Observable<List<Mock>> getMocksFilteredEvict(Observable<List<Mock>> oMocks, DynamicKey filter, EvictProvider evictDynamicKey);
```

> Runtime usage:

```java
//Hit observable evicting all mocks using EvictProvider
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictProvider(true))

//Hit observable evicting mocks of one filter using EvictDynamicKey
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKey(true))

//This line throws an IllegalArgumentException: "EvictDynamicKeyGroup was provided but not was provided any Group"
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKeyGroup(true))
```

### List Paginated with filters

List paginated with filters without evicting:
```java
Observable<List<Mock>> getMocksFilteredPaginate(Observable<List<Mock>> oMocks, DynamicKey filterAndPage);
```


List paginated with filters evicting:
```java
Observable<List<Mock>> getMocksFilteredPaginateEvict(Observable<List<Mock>> oMocks, DynamicKeyGroup filterAndPage, EvictProvider evictProvider);
```

> Runtime usage:

```java
//Hit observable evicting all mocks using EvictProvider
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictProvider(true))

//Hit observable evicting all mocks pages of one filter using EvictDynamicKey
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKey(true))

//Hit observable evicting one page mocks of one filter using EvictDynamicKeyGroup
getMocksFilteredPaginateInvalidate(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKeyGroup(true))
```

As you may already notice, the whole point of using `DynamicKey` or `DynamicKeyGroup` along with `Evict` classes is to play with several scopes when evicting objects.

The above examples declare providers which their method signature accepts `EvictProvider` in order to be able to concrete more specifics types of `EvictProvider` at runtime.

But I have done that for demonstration purposes, you always should narrow the evicting classes in your method signature to the type which you really need. For the last example, I would use `EvictDynamicKey` in production code, because this way I would be able to paginate the filtered items and evict them per its filter, triggered by a pull to refresh for instance.

Nevertheless, there are complete examples for [Android and Java projects](https://github.com/VictorAlbertos/RxCacheSamples).

## <a name="actionable_section"></a>Actionable API RxCache:

**Limitation: This actionable API only support `Observable` as Reactive type.**

This actionable api offers an easy way to perform write operations using providers. Although write operations could be achieved using the classic api too, it's much complex and error-prone. Indeed, the [Actions](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/ActionsList.java) class it's a wrapper around the classic api which play with evicting scopes and lists.

In order to use this actionable api, first you need to add the [repository compiler](https://github.com/VictorAlbertos/RxCache/tree/master/compiler) as a dependency to your project using an annotation processor. For Android, it would be as follows:

Add this line to your root build.gradle:

```gradle
dependencies {
     // other classpath definitions here
     classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
 }
```


Then make sure to apply the plugin in your app/build.gradle and add the compiler dependency:

```gradle
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    // apt command comes from the android-apt plugin
    apt "com.github.VictorAlbertos.RxCache:compiler:1.8.3-2.x"
}
```

After this configuration, every provider annotated with [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Actionable.java) `annotation` 
will expose an accessor method in a new generated class called with the same name as the interface, but appending an 'Actionable' suffix.

The order in the params supplies must be as in the following example:

```java
public interface RxProviders {
    @Actionable
    Observable<List<Mock.InnerMock>> mocks(Observable<List<Mock.InnerMock>> message, EvictProvider evictProvider);

    @Actionable
    Observable<List<Mock>> mocksDynamicKey(Observable<List<Mock>> message, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);

    @Actionable
    Observable<List<Mock>> mocksDynamicKeyGroup(Observable<List<Mock>> message, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKey);
}
```

The observable value must be a `List`, otherwise an error will be thrown.

The previous RxProviders `interface` will expose the next accessors methods in the generated `RxProvidersActionable` class.
```java
RxProvidersActionable.mocks(RxProviders proxy);
RxProvidersActionable.mocksDynamicKey(RxProviders proxy, DynamicKey dynamicKey);
RxProvidersActionable.mocksDynamicKeyGroup(RxProviders proxy, DynamicKeyGroup dynamicKeyGroup);
```

These methods return an instance of the `Actions` class, so now you are ready to use every write operation available in the [Actions](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/ActionsList.java) class. It is advisable to explore the [ActionsTest](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/test/java/io/rx_cache/internal/ActionsListTest.java) class to see what action fits better for your case. If you feel that some action has been missed please don't hesitate to open an issue to request it.

Some actions examples:

```java
ActionsProviders.mocks(rxProviders)
    .addFirst(new Mock())
    .addLast(new Mock())
    //Add a new mock at 5 position
    .add((position, count) -> position == 5, new Mock())

    .evictFirst()
    //Evict first element if the cache has already 300 records
    .evictFirst(count -> count > 300)
    .evictLast()
    //Evict last element if the cache has already 300 records
    .evictLast(count -> count > 300)
    //Evict all inactive elements
    .evictIterable((position, count, mock) -> mock.isInactive())
    .evictAll()

    //Update the mock with id 5
    .update(mock -> mock.getId() == 5, mock -> {
        mock.setActive();
        return mock;
    })
    //Update all inactive mocks
    .updateIterable(mock -> mock.isInactive(), mock -> {
        mock.setActive();
        return mock;
    })
    .toObservable()
    .subscribe(processedMocks -> {})
```

Every one of the previous actions will be execute only after the composed observable receives a subscription. This way, the underliyng provider cache will be modified its elements without effort at all.

## <a name="migrations_section"></a>Migrations

RxCache provides a simple mechanism for handling migrations between releases.

You need to annotate your providers `interface` with [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/SchemeMigration.java). This `annotation` accepts an array of [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Migration.java) annotations, and, in turn, `@Migration` annotation accepts both, a version number and an array of `Class`es which will be deleted from persistence layer.

```java
@SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock2.class}),
            @Migration(version = 3, evictClasses = {Mock3.class})
    })
interface Providers {}
```

You want to annotate a new migration only when a new field has been added in a class model used by RxCache.

Deleting classes or deleting fields of classes would be handle automatically by RxCache, so you don't need to annotate a new migration when a field or an entire class has been deleted.

For instance:

A migration was added at some point. After that, a second one was added eventually.

```java
@SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock2.class})
    })
interface Providers {}
```

But now `Mock` class has been deleted from the project, so it is impossible to reference its class anymore. To fix this, just delete the migration `annotation`.

```java
@SchemeMigration({
            @Migration(version = 2, evictClasses = {Mock2.class})
    })
interface Providers {}
```

Because RxCache has an internal process to clean memory when it is required, the data will be evicted eventually.

## <a name="encryption_section"></a>Encryption

RxCache provides a simple mechanism to encrypt the data.

You need to annotate your providers `interface` with [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java). This `annotation` accepts a string as the `key` necessary to encrypt/decrypt the data. But you will need to annotate your provider's records with [@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java) in order to saved the data encrypted. If no [@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java) is set, then no encryption will be held. 

**Important:** If the value of the `key` supplied on [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java) is modified between compilations, then the previous persisted data will not be able to be evicted/retrieved by RxCache. 

```java
@EncryptKey("myStrongKey-1234")
interface Providers {
        @Encrypt
        Observable<List<Mock>> getMocksEncrypted(Observable<List<Mock>> oMocks);

        Observable<List<Mock>> getMocksNotEncrypted(Observable<List<Mock>> oMocks);
}
```

## Configure general behaviour

RxCache allows to set certain parameters when building the providers instance:

### <a name="expirable_section"></a>Configure the limit in megabytes for the data to be persisted

By default, RxCache sets the limit in 100 megabytes, but you can change this value by calling setMaxMBPersistenceCache method when building the provider instance.

```java
new RxCache.Builder()
            .setMaxMBPersistenceCache(maxMgPersistenceCache)
            .persistence(cacheDir)
            .using(Providers.class);
```

This limit ensure that the disk will no grow up limitless in case you have providers with dynamic keys which values changes dynamically, like filters based on gps location or dynamic filters supplied by your back-end solution.

When this limit is reached, RxCache will not be able to persist in disk new data. That's why RxCache has an automated process to evict any record when the threshold memory assigned to the persistence layer is close to be reached, even if the record life time has not been fulfilled.

But provider's record annotated with [@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Expirable.java) annotation and set its value to false will be excluded from the process.

```java
interface Providers {
    @Expirable(false)
    Observable<List<Mock>> getMocksNotExpirable(Observable<List<Mock>> oMocks);
}
```

### Use expired data if loader not available

By default, RxCache will throw a RuntimeException if the cached data has expired and the data returned by the observable loader is null, 
preventing this way serving data which has been marked as evicted.

You can modify this behaviour, allowing RxCache serving evicted data when the loader has returned null values, by setting as true the value of useExpiredDataIfLoaderNotAvailable

```java
new RxCache.Builder()
            .useExpiredDataIfLoaderNotAvailable(true)
            .persistence(cacheDir)
            .using(Providers.class);
```

## Android considerations

To build an instance of the interface used as provides by RxCache, you need to supply a reference to a file system. On Android, you can get the File reference calling getFilesDir() from the [Android Application](http://developer.android.com/intl/es/reference/android/app/Application.html) class.

Also, it is recommended to use this Android Application class to provide a unique instance of RxCache for the entire life cycle of your application.

In order execute the Observable on a new thread, and emit results through onNext on the main UI thread, you should use the built in methods provided by [RxAndroid](https://github.com/ReactiveX/RxAndroid).

Check the [Android example](https://github.com/VictorAlbertos/RxCacheSamples/tree/master/sample_android)

## Retrofit

RxCache is the perfect match for Retrofit to create a repository of auto-managed-caching data pointing to endpoints. 
You can check an [example](https://github.com/VictorAlbertos/RxCacheSamples/blob/master/sample_data/src/main/java/sample_data/Repository.java) of RxCache and Retrofit working together.

## Internals

RxCache serves the data from one of its three layers:

* A memory layer -> Powered by [Apache ReferenceMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/ReferenceMap.html).
* A persisting layer -> RxCache uses internally [Jolyglot](https://github.com/VictorAlbertos/Jolyglot) for serialize and deserialize objects.
* A loader layer (the observable supplied by the client library)

The policy is very simple: 

* If the data requested is in memory, and It has not been expired, get it from memory.
* Else if the data requested is in persistence layer, and It has not been expired, get it from persistence.
* Else get it from the loader layer. 

## Proguard
Proguard users MUST add the two given lines to their proguard configuration file and MUST use the `@ProviderKey` annotation method for every method that is being used as provider. Without the `@ProviderKey` annotation the method name will be used instead which can lead to providers that use the same name, see issue [#96](https://github.com/VictorAlbertos/RxCache/issues/96) for detailed information.

```
-dontwarn io.rx_cache2.internal.**
-keepclassmembers enum io.rx_cache2.Source { *; }
-keepclassmembernames class * { @io.rx_cache2.* <methods>; }
```


## Author

**Víctor Albertos**

* <https://twitter.com/_victorAlbertos>
* <https://www.linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

## RxCache Swift version:
[RxCache](https://github.com/VictorAlbertos/RxSCache): Reactive caching library for Swift.

## Another author's libraries using RxJava:
* [Mockery](https://github.com/VictorAlbertos/Mockery): Android and Java library for mocking and testing networking layers with built-in support for Retrofit.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 
* [RxFcm](https://github.com/VictorAlbertos/RxFcm): RxJava extension for Android Firebase Cloud Messaging (aka fcm).
* [RxSocialConnect](https://github.com/VictorAlbertos/RxSocialConnect-Android): OAuth RxJava extension for Android.
