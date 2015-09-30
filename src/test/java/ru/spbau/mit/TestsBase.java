package ru.spbau.mit;

import org.junit.Assert;
import java.util.*;

public abstract class TestsBase extends Assert {

    protected Random random = new Random();

    protected TreeSetImpl<Integer> buildSet() {
        return new TreeSetImpl<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
    }
}
