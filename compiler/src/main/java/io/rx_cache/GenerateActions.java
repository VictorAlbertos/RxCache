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

package io.rx_cache;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import io.rx_cache.internal.RxCache;

class GenerateActions {
    private final List<ProviderScheme> providersScheme;
    private final String nameProxyProviders;
    private final Filer filer;

    GenerateActions(Filer filer, List<ProviderScheme> providersScheme, String nameProxyProviders) {
        this.filer = filer;
        this.providersScheme = providersScheme;
        this.nameProxyProviders = nameProxyProviders;
    }

    void generate() throws IOException {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        for (ProviderScheme providerScheme : providersScheme) {
            methodSpecs.add(getActionProvider(providerScheme));
        }

        TypeSpec actionsProviders = TypeSpec.classBuilder("ActionsProviders")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(methodSpecs)
                .build();

        JavaFile javaFile = JavaFile.builder("io.rx_cache", actionsProviders)
                .build();

        javaFile.writeTo(filer);
    }

    private MethodSpec getActionProvider(ProviderScheme providerScheme) {
        String methodName = providerScheme.getNameMethod();

        ClassName type = ClassName.get(providerScheme.getPackageNameTypeList(), providerScheme.getSimpleNameTypeList());
        ParameterizedTypeName action = ParameterizedTypeName.get(ClassName.get("io.rx_cache", "Actions"), type);
        ParameterizedTypeName list = ParameterizedTypeName.get(ClassName.get("java.util", "List"), type);
        ParameterizedTypeName arrayList = ParameterizedTypeName.get(ClassName.get("java.util", "ArrayList"), type);
        ParameterizedTypeName observable = ParameterizedTypeName.get(ClassName.get("rx", "Observable"), list);

        ParameterizedTypeName evict = ParameterizedTypeName.get(ClassName.get(Actions.Evict.class), type);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(action);

        if (providerScheme.hasDynamicKey()) {
            builder.addParameter(DynamicKey.class, "dynamicKey", Modifier.FINAL);
        } else if (providerScheme.hasDynamicKeyGroup()) {
            builder.addParameter(DynamicKeyGroup.class, "dynamicKeyGroup", Modifier.FINAL);
        }

        ClassName proxyClass = ClassName.get(providerScheme.getPackageNameOwner(), providerScheme.getSimpleNameOwner());
        builder.addStatement("final $T proxy = ($T) $T.retainedProxy()", proxyClass, proxyClass, RxCache.class);

        builder.beginControlFlow("$T evict = new $T()", evict, evict)
                .beginControlFlow("@Override public $T call($T elements)", observable, observable);
        if (providerScheme.hasDynamicKey()) {
            setReturnEvictForEvictDynamicKey(builder, methodName);
        } else if (providerScheme.hasDynamicKeyGroup()) {
            setReturnEvictForEvictDynamicKeyGroup(builder, methodName);
        } else {
            setReturnEvictForEvictProvider(builder, methodName);
        }
        builder.endControlFlow()
                .endControlFlow(";");

        if (providerScheme.hasDynamicKey()) {
            setCacheForEvictDynamicKey(builder, observable, arrayList, methodName, providerScheme.getSimpleNameTypeList());
        } else if (providerScheme.hasDynamicKeyGroup()) {
            setCacheForEvictDynamicKeyGroup(builder, observable, arrayList, methodName, providerScheme.getSimpleNameTypeList());
        } else {
            setCacheForEvictProvider(builder, observable, arrayList, methodName, providerScheme.getSimpleNameTypeList());
        }

        builder.addStatement("return Actions.with(evict, oCache)");

        return builder.build();
    }

    private void setReturnEvictForEvictProvider(MethodSpec.Builder builder, String methodName) {
        builder.addStatement("return proxy."+methodName+"(elements, new $T(true))", EvictProvider.class);
    }

    private void setReturnEvictForEvictDynamicKey(MethodSpec.Builder builder, String methodName) {
        builder.addStatement("return proxy."+methodName+"(elements, dynamicKey, new $T(true))", EvictDynamicKey.class);
    }

    private void setReturnEvictForEvictDynamicKeyGroup(MethodSpec.Builder builder, String methodName) {
        builder.addStatement("return proxy."+methodName+"(elements, dynamicKeyGroup, new $T(true))", EvictDynamicKeyGroup.class);
    }

    private void setCacheForEvictProvider(MethodSpec.Builder builder, ParameterizedTypeName observable, ParameterizedTypeName arrayList, String methodName, String typeName) {
        builder.addStatement("$T oCache = proxy."+methodName+"(Observable.<List<"+typeName+">>just(new $T()), new EvictProvider(false))", observable, arrayList);
    }

    private void setCacheForEvictDynamicKey(MethodSpec.Builder builder, ParameterizedTypeName observable, ParameterizedTypeName arrayList, String methodName, String typeName) {
        builder.addStatement("$T oCache = proxy."+methodName+"(Observable.<List<"+typeName+">>just(new $T()), dynamicKey, new EvictDynamicKey(false))", observable, arrayList);
    }

    private void setCacheForEvictDynamicKeyGroup(MethodSpec.Builder builder, ParameterizedTypeName observable, ParameterizedTypeName arrayList, String methodName, String typeName) {
        builder.addStatement("$T oCache = proxy."+methodName+"(Observable.<List<"+typeName+">>just(new $T()), dynamicKeyGroup, new EvictDynamicKeyGroup(false))", observable, arrayList);
    }
}
