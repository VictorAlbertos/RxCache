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

package io.rx_cache.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;

import io.rx_cache.DynamicKey;
import io.rx_cache.InvalidateCache;
import io.rx_cache.Invalidator;
import io.rx_cache.LifeCache;
import io.rx_cache.Loader;
import io.rx_cache.Reply;
import rx.Observable;

final class ProxyTranslator {

    @Inject ProxyTranslator() {}

    Translation processMethod(Method method, Object[] objectsMethod) {
        return new Translator(method, objectsMethod).getTranslation();
    }

    private static class Translator {
        protected final Method method;
        protected final Object[] objectsMethod;

        public Translator(Method method, Object[] objectsMethod) {
            this.method = method;
            this.objectsMethod = objectsMethod;
        }

        Translation getTranslation() {
            return new Translation(getKey(), getDynamicKey(), getLoaderObservable(), getLifeTimeCache(), requiredDetailResponse(), invalidator());
        }

        protected String getKey() {
            return method.getName();
        }

        protected String getDynamicKey() {
            Object page = annotationConverter(DynamicKey.class, Object.class);
            return page != null ? page.toString() : "";
        }

        protected Observable getLoaderObservable() {
            Observable observable = annotationConverter(Loader.class, Observable.class);
            if (observable != null) return observable;

            String errorMessage = method.getName() + Locale.NOT_OBSERVABLE_LOADER_FOUND;
            throw new IllegalArgumentException(errorMessage);
        }

        protected long getLifeTimeCache() {
            LifeCache lifeCache = method.getAnnotation(LifeCache.class);
            if (lifeCache == null) return 0;
            return lifeCache.timeUnit().toMillis(lifeCache.duration());
        }

        protected boolean requiredDetailResponse() {
            if (method.getReturnType() != Observable.class) {
                String errorMessage = method.getName() + Locale.INVALID_RETURN_TYPE;
                throw new IllegalArgumentException(errorMessage);
            }

            return method.getGenericReturnType().toString().contains(Reply.class.getName());
        }

        protected Invalidator invalidator() {
            Invalidator invalidateCache = annotationConverter(InvalidateCache.class, Invalidator.class);
            if (invalidateCache != null) return invalidateCache;
            else return new Invalidator() {
                @Override public boolean invalidate() {
                    return false;
                }
            };
        }

        protected <T> T annotationConverter(Class candidate, Class<T> expectedCast) {
            int indexObjectForAnnotation = -1;
            Annotation[][] annotations = method.getParameterAnnotations();

            for (int i = 0; i < annotations.length; i++) {
                Annotation[] annotation = annotations[i];
                if (annotation.length == 0) continue;
                if (annotation[0].annotationType() != candidate) continue;

                if (indexObjectForAnnotation != -1) throw new IllegalArgumentException(Locale.NOT_MORE_THAN_ONE_ANNOTATION_TYPE + candidate.getName());
                else indexObjectForAnnotation = i;
            }

            try {
                return expectedCast.cast(objectsMethod[indexObjectForAnnotation]);
            } catch (Exception ignore) {
                return null;
            }
        }
    }

    final static class Translation {
        private final String key, dynamicKey;
        private final Observable loaderObservable;
        private final long lifeTime;
        private final boolean requiredDetailedResponse;
        private final Invalidator invalidator;

        Translation(String key, String dynamicKey, Observable loaderObservable, long lifeTime, boolean requiredDetailedResponse, Invalidator invalidator) {
            this.key = key;
            this.dynamicKey = dynamicKey;
            this.loaderObservable = loaderObservable;
            this.lifeTime = lifeTime;
            this.invalidator = invalidator;
            this.requiredDetailedResponse = requiredDetailedResponse;
        }

        String getKey() {
            return key;
        }

        public String getDynamicKey() {
            return dynamicKey;
        }

        long getLifeTimeMillis() {
            return lifeTime;
        }

        boolean requiredDetailedResponse() {
            return requiredDetailedResponse;
        }

        Observable getLoaderObservable() {
            return loaderObservable;
        }

        public Invalidator invalidator() {
            return invalidator;
        }
    }
}
