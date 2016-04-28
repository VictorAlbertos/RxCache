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


import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import io.rx_cache.Actionable;
import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.EvictProvider;

class ParseProviderScheme {
    private final Symbol.MethodSymbol element;

    ParseProviderScheme(Element element) throws ParseException {
        String nameMethod = element.getSimpleName().toString();

        if (element.getKind() != ElementKind.METHOD) {
            throw new ParseException(element, "Error parsing @%s provider. Only methods can be annotated with @%s",
                    nameMethod, Actionable.class.getSimpleName());
        }

        this.element = (Symbol.MethodSymbol) element;
    }

    public ProviderScheme getProviderScheme() throws ParseException {
        String classNameOwner = element.owner.toString();
        String nameMethod = element.getSimpleName().toString();
        String signatureMethod = element.toString();
        List<Symbol.VarSymbol> symbols = element.getParameters();


        String fullQualifiedNameTypeList = fullQualifiedNameTypeList(symbols);
        if (fullQualifiedNameTypeList == null) {
            throw new ParseException(element, "Error parsing @%s provider. Only list is supported as observable loader", nameMethod);
        }

        boolean hasDynamicKey = hasDynamicKey(symbols);
        boolean hasDynamicKeyGroup = hasDynamicKeyGroup(symbols);
        boolean hasEvictProvider = hasEvictProvider(symbols);
        boolean hasEvictDynamicKey = hasEvictDynamicKey(symbols);
        boolean hasEvictDynamicKeyGroup = hasEvictDynamicKeyGroup(symbols);

        if (!hasEvictProvider && !hasEvictDynamicKey && !hasEvictDynamicKeyGroup) {
            throw new ParseException(element, "Error parsing @%s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
        }

        if (hasEvictProvider && hasEvictDynamicKey) {
            throw new ParseException(element, "Error parsing @%s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
        }

        if (hasEvictProvider && hasEvictDynamicKeyGroup) {
            throw new ParseException(element, "Error parsing @%s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
        }

        if (hasEvictDynamicKey && hasEvictDynamicKeyGroup) {
            throw new ParseException(element, "Error parsing @%s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
        }

        ProviderScheme providerScheme = new ProviderScheme(classNameOwner, nameMethod, fullQualifiedNameTypeList, hasDynamicKey, hasDynamicKeyGroup);
        return providerScheme;
    }

    private String fullQualifiedNameTypeList(List<Symbol.VarSymbol> symbols) {
        for (Symbol.VarSymbol symbol: symbols) {
            String nameSymbol = ((Type.ClassType) (symbol.type)).tsym.toString();
            if (nameSymbol.equals(rx.Observable.class.getName())) {
                String fullQualifiedNameTypeList = ((Type.ClassType) (symbol).type).typarams_field.get(0).getTypeArguments().get(0).toString();
                return fullQualifiedNameTypeList;
            }
        }
        return null;
    }

    private boolean hasDynamicKey(List<Symbol.VarSymbol> symbols) {
        return hasSymbol(symbols, DynamicKey.class);
    }

    private boolean hasDynamicKeyGroup(List<Symbol.VarSymbol> symbols) {
        return hasSymbol(symbols, DynamicKeyGroup.class);
    }

    private boolean hasEvictProvider(List<Symbol.VarSymbol> symbols) {
        return hasSymbol(symbols, EvictProvider.class);
    }

    private boolean hasEvictDynamicKey(List<Symbol.VarSymbol> symbols) {
        return hasSymbol(symbols, EvictDynamicKey.class);
    }

    private boolean hasEvictDynamicKeyGroup(List<Symbol.VarSymbol> symbols) {
        return hasSymbol(symbols, EvictDynamicKeyGroup.class);
    }

    private boolean hasSymbol(List<Symbol.VarSymbol> symbols, Class candidateClass) {
        for (Symbol.VarSymbol symbol: symbols) {
            String symbolClassName = symbol.type.toString();
            String candidateClassName = candidateClass.getCanonicalName();
            if (symbolClassName.equals(candidateClassName)) return true;
        }

        return false;
    }

    static class ParseException extends Exception {
        private final Element element;

        public ParseException(Element element, String msg, Object... args) {
            super(String.format(msg, args));
            this.element = element;
        }

        public Element getElement() {
            return element;
        }

    }
}
