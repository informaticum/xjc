package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeModelAnalysis.GETTHROWS_METHOD;
import static java.lang.reflect.Modifier.isPrivate;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import com.sun.codemodel.JMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CheckPrivateMethodTest {

    @Parameters(name = "{0}#{1}")
    public static List<Object[]> data() {
        return asList(new Object[][]{
            {JMethod.class, GETTHROWS_METHOD},
        });
    }

    @Parameter(0)
    public Class<?> clazz;

    @Parameter(1)
    public String methodName;

    @Test
    public void testDeclaredMethod()
    throws Exception {
        assertThat(clazz).withFailMessage("This assertion checks if the %s class still has a method with name %s. " +
                                          "If this fails, the implementation of CodeModelAnalysis must be adopted.", clazz, methodName)
                         .hasDeclaredMethods(methodName);
        final var field = clazz.getDeclaredMethod(methodName);
        assertThat(isPrivate(field.getModifiers())).withFailMessage("This assertion checks if the %s method of %s is still private. " +
                                                                    "If this fails, the implementation of CodeModelAnalysis most likely should be adopted.", methodName, clazz)
                                                   .isTrue();
    }

}