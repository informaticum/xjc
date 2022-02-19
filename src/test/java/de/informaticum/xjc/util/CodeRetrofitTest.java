package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeRetrofit.BODY_FIELD;
import static java.lang.reflect.Modifier.isPrivate;
import static org.assertj.core.api.Assertions.assertThat;
import com.sun.codemodel.JMethod;
import org.junit.Test;

public class CodeRetrofitTest {

    @Test
    public void testDeclaredBodyField() throws Exception {
        assertThat(JMethod.class).withFailMessage("This assertion checks if the JMethod class still has a field with name %s. " +
                                                  "If this fails, the implementation of CodeRetrofit must be adopted.", BODY_FIELD)
                                 .hasDeclaredFields(BODY_FIELD);
        final var internalBodyField = JMethod.class.getDeclaredField(BODY_FIELD);
        assertThat(isPrivate(internalBodyField.getModifiers())).withFailMessage("This assertion checks if the %s field of JMethod is still private. " +
                                                                                "If this fails, the implementation of CodeRetrofit most likely should be adopted.", BODY_FIELD)
                                                               .isTrue();
    }

}
