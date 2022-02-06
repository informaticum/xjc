package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr._new;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to {@link Collection} types.
 */
public enum CollectionAnalysis {
    ;

    /**
     * This is the specific array of {@link Class} to use for creating the diamond operator expression when calling {@link JClass#narrow(Class...)}.
     */
    public static final Class<?>[] DIAMOND = {};

    private static final String UNEXPECTED_MODIFICATION = "WTF! The long-time existing constructor/factory-method has been modified ;-(";

    /**
     * @param $method
     *            the method to analyse
     * @return {@code true} iff the give method's return type is assignable to {@link Collection}
     */
    public static final boolean isCollectionMethod(final JMethod $method) {
        final var model = $method.type().owner();
        final var raw = $method.type().erasure().boxify();
        return model.ref(Collection.class).isAssignableFrom(raw);
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
        final var model = $type.owner();
        final var $Collections = model.ref(Collections.class);
        final var rawType = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom(rawType)) {
            assertThat(emptyNavigableSet()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptyNavigableSet");
        } else if (model.ref(SortedSet.class).isAssignableFrom(rawType)) {
            assertThat(emptySortedSet()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptySortedSet");
        } else if (model.ref(Set.class).isAssignableFrom(rawType)) {
            assertThat(emptySet()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptySet");
        } else if (model.ref(List.class).isAssignableFrom(rawType)) {
            assertThat(emptyList()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptyList");
        } else if (model.ref(Collection.class).isAssignableFrom(rawType)) {
            assertThat(emptyList()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("emptyList");
        } else {
            throw new IllegalArgumentException("There is no empty-collection instance of type " + $type);
        }
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
        final var rawType = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom(rawType)) {
            assertThat(new TreeSet<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(SortedSet.class).isAssignableFrom(rawType)) {
            assertThat(new TreeSet<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(Set.class).isAssignableFrom(rawType)) {
            assertThat(new HashSet<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(HashSet.class).narrow(DIAMOND));
        } else if (model.ref(List.class).isAssignableFrom(rawType)) {
            assertThat(new ArrayList<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else if (model.ref(Collection.class).isAssignableFrom(rawType)) {
            assertThat(new ArrayList<>()).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else {
            throw new IllegalArgumentException("There is no default-collection instance of type " + $type);
        }
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
        final var rawType = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom(rawType)) {
            assertThat(new TreeSet<>(emptySortedSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(SortedSet.class).isAssignableFrom(rawType)) {
            assertThat(new TreeSet<>(emptySortedSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(Set.class).isAssignableFrom(rawType)) {
            assertThat(new HashSet<>(emptySet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(HashSet.class).narrow(DIAMOND));
        } else if (model.ref(List.class).isAssignableFrom(rawType)) {
            assertThat(new ArrayList<>(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else if (model.ref(Collection.class).isAssignableFrom(rawType)) {
            assertThat(new ArrayList<>(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else {
            throw new IllegalArgumentException("There is no copy-collection factory for type " + $type);
        }
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
        final var rawType = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom(rawType)) {
            assertThat(unmodifiableNavigableSet(emptyNavigableSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableNavigableSet");
        } else if (model.ref(SortedSet.class).isAssignableFrom(rawType)) {
            assertThat(unmodifiableSortedSet(emptySortedSet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableSortedSet");
        } else if (model.ref(Set.class).isAssignableFrom(rawType)) {
            assertThat(unmodifiableSet(emptySet())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableSet");
        } else if (model.ref(List.class).isAssignableFrom(rawType)) {
            assertThat(unmodifiableList(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableList");
        } else if (model.ref(Collection.class).isAssignableFrom(rawType)) {
            assertThat(unmodifiableCollection(emptyList())).withFailMessage(UNEXPECTED_MODIFICATION).isEmpty();
            return $Collections.staticInvoke("unmodifiableCollection");
        } else {
            throw new IllegalArgumentException("There is no unmodifiable-view-collection factory for type " + $type);
        }
        // TODO: Handle <a href="https://docs.oracle.com/javase/tutorial/jaxb/intro/custom.html">jxb:globalBindings collectionType="java.util.Vector</a> accordingly
    }

}
