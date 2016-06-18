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

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.rx_cache.JsonConverter;

public class JsonConverterGson implements JsonConverter {

    @Override public String toJson(Object src) throws RuntimeException {
        return new Gson().toJson(src);
    }

    @Override public <T> T fromJson(String json, Class<T> classOfT) throws RuntimeException {
        return new Gson().fromJson(json, classOfT);
    }

    @Override public <T> T fromJson(String json, Type typeOfT) throws RuntimeException {
        return new Gson().fromJson(json, typeOfT);
    }

    @Override public <T> T fromJson(File file, Class<T> classOfT) throws RuntimeException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            T t =  new Gson().fromJson(reader, classOfT);
            reader.close();
            return t;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException i) {}
            }
        }
    }

    @Override public <T> T fromJson(File file, Type typeOfT) throws RuntimeException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            T t =  new Gson().fromJson(reader, typeOfT);
            reader.close();
            return t;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException i) {}
            }
        }
    }

    @Override public GenericArrayType arrayOf(Type componentType) {
        return $Gson$Types.arrayOf(componentType);
    }

    @Override public ParameterizedType parameterizedTypeWithOwner(Type rawType, Type... typeArguments) {
        return $Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments);
    }

}
