package ru.spbau.mit;

import org.junit.Test;
import java.util.*;

public class Level2Test extends TestsBase {

    @Test
    public void testAddAndRemoveFrom0To5() {
        Set<Integer> set = buildSet();

        for (int i = 0; i < 5; i++) {
            assertFalse(set.remove(i));
            assertTrue(set.add(i));
            assertTrue(set.contains(i));
            assertTrue(set.remove(i));
            assertTrue(set.add(i));

            assertEquals(i + 1, set.size());
        }
    }

    @Test
    public void testAddAndRemoveFrom4DownTo0() {
        Set<Integer> set = buildSet();

        for (int i = 4; i >= 0; i--) {
            assertFalse(set.remove(i));
            assertTrue(set.add(i));
            assertTrue(set.contains(i));
            assertTrue(set.remove(i));
            assertTrue(set.add(i));

            assertEquals(5 - i, set.size());
        }
    }

    @Test
    public void testAddAndRemoveFrom0To5WithOpposites() {
        Set<Integer> set = buildSet();

        for (int i = 1; i <= 5; i++) {
            assertFalse(set.remove(i));
            assertTrue(set.add(i));
            assertTrue(set.contains(i));
            assertTrue(set.remove(i));
            assertTrue(set.add(i));
            assertEquals(2 * i - 1, set.size());

            int j = -i;
            assertFalse(set.remove(j));
            assertTrue(set.add(j));
            assertTrue(set.contains(j));
            assertTrue(set.remove(j));
            assertTrue(set.add(j));

            assertEquals(2 * i, set.size());
        }
    }

    @Test
    public void testAddAndRemoveRandom() {
        Set<Integer> set = buildSet();
        Set<Integer> real = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            int e = (i > 0 && random.nextBoolean())
                ? new ArrayList<>(real).get(random.nextInt(real.size()))
                : random.nextInt(123456);
            assertEquals(real.add(e), set.add(e));
            assertTrue(set.contains(e));
            assertTrue(set.remove(e));
            assertTrue(set.add(e));
            assertEquals(real.size(), set.size());
        }
    }

    @Test(expected=IllegalStateException.class)
    public void testRemoveFromIteratorWithoutCallingNextFirst() {
        Set<Integer> testSet = buildSet();
        assertTrue(testSet.isEmpty());
        Iterator<Integer> testIterator = testSet.iterator();
        testIterator.remove();
    }

    @Test
    public void testRandomRemoveFromIterator() {
        Set<Integer> testSet = buildSet();
        Set<Integer> realSet = new TreeSet<>();

        for (int i = 0; i < 100; i++) {
            int element = random.nextInt(123456);
            assertEquals(realSet.add(element), testSet.add(element));
        }

        Iterator<Integer> testIterator = testSet.iterator();
        Iterator<Integer> realIterator = realSet.iterator();
        while (testIterator.hasNext() && realIterator.hasNext()) {
            testIterator.next();
            realIterator.next();
            boolean shouldRemove = random.nextBoolean();
            if (shouldRemove) {
                testIterator.remove();
                realIterator.remove();
            }
        }
        assertEquals(testIterator.hasNext(), realIterator.hasNext());
        assertEquals(new ArrayList<>(realSet), new ArrayList<>(testSet));
    }
}
