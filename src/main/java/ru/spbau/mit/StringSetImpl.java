package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ldvsoft on 18.09.15.
 */
public class StringSetImpl implements StringSet, StreamSerializable {
	private static final int ALPHABET_SIZE = 127;

	@Override
	public void serialize(OutputStream out) {
		try {
			root.serialize(out);
		} catch (IOException e) {
			throw new SerializationException();
		}
	}

	@Override
	public void deserialize(InputStream in) {
		// Delete old trie
		root = null;
		// Then build new one
		try {
			root = new TrieNode(in, null);
		} catch (IOException e) {
			throw new SerializationException();
		}
	}

	private static class TrieNode {
		public TrieNode[] children = new TrieNode[ALPHABET_SIZE];
		public TrieNode parent = null;
		public boolean isFinal = false;
		public int count = 0;

		public TrieNode(TrieNode parent) {
			this.parent = parent;
		}

		public void serialize(OutputStream out) throws IOException {
			out.write(isFinal ? 1 : 0);
			for (char c = 1; c != ALPHABET_SIZE; ++c) {
				if (children[c] == null)
					continue;
				out.write(c);
				children[c].serialize(out);
			}
			out.write(0);
		}

		public TrieNode(InputStream in, TrieNode parent) throws IOException {
			if (in.read() == 1) {
				isFinal = true;
				count += 1;
			}
			while (true) {
				char c = (char) in.read();
				if (c == 0)
					return;
				children[c] = new TrieNode(in, this);
				count += children[c].count;
			}
		}
	}

	private TrieNode root = new TrieNode(null);

	@Override
	public boolean add(String element) {
		TrieNode node = root;
		int pos = 0;
		while (pos != element.length()) {
			if (node.children[(int)element.charAt(pos)] == null) {
				node = node.children[(int) element.charAt(pos)] = new TrieNode(node);
			}
			else {
				node = node.children[(int) element.charAt(pos)];
			}
			pos += 1;
		}
		if (node.isFinal)
			return false;
		node.isFinal = true;
		while (node != null)
		{
			node.count += 1;
			node = node.parent;
		}
		return true;
	}

	@Override
	public boolean contains(String element) {
		TrieNode node = root;
		int pos = 0;
		while (pos != element.length()) {
			if (node.children[(int)element.charAt(pos)] == null) {
				return false;
			}
			node = node.children[(int) element.charAt(pos)];
			pos += 1;
		}
		return node.isFinal;
	}

	@Override
	public boolean remove(String element) {
		TrieNode node = root;
		int pos = 0;
		while (pos != element.length()) {
			if (node.children[(int)element.charAt(pos)] == null) {
				return false;
			}
			node = node.children[(int) element.charAt(pos)];
			pos += 1;
		}
		if (node.isFinal)
			return false;
		node.isFinal = false;
		while (node != null)
		{
			node.count -= 1;
			node = node.parent;
		}
		return true;
	}

	@Override
	public int size() {
		return howManyStartsWithPrefix("");
	}

	@Override
	public int howManyStartsWithPrefix(String prefix) {
		TrieNode node = root;
		int pos = 0;
		while (pos != prefix.length()) {
			if (node.children[(int)prefix.charAt(pos)] == null) {
				return 0;
			}
			node = node.children[(int) prefix.charAt(pos)];
			pos += 1;
		}
		return node.count;
	}
}
