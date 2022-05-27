package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._super;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptyNavigableSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableNavigableSet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import javax.xml.bind.JAXBElement;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JGenerable;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JMods;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to {@code com.sun.codemodel.*} types.
 */
public enum CodeModelAnalysis {
    ;

    /*pkg*/ static final String ATTHROWS_FIELD = "atThrows";
    /*pkg*/ static final String ATPARAMS_FIELD = "atParams";
    /*pkg*/ static final String GETTHROWS_METHOD = "getThrows";
    /*pkg*/ static final String JDOC_FIELD = "jdoc";
    /*pkg*/ static final String OUTER_FIELD = "outer";

    private static final Class<?>[] DIAMOND = {};

    private static final String UNEXPECTED_MODIFICATION = "WTF! The long-time existing constructor/factory-method has been modified ;-(";

    /**
     * Syntactic sugar, actually just a synonym of {@link com.sun.codemodel.JExpr#_super()}.
     */
    public static final JExpression $super = _super();

    /**
     * Syntactic sugar, actually just a synonym of {@link com.sun.codemodel.JExpr#_this()}.
     */
    public static final JExpression $this = _this();

    /**
     * Syntactic sugar, actually just a synonym of {@link com.sun.codemodel.JExpr#_null()}.
     */
    public static final JExpression $null = _null();

    private static boolean hasModifier(final JMods mods, final int lookup) {
        return (mods.getValue() & lookup) != 0;
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#PUBLIC} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code public}
     * @see #isProtected(JMods)
     * @see #isPrivate(JMods)
     * @see #isFinal(JMods)
     * @see #isStatic(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isTransient(JMods)
     * @see #isVolatile(JMods)
     */
    public static boolean isPublic(final JMods mods) {
        return hasModifier(mods, JMod.PUBLIC);
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#PROTECTED} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code protected}
     * @see #isPublic(JMods)
     * @see #isPrivate(JMods)
     * @see #isFinal(JMods)
     * @see #isStatic(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isTransient(JMods)
     * @see #isVolatile(JMods)
     */
    public static boolean isProtected(final JMods mods) {
        return hasModifier(mods, JMod.PROTECTED);
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#PRIVATE} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code private}
     * @see #isPublic(JMods)
     * @see #isProtected(JMods)
     * @see #isFinal(JMods)
     * @see #isStatic(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isTransient(JMods)
     * @see #isVolatile(JMods)
     */
    public static boolean isPrivate(final JMods mods) {
        return hasModifier(mods, JMod.PRIVATE);
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#FINAL} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code final}
     * @see #isPublic(JMods)
     * @see #isProtected(JMods)
     * @see #isPrivate(JMods)
     * @see #isStatic(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isTransient(JMods)
     * @see #isVolatile(JMods)
     */
    public static boolean isFinal(final JMods mods) {
        return hasModifier(mods, JMod.FINAL);
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#STATIC} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code static}
     * @see #isPublic(JMods)
     * @see #isProtected(JMods)
     * @see #isPrivate(JMods)
     * @see #isFinal(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isTransient(JMods)
     * @see #isVolatile(JMods)
     */
    public static boolean isStatic(final JMods mods) {
        return hasModifier(mods, JMod.STATIC);
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#TRANSIENT} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code transient}
     * @see #isPublic(JMods)
     * @see #isProtected(JMods)
     * @see #isPrivate(JMods)
     * @see #isFinal(JMods)
     * @see #isStatic(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isVolatile(JMods)
     */
    public static boolean isTransient(final JMods mods) {
        return hasModifier(mods, JMod.TRANSIENT);
    }

    /**
     * Checks whether or not a given {@linkplain JMods modifier group} includes the {@link JMod#VOLATILE} value.
     * 
     * @param mods
     *            the modifier group to analyse
     * @return {@code true} iff the given modifier group includes {@code volatile}
     * @see #isPublic(JMods)
     * @see #isProtected(JMods)
     * @see #isPrivate(JMods)
     * @see #isFinal(JMods)
     * @see #isStatic(JMods)
     * @see JMods#isAbstract()
     * @see JMods#isNative()
     * @see JMods#isSynchronized()
     * @see #isTransient(JMods)
     */
    public static boolean isVolatile(final JMods mods) {
        return hasModifier(mods, JMod.VOLATILE);
    }

    /**
     * Returns the erasure of all types.
     * 
     * @param $types
     *            the given types
     * @return the erasure of all types
     * @see JType#erasure()
     */
    public static JType[] erasure(final JType... $types) {
        return stream($types).map(JType::erasure).toArray(JType[]::new);
    }

    /**
     * Checks whether or not a given {@linkplain JType type} represents any of {@link OptionalDouble}, {@link OptionalInt}, or {@link OptionalLong}.
     * 
     * @param $type
     *            the type to analyse
     * @return {@code true} iff the give type is assignable to {@link OptionalDouble}, {@link OptionalInt}, or {@link OptionalLong}
     */
    public static final boolean isPrimitiveOptionalType(final JType $type) {
        final var $model = $type.owner();
        final var $raw = $type.erasure();
        return $model.ref(OptionalDouble.class).equals($raw) || $model.ref(OptionalInt.class).equals($raw) || $model.ref(OptionalLong.class).equals($raw);
    }

    /**
     * Checks whether or not a given {@linkplain JType type} represents any of {@link OptionalDouble}, {@link OptionalInt}, {@link OptionalLong}, or {@link Optional}.
     * 
     * @param $type
     *            the type to analyse
     * @return {@code true} iff the give type is assignable to {@link OptionalDouble}, {@link OptionalInt}, {@link OptionalLong}, or {@link Optional}
     */
    public static final boolean isOptionalType(final JType $type) {
        final var $model = $type.owner();
        final var $raw = $type.erasure();
        return isPrimitiveOptionalType($type) || $model.ref(Optional.class).equals($raw);
    }

    /**
     * Identifies the according {@code OptionalX} container class for the given {@linkplain JType type}.
     * 
     * @param $type
     *            the type to analyse
     * @return the according optional type (i.e., {@link JClass} of either {@link OptionalDouble} for {@code double}/{@link Double} type, {@link OptionalInt} for
     *         {@code int}/{@link Integer} type, {@link OptionalLong} for {@code long}/{@link Long} type, or {@link Optional Optional&lt;T&gt;} in any other case)
     */
    public static final JClass optionalTypeFor(final JType $type) {
        final var $model = $type.owner();
        final var $primitive = $type.unboxify();
        if ($model.DOUBLE.equals($primitive)) {
            assertThat(OptionalDouble.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalDouble.of(0.0d)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return $model.ref(OptionalDouble.class);
        } else if ($model.INT.equals($primitive)) {
            assertThat(OptionalInt.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalInt.of(0)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return $model.ref(OptionalInt.class);
        } else if ($model.LONG.equals($primitive)) {
            assertThat(OptionalLong.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(OptionalLong.of(0L)).withFailMessage(UNEXPECTED_MODIFICATION).hasValue(0);
            return $model.ref(OptionalLong.class);
        } else {
            assertThat(Optional.empty()).withFailMessage(UNEXPECTED_MODIFICATION).isNotPresent();
            assertThat(Optional.of("")).withFailMessage(UNEXPECTED_MODIFICATION).hasValue("");
            return $model.ref(Optional.class).narrow($type.boxify());
        }
    }

    /**
     * Identifies the according {@linkplain JType value type} represented by the given {@code OptionalX} container class. If there is such value type, an {@link Optional} of that
     * value type is returned. If the given {@linkplain JClass class} neither represents {@link OptionalDouble}, {@link OptionalInt}, {@link OptionalLong}, nor {@link Optional
     * Optional&lt;T&gt;} an {@linkplain Optional#empty() empty Optional} is returned.
     * 
     * @param $Class
     *            the {@code OptionalX} container class to deoptionalise
     * @return the deoptionalised type of the given class (i.e., {@link com.sun.codemodel.JCodeModel#DOUBLE double}/{@link com.sun.codemodel.JCodeModel#INT
     *         int}/{@link com.sun.codemodel.JCodeModel#LONG long} for {@link OptionalDouble}/{@link OptionalInt}/{@link OptionalLong}, or {@code T} for bound {@link Optional
     *         Optional&lt;T&gt;}, or {@code Object} for wildcard {@code Optional}/unbound {@code Optional})
     */
    public static final Optional<JType> deoptionalisedTypeFor(final JClass $Class) {
        final var $model = $Class.owner();
        final var $Raw = $Class.erasure();
        if ($model.ref(OptionalDouble.class).equals($Raw)) {
            return Optional.of($model.DOUBLE);
        } else if ($model.ref(OptionalInt.class).equals($Raw)) {
            return Optional.of($model.INT);
        } else if ($model.ref(OptionalLong.class).equals($Raw)) {
            return Optional.of($model.LONG);
        } else if ($model.ref(Optional.class).equals($Raw)) {
            final var $Narrowed = $Class.getTypeParameters();
            if ($Narrowed.isEmpty()) {
                return Optional.of($model.ref(Object.class));
            } else {
                assertThat($Narrowed.size() == 1);
                return Optional.of($Narrowed.get(0));
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks whether or not a given {@linkplain JType type} represents a {@link Collection} type.
     * 
     * @param $type
     *            the type to analyse
     * @return {@code true} iff the give type is assignable to {@link Collection}
     */
    public static final boolean isCollectionType(final JType $type) {
        final var $model = $type.owner();
        final var $Raw = $type.boxify().erasure();
        return $model.ref(Collection.class).isAssignableFrom($Raw);
    }

    /**
     * Identifies/supposes the first type parameter for the given class. If there is no type parameter, {@link Object} is supposed. If there is exactly one type parameter, that
     * parameter is returned. If there are more than one type parameter, the first parameter is returned.
     * 
     * @param $Class
     *            the class to analyse
     * @return the identified/supposed type parameter
     */
    public static final JClass typeParameterOf(final JClass $Class) {
        final var $model = $Class.owner();
        final var $Narrowed = $Class.getTypeParameters();
        if ($Narrowed.isEmpty()) {
            return $model.ref(Object.class);
        } else {
            return $Narrowed.get(0);
        }
    }

    /**
     * Generates the according PECS-compliant producer type (covariance) for the given parameterised type. For example, {@code List<String>} becomes {@code List<? extends String>}.
     * If the given type is not parameterised or is primitive, it will be returned immediately.
     * 
     * @param $type
     *            the given parameterised type
     * @return the according PECS-compliant producer type
     */
    public static final JType pecsProducerTypeOf(final JType $type) {
        final var $model = $type.owner();
        final var $JAXBElement = $model.ref(JAXBElement.class);
        final var $Class = $type.boxify();
        final var typeParameters = $Class.getTypeParameters();
        if (typeParameters.isEmpty()) {
            return $type;
        } else if ($JAXBElement.isAssignableFrom($Class.erasure())) {
            // TODO: Learn more about the nature of JAXBElement<X> parameters. When/Why do they appear?
            return $type;
        } else {
            // TODO: Do not build wildcard type of already-wildcard type
            return $Class.erasure().narrow(typeParameters.stream().map(JClass::wildcard).collect(toList()));
        }
    }

    /**
     * @param $type
     *            the collection type to analyse
     * @return the according invocation code to create an unmodifiable empty instance of the given type
     * @throws IllegalArgumentException
     *             iff there is no empty-collection instance of the given type
     */
    public static final JInvocation emptyImmutableInstanceOf(final JType $type)
    throws IllegalArgumentException {
        final var $model = $type.owner();
        final var $Collections = $model.ref(Collections.class);
        final var $Raw = $type.boxify().erasure();
        if ($model.ref(NavigableSet.class).isAssignableFrom($Raw)) {
            assertThat(emptyNavigableSet()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptyNavigableSet");
        } else if ($model.ref(SortedSet.class).isAssignableFrom($Raw)) {
            assertThat(emptySortedSet()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptySortedSet");
        } else if ($model.ref(Set.class).isAssignableFrom($Raw)) {
            assertThat(emptySet()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptySet");
        } else if ($model.ref(List.class).isAssignableFrom($Raw)) {
            assertThat(emptyList()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptyList");
        } else if ($model.ref(Collection.class).isAssignableFrom($Raw)) {
            assertThat(emptyList()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptyList");
        } else {
            // TODO: Optional<T> instead of IllegalArgumentException
            throw new IllegalArgumentException("There is no empty-collection instance of type " + $type);
        }
        // TODO: Handle <a href="https://docs.oracle.com/javase/tutorial/jaxb/intro/custom.html">jxb:globalBindings collectionType="java.util.Vector</a> accordingly
    }

    /**
     * @param $type
     *            the collection type to analyse
     * @return the according invocation code to create a modifiable empty instance of the given type
     * @throws IllegalArgumentException
     *             iff there is no default-collection instance of the given type
     */
    public static final JInvocation emptyModifiableInstanceOf(final JType $type)
    throws IllegalArgumentException {
        final var model = $type.owner();
        final var $Raw = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom($Raw)) {
            assertThat(new TreeSet<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(SortedSet.class).isAssignableFrom($Raw)) {
            assertThat(new TreeSet<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(Set.class).isAssignableFrom($Raw)) {
            assertThat(new HashSet<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(HashSet.class).narrow(DIAMOND));
        } else if (model.ref(List.class).isAssignableFrom($Raw)) {
            assertThat(new ArrayList<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else if (model.ref(Collection.class).isAssignableFrom($Raw)) {
            assertThat(new ArrayList<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else {
            // TODO: Optional<T> instead of IllegalArgumentException
            throw new IllegalArgumentException("There is no default-collection instance of type " + $type);
        }
        // TODO: Handle <a href="https://docs.oracle.com/javase/tutorial/jaxb/intro/custom.html">jxb:globalBindings collectionType="java.util.Vector</a> accordingly
    }

    /**
     * @param $type
     *            the collection type to analyse
     * @return the according invocation code to create a factory for a modifiable copy instance of the given type
     * @throws IllegalArgumentException
     *             if there is no copy-collection factory for the given type
     */
    public static final JInvocation copyFactoryFor(final JType $type)
    throws IllegalArgumentException {
        final var model = $type.owner();
        final var $Raw = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom($Raw)) {
            assertThat(new TreeSet<>(emptySortedSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(SortedSet.class).isAssignableFrom($Raw)) {
            assertThat(new TreeSet<>(emptySortedSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(Set.class).isAssignableFrom($Raw)) {
            assertThat(new HashSet<>(emptySet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(HashSet.class).narrow(DIAMOND));
        } else if (model.ref(List.class).isAssignableFrom($Raw)) {
            assertThat(new ArrayList<>(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else if (model.ref(Collection.class).isAssignableFrom($Raw)) {
            assertThat(new ArrayList<>(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else {
            // TODO: Optional<T> instead of IllegalArgumentException
            throw new IllegalArgumentException("There is no copy-collection factory for type " + $type);
        }
        // TODO: Handle <a href="https://docs.oracle.com/javase/tutorial/jaxb/intro/custom.html">jxb:globalBindings collectionType="java.util.Vector</a> accordingly
    }

    /**
     * @param $type
     *            the collection type to analyse
     * @return the according invocation code to create a factory for an unmodifiable view instance of the given type
     * @throws IllegalArgumentException
     *             if there is no unmodifiable-view-collection factory for the given type
     */
    public static final JInvocation unmodifiableViewFactoryFor(final JType $type)
    throws IllegalArgumentException {
        final var model = $type.owner();
        final var $Collections = model.ref(Collections.class);
        final var $Raw = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom($Raw)) {
            assertThat(unmodifiableNavigableSet(emptyNavigableSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableNavigableSet");
        } else if (model.ref(SortedSet.class).isAssignableFrom($Raw)) {
            assertThat(unmodifiableSortedSet(emptySortedSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableSortedSet");
        } else if (model.ref(Set.class).isAssignableFrom($Raw)) {
            assertThat(unmodifiableSet(emptySet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableSet");
        } else if (model.ref(List.class).isAssignableFrom($Raw)) {
            assertThat(unmodifiableList(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableList");
        } else if (model.ref(Collection.class).isAssignableFrom($Raw)) {
            assertThat(unmodifiableCollection(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableCollection");
        } else {
            // TODO: Optional<T> instead of IllegalArgumentException
            throw new IllegalArgumentException("There is no unmodifiable-view-collection factory for type " + $type);
        }
        // TODO: Handle <a href="https://docs.oracle.com/javase/tutorial/jaxb/intro/custom.html">jxb:globalBindings collectionType="java.util.Vector</a> accordingly
    }

    /**
     * This method returns the clone expression for the given type and for the given actual {@linkplain JExpression expression} if such clone expression exists. In detail, this
     * means (in order):
     * <dl>
     * <dt>for any array</dt>
     * <dd>a shallow {@linkplain Object#clone() clone} but not a deep clone (multi-dimensional arrays are not cloned in deep, neither are the arrays's elements),</dd>
     * <dt>for any {@link Cloneable} type</dt>
     * <dd>a clone of this instance (either shallow or deep clone, depending on the specific internal {@link Object#clone()} implementation),</dd>
     * <dt>for any collection type</dt>
     * <dd>a {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#copyFactoryFor(JType) modifiable} or
     * {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#unmodifiableViewFactoryFor(JType) unmodifiable} collection copy (see parameter {@code unmodifiableCollections})</dd>
     * <dd>note, the copy most likely won't be a real clone as the collection type may differ and collection elements won't be cloned),</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * Note: The generated expression most likely cannot deal a {@code null} argument accordingly.
     *
     * @param $type
     *            the type to analyse
     * @param $expression
     *            the actual expression
     * @param unmodifiableCollections
     *            switch to turn on/off the collection clones as being unmodifiable
     * @return an {@link Optional} holding the clone expression for the actual expression if such clone expression exists; the {@linkplain Optional#empty() empty Optional}
     *         otherwise
     */
    public static final Optional<JExpression> cloneExpressionFor(final JType $type, final JExpression $expression, final boolean unmodifiableCollections) {
        if ($type.isArray()) {
            // TODO: Deep copy (instead of shallow copy) for multi-dimensional arrays; Or even further, cloning the array's elements in general (a.k.a. deep clone)
            return Optional.of(cast($type, $expression.invoke("clone")));
        } else if ($type.owner().ref(Cloneable.class).isAssignableFrom($type.boxify())) {
            // TODO: Get deep clone (instead of shallow copy) even if the origin type does not? (for example ArrayList#clone() only returns a shallow copy)
            return Optional.of(cast($type, $expression.invoke("clone")));
        } else if (isCollectionType($type)) {
            // TODO: Cloning the collection's elements (a.k.a. deep clone instead of shallow copy)
            return Optional.of(unmodifiableCollections ? unmodifiableViewFactoryFor($type).arg($expression) : copyFactoryFor($type).arg($expression));
// TODO } else if ($type instanceof JDefinedClass /* copy-constructor? */) {
//          final var $class = (JDefinedClass) $type;O
//          final var model = $type.owner();
//          final var $AnonymousUnary = model.anonymousClass(model.ref(UnaryOperator.class).narrow($class));
//          final var $apply = $AnonymousUnary.method(JMod.PUBLIC, $class, "apply");
//          $apply.annotate(Override.class);
//          $apply.javadoc().append("@implNote This implementation queries the actual class and tries to invoke the copy constructor. If no such constructor exists or cannot be invoked for any reason, the origin value is returned.");
//          final var $arg = $apply.param($type, "arg");
//          final var $reflectiveInvocation = $apply.body()._try();
//          $reflectiveInvocation.body()._return($arg.invoke("getClass").invoke("getConstructor").arg($arg.invoke("getClass")).invoke("newInstance").arg($arg));
//          final var $fallback = $reflectiveInvocation._catch(model.ref(Exception.class));
//          $fallback.param("any").mods().setFinal(true);
//          $fallback.body()._return($arg);
//          return Optional.of(JExpr._new($AnonymousUnary).invoke("apply").arg($expression));
// TODO } else if (copy-factory-method (in some util class)?) {
        }
        return Optional.empty();
    }

    /**
     * Inspects the default constructor of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @return the default constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final JDefinedClass $Class) {
        final var $types = new JType[0];
        return getConstructor($Class, $types);
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final JDefinedClass $Class, final Class<?>... argumentTypes) {
        final var $types = stream(argumentTypes).map($Class.owner()::ref).toArray(JType[]::new);
        return getConstructor($Class, $types);
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param $argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final JDefinedClass $Class, final JVar... $argumentTypes) {
        final var $types = stream($argumentTypes).map(JVar::type).toArray(JType[]::new);
        return getConstructor($Class, $types);
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param $argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     */
    public static final Optional<JMethod> getConstructor(final JDefinedClass $Class, final Collection<? extends JVar> $argumentTypes) {
        final var $types = $argumentTypes.stream().map(JVar::type).toArray(JType[]::new);
        return getConstructor($Class, $types);
    }

    /**
     * Inspects a specific value constructor of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param $argumentTypes
     *            the constructor's signature types
     * @return the specific value constructor if it exists
     * @see JDefinedClass#getConstructor(JType[])
     */
    public static final Optional<JMethod> getConstructor(final JDefinedClass $Class, final JType... $argumentTypes) {
        var $constructor = $Class.getConstructor($argumentTypes);
        if ($constructor == null) {
            $constructor = $Class.getConstructor(erasure($argumentTypes));
        }
        return Optional.ofNullable($constructor);
    }

    /**
     * Looks for all constructors that matches a specific predicate.
     *
     * @param $Class
     *            the class to analyse
     * @param filter
     *            the predicate to use when filtering the list of all constructors
     * @return a list of all constructors matching the given predicate
     */
    public static final List<JMethod> getConstructors(final JDefinedClass $Class, final Predicate<? super JMethod> filter) {
        return stream(spliteratorUnknownSize($Class.constructors(), DISTINCT | NONNULL), false).filter(filter).collect(toList());
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the method name
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final JDefinedClass $Class, final String name) {
        final var $types = new JType[0];
        return getMethod($Class, name, $types);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the method name
     * @param argumentTypes
     *            the method's signature types
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final JDefinedClass $Class, final String name, final Class<?>... argumentTypes) {
        final var $types = stream(argumentTypes).map($Class.owner()::ref).toArray(JType[]::new);
        return getMethod($Class, name, $types);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the method name
     * @param $argumentTypes
     *            the method's signature types
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final JDefinedClass $Class, final String name, final JVar... $argumentTypes) {
        final var $types = stream($argumentTypes).map(JVar::type).toArray(JType[]::new);
        return getMethod($Class, name, $types);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the method name
     * @param $argumentTypes
     *            the method's signature types
     * @return the specific method if it exists
     */
    public static final Optional<JMethod> getMethod(final JDefinedClass $Class, final String name, final Collection<? extends JVar> $argumentTypes) {
        final var $types = $argumentTypes.stream().map(JVar::type).toArray(JType[]::new);
        return getMethod($Class, name, $types);
    }

    /**
     * Inspects a specific method of the given class.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the method name
     * @param $argumentTypes
     *            the method's signature types
     * @return the specific method if it exists
     * @see JDefinedClass#getMethod(String, JType[])
     */
    public static final Optional<JMethod> getMethod(final JDefinedClass $Class, final String name, final JType... $argumentTypes) {
        var $method = $Class.getMethod(name, $argumentTypes);
        if ($method == null) {
            $method = $Class.getMethod(name, erasure($argumentTypes));
        }
        return Optional.ofNullable($method);
    }

    /**
     * Looks for all methods that matches a specific predicate.
     *
     * @param $Class
     *            the class to analyse
     * @param filter
     *            the predicate to use when filtering the list of all methods
     * @return a list of all methods matching the given predicate
     */
    public static final List<JMethod> getMethods(final JDefinedClass $Class, final Predicate<? super JMethod> filter) {
        return $Class.methods().stream().filter(filter).collect(toList());
    }

    /**
     * Looks for an embedded class and returns it.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the name of the embedded class to look for
     * @return an {@link Optional} holding the embedded class if found; an {@linkplain Optional#empty() empty Optional} if not found
     */
    public static final Optional<JDefinedClass> getEmbeddedClass(final JDefinedClass $Class, final String name) {
        final var embedded = stream(spliteratorUnknownSize($Class.classes(), DISTINCT | NONNULL), false).filter(c -> name.equals(c.name())).collect(toList());
        assertThat(embedded).hasSizeBetween(0, 1);
        return (embedded.isEmpty()) ? Optional.empty() : Optional.of(embedded.get(0)); 
    }

    /**
     * Checks whether or not the return type of a given {@linkplain JMethod method} is an {@linkplain #isOptionalType(JType) optional type}.
     * 
     * @param $method
     *            the method to analyse
     * @return {@code true} iff the give method's return type is assignable to {@link OptionalDouble}, {@link OptionalInt}, {@link OptionalLong}, or {@link Optional Optional&lt;T&gt;}
     */
    public static final boolean isOptionalMethod(final JMethod $method) {
        return isOptionalType($method.type());
    }

    /**
     * Checks whether or not the return type of a given {@linkplain JMethod method} is an {@linkplain #isCollectionType(JType) collection type}.
     * 
     * @param $method
     *            the method to analyse
     * @return {@code true} iff the give method's return type is assignable to {@link Collection}
     */
    public static final boolean isCollectionMethod(final JMethod $method) {
        return isCollectionType($method.type());
    }

    /**
     * As far as I can see a given {@link JMethod} does not tell about its enclosing class; Thus, this helper method reflectively inspects that enclosing class.
     * 
     * @param $method
     *            the method to analyse
     * @return the enclosing class
     */
    public static final JDefinedClass enclosingClass(final JMethod $method) {
        try {
            final var internalOuter = JMethod.class.getDeclaredField(OUTER_FIELD);
            internalOuter.setAccessible(true);
            return (JDefinedClass) internalOuter.get($method);
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

    /**
     * Reflectively inspects all exception classes currently thrown by the given {@linkplain JMethod method/constructor}.
     * 
     * @param $method
     *            the method/constructor to analyse
     * @return set of all exception types thrown by the given method/constructor
     */
    public static final Set<JClass> allThrows(final JMethod $method) {
        try {
            final var internalThrowsSet = JMethod.class.getDeclaredMethod(GETTHROWS_METHOD);
            internalThrowsSet.setAccessible(true);
            final var $throws = (Set<JClass>) internalThrowsSet.invoke($method);
            return unmodifiableSet($throws);
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

    /**
     * Reflectively identifies if a given {@linkplain JMethod method/constructor} throws a specific exception type.
     * 
     * @param $method
     *            the method/constructor to analyse
     * @param exception
     *            the exception type to look for 
     * @return {@code true} iff the method/constructor throws a specific exception type
     * @see #allThrows(JMethod)
     */
    public static final boolean doesThrow(final JMethod $method, final JClass exception) {
        return allThrows($method).contains(exception);
    }

    /**
     * Returns the (reflectively inspected) current {@link JDocComment} of the given {@link JMethod}. To be exact, it does not call the side-effect-containing
     * {@link JMethod#javadoc()}.
     * 
     * @param $method
     *            the method to analyse
     * @return the current {@link JDocComment} if existing
     */
    public static final Optional<JDocComment> currentJavadoc(final JMethod $method) {
        try {
            final var internalJavadoc = JMethod.class.getDeclaredField(JDOC_FIELD);
            internalJavadoc.setAccessible(true);
            final var $javadoc = (JDocComment) internalJavadoc.get($method);
            return Optional.ofNullable($javadoc);
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

    /**
     * Returns the (reflectively inspected) the current {@code @param} map of the given {@link JMethod}'s Javadoc. To be exact, it does not call the side-effect-containing
     * {@link JMethod#javadoc()}.
     * 
     * @param $method
     *            the method to analyse
     * @return the current {@code @param} map
     */
    public static final Map<String, JCommentPart> allJavadocParams(final JMethod $method) {
        final var javadoc = currentJavadoc($method);
        if (javadoc.isPresent()) {
            try {
                final var internalJavadocParams = JDocComment.class.getDeclaredField(ATPARAMS_FIELD);
                internalJavadocParams.setAccessible(true);
                final var $javadocParams = (Map<String,JCommentPart>) internalJavadocParams.get(javadoc.get());
                return unmodifiableMap($javadocParams);
            } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
                throw new RuntimeException(seriousProblem);
            }
        } else {
            return emptyMap(); 
        }
    }

    /**
     * Returns the (reflectively inspected) current {@code @throws} map of the given {@link JMethod}'s Javadoc. To be exact, it does not call the side-effect-containing
     * {@link JMethod#javadoc()}.
     * 
     * @param $method
     *            the method to analyse
     * @return the current {@code @throws} map
     */
    public static final Map<JClass,JCommentPart> allJavadocThrows(final JMethod $method) {
        final var javadoc = currentJavadoc($method);
        if (javadoc.isPresent()) {
            try {
                final var internalJavadocParams = JDocComment.class.getDeclaredField(ATTHROWS_FIELD);
                internalJavadocParams.setAccessible(true);
                final var $javadocParams = (Map<JClass,JCommentPart>) internalJavadocParams.get(javadoc.get());
                return unmodifiableMap($javadocParams);
            } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
                throw new RuntimeException(seriousProblem);
            }
        } else {
            return emptyMap(); 
        }
    }

    /**
     * @param $component
     *            the requested code component
     * @return the according Java code (generated by the component itself)
     * @see JGenerable#generate(JFormatter)
     */
    public static final String render(final JGenerable $component) {
        final var out = new StringWriter();
        $component.generate(new JFormatter(out));
        return out.toString();
    }

    /**
     * Returns the fully-qualified Javadoc name of the given type. Nested classes will be written as {@code de.package.OuterClass.NestedClass}, generic types (e.g.,
     * {@code java.util.Collection<String>} will be erased (e.g., {@code java.util.Collection}).
     *
     * @param $type
     *            the requested type
     * @return the fully-qualified Javadoc name of the given type
     */
    public static final String javadocNameOf(final JType $type) {
        return $type.erasure().fullName();
    }

    /**
     * Returns the simple Javadoc name of the given type. Nested classes will be written as {@code NestedClass}, generic types (e.g.,
     * {@code java.util.Collection<String>} will be erased (e.g., {@code Collection}).
     *
     * @param $type
     *            the requested type
     * @return the simple Javadoc name of the given type
     */
    public static final String javadocSimpleNameOf(final JType $type) {
        return $type.erasure().name();
    }

    /**
     * Returns the parameter-including Javadoc name of the given method. (Does not include the class-prefix of the enclosing class.)
     *
     * @param $method
     *            the requested method
     * @return the parameter-including Javadoc name of the given method
     */
    public static final String javadocNameOf(final JMethod $method) {
        return $method.name() + "(" + join(",", stream($method.listParamTypes()).map(CodeModelAnalysis::javadocNameOf).collect(toList())) + ")"; 
    }

    /**
     * Returns the Javadoc name of the given variable. (Does not include the class-prefix of the enclosing class.)
     *
     * @param $var
     *            the variable to consider
     * @return the Javadoc name of the given variable
     */
    public static final String javadocNameOf(final JVar $var) {
        return $var.name();
    }

}
