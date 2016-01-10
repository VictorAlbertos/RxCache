package io.rx_cache;

/**
 * Allows to clear the cache for an specific key used in a provider
 */
public interface InvalidatorDynamicKey extends Invalidator {
    Object dynamicKey();
}
