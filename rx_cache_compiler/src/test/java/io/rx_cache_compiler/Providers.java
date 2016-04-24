package io.rx_cache_compiler;

import java.util.List;

import io.rx_cache.Actionable;
import rx.Observable;

/**
 * Created by victor on 24/04/16.
 */
public interface Providers {
    @Actionable
    Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);
}
