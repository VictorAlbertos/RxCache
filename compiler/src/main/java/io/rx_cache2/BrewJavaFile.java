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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.reactivex.Observable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;

final class BrewJavaFile {

    JavaFile from(ProvidersClass providersClass) throws IOException {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        for (ProvidersClass.Method method : providersClass.methods) {
            methodSpecs.add(getActionProvider(providersClass.className, method));
        }

        TypeSpec typeSpec = classProviders(providersClass.className, methodSpecs);

        return JavaFile.builder(providersClass.className.packageName(), typeSpec)
                .build();
    }

    private MethodSpec getActionProvider(ClassName providersClassName, ProvidersClass.Method method) {
        String methodName = method.name;
        TypeName list = ClassName.get(method.enclosingTypeObservable);
        TypeName enclosingTypeList = ClassName
                .get(method.enclosingTypeObservable.getTypeArguments().get(0));
        String enclosingTypeListName = enclosingTypeList.toString();

        ParameterizedTypeName action =
                ParameterizedTypeName.get(ClassName.get(ActionsList.class), enclosingTypeList);

        ParameterizedTypeName evict =
                ParameterizedTypeName.get(ClassName.get(ActionsList.Evict.class), enclosingTypeList);

        ParameterizedTypeName arrayList =
                ParameterizedTypeName.get(ClassName.get(ArrayList.class), enclosingTypeList);

        ParameterizedTypeName observable =
                ParameterizedTypeName.get(ClassName.get(Observable.class), list);

        ParameterSpec rxProvidersInstance = ParameterSpec
                .builder(providersClassName, "proxy")
                .addModifiers(Modifier.FINAL)
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(rxProvidersInstance)
                .returns(action);

        if (method.hasDynamicKey) {
            builder.addParameter(DynamicKey.class, "dynamicKey", Modifier.FINAL);
        } else if (method.hasDynamicKeyGroup) {
            builder.addParameter(DynamicKeyGroup.class, "dynamicKeyGroup", Modifier.FINAL);
        }

        builder.beginControlFlow("$T evict = new $T()", evict, evict)
                .beginControlFlow("@Override public $T call($T elements)", observable, observable);
        if (method.hasDynamicKey) {
            setReturnEvictForEvictDynamicKey(builder, methodName);
        } else if (method.hasDynamicKeyGroup) {
            setReturnEvictForEvictDynamicKeyGroup(builder, methodName);
        } else {
            setReturnEvictForEvictProvider(builder, methodName);
        }
        builder.endControlFlow().endControlFlow(";");

        if (method.hasDynamicKey) {
            setCacheForEvictDynamicKey(builder, observable, arrayList, methodName, enclosingTypeListName);
        } else if (method.hasDynamicKeyGroup) {
            setCacheForEvictDynamicKeyGroup(builder, observable, arrayList, methodName, enclosingTypeListName);
        } else {
            setCacheForEvictProvider(builder, observable, arrayList, methodName, enclosingTypeListName);
        }

        builder.addStatement("return ActionsList.with(evict, oCache)");

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


    private TypeSpec classProviders(ClassName className, List<MethodSpec> methodSpecs) {
        return TypeSpec.classBuilder(className.simpleName() + "Actionable")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", ActionsProcessor.class.getCanonicalName())
                        .addMember("comments", "$S", "Generated code from RxCache. Don't modify. Or modify. It doesn't matter.")
                        .build())
                .addMethods(methodSpecs)
                .build();
    }

}
