package de.informaticum.xjc.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sun.codemodel.JClass;

/**
 * In object oriented programming languages, the class hierarchy is a partially ordered set -- and this {@link Comparator} {@linkplain #sorted(Collection) sorts} any collection of
 * classes {@linkplain #compare(JClass, JClass) accordingly}.
 */
public enum JClassComparator
implements Comparator<JClass> {

    SINGLETON;

    /**
     * @implNote This implementation compares two {@linkplain JClass classes}. In detail, a class {@code x} is declared smaller than another class {@code y} if it has less super
     *           classes. The other way round, a class {@code x} is declared larger than another class {@code y} if it has more super classes. If both classes have the equal number
     *           of super classes, the compare result is deduced from the {@linkplain String#compareTo(String) comparison} of the {@linkplain JClass#fullName() class names},
     *           starting with the names of the root classes.
     */
    @Override
    public final int compare(final JClass left, final JClass right) {
        if (right == null) {
            return (left == null) ? 0 : +1;
        } else if (left == null) {
            return -1;
        } else {
            final var compare = this.compare(left._extends(), right._extends());
            return (compare == 0) ? left.fullName().compareTo(right.fullName()) : compare;
        }
    }

    /**
     * Creates and returns a sorted list of classes.
     *
     * @param unsorted
     *            the unsorted collection of classes to sort
     * @return a list of classes, with all elements in order
     * @see #compare(JClass, JClass)
     */
    public final static <T extends JClass> List<T> sorted(final Collection<T> unsorted) {
        final var sorted = new ArrayList<>(unsorted);
        Collections.sort(sorted, SINGLETON);
        return sorted;
    }

    /**
     * Creates and returns a sorted list of classes.
     *
     * @param unsorted
     *            the unsorted bunch of classes to sort
     * @return a list of classes, with all elements in order
     * @see #compare(JClass, JClass)
     */
    public final static <T extends JClass> List<T> sorted(final Iterable<T> unsorted) {
        return sorted(stream(unsorted.spliterator(), false).collect(toList()));
    }

}
