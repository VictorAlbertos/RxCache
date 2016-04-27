package victoralbertos.io.android;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rx_cache.Actionable;
import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.EvictProvider;
import io.rx_cache.LifeCache;
import rx.Observable;

/**
 * Created by victor on 21/01/16.
 */
public interface RxProviders {
    @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
    Observable<String> getMessage(Observable<String> message, DynamicKey dynamicKey);
    Observable<List<Mock>> getMocksPaginate(Observable<List<Mock>> mocks, DynamicKey page);

    @LifeCache(duration = 1, timeUnit = TimeUnit.MILLISECONDS)
    Observable<List<Mock>> getMocksEphemeralPaginate(Observable<List<Mock>> mocks, DynamicKey page);

    @Actionable
    Observable<List<Mock>> mocks(Observable<List<Mock>> message, EvictProvider evictProvider);

    @Actionable
    Observable<List<Mock>> mocksDynamicKey(Observable<List<Mock>> message, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);

    @Actionable
    Observable<List<Mock>> mocksDynamicKeyGroup(Observable<List<Mock>> message, DynamicKeyGroup dynamicKey, EvictDynamicKeyGroup evictDynamicKey);
}
