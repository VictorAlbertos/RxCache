package io.rx_cache.internal;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.EvictProvider;
import io.rx_cache.internal.actions.Actions;
import rx.Observable;

/**
 * Created by victor on 26/04/16.
 */
public final class ActionsProviders {

    public static Actions<Mock> mocks() {
        final ProvidersRxCache rxProviders = (ProvidersRxCache) RxCache.retainedProxy();

        Actions.Evict<Mock> evict = new Actions.Evict<Mock>() {
            @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {
                return rxProviders.mocks(elements, new EvictProvider(true));
            }
        };

        Observable<List<Mock>> oCache = rxProviders.mocks(Observable.<List<Mock>>just(new ArrayList<Mock>()), new EvictProvider(false));

        return new Actions<>(evict, oCache);
    }

    public static Actions<Mock> mocksPaginated(final DynamicKey dynamicKey) {
        final ProvidersRxCache rxProviders = (ProvidersRxCache) RxCache.retainedProxy();

        Actions.Evict<Mock> evict = new Actions.Evict<Mock>() {
            @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {
                return rxProviders.mocksPaginated(elements, dynamicKey, new EvictDynamicKey(true));
            }
        };

        Observable<List<Mock>> oCache = rxProviders.mocksPaginated(Observable.<List<Mock>>just(new ArrayList<Mock>()), dynamicKey, new EvictDynamicKey(false));

        return new Actions<>(evict, oCache);
    }

    public static Actions<Mock> mocksPaginatedFiltered(final DynamicKeyGroup dynamicKeyGroup) {
        final ProvidersRxCache rxProviders = (ProvidersRxCache) RxCache.retainedProxy();

        Actions.Evict<Mock> evict = new Actions.Evict<Mock>() {
            @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {
                return rxProviders.mocksPaginatedFiltered(elements, dynamicKeyGroup, new EvictDynamicKeyGroup(true));
            }
        };

        Observable<List<Mock>> oCache = rxProviders.mocksPaginatedFiltered(Observable.<List<Mock>>just(new ArrayList<Mock>()), dynamicKeyGroup, new EvictDynamicKeyGroup(false));

        return new Actions<>(evict, oCache);
    }

    private void ttt() {
        ActionsProviders.mocks()
                .addFirst(new Mock(""))
                .toObservable();

        ActionsProviders.mocksPaginated(new DynamicKey(1))
                .addFirst(new Mock(""))
                .toObservable();


        ActionsProviders.mocksPaginatedFiltered(new DynamicKeyGroup(1, true))
                .addFirst(new Mock(""))
                .toObservable();
    }

}
