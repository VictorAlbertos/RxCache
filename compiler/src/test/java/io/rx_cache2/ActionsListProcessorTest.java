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

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ActionsListProcessorTest {

    @Test public void Test_Processor() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("RxProviders", "" +
                "package test;\n" +
                "import io.reactivex.Observable;\n"
            + "import io.rx_cache2.Actionable;\n"
            + "import io.rx_cache2.DynamicKey;\n"
            + "import io.rx_cache2.DynamicKeyGroup;\n"
            + "import io.rx_cache2.EvictDynamicKey;\n"
            + "import io.rx_cache2.EvictDynamicKeyGroup;\n"
            + "import io.rx_cache2.EvictProvider;\n"
            + "import io.rx_cache2.Mock;\n"
            + "import java.util.List;\n"
            + "\n"
            + "public interface RxProviders {\n"
            + "  @Actionable Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks,\n"
            + "      EvictProvider evictProvider);\n"
            + "\n"
            + "  @Actionable Observable<List<Mock>> getMocksEvictDynamicKey(Observable<List<Mock>> oMocks,\n"
            + "      DynamicKey dynamicKey,\n"
            + "      EvictDynamicKey evictDynamicKey);\n"
            + "\n"
            + "  @Actionable Observable<List<Mock>> getMocksEvictDynamicKeyGroup(Observable<List<Mock>> oMocks,\n"
            + "      DynamicKeyGroup dynamicKeyGroup,\n"
            + "      EvictDynamicKeyGroup evictDynamicKeyGroup);\n"
            + "}");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/RxProvidersActionable", "" +
                "package test;\n" +
                "import io.reactivex.Observable;\n"
            + "import io.rx_cache2.ActionsList;\n"
            + "import io.rx_cache2.DynamicKey;\n"
            + "import io.rx_cache2.DynamicKeyGroup;\n"
            + "import io.rx_cache2.EvictDynamicKey;\n"
            + "import io.rx_cache2.EvictDynamicKeyGroup;\n"
            + "import io.rx_cache2.EvictProvider;\n"
            + "import io.rx_cache2.Mock;\n"
            + "import java.util.ArrayList;\n"
            + "import java.util.List;\n"
            + "import javax.annotation.Generated;\n"
            + "\n"
            + "@Generated(\n"
            + "    value = \"io.rx_cache2.ActionsProcessor\",\n"
            + "    comments = \"Generated code from RxCache. Don't modify. Or modify. It doesn't matter.\"\n"
            + ")\n"
            + "public class RxProvidersActionable {\n"
            + "  public static ActionsList<Mock> getMocksEvictProvider(final RxProviders proxy) {\n"
            + "    ActionsList.Evict<Mock> evict = new ActionsList.Evict<Mock>() {\n"
            + "      @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {\n"
            + "        return proxy.getMocksEvictProvider(elements, new EvictProvider(true));\n"
            + "      }\n"
            + "    } ;;\n"
            + "    Observable<List<Mock>> oCache = proxy.getMocksEvictProvider(Observable.<List<io.rx_cache2.Mock>>just(new ArrayList<Mock>()), new EvictProvider(false));\n"
            + "    return ActionsList.with(evict, oCache);\n"
            + "  }\n"
            + "\n"
            + "  public static ActionsList<Mock> getMocksEvictDynamicKey(final RxProviders proxy, final DynamicKey dynamicKey) {\n"
            + "    ActionsList.Evict<Mock> evict = new ActionsList.Evict<Mock>() {\n"
            + "      @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {\n"
            + "        return proxy.getMocksEvictDynamicKey(elements, dynamicKey, new EvictDynamicKey(true));\n"
            + "      }\n"
            + "    } ;;\n"
            + "    Observable<List<Mock>> oCache = proxy.getMocksEvictDynamicKey(Observable.<List<io.rx_cache2.Mock>>just(new ArrayList<Mock>()), dynamicKey, new EvictDynamicKey(false));\n"
            + "    return ActionsList.with(evict, oCache);\n"
            + "  }\n"
            + "\n"
            + "  public static ActionsList<Mock> getMocksEvictDynamicKeyGroup(final RxProviders proxy, final DynamicKeyGroup dynamicKeyGroup) {\n"
            + "    ActionsList.Evict<Mock> evict = new ActionsList.Evict<Mock>() {\n"
            + "      @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {\n"
            + "        return proxy.getMocksEvictDynamicKeyGroup(elements, dynamicKeyGroup, new EvictDynamicKeyGroup(true));\n"
            + "      }\n"
            + "    } ;;\n"
            + "    Observable<List<Mock>> oCache = proxy.getMocksEvictDynamicKeyGroup(Observable.<List<io.rx_cache2.Mock>>just(new ArrayList<Mock>()), dynamicKeyGroup, new EvictDynamicKeyGroup(false));\n"
            + "    return ActionsList.with(evict, oCache);\n"
            + "  }\n"
            + "}");


        assertAbout(javaSource()).that(source)
                .processedWith(new io.rx_cache2.ActionsProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

}
