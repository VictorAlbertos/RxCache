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

package io.rx_cache;

/**
 * Wrapper around the actual data in order to know if its life time has been expired
 * @param <T> The actual data
 */
public class Record<T> {
    private Source source;
    private final T data;
    private final long expirationDate;

    public Record(T data, long expirationDate) {
        this.data = data;
        this.expirationDate = expirationDate;
        this.source = Source.MEMORY;
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

    public long getExpirationDate() {
        return expirationDate;
    }
}
