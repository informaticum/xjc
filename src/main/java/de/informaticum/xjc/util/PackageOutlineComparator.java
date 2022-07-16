package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.IterationUtil.listOf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sun.tools.xjc.outline.PackageOutline;

/**
 * Simple {@link Comparator} to directly {@linkplain #sorted(Collection) sort} any collection of packages in {@linkplain #compare(PackageOutline, PackageOutline) its natural
 * order}.
 *
 * @see #INSTANCE
 */
public enum PackageOutlineComparator
implements Comparator<PackageOutline> {

    /**
     * Singleton instance of {@link PackageOutlineComparator}.
     */
    INSTANCE;

    /**
     * @implNote The comparison is quite simply delegated to {@link com.sun.codemodel.JPackage#compareTo(com.sun.codemodel.JPackage)}.
     */
    @Override
    public final int compare(final PackageOutline left, final PackageOutline right) {
        if (right == null) {
            return (left == null) ? 0 : +1;
        } else if (left == null) {
            return -1;
        } else {
            return left._package().compareTo(right._package());
        }
    }

    /**
     * Creates and returns a sorted list of packages.
     *
     * @param unsorted
     *            the unsorted collection of packages to sort
     * @param <T>
     *            the specific subtype of {@link PackageOutline}
     * @return a list of packages, with all elements in order
     * @see #compare(PackageOutline, PackageOutline)
     */
    public final static <T extends PackageOutline> List<T> sorted(final Collection<T> unsorted) {
        final var sorted = new ArrayList<>(unsorted);
        Collections.sort(sorted, PackageOutlineComparator.INSTANCE);
        return sorted;
    }

    /**
     * Creates and returns a sorted list of packages.
     *
     * @param unsorted
     *            the unsorted bunch of packages to sort
     * @param <T>
     *            the specific subtype of {@link PackageOutline}
     * @return a list of packages, with all elements in order
     * @see #compare(PackageOutline, PackageOutline)
     */
    public final static <T extends PackageOutline> List<T> sorted(final Iterable<T> unsorted) {
        return sorted(listOf(unsorted));
    }

}
