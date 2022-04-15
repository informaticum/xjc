package de.informaticum.xjc.plugins;

import java.util.Map.Entry;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

public final class PropertyAccessor {

    public final ClassOutline clazz;

    public final FieldOutline attribute;

    public final JDefinedClass $ImplClass;

    public final JFieldVar $property;

    public final JMethod $method;

    public PropertyAccessor(final Entry<? extends FieldOutline, ? extends Entry<? extends JFieldVar, ? extends JMethod>> accessor) {
        this.clazz = accessor.getKey().parent();
        this.attribute = accessor.getKey();
        this.$ImplClass = accessor.getKey().parent().getImplClass();
        this.$property = accessor.getValue().getKey();
        this.$method = accessor.getValue().getValue();
    }

}
