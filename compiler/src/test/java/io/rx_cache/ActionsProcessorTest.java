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

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ActionsProcessorTest {

    @Test public void Test_Processor() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("RxProviders", "" +
                "package test;\n" +
                "import java.util.List;\n" +
                "import io.rx_cache.Actionable;\n" +
                "import io.rx_cache.DynamicKey;\n" +
                "import io.rx_cache.DynamicKeyGroup;\n" +
                "import io.rx_cache.EvictDynamicKey;\n" +
                "import io.rx_cache.EvictDynamicKeyGroup;\n" +
                "import io.rx_cache.EvictProvider;\n" +
                "import rx.Observable;\n" +

                "public interface RxProviders {\n" +
                "    @Actionable\n" +
                "    Observable<List<String>> mocks(Observable<List<String>> message, EvictProvider evictProvider);\n" +

                "    @Actionable\n" +
                "    Observable<List<String>> mocksDynamicKey(Observable<List<String>> message, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);\n" +

                "    @Actionable\n" +
                "    Observable<List<String>> mocksDynamicKeyGroup(Observable<List<String>> message, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKey);\n" +
                "}");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/ActionsProviders", "" +
                "package io.rx_cache;\n" +
                "\n" +
                "import io.rx_cache.internal.RxCache;\n" +
                "import java.lang.String;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import rx.Observable;\n" +
                "import test.RxProviders;\n" +
                "\n" +
                "public final class ActionsProviders {\n" +
                "  public static Actions<String> mocks() {\n" +
                "    final RxProviders proxy = (RxProviders) RxCache.retainedProxy();\n" +
                "    Actions.Evict<String> evict = new Actions.Evict<String>() {\n" +
                "      @Override public Observable<List<String>> call(Observable<List<String>> elements) {\n" +
                "        return proxy.mocks(elements, new EvictProvider(true));\n" +
                "      }\n" +
                "    } ;;\n" +
                "    Observable<List<String>> oCache = proxy.mocks(Observable.<List<String>>just(new ArrayList<String>()), new EvictProvider(false));\n" +
                "    return Actions.with(evict, oCache);\n" +
                "  }\n" +
                "\n" +
                "  public static Actions<String> mocksDynamicKey(final DynamicKey dynamicKey) {\n" +
                "    final RxProviders proxy = (RxProviders) RxCache.retainedProxy();\n" +
                "    Actions.Evict<String> evict = new Actions.Evict<String>() {\n" +
                "      @Override public Observable<List<String>> call(Observable<List<String>> elements) {\n" +
                "        return proxy.mocksDynamicKey(elements, dynamicKey, new EvictDynamicKey(true));\n" +
                "      }\n" +
                "    } ;;\n" +
                "    Observable<List<String>> oCache = proxy.mocksDynamicKey(Observable.<List<String>>just(new ArrayList<String>()), dynamicKey, new EvictDynamicKey(false));\n" +
                "    return Actions.with(evict, oCache);\n" +
                "  }\n" +
                "\n" +
                "  public static Actions<String> mocksDynamicKeyGroup(final DynamicKeyGroup dynamicKeyGroup) {\n" +
                "    final RxProviders proxy = (RxProviders) RxCache.retainedProxy();\n" +
                "    Actions.Evict<String> evict = new Actions.Evict<String>() {\n" +
                "      @Override public Observable<List<String>> call(Observable<List<String>> elements) {\n" +
                "        return proxy.mocksDynamicKeyGroup(elements, dynamicKeyGroup, new EvictDynamicKeyGroup(true));\n" +
                "      }\n" +
                "    } ;;\n" +
                "    Observable<List<String>> oCache = proxy.mocksDynamicKeyGroup(Observable.<List<String>>just(new ArrayList<String>()), dynamicKeyGroup, new EvictDynamicKeyGroup(false));\n" +
                "    return Actions.with(evict, oCache);\n" +
                "  }\n" +
                "}");


        assertAbout(javaSource()).that(source)
                .processedWith(new ActionsProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

}
