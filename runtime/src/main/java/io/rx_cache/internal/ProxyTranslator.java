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

import io.rx_cache.ConfigProvider;
import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.Encrypt;
import io.rx_cache.EvictProvider;
import io.rx_cache.Expirable;
import io.rx_cache.LifeCache;
import io.rx_cache.Reply;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;

public final class ProxyTranslator {
  private final Map<Method, ConfigProvider> configProviderMethodCache;

  @Inject ProxyTranslator() {
    configProviderMethodCache = new HashMap<>();
  }

  ConfigProvider processMethod(Method method, Object[] objectsMethod) {
    ConfigProvider prev = loadConfigProviderMethod(method);

    ConfigProvider configProvider = new ConfigProvider(prev.getProviderKey(),
        null, prev.getLifeTimeMillis(), prev.requiredDetailedResponse(), prev.isExpirable(),
        prev.isEncrypted(), getDynamicKey(method, objectsMethod),
        getDynamicKeyGroup(method, objectsMethod),
        getLoaderObservable(method, objectsMethod),
        evictProvider(method, objectsMethod));

    return configProvider;
  }

  private String getProviderKey(Method method) {
    return method.getName();
  }

  private String getDynamicKey(Method method, Object[] objectsMethod) {
    DynamicKey dynamicKey = getObjectFromMethodParam(method, DynamicKey.class, objectsMethod);
    if (dynamicKey != null) return dynamicKey.getDynamicKey().toString();

    DynamicKeyGroup dynamicKeyGroup =
        getObjectFromMethodParam(method, DynamicKeyGroup.class, objectsMethod);
    if (dynamicKeyGroup != null) return dynamicKeyGroup.getDynamicKey().toString();

    return "";
  }

  private String getDynamicKeyGroup(Method method, Object[] objectsMethod) {
    DynamicKeyGroup dynamicKeyGroup =
        getObjectFromMethodParam(method, DynamicKeyGroup.class, objectsMethod);
    return dynamicKeyGroup != null ? dynamicKeyGroup.getGroup().toString() : "";
  }

  private Observable getLoaderObservable(Method method, Object[] objectsMethod) {
    Observable observable = getObjectFromMethodParam(method, Observable.class, objectsMethod);
    if (observable != null) return observable;

    String errorMessage = method.getName() + Locale.NOT_OBSERVABLE_LOADER_FOUND;
    throw new IllegalArgumentException(errorMessage);
  }

  private Long getLifeTimeCache(Method method) {
    LifeCache lifeCache = method.getAnnotation(LifeCache.class);
    if (lifeCache == null) return null;
    return lifeCache.timeUnit().toMillis(lifeCache.duration());
  }

  private boolean getExpirable(Method method) {
    Expirable expirable = method.getAnnotation(Expirable.class);
    if (expirable != null) return expirable.value();
    return true;
  }

  private boolean isEncrypted(Method method) {
    Encrypt encrypt = method.getAnnotation(Encrypt.class);
    if (encrypt != null) return true;
    return false;
  }

  private boolean requiredDetailResponse(Method method) {
    if (method.getReturnType() != Observable.class) {
      String errorMessage = method.getName() + Locale.INVALID_RETURN_TYPE;
      throw new IllegalArgumentException(errorMessage);
    }

    return method.getGenericReturnType().toString().contains(Reply.class.getName());
  }

  private EvictProvider evictProvider(Method method, Object[] objectsMethod) {
    EvictProvider evictProvider =
        getObjectFromMethodParam(method, EvictProvider.class, objectsMethod);
    if (evictProvider != null) {
      return evictProvider;
    } else {
      return new EvictProvider(false);
    }
  }

  private <T> T getObjectFromMethodParam(Method method, Class<T> expectedClass,
      Object[] objectsMethod) {
    int countSameObjectsType = 0;
    T expectedObject = null;

    for (Object objectParam : objectsMethod) {
      if (expectedClass.isAssignableFrom(objectParam.getClass())) {
        expectedObject = (T) objectParam;
        countSameObjectsType++;
      }
    }

    if (countSameObjectsType > 1) {
      String errorMessage =
          method.getName() + Locale.JUST_ONE_INSTANCE + expectedObject.getClass().getSimpleName();
      throw new IllegalArgumentException(errorMessage);
    }

    return expectedObject;
  }

  private ConfigProvider loadConfigProviderMethod(Method method) {
    ConfigProvider result;
    synchronized (configProviderMethodCache) {
      result = configProviderMethodCache.get(method);
      if (result == null) {
        result = new ConfigProvider(getProviderKey(method),
            null, getLifeTimeCache(method),
            requiredDetailResponse(method), getExpirable(method), isEncrypted(method),
            null, null, null, null);
        configProviderMethodCache.put(method, result);
      }
    }
    return result;
  }
}
