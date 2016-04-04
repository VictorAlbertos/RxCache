package io.rx_cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A migration configuration class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Migration {
    /**
     * Migration number version. The first migration should start with 1
     * @return
     */
    int version();

    /**
     * Classes to be evicted due to inconsistency properties regarding prior migrations.
     * It means when a new field of a class has been added.
     * Deleting classes or deleting fields of classes would be handle automatically by RxCache.
     * @return
     */
    Class[] evictClasses();
}
