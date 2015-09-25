package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.*;

public class PredicateTest {
    /*package*/ static final Predicate<Integer> ID = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x != 0;
        }
    };
    /*package*/ static final Predicate<Pair<Integer, Integer>> FIRST = Predicate.fromFunction1(
            new Function2<Integer, Integer, Boolean>() {
                @Override
                public Boolean apply(Integer x, Integer y) {
                    return x != 0;
                }
            }.carry());
    /*package*/ static final Predicate<Pair<Integer, Integer>> SECOND = Predicate.fromFunction1(
            new Function2<Integer, Integer, Boolean>() {
                @Override
                public Boolean apply(Integer x, Integer y) {
                    return y != 0;
                }
            }.carry());
    /*package*/ static final Predicate<Integer> IS_ODD = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x % 2 == 1;
        }
    };
    /*package*/ static final Predicate<Object> IS_STRING = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return String.class.isInstance(x);
        }
    };


    @Test
    public void testOr() throws Exception {
        Predicate<Pair<Integer, Integer>> or1 = FIRST.or(SECOND);
        for (int i = 0; i != 2; i++)
            for (int j = 0; j != 2; j++) {
                assertEquals(i + j > 0, or1.apply(new SimplePair<>(i, j)));
            }

        Predicate<Integer> or2 = Predicate.ALWAYS_TRUE.or(new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                throw new IllegalArgumentException();
            }
        });
        assertTrue(or2.apply(3));
    }

    @Test
    public void testAnd() throws Exception {
        Predicate<Pair<Integer, Integer>> and1 = FIRST.and(SECOND);
        for (int i = 0; i != 2; i++)
            for (int j = 0; j != 2; j++) {
                assertEquals(i + j == 2, and1.apply(new SimplePair<>(i, j)));
            }

        Predicate<Integer> and2 = Predicate.ALWAYS_FALSE.and(new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                throw new IllegalArgumentException();
            }
        });
        assertFalse(and2.apply(3));
    }

    @Test
    public void testNot() throws Exception {
        Predicate<Integer> f = ID.not();
        assertTrue(f.apply(0));
        assertFalse(f.apply(1));
    }

    @Test
    public void testStationary() throws Exception {
        Object[] testing = {null, "abacaba", 2, 3.2, ID};
        for (Object object : testing) {
            assertTrue(Predicate.ALWAYS_TRUE.apply(object));
            assertFalse(Predicate.ALWAYS_FALSE.apply(object));
        }
    }
}