package ru.spbau.mit;

public interface StringSet {
    /**
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element
     */
    boolean add(String element);


    boolean contains(String element);

    /**
     * @return <tt>true</tt> if this set contained the specified element
     */
    boolean remove(String element);

    int size();

    int howManyStartsWithPrefix(String prefix);
}
