package victoralbertos.io.android;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rx_cache.DynamicKey;
import io.rx_cache.LifeCache;
import rx.Observable;

/**
 * Created by victor on 21/01/16.
 */
public interface RxProviders {
    @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
    Observable<String> getMessage(Observable<String> message, DynamicKey dynamicKey);
    Observable<List<MainActivity.Mock>> getMocksPaginate(Observable<List<MainActivity.Mock>> mocks, DynamicKey page);

    @LifeCache(duration = 1, timeUnit = TimeUnit.MILLISECONDS)
    Observable<List<MainActivity.Mock>> getMocksEphemeralPaginate(Observable<List<MainActivity.Mock>> mocks, DynamicKey page);

}
