package io.rx_cache2;

import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

final class GetProvidersClass {

    ProvidersClass from(Element element) throws ValidationException {
        if (!SuperficialValidation.validateElement(element)) return null;
        if (element.getKind() != ElementKind.INTERFACE) return null;

        ClassName className = ClassName.get((TypeElement) element);
        List<ProvidersClass.Method> methods = getMethods(element);

        return new ProvidersClass(className, element, methods);
    }

    private List<ProvidersClass.Method> getMethods(Element classElement) throws ValidationException {
        List<? extends Element> enclosedElements = classElement.getEnclosedElements();
        List<ProvidersClass.Method> methods = new ArrayList<>();

        for (Element methodElement : enclosedElements) {
            if (!isAnnotatedWithActionable(methodElement)) continue;
            if (methodElement.getKind() != ElementKind.METHOD) continue;

            Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) methodElement;
            String nameMethod = methodSymbol.getSimpleName().toString();

            Type returnType = methodSymbol.getReturnType();

            if (!returnType.tsym.toString()
                    .equals(TypeName.get(Observable.class).toString())) {
                throw new ValidationException(methodSymbol,
                        "Error parsing %s provider. Only Observable<List> type is supported as observable loader", nameMethod);
            }

            Type enclosingTypeObservable = returnType.getTypeArguments().get(0);

            if (!enclosingTypeObservable.tsym.toString()
                    .equals(TypeName.get(List.class).toString())) {
                throw new ValidationException(methodSymbol,
                        "Error parsing %s provider. Only Observable<List> type is supported as observable loader", nameMethod);
            }

            List<Symbol.VarSymbol> params = methodSymbol.getParameters();
            boolean hasEvictProvider = hasEvictProvider(params);
            boolean hasEvictDynamicKey = hasEvictDynamicKey(params);
            boolean hasEvictDynamicKeyGroup = hasEvictDynamicKeyGroup(params);

            if (!hasEvictProvider && !hasEvictDynamicKey && !hasEvictDynamicKeyGroup) {
                throw new ValidationException(methodElement,
                        "Error parsing %s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
            }

            if (hasEvictProvider && hasEvictDynamicKey) {
                throw new ValidationException(methodElement,
                        "Error parsing %s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
            }

            if (hasEvictProvider && hasEvictDynamicKeyGroup) {
                throw new ValidationException(methodElement,
                        "Error parsing %s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
            }

            if (hasEvictDynamicKey && hasEvictDynamicKeyGroup) {
                throw new ValidationException(methodElement,
                        "Error parsing %s provider. The provider requires one evicting argument: EvictProvider, EvictDynamicKey or EvictDynamicKeyGroup", nameMethod);
            }

            boolean hasDynamicKey = hasDynamicKey(params);
            boolean hasDynamicKeyGroup = hasDynamicKeyGroup(params);

            methods.add(new ProvidersClass.Method(nameMethod, methodElement,
                    enclosingTypeObservable, hasDynamicKey, hasDynamicKeyGroup));
        }

        return methods;
    }

    private boolean isAnnotatedWithActionable(Element element) {
        Actionable actionable = element.getAnnotation(Actionable.class);
        return actionable != null;
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

    static class ValidationException extends Exception {
        private final Element element;

        public ValidationException(Element element, String msg, Object... args) {
            super(String.format(msg, args));
            this.element = element;
        }

        public Element getElement() {
            return element;
        }

    }

}
