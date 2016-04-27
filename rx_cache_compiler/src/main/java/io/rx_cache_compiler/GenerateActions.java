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

import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.internal.RxCache;
import io.rx_cache.internal.actions.Actions;

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
        ParameterizedTypeName action = ParameterizedTypeName.get(ClassName.get("io.rx_cache.internal.actions", "Actions"), type);
        ParameterizedTypeName list = ParameterizedTypeName.get(ClassName.get("java.util", "List"), type);
        ParameterizedTypeName observable = ParameterizedTypeName.get(ClassName.get("rx", "Observable"), list);

        ParameterizedTypeName evict = ParameterizedTypeName.get(ClassName.get(Actions.Evict.class), type);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(action);

        //final ProvidersRxCache rxProviders = (ProvidersRxCache) RxCache.retainedProxy();
        ClassName proxyClass = ClassName.get(providerScheme.getPackageNameOwner(), providerScheme.getSimpleNameOwner());
        builder.addStatement("final $T rxProviders = ($T) $T.retainedProxy()", proxyClass, proxyClass, RxCache.class);

        //Actions.Evict<Mock> evict = new Actions.Evict<Mock>() {
        //  @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {
        //      return null;
        //  }
        //}

        builder.beginControlFlow("$T evict = new $T()", evict, evict)
                .beginControlFlow("@Override public $T call($T elements)", observable, observable)
                .addStatement("return null")
                .endControlFlow()
                .endControlFlow(";");

        if (providerScheme.hasDynamicKey()) {
            builder.addParameter(DynamicKey.class, "dynamicKey");
        } else if (providerScheme.hasDynamicKeyGroup()) {
            builder.addParameter(DynamicKeyGroup.class, "dynamicKeyGroup");
        }


        builder.addStatement("new Actions<>(evict, oCache)");

        return builder.build();
    }
}
