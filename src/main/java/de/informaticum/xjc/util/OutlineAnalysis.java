package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CodeModelAnalysis.deoptionalisedTypeFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.emptyImmutableInstanceOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.emptyModifiableInstanceOf;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import com.sun.codemodel.JExpression;
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
    private static final String ILLEGAL_DEFAULT_VALUE = "Lexical representation of the existing default value for [{}] is [{}]!";

    /**
     * Returns the fully-qualified name of the given {@linkplain PackageOutline package}.
     * 
     * @param pakkage
     *            the requested package
     * @return the fully-qualified name of the given package
     */
    public static final String fullNameOf(final PackageOutline pakkage) {
        return pakkage._package().name();
    }

    /**
     * Returns the fully-qualified name of the given {@linkplain CustomizableOutline type}.
     * 
     * @param type
     *            the requested type
     * @return the fully-qualified name of the given type
     */
    public static final String fullNameOf(final CustomizableOutline type) {
        return type.getImplClass().fullName();
    }

    /**
     * Returns {@code true} iff this attribute is required (a.k.a. mandatory).
     * 
     * @param attribute
     *            the attribute to analyse
     * @return {@code true} if this attribute is required; {@code false} otherwise
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
            // There is no "#isRequired()" for "CValuePropertyInfo", so this does not work:
            // return ((CValuePropertyInfo) property).isRequired();
            return false;
        } else {
            return false;
        }
    }

    /**
     * Returns {@code true} iff this attribute is optional (a.k.a. not required/not mandatory).
     * 
     * @param attribute
     *            the attribute to analyse
     * @return {@code true} if this attribute is optional; {@code false} otherwise
     * @see #isRequired(FieldOutline)
     */
    public static final boolean isOptional(final FieldOutline attribute) {
        return !isRequired(attribute);
    }

    /* Do not (!) assign the following values. Instead, let Java do the initialisation. */
    /* In result, each field's value will be defaulted as specified by the JLS.         */
    /* --> https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html     */
    private static boolean DEFAULT_BOOLEAN; /* no assignment, defaulted by Java instead */
    private static byte    DEFAULT_BYTE   ; /* no assignment, defaulted by Java instead */
    private static char    DEFAULT_CHAR   ; /* no assignment, defaulted by Java instead */
    private static double  DEFAULT_DOUBLE ; /* no assignment, defaulted by Java instead */
    private static float   DEFAULT_FLOAT  ; /* no assignment, defaulted by Java instead */
    private static int     DEFAULT_INT    ; /* no assignment, defaulted by Java instead */
    private static long    DEFAULT_LONG   ; /* no assignment, defaulted by Java instead */
    private static short   DEFAULT_SHORT  ; /* no assignment, defaulted by Java instead */

    /**
     * Returns the the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any XSD attribute with a given lexical value</dt>
     * <dd>{@linkplain com.sun.tools.xjc.model.CDefaultValue#compute(com.sun.tools.xjc.outline.Outline) the according Java expression} is chosen if it can be computed,</dd>
     * <dt>for any primitive type</dt>
     * <dd><a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">the according Java default value</a> is chosen,</dd>
     * <dt>for any collection type</dt>
     * <dd>if requested (see parameter {@code initCollections}), the according {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#emptyModifiableInstanceOf(JType) modifiable}
     * or {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#emptyImmutableInstanceOf(JType) unmodifiable} empty instance will be chosen (see parameter
     * {@code unmodifiableCollections}),</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param attribute
     *            the field to analyse
     * @param initCollections
     *            either to initialise collections or not
     * @param unmodifiableCollections
     *            if collections are initialised this specifies either to return an unmodifiable or a modifiable collection
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultExpressionFor(final FieldOutline attribute, final boolean initCollections, final boolean unmodifiableCollections) {
        final var outline = attribute.parent().parent();
        final var $model = outline.getCodeModel();
        final var property = attribute.getPropertyInfo();
        if (property.defaultValue != null) {
            assertThat(property.isCollection()).isFalse();
            final var $default = property.defaultValue.compute(outline);
            if ($default != null) { return Optional.of($default); }
            else { LOG.error(ILLEGAL_DEFAULT_VALUE, property.getName(false), null); }
        }
        final var $raw = attribute.getRawType();
        // TODO: Checken, ob es einen Fall gibt, wo einem Non-Primitive-Boolean (etc.) ein false zugewiesen wird, ohne
        //       dass ein Default-Wert existiert. Das darf nicht passieren. Ein "Boolean" ist initial "null".
        // TODO: Consider property.isUnboxable()? What to do in that case?
        // TODO: Consider property.isOptionalPrimitive()? What to do in that case?
        if      ($raw.equals($model.BOOLEAN)) { return Optional.of(lit(DEFAULT_BOOLEAN)); }
        else if ($raw.equals($model.BYTE   )) { return Optional.of(lit(DEFAULT_BYTE   )); }
        else if ($raw.equals($model.CHAR   )) { return Optional.of(lit(DEFAULT_CHAR   )); }
        else if ($raw.equals($model.DOUBLE )) { return Optional.of(lit(DEFAULT_DOUBLE )); }
        else if ($raw.equals($model.FLOAT  )) { return Optional.of(lit(DEFAULT_FLOAT  )); }
        else if ($raw.equals($model.INT    )) { return Optional.of(lit(DEFAULT_INT    )); }
        else if ($raw.equals($model.LONG   )) { return Optional.of(lit(DEFAULT_LONG   )); }
        else if ($raw.equals($model.SHORT  )) { return Optional.of(lit(DEFAULT_SHORT  )); }
        else if (property.isCollection() && initCollections) {
            return Optional.of(unmodifiableCollections ? emptyImmutableInstanceOf($raw) : emptyModifiableInstanceOf($raw));
        } else {
            return Optional.empty();
        }
    }

    /**
     * @see #guessFactoryName(ClassOutline)
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.model.CElementInfo, com.sun.tools.xjc.outline.Aspect,
     *      com.sun.tools.xjc.outline.Aspect)
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl, com.sun.codemodel.JClass)
     */
    public static final String CREATE = "create";

    /**
     * @see #guessGetterName(FieldOutline)
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String GET = "get";

    /**
     * @see #guessGetterName(FieldOutline)
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String IS = "is";

    /**
     * @see #guessSetterName(FieldOutline)
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)
     */
    public static final String SET = "set";

    /**
     * @see #guessWitherName(FieldOutline)
     */
    public static final String WITH = "with";

    /**
     * Guesses the name of the according getter method for a given field. Whether or not the field is boolean, the return value may start with prefix {@value #IS} or with prefix
     * {@value #GET}. Further, the value of {@link com.sun.tools.xjc.Options#enableIntrospection} is also considered to decide that prefix. Doing so, this method should return
     * similar values compared to the names used for the generated getter methods.
     *
     * @param attribute
     *            the given field
     * @return the name of the according getter method
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String guessGetterName(final FieldOutline attribute) {
        final var isBoolean = attribute.parent().parent().getCodeModel().BOOLEAN.equals(attribute.getRawType().boxify().getPrimitiveType());
        if (attribute.parent().parent().getModel().options.enableIntrospection) {
            return (attribute.getRawType().isPrimitive() && isBoolean ? IS : GET) + attribute.getPropertyInfo().getName(true);
        } else {
            return (isBoolean ? IS : GET) + attribute.getPropertyInfo().getName(true);
        }
    }

    /**
     * Guesses the name of the according setter method for a given field. The return value will start with prefix {@value #SET}. Doing so, this method should return similar values
     * compared to the names used for the generated setter methods.
     *
     * @param attribute
     *            the given field
     * @return the name of the according setter method
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)
     */
    public static final String guessSetterName(final FieldOutline attribute) {
        return SET + attribute.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according wither method for a given field. The return value will start with prefix {@value #WITH}.
     *
     * @param attribute
     *            the given field
     * @return the name of the according wither method
     */
    public static final String guessWitherName(final FieldOutline attribute) {
        return WITH + attribute.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according factory method for a given class. The return value will start with prefix {@value #CREATE}. Further, the
     * {@linkplain com.sun.tools.xjc.model.CClassInfo#getSqueezedName() whole class hierarchy} is considered to create the factory name. Doing so, this method should return similar
     * values compared to the names used for the object-factories' population methods.
     *
     * @param clazz
     *            the given class
     * @return the name of the according factory method
     * @see com.sun.tools.xjc.model.CClassInfo#getSqueezedName()
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.model.CElementInfo, com.sun.tools.xjc.outline.Aspect,
     *      com.sun.tools.xjc.outline.Aspect)
     * @see com.sun.tools.xjc.generator.bean.ObjectFactoryGeneratorImpl#populate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl, com.sun.codemodel.JClass)
     */
    public static final String guessFactoryName(final ClassOutline clazz) {
        return CREATE + clazz.target.getSqueezedName();
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
    public static final <K, T extends Map<? extends K, ?>> T filter(final T all, final Predicate<? super K> filter) {
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
