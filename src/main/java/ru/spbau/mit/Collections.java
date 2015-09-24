package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ldvsoft on 24.09.15.
 */
public abstract class Collections {
    public static <T, R> Iterable<R> map(final Function1<? super T, ? extends R> f, final Iterable<T> src) {
        List<R> result = new ArrayList<>();
        for (T element : src) {
            result.add(f.apply(element));
        }
        return result;
    }

    public static <T> Iterable<T> filter(final Predicate<? super T> f, final Iterable<T> src) {
        List<T> result = new ArrayList<>();
        for (T element : src) {
            if (f.apply(element))
                result.add(element);
        }
        return result;
    }

    public static <T> Iterable<T> takeWhile(final Predicate<? super T> f, final Iterable<T> src) {
        List<T> result = new ArrayList<>();
        for (T element : src) {
            if (!f.apply(element))
                break;
            result.add(element);
        }
        return result;
    }

    public static <T> Iterable<T> takeUnless(final Predicate<? super T> f, final Iterable<T> src) {
        List<T> result = new ArrayList<>();
        for (T element : src) {
            if (f.apply(element))
                break;
            result.add(element);
        }
        return result;
    }

    public static <T, R> R foldl(final Function2<? super R, ? super T, ? extends R> f, R x, final Iterable<T> src) {
        for (T element : src) {
            x = f.apply(x, element);
        }
        return x;
    }

    private static <T, R> R foldr(final Function2<? super T, ? super R, ? extends R> f, R x, final Iterator<T> src) {
        if (!src.hasNext())
            return x;
        T element = src.next();
        return f.apply(element, foldr(f, x, src));
    }

    public static <T, R> R foldr(final Function2<? super T, ? super R, ? extends R> f, R x, final Iterable<T> src) {
        return foldr(f, x, src.iterator());
    }
}
