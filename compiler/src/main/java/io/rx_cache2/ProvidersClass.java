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

package io.rx_cache2;

import com.squareup.javapoet.ClassName;
import com.sun.tools.javac.code.Type;

import java.util.List;

import javax.lang.model.element.Element;

final class ProvidersClass {
    final ClassName className;
    final Element element;
    final List<Method> methods;

    ProvidersClass(ClassName className, Element element,
                   List<Method> methods) {
        this.className = className;
        this.element = element;
        this.methods = methods;
    }

    static class Method {
        final String name;
        final Element element;
        final Type enclosingTypeObservable;
        final boolean hasDynamicKey, hasDynamicKeyGroup;

        Method(String name, Element element, Type enclosingTypeObservable,
               boolean hasDynamicKey, boolean hasDynamicKeyGroup) {
            this.name = name;
            this.element = element;
            this.enclosingTypeObservable = enclosingTypeObservable;
            this.hasDynamicKey = hasDynamicKey;
            this.hasDynamicKeyGroup = hasDynamicKeyGroup;
        }

    }
}
