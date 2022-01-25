package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeRetrofit.BODY_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import com.sun.codemodel.JMethod;
import org.junit.Test;

public class CodeRetrofitTest {

    @Test
    public void testDeclaredBodyField() {
        assertThat(JMethod.class).hasDeclaredFields(BODY_FIELD);
    }

}
