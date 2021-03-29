package de.informaticum.xjc.util;

import static com.sun.tools.xjc.generator.bean.field.XjcSpy.spyGetterName;
import static de.informaticum.xjc.AbstractPlugin.NO_ARG;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DeclarationAnalysis {
    ;

    private static final Logger LOG = LoggerFactory.getLogger(DeclarationAnalysis.class);

    public static final Map<FieldOutline, JFieldVar> declaredFieldsOf(final ClassOutline clazz) {
        final var result = new LinkedHashMap<FieldOutline, JFieldVar>();
        for (final var declared : clazz.getDeclaredFields()) {
            // TODO: check type equality
            result.put(declared, fieldFor(declared));
        }
        final var diff = clazz.getImplClass().fields().size() - result.size();
        if (diff != 0) {
            LOG.debug("Class [{}] contains {} fields that are not caused by declared properties.", clazz.getImplClass().fullName(), diff);
        }
        return result;
    }

    private static final JFieldVar fieldFor(final FieldOutline declared) {
        final var name = declared.getPropertyInfo().getName(false);
        final var field = declared.parent().getImplClass().fields().get(name);
        if (field == null) {
            LOG.error("There is no according field in class [{}] for declared property [{}].", declared.parent().getImplClass().fullName(), name);
        }
        return field;
    }

    public static final List<JFieldVar> undeclaredFieldsOf(final ClassOutline clazz) {
        final var declareds = declaredFieldsOf(clazz);
        final var nondeclared = new ArrayList<>(clazz.getImplClass().fields().values());
        nondeclared.removeAll(declareds.values());
        return nondeclared;
    }

    public static final Map<FieldOutline, JMethod> declaredGettersOf(final ClassOutline clazz) {
        final var result = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var entry : declaredFieldsOf(clazz).entrySet()) {
            if (entry.getValue() != null) {
                final var name = spyGetterName(entry.getKey());
                final var getter = clazz.getImplClass().getMethod(name, NO_ARG);
                if (getter != null && getter.type().boxify().equals(entry.getValue().type().boxify())) {
                    result.put(entry.getKey(), getter);
                } else {
                    LOG.error("There is no valid getter method [{}] for field {} of class [{}].", name, entry.getValue().name(), clazz.getImplClass().fullName());
                }
            } else {
                LOG.warn("Skip getter lookup for declared property [{}] of class [{}] because field does not exist neither.", entry.getKey().getPropertyInfo().getName(false),
                         clazz.getImplClass().fullName());
            }
        }
        return result;
    }

    public static final Map<JFieldVar, JMethod> undeclaredGettersOf(final ClassOutline clazz) {
        final var result = new LinkedHashMap<JFieldVar, JMethod>();
        for (final var field : undeclaredFieldsOf(clazz)) {
            final var name = spyGetterName(clazz, field);
            final var getter = clazz.getImplClass().getMethod(name, NO_ARG);
            if (getter != null && getter.type().boxify().equals(field.type().boxify())) {
                result.put(field, getter);
            } else {
                LOG.warn("There is no valid getter method [{}] for field {} of class [{}].", name, field.name(), clazz.getImplClass().fullName());
            }
        }
        return result;
    }

}
