package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.OptionalAnalysis.deoptionalisedTypeFor;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessGetterName;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessSetterName;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
// TODO: Check all methods returning LinkedHashMap for variable name attribute/property/...
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
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
        return fullNameOf(pakkage._package());
    }

    /**
     * @param $package
     *            the requested package
     * @return the fully-qualified name of the given package
     */
    public static final String fullNameOf(final JPackage $package) {
        return $package.name();
    }

    /**
     * @param type
     *            the requested type
     * @return the fully-qualified name of the given type
     */
    public static final String fullNameOf(final CustomizableOutline type) {
        return fullNameOf(type.getImplClass());
    }

    /**
     * @param $Type
     *            the requested type
     * @return the fully-qualified name of the given type
     */
    public static final String fullNameOf(final JType $Type) {
        return $Type.fullName();
    }

    /**
     * Returns {@code true} iff this attribute is mandatory.
     * 
     * @param attribute
     *            the attribute to analyse
     * @return {@code true} if this attribute is mandatory; {@code false} otherwise
     * @see #isOptional(FieldOutline)
     */
    public static final boolean isRequired(final FieldOutline attribute) {
        final var property = attribute.getPropertyInfo();
        if (property instanceof CElementPropertyInfo) {
            // case (1/4): CElementPropertyInfo extends CPropertyInfo
            return ((CElementPropertyInfo) property).isRequired();
        } else if (property instanceof CReferencePropertyInfo) {
            // case (2/4): CReferencePropertyInfo extends CPropertyInfo
            return ((CReferencePropertyInfo) property).isRequired();
        } else if (property instanceof CAttributePropertyInfo) {
            // case (3/4): CAttributePropertyInfo extends CSingleTypePropertyInfo extends CPropertyInfo
            return ((CAttributePropertyInfo) property).isRequired();
        } else if (property instanceof CValuePropertyInfo) {
            // case (4/4): CValuePropertyInfo extends CSingleTypePropertyInfo extends CPropertyInfo
            // Currently, there is no similar "#isRequired()" for "CValuePropertyInfo", so this does not work:
            // return ((CValuePropertyInfo) property).isRequired();
            return false;
        } else {
            return false;
        }
    }

    /**
     * Returns {@code true} iff this attribute is not mandatory.
     * 
     * @param attribute
     *            the attribute to analyse
     * @return {@code true} if this attribute is not mandatory; {@code false} otherwise
     * @see #isRequired(FieldOutline)
     */
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

    /**
     * @param all
     *            a map of all values so far
     * @param filter
     *            the filter to apply
     * @return a map of all values that satisfy the filter predicate
     */
    public static final <K, T extends Map<? extends K, ?>> T filter(final T all, final Predicate<? super K> filter)  {
        // TODO: Is there anything we can reuse instead of providing this custom filter function?
        all.entrySet().removeIf(e -> filter.negate().test(e.getKey()));
        return all;
    }

    public static final Optional<JMethod> getConstructor(final ClassOutline clazz) {
        return CodeModelAnalysis.getConstructor(clazz.implClass);
    }

    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final Class<?>... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.implClass, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final JType... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.implClass, argumentTypes);
    }

    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).toArray(JType[]::new);
        return CodeModelAnalysis.getConstructor(clazz.implClass, argumentTypes);
    }

    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name) {
        return CodeModelAnalysis.getMethod(clazz.implClass, name);
    }

    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name, final Class<?>... argumentTypes) {
        return CodeModelAnalysis.getMethod(clazz.implClass, name, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name, final JType... argumentTypes) {
        return CodeModelAnalysis.getMethod(clazz.implClass, name, argumentTypes);
    }

    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).toArray(JType[]::new);
        return CodeModelAnalysis.getMethod(clazz.implClass, name, argumentTypes);
    }

    /**
     * The returned {@link LinkedHashMap} is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     */
    public static final LinkedHashMap<FieldOutline, JMethod> generatedGettersOf(final ClassOutline clazz) {
        final var getters = new LinkedHashMap<FieldOutline, JMethod>();
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var getterName = guessGetterName(attribute);
            final var $getterLookup = getMethod(clazz, getterName);
            if ($getterLookup.isPresent()) {
                final var $getter = $getterLookup.get();
                assertThat(
                  // this is the obvious assertion:
                  $getter.type().boxify().equals($property.type().boxify()) ||
                  // this is the alternative assertion (because PropertyPlugin modifies the getter methods):
                  (deoptionalisedTypeFor($getter.type().boxify()).isPresent() && deoptionalisedTypeFor($getter.type().boxify()).get().boxify().equals($property.type().boxify()))
                ).isTrue();
                getters.put(attribute, $getter);
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
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var setterName = guessSetterName(attribute);
            final var $setterLookup = getMethod(clazz, setterName, $property.type());
            if ($setterLookup.isPresent()) {
                final var $setter = $setterLookup.get();
                assertThat($setter.type()).isEqualTo(clazz.implClass.owner().VOID);
                setters.put(attribute, $setter);
            } else if (attribute.getPropertyInfo().isCollection()) {
                LOG.info("Expectedly, there is no setter method [{}#{}({})] for collection property [{}].", clazz.implClass.fullName(), setterName, $property.type(), $property.name());
            } else {
                LOG.error("Unexpectedly, there is no setter method [{}#{}({})] for property [{}].", clazz.implClass.fullName(), setterName, $property.type(), $property.name());
            }
        }
        return setters;
    }

}
