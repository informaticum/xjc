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

    public static final boolean isOptionalMethod(final JMethod origin) {
        final var model = origin.type().owner();
        final var raw = origin.type().erasure();
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

    public static final JClass accordingOptionalFor(final JType originType) {
        final var model = originType.owner();
        if (originType.unboxify().equals(model.DOUBLE)) {
            return model.ref(OptionalDouble.class);
        } else if (originType.unboxify().equals(model.INT)) {
            return model.ref(OptionalInt.class);
        } else if (originType.unboxify().equals(model.LONG)) {
            return model.ref(OptionalLong.class);
        } else {
            return model.ref(Optional.class).narrow(originType.boxify());
        }
    }

}
