package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testOr() throws Exception {
        Predicate<Pair<Integer, Integer>> or = FIRST.or(SECOND);
        for (int i = 0; i != 2; i++)
            for (int j = 0; j != 2; j++) {
                assertEquals(i + j > 0, or.apply(new SimplePair<>(i, j)));
            }
    }

    @Test
    public void testAnd() throws Exception {
        Predicate<Pair<Integer, Integer>> and = FIRST.and(SECOND);
        for (int i = 0; i != 2; i++)
            for (int j = 0; j != 2; j++) {
                assertEquals(i + j == 2, and.apply(new SimplePair<>(i, j)));
            }
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
        for (Object object: testing) {
            assertTrue(Predicate.ALWAYS_TRUE.apply(object));
            assertFalse(Predicate.ALWAYS_FALSE.apply(object));
        }
    }
}