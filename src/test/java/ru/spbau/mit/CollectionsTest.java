package ru.spbau.mit;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CollectionsTest {
    @Test
    public void testMap() throws Exception {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        ArrayList<String> strs1 = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7"));
        ArrayList<String> strs2 = (ArrayList<String>) Collections.map(Function1Test.STRINGIFY, ints);
        assertEquals(strs1, strs2);
    }

    @Test
    public void testFilter() throws Exception {
        Iterable<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        ArrayList<Integer> odd1 = new ArrayList<>(Arrays.asList(1, 3, 5, 7));
        ArrayList<Integer> odd2 = (ArrayList<Integer>) Collections.filter(PredicateTest.IS_ODD, ints);
        assertEquals(odd1, odd2);
    }

    @Test
    public void testTakeWhile() throws Exception {
        Iterable<Object> objs = Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр", null, 2, 12, "123", "21");
        ArrayList<Object> str1 = new ArrayList<>(Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр"));
        ArrayList<Object> str2 = (ArrayList<Object>) Collections.takeWhile(PredicateTest.IS_NOT_NULL, objs);
        assertEquals(str1, str2);
    }

    @Test
    public void testTakeUnless() throws Exception {
        Iterable<Object> objs = Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр", null, 2, 12, "123", "21");
        ArrayList<Object> str1 = new ArrayList<>(Arrays.<Object>asList("Abc", "", "/dev/null", "Пётр"));
        ArrayList<Object> str2 = (ArrayList<Object>) Collections.takeUnless(PredicateTest.IS_NOT_NULL.not(), objs);
        assertEquals(str1, str2);
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
