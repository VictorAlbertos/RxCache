package io.rx_cache.internal.cache;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import io.rx_cache.Persistence;
import io.rx_cache.internal.Memory;

public class GetDeepCopy extends Action {

    @Inject public GetDeepCopy(Memory memory, Persistence persistence) {
        super(memory, persistence);
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
        Type typeCollection = $Gson$Types.newParameterizedTypeWithOwner(null, classData, classItemCollection);
        String dataString = new Gson().toJson(data);

        return new Gson().fromJson(dataString, typeCollection);
    }

    private <T> T getDeepCopyArray(T data) {
        T[] array = (T[]) data;
        if (array.length == 0) return data;

        Class classItemArray = array[0].getClass();
        Type typeRecord = $Gson$Types.arrayOf(classItemArray);
        String dataString = new Gson().toJson(data);

        return new Gson().fromJson(dataString, typeRecord);
    }

    private <T, K, V> T getDeepCopyMap(T data) {
        Map<K,V> map = (Map<K,V>) data;
        if (map.isEmpty()) return data;

        Class classData = data.getClass();
        Class classValueMap = map.values().toArray()[0].getClass();
        Class classKeyMap = map.keySet().toArray()[0].getClass();
        Type typeMap = $Gson$Types.newParameterizedTypeWithOwner(null, classData, classKeyMap, classValueMap);
        String dataString = new Gson().toJson(data);

        return new Gson().fromJson(dataString, typeMap);
    }

    private <T> T getDeepCopyObject(T data) {
        if (data == null) return data;

        Class classData = data.getClass();
        Type type = $Gson$Types.newParameterizedTypeWithOwner(null, classData);
        String dataString = new Gson().toJson(data);

        return new Gson().fromJson(dataString, type);
    }
}
