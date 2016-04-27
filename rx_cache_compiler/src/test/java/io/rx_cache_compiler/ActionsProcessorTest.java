package io.rx_cache_compiler;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by victor on 24/04/16.
 */
public class ActionsProcessorTest {
    //test with no dynamicKey nor dynamicKeyGroup
    //test with dynamicKey
    //test with dynamicKeyGroup
    //errors : both dynamicKey or dynamicKeyGroup, lack of type class as an element of list

    @Test public void with_No_DynamicKey_Neither_DynamicKeyGroup() throws Exception {
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
                "    Observable<List<Mock.InnerMock>> mocks(Observable<List<Mock.InnerMock>> message, EvictProvider evictProvider);\n" +

                "    @Actionable\n" +
                "    Observable<List<Mock>> mocksDynamicKey(Observable<List<Mock>> message, DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);\n" +

                "    @Actionable\n" +
                "    Observable<List<Mock>> mocksDynamicKeyGroup(Observable<List<Mock>> message, DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKey);\n" +
                "}");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/ActionsProviders", "" +
                "package io.rx_cache;\n" +
                "\n" +
                "import Mock.InnerMock;\n" +
                "import Mock.Mock;\n" +
                "import io.rx_cache.internal.RxCache;\n" +
                "import io.rx_cache.internal.actions.Actions;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import rx.Observable;\n" +
                "import test.RxProviders;\n" +
                "\n" +
                "public final class ActionsProviders {\n" +
                "  public static Actions<InnerMock> mocks() {\n" +
                "    final RxProviders proxy = (RxProviders) RxCache.retainedProxy();\n" +
                "    Actions.Evict<InnerMock> evict = new Actions.Evict<InnerMock>() {\n" +
                "      @Override public Observable<List<InnerMock>> call(Observable<List<InnerMock>> elements) {\n" +
                "        return proxy.mocks(elements, new EvictProvider(true));\n" +
                "      }\n" +
                "    } ;;\n" +
                "    Observable<List<InnerMock>> oCache = proxy.mocks(Observable.<List<InnerMock>>just(new ArrayList<InnerMock>()), new EvictProvider(false));\n" +
                "    return new Actions<>(evict, oCache);\n" +
                "  }\n" +
                "\n" +
                "  public static Actions<Mock> mocksDynamicKey(final DynamicKey dynamicKey) {\n" +
                "    final RxProviders proxy = (RxProviders) RxCache.retainedProxy();\n" +
                "    Actions.Evict<Mock> evict = new Actions.Evict<Mock>() {\n" +
                "      @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {\n" +
                "        return proxy.mocksDynamicKey(elements, dynamicKey, new EvictDynamicKey(true));\n" +
                "      }\n" +
                "    } ;;\n" +
                "    Observable<List<Mock>> oCache = proxy.mocksDynamicKey(Observable.<List<Mock>>just(new ArrayList<Mock>()), dynamicKey, new EvictDynamicKey(false));\n" +
                "    return new Actions<>(evict, oCache);\n" +
                "  }\n" +
                "\n" +
                "  public static Actions<Mock> mocksDynamicKeyGroup(final DynamicKeyGroup dynamicKeyGroup) {\n" +
                "    final RxProviders proxy = (RxProviders) RxCache.retainedProxy();\n" +
                "    Actions.Evict<Mock> evict = new Actions.Evict<Mock>() {\n" +
                "      @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {\n" +
                "        return proxy.mocksDynamicKeyGroup(elements, dynamicKeyGroup, new EvictDynamicKeyGroup(true));\n" +
                "      }\n" +
                "    } ;;\n" +
                "    Observable<List<Mock>> oCache = proxy.mocksDynamicKeyGroup(Observable.<List<Mock>>just(new ArrayList<Mock>()), dynamicKeyGroup, new EvictDynamicKeyGroup(false));\n" +
                "    return new Actions<>(evict, oCache);\n" +
                "  }\n" +
                "}");


        assertAbout(javaSource()).that(source)
                .processedWith(new ActionsProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }
}
