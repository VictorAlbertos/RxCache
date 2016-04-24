package io.rx_cache_compiler;

import com.google.common.base.Joiner;
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

    @Test public void stringArray() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;\n",
                "import java.util.List;\n",
                "import io.rx_cache.Actionable;\n",
                "import rx.Observable;\n",

                "public interface Test {\n",
                    "@Actionable\n",
                    "Observable<List<Mock>> getMocks(Observable<List<Mock>> oMocks);\n",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test$$ViewBinder", ""
                + "package test;\n"
                + "import android.content.res.Resources;\n"
                + "import butterknife.Unbinder;\n"
                + "import butterknife.internal.Finder;\n"
                + "import butterknife.internal.ViewBinder;\n"
                + "import java.lang.Object;\n"
                + "import java.lang.Override;\n"
                + "import java.lang.SuppressWarnings;\n"
                + "public class Test$$ViewBinder<T extends Test> implements ViewBinder<T> {\n"
                + "  @Override\n"
                + "  @SuppressWarnings(\"ResourceType\")\n"
                + "  public Unbinder bind(final Finder finder, final T target, Object source) {\n"
                + "    Resources res = finder.getContext(source).getResources();\n"
                + "    target.one = res.getStringArray(1);\n"
                + "    return Unbinder.EMPTY;\n"
                + "  }\n"
                + "}");

        assertAbout(javaSource()).that(source)
                .processedWith(new ActionsProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }
}
