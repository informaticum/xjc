package de.informaticum.xjc.util;

import static java.util.Arrays.stream;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

public enum CodeModelAnalysis {
    ;

    public static final JMethod getConstructor(final JDefinedClass $clazz, final JType... argumentTypes) {
        final var rawTypes = stream(argumentTypes).map(JType::erasure).toArray(JType[]::new);
        return $clazz.getConstructor(rawTypes);
    }

    public static final JMethod getMethod(final JDefinedClass $clazz, final String name, final JType... argumentTypes) {
        final var rawTypes = stream(argumentTypes).map(JType::erasure).toArray(JType[]::new);
        return $clazz.getMethod(name, rawTypes);
    }

}
