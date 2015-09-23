package ru.spbau.mit;

/**
 * Predicates base class.
 * Predicate is boolean function: it takes argument and returns boolean.
 */
public abstract class Predicate<T> extends Function1<T, Boolean> {
    /**
     * Always true predicate.
     */
    public static final Predicate<Object> ALWAYS_TRUE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return Boolean.TRUE;
        }
    };

    /**
     * Always false predicate.
     */
    public static final Predicate<Object> ALWAYS_FALSE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return Boolean.FALSE;
        }
    };

    /**
     * Or takes this predicate, other one, and returns new one, that is equivalent to "this || g".
     * It uses lazy evaluation: if this argument returns true, the other one won't be evaluated.
     * @param g second predicate
     * @return new Or predicate
     */
    //TODO Maybe the is more generic way for writing or/and with Lowest Common Ancestor
    public Predicate<T> or(final Predicate<? super T> g) {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                if (Predicate.this.apply(x))
                    return Boolean.TRUE;
                return g.apply(x);
            }
        };
    }

    /**
     * And takes this predicate, other one, and returns new one, that is equivalent to "this && g".
     * It uses lazy evaluation: if this argument returns false, the other one won't be evaluated.
     * @param g second predicate
     * @return new And predicate
     */
    public Predicate<T> and(final Predicate<? super T> g) {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                if (!Predicate.this.apply(x))
                    return Boolean.FALSE;
                return g.apply(x);
            }
        };
    }

    /**
     * Not takes this predicate, and returns new one, that is equivalent to "!this".
     * @return new Not predicate
     */
    public Predicate<T> not() {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                return !Predicate.this.apply(x);
            }
        };
    }


}
