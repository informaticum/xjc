package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

public enum XjcSpy {
    ;

    private static final String GET = "get";

    private static final String IS = "is";

    private static final String SET = "set";

    /*
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()}
     *
     * {@link com.sun.tools.xjc.generator.bean.field.AbstractListField#generateAccessors()}
     *
     * ...
     */
    public static final String spyGetterName(final FieldOutline field) {
        try {
            if (field instanceof AbstractFieldWithVar) {
                // includes the decision between "getXYZ()" of "isXYZ()"
                return ((AbstractFieldWithVar) field).getGetterMethod();
            } else {
                final var property = field.getPropertyInfo().getName(true);
                return GET + property;
            }
        } catch (final IllegalAccessError cxfXjcPluginDeniesAccess) {
            /* java.lang.IllegalAccessError: failed to access class com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar from class com.sun.tools.xjc.generator.bean.field.XjcSpy (com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar is in unnamed module of loader 'app'; com.sun.tools.xjc.generator.bean.field.XjcSpy is in unnamed module of loader java.net.URLClassLoader @48503868)
             *         at com.sun.tools.xjc.generator.bean.field.XjcSpy.spyGetterName(XjcSpy.java:22)
             *         at de.informaticum.xjc.util.DeclarationAnalysis.declaredGettersOf(DeclarationAnalysis.java:54)
             *         at de.informaticum.xjc.InspectionPlugin.runClass(InspectionPlugin.java:29)
             *         at de.informaticum.xjc.AbstractPlugin.run(AbstractPlugin.java:60)
             *         at com.sun.tools.xjc.model.Model.generateCode(Model.java:262)
             *         at org.apache.cxf.maven_plugin.XSDToJavaRunner.run(XSDToJavaRunner.java:179)
             *         at org.apache.cxf.maven_plugin.XSDToJavaRunner.main(XSDToJavaRunner.java:360)
             *
             * ===> Let's do the best guess: 
             */
            final var prefix = field.parent().parent().getCodeModel().BOOLEAN.equals(field.getRawType().unboxify()) ? IS : GET;
            final var property = field.getPropertyInfo().getName(true);
            return prefix + property;
        }
    }

    public static final String spyGetterName(final ClassOutline clazz, final JFieldVar field) {
        final var nameConverter = clazz.parent().getModel().getNameConverter();
        final var property = nameConverter.toPropertyName(field.name());
        return GET + property;
    }

    public static final String spySetterName(final FieldOutline field) {
        final var property = field.getPropertyInfo().getName(true);
        return SET + property;
    }

    public static final String spySetterName(final ClassOutline clazz, final JFieldVar field) {
        final var nameConverter = clazz.parent().getModel().getNameConverter();
        final var property = nameConverter.toPropertyName(field.name());
        return SET + property;
    }

}
