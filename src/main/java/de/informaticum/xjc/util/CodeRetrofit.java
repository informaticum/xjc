package de.informaticum.xjc.util;

import com.sun.codemodel.JMethod;

public enum CodeRetrofit {
    ;

    static final String BODY_FIELD = "body";

    public static final JMethod eraseBody(final JMethod $method) {
        try {
            final var internalBodyField = JMethod.class.getDeclaredField(BODY_FIELD);
            internalBodyField.setAccessible(true);
            internalBodyField.set($method, null);
            return $method;
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

}
