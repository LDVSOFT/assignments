package ru.spbau.mit;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionsTest {
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

    private interface Comparator<T, R> {
        boolean compare(T x, R y);
    }

    private static <T, R> void assertContainersEquals(Iterator<T> it1, Iterator<R> it2, Comparator<? super T, ? super R> test) {
        while (it1.hasNext()) {
            assertTrue(it2.hasNext());
            assertTrue(test.compare(it1.next(), it2.next()));
        }
        assertFalse(it2.hasNext());
    }

    @Test
    public void testMap() throws Exception {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        Iterable<String> strs = Collections.map(Function1Test.STRINGIFY, ints);
        assertContainersEquals(ints.iterator(), strs.iterator(), new Comparator<Integer, String>() {
            @Override
            public boolean compare(Integer x, String y) {
                return y.equals(x.toString());
            }
        });
    }

    @Test
    public void testFilter() throws Exception {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        Iterable<Integer> odd1 = Arrays.asList(1, 3, 5, 7);
        Iterable<Integer> odd2 = Collections.filter(IS_ODD, ints);
        assertContainersEquals(odd1.iterator(), odd2.iterator(), new Comparator<Integer, Integer>() {
            @Override
            public boolean compare(Integer x, Integer y) {
                return x.equals(y);
            }
        });
    }

    @Test
    public void testTakeWhile() throws Exception {
        Iterable<Object> objs = Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр", 2, 12, "123", "21");
        Iterable<Object> str1 = Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр");
        Iterable<Object> str2 = Collections.takeWhile(IS_STRING, objs);
        assertContainersEquals(str1.iterator(), str2.iterator(), new Comparator<Object, Object>() {
            @Override
            public boolean compare(Object x, Object y) {
                return x.equals(y);
            }
        });
    }

    @Test
    public void testTakeUnless() throws Exception {
        Iterable<Object> objs = Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр", 2, 12, "123", "21");
        Iterable<Object> str1 = Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр");
        Iterable<Object> str2 = Collections.takeUnless(IS_STRING.not(), objs);
        assertContainersEquals(str1.iterator(), str2.iterator(), new Comparator<Object, Object>() {
            @Override
            public boolean compare(Object x, Object y) {
                return ((String) x).equals((String) y);
            }
        });
    }

    @Test
    public void testFoldl() throws Exception {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(Collections.foldl(
                        new Function2<String, Integer, String>() {
                            @Override
                            public String apply(String x, Integer y) {
                                return x + y;
                            }
                        },
                        "trololo:", ints),
                "trololo:12345678");
    }

    @Test
    public void testFoldr() throws Exception {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(Collections.foldr(
                        new Function2<Integer, String, String>() {
                            @Override
                            public String apply(Integer x, String y) {
                                return y + x;
                            }
                        },
                        "trololo:", ints),
                "trololo:87654321");
    }
}