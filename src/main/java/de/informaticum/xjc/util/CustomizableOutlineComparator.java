package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.IterationUtil.listOf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sun.tools.xjc.outline.CustomizableOutline;

/**
 * In object oriented programming languages, the class hierarchy is a partially ordered set -- and this {@link Comparator} {@linkplain #sorted(Collection) sorts} any collection of
 * classes {@linkplain #compare(CustomizableOutline, CustomizableOutline) accordingly}.
 *
 * @see #INSTANCE
 */
public enum CustomizableOutlineComparator
implements Comparator<CustomizableOutline> {

    /**
     * Singleton instance of {@link PackageOutlineComparator}.
     */
    INSTANCE;

    /**
     * @implNote The comparison is quite simply delegated to {@link de.informaticum.xjc.util.JClassComparator#compare(com.sun.codemodel.JClass, com.sun.codemodel.JClass)}.
     */
    @Override
    public final int compare(final CustomizableOutline left, final CustomizableOutline right) {
        if (right == null) {
            return (left == null) ? 0 : +1;
        } else if (left == null) {
            return -1;
        } else {
            return JClassComparator.INSTANCE.compare(left.getImplClass(), right.getImplClass());
        }
    }

    /**
     * Creates and returns a sorted list of classes.
     *
     * @param unsorted
     *            the unsorted collection of classes to sort
     * @param <T>
     *            the specific subtype of {@link CustomizableOutline}
     * @return a list of classes, with all elements in order
     * @see #compare(CustomizableOutline, CustomizableOutline)
     */
    public final static <T extends CustomizableOutline> List<T> sorted(final Collection<T> unsorted) {
        final var sorted = new ArrayList<>(unsorted);
        Collections.sort(sorted, CustomizableOutlineComparator.INSTANCE);
        return sorted;
    }

    /**
     * Creates and returns a sorted list of classes.
     *
     * @param unsorted
     *            the unsorted bunch of classes to sort
     * @param <T>
     *            the specific subtype of {@link CustomizableOutline}
     * @return a list of classes, with all elements in order
     * @see #compare(CustomizableOutline, CustomizableOutline)
     */
    public final static <T extends CustomizableOutline> List<T> sorted(final Iterable<T> unsorted) {
        return sorted(listOf(unsorted));
    }

}
