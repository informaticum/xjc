package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeModelAnalysis.ATPARAMS_FIELD;
import static de.informaticum.xjc.util.CodeModelAnalysis.ATTHROWS_FIELD;
import static de.informaticum.xjc.util.CodeModelAnalysis.JDOC_FIELD;
import static de.informaticum.xjc.util.CodeModelAnalysis.OUTER_FIELD;
import static de.informaticum.xjc.util.CodeRetrofit.ANNOTATION_MEMBERS;
import static de.informaticum.xjc.util.CodeRetrofit.BODY_FIELD;
import static java.lang.reflect.Modifier.isPrivate;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CheckPrivateFieldTest {

    @Parameters(name = "{0}#{1}")
    public static List<Object[]> data() {
        return asList(new Object[][]{
            {JDocComment.class, ATTHROWS_FIELD},
            {JDocComment.class, ATPARAMS_FIELD},
            {JAnnotationUse.class, ANNOTATION_MEMBERS},
            {JMethod.class, BODY_FIELD},
            {JMethod.class, JDOC_FIELD},
            {JMethod.class, OUTER_FIELD},
        });
    }

    @Parameter(0)
    public Class<?> clazz;

    @Parameter(1)
    public String fieldName;

    @Test
    public void testDeclaredField()
    throws Exception {
        assertThat(this.clazz).withFailMessage("This assertion checks if the %s class still has a field with name %s. " +
                                               "If this fails, the implementation of XJC-Plugins must be adopted.", this.clazz, this.fieldName)
                              .hasDeclaredFields(this.fieldName);
        final var field = this.clazz.getDeclaredField(this.fieldName);
        assertThat(isPrivate(field.getModifiers())).withFailMessage("This assertion checks if the %s field of %s is still private. " +
                                                                    "If this fails, the implementation of XJC-Plugins most likely should be adopted.", this.fieldName, this.clazz)
                                                   .isTrue();
    }

}