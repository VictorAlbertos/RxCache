package io.rx_cache2;

/**
 * Exception thrown by RxCache when some error happens.
 */
public final class RxCacheException extends RuntimeException {

  public RxCacheException() {
    super();
  }

  public RxCacheException(String message) {
    super(message);
  }

  public RxCacheException(Throwable throwable) {
    super(throwable);
  }

  public RxCacheException(String message, Throwable exception) {
    super(message, exception);
  }
}
