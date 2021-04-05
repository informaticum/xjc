package de.informaticum.xjc.util;

import static com.sun.tools.xjc.generator.bean.field.XjcPropertySpy.spyGetterName;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessSetterName;
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
     * {@linkplain JFieldVar property} (for whatever reason).
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    private static final LinkedHashMap<FieldOutline, JFieldVar> declaredPropertiesOf(final ClassOutline clazz) {
        final var properties = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz == null) {
            return properties;
        }
        for (final var outline : clazz.getDeclaredFields()) {
            final var name = outline.getPropertyInfo().getName(false);
            final var $property = clazz.implClass.fields().get(name);
            if ($property == null) {
                LOG.warn("There is no according field in class [{}] for declared property [{}].", clazz.implClass.fullName(), name);
            }
            properties.put(outline, $property);
        }
        final var diff = clazz.implClass.fields().size() - properties.size();
        if (diff != 0) {
            LOG.warn("Class [{}] contains {} fields that are not caused by declared properties.", clazz.implClass.fullName(), diff);
        }
        return properties;
    }

    /**
     * Values cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according
     * {@linkplain JFieldVar property} (for whatever reason), it is not contained in the returned result.
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> generatedPropertiesOf(final ClassOutline clazz) {
        final var properties = declaredPropertiesOf(clazz);
        properties.entrySet().removeIf(property -> property.getValue() == null);
        return properties;
    }

    /**
     * Values might be {@code null}: A {@linkplain FieldOutline field outline} might not map onto an according
     * {@linkplain JFieldVar property} (for whatever reason).
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class'
     * fields comes first.
     */
    private static final LinkedHashMap<FieldOutline, JFieldVar> superAndDeclaredPropertiesOf(final ClassOutline clazz) {
        final var properties = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz == null) {
            return properties;
        }
        properties.putAll(superAndDeclaredPropertiesOf(clazz.getSuperClass()));
        properties.putAll(declaredPropertiesOf(clazz));
        return properties;
    }

    /**
     * Values cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according
     * {@linkplain JFieldVar property} (for whatever reason), it is not contained in the returned result.
     *
     * Return result is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class'
     * fields comes first.
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> superAndGeneratedPropertiesOf(final ClassOutline clazz) {
        final var properties = superAndDeclaredPropertiesOf(clazz);
        properties.entrySet().removeIf(property -> property.getValue() == null);
        return properties;
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final Class<?>... argumentTypes) {
        return getConstructor(clazz.implClass, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).collect(toList()));
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final List<? extends JType> argumentTypes) {
        return getConstructor(clazz.implClass, argumentTypes);
    }

    public static final JMethod getConstructor(final JDefinedClass $clazz, final JType... argumentTypes) {
        return getConstructor($clazz, asList(argumentTypes));
    }

    public static final JMethod getConstructor(final JDefinedClass $clazz, final List<? extends JType> argumentTypes) {
        final var $constructor = $clazz.getConstructor(argumentTypes.toArray(JType[]::new));
        if ($constructor != null) {
            return $constructor;
        } else {
            final var rawTypes = argumentTypes.stream().map(JType::erasure).toArray(JType[]::new);
            return $clazz.getConstructor(rawTypes);
        }
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).collect(toList());
        return getConstructor(clazz.implClass, argumentTypes);
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final Class<?>... argumentTypes) {
        return getMethod(clazz.implClass, name, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).collect(toList()));
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final List<? extends JType> argumentTypes) {
        return getMethod(clazz.implClass, name, argumentTypes);
    }

    public static final JMethod getMethod(final JDefinedClass $clazz, final String name, final JType... argumentTypes) {
        return getMethod($clazz, name, asList(argumentTypes));
    }

    public static final JMethod getMethod(final JDefinedClass $clazz, final String name, final List<? extends JType> argumentTypes) {
        final var $method = $clazz.getMethod(name, argumentTypes.toArray(JType[]::new));
        if ($method != null) {
            return $method;
        } else {
            final var rawTypes = argumentTypes.stream().map(JType::erasure).toArray(JType[]::new);
            return $clazz.getMethod(name, rawTypes);
        }
    }

    public static final LinkedHashMap<FieldOutline, JMethod> generatedGettersOf(final ClassOutline clazz) {
        final var getters = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var properties : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = properties.getKey();
            final var $property = properties.getValue();
            final var getterName = spyGetterName(attribute);
            final var getter = getMethod(clazz, getterName);
            if (getter != null) {
                assertThat(getter.type().boxify()).isEqualTo($property.type().boxify());
                getters.put(attribute, getter);
            } else {
                LOG.error("There is no getter method [{}] for property {} of class [{}].", getterName, $property.name(), clazz.implClass.fullName());
            }
        }
        return getters;
    }

    public static final LinkedHashMap<FieldOutline, JMethod> generatedSettersOf(final ClassOutline clazz) {
        final var setters = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var properties : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = properties.getKey();
            final var $property = properties.getValue();
            final var setterName = guessSetterName(attribute);
            final var setter = getMethod(clazz.implClass, setterName, $property.type());
            if (setter != null) {
                assertThat(setter.type()).isEqualTo(clazz.implClass.owner().VOID);
                setters.put(attribute, setter);
            } else {
                LOG.error("There is no setter method [{}] for property {} of class [{}].", setterName, $property.name(), clazz.implClass.fullName());
            }
        }
        return setters;
    }

}
