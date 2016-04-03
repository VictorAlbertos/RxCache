package io.rx_cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set of migrations to be performed in order to guaranty data integrity between releases if data model changes has been performed.
 * @see Migration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchemeMigration {
    Migration[] value();
}
