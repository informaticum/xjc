package de.informaticum.xjc.util;

import static com.sun.tools.xjc.generator.bean.field.XjcPropertySpy.spyGetterName;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.LinkedHashMap;
import java.util.List;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OutlineAnalysis {
    ;

    private static final Logger LOG = LoggerFactory.getLogger(OutlineAnalysis.class);

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

    /**
     * Values might be {@code null}: A {@linkplain FieldOutline field outline} might not map onto an according
     * {@linkplain JFieldVar code field} (for whatever reason).
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    private static final LinkedHashMap<FieldOutline, JFieldVar> declaredFieldsOf(final ClassOutline clazz) {
        final var fields = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz == null) {
            return fields;
        }
        for (final var outline : clazz.getDeclaredFields()) {
            final var name = outline.getPropertyInfo().getName(false);
            final var $clazz = outline.parent().getImplClass();
            final var $field = $clazz.fields().get(name);
            if ($field == null) {
                LOG.warn("There is no according field in class [{}] for declared property [{}].", $clazz.fullName(), name);
            }
            fields.put(outline, $field);
        }
        final var diff = clazz.getImplClass().fields().size() - fields.size();
        if (diff != 0) {
            LOG.warn("Class [{}] contains {} fields that are not caused by declared properties.", clazz.getImplClass().fullName(), diff);
        }
        return fields;
    }

    /**
     * Values cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according
     * {@linkplain JFieldVar code field} (for whatever reason), it is not contained in the returned result.
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> generatedFieldsOf(final ClassOutline clazz) {
        final var fields = declaredFieldsOf(clazz);
        fields.entrySet().removeIf(field -> field.getValue() == null);
        return fields;
    }

    /**
     * Values might be {@code null}: A {@linkplain FieldOutline field outline} might not map onto an according
     * {@linkplain JFieldVar code field} (for whatever reason).
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class'
     * fields comes first.
     */
    private static final LinkedHashMap<FieldOutline, JFieldVar> superAndDeclaredFieldsOf(final ClassOutline clazz) {
        final var fields = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz == null) {
            return fields;
        }
        fields.putAll(superAndDeclaredFieldsOf(clazz.getSuperClass()));
        fields.putAll(declaredFieldsOf(clazz));
        return fields;
    }

    /**
     * Values cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according
     * {@linkplain JFieldVar code field} (for whatever reason), it is not contained in the returned result.
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class'
     * fields comes first.
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> superAndGeneratedFieldsOf(final ClassOutline clazz) {
        final var fields = superAndDeclaredFieldsOf(clazz);
        fields.entrySet().removeIf(field -> field.getValue() == null);
        return fields;
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final Class<?>... argTypes) {
        return getConstructor(clazz.getImplClass(), stream(argTypes).map(clazz.parent().getCodeModel()::ref).collect(toList()));
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final List<? extends JType> argTypes) {
        return getConstructor(clazz.getImplClass(), argTypes);
    }

    public static final JMethod getConstructor(final JDefinedClass $clazz, final JType... argTypes) {
        return getConstructor($clazz, asList(argTypes));
    }

    public static final JMethod getConstructor(final JDefinedClass $clazz, final List<? extends JType> argTypes) {
        final var $constructor = $clazz.getConstructor(argTypes.toArray(JType[]::new));
        if ($constructor != null) {
            return $constructor;
        } else {
            final var rawTypes = argTypes.stream().map(JType::erasure).toArray(JType[]::new);
            return $clazz.getConstructor(rawTypes);
        }
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> argTypes) {
        return getConstructor(clazz.getImplClass(), argTypes.values().stream().map(JFieldVar::type).collect(toList()));
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final Class<?>... argTypes) {
        return getMethod(clazz.getImplClass(), name, stream(argTypes).map(clazz.parent().getCodeModel()::ref).collect(toList()));
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final List<? extends JType> argTypes) {
        return getMethod(clazz.getImplClass(), name, argTypes);
    }

    public static final JMethod getMethod(final JDefinedClass $clazz, final String name, final JType... argTypes) {
        return getMethod($clazz, name, asList(argTypes));
    }

    public static final JMethod getMethod(final JDefinedClass $clazz, final String name, final List<? extends JType> argTypes) {
        final var $method = $clazz.getMethod(name, argTypes.toArray(JType[]::new));
        if ($method != null) {
            return $method;
        } else {
            final var rawTypes = argTypes.stream().map(JType::erasure).toArray(JType[]::new);
            return $clazz.getMethod(name, rawTypes);
        }
    }

    public static final LinkedHashMap<FieldOutline, JMethod> generatedGettersOf(final ClassOutline clazz) {
        final var getters = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var fields : generatedFieldsOf(clazz).entrySet()) {
            final var outline = fields.getKey();
            final var $field = fields.getValue();
            final var name = spyGetterName(outline);
            final var getter = getMethod(clazz, name);
            if (getter != null) {
                assertThat(getter.type().boxify()).isEqualTo($field.type().boxify());
                getters.put(outline, getter);
            } else {
                LOG.error("There is no getter method [{}] for field {} of class [{}].", name, $field.name(), clazz.getImplClass().fullName());
            }
        }
        return getters;
    }

}
