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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public final class ActionsProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private GetProvidersClass getProvidersClass;
    private BrewJavaFile brewJavaFile;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.messager = env.getMessager();
        this.filer = env.getFiler();
        this.getProvidersClass = new GetProvidersClass();
        this.brewJavaFile = new BrewJavaFile();
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            try {
                ProvidersClass testClass = getProvidersClass.from(element);
                if (testClass == null) continue;
                if (testClass.methods.isEmpty()) continue;

                JavaFile javaFile = brewJavaFile.from(testClass);
                javaFile.writeTo(filer);
            } catch (GetProvidersClass.ValidationException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.getElement());
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
            }
        }

        return false;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet();
        annotations.add(Actionable.class.getCanonicalName());
        return annotations;
    }

}
