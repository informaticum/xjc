package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeModelAnalysis.ATPARAMS_FIELD;
import static de.informaticum.xjc.util.CodeModelAnalysis.ATTHROWS_FIELD;
import static de.informaticum.xjc.util.CodeModelAnalysis.GETTHROWS_METHOD;
import static de.informaticum.xjc.util.CodeModelAnalysis.JDOC_FIELD;
import static de.informaticum.xjc.util.CodeModelAnalysis.OUTER_FIELD;
import static java.lang.reflect.Modifier.isPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import org.junit.Test;

public class CodeModelAnalysisTest {

    @Test
    public void testDeclaredAtThrowsField()
    throws Exception {
        assertThat(JDocComment.class).withFailMessage("This assertion checks if the JDocComment class still has a field with name %s. " +
                                                      "If this fails, the implementation of CodeModelAnalysis must be adopted.", ATTHROWS_FIELD)
                                     .hasDeclaredFields(ATTHROWS_FIELD);
        final var inspected = JDocComment.class.getDeclaredField(ATTHROWS_FIELD);
        assertThat(isPrivate(inspected.getModifiers())).withFailMessage("This assertion checks if the %s field of JDocComment is still private. " +
                                                                        "If this fails, the implementation of CodeModelAnalysis most likely should be adopted.", ATTHROWS_FIELD)
                                                       .isTrue();
    }

    @Test
    public void testDeclaredAtParamsField()
    throws Exception {
        assertThat(JDocComment.class).withFailMessage("This assertion checks if the JDocComment class still has a field with name %s. " +
                                                      "If this fails, the implementation of CodeModelAnalysis must be adopted.", ATPARAMS_FIELD)
                                     .hasDeclaredFields(ATPARAMS_FIELD);
        final var inspected = JDocComment.class.getDeclaredField(ATPARAMS_FIELD);
        assertThat(isPrivate(inspected.getModifiers())).withFailMessage("This assertion checks if the %s field of JDocComment is still private. " +
                                                                        "If this fails, the implementation of CodeModelAnalysis most likely should be adopted.", ATPARAMS_FIELD)
                                                       .isTrue();
    }

    @Test
    public void testDeclaredGetThrowsMethod()
    throws Exception {
        assertThat(JMethod.class).withFailMessage("This assertion checks if the JMethod class still has a method with name %s. " +
                                                  "If this fails, the implementation of CodeModelAnalysis must be adopted.", GETTHROWS_METHOD)
                                 .hasDeclaredMethods(GETTHROWS_METHOD);
        final var inspected = JMethod.class.getDeclaredMethod(GETTHROWS_METHOD);
        assertThat(isPrivate(inspected.getModifiers())).withFailMessage("This assertion checks if the %s method of JMethod is still private. " +
                                                                        "If this fails, the implementation of CodeModelAnalysis most likely should be adopted.", GETTHROWS_METHOD)
                                                       .isTrue();
    }


    @Test
    public void testDeclaredJdocField()
    throws Exception {
        assertThat(JMethod.class).withFailMessage("This assertion checks if the JMethod class still has a field with name %s. " +
                                                  "If this fails, the implementation of CodeModelAnalysis must be adopted.", JDOC_FIELD)
                                 .hasDeclaredFields(JDOC_FIELD);
        final var inspected = JMethod.class.getDeclaredField(JDOC_FIELD);
        assertThat(isPrivate(inspected.getModifiers())).withFailMessage("This assertion checks if the %s field of JMethod is still private. " +
                                                                        "If this fails, the implementation of CodeModelAnalysis most likely should be adopted.", JDOC_FIELD)
                                                       .isTrue();
    }

    @Test
    public void testDeclaredOuterField()
    throws Exception {
        assertThat(JMethod.class).withFailMessage("This assertion checks if the JMethod class still has a field with name %s. " +
                                                  "If this fails, the implementation of CodeModelAnalysis must be adopted.", OUTER_FIELD)
                                 .hasDeclaredFields(OUTER_FIELD);
        final var inspected = JMethod.class.getDeclaredField(OUTER_FIELD);
        assertThat(isPrivate(inspected.getModifiers())).withFailMessage("This assertion checks if the %s field of JMethod is still private. " +
                                                                        "If this fails, the implementation of CodeModelAnalysis most likely should be adopted.", OUTER_FIELD)
                                                       .isTrue();
    }

}
