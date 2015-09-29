package ru.spbau.mit;

import org.junit.Test;

import static java.lang.Math.max;
import static org.junit.Assert.assertEquals;

public class Function2Test {
    /*package*/ static final Function2<Integer, Integer, Integer> MAX = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return max(x, y);
        }
    };
    /*package*/ static final Function2<Integer, Integer, Integer> F1 = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return x + 2 * y;
        }
    };

    @Test
    public void testCompose() throws Exception {
        Function2<Integer, Integer, String> maxToString = MAX.compose(Function1Test.STRINGIFY);
        for (int i = -5; i != 6; i++)
            for (int j = -5; j != 6; j++)
                assertEquals(Integer.toString(max(i, j)), maxToString.apply(i, j));
    }

    @Test
    public void testBind1() throws Exception {
        final int x = 2;
        Function1<Integer, Integer> f1Bound1 = F1.bind1(x);
        for (int i = -5; i != 6; i++)
            assertEquals(F1.apply(x, i), f1Bound1.apply(i));
    }

    @Test
    public void testBind2() throws Exception {
        final int y = 3;
        Function1<Integer, Integer> f1Bound2 = F1.bind2(y);
        for (int i = -5; i != 6; i++)
            assertEquals(F1.apply(i, y), f1Bound2.apply(i));
    }

    @Test
    public void testCurry() throws Exception {
        Function1<Integer, Function1<Integer, Integer>> f1Carried = F1.curry();
        for (int i = -5; i != 6; i++)
            for (int j = -5; j != 6; j++) {
                assertEquals(F1.apply(i, j), f1Carried.apply(i).apply(j));
            }
    }
}
