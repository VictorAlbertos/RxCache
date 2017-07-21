/*
 * Copyright 2015 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rx_cache2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache methods can be annotated with this annotation to provide the unique (provider) key that is
 * used to cache the data in the memory. Without this annotation the method name will be used
 * instead. The provider key is not dynamic and does not change at runtime. Using this annotation is
 * required when using proguard, not doing so can lead to strange runtime behaviour as proguard
 * changes method names. In the worst case scenario (which is very likely) it can even happen that
 * two or more different methods (different arguments) end-up having the same name. Which
 * effectively leads to having two or more runtime providers using the same "memory space' in cache.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProviderKey {

    String value();

}
