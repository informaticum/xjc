package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CodeModelAnalysis.deoptionalisedTypeFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.emptyImmutableInstanceOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.emptyModifiableInstanceOf;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import com.sun.codemodel.JDefinedClass;
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

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to {@code com.sun.tools.xjc.outline.*} types.
 */
public enum OutlineAnalysis {
    ;

    private static final Logger LOG = getLogger(OutlineAnalysis.class);
    private static final String MISSING_JAVA_DEFAULT_VALUE = "Lexical Java representation of the existing XSD default value for [{}#{}] is [{}]!";

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
     * Returns the fully-qualified Javadoc name of the given {@linkplain CustomizableOutline type}. Nested classes will be written as {@code de.package.OuterClass.NestedClass},
     * generic types (e.g., {@code java.util.Collection<String>} will be erased (e.g., {@code java.util.Collection}).
     *
     * @param type
     *            the requested type
     * @return the fully-qualified Javadoc name of the given type
     */
    public static final String javadocNameOf(final CustomizableOutline type) {
        return CodeModelAnalysis.javadocNameOf(type.getImplClass());
    }

    /**
     * Returns {@code true} iff this field is required (a.k.a. mandatory).
     *
     * @param field
     *            the field to analyse
     * @return {@code true} if this field is required; {@code false} otherwise
     * @see #isOptional(FieldOutline)
     */
    public static final boolean isRequired(final FieldOutline field) {
        final var property = field.getPropertyInfo();
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
     * Returns {@code true} iff this field is optional (a.k.a. not required/not mandatory).
     *
     * @param field
     *            the field to analyse
     * @return {@code true} if this field is optional; {@code false} otherwise
     * @see #isRequired(FieldOutline)
     */
    public static final boolean isOptional(final FieldOutline field) {
        return !isRequired(field);
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
     * Returns the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any field with a given lexical value</dt>
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
     * @param field
     *            the field to analyse
     * @param initCollections
     *            either to initialise collections or not
     * @param unmodifiableCollections
     *            if collections are initialised this specifies either to return an unmodifiable or a modifiable collection
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultExpressionFor(final FieldOutline field, final boolean initCollections, final boolean unmodifiableCollections) {
        final var outline = field.parent().parent();
        final var $model = outline.getCodeModel();
        final var property = field.getPropertyInfo();
        if (property.defaultValue != null) {
            assertThat(property.isCollection()).isFalse();
            final var $default = property.defaultValue.compute(outline);
            if ($default != null) {
                return Optional.of($default);
            } else {
                /*
                 * CPropertyInfo#defaultValue vom Typ CDefaultValue werden in CDefaultValue#create(TypeUse,XmlString)
                 * mit einem TypeUse erstellt. Und diese (für diverse Standard-XSD-Typen in CBuiltinLeafInfo erstellten)
                 * werden am Ende von SimpleTypeBuilder in einer Map, konkret SimpleTypeBuilder#builtinConversions
                 * vorgehalten. Und etliche dieser CBuiltinLeafInfo davon sind konkret ein NoConstantBuiltin. Und leider
                 * kann das (wie der Name sagt) für eine XSD-Konstante keinen passenden Java-Code erstellen, vgl.
                 * NoConstantBuiltin#createConstant(Outline, XmlString).
                 * 
                 * Für alle Varianten, wo dieses NoConstantBuiltin zum Einsatz kommt, kann es passieren, dass es zwar
                 * einen deklarierten Default-Value gibt -- aber gleichzeitig keine Java-Repräsentation von diesem
                 * Default-Value. 
                 */
                LOG.error(MISSING_JAVA_DEFAULT_VALUE, field.parent().getImplClass().fullName(), property.getName(false), $default);
            }
        }
        final var $raw = field.getRawType();
        // TODO: Checken, ob es einen Fall gibt, wo einem Non-Primitive-Boolean (etc.) ein false zugewiesen wird, ohne
        //       dass ein Default-Wert existiert. Das darf nicht passieren. Ein "Boolean" ist initial "null".
        //       (Vgl. com.sun.tools.xjc.model.CBuiltinLeafInfo#BOOLEAN_ZERO_OR_ONE oder
        //       com.sun.tools.xjc.runtime.ZeroOneBooleanAdapter)
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
     * @see #guessBuilderName(ClassOutline)
     */
    public static final String BUILDER = "Builder";

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
     * @see #guessWitherName(FieldOutline)
     */
    public static final String WITHOUT = "without";

    /**
     * @see #guessAdderName(FieldOutline)
     */
    public static final String ADD = "add";

    /**
     * @see #guessRemoverName(FieldOutline)
     */
    public static final String REMOVE = "remove";

    /**
     * @see #guessWithAdditionalName(FieldOutline)
     */
    private static final String WITH_ADDITIONAL = "withAdditional";

    /**
     * @see #guessWithoutSpecificName(FieldOutline)
     */
    private static final String WITHOUT_SPECIFIC = "withoutSpecific";

    /**
     * Guesses the name of the according getter method for a given field. Whether or not the field is boolean, the return value may start with prefix {@value #IS} or with prefix
     * {@value #GET}. Further, the value of {@link com.sun.tools.xjc.Options#enableIntrospection} is also considered to decide that prefix. Doing so, this method should return
     * similar values compared to the names used for the generated getter methods.
     *
     * @param field
     *            the given field
     * @return the name of the according getter method
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar#getGetterMethod()
     */
    public static final String guessGetterName(final FieldOutline field) {
        final var isBoolean = field.parent().parent().getCodeModel().BOOLEAN.equals(field.getRawType().boxify().getPrimitiveType());
        if (field.parent().parent().getModel().options.enableIntrospection) {
            return (field.getRawType().isPrimitive() && isBoolean ? IS : GET) + field.getPropertyInfo().getName(true);
        } else {
            return (isBoolean ? IS : GET) + field.getPropertyInfo().getName(true);
        }
    }

    /**
     * Guesses the name of the according setter method for a given field. The return value will start with prefix {@value #SET}. Doing so, this method should return similar values
     * compared to the names used for the generated setter methods.
     *
     * @param field
     *            the given field
     * @return the name of the according setter method
     * @see com.sun.tools.xjc.generator.bean.field.AbstractFieldWithVar.Accessor#fromRawValue(com.sun.codemodel.JBlock, String, com.sun.codemodel.JExpression)
     */
    public static final String guessSetterName(final FieldOutline field) {
        return SET + field.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according wither method for a given field. The return value will start with prefix {@value #WITH}.
     *
     * @param field
     *            the given field
     * @return the name of the according wither method
     */
    public static final String guessWitherName(final FieldOutline field) {
        return WITH + field.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according withouter method for a given field. The return value will start with prefix {@value #WITHOUT}.
     *
     * @param field
     *            the given field
     * @return the name of the according withouter method
     */
    public static final String guessWithouterName(final FieldOutline field) {
        return WITHOUT + field.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according adder method for a given field. The return value will start with prefix {@value #ADD}.
     *
     * @param field
     *            the given field
     * @return the name of the according adder method
     */
    public static final String guessAdderName(final FieldOutline field) {
        return ADD + field.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according remover method for a given field. The return value will start with prefix {@value #REMOVE}.
     *
     * @param field
     *            the given field
     * @return the name of the according adder method
     */
    public static final String guessRemoverName(final FieldOutline field) {
        return REMOVE + field.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according adder method for a given field. The return value will start with prefix {@value #WITH_ADDITIONAL}.
     *
     * @param field
     *            the given field
     * @return the name of the according adder method
     */
    public static final String guessWithAdditionalName(final FieldOutline field) {
        return WITH_ADDITIONAL + field.getPropertyInfo().getName(true);
    }

    /**
     * Guesses the name of the according remover method for a given field. The return value will start with prefix {@value #WITHOUT_SPECIFIC}.
     *
     * @param field
     *            the given field
     * @return the name of the according remover method
     */
    public static final String guessWithoutSpecificName(final FieldOutline field) {
        return WITHOUT_SPECIFIC + field.getPropertyInfo().getName(true);
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
     * Guesses the name of the according embedded builder class for a given class. Returns {@value #BUILDER} in general. However, different names are essential because nested type
     * {@code XYZ} cannot hide an enclosing type {@code XYZ}. Thus, in the very rare case where the class name equals the embedded builder's name, the return value of this method
     * gets prefixed with the class name.
     * 
     * @param clazz
     *            the given class
     * @return the name of the according builder class
     */
    public static final String guessBuilderName(final ClassOutline clazz) {
        final var prefix = BUILDER.equals(clazz.getImplClass().name()) ? clazz.getImplClass().name() : "";
        return prefix + BUILDER;
    }

    /*
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
        for (final var field : clazz.getDeclaredFields()) {
            final var name = field.getPropertyInfo().getName(false);
            final var $field = clazz.getImplClass().fields().get(name);
            if ($field == null) {
                LOG.warn("There is no according Java field in class [{}] for declared outline field [{}].", clazz.getImplClass().fullName(), name);
            }
            properties.put(field, $field);
        }
        if ((clazz.getImplClass().fields().size() - properties.size()) != 0) {
            final var $fields = new HashMap<>(clazz.getImplClass().fields());
            $fields.values().removeAll(properties.values());
            LOG.warn("Java class [{}] contains fields that are not caused by declared outline fields: {}", clazz.getImplClass().fullName(), $fields.keySet());
        }
        return properties;
    }

    /**
     * Returns all declared properties of a given {@link ClassOutline class outline}, but only those with an according generated according field variable.
     *
     * Values of the returned {@link LinkedHashMap} cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according {@linkplain JFieldVar
     * property} (for whatever reason), it is not contained in the returned result.
     *
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     *
     * @param clazz
     *            the class to analyse
     * @return a map of all declared properties (each with its according field-outline and the generated field-variable)
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> generatedPropertiesOf(final ClassOutline clazz) {
        final var properties = declaredPropertiesOf(clazz);
        properties.entrySet().removeIf(property -> property.getValue() == null);
        return properties;
    }

    /*
     * Values of the returned {@link LinkedHashMap} might be {@code null}: A {@linkplain FieldOutline field outline} might not map onto an according {@linkplain JFieldVar property}
     * (for whatever reason).
     *
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class' fields comes first.
     */
    private static final LinkedHashMap<FieldOutline, JFieldVar> superAndDeclaredPropertiesOf(final ClassOutline clazz) {
        final var properties = new LinkedHashMap<FieldOutline, JFieldVar>();
        if (clazz != null) {
            properties.putAll(superAndDeclaredPropertiesOf(clazz.getSuperClass()));
            properties.putAll(declaredPropertiesOf(clazz));
        }
        return properties;
    }

    /**
     * Returns all declared properties and all super properties of a given {@link ClassOutline class outline}, but only those with an according generated according field variable.
     *
     * Values of the returned {@link LinkedHashMap} cannot be {@code null}: If a {@linkplain FieldOutline field outline} is not mapped onto an according {@linkplain JFieldVar
     * property} (for whatever reason), it is not contained in the returned result.
     *
     * Further, the result map is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()} and super class' fields comes first.
     *
     * @param clazz
     *            the class to analyse
     * @return a map of all declared and all super properties (each with its according field-outline and the generated field-variable)
     */
    public static final LinkedHashMap<FieldOutline, JFieldVar> superAndGeneratedPropertiesOf(final ClassOutline clazz) {
        final var properties = superAndDeclaredPropertiesOf(clazz);
        properties.entrySet().removeIf(property -> property.getValue() == null);
        return properties;
    }

    /**
     * Runs a filter predicate in order to remove all entries where the entry's key does not match.
     *
     * @param all
     *            a map of all values so far
     * @param filter
     *            the filter to apply
     * @param <K>
     *            map key type
     * @param <M>
     *            map type
     * @return a map of all values that satisfy the filter predicate
     */
    public static final <K, M extends Map<? extends K, ?>> M filter(final M all, final Predicate<? super K> filter) {
        // TODO: Is there anything we can reuse instead of providing this custom filter function?
        all.entrySet().removeIf(e -> filter.negate().test(e.getKey()));
        return all;
    }

    /**
     * Inspects the default constructor of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @return the default constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final ClassOutline clazz) {
        return CodeModelAnalysis.getConstructor(clazz.getImplClass());
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final ClassOutline... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.getImplClass(), stream(argumentTypes).map(ClassOutline::getImplClass).toArray(JType[]::new));
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final Class<?>... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.getImplClass(), stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final JType... argumentTypes) {
        return CodeModelAnalysis.getConstructor(clazz.getImplClass(), argumentTypes);
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param properties
     *            the properties indicating the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final ClassOutline clazz, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).toArray(JType[]::new);
        return CodeModelAnalysis.getConstructor(clazz.getImplClass(), argumentTypes);
    }

    /**
     * Looks for all constructors that matches a specific predicate.
     *
     * @param clazz
     *            the class to analyse
     * @param filter
     *            the predicate to use when filtering the list of all constructors
     * @return a list of all constructor matching the given predicate
     */
    public static final List<JMethod> getConstructors(final ClassOutline clazz, final Predicate<? super JMethod> filter) {
        return CodeModelAnalysis.getConstructors(clazz.getImplClass(), filter);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param name
     *            the method name
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name) {
        return CodeModelAnalysis.getMethod(clazz.getImplClass(), name);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param name
     *            the method name
     * @param argumentTypes
     *            the method's signature types
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name, final Class<?>... argumentTypes) {
        return CodeModelAnalysis.getMethod(clazz.getImplClass(), name, stream(argumentTypes).map(clazz.parent().getCodeModel()::ref).toArray(JType[]::new));
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param name
     *            the method name
     * @param argumentTypes
     *            the method's signature types
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name, final JType... argumentTypes) {
        return CodeModelAnalysis.getMethod(clazz.getImplClass(), name, argumentTypes);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param clazz
     *            the class to analyse
     * @param name
     *            the method name
     * @param properties
     *            the properties indicating the method's signature types
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final ClassOutline clazz, final String name, final LinkedHashMap<? extends FieldOutline, ? extends JFieldVar> properties) {
        final var argumentTypes = properties.values().stream().map(JFieldVar::type).toArray(JType[]::new);
        return CodeModelAnalysis.getMethod(clazz.getImplClass(), name, argumentTypes);
    }

    /**
     * Returns {@linkplain #generatedPropertiesOf(ClassOutline) all declared fields} and each according getter method of a given {@link ClassOutline class outline}.
     *
     * The returned {@link LinkedHashMap} is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     *
     * @param clazz
     *            the class to analyse
     * @return a map of all declared fields and according getter method
     */
    public static final LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>> generatedGettersOf(final ClassOutline clazz) {
        final var getters = new LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>>();
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var field = property.getKey();
            final var $field = property.getValue();
            final var getterName = guessGetterName(field);
            final var $getterLookup = getMethod(clazz, getterName);
            if ($getterLookup.isPresent()) {
                final var $getter = $getterLookup.get();
                assertThat(
                  // this is the obvious assertion:
                  $getter.type().boxify().equals($field.type().boxify()) ||
                  // this is the alternative assertion (because PropertyPlugin modifies the getter methods):
                  (deoptionalisedTypeFor($getter.type().boxify()).isPresent() && deoptionalisedTypeFor($getter.type().boxify()).get().boxify().equals($field.type().boxify()))
                ).isTrue();
                getters.put(field, new SimpleImmutableEntry<>($field, $getter));
            } else {
                LOG.error("There is no getter method [#{}()] for declared field {} of class [{}].", getterName, $field.name(), clazz.getImplClass().fullName());
            }
        }
        return getters;
    }

    /**
     * Returns {@linkplain #generatedPropertiesOf(ClassOutline) all declared fields} and each according setter method of a given {@link ClassOutline class outline}.
     *
     * The returned {@link LinkedHashMap} is ordered similar to the result order of {@link ClassOutline#getDeclaredFields()}.
     *
     * @param clazz
     *            the class to analyse
     * @return a map of all declared fields and according setter method
     */
    public static final LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>> generatedSettersOf(final ClassOutline clazz) {
        final var setters = new LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>>();
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var field = property.getKey();
            final var $field = property.getValue();
            final var setterName = guessSetterName(field);
            final var $setterLookup = getMethod(clazz, setterName, $field.type());
            if ($setterLookup.isPresent()) {
                final var $setter = $setterLookup.get();
                assertThat($setter.type()).isEqualByComparingTo(clazz.getImplClass().owner().VOID);
                setters.put(field, new SimpleImmutableEntry<>($field, $setter));
            } else if (field.getPropertyInfo().isCollection()) {
                LOG.info("Expectedly, there is no setter method [{}#{}({})] for declared collection field [{}].", clazz.getImplClass().fullName(), setterName, $field.type(), $field.name());
            } else {
                LOG.error("Unexpectedly, there is no setter method [{}#{}({})] for declared field [{}].", clazz.getImplClass().fullName(), setterName, $field.type(), $field.name());
            }
        }
        return setters;
    }

    /**
     * Looks for an embedded class and returns it.
     *
     * @param clazz
     *            the class to analyse
     * @param name
     *            the name of the embedded class to look for
     * @return an {@link Optional} holding the embedded class if found; an {@linkplain Optional#empty() empty Optional} if not found
     */
    public static final Optional<JDefinedClass> getEmbeddedClass(final ClassOutline clazz, final String name) {
        return CodeModelAnalysis.getEmbeddedClass(clazz.getImplClass(), name);
    }

}
