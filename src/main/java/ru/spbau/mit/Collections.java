package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Collections utils class in functional style
 */
public abstract class Collections {
    /**
     * Map, i. e. apply function to every element of collection.
     *
     * @param f   function to be applied
     * @param src source collection
     * @param <T> type of element in source collection
     * @param <R> type of element in result collection
     * @return result collection
     */
    public static <T, R> Iterable<R> map(final Function1<? super T, R> f, final Iterable<T> src) {
        List<R> result = new ArrayList<>();
        for (T element : src) {
            result.add(f.apply(element));
        }
        return result;
    }

    /**
     * Filter, i. e. return subcollection of elements, that have `f(x)' true.
     *
     * @param f   filter function
     * @param src source collection
     * @param <T> type of element in source collection
     * @return result collection
     */
    public static <T> Iterable<T> filter(final Predicate<? super T> f, final Iterable<T> src) {
        List<T> result = new ArrayList<>();
        for (T element : src) {
            if (f.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Take maximum prefix of collection, in which every element has `f(x)' true.
     *
     * @param f   filter function
     * @param src source collection
     * @param <T> type of element in source collection
     * @return result collection
     */
    public static <T> Iterable<T> takeWhile(final Predicate<? super T> f, final Iterable<T> src) {
        List<T> result = new ArrayList<>();
        for (T element : src) {
            if (!f.apply(element)) {
                break;
            }
            result.add(element);
        }
        return result;
    }

    /**
     * Take maximum prefix of collection, in which every element has `f(x)' false.
     *
     * @param f   filter function
     * @param src source collection
     * @param <T> type of element in source collection
     * @return result collection
     */
    public static <T> Iterable<T> takeUnless(final Predicate<? super T> f, final Iterable<T> src) {
        return takeWhile(f.not(), src);
    }

    /**
     * Fold from left, i. e. take x, and return f(f(f(x, e_1,), e_2), e_3)
     *
     * @param f   function to be applied. It should take types R and T and return R
     * @param x   initial element
     * @param src source collection
     * @param <T> type of element in collection
     * @param <R> type of fold result
     * @return result of folding
     */
    public static <T, R> R foldl(final Function2<? super R, ? super T, ? extends R> f, R x, final Iterable<T> src) {
        for (T element : src) {
            x = f.apply(x, element);
        }
        return x;
    }

    /**
     * Fold from right implementation.
     */
    private static <T, R> R foldr(final Function2<? super T, ? super R, ? extends R> f, R x, final Iterator<T> src) {
        if (!src.hasNext()) {
            return x;
        }
        T element = src.next();
        return f.apply(element, foldr(f, x, src));
    }

    /**
     * Fold from right, i. e. take x, and f(e_1, f(e_2, f(e_3, x)))
     *
     * @param f   function to be applied. It should take types R and T and return R
     * @param x   initial element
     * @param src source collection
     * @param <T> type of element in collection
     * @param <R> type of fold result
     * @return result of folding
     */
    public static <T, R> R foldr(final Function2<? super T, ? super R, ? extends R> f, R x, final Iterable<T> src) {
        return foldr(f, x, src.iterator());
    }
}
