package de.informaticum.xjc.plugins;

import java.util.Map.Entry;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

public final class PropertyAccessor {

    public final ClassOutline clazz;

    public final FieldOutline field;

    public final JDefinedClass $ImplClass;

    public final JFieldVar $field;

    public final JMethod $method;

    public PropertyAccessor(final Entry<? extends FieldOutline, ? extends Entry<? extends JFieldVar, ? extends JMethod>> accessor) {
        this.clazz = accessor.getKey().parent();
        this.field = accessor.getKey();
        this.$ImplClass = accessor.getKey().parent().getImplClass();
        this.$field = accessor.getValue().getKey();
        this.$method = accessor.getValue().getValue();
        // TODO: Reuse something from com.sun.tools.xjc.outline.FieldAccessor or any of the sub-classes?
    }

}
