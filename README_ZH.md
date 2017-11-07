![Downloads](https://jitpack.io/v/VictorAlbertos/RxCache/month.svg)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxCache-green.svg?style=true)](https://android-arsenal.com/details/1/3016)

# RxCache中文文档

### [1 概述](#1)
 
### [2 基本使用](#2)
 
[2.1 依赖配置](#2.1)
    
[2.2 接口配置](#2.2)

[2.3 新建Provider实例并使用它](#2.3)

[2.4 再次回顾整个流程](#2.4)
    
### [3 RxCache使用场景](#3)
 
### [4 RxCache API](#4)
 
 [4.1 EvictProvider:驱逐缓存数据](#4.1)

 [4.2 DynamicKey:筛选数据](#4.2)

 [4.3 DynamicKeyGroup:分页和过滤](#4.3)
  
### [5 Actionable RxCache API](#5)

### [6 高级选项](#6)
 
 [6.1 数据迁移](#6.1)

 [6.2 数据加密](#6.2)

 [6.3 常规配置](#6.3)

  [6.3.1  配置要保留的数据的大小限制（以兆字节为单位）](#6.3.1)

  [6.3.2 如果未加载到数据，使用过期的缓存数据](#6.3.2)

 [6.4 Android注意事项](#6.4)

 [6.5 和Retrofit搭配使用](#6.5)

### [7 其他](#7)
    
 [7.1 RxCache原理](#7.1)

 [7.2 代码混淆](#7.2)

 [7.3 关于作者](#7.3)

 [7.4 RxCache Swift版本](#7.4)

 [7.5 作者其它使用RxJava的库](#7.5)
 
### [8 关于中文文档](#8)
 
## <h2 id="1">概述</h2>

本库的 **目标** 很简单: **就像[Picasso](https://github.com/square/picasso) 缓存您的图片一样，毫不费力缓存您的数据对象。** 

每个Android Application都是一个客户端应用程序，这意味着仅仅为缓存数据创建数据库并进行维护毫无意义。

事实上，传统方式通过数据库来缓存数据并没有解决根本性的问题：以更加灵活简单的方式配置缓存。

灵感来源于 [Retrofit](http://square.github.io/retrofit/) , **RxCache是一个用于Android和Java的响应式缓存库，它可将您的缓存需求转换为一个接口进行配置。** 

当提供一个 **`observable`, `single`, `maybe` or `flowable` (这些是RxJava2支持的响应式数据类型)** 这些由耗时操作提供的数据，RxCache确定是否需要subscribe，或覆盖先前缓存的数据。
 
此决定是基于RxCache的Providers进行配置的。
 
## <h2 id="2">基本使用</h2>
## <h3 id="2.1">依赖配置</h3>

在您的Project级的build.gradle中添加JitPack仓库：

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

因为RxCache在内部使用 [Jolyglot](https://github.com/VictorAlbertos/Jolyglot) 对数据进行序列化和反序列化, 您需要选择下列的依赖中选择一个进行添加：
 
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

## <h3 id="2.2">接口配置</h3> 

声明一个接口，常规使用方式中（以Retrofit网络请求为例），创建和API需求同样多的Providers来缓存您的数据。
> 这意味着，项目中Retrofit的APIService接口有多少个抽象方法的API需要实现缓存，一一对应，就需要Providers提供多少个缓存API方法

```java
interface Providers {
        
        @ProviderKey("mocks")
        Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
    
        @ProviderKey("mocks-5-minute-ttl")
        @LifeCache(duration = 5, timeUnit = TimeUnit.MINUTES)   //缓存有效期5分钟
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

RxCache的Provider配置中，方法所需要的参数用来配置Provider处理缓存的方式：

* 无论如何，必不可少的参数是RxJava提供的响应式基本数据类型（如Observable），这个参数的意义是将你想缓存的Retrofit接口作为参数传入，并以相同的RxJava数据类型作为返回。
  > 这意味着，您可以不配置任何可选项，但是您必须将您要缓存的数据作为参数交给RxCache进行缓存.
* [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictProvider.java) 是否驱逐与该Provider相关联的所有缓存数据.
  > 该对象通过构造方法进行实例化，创建时需要传入boolean类型的参数，当参数为true时，RxCache会直接驱逐该Provider的缓存数据，进行最新的网络请求并进行缓存；若参数为false，若缓存数据未过期，正常加载缓存数据
* [@ProviderKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/ProviderKey.java) 保护用户数据的Provider方法的注解，强烈建议使用这个注解！ 如果不使用该注解，该方法的名称会被作为该Provider的key进行文件缓存, 使用了代码混淆的用户很快会遇到问题，详情请参阅 [Proguard](proguard) . 如果不使用代码混淆，该注解也很有用，因为它可以确保您可以随心所欲修改Provider数据缓存的方法名，而无需为旧缓存文件迁移问题而苦恼。
  > 该注解是最近版本添加的，在考虑到代码混淆（方法名的改变导致缓存文件命名的改变）和缓存数据迁移,强烈建议使用该注解！
* [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKey.java) 是否驱逐具体的缓存数据 [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKey.java).
  > 缓存数据驱逐范围比EvictProvider小（后者是驱逐所有缓存），比EvictDynamicKeyGroup大（后者是驱逐更精细分类的缓存），举例，若将userId（唯一）作为参数传入DynamicKey，清除缓存时，仅清除该userId下的对应缓存
* [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) 是否驱逐更加具体的缓存数据 [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKeyGroup.java).
  > 和EvictDynamicKey对比，上述案例中，DynamicKeyGroup可以filter到某userId下缓存的某一页数据进行驱逐，其他缓存不驱逐
* [DynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKey.java) 通过传入一个对象参数（比如userId）实现和对应缓存数据的绑定， 清除该key相关联的缓存数据请使用 `EvictDynamicKey`.
* [DynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/DynamicKeyGroup.java) 通过传入一个Group参数（比如userId，数据的分类）实现和对应缓存数据的绑定， 清除该keyGroup相关联的缓存数据请使用`EvictDynamicKeyGroup`.

Supported annotations:

* [@LifeCache](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/LifeCache.java) 设置缓存过期时间. 如果没有设置@LifeCache , 数据将被永久缓存，直到你使用了 [EvictProvider](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictProvider.java), [EvictDynamicKey](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKey.java) or [EvictDynamicKeyGroup](https://github.com/VictorAlbertos/RxCache/blob/master/core/src/main/java/io/rx_cache/EvictDynamicKeyGroup.java) .
* [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Actionable.java) 提供了使用提供程序执行写入操作的简单方法。 详情参考 [here](#actionable_section)
* [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/SchemeMigration.java) 和 [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Migration.java) 提供了一种处理版本之间迁移的简单机制。 详情参考 [here](#migrations_section)
* [@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Expirable.java) 决定该Provider是否将被排除在清除范围之外.详情参考 [here](#expirable_section)
* [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java) 和 [@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java) 提供了一种在持久层上加密/解密数据的简单方法。详情参考 [here](#encryption_section)

### <h3 id="2.3">新建Provider实例并使用它</h3> 

最后，使用RxCache.Builder实例化Provider接口，并提供一个有效的文件系统路径，这将允许RxCache在磁盘上写入缓存数据。

```java
//获取缓存的文件存放路径
File cacheDir = getFilesDir();
Providers providers = new RxCache.Builder()
                            .persistence(cacheDir, new GsonSpeaker())//配置缓存的文件存放路径，以及数据的序列化和反序列化
                            .using(Providers.class);    //和Retrofit相似，传入缓存API的接口
```

### <h3 id="2.4">再次回顾整个流程</h3> 

```java
interface Providers {
    //配置要缓存的数据，以及是否驱逐缓存数据并请求网络
    @ProviderKey("mocks-evict-provider")
    Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
    
    //配置要缓存的数据，简单的缓存数据分类，以及是否驱逐该分类下的缓存数据并请求网络
    @ProviderKey("mocks-paginate-evict-per-page")
    Observable<List<Mock>> getMocksPaginateEvictingPerPage(Observable<List<Mock>> oMocks, DynamicKey page, EvictDynamicKey evictPage);
    
    //配置要缓存的数据，复杂的缓存数据分类，以及是否驱逐该详细分类下的缓存数据并请求网络
    @ProviderKey("mocks-paginate-evict-per-filter")
    Observable<List<Mock>> getMocksPaginateWithFiltersEvictingPerFilter(Observable<List<Mock>> oMocks, DynamicKeyGroup filterPage, EvictDynamicKey evictFilter);
}
```

```java
public class Repository {
    private final Providers providers;
    
    //初始化RxCache的Provider
    public Repository(File cacheDir) {
        providers = new RxCache.Builder()
                .persistence(cacheDir, new GsonSpeaker())
                .using(Providers.class);
    }
    
    //参数update：是否加载最新数据
    public Observable<List<Mock>> getMocks(final boolean update) {
        return providers.getMocksEvictProvider(getExpensiveMocks(), new EvictProvider(update));
    }
    
    //参数page：第几页的数据，update：是否加载该页的最新数据
    public Observable<List<Mock>> getMocksPaginate(final int page, final boolean update) {
        return providers.getMocksPaginateEvictingPerPage(getExpensiveMocks(), new DynamicKey(page), new EvictDynamicKey(update));
    }
    
    //参数filter：某个条件（比如userName），参数page：第几页数据，参数updateFilter：是否加载该userName该页的最新数据
    public Observable<List<Mock>> getMocksWithFiltersPaginate(final String filter, final int page, final boolean updateFilter) {
        return providers.getMocksPaginateWithFiltersEvictingPerFilter(getExpensiveMocks(), new DynamicKeyGroup(filter, page), new EvictDynamicKey(updateFilter));
    }

    //这个方法的返回值代替了现实开发中，您通过耗时操作获得的数据类型（比如Observable<T>）
    //如果这里您使用了Retrofit进行网络请求，那么可以说是拿来即用。
    private Observable<List<Mock>> getExpensiveMocks() {
        return Observable.just(Arrays.asList(new Mock("")));
    }
}
```

## <h2 id="3">RxCache使用场景</h2>

* 使用经典的RxCache API进行文件的读写操作。
* 使用Actionable的API，专用于文件的写操作。

## <h2 id="4">RxCache API</h2>

下面的用例说明了一些常见的情况，这将有助于您了解“DynamicKey”和“DynamicKeyGroup”类的使用以及清除数据。

### <h3 id="4.1">EvictProvider:驱逐缓存数据</h3>

不驱逐数据
```java
Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
```

驱逐数据
```java
Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);
```

> 业务代码中使用:

```java

//接收到Observable时驱逐该Provider所有缓存数据并重新请求
getMocksEvictProvider(oMocks, new EvictProvider(true))

//这行会抛出一个IllegalArgumentException：“提供了EvictDynamicKey但没有提供任何DynamicKey”
getMocksEvictProvider(oMocks, new EvictDynamicKey(true))
```

### <h3 id="4.2">DynamicKey:筛选数据</h3>

指定某个条件，不驱逐该条件下的缓存数据
```java
Observable<List<Mock>> getMocksFiltered(Observable<List<Mock>> oMocks, DynamicKey filter);
```

指定某个条件，可选择是否驱逐该条件下的缓存数据
```java
Observable<List<Mock>> getMocksFilteredEvict(Observable<List<Mock>> oMocks, DynamicKey filter, EvictProvider evictDynamicKey);
```

> 业务代码中使用:

```java

//接收到Observable时驱逐该Provider所有缓存数据并重新请求
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictProvider(true))

//通过使用EvictDynamicKey，接收到Observable时，驱逐该DynamicKey（"actives"）下所有缓存数据并重新请求
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKey(true))

//这行抛出一个IllegalArgumentException：“提供了EvictDynamicKeyGroup，但没有提供任何DynamicKeyGroup”
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKeyGroup(true))
```

### <h3 id="4.3">DynamicKeyGroup:分页和过滤</h3>

List数据的分页和过滤，不驱逐缓存数据
```java
Observable<List<Mock>> getMocksFilteredPaginate(Observable<List<Mock>> oMocks, DynamicKey filterAndPage);
```


List数据的分页和过滤，包含是否驱逐缓存数据选项
```java
Observable<List<Mock>> getMocksFilteredPaginateEvict(Observable<List<Mock>> oMocks, DynamicKeyGroup filterAndPage, EvictProvider evictProvider);
```

> 运行时使用:

```java

//接收到Observable时驱逐该Provider所有缓存数据并重新请求
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictProvider(true))

//通过使用EvictDynamicKey，接收到Observable时，驱逐该DynamicKey("actives", "page1")下所有缓存数据并重新请求
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKey(true))

//通过使用EvictDynamicKey，接收到Observable时，驱逐该DynamicKeyGroup("actives", "page1")下所有缓存数据并重新请求
getMocksFilteredPaginateInvalidate(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKeyGroup(true))
```

正如你所看到的，使用“DynamicKey”或“DynamicKeyGroup”以及“EvictProvider”类的重点就是在根据不同范围下，驱逐缓存数据对象。

上述示例代码中展示了方法接收“EvictProvider”的参数，以及EvictProvider的子类DynamicKey、DynamicKeyGroup，保证更详细的数据分类和筛选，并进行缓存。

上述代码中，我已经做到了这一点，您总是可以通过自己的筛选，将数据的key类别缩小到你真正需要驱逐的类型。对于最后一个例子，我将在实际产品代码中使用“EvictDynamicKey”，因为这样我就可以对已过滤的项目进行分页，并将其按过滤器排除，例如通过刷新来触发。

这里还有完整的例子 [Android and Java projects](https://github.com/VictorAlbertos/RxCacheSamples).

## <h2 id="5">Actionable RxCache API</h2>

**限制：目前actionable的API仅支持Observable的数据类型。**

这个actionable的API提供了一种Application执行文件写入操作的简单方法。 尽管使用RxCache经典的api也可以实现写入操作，但经典的api有着复杂性且容易出错。实际上，[Actions](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/ActionsList.java)类是围绕经典api的进行了一层包装。

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

配置完成后，为每个Provider添加注解 [@Actionable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Actionable.java) `annotation` 

编译器会生成一个新的类，该类与接口名称相同，但是附加了一个“Actionable”后缀，并暴露出和该接口同样多的方法

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

请注意，Observable内的值必须是“List”类型，否则将抛出异常。

这样上面的RxProviders接口将会在生成的“RxProvidersActionable”类中暴露出下面的方法：

```java
RxProvidersActionable.mocks(RxProviders proxy);
RxProvidersActionable.mocksDynamicKey(RxProviders proxy, DynamicKey dynamicKey);
RxProvidersActionable.mocksDynamicKeyGroup(RxProviders proxy, DynamicKeyGroup dynamicKeyGroup);
```

这些方法返回“Actions”类的一个实例，现在你已经可以尝试使用每个可用的写操作 [Actions](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/ActionsList.java) .建议您浏览[ActionsTest](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/test/java/io/rx_cache/internal/ActionsListTest.java)类，以查看哪些操作适合更适合你的现实需求。

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

之前的每个Action只有在composed的observable接收到subscribe之后才会执行。

## <h2 id="6">高级选项</h2>

## <h3 id="6.1">数据迁移</h3>

RxCache提供了一种处理版本之间缓存数据迁移的简单方式。

> 简单来说，最新的版本中某个接口返回值类型内部发生了改变,从而获取数据的方式发生了改变,但是存储在本地的数据,是未改变的版本,这样在反序列化时就可能发生错误,为了规避这个风险,作者就加入了数据迁移的功能

您需要为您的Provider接口添加注解 [@SchemeMigration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/SchemeMigration.java). 这个注解接受一个数组 [@Migration](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Migration.java) ，反过来，Migration注释同时接受一个版本号和一个Classes的数组，这些数组将从持久层中删除。

```java
@SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock2.class}),
            @Migration(version = 3, evictClasses = {Mock3.class})
    })
interface Providers {}
```

只有当RxCache使用的Class类中数据结构发生了改变，才需要添加新的迁移注解。
> 比如说，您的缓存数据User中有 int userId这个属性，新的版本中变成了 long userId,这样缓存数据的反序列化就会出现问题，因此需要配置迁移注解

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

## <h3 id="6.2">数据加密</h3>

RxCache提供了一种加密数据的简单机制。

您需要为您的Provider接口添加注解[@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java). 这个`annotation`接受一个字符串作为加密/解密数据所必需的`key`。 但是，您需要使用[@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java)对Provider的缓存进行注解，以便缓存数据加密。 如果没有设置[@Encrypt](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Encrypt.java)，则不会进行加密。

**重要提示：**如果提供的“key”值 [@EncryptKey](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/EncryptKey.java) 在编译期间进行了修改，那么以前的缓存数据将无法被RxCache驱逐/获取。

```java
@EncryptKey("myStrongKey-1234")
interface Providers {
        @Encrypt
        Observable<List<Mock>> getMocksEncrypted(Observable<List<Mock>> oMocks);

        Observable<List<Mock>> getMocksNotEncrypted(Observable<List<Mock>> oMocks);
}
```
## <h3 id="6.3">常规配置</h3>

RxCache允许在构建Provider实例时设置某些参数：

### <h4 id="6.3.1">配置要保留的数据的大小限制（以兆字节为单位）</h4>

默认情况下，RxCache将限制设置为100M，但您可以在构建Provider实例时调用setMaxMBPersistenceCache方法来更改此值。

```java
new RxCache.Builder()
            .setMaxMBPersistenceCache(maxMgPersistenceCache)
            .persistence(cacheDir)
            .using(Providers.class);
```

当达到此限制时，RxCache将无法继续缓存数据。 这就是为何当缓存数据容量即将达到阈值时，RxCache有一个自动化的过程来驱逐任何记录，即使没有满足失效时间的缓存数据也被驱逐。

唯一的例外是，当您的Provider的某方法用[@Expirable](https://github.com/VictorAlbertos/RxCache/blob/master/runtime/src/main/java/io/rx_cache/Expirable.java) 注解注释，并将其值设置为false将会被保存，而不会被RxCache自动化驱逐。

```java
interface Providers {
    //即使缓存数据达到阈值，也不会被RxCache自动驱逐
    @Expirable(false)
    Observable<List<Mock>> getMocksNotExpirable(Observable<List<Mock>> oMocks);
}
```

### <h4 id="6.3.2">如果未加载到数据，使用过期的缓存数据</h4>

默认情况下，如果缓存的数据已过期并且observable loader返回的数据为空，RxCache将抛出RuntimeException异常。

您可以修改此行为，允许RxCache在这种情况下提供被驱逐的数据，使用方式很简单，通过将useExpiredDataIfLoaderNotAvailable的值设置为true：

```java
new RxCache.Builder()
            .useExpiredDataIfLoaderNotAvailable(true)   //RxCache提供被驱逐的数据
            .persistence(cacheDir)
            .using(Providers.class);
```

## <h3 id="6.4">Android注意事项</h3>

要构建由RxCache提供的接口实例，您需要提供对文件系统的引用。 在Android上，您可以从[Application](http://developer.android.com/intl/es/reference/android/app/Application.html)类获取文件引用调用getFilesDir（）。

此外，建议您在应用程序的整个生命周期中使用此Android应用程序类来提供RxCache的唯一实例(全局单例)。

为了在子线程上执行Observable，并通过主UI线程上的onNext发出结果，您应该使用[RxAndroid](https://github.com/ReactiveX/RxAndroid)提供的内置方法。

> 即 observable.subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe();

你可以查看Demo： [Android example](https://github.com/VictorAlbertos/RxCacheSamples/tree/master/sample_android)

## <h3 id="6.5">和Retrofit搭配使用</h3>

RxCache和Retrofit完美搭配，两者配合可以实现从始至终的自动管理缓存数据库。
您可以检查RxCache和Retrofit的一个[示例](https://github.com/VictorAlbertos/RxCacheSamples/blob/master/sample_data/src/main/java/sample_data/Repository.java)。


## <h2 id="7">其他</h2>

## <h3 id="7.1">RxCache原理</h3>

RxCache的数据来源取决于下面三个数据层中某一层：

*内存层 - >由[Apache ReferenceMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/ReferenceMap.html)提供支持。
*持久层 - > RxCache内部使用[Jolyglot](https://github.com/VictorAlbertos/Jolyglot)来对对象进行序列化和反序列化。
*加载器层（由客户端库提供的Observable请求，比如网络请求）

*如果请求的数据在内存中，并且尚未过期，则从内存中获取。
*否则请求的数据在持久层中，并且尚未过期，则从持久层获取。
*否则从加载器层请求获取数据。

## <h3 id="7.2">代码混淆</h3>

```
-dontwarn io.rx_cache.internal.**
-keepclassmembers enum io.rx_cache.Source { *; }
```


## <h3 id="7.3">关于作者</h3>

**Víctor Albertos**

* <https://twitter.com/_victorAlbertos>
* <https://www.linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

## <h3 id="7.4">RxCache Swift版本：</h3>
[RxCache](https://github.com/VictorAlbertos/RxSCache): Reactive caching library for Swift.

## <h3 id="7.5">作者其它使用RxJava的库:</h3>
* [Mockery](https://github.com/VictorAlbertos/Mockery): Android and Java library for mocking and testing networking layers with built-in support for Retrofit.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 
* [RxFcm](https://github.com/VictorAlbertos/RxFcm): RxJava extension for Android Firebase Cloud Messaging (aka fcm).
* [RxSocialConnect](https://github.com/VictorAlbertos/RxSocialConnect-Android): OAuth RxJava extension for Android.

## <h2 id="8">关于中文文档</h2>

### 翻译

* 翻译：[qingmei2](https://github.com/qingmei2) 

### 参考

* [《你不知道的Retrofit缓存库RxCache》](http://www.jianshu.com/p/b58ef6b0624b)by [@JessYan](https://github.com/JessYanCoding):讲述了RxCache的使用，以及相关功能的原理分析。

