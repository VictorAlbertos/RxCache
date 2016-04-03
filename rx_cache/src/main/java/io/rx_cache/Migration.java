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
     * Migration number version.
     * @return
     */
    int version();

    /**
     * Classes to be evicted due to inconsistency properties regarding prior migrations.
     * It means when a new field of a class has been added or deleted.
     * @return
     */
    Class[] evictClasses();
}
