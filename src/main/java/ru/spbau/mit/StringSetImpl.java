package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * StringSetImplementation
 *
 * @author ldvsoft
 */
public class StringSetImpl implements StringSet, StreamSerializable {
    /**
     * Alphabet size.
     * Value is to support ASCII letters.
     */
    private static final int ALPHABET_SIZE = 127;
    /**
     * Trie root node.
     */
    private TrieNode root = new TrieNode();

    /**
     * Serialization.
     * Just calling TrieNode deep-first search-alike recursion.
     *
     * @param out Output stream, serialized data goes there
     */
    @Override
    public void serialize(OutputStream out) {
        try {
            root.serialize(out);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    /**
     * Deserialization.
     * Just calling TrieNode deep-first search-alike constructor.
     *
     * @param in Input stream, from which data is taken
     */
    @Override
    public void deserialize(InputStream in) {
        // Delete old trie
        root = new TrieNode();
        // Then build new one
        try {
            root = new TrieNode(in);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    /**
     * Adding string. If string was already in trie, do nothing.
     *
     * @param element new string to be added
     * @return was adding successful
     */
    @Override
    public boolean add(String element) {
        return root.dfs(element,
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // While descending child was not found, creating new one
                        node.children[c] = new TrieNode();
                        return true;
                    }
                },
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // Node for new string found, checking if it's already occupied
                        if (node.isFinal)
                            return false;
                        node.isFinal = true;
                        node.count += 1;
                        return true;
                    }
                },
                new TrieNode.DfsCallback() {
                    @Override
                    public void invoke(char c, TrieNode node) {
                        // Incrementing all parents' counts on success
                        node.count += 1;
                    }
                }
        );
    }

    /**
     * Check is there a given string in trie.
     *
     * @param element string to be found
     * @return is there a given string
     */
    @Override
    public boolean contains(String element) {
        return root.dfs(element,
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // If child was not found, just halt
                        return false;
                    }
                },
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // Found node, halting if there is no such element
                        return node.isFinal;
                    }
                },
                null
        );
    }

    /**
     * Remove string from trie.
     * If there was not, do nothing.
     *
     * @param element string to be removed
     * @return success on deleting
     */
    @Override
    public boolean remove(String element) {
        return root.dfs(element,
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // While descending child was not found, halting
                        return false;
                    }
                },
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // Node for new string found, checking if it's not occupied
                        if (!node.isFinal)
                            return false;
                        node.isFinal = false;
                        node.count -= 1;
                        return true;
                    }
                }, new TrieNode.DfsCallback() {
                    @Override
                    public void invoke(char c, TrieNode node) {
                        // Decrementing all parents' counts on success
                        node.count -= 1;
                        // If child has no strings in it, free memory
                        if (node.children[c].count == 0) {
                            node.children[c] = null;
                        }
                    }
                });
    }

    /**
     * How many string there are in trie.
     *
     * @return count of elements in trie
     */
    @Override
    public int size() {
        return root.count;
    }

    /**
     * Variable to save result of callback in `howManyStartsWithPrefix', because Java can't capture properly.
     */
    private int howManyStartsWithPrefixResult;

    /**
     * How many strings there are with given prefix.
     *
     * @param prefix the prefix
     * @return count of elements with that prefix
     */
    @Override
    public int howManyStartsWithPrefix(String prefix) {
        howManyStartsWithPrefixResult = 0;
        root.dfs(prefix,
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // While descending child was not found, halting
                        return false;
                    }
                },
                new TrieNode.DfsStoppableCallback() {
                    @Override
                    public boolean invoke(char c, TrieNode node) {
                        // Node found, gathering result
                        howManyStartsWithPrefixResult = node.count;
                        return true;
                    }
                },
                null);
        return howManyStartsWithPrefixResult;
    }

    /**
     * Internal trie nodes
     */
    private static class TrieNode {
        /**
         * Is that node final for string stored in trie
         */
        private boolean isFinal = false;
        /**
         * Array of children nodes. If there is child, corresponding to letter `a',
         * it will be stored at `children[`a']', else there will be null
         */
        private TrieNode[] children = new TrieNode[ALPHABET_SIZE];
        /**
         * Count of string in subtrie of this node. That does include this node.
         */
        private int count = 0;

        /**
         * Default constructor. Used for creating empty root node.
         */
        private TrieNode() {
        }

        /**
         * Deserialisation constructor.
         * Creates subtrie described in input stream.
         * As described in `serialize', it will read subtree until zero-child.
         *
         * @param in Input stream
         * @throws IOException in case of IO failure
         */
        private TrieNode(InputStream in) throws IOException {
            if (in.read() == 1) {
                isFinal = true;
                count += 1;
            }
            while (true) {
                char c = (char) in.read();
                if (c == 0) {
                    return;
                }
                children[c] = new TrieNode(in);
                count += children[c].count;
            }
        }

        /**
         * Serialisation of subtree of this node.
         * Format is:
         * 1. 1 byte: 0 or 1 for `isFinal'
         * 2. 1 byte for next child's letter, 0 if there's none
         * 3. If previous char was not zero, recursively described subtrie of that child.
         * After, go again to step 2.
         *
         * @param out Output stream
         * @throws IOException in case of IO failure
         */
        private void serialize(OutputStream out) throws IOException {
            out.write(isFinal ? 1 : 0);
            for (char c = 1; c != ALPHABET_SIZE; c++) {
                if (children[c] == null) {
                    continue;
                }
                out.write(c);
                children[c].serialize(out);
            }
            out.write(0);
        }

        private interface DfsStoppableCallback {
            /**
             * Callback interface for dfs.
             * It can interrupt work of descending and ascending back.
             * For example, child node not found while removing element
             *
             * @param c    current char of descending, 0 if not required
             * @param node current node
             * @return true if to continue, false to stop
             */
            boolean invoke(char c, TrieNode node);
        }

        private interface DfsCallback {
            /**
             * Callback interface for dfs.
             *
             * @param c    current char of descending, 0 if not required
             * @param node current node
             */
            void invoke(char c, TrieNode node);
        }

        /**
         * General method for descending-ascening in trie.
         *
         * @param param string, describing descending path
         * @param onChildNotFound Callback, invoked when child not was not found while descending
         * @param onNodeReached Callback, invoked when node for `param' was found
         * @param onRollback Callback, invoked when ascending back from found node
         * @return true on success, false on failure (may be impiled by first two callbacks)
         */
        private boolean dfs(String param,
                            DfsStoppableCallback onChildNotFound,
                            DfsStoppableCallback onNodeReached,
                            DfsCallback onRollback) {
            return dfs(param, 0, onChildNotFound, onNodeReached, onRollback);
        }

        /**
         * Implementation for descending-ascening in trie.
         *
         * @param param string, describing descending path
         * @param pos current string pos
         * @param onChildNotFound Callback, invoked when child not was not found while descending
         * @param onNodeReached Callback, invoked when node for `param' was found
         * @param onRollback Callback, invoked when ascending back from found node
         * @return true on success, false on failure (may be impiled by first two callbacks)
         */
        private boolean dfs(String param, int pos,
                            DfsStoppableCallback onChildNotFound,
                            DfsStoppableCallback onNodeReached,
                            DfsCallback onRollback) {
            if (pos != param.length()) {
                char nextChar = param.charAt(pos);
                if (children[nextChar] == null) {
                    if (onChildNotFound != null && !onChildNotFound.invoke(nextChar, this)) {
                        return false;
                    }
                }
                TrieNode child = children[nextChar];
                if (!child.dfs(param, pos + 1, onChildNotFound, onNodeReached, onRollback)) {
                    return false;
                }
                // Already success, not interrupting
                if (onRollback != null)
                    onRollback.invoke(nextChar, this);
                return true;
            } else {
                return !(onNodeReached != null && !onNodeReached.invoke((char) 0, this));
            }
        }
    }
}
