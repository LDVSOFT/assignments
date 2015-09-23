package ru.spbau.mit;

/**
 * Pair interface.
 * Should be able to return first and second elements.
 * Very easy.
 */
public interface Pair<T1, T2> {
    /**
     * @return first element of pair
     */
    T1 getFirst();

    /**
     * @return second element of pair
     */
    T2 getSecond();
}
