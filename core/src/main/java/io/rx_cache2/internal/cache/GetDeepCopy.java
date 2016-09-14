package io.rx_cache2.internal.cache;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.rx_cache2.internal.Memory;
import io.victoralbertos.jolyglot.JolyglotGenerics;

public final class GetDeepCopy extends Action {
  private final JolyglotGenerics jolyglot;

  @Inject public GetDeepCopy(Memory memory, io.rx_cache2.internal.Persistence persistence, JolyglotGenerics jolyglot) {
    super(memory, persistence);
    this.jolyglot = jolyglot;
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
    if (List.class.isAssignableFrom(classData)) classData = List.class;

    Class classItemCollection = collection.toArray()[0].getClass();
    Type typeCollection = jolyglot.newParameterizedType(classData, classItemCollection);
    String dataString = jolyglot.toJson(data);

    return jolyglot.fromJson(dataString, typeCollection);
  }

  private <T> T getDeepCopyArray(T data) {
    T[] array = (T[]) data;
    if (array.length == 0) return data;

    Class classItemArray = array[0].getClass();
    Type typeRecord = jolyglot.arrayOf(classItemArray);
    String dataString = jolyglot.toJson(data);

    return jolyglot.fromJson(dataString, typeRecord);
  }

  private <T, K, V> T getDeepCopyMap(T data) {
    Map<K, V> map = (Map<K, V>) data;
    if (map.isEmpty()) return data;

    Class classData = Map.class;
    Class classValueMap = map.values().toArray()[0].getClass();
    Class classKeyMap = map.keySet().toArray()[0].getClass();
    Type typeMap = jolyglot.newParameterizedType(classData, classKeyMap, classValueMap);
    String dataString = jolyglot.toJson(data);

    return jolyglot.fromJson(dataString, typeMap);
  }

  private <T> T getDeepCopyObject(T data) {
    if (data == null) return data;

    Class classData = data.getClass();
    Type type = jolyglot.newParameterizedType(classData);
    String dataString = jolyglot.toJson(data);

    return jolyglot.fromJson(dataString, type);
  }
}
