package ru.spbau.mit;

/**
 * Pair interface.
 * Should be able to return first and second elements.
 * Very easy.
 */
public interface Pair<T1, T2> {
    T1 getFirst();

    T2 getSecond();
}
