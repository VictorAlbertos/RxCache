package victoralbertos.io.android;

import io.rx_cache.Loader;
import rx.Observable;

/**
 * Created by victor on 21/01/16.
 */
public interface RxProviders {
    Observable<String> getMessage(@Loader Observable<String> message);
}
