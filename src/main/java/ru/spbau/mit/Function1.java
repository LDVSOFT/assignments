package ru.spbau.mit;

/**
 * One-argument functions base class.
 * Accepts one argument of type T (or it's ancestor)
 */
public abstract class Function1<T, R> {
    /**
     * Invoke function
     * @param x argument
     * @return result
     */
    public abstract R apply(T x);

    public <E> Function1<T, E> compose(final Function1<? super R, E> g) {
        return new Function1<T, E>() {
            @Override
            public E apply(T x) {
                return g.apply(Function1.this.apply(x));
            }
        };
    }
}
