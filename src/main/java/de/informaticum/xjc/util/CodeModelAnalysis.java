package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._super;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyNavigableSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableSet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JGenerable;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JMods;
import com.sun.codemodel.JType;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to {@code com.sun.codemodel.*} types.
 */
public enum CodeModelAnalysis {
    ;

    private static final Class<?>[] DIAMOND = {};

    private static final String UNEXPECTED_MODIFICATION = "WTF! The long-time existing constructor/factory-method has been modified ;-(";

    public static final JExpression $super = _super();

    public static final JExpression $this = _this();

    public static final JExpression $null = _null();

    public static boolean isFinal(final JMods mods) {
        return (mods.getValue() & JMod.FINAL) != 0;
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
        // TODO } else if (copy-constructor?) {
        // TODO } else if (copy-factory-method (in some util class)?) {
        }
        return Optional.empty();
    }

    /**
     * Looks for a constructor that has the specified method signature and returns it.
     *
     * @param $Class
     *            the class to analyse
     * @param $argumentTypes
     *            the list of the constructor's argument types
     * @return an {@link Optional} holding the constructor if found; an {@linkplain Optional#empty() empty Optional} if not found
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
     * Looks for a method that has the specified name/method signature and returns it.
     *
     * @param $Class
     *            the class to analyse
     * @param name
     *            the method name to look for
     * @param $argumentTypes
     *            the list of the method's argument types
     * @return an {@link Optional} holding the method if found; an {@linkplain Optional#empty() empty Optional} if not found
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

}
