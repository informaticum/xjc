package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr._new;
import static de.informaticum.xjc.plugin.TargetCode.DIAMOND;
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
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

public enum CollectionAnalysis {
    ;

    private static final String UNEXPECTED_MODIFICATION = "WTF! The long-time existing factory method has been modified ;-(";

    public static final boolean isCollectionMethod(final JMethod $method) {
        final var model = $method.type().owner();
        final var raw = $method.type().erasure().boxify();
        return model.ref(Collection.class).isAssignableFrom(raw);
    }

    public static final JInvocation accordingEmptyFactoryFor(final JType $type) {
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
            throw new IllegalArgumentException("There is no empty-collection factory for type " + $type);
        }
    }

    public static final JInvocation accordingDefaultFactoryFor(final JType $type) {
        try {
            // default constructors and copy constructors are named similarly ;-)
            return accordingCopyFactoryFor($type);
        } catch (final IllegalArgumentException illegal) {
            throw new IllegalArgumentException("There is no default-collection factory for type " + $type);
        }
    }

    public static final JInvocation accordingCopyFactoryFor(final JType $type) {
        final var model = $type.owner();
        final var rawType = $type.boxify().erasure();
        if (model.ref(NavigableSet.class).isAssignableFrom(rawType)) {
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(SortedSet.class).isAssignableFrom(rawType)) {
            return _new(model.ref(TreeSet.class).narrow(DIAMOND));
        } else if (model.ref(Set.class).isAssignableFrom(rawType)) {
            return _new(model.ref(HashSet.class).narrow(DIAMOND));
        } else if (model.ref(List.class).isAssignableFrom(rawType)) {
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else if (model.ref(Collection.class).isAssignableFrom(rawType)) {
            return _new(model.ref(ArrayList.class).narrow(DIAMOND));
        } else {
            throw new IllegalArgumentException("There is no copy-collection factory for type " + $type);
        }
    }

    public static final JInvocation accordingImmutableFactoryFor(final JType $type) {
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
            throw new IllegalArgumentException("There is no unmodifiable-collection factory for type " + $type);
        }
    }

}
