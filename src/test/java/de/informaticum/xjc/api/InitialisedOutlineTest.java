package de.informaticum.xjc.api;

import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InitialisedOutlineTest {

    private static final JCodeModel MODEL = new JCodeModel();

    private static final InitialisedOutline sut = new InitialisedOutline() {
        @Override
        public JCodeModel codeModel() {
            return MODEL;
        }
    };

    @BeforeClass
    public static void initialiseCodeModel()
    throws JClassAlreadyExistsException {
        final var xyz = MODEL._class("de.informaticum.existing.Xyz");
        final var foo = xyz._class(PUBLIC | STATIC, "Foo");
        final var foobar = foo._class(PUBLIC | STATIC, "FooBar");
    }

    @Parameters(name = "{0} via {1}-times array of {2}")
    public static List<Object[]> data() {
        return asList(new Object[][]{
            {"boolean", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[Z", 1, "com.sun.codemodel.JPrimitiveType"},
            {"boolean[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[Z", 2, "com.sun.codemodel.JPrimitiveType"},
            {"boolean[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"byte", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[B", 1, "com.sun.codemodel.JPrimitiveType"},
            {"byte[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[B", 2, "com.sun.codemodel.JPrimitiveType"},
            {"byte[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"char", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[C", 1, "com.sun.codemodel.JPrimitiveType"},
            {"char[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[C", 2, "com.sun.codemodel.JPrimitiveType"},
            {"char[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"double", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[D", 1, "com.sun.codemodel.JPrimitiveType"},
            {"double[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"double[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"float", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[F", 1, "com.sun.codemodel.JPrimitiveType"},
            {"float[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[F", 2, "com.sun.codemodel.JPrimitiveType"},
            {"float[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"int", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[I", 1, "com.sun.codemodel.JPrimitiveType"},
            {"int[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[I", 2, "com.sun.codemodel.JPrimitiveType"},
            {"int[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"long", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[J", 1, "com.sun.codemodel.JPrimitiveType"},
            {"long[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[J", 2, "com.sun.codemodel.JPrimitiveType"},
            {"long[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"short", 0, "com.sun.codemodel.JPrimitiveType"},
            {"[S", 1, "com.sun.codemodel.JPrimitiveType"},
            {"short[]", 1, "com.sun.codemodel.JPrimitiveType"},
            {"[[S", 2, "com.sun.codemodel.JPrimitiveType"},
            {"short[][]", 2, "com.sun.codemodel.JPrimitiveType"},
            {"java.lang.String", 0, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"[Ljava.lang.String;", 1, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"java.lang.String[]", 1, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"[[Ljava.lang.String;", 2, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"java.lang.String[][]", 2, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"de.informaticum.xjc.api.BasePlugin", 0, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"[Lde.informaticum.xjc.api.BasePlugin;", 1, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"de.informaticum.xjc.api.BasePlugin[]", 1, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"[[Lde.informaticum.xjc.api.BasePlugin;", 2, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"de.informaticum.xjc.api.BasePlugin[][]", 2, "com.sun.codemodel.JCodeModel$JReferencedClass"},
            {"de.informaticum.nonexisting.Xyz", 0, "com.sun.codemodel.JDirectClass"},
            {"[Lde.informaticum.nonexisting.Xyz;", 1, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.nonexisting.Xyz[]", 1, "com.sun.codemodel.JDirectClass"},
            {"[[Lde.informaticum.nonexisting.Xyz;", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.nonexisting.Xyz[][]", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.Xyz", 0, "com.sun.codemodel.JDefinedClass"},
            {"[Lde.informaticum.existing.Xyz;", 1, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz[]", 1, "com.sun.codemodel.JDefinedClass"},
            {"[[Lde.informaticum.existing.Xyz;", 2, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz[][]", 2, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz.Foo", 0, "com.sun.codemodel.JDefinedClass"},
            {"[Lde.informaticum.existing.Xyz.Foo;", 1, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz.Foo[]", 1, "com.sun.codemodel.JDefinedClass"},
            {"[[Lde.informaticum.existing.Xyz.Foo;", 2, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz.Foo[][]", 2, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz.Foo.FooBar", 0, "com.sun.codemodel.JDefinedClass"},
            {"[Lde.informaticum.existing.Xyz.Foo.FooBar;", 1, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz.Foo.FooBar[]", 1, "com.sun.codemodel.JDefinedClass"},
            {"[[Lde.informaticum.existing.Xyz.Foo.FooBar;", 2, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.Xyz.Foo.FooBar[][]", 2, "com.sun.codemodel.JDefinedClass"},
            {"de.informaticum.existing.NonexistingBar", 0, "com.sun.codemodel.JDirectClass"},
            {"[Lde.informaticum.existing.NonexistingBar;", 1, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.NonexistingBar[]", 1, "com.sun.codemodel.JDirectClass"},
            {"[[Lde.informaticum.existing.NonexistingBar;", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.NonexistingBar[][]", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.NonexistingBar.Nested", 0, "com.sun.codemodel.JDirectClass"},
            {"[Lde.informaticum.existing.NonexistingBar.Nested;", 1, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.NonexistingBar.Nested[]", 1, "com.sun.codemodel.JDirectClass"},
            {"[[Lde.informaticum.existing.NonexistingBar.Nested;", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.Xyz.NonexistingBar.Nested[][]", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.Xyz.NonexistingBar", 0, "com.sun.codemodel.JDirectClass"},
            {"[Lde.informaticum.existing.Xyz.NonexistingBar;", 1, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.Xyz.NonexistingBar[]", 1, "com.sun.codemodel.JDirectClass"},
            {"[[Lde.informaticum.existing.Xyz.NonexistingBar;", 2, "com.sun.codemodel.JDirectClass"},
            {"de.informaticum.existing.Xyz.NonexistingBar[][]", 2, "com.sun.codemodel.JDirectClass"},
        });
    }

    @Parameter(0)
    public String lookupName;

    @Parameter(1)
    public int arrayLevel;

    @Parameter(2)
    public String lookupType;

    @Test
    public void testTypeReference()
    throws Exception {
        var reference = sut.reference(this.lookupName);
        for (var level = 1; level <= this.arrayLevel; level++) {
            assertThat(reference).isNotNull().isInstanceOf(Class.forName("com.sun.codemodel.JArrayClass"));
            reference = reference.boxify().elementType();
        }
        assertThat(reference).isNotNull().isInstanceOf(Class.forName(this.lookupType));
    }

}
