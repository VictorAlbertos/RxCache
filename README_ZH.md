# RxCache使用教程

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
//驱逐所有morks
getMocksEvictProvider(oMocks, new EvictProvider(true))
//这一行抛出一个异常IllegalArgumentException:"EvictDynamicKey已经提供但是却少DynamicKey"
getMocksEvictProvider(oMocks, new EvictDynamicKey(true))
```

### List 过滤

List 过滤不驱逐:
```java
Observable<List<Mock>> getMocksFiltered(Observable<List<Mock>> oMocks, DynamicKey filter);
```


List 过滤驱逐:
```java
Observable<List<Mock>> getMocksFilteredEvict(Observable<List<Mock>> oMocks, DynamicKey filter, EvictProvider evictDynamicKey);
```

> 运行时使用：

```java
// 驱逐所有 mocks使用EvictProvider
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictProvider(true))
//一个过滤器使用EvictDynamicKey的驱逐
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKey(true))

//这一行抛出异常 IllegalArgumentException: "EvictDynamicKeyGroup提供 但是却少EvictDynamicKey"
getMocksFilteredEvict(oMocks, new DynamicKey("actives"), new EvictDynamicKeyGroup(true))
```		
		
###List分页带过滤器

List分页带过滤器不驱逐数据:
```java
Observable<List<Mock>> getMocksFilteredPaginate(Observable<List<Mock>> oMocks, DynamicKey filterAndPage);
```


List分页带过滤器驱逐：
```java
Observable<List<Mock>> getMocksFilteredPaginateEvict(Observable<List<Mock>> oMocks, DynamicKeyGroup filterAndPage, EvictProvider evictProvider);
```

> 运行时使用:

```java
// 驱逐所有的 mocks 使用 EvictProvider
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictProvider(true))

//驱逐所有的morks一个过滤器使用evictdynamickey页
getMocksFilteredPaginateEvict(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKey(true))

//驱逐一个过滤器使用EvictDynamicKeyGroup的一页morks
getMocksFilteredPaginateInvalidate(oMocks, new DynamicKeyGroup("actives", "page1"), new EvictDynamicKeyGroup(true))
```

正如你可能已经注意到，使用`DynamicKey`或`DynamicKeyGroup`用`Evict`类一起整点驱逐对象时，有几个领域发挥。

上面的例子，其方法签名声明供应商接受` EvictProvider `为了能够具体的更多细节的类型在运行时` EvictProvider `。

但我这样做，是出于演示的目的，你总是应该缩小你的方法签名驱逐类，你真正需要的类型。对于最后一个例子，我在生产代码使用`EvictDynamicKey`，因为这样我就能够分页经过滤项，并驱逐他们每它的过滤，通过拉触发刷新实例。

	
不过，也有完整的例子 [RxCacheSamples](https://github.com/VictorAlbertos/RxCacheSamples).			

## 混淆配置
```
-dontwarn io.rx_cache.internal.**
```

