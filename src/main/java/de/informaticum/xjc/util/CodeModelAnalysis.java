package de.informaticum.xjc.util;

import static java.util.Arrays.stream;
import java.util.Optional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to {@code com.sun.codemodel.*} types.
 */
public enum CodeModelAnalysis {
    ;

    /**
     * Looks for a constructor that has the specified method signature and returns it.
     *
     * @param $clazz
     *            the class to analyse
     * @param $argumentTypes
     *            the list of the constructor's argument types
     * @return an {@link Optional} holding the constructor if found; an {@linkplain Optional#empty() empty Optional} if not found
     */
    public static final Optional<JMethod> getConstructor(final JDefinedClass $clazz, final JType... $argumentTypes) {
        final var $constructor = $clazz.getConstructor($argumentTypes);
        if ($constructor != null) {
            return Optional.of($constructor);
        } else {
            final var $rawTypes = stream($argumentTypes).map(JType::erasure).toArray(JType[]::new);
            return Optional.ofNullable($clazz.getConstructor($rawTypes));
        }
    }

    /**
     * Looks for a method that has the specified name/method signature and returns it.
     *
     * @param $clazz
     *            the class to analyse
     * @param name
     *            the method name to look for
     * @param $argumentTypes
     *            the list of the method's argument types
     * @return an {@link Optional} holding the method if found; an {@linkplain Optional#empty() empty Optional} if not found
     */
    public static final Optional<JMethod> getMethod(final JDefinedClass $clazz, final String name, final JType... $argumentTypes) {
        final var $method = $clazz.getMethod(name, $argumentTypes);
        if ($method != null) {
            return Optional.of($method);
        } else {
            final var $rawTypes = stream($argumentTypes).map(JType::erasure).toArray(JType[]::new);
            return Optional.ofNullable($clazz.getMethod(name, $rawTypes));
        }
    }

}
