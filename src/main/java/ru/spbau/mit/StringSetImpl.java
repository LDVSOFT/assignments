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
	 * Value is to support ASCII letters
	 */
	private static final int ALPHABET_SIZE = 127;
	/**
	 * Trie root node.
	 */
	private TrieNode root = new TrieNode();

	/**
	 * Serialization.
	 * Just calling TrieNode deep-first search-alike recursion
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
	 * Just calling TrieNode deep-first search-alike constructor
	 *
	 * @param in Input stream, frow which data is taken
	 */
	@Override
	public void deserialize(InputStream in) {
		// Delete old trie
		root = null;
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
		// Descending in trie, creating new nodes if needed
		TrieNode node = root;
		int pos = 0;
		while (pos != element.length()) {
			if (node.children[(int) element.charAt(pos)] == null) {
				node = node.children[(int) element.charAt(pos)] = new TrieNode(node);
			} else {
				node = node.children[(int) element.charAt(pos)];
			}
			pos += 1;
		}
		if (node.isFinal) {
			// There is already given string in trie
			return false;
		}
		node.isFinal = true;
		// Ascending back, incrementing count
		while (node != null) {
			node.count += 1;
			node = node.parent;
		}
		return true;
	}

	/**
	 * Check is there a given string in trie
	 *
	 * @param element string to be found
	 * @return is there a given string
	 */
	@Override
	public boolean contains(String element) {
		// Descending in trie, searching for string.
		// If there is missing node, just return false
		TrieNode node = root;
		int pos = 0;
		while (pos != element.length()) {
			if (node.children[(int) element.charAt(pos)] == null) {
				return false;
			}
			node = node.children[(int) element.charAt(pos)];
			pos += 1;
		}
		return node.isFinal;
	}

	/**
	 * Remove string from trie.
	 * If there was not, do nothing
	 *
	 * @param element string to be removed
	 * @return success on deleting
	 */
	@Override
	public boolean remove(String element) {
		// Descending in trie, searching for string.
		// If there is missing node, just return false
		TrieNode node = root;
		int pos = 0;
		while (pos != element.length()) {
			if (node.children[(int) element.charAt(pos)] == null) {
				return false;
			}
			node = node.children[(int) element.charAt(pos)];
			pos += 1;
		}
		if (node.isFinal) {
			return false;
		}
		// Ascending back, decrementing count
		node.isFinal = false;
		while (node != null) {
			node.count -= 1;
			node = node.parent;
		}
		return true;
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
	 * How many strings there are with given prefix.
	 *
	 * @param prefix the prefix
	 * @return count of elements with that prefix
	 */
	@Override
	public int howManyStartsWithPrefix(String prefix) {
		// Descending in trie for common prefix node.
		// If there is missing node, just return 0
		TrieNode node = root;
		int pos = 0;
		while (pos != prefix.length()) {
			if (node.children[(int) prefix.charAt(pos)] == null) {
				return 0;
			}
			node = node.children[(int) prefix.charAt(pos)];
			pos += 1;
		}
		return node.count;
	}

	/**
	 * Internal trie nodes
	 */
	private static class TrieNode {
		/**
		 * Parent node. null, if root.
		 */
		public TrieNode parent = null;
		/**
		 * Is that node final for string stored in trie
		 */
		public boolean isFinal = false;
		/**
		 * Array of children nodes. If there is child, corresponding to letter `a',
		 * it will be stored at `children[`a']', else there will be null
		 */
		public TrieNode[] children = new TrieNode[ALPHABET_SIZE];
		/**
		 * Count of string in subtrie of this node. That does include this node.
		 */
		public int count = 0;

		/**
		 * Default constructor. Used for creating empty root node.
		 */
		public TrieNode() {
			this((TrieNode) null);
		}

		/**
		 * Constructor for empty non-root node.
		 *
		 * @param parent parent of a new node
		 */
		public TrieNode(TrieNode parent) {
			this.parent = parent;
		}

		/**
		 * Deserialisation constructor.
		 * Creates trie described in input stream (root node).
		 * As described in `serialize', it will read subtree until zero-child.
		 *
		 * @param in Input stream
		 * @throws IOException in case of IO failure
		 */
		public TrieNode(InputStream in) throws IOException {
			this(in, null);
		}

		/**
		 * Deserialisation constructor.
		 * Creates subtrie described in input stream with given parent.
		 * As described in `serialize', it will read subtree until zero-child.
		 *
		 * @param in     Input stream
		 * @param parent Parent of new node
		 * @throws IOException in case of IO failure
		 */
		private TrieNode(InputStream in, TrieNode parent) throws IOException {
			if (in.read() == 1) {
				isFinal = true;
				count += 1;
			}
			while (true) {
				char c = (char) in.read();
				if (c == 0) {
					return;
				}
				children[c] = new TrieNode(in, this);
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
		public void serialize(OutputStream out) throws IOException {
			out.write(isFinal ? 1 : 0);
			for (char c = 1; c != ALPHABET_SIZE; ++c) {
				if (children[c] == null) {
					continue;
				}
				out.write(c);
				children[c].serialize(out);
			}
			out.write(0);
		}


	}
}
