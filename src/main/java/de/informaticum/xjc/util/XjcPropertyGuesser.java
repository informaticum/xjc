package de.informaticum.xjc.util;

import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to accessor
 * methods of fields (getter/setter) and to the population methods of classes (factory).
 */
public enum XjcPropertyGuesser {
    ;

    /**
     * @see #guessFactoryName(ClassOutline)
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.model.CElementInfo,
     *      com.sun.tools.xjc.outline.Aspect, com.sun.tools.xjc.outline.Aspect)
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,
     *      com.sun.codemodel.JClass)
     */
    public static final String CREATE = "create";

    /**
     * @see #guessGetterName(FieldOutline)
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String GET = "get";

    /**
     * @see #guessGetterName(FieldOutline)
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String IS = "is";

    /**
     * @see #guessSetterName(FieldOutline)
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock,
     *      String, com.sun.codemodel.JExpression)
     */
    public static final String SET = "set";

    /**
     * @see #guessWitherName(FieldOutline)
     */
    public static final String WITH = "with";

    /**
     * Guesses the name of the according getter method for a given field. Whether or not the field is boolean, the
     * return value may start with prefix {@value #IS} or with prefix {@value #GET}. Further, the value of
     * {@link com.sun.tools.xjc.Options#enableIntrospection} is also considered to decide that prefix. Doing so, this
     * method should return similar values compared to the names used for the generated getter methods.
     *
     * @param attribute
     *            the given field
     * @return the name of the according getter method
     *
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String guessGetterName(final FieldOutline attribute) {
        final var isBoolean = attribute.parent().parent().getCodeModel().BOOLEAN.equals(attribute.getRawType().boxify().getPrimitiveType());
        if (attribute.parent().parent().getModel().options.enableIntrospection) {
            return (attribute.getRawType().isPrimitive() && isBoolean ? IS : GET) + attribute.getPropertyInfo().getName(true);
        } else {
            return (isBoolean ? IS : GET) + attribute.getPropertyInfo().getName(true);
        }
    }

    /**
     * Guesses the name of the according setter method for a given field. The return value will start with prefix
     * {@value #SET}. Doing so, this method should return similar values compared to the names used for the generated
     * setter methods.
     *
     * @param attribute
     *            the given field
     * @return the name of the according setter method
     *
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock,
     *      String, com.sun.codemodel.JExpression)
     */
    public static final String guessSetterName(final FieldOutline attribute) {
        return SET + attribute.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according wither method for a given field. The return value will start with prefix
     * {@value #WITH}.
     *
     * @param attribute
     *            the given field
     * @return the name of the according wither method
     */
    public static final String guessWitherName(final FieldOutline attribute) {
        return WITH + attribute.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according factory method for a given class. The return value will start with prefix
     * {@value #CREATE}. Further, the {@linkplain com.sun.tools.xjc.model.CClassInfo#getSqueezedName() whole class
     * hierarchy} is considered to create the factory name. Doing so, this method should return similar values compared
     * to the names used for the object-factories' population methods.
     *
     * @param clazz
     *            the given class
     * @return the name of the according factory method
     *
     * @see com.sun.tools.xjc.model.CClassInfo#getSqueezedName()
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.model.CElementInfo,
     *      com.sun.tools.xjc.outline.Aspect, com.sun.tools.xjc.outline.Aspect)
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,
     *      com.sun.codemodel.JClass)
     */
    public static final String guessFactoryName(final ClassOutline clazz) {
        return CREATE + clazz.target.getSqueezedName();
    }

}
