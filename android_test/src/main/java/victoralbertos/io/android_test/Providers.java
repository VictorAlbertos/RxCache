package victoralbertos.io.android_test;

import io.rx_cache.Loader;
import rx.Observable;

/**
 * Created by victor on 16/01/16.
 */
public interface Providers {
    Observable<String> getStrings(@Loader Observable<String> strings);
}
