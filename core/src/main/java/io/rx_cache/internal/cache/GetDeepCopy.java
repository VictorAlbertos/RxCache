package io.rx_cache.internal.cache;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import io.rx_cache.JsonConverter;
import io.rx_cache.internal.Memory;
import io.rx_cache.internal.Persistence;

public class GetDeepCopy extends Action {
    private final JsonConverter jsonConverter;

    @Inject public GetDeepCopy(Memory memory, Persistence persistence, JsonConverter jsonConverter) {
        super(memory, persistence);
        this.jsonConverter = jsonConverter;
    }

    public <T> T deepCopy(T data) {
        try {
            Class classData = data.getClass();
            boolean isCollection = Collection.class.isAssignableFrom(classData);
            boolean isArray = classData.isArray();
            boolean isMap = Map.class.isAssignableFrom(classData);

            if (isCollection) {
                return getDeepCopyCollection(data);
            } else if (isArray) {
                return getDeepCopyArray(data);
            } else if (isMap) {
                return getDeepCopyMap(data);
            } else {
                return getDeepCopyObject(data);
            }
        } catch (Exception ignore) {
            return data;
        }
    }

    private <T> T getDeepCopyCollection(T data) {
        Collection<T> collection = (Collection<T>) data;
        if (collection.isEmpty()) return data;

        Class classData = data.getClass();
        Class classItemCollection = collection.toArray()[0].getClass();
        Type typeCollection = jsonConverter.parameterizedTypeWithOwner(null, classData, classItemCollection);
        String dataString = jsonConverter.toJson(data);

        return jsonConverter.fromJson(dataString, typeCollection);
    }

    private <T> T getDeepCopyArray(T data) {
        T[] array = (T[]) data;
        if (array.length == 0) return data;

        Class classItemArray = array[0].getClass();
        Type typeRecord = jsonConverter.arrayOf(classItemArray);
        String dataString = jsonConverter.toJson(data);

        return jsonConverter.fromJson(dataString, typeRecord);
    }

    private <T, K, V> T getDeepCopyMap(T data) {
        Map<K,V> map = (Map<K,V>) data;
        if (map.isEmpty()) return data;

        Class classData = data.getClass();
        Class classValueMap = map.values().toArray()[0].getClass();
        Class classKeyMap = map.keySet().toArray()[0].getClass();
        Type typeMap = jsonConverter.parameterizedTypeWithOwner(null, classData, classKeyMap, classValueMap);
        String dataString = jsonConverter.toJson(data);

        return jsonConverter.fromJson(dataString, typeMap);
    }

    private <T> T getDeepCopyObject(T data) {
        if (data == null) return data;

        Class classData = data.getClass();
        Type type = jsonConverter.parameterizedTypeWithOwner(null, classData);
        String dataString = jsonConverter.toJson(data);

        return jsonConverter.fromJson(dataString, type);
    }
}
