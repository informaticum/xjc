package de.informaticum.xjc.util;

import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

public enum XjcAccessorGuesser {
    ;

    static final String GET = "get";

    static final String IS = "is";

    static final String SET = "set";

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()}
     */
    public static final String guessGetterName(final FieldOutline field) {
        final var outline = field.parent().parent();
        final var model = outline.getModel();
        final var codeModel = outline.getCodeModel();
        final var isBoolean = codeModel.BOOLEAN.equals(field.getRawType().boxify().getPrimitiveType());
        final var prefix = model.options.enableIntrospection ? field.getRawType().isPrimitive() && isBoolean ? IS : GET : isBoolean ? IS : GET;
        final var property = field.getPropertyInfo().getName(true);
        return prefix + property;
    }

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()}
     */
    public static final String guessGetterName(final ClassOutline clazz, final JFieldVar field) {
        final var outline = clazz.parent();
        final var codeModel = outline.getCodeModel();
        final var model = outline.getModel();
        final var isBoolean = codeModel.BOOLEAN.equals(field.type().boxify().getPrimitiveType());
        final var prefix = model.options.enableIntrospection ? field.type().isPrimitive() && isBoolean ? IS : GET : isBoolean ? IS : GET;
        final var property = model.getNameConverter().toPropertyName(field.name());
        return prefix + property;
    }

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)}
     */
    public static final String guessSetterName(final FieldOutline field) {
        final var prefix = SET;
        final var property = field.getPropertyInfo().getName(true);
        return prefix + property;
    }

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)}
     */
    public static final String guessSetterName(final ClassOutline clazz, final JFieldVar field) {
        final var prefix = SET;
        final var property = clazz.parent().getModel().getNameConverter().toPropertyName(field.name());
        return prefix + property;
    }

}
