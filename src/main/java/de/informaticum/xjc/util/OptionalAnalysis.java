package de.informaticum.xjc.util;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

public enum OptionalAnalysis {
    ;

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

    public static final JClass accordingOptionalTypeFor(final JType $type) {
        final var model = $type.owner();
        if (model.DOUBLE.equals($type.unboxify())) {
            return model.ref(OptionalDouble.class);
        } else if (model.INT.equals($type.unboxify())) {
            return model.ref(OptionalInt.class);
        } else if (model.LONG.equals($type.unboxify())) {
            return model.ref(OptionalLong.class);
        } else {
            return model.ref(Optional.class).narrow($type.boxify());
        }
    }

}
