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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.rx_cache.Record;

final class DiskRecord<T> extends Record<T> {
    private final String dataClassName, dataCollectionClassName, dataKeyMapClassName;

    DiskRecord(Record<T> record) {
        this(record.getData());
    }

    DiskRecord(T data) {
        super(data);

        boolean isList = Collection.class.isAssignableFrom(data.getClass());
        boolean isArray = data.getClass().isArray();
        boolean isMap = Map.class.isAssignableFrom(data.getClass());

        if (isList) {
            dataKeyMapClassName = null;
            List list = (List) data;
            if (list.size() > 0) {
                dataCollectionClassName = data.getClass().getName();
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
                Map.Entry<Object, Object>  object = (Map.Entry<Object, Object>) map.entrySet().iterator().next();
                dataClassName = object.getValue().getClass().getName();
                dataKeyMapClassName = object.getKey().getClass().getName();
                dataCollectionClassName = data.getClass().getName();
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

    String getDataClassName() {
        return dataClassName;
    }

    String getDataCollectionClassName() {
        return dataCollectionClassName;
    }

    String getDataKeyMapClassName() {
        return dataKeyMapClassName;
    }
}
