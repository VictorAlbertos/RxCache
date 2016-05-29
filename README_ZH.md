[![Build Status](https://travis-ci.org/VictorAlbertos/RxCache.svg?branch=master)](https://travis-ci.org/VictorAlbertos/RxCache)

[![Android Gems](http://www.android-gems.com/badge/VictorAlbertos/RxCache.svg?branch=master)](http://www.android-gems.com/lib/VictorAlbertos/RxCache)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxCache-green.svg?style=true)](https://android-arsenal.com/details/1/3016)

# RxCache

_`swift`版请点击[这里](https://github.com/FuckBoilerplate/RxCache)_.

这个库的**目标**非常简单: **像[Picasso](https://github.com/square/picasso) 缓存图片一样毫不费力地缓存你的数据** 

每一个Android 应用都属于客户端程序,这意味着创建和维护一个仅用于缓存数据的数据库没有任何意义.

另外，事实上用数据库持久化保存数据并不能解决真正的挑战：以灵活简单的方式实现数据缓存.


源于  [Retrofit](http://square.github.io/retrofit/) api, ** RxCache 是一个Reactive缓存库，可用于Android 和Java。能够将你的缓存成需求转成一个接口.**

每个方法都充当RxCache的提供者, 他们都是通过`observables`来管理的,他们是类库和当前客户端之间的基本联系

当提供一个`observable`包含的数据由一个昂贵的任务提供者可能是一个HTTP连接，rxcache确定是否需要订阅，或代替获取数据以前缓存。这取决于你的配置

因此，提供当`observable`你得到你的`observable`达到了回来，下次还会检索它不与它的基本任务相关联的时间成本.
 
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

## 配置

 添加JitPack仓库在你的build.gradle文件 (项目根目录下):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

添加依赖库,在项目模块中
```gradle
dependencies {
    compile "com.github.VictorAlbertos.RxCache:core:1.4.6"
    compile "io.reactivex:rxjava:1.1.5"
}
```

##  使用方法

定义一个 `接口`方法对应你所需要缓存的数据提供者

```java
interface Providers {
        Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
    
        @LifeCache(duration = 5, timeUnit = TimeUnit.MINUTES)
        Observable<List<Mock>> getMocksWith5MinutesLifeTime(Observable<List<Mock>> oMocks);
    
        Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
    
        Observable<List<Mock>> getMocksPaginate(Observable<List<Mock>> oMocks, DynamicKey page);
    
        Observable<List<Mock>> getMocksPaginateEvictingPerPage(Observable<List<Mock>> oMocks, DynamicKey page, EvictDynamicKey evictPage);
        
        Observable<List<Mock>> getMocksPaginateWithFiltersEvictingPerFilter(Observable<List<Mock>> oMocks, DynamicKeyGroup filterPage, EvictDynamicKey evictFilter);
}
```


RxCache接受作为参数一组类来表示提供商需要如何处理缓存的数据:

*`Observable`是创建一个提供所需的唯一对象。 `Observable`类型必须等于提供商的返回值指定的。
* [@LifeCache](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/LifeCache.java)设置缓存过期时间. 如果没有设置`@LifeCache` , 数据将用于不会过期清理除非你使用了 [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/EvictProvider.java), [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/EvictDynamicKey.java) or [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) .
* [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/EvictProvider.java)可以明确地清理与提供者有关的所有数据. 
* [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/EvictDynamicKey.java)允许明确地清理特殊的数据 [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/DynamicKey.java).
* [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) 允许明确地驱逐一组特定的数据. [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/DynamicKeyGroup.java).
* [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/DynamicKey.java)对于那些需要处理多个记录供应商的关键对象的包装，所以他们需要提供多个密钥，具有分页，排序或筛选要求，我们的端点。驱逐与一个特定的键使用`EvictDynamicKey`相关的数据。
 
* [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/DynamicKeyGroup.java)是围绕重点及本集团对于那些需要处理分组的多个记录提供者的包装，所以他们需要提供团体组织的多个key，例如我们与终端过滤和分页要求。驱逐与一个特定群体的密钥关联的数据，使用`EvictDynamicKeyGroup`。


###创建一个提供者的实例并使用它

最后,使用 `RxCache.Builder`实例化提供者`interface`，提供一个有效的文件系统路径允许RxCache写磁盘上。

```java
File cacheDir = getFilesDir();
Providers providers = new RxCache.Builder()
                            .persistence(cacheDir)
                            .using(Providers.class);
```

###全部放在一起

```java
interface Providers {        
    Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
        
    Observable<List<Mock>> getMocksPaginateEvictingPerPage(Observable<List<Mock>> oMocks, DynamicKey page, EvictDynamicKey evictPage);
    
    Observable<List<Mock>> getMocksPaginateWithFiltersEvictingPerFilter(Observable<List<Mock>> oMocks, DynamicKeyGroup filterPage, EvictDynamicKey evictFilter);
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
        return providers.getMocksEvictProvider(getExpensiveMocks(), new EvictProvider(update));
    }

    public Observable<List<Mock>> getMocksPaginate(final int page, final boolean update) {
        return providers.getMocksPaginateEvictingPerPage(getExpensiveMocks(), new DynamicKey(page), new EvictDynamicKey(update));
    }

    public Observable<List<Mock>> getMocksWithFiltersPaginate(final String filter, final int page, final boolean updateFilter) {
        return providers.getMocksPaginateWithFiltersEvictingPerFilter(getExpensiveMocks(), new DynamicKeyGroup(filter, page), new EvictDynamicKey(updateFilter));
    }

    //在实际的使用情况下，这里是当你建立你观察到的与昂贵的操作。
    //如果你正在使用HTTP调用可以改造出来的盒子。
    private Observable<List<Mock>> getExpensiveMocks() {
        return Observable.just(Arrays.asList(new Mock("")));
    }
}
```


## 用例

* 使用经典的API RxCache对阅读的行为很少写的需要。
* 使用可操作的API接收缓存，独有的写操作。


## 经典API缓存：

下面用案例说明一些常见的场景，这将有助于了解`DynamicKey`和`DynamicKey`类随着驱逐范围使用。

### List

List 不需要驱逐:
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

List 驱逐缓存数据:
```java
Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
```

> 运行时的用法：

```java
//Hit observable evicting all mocks 
getMocksEvictProvider(oMocks, new EvictProvider(true))

//This line throws an IllegalArgumentException: "EvictDynamicKey was provided but not was provided any DynamicKey"
getMocksEvictProvider(oMocks, new EvictDynamicKey(true))
```

### List 过滤

List filtering without evicting:
```java
Observable<List<Mock>> getMocksFiltered(Observable<List<Mock>> oMocks, DynamicKey filter);
```


List 过滤驱逐:
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
		
## Actionable API RxCache:

This actionable api offers an easy way to perform write operations using providers. Although write operations could be achieved using the classic api too, it's much complex and error-prone. Indeed, the [Actions](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Actions.java) class it's a wrapper around the classic api which play with evicting scopes and lists.

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
    apt "com.github.VictorAlbertos.RxCache:compiler:1.4.6"
}
```
		
After this configuration, every provider annotated with [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Actionable.java) `annotation` will generate an accessor method in the `ActionsProviders` class.

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

The previous RxProviders `interface` will expose the next accessors methods in the `ActionsProviders` class.
```java
ActionsProviders.mocks();
ActionsProviders.mocksDynamicKey(DynamicKey dynamicKey);
ActionsProviders.mocksDynamicKeyGroup(DynamicKeyGroup dynamicKeyGroup);
```

This methods return an instance of the `Actions` class, so now you are ready to use every write operation available in the [Actions](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Actions.java) class. It is advisable to explore the [ActionsTest](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/test/java/io/rx_cache/internal/ActionsTest.java) class to see what action fits better for your case. If you feel that some action has been missed please don't hesitate to open an issue to request it.   

Some actions examples: 

```java
ActionsProviders.mocks()
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

## Migrations

RxCache provides a simple mechanism for handling migrations between releases. 

You need to annotate your providers `interface` with [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/SchemeMigration.java). This `annotation` accepts an array of [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Migration.java) annotations, and, in turn, `@Migration` annotation accepts both, a version number and an array of `Class`es which will be deleted from persistence layer.

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

## Configure general behaviour

RxCache allows to set certain parameters when building the providers instance:

### Configure the limit in megabytes for the data to be persisted 

By default, RxCache sets the limit in 100 megabytes, but you can change this value by calling setMaxMBPersistenceCache method when building the provider instance.

```java
new RxCache.Builder()
            .setMaxMBPersistenceCache(maxMgPersistenceCache)
            .persistence(cacheDir)
            .using(Providers.class);
```

This limit ensure that the disk will no grow up limitless in case you have providers with dynamic keys which values changes dynamically, like filters based on gps location or dynamic filters supplied by your back-end solution.

When this limit is reached, RxCache will not be able to persist in disk new data. That's why RxCache has an automated process to evict any record when the threshold memory assigned to the persistence layer is close to be reached, even if the record life time has not been fulfilled.

But provider's record annotated with [@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/rx_cache/src/main/java/io/rx_cache/Expirable.java) annotation and set its value to false will be exclude from the process.

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
* A persisting layer -> RxCache uses internally [Gson](https://github.com/google/gson) for serialize and deserialize objects.
* A loader layer (the observable supplied by the client library)

The policy is very simple: 

* If the data requested is in memory, and It has not been expired, get it from memory.
* Else if the data requested is in persistence layer, and It has not been expired, get it from persistence.
* Else get it from the loader layer. 

## Proguard
```
-dontwarn io.rx_cache.internal.**
```


## Author

**Víctor Albertos**

* <https://twitter.com/_victorAlbertos>
* <https://www.linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

## RxCache Swift version:
[RxCache](https://github.com/VictorAlbertos/RxSCache): Reactive caching library for Swift.

## Another author's libraries using RxJava:
* [RxPaparazzo](https://github.com/FuckBoilerplate/RxPaparazzo): RxJava extension for Android to take images using camera and gallery.
* [RxGcm](https://github.com/VictorAlbertos/RxGcm): A reactive wrapper for Android Google Cloud Messaging to get rid of Service(s) configuration, handling foreground and background notifications depending on application state.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 
