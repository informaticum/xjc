package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.XjcPropertyGuesser.guessGetterName;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessSetterName;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.CustomizableOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OutlineAnalysis {
    ;

    private static final Logger LOG = LoggerFactory.getLogger(OutlineAnalysis.class);

    /**
     * @param pakkage
     *            the requested package
     * @return the fully-qualified name of the given package
     */
    public static final String fullNameOf(final PackageOutline pakkage) {
        return pakkage._package().name();
    }

    /**
     * @param type
     *            the requested type
     * @return the fully-qualified name of the given type
     */
    public static final String fullNameOf(final CustomizableOutline type) {
        return type.getImplClass().fullName();
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

    /**
     * Values of the returned {@link LinkedHashMap} might be {@code null}: A {@linkplain FieldOutline field outline} might not map onto an according {@linkplain JFieldVar property}
     * (for whatever reason).
     * 
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
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
        if ((clazz.implClass.fields().size() - properties.size()) != 0) {
            final var noncaused = new HashMap<>(clazz.implClass.fields());
            noncaused.values().removeAll(properties.values());
            LOG.warn("Class [{}] contains fields that are not caused by declared properties: {}", clazz.implClass.fullName(), noncaused.keySet());
        }
        return properties;
    }

    /**
     * Values of the returned {@link LinkedHashMap} cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according {@linkplain JFieldVar
     * property} (for whatever reason), it is not contained in the returned result.
     * 
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> generatedPropertiesOf(final ClassOutline clazz) {
        final var properties = declaredPropertiesOf(clazz);
        properties.entrySet().removeIf(property -> property.getValue() == null);
        return properties;
    }

    /**
     * Values of the returned {@link LinkedHashMap} might be {@code null}: A {@linkplain FieldOutline field outline} might not map onto an according {@linkplain JFieldVar property}
     * (for whatever reason).
     * 
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class' fields comes first.
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
     * Values of the returned {@link LinkedHashMap} cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according {@linkplain JFieldVar
     * property} (for whatever reason), it is not contained in the returned result.
     * 
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class' fields comes first.
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> superAndGeneratedPropertiesOf(final ClassOutline clazz) {
        final var properties = superAndDeclaredPropertiesOf(clazz);
        properties.entrySet().removeIf(property -> property.getValue() == null);
        return properties;
    }

    public static final JMethod getConstructor(final ClassOutline clazz) {
        return CodeModelAnalysis.getConstructor(clazz.implClass);
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final Class<?>... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.implClass, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final JType... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.implClass, argumentTypes);
    }

    public static final JMethod getConstructor(final ClassOutline clazz, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).toArray(JType[]::new);
        return CodeModelAnalysis.getConstructor(clazz.implClass, argumentTypes);
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name) {
        return CodeModelAnalysis.getMethod(clazz.implClass, name);
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final Class<?>... argumentTypes) {
        return CodeModelAnalysis.getMethod(clazz.implClass, name, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final JType... argumentTypes) {
        return CodeModelAnalysis.getMethod(clazz.implClass, name, argumentTypes);
    }

    public static final JMethod getMethod(final ClassOutline clazz, final String name, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).toArray(JType[]::new);
        return CodeModelAnalysis.getMethod(clazz.implClass, name, argumentTypes);
    }

    /**
     * The returned {@link LinkedHashMap} is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    public static final LinkedHashMap<FieldOutline, JMethod> generatedGettersOf(final ClassOutline clazz) {
        final var getters = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var properties : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = properties.getKey();
            final var $property = properties.getValue();
            final var getterName = guessGetterName(attribute);
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

    /**
     * The returned {@link LinkedHashMap} is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    public static final LinkedHashMap<FieldOutline, JMethod> generatedSettersOf(final ClassOutline clazz) {
        final var setters = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var properties : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = properties.getKey();
            final var $property = properties.getValue();
            final var setterName = guessSetterName(attribute);
            final var setter = getMethod(clazz, setterName, $property.type());
            if (setter != null) {
                assertThat(setter.type()).isEqualTo(clazz.implClass.owner().VOID);
                setters.put(attribute, setter);
            } else {
                LOG.error("There is no setter method [{}] for property [{}] of class [{}].", setterName, $property.name(), clazz.implClass.fullName());
            }
        }
        return setters;
    }

}
