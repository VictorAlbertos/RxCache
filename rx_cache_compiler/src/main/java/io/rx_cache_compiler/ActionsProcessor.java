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


import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import io.rx_cache.Actionable;

public class ActionsProcessor extends AbstractProcessor {
    private Messager messager;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
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
            boolean hasDynamicKey = hasDynamicKey(element.toString());
            boolean hasDynamicKeyGroup = hasDynamicKeyGroup(element.toString());
        }

        return false;
    }

    private String parseClassItemName(String methodName) {
        int startPosition = methodName.indexOf("List<")+5;
        int endPosition = methodName.indexOf(">>");
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
