![Downloads](https://jitpack.io/v/VictorAlbertos/RxCache/month.svg)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxCache-green.svg?style=true)](https://android-arsenal.com/details/1/3016)

# RxCache中文文档

本库的 **目标** 很简单: **就像[Picasso](https://github.com/square/picasso) 缓存您的图片一样，毫不费力缓存您的数据模型。** 

每个Android应用程序都是一个客户端应用程序，这意味着仅仅为缓存数据创建和维护数据库是没有意义的。

事实上，对于通过传统数据库来持久保留数据并不能解决真正的挑战：能够以灵活简单的方式配置缓存需求。

灵感来源于 [Retrofit](http://square.github.io/retrofit/) , **RxCache是一个用于Android和Java的响应式缓存库，它可将您的缓存需求转换为一个接口。** 

当提供一个 **`observable`, `single`, `maybe` or `flowable` (这些是受支持的响应式数据类型)** 这些由比较重的任务提供的数据，RxCache确定是否需要订阅它，或取代先前缓存的数据。 此决定是基于Providers配置进行的。
 
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

## 依赖配置

在您的build.gradle（顶层级的Module）中添加JitPack仓库：

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

将下列的依赖添加到Module的build.gradle中：

```gradle
dependencies {
    compile "com.github.VictorAlbertos.RxCache:runtime:1.8.1-2.x"
    compile "io.reactivex.rxjava2:rxjava:2.0.6"
}
```

因为RxCache在内部使用 [Jolyglot](https://github.com/VictorAlbertos/Jolyglot) 对对象进行序列化和反序列化, 您需要选择下列的依赖中选择一个进行添加：
 
```gradle
dependencies {
    // To use Gson 
    compile 'com.github.VictorAlbertos.Jolyglot:gson:0.0.3'
    
    // To use Jackson
    compile 'com.github.VictorAlbertos.Jolyglot:jackson:0.0.3'
    
    // To use Moshi
    compile 'com.github.VictorAlbertos.Jolyglot:moshi:0.0.3'
}
```

## 用法

Define an `interface` with as much methods as needed to create the caching providers:
声明一个接口，通过和需求一样多的方法来创建缓存的Providers。


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

RxCache暴露了`evictAll（）`方法来清除一行中的整个缓存。

RxCache接受作为参数的一组类，以指示Provider如何处理缓存的数据：

* 一个响应式支持的类型是创建Provider所需要的唯一对象，该响应式所支持类型必须和该方法的返回值的响应式类型保持一致。
* [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictProvider.java) 允许明确地清除与Provider相关联的所有数据.
* [@ProviderKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/ProviderKey.java) 是强烈建议使用和保护用户数据的Provider方法的注解，必须使用这个注解！ 如果不使用该注解，该方法的名称会被作为该Provider的缓存键, 用户很快会遇到问题，详情请参阅 [Proguard](proguard) . 如果不使用Proguard，使用注解也很有用，因为它可以确保您可以更改方法名称，而无需为旧缓存文件编写迁移。
* [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKey.java) 允许明确地清除具体的缓存数据 [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKey.java).
* [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) 允许明确地清除具体的缓存数据 [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKeyGroup.java).
* [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKey.java) 是那些需要处理多个记录的Provider的key对象的包装器，所以他们需要提供多个key来满足，比如说我们的端点具有分页，排序或过滤的需求。 清除与一个特定key相关联的数据使用 `EvictDynamicKey`.
* [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKeyGroup.java) 是那些需要处理多个记录的Provider的key对象或key对象组的包装器，所以他们需要提供多个key的组合，比如我们的端点具有分页并且过滤的需求。 清除与一个特定key组相关联的数据使用`EvictDynamicKeyGroup`.

Supported annotations:

* [@LifeCache](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/LifeCache.java) 设置缓存过期时间. 如果没有设置@LifeCache , 数据将被永久缓存理除非你使用了 [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictProvider.java), [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKey.java) or [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) .
* [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Actionable.java) 提供了使用提供程序执行写入操作的简单方法。 详情参考 [here](#actionable_section)
* [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/SchemeMigration.java) 和 [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Migration.java) 提供了一种处理版本之间迁移的简单机制。 详情参考 [here](#migrations_section)
* [@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Expirable.java) 决定该Provider是否将被排除在清除范围之外.详情参考 [here](#expirable_section)
* [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java) 和 [@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java) 提供了一种在持久层上加密/解密数据的简单方法。详情参考 [here](#encryption_section)

### 新建Provider的实例并使用它

最后，使用RxCache.Builder实例化Provider接口，并提供一个有效的文件系统路径，这将允许RxCache在磁盘上写入缓存数据。

```java
File cacheDir = getFilesDir();
Providers providers = new RxCache.Builder()
                            .persistence(cacheDir, new GsonSpeaker())
                            .using(Providers.class);
```

### 将它们放在一起

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

    //实际项目中，这里是当你用「重」的操作构建你的observable。
    //或者如果您正在进行http请求，您可以使用Retrofit将其开箱即用。
    private Observable<List<Mock>> getExpensiveMocks() {
        return Observable.just(Arrays.asList(new Mock("")));
    }
}
```

## 使用场景
*使用经典的RxCache API进行文件的读写操作。
*使用Actionable的RxCache API，专用于写入操作。

## 经典的RxCache API:

下面的用例说明了一些常见的情况，这将有助于您了解“DynamicKey”和“DynamicKeyGroup”类的使用以及清除数据。

### List

不清除List数据
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

清除List数据
```java
Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
```

> 运行时使用:（译者：Runtime usage直译，这里理解为真实环境下的代码，下同）

```java
//接收到Observable时清除该Provider所有Mock数据
getMocksEvictProvider(oMocks, new EvictProvider(true))

//这行会抛出一个IllegalArgumentException：“提供了EvictDynamicKey但没有提供任何DynamicKey”
getMocksEvictProvider(oMocks, new EvictDynamicKey(true))
```

### List 筛选

List筛选，不清除缓存数据
```java
Observable<List<Mock>> getMocksFiltered(Observable<List<Mock>> oMocks, DynamicKey filter);
```

List筛选，清除缓存数据
```java
Observable<List<Mock>> getMocksFilteredEvict(Observable<List<Mock>> oMocks, DynamicKey filter, EvictProvider evictDynamicKey);
```

> 运行时使用:

```java
//接收到Observable时清除该Provider所有Mock数据
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictProvider(true))

//通过使用EvictDynamicKey，接收到Observable时，清除该DynamicKey下所有Mock数据
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKey(true))

//这行抛出一个IllegalArgumentException：“提供了EvictDynamicKeyGroup，但没有提供任何DynamicKeyGroup”
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKeyGroup(true))
```

### List的分页和过滤

List数据的分页和过滤，不清除缓存数据
```java
Observable<List<Mock>> getMocksFilteredPaginate(Observable<List<Mock>> oMocks, DynamicKey filterAndPage);
```


List数据的分页和过滤，包含是否清除缓存数据选项
```java
Observable<List<Mock>> getMocksFilteredPaginateEvict(Observable<List<Mock>> oMocks, DynamicKeyGroup filterAndPage, EvictProvider evictProvider);
```

> 运行时使用:

```java

//接收到Observable时清除该Provider所有Mock数据
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictProvider(true))

//通过使用EvictDynamicKey，接收到Observable时，清除该DynamicKey("actives", "page1")下所有Mock数据
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKey(true))

//通过使用EvictDynamicKey，接收到Observable时，清除该DynamicKeyGroup("actives", "page1")下所有Mock数据
getMocksFilteredPaginateInvalidate(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKeyGroup(true))
```

正如你可能已经注意到的那样，使用“DynamicKey”或“DynamicKeyGroup”以及“Evict”类的整个重点是在根据不同范围，清除缓存数据对象。

上述示例声明了他们的方法签名接受“EvictProvider”的提供者，以便在运行时能够具体具体更详细的“EvictProvider”类型。

但是为了示范，我已经做到了这一点，您总是把您的方法通过标记，将类别缩小到你真正需要清除的类型。对于最后一个例子，我将在实际产品代码中使用“EvictDynamicKey”，因为这样我就可以对已过滤的项目进行分页，并将其按过滤器排除，例如通过刷新来触发。

这里还有完整的例子 [Android and Java projects](https://github.com/VictorAlbertos/RxCacheSamples).

## <a name="actionable_section"></a>Actionable RxCache API:

**限制：此actionable的API仅支持Observable的数据类型。**

这个actionable的api提供了一种使用提供程序执行文件写入操作的简单方法。 尽管使用经典的api也可以实现写入操作，但经典的api有着复杂性且容易出错。实际上，[Actions](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/ActionsList.java)类是围绕经典api的包装器。

In order to use this actionable api, first you need to add theas a dependency to your project using an annotation processor. For Android, it would be as follows:
为了能够使用该actionable API，首先，你需要添加 [repository compiler](https://github.com/VictorAlbertos/RxCache/tree/master/compiler) 的依赖到您的build.gradle:


```gradle
dependencies {
     // other classpath definitions here
     classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
 }
```

然后确保在您的app / build.gradle中应用该插件，并添加编译器依赖关系：

```gradle
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    // apt command comes from the android-apt plugin
    apt "com.github.VictorAlbertos.RxCache:compiler:1.8.0-1.x"
}
```

配置完成后，为每个Provider注释 [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Actionable.java) `annotation` 
将在一个新的生成的类中暴露一个访问方法，该类与接口名称相同，但是附加了一个“Actionable”后缀。

参数供应中的顺序必须与以下示例保持一致：

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

Observable内的值必须是“List”类型，否则将抛出异常。

这样上面的RxProviders接口将会在生成的“RxProvidersActionable”类中暴露出下面的方法：
```java
RxProvidersActionable.mocks(RxProviders proxy);
RxProvidersActionable.mocksDynamicKey(RxProviders proxy, DynamicKey dynamicKey);
RxProvidersActionable.mocksDynamicKeyGroup(RxProviders proxy, DynamicKeyGroup dynamicKeyGroup);
```

这些方法返回“Actions”类的一个实例，所以现在你已经准备好使用可用的每个写操作 [Actions](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/ActionsList.java) .建议您浏览[ActionsTest](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/test/java/io/rx_cache/internal/ActionsListTest.java)类，以查看哪些操作适合 更适合你的情况。

一些示例代码：

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

之前的每个Action只有在composed的observable接收到订阅之后才会执行。 这样，下层的Provider缓存将完全修改其元素。

## <a name="migrations_section"></a>迁移

RxCache提供了一种处理版本之间迁移的简单机制。

您需要为您的Provider接口提供注解 [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/SchemeMigration.java). 这个注解接受一个数组 [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Migration.java) ，反过来，Migration注释同时接受一个版本号和一个Classes的数组，这些数组将从持久层中删除。

```java
@SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock2.class}),
            @Migration(version = 3, evictClasses = {Mock3.class})
    })
interface Providers {}
```

只有当RxCache使用的Class模型中添加了新的字段时，才需要迁移新的注解。

删除类或删除类的字段将由RxCache自动处理，因此当字段或整个类被删除时，不需要迁移新的注解。

```java
@SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock2.class})
    })
interface Providers {}
```

但是现在，“Mock”类已经从项目中删除了，所以不可能再引用它的类了。 要解决这个问题，只需删除这行迁移的注解即可。

```java
@SchemeMigration({
            @Migration(version = 2, evictClasses = {Mock2.class})
    })
interface Providers {}
```

因为RxCache需要内部进程才能清理内存，所以数据最终将被全部清除。

## <a name="encryption_section"></a>数据加密

RxCache提供了一种加密数据的简单机制。

您需要为您的Provider接口添加注解[@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java). 这个`annotation`接受一个字符串作为加密/解密数据所必需的`key`。 但是，您需要使用[@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java)对Provider的缓存进行注解，以便缓存数据加密。 如果没有设置[@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java)，则不会进行加密。
**重要提示：**如果提供的“key”值 [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java) 在编译期间进行了修改，那么以前的持久化数据将无法被RxCache清除/获取。

```java
@EncryptKey("myStrongKey-1234")
interface Providers {
        @Encrypt
        Observable<List<Mock>> getMocksEncrypted(Observable<List<Mock>> oMocks);

        Observable<List<Mock>> getMocksNotEncrypted(Observable<List<Mock>> oMocks);
}
```

## 常规配置

RxCache允许在构建Provider实例时设置某些参数：

### <a name="expirable_section"></a>配置要保留的数据的大小限制（以兆字节为单位）

默认情况下，RxCache将限制设置为100M，但您可以在构建Provider实例时调用setMaxMBPersistenceCache方法来更改此值。

```java
new RxCache.Builder()
            .setMaxMBPersistenceCache(maxMgPersistenceCache)
            .persistence(cacheDir)
            .using(Providers.class);
```

当达到此限制时，RxCache将无法保留磁盘新数据。 这就是为什么当分配给持久层的阈值内存接近达到时，RxCache有一个自动化的过程来清除任何记录，即使没有满足失效时间的缓存数据也被清除。

但是Provider的某方法用[@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Expirable.java) 注解注释，并将其值设置为false将额外被保存。

```java
interface Providers {
    @Expirable(false)
    Observable<List<Mock>> getMocksNotExpirable(Observable<List<Mock>> oMocks);
}
```

### 如果正常请求加载数据，使用过期的缓存数据

默认情况下，如果缓存的数据已过期并且observable loader返回的数据为空，RxCache将抛出RuntimeException异常。

您可以修改此行为，允许RxCache在加载程序返回空值时提供被清除的数据，通过将useExpiredDataIfLoaderNotAvailable的值设置为true
```java
new RxCache.Builder()
            .useExpiredDataIfLoaderNotAvailable(true)
            .persistence(cacheDir)
            .using(Providers.class);
```

## Android注意事项

要构建由RxCache提供的接口实例，您需要提供对文件系统的引用。 在Android上，您可以从[Android应用程序](http://developer.android.com/intl/es/reference/android/app/Application.html)类获取文件引用调用getFilesDir（）。

此外，建议您在应用程序的整个生命周期中使用此Android应用程序类来提供RxCache的唯一实例(全局单例)。

为了在新线程上执行Observable，并通过主UI线程上的onNext发出结果，您应该使用[RxAndroid](https://github.com/ReactiveX/RxAndroid)提供的内置方法。

查看案例： [Android example](https://github.com/VictorAlbertos/RxCacheSamples/tree/master/sample_android)

## Retrofit

RxCache和Retrofit完美搭配，创建一个指向端点的自动管理缓存数据库。
您可以检查RxCache和Retrofit的一个[示例](https://github.com/VictorAlbertos/RxCacheSamples/blob/master/sample_data/src/main/java/sample_data/Repository.java)。

## 本质

RxCache serves the data from one of its three layers:

* A memory layer -> Powered by [Apache ReferenceMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/ReferenceMap.html).
* A persisting layer -> RxCache uses internally [Jolyglot](https://github.com/VictorAlbertos/Jolyglot) for serialize and deserialize objects.
* A loader layer (the observable supplied by the client library)

The policy is very simple: 

* If the data requested is in memory, and It has not been expired, get it from memory.
* Else if the data requested is in persistence layer, and It has not been expired, get it from persistence.
* Else get it from the loader layer. 

RxCache服务于三个层之一的数据：

*内存层 - >由[Apache ReferenceMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/ReferenceMap.html)提供支持。
*持久层 - > RxCache内部使用[Jolyglot](https://github.com/VictorAlbertos/Jolyglot)来对对象进行序列化和反序列化。
*加载器层（由客户端库提供的Observable请求）

*如果请求的数据在内存中，并且尚未过期，请从内存中获取。
*否则请求的数据在持久层中，并且尚未过期，请从内存层获取。
*否则从加载器层请求获取它。

## 混淆

```
-dontwarn io.rx_cache.internal.**
-keepclassmembers enum io.rx_cache.Source { *; }
```


## 关于作者

**Víctor Albertos**

* <https://twitter.com/_victorAlbertos>
* <https://www.linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

## RxCache Swift版本：
[RxCache](https://github.com/VictorAlbertos/RxSCache): Reactive caching library for Swift.

## 作者其它使用RxJava的库:
* [Mockery](https://github.com/VictorAlbertos/Mockery): Android and Java library for mocking and testing networking layers with built-in support for Retrofit.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 
* [RxFcm](https://github.com/VictorAlbertos/RxFcm): RxJava extension for Android Firebase Cloud Messaging (aka fcm).
* [RxSocialConnect](https://github.com/VictorAlbertos/RxSocialConnect-Android): OAuth RxJava extension for Android.

