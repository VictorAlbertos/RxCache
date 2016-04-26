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


import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import io.rx_cache.Actionable;

@AutoService(Processor.class)
public class ActionsProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private Elements elementsUtils;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementsUtils = processingEnv.getElementUtils();
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Actionable.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                String error = Locale.ONLY_METHODS_CAN_BE_ANNOTATED_WITH + Actionable.class.getSimpleName();
                messager.printMessage(Diagnostic.Kind.ERROR, error, element);
                return true;
            }

            String methodName = element.getSimpleName().toString();
            String classItemName = parseClassItemName(element.toString());

            if (classItemName.equals("")) {
                String error = Locale.ONLY_LIST_IS_SUPPORTED_AS_LOADER + methodName;
                messager.printMessage(Diagnostic.Kind.ERROR, error, element);
                return true;
            }

            boolean hasDynamicKey = hasDynamicKey(element.toString());
            boolean hasDynamicKeyGroup = hasDynamicKeyGroup(element.toString());

            if (hasDynamicKey && hasDynamicKeyGroup) {
                String error = Locale.ONLY_DYNAMIC_KEY_OR_DYNAMIC_KEY_GROUP + methodName;
                messager.printMessage(Diagnostic.Kind.ERROR, error, element);
                return true;
            }

        }

        generateActions();
        return false;
    }

    private static boolean hasBeenCreated;
    private void generateActions() {
        if (hasBeenCreated) return;

        hasBeenCreated = true;

        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("ActionsTest")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();


        JavaFile javaFile = JavaFile.builder("test", helloWorld)
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }


    private String parseClassItemName(String methodName) {
        int startPosition = methodName.indexOf("List<")+5;
        int endPosition = methodName.indexOf(">>");

        if (startPosition  == -1 || endPosition == -1) return "";
        return methodName.substring(startPosition, endPosition);
    }

    private boolean hasDynamicKey(String methodName) {
        return methodName.contains("DynamicKey");
    }

    private boolean hasDynamicKeyGroup(String methodName) {
        return methodName.contains("hasDynamicKeyGroup");
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet();
        annotations.add(Actionable.class.getCanonicalName());
        return annotations;
    }

}
