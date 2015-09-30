package ru.spbau.mit;

import org.junit.Test;
import java.util.*;

public class Level1Test extends TestsBase {

    @Test
    public void testAddFrom0To5() {
        Set<Integer> set = buildSet();

        for (int i = 0; i < 5; i++) {
            assertTrue(set.add(i));
            assertTrue(set.contains(i));
            assertFalse(set.add(i));

            assertEquals(i + 1, set.size());
        }
    }

    @Test
    public void testAddFrom4DownTo0() {
        Set<Integer> set = buildSet();

        for (int i = 4; i >= 0; i--) {
            assertTrue(set.add(i));
            assertTrue(set.contains(i));
            assertFalse(set.add(i));

            assertEquals(5 - i, set.size());
        }
    }

    @Test
    public void testAddFrom0To5WithOpposites() {
        Set<Integer> set = buildSet();

        for (int i = 1; i <= 5; i++) {
            assertTrue(set.add(i));
            assertTrue(set.contains(i));
            assertFalse(set.add(i));
            assertEquals(2 * i - 1, set.size());

            assertTrue(set.add(-i));
            assertTrue(set.contains(-i));
            assertFalse(set.add(-i));
            assertEquals(2 * i, set.size());
        }
    }

    @Test
    public void testAddRandom() {
        Set<Integer> set = buildSet();
        Set<Integer> real = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            int e = (i > 0 && random.nextBoolean())
                ? new ArrayList<>(real).get(random.nextInt(real.size()))
                : random.nextInt(123456);
            assertEquals(real.add(e), set.add(e));
            assertTrue(set.contains(e));
            assertFalse(set.add(e));
            assertEquals(real.size(), set.size());
        }
    }

    @Test(expected=NoSuchElementException.class)
    public void testIteratorOfEmptySet() {
        Set<Integer> testSet = buildSet();
        assertTrue(testSet.isEmpty());
        Iterator<Integer> testIterator = testSet.iterator();
        assertFalse(testIterator.hasNext());
        testIterator.next();
    }

    @Test
    public void testIterator() {
        Set<Integer> set = buildSet();
        Set<Integer> real = new TreeSet<>();

        for (int i = 0; i < 100; i++) {
            int e = random.nextInt();
            assertEquals(real.add(e), set.add(e));
            assertEquals(new ArrayList<>(real), new ArrayList<>(set));
        }
    }
}
