package io.rx_cache.internal.actions;

import java.util.List;

import rx.Observable;

/**
 * Created by victor on 26/04/16.
 */
public class Actions {
    private final RxProviders rxProviders;

    public Actions(RxProviders rxProviders) {
        this.rxProviders = rxProviders;
    }

    public Actions$$Internal<Mock> mocks() {
        return new Actions$$Internal<>();
    }

}
