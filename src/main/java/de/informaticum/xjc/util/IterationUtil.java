package de.informaticum.xjc.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to iteration types.
 */
public enum IterationUtil {
    ;

    /**
     * @param <T>
     *            the type of elements contained within the given {@link Iterable} instance
     * @param iterable
     *            the given {@link Iterable} instance
     * @return the according {@link Stream} instance
     */
    public static final <T> Stream<T> streamOf(final Iterable<T> iterable) {
        return stream(iterable.spliterator(), false);
    }

    /**
     * @param <T>
     *            the type of elements contained within the given {@link Iterable} instance
     * @param iterable
     *            the given {@link Iterable} instance
     * @return the according {@link List} instance
     */
    public static final <T> List<T> listOf(final Iterable<T> iterable) {
        return streamOf(iterable).collect(toList());
    }

    /**
     * @param <T>
     *            the type of elements contained within the given {@link Iterator} instance
     * @param iterator
     *            the given {@link Iterator} instance
     * @return the according {@link Iterable} instance
     */
    public static final <T> Iterable<T> iterableOf(final Iterator<T> iterator) {
        return () -> iterator;
    }

    /**
     * @param <T>
     *            the type of elements contained within the given {@link Iterator} instance
     * @param iterator
     *            the given {@link Iterator} instance
     * @return the according {@link Spliterator} instance
     */
    public static final <T> Spliterator<T> spliteratorOf(final Iterator<T> iterator) {
        return iterableOf(iterator).spliterator();
    }

    /**
     * @param <T>
     *            the type of elements contained within the given {@link Iterator} instance
     * @param iterator
     *            the given {@link Iterator} instance
     * @return the according {@link Stream} instance
     */
    public static final <T> Stream<T> streamOf(final Iterator<T> iterator) {
        return streamOf(iterableOf(iterator));
    }

    /**
     * @param <T>
     *            the type of elements contained within the given {@link Iterator} instance
     * @param iterator
     *            the given {@link Iterator} instance
     * @return the according {@link List} instance
     */
    public static final <T> List<T> listOf(final Iterator<T> iterator) {
        return listOf(iterableOf(iterator));
    }

}
