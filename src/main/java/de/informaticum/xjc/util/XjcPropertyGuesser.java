package de.informaticum.xjc.util;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

public enum XjcPropertyGuesser {
    ;

    public static final String CREATE = "create";

    public static final String GET = "get";

    public static final String IS = "is";

    public static final String SET = "set";

    public static final String WITH = "with";

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()}
     */
    public static final String guessGetterName(final FieldOutline attribute) {
        final var outline = attribute.parent().parent();
        final var model = outline.getModel();
        final var codeModel = outline.getCodeModel();
        final var isBoolean = codeModel.BOOLEAN.equals(attribute.getRawType().boxify().getPrimitiveType());
        final var prefix = model.options.enableIntrospection ? attribute.getRawType().isPrimitive() && isBoolean ? IS : GET : isBoolean ? IS : GET;
        final var property = attribute.getPropertyInfo().getName(true);
        return prefix + property;
    }

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()}
     */
    public static final String guessGetterName(final ClassOutline clazz, final JFieldVar $property) {
        final var outline = clazz.parent();
        final var codeModel = outline.getCodeModel();
        final var model = outline.getModel();
        final var isBoolean = codeModel.BOOLEAN.equals($property.type().boxify().getPrimitiveType());
        final var prefix = model.options.enableIntrospection ? $property.type().isPrimitive() && isBoolean ? IS : GET : isBoolean ? IS : GET;
        final var property = model.getNameConverter().toPropertyName($property.name());
        return prefix + property;
    }

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)}
     */
    public static final String guessSetterName(final FieldOutline attribute) {
        final var prefix = SET;
        final var property = attribute.getPropertyInfo().getName(true);
        return prefix + property;
    }

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)}
     */
    public static final String guessSetterName(final ClassOutline clazz, final JFieldVar $property) {
        final var prefix = SET;
        final var property = clazz.parent().getModel().getNameConverter().toPropertyName($property.name());
        return prefix + property;
    }

    public static final String guessWitherName(final FieldOutline attribute) {
        final var prefix = WITH;
        final var property = attribute.getPropertyInfo().getName(true);
        return prefix + property;
    }

    public static final String guessWitherName(final ClassOutline clazz, final JFieldVar $property) {
        final var prefix = WITH;
        final var property = clazz.parent().getModel().getNameConverter().toPropertyName($property.name());
        return prefix + property;
    }

    public static final String guessFactoryName(final ClassOutline clazz) {
        final var prefix = CREATE;
        var factory = clazz.implClass.name();
        for (var parent = clazz.implClass.parentContainer(); parent instanceof JDefinedClass; parent = parent.parentContainer()) {
            factory = ((JDefinedClass) parent).name() + factory;
        }
        return prefix + factory;
    }

}
