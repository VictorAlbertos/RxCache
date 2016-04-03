import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictProvider;
import io.rx_cache.LifeCache;
import io.rx_cache.Migration;
import io.rx_cache.SchemeMigration;
import io.rx_cache.internal.Mock;
import rx.Observable;

/**
 * Created by victor on 27/02/16.
 */
@SchemeMigration({
    @Migration(version = 1, evictClasses = {
        Mock.class
    })
})
interface Providers {
    Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);

    @LifeCache(duration = 5, timeUnit = TimeUnit.MINUTES)
    Observable<List<Mock>> getMocksWith5MinutesLifeTime(Observable<List<Mock>> oMocks);

    Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks, EvictProvider evictProvider);

    Observable<List<Mock>> getMocksPaginate(Observable<List<Mock>> oMocks, DynamicKey page);

    Observable<List<Mock>> getMocksPaginateEvictPerPage(Observable<List<Mock>> oMocks, DynamicKey page, EvictDynamicKey evictPage);

    Observable<List<Mock>> getMocksPaginateWithFiltersEvictingPerFilter(Observable<List<Mock>> oMocks, DynamicKeyGroup filterPage, EvictDynamicKey evictFilter);
}
