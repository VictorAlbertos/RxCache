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


import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
public class ActionsProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private List<ProviderScheme> providerSchemes;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.providerSchemes = new ArrayList<>();
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        providerSchemes.clear();

        for (Element element : roundEnv.getElementsAnnotatedWith(Actionable.class)) {
            try {
                ParseProviderScheme parseProviderScheme = new ParseProviderScheme(element);
                ProviderScheme providerScheme = parseProviderScheme.getProviderScheme();
                providerSchemes.add(providerScheme);
            } catch (ParseProviderScheme.ParseException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.getElement());
                return true;
            }
        }

        if (providerSchemes.isEmpty()) return false;

        GenerateActions generateActions = new GenerateActions(filer, providerSchemes, "");

        try {
            generateActions.generate();
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return false;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet();
        annotations.add(Actionable.class.getCanonicalName());
        return annotations;
    }

}
