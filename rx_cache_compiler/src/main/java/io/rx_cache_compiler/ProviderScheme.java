/*
 * Copyright 2016 Victor Albertos
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

package io.rx_cache_compiler;

class ProviderScheme {
    private final String fullQualifiedNameOwner, nameMethod, fullQualifiedNameTypeList;
    private final boolean hasDynamicKey, hasDynamicKeyGroup;

    public ProviderScheme(String fullQualifiedNameOwner, String nameMethod, String fullQualifiedNameTypeList, boolean hasDynamicKey, boolean hasDynamicKeyGroup) {
        this.fullQualifiedNameOwner = fullQualifiedNameOwner;
        this.nameMethod = nameMethod;
        this.fullQualifiedNameTypeList = fullQualifiedNameTypeList;
        this.hasDynamicKey = hasDynamicKey;
        this.hasDynamicKeyGroup = hasDynamicKeyGroup;
    }

    public String getSimpleNameOwner() {
        return fullQualifiedNameOwner.substring(fullQualifiedNameOwner.lastIndexOf(".") + 1);
    }

    public String getPackageNameOwner() {
        return fullQualifiedNameOwner.substring(0, fullQualifiedNameOwner.lastIndexOf("."));
    }

    public String getNameMethod() {
        return nameMethod;
    }

    public String getSimpleNameTypeList() {
        return fullQualifiedNameTypeList.substring(fullQualifiedNameTypeList.lastIndexOf(".") + 1);
    }

    public String getPackageNameTypeList() {
        return fullQualifiedNameTypeList.substring(0, fullQualifiedNameTypeList.lastIndexOf("."));
    }

    public boolean hasDynamicKey() {
        return hasDynamicKey;
    }

    public boolean hasDynamicKeyGroup() {
        return hasDynamicKeyGroup;
    }

}
