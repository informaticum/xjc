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
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to {@link OptionalDouble}, {@link OptionalInt}, {@link OptionalLong}, and
 * {@link Optional} types.
 */
public enum OptionalAnalysis {
    ;

    private static final String UNEXPECTED_MODIFICATION = "WTF! The long-time existing factory-method has been modified ;-(";

    /**
     * @param $method
     *            the method to analyse
     * @return {@code true} iff the give method's return type is assignable to {@link OptionalDouble}, {@link OptionalInt}, {@link OptionalLong}, or {@link Optional}
     */
    public static final boolean isOptionalMethod(final JMethod $method) {
        final var $model = $method.type().owner();
        final var $raw = $method.type().erasure();
        if (isPrimitiveOptional($method.type())) {
            return true;
        } else if ($model.ref(Optional.class).equals($raw)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param $type
     *            the type to analyse
     * @return the according optional type (i.e., {@link JClass} of either {@link OptionalDouble} for {@code double}/{@link Double} type, {@link OptionalInt} for
     *         {@code int}/{@link Integer} type, {@link OptionalLong} for {@code long}/{@link Long} type, or {@link Optional} in any other case)
     */
    public static final JClass optionalTypeFor(final JType $type) {
        final var $model = $type.owner();
        final var $primitive = $type.unboxify();
        if ($model.DOUBLE.equals($primitive)) {
            assertThat(OptionalDouble.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalDouble.of(0.0d)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return $model.ref(OptionalDouble.class);
        } else if ($model.INT.equals($primitive)) {
            assertThat(OptionalInt.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalInt.of(0)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return $model.ref(OptionalInt.class);
        } else if ($model.LONG.equals($primitive)) {
            assertThat(OptionalLong.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalLong.of(0L)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return $model.ref(OptionalLong.class);
        } else {
            assertThat(Optional.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(Optional.of("")).withFailMessage(UNEXPECTED_MODIFICATION).hasValue("");
            return $model.ref(Optional.class).narrow($type.boxify());
        }
    }

    /**
     * @param $type
     *            the type to analyse
     * @return {@code true} iff the give type is assignable to {@link OptionalDouble}, {@link OptionalInt}, or {@link OptionalLong}
     */
    public static final boolean isPrimitiveOptional(final JType $type) {
        final var $model = $type.owner();
        final var $raw = $type.erasure();
        if ($model.ref(OptionalDouble.class).equals($raw)) {
            return true;
        } else if ($model.ref(OptionalInt.class).equals($raw)) {
            return true;
        } else if ($model.ref(OptionalLong.class).equals($raw)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param $Class
     *            the class to deoptionalise
     * @return the deoptionalised type of the given class (i.e., {@link com.sun.codemodel.JCodeModel#DOUBLE double}/{@link com.sun.codemodel.JCodeModel#INT
     *         int}/{@link com.sun.codemodel.JCodeModel#LONG long} for {@link OptionalDouble}/{@link OptionalInt}/{@link OptionalLong}, or {@code T} for bound {@code Optional<T>},
     *         or {@code Object} for wildcard {@code Optional}/unbound {@code Optional})
     */
    public static final Optional<JType> deoptionalisedTypeFor(final JClass $Class) {
        final var $model = $Class.owner();
        final var $Raw = $Class.erasure();
        if ($model.ref(OptionalDouble.class).equals($Raw)) {
            return Optional.of($model.DOUBLE);
        } else if ($model.ref(OptionalInt.class).equals($Raw)) {
            return Optional.of($model.INT);
        } else if ($model.ref(OptionalLong.class).equals($Raw)) {
            return Optional.of($model.LONG);
        } else if ($model.ref(Optional.class).equals($Raw)) {
            final var $Narrowed = $Class.getTypeParameters();
            if ($Narrowed.isEmpty()) {
                return Optional.of($model.ref(Object.class));
            } else {
                assertThat($Narrowed.size() == 1);
                return Optional.of($Narrowed.get(0));
            }
        } else {
            return Optional.empty();
        }
    }

}
