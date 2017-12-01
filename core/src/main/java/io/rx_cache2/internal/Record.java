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

package io.rx_cache2.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.rx_cache2.Source;

/**
 * Wrapper around the actual data in order to know if its life time has been expired
 *
 * @param <T> The actual data
 */
public final class Record<T> {
  private Source source;
  private final T data;
  private final long timeAtWhichWasPersisted;
  private final String dataClassName, dataCollectionClassName, dataKeyMapClassName;
  private Boolean expirable;

  //LifeTime requires to be stored to be evicted by EvictExpiredRecordsTask when no life time is available without a config provider
  private Long lifeTime;

  //Required by EvictExpirableRecordsPersistence task
  private transient float sizeOnMb;

  //VisibleForTesting
  Record(T data) {
    this(data, true, null);
  }

  public Record() {
    data = null;
    timeAtWhichWasPersisted = 0;
    dataClassName = null;
    dataCollectionClassName = null;
    dataKeyMapClassName = null;
    expirable = true;
  }

  public Record(T data, Boolean expirable, Long lifeTime) {
    this.data = data;
    this.expirable = expirable;
    this.lifeTime = lifeTime;
    this.timeAtWhichWasPersisted = System.currentTimeMillis();
    this.source = Source.MEMORY;

    boolean isList = Collection.class.isAssignableFrom(data.getClass());
    boolean isArray = data.getClass().isArray();
    boolean isMap = Map.class.isAssignableFrom(data.getClass());

    if (isList) {
      dataKeyMapClassName = null;
      List list = (List) data;
      if (list.size() > 0) {
        dataCollectionClassName = List.class.getName();
        ;
        dataClassName = list.get(0).getClass().getName();
      } else {
        dataClassName = null;
        dataCollectionClassName = null;
      }
    } else if (isArray) {
      dataKeyMapClassName = null;
      Object[] array = (Object[]) data;
      if (array.length > 0) {
        dataClassName = (array)[0].getClass().getName();
        dataCollectionClassName = data.getClass().getName();
      } else {
        dataClassName = null;
        dataCollectionClassName = null;
      }
    } else if (isMap) {
      Map map = ((Map) data);
      if (map.size() > 0) {
        dataCollectionClassName = Map.class.getName();

        final Iterator<Map.Entry> iterator = ((Set<Entry>) map.entrySet()).iterator();

        Map.Entry firstEntry = iterator.next();
        Class valueClass = firstEntry.getValue().getClass();
        Class keyClass = firstEntry.getKey().getClass();

        //makes sure, that all the keys and values are of the same type. E.g. values are different for multi-level map.
        while (iterator.hasNext() || valueClass == null && keyClass == null) {
          final Entry next = iterator.next();

          if (keyClass != null && keyClass != next.getKey().getClass()) keyClass = null;
          if (valueClass != null && valueClass != next.getValue().getClass()) valueClass = null;
        }

        dataClassName = valueClass != null ? valueClass.getName() : null;
        dataKeyMapClassName = keyClass != null ? keyClass.getName() : null;
      } else {
        dataClassName = null;
        dataCollectionClassName = null;
        dataKeyMapClassName = null;
      }
    } else {
      dataKeyMapClassName = null;
      dataClassName = data.getClass().getName();
      dataCollectionClassName = null;
    }
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public T getData() {
    return data;
  }

  public long getTimeAtWhichWasPersisted() {
    return timeAtWhichWasPersisted;
  }

  public Long getLifeTime() {
    return lifeTime;
  }

  public void setLifeTime(Long lifeTime) {
    this.lifeTime = lifeTime;
  }

  public float getSizeOnMb() {
    return sizeOnMb;
  }

  public void setSizeOnMb(float sizeOnMb) {
    this.sizeOnMb = sizeOnMb;
  }

  public String getDataClassName() {
    return dataClassName;
  }

  public String getDataCollectionClassName() {
    return dataCollectionClassName;
  }

  public String getDataKeyMapClassName() {
    return dataKeyMapClassName;
  }

  public Boolean getExpirable() {
    return expirable;
  }

  public void setExpirable(Boolean expirable) {
    this.expirable = expirable;
  }
}
