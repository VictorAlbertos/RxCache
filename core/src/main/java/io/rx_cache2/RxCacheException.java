package io.rx_cache2;

/**
 * Exception thrown by RxCache when some error happens.
 */
public final class RxCacheException extends RuntimeException {

  public RxCacheException(String message) {
    super(message);
  }

  public RxCacheException(String message, Throwable exception) {
    super(message, exception);
  }
}
