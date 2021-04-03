package de.informaticum.xjc.util;

import static com.sun.tools.xjc.generator.bean.field.XjcAccessorSpy.spyGetterName;
import static de.informaticum.xjc.AbstractPlugin.NO_ARG;
import static java.util.Arrays.stream;
import java.util.LinkedHashMap;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OutlineAnalysis {
    ;

    private static final Logger LOG = LoggerFactory.getLogger(OutlineAnalysis.class);

    public static final JFieldVar fieldFor(final FieldOutline field) {
        final var name = field.getPropertyInfo().getName(false);
        final var $clazz = field.parent().getImplClass();
        final var $field = $clazz.fields().get(name);
        if ($field == null) {
            LOG.warn("There is no according field in class [{}] for declared property [{}].", $clazz.fullName(), name);
        }
        return $field;
    }

    public static final JType[] typesOf(final FieldOutline... fields) {
        return stream(fields).map(FieldOutline::getRawType).toArray(JType[]::new);
    }

    public static final JType[] typesOf(final JVar... fields) {
        return stream(fields).map(JVar::type).toArray(JType[]::new);
    }

    /**
     * Values can be {@code null}
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> declaredFieldsOf(final ClassOutline clazz) {
        final var fields = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz == null) {
            return fields;
        }
        for (final var declared : clazz.getDeclaredFields()) {
            fields.put(declared, fieldFor(declared));
        }
        final var diff = clazz.getImplClass().fields().size() - fields.size();
        if (diff != 0) {
            LOG.warn("Class [{}] contains {} fields that are not caused by declared properties.", clazz.getImplClass().fullName(), diff);
        }
        return fields;
    }

    /**
     * Values cannot be {@code null}
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> generatedFieldsOf(final ClassOutline clazz) {
        final var fields = declaredFieldsOf(clazz);
        fields.entrySet().removeIf(field -> field.getValue() == null);
        return fields;
    }

    /**
     * Values can be {@code null}
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> superAndDeclaredFieldsOf(final ClassOutline clazz) {
        final var fields = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz == null) {
            return fields;
        }
        fields.putAll(superAndDeclaredFieldsOf(clazz.getSuperClass()));
        fields.putAll(declaredFieldsOf(clazz));
        return fields;
    }

    /**
     * Values cannot be {@code null}
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> superAndGeneratedFieldsOf(final ClassOutline clazz) {
        final var fields = superAndDeclaredFieldsOf(clazz);
        fields.entrySet().removeIf(e -> e.getValue() == null);
        return fields;
    }

    public static final FieldOutline[] allValueConstructorArguments(final ClassOutline clazz) {
        return superAndGeneratedFieldsOf(clazz).keySet().toArray(FieldOutline[]::new);
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final JType... argTypes) {
        final var constructor = clazz.getImplClass().getConstructor(argTypes);
        if (constructor != null) {
            return constructor;
        } else {
            final var rawTypes = stream(argTypes).map(JType::erasure).toArray(JType[]::new);
            return clazz.getImplClass().getConstructor(rawTypes);
        }
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final FieldOutline... argTypes) {
        return getConstructor(clazz, typesOf(argTypes));
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final Class<?>... argTypes) {
        return getConstructor(clazz, stream(argTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final JVar... vars) {
        return getConstructor(clazz, typesOf(vars));
    }

    public static final LinkedHashMap<FieldOutline, JMethod> generatedGettersOf(final ClassOutline clazz) {
        final var result = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var entry : generatedFieldsOf(clazz).entrySet()) {
            final var outline = entry.getKey();
            final var field = entry.getValue();
            final var name = spyGetterName(outline);
            final var getter = getMethod(clazz, name, NO_ARG);
            if (getter != null && getter.type().boxify().equals(field.type().boxify())) {
                result.put(outline, getter);
            } else {
                LOG.error("There is no valid getter method [{}] for field {} of class [{}].", name, field.name(), clazz.getImplClass().fullName());
            }
        }
        return result;
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final JType... argTypes) {
        final var method = clazz.getImplClass().getMethod(name, argTypes);
        if (method != null) {
            return method;
        } else {
            final var rawTypes = stream(argTypes).map(JType::erasure).toArray(JType[]::new);
            return clazz.getImplClass().getMethod(name, rawTypes);
        }
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final FieldOutline... argTypes) {
        return getMethod(clazz, name, typesOf(argTypes));
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final Class<?>... argTypes) {
        return getMethod(clazz, name, stream(argTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final JVar... vars) {
        return getMethod(clazz, name, typesOf(vars));
    }

    public static final boolean isRequired(final FieldOutline attribute) {
        final var property = attribute.getPropertyInfo();
        if (property instanceof CElementPropertyInfo) {
            return ((CElementPropertyInfo) property).isRequired();
        } else if (property instanceof CReferencePropertyInfo) {
            return ((CReferencePropertyInfo) property).isRequired();
        } else if (property instanceof CAttributePropertyInfo) {
            return ((CAttributePropertyInfo) property).isRequired();
        } else if (property instanceof CValuePropertyInfo) {
            // return ((CValuePropertyInfo) property).isRequired();
            return false;
        } else {
            return false;
        }
    }

    public static final boolean isOptional(final FieldOutline attribute) {
        return !isRequired(attribute);
    }

}
