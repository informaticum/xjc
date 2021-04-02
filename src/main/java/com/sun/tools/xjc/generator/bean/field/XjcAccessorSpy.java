package com.sun.tools.xjc.generator.bean.field;

import static de.informaticum.xjc.util.XjcAccessorGuesser.guessGetterName;
import com.sun.tools.xjc.outline.FieldOutline;

public enum XjcAccessorSpy {
    ;

    public static final String spyGetterName(final FieldOutline field) {
        try {
            if (field instanceof AbstractFieldWithVar) {
                return ((AbstractFieldWithVar) field).getGetterMethod();
            } else {
                /* No XJC source to query the getter name?
                 * ===> Let's do the best guess: */
                return guessGetterName(field);
            }
        } catch (final IllegalAccessError cxfXjcPluginDeniesAccess) {
            /* java.lang.IllegalAccessError: failed to access class com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar
             * from class com.sun.tools.xjc.generator.bean.field.XjcSpy (com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar
             * is in unnamed module of loader 'app'; com.sun.tools.xjc.generator.bean.field.XjcSpy is in unnamed module of loader
             * java.net.URLClassLoader @48503868)
             *         at com.sun.tools.xjc.generator.bean.field.XjcSpy.spyGetterName(XjcSpy.java:22)
             *         at de.informaticum.xjc.util.DeclarationAnalysis.declaredGettersOf(DeclarationAnalysis.java:54)
             *         at de.informaticum.xjc.InspectionPlugin.runClass(InspectionPlugin.java:29)
             *         at de.informaticum.xjc.AbstractPlugin.run(AbstractPlugin.java:60)
             *         at com.sun.tools.xjc.model.Model.generateCode(Model.java:262)
             *         at org.apache.cxf.maven_plugin.XSDToJavaRunner.run(XSDToJavaRunner.java:179)
             *         at org.apache.cxf.maven_plugin.XSDToJavaRunner.main(XSDToJavaRunner.java:360)
             *
             * ===> Let's do the best guess: */
            return guessGetterName(field);
        }
    }

}
