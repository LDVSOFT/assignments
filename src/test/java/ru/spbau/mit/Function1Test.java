package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Function1Test {
    /*package*/ static final Function1<Integer, Integer> SQR = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return x * x;
        }
    };
    /*package*/ static final Function1<Integer, Integer> ID = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return x;
        }
    };
    /*package*/ static final Function1<Object, String> STRINGIFY = new Function1<Object, String>() {
        @Override
        public String apply(Object x) {
            if (x == null)
                return "null";
            return x.toString();
        }
    };

    @Test
    public void testCompose() throws Exception {
        Function1<Integer, Integer> sqr1 = SQR.compose(ID);
        Function1<Integer, Integer> sqr2 = ID.compose(SQR);
        for (int i = -5; i != 6; i++) {
            assertEquals(SQR.apply(i), sqr1.apply(i));
            assertEquals(SQR.apply(i), sqr2.apply(i));
        }

        assertEquals("144", SQR.compose(STRINGIFY).apply(12));
    }
}