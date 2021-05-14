package de.informaticum.xjc.util;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to
 * {@link Optional} types.
 */
public enum OptionalAnalysis {
    ;

    private static final String UNEXPECTED_MODIFICATION = "WTF! The long-time existing factory-method has been modified ;-(";

    /**
     * @param $method
     *            the method to analyse
     * @return {@code true} iff the give method's return type is assignable to {@link OptionalDouble},
     *         {@link OptionalInt}, {@link OptionalLong}, or {@link Optional}
     */
    public static final boolean isOptionalMethod(final JMethod $method) {
        final var model = $method.type().owner();
        final var raw = $method.type().erasure();
        if (model.ref(OptionalDouble.class).equals(raw)) {
            return true;
        } else if (model.ref(OptionalInt.class).equals(raw)) {
            return true;
        } else if (model.ref(OptionalLong.class).equals(raw)) {
            return true;
        } else if (model.ref(Optional.class).equals(raw)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param $type
     *            the type to analyse
     * @return the according optional type (i.e., {@link JClass} of either {@link OptionalDouble} for
     *         {@code double}/{@link Double} type, {@link OptionalInt} for {@code int}/{@link Integer} type,
     *         {@link OptionalLong} for {@code long}/{@link Long} type, or {@link Optional} in any other case)
     */
    public static final JClass optionalTypeFor(final JType $type) {
        final var model = $type.owner();
        final var primitive = $type.unboxify();
        if (model.DOUBLE.equals(primitive)) {
            assertThat(OptionalDouble.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalDouble.of(0.0d)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return model.ref(OptionalDouble.class);
        } else if (model.INT.equals(primitive)) {
            assertThat(OptionalInt.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalInt.of(0)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return model.ref(OptionalInt.class);
        } else if (model.LONG.equals(primitive)) {
            assertThat(OptionalLong.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalLong.of(0L)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return model.ref(OptionalLong.class);
        } else {
            assertThat(Optional.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(Optional.of("foobar")).withFailMessage(UNEXPECTED_MODIFICATION).hasValue("foobar");
            return model.ref(Optional.class).narrow($type.boxify());
        }
    }

}
