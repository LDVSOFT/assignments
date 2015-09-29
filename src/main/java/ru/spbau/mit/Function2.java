package ru.spbau.mit;

/**
 * Two-argument functions base class.
 * Accepts two arguments of types T1 and T2 (or theirs ancestors, respectively)
 */
public abstract class Function2<T1, T2, R> {
    /**
     * Invoke function.
     *
     * @param x first argument
     * @param y second argument
     * @return evaluation result
     */
    public abstract R apply(T1 x, T2 y);

    /**
     * Composes this function with another one.
     * Takes function g, and returns function g(f(x, y)).
     *
     * @param g   another function
     * @param <E> type of result of g
     * @return composed function
     */
    public <E> Function2<T1, T2, E> compose(final Function1<? super R, E> g) {
        return new Function2<T1, T2, E>() {
            @Override
            public E apply(T1 x, T2 y) {
                return g.apply(Function2.this.apply(x, y));
            }
        };
    }

    /**
     * First argument binding.
     * Return function with bound first argument.
     *
     * @param x first argument
     * @return bound function
     */
    public Function1<T2, R> bind1(final T1 x) {
        return new Function1<T2, R>() {
            @Override
            public R apply(T2 y) {
                return Function2.this.apply(x, y);
            }
        };
    }

    /**
     * Second argument binding.
     * Return function with bound second argument.
     *
     * @param y first argument
     * @return bound function
     */
    public Function1<T1, R> bind2(final T2 y) {
        return new Function1<T1, R>() {
            @Override
            public R apply(T1 x) {
                return Function2.this.apply(x, y);
            }
        };
    }

    /**
     * Currying
     * Return new function, equivalent to this, but that takes first argument,
     * and returns new function, that takes the second one.
     *
     * @return curried function
     */
    public Function1<T1, Function1<T2, R>> curry() {
        return new Function1<T1, Function1<T2, R>>() {
            @Override
            public Function1<T2, R> apply(final T1 x) {
                return Function2.this.bind1(x);
            }
        };
    }
}
