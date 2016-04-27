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
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "" +
                getImports() +
                "public interface Test {\n"+
                    "@Actionable\n"+
                    "Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks, EvictProvider evictProvider);\n"+
                "}");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/ActionsTest", ""
                +   "package test;\n" +
                    "import java.lang.String;\n" +
                    "import java.lang.System;\n" +

                    "public final class ActionsTest {\n" +
                        "public static void main(String[] args) {\n" +
                            "System.out.println(\"Hello, JavaPoet!\");\n" +
                        "}\n" +
                    "}");


        assertAbout(javaSource()).that(source)
                .processedWith(new ActionsProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    private String getImports() {
        return  "package Test;\n" +
                "import java.util.List;\n" +
                "import io.rx_cache.Actionable;\n" +
                "import io.rx_cache.DynamicKey;\n" +
                "import io.rx_cache.DynamicKeyGroup;\n" +
                "import io.rx_cache.EvictDynamicKey;\n" +
                "import io.rx_cache.EvictDynamicKeyGroup;\n" +
                "import io.rx_cache.EvictProvider;\n" +
                "import io.rx_cache.Expirable;\n" +
                "import io.rx_cache_compiler.Mock;\n" +
                "import rx.Observable;";
    }

}
