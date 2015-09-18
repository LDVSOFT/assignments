package ru.spbau.mit;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class SerializableStringSetTest {

    @Test
    public void testSimple() {
        StringSet stringSet = instance();

        assertTrue(stringSet.add("abc"));
        assertTrue(stringSet.contains("abc"));
        assertEquals(1, stringSet.size());
        assertEquals(1, stringSet.howManyStartsWithPrefix("abc"));
    }

    @Test
    public void testSimpleSerialization() {
        StringSet stringSet = instance();

        assertTrue(stringSet.add("abc"));
        assertTrue(stringSet.add("cde"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ((StreamSerializable) stringSet).serialize(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        StringSet newStringSet = instance();
        ((StreamSerializable) newStringSet).deserialize(inputStream);

        assertTrue(newStringSet.contains("abc"));
        assertTrue(newStringSet.contains("cde"));
    }


    @Test(expected=SerializationException.class)
    public void testSimpleSerializationFails() {
        StringSet stringSet = instance();

        assertTrue(stringSet.add("abc"));
        assertTrue(stringSet.add("cde"));

        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Fail");
            }
        };

        ((StreamSerializable) stringSet).serialize(outputStream);
    }

	@Test
	public void testRandom01() {
		StringSet stringSet = instance();

		final String[] test = {"abc", "abd", "aba", "cd", "d" , "c" , "ab", "", "abcd"};
		final int[] testAB  = {1    , 2    , 3    , 3   , 3   , 3   , 4   , 4 , 5};

		assertEquals(0, stringSet.size());
		for (String s: test)
			assertFalse(stringSet.contains(s));

		for (int i = 0; i != test.length; i++)
		{
			assertEquals(true, stringSet.add(test[i]));
			assertEquals(i + 1, stringSet.size());
			assertEquals(testAB[i], stringSet.howManyStartsWithPrefix("ab"));
			for (int j = 0; j != test.length; j++)
				assertEquals(j <= i, stringSet.contains(test[j]));
		}

		for (int i = 0; i != test.length; i++)
		{
			assertEquals(true, stringSet.remove(test[i]));
			assertEquals(test.length - 1 - i, stringSet.size());
			assertEquals(testAB[testAB.length - 1] - testAB[i], stringSet.howManyStartsWithPrefix("ab"));
			for (int j = 0; j != test.length; j++)
				assertEquals(j > i, stringSet.contains(test[j]));
		}

		try {
			PipedOutputStream outputStream = new PipedOutputStream();
			PipedInputStream inputStream = new PipedInputStream(outputStream, 1024);
			((StreamSerializable) stringSet).serialize(outputStream);
			StringSet stringSet2 = instance();
			((StreamSerializable) stringSet2).deserialize(inputStream);
			for (String s: test)
				assertFalse(stringSet2.contains(s));
		} catch (IOException e) {
			//Well...
			e.printStackTrace();
		}
	}

    public static StringSet instance() {
        try {
            return (StringSet) Class.forName("ru.spbau.mit.StringSetImpl").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Error while class loading");
    }
}
