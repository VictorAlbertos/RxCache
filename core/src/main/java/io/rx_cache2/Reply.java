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

/**
 * Wrapper to provide the Source used for retrieving the actual data, plus, the actual data. You can
 * set this object as return type for your methods if you want to know which was the source for an
 * specific data
 *
 * @param <T> The actual data to be retrieved encapsulated inside a Provider object
 * @see Source
 */
public final class Reply<T> {
  private final T data;
  private final Source source;
  private final boolean isEncrypted;

  public Reply(T data, Source source, boolean isEncrypted) {
    this.data = data;
    this.source = source;
    this.isEncrypted = isEncrypted;
  }

  public T getData() {
    return data;
  }

  public Source getSource() {
    return source;
  }

  public boolean isEncrypted() {
    return isEncrypted;
  }
  
  @Override 
  public String toString() {
    return "Reply{" +
      "data=" + data +
      ", source=" + source +
      ", isEncrypted=" + isEncrypted +
      '}';
  }
}
