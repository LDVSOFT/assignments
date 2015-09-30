package ru.spbau.mit;

import java.util.*;

public class TreeSetImpl<E> extends AbstractSet<E> {
    private Comparator<E> comparator;
    private TreeNode root = null;
    private Random random = new Random(18031997);

    public TreeSetImpl(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    private NodePair split(TreeNode elem, E x, boolean allowEquals) {
        NodePair result = new NodePair();
        if (elem == null)
            return result;

        NodePair temp = null;
        int testedValue = allowEquals ? 1 : 0;
        if (comparator.compare(elem.x, x) < testedValue) {
            if (elem.right != null)
                elem.right.parent = null;
            temp = split(elem.right, x, allowEquals);
            result.l = elem;
            result.r = temp.r;
            elem.setRight(temp.l);
        } else {
            if (elem.left != null)
                elem.left.parent = null;
            temp = split(elem.left, x, allowEquals);
            result.l = temp.l;
            result.r = elem;
            elem.setLeft(temp.r);
        }
        if (result.l != null)
            result.l.parent = null;
        if (result.r != null)
            result.r.parent = null;
        return result;
    }

    private TreeNode merge(TreeNode l, TreeNode r) {
        if (l == null)
            return r;
        if (r == null)
            return l;
        if (l.y < r.y) {
            l.setRight(merge(l.right, r));
            return l;
        } else {
            r.setLeft(merge(l, r.left));
            return r;
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new TreeIterator();
    }

    @Override
    public int size() {
        return (root != null) ? root.size : 0;
    }

    @Override
    public boolean add(E e) {
        NodePair p1 = split(root, e, false);
        NodePair p2 = split(p1.r, e, true);
        boolean result = false;
        if (p2.l == null) {
            p2.l = new TreeNode(e);
            result = true;
        }
        root = merge(p1.l, merge(p2.l, p2.r));
        return result;
    }

    @Override
    public boolean contains(Object o) {
        NodePair p1 = split(root, (E) o, false);
        NodePair p2 = split(p1.r, (E) o, true);
        boolean result = false;
        if (p2.l != null) {
            result = true;
        }
        root = merge(p1.l, merge(p2.l, p2.r));
        return result;
    }

    @Override
    public boolean remove(Object o) {
        NodePair p1 = split(root, (E) o, false);
        NodePair p2 = split(p1.r, (E) o, true);
        boolean result = false;
        if (p2.l != null) {
            result = true;
        }
        root = merge(p1.l, p2.r);
        return result;
    }

    private class TreeNode {
        private E x;
        private int y = random.nextInt();
        private TreeNode left = null;
        private TreeNode right = null;
        private TreeNode parent = null;

        private int size = 1;

        private TreeNode(E x) {
            this.x = x;
        }

        private void invalidate() {
            size = 1;
            if (left != null)
                size += left.size;
            if (right != null)
                size += right.size;
        }

        private void setLeft(TreeNode child) {
            left = child;
            if (left != null) {
                left.parent = this;
            }
            invalidate();
        }


        private void setRight(TreeNode child) {
            right = child;
            if (right != null) {
                right.parent = this;
            }
            invalidate();
        }
    }

    private class NodePair {
        private TreeNode l = null;
        private TreeNode r = null;

        private NodePair() {
        }
    }

    private class TreeIterator implements Iterator<E> {
        private TreeNode lastNode = null;

        private TreeIterator() {
            if (node != null) {
                while (node.left != null) {
                    node = node.left;
                }
            }
        }

        private TreeNode node = root;

        private void goToNext() {
            if (node.right != null) {
                node = node.right;
                while (node.left != null) {
                    node = node.left;
                }
                return;
            }
            boolean wasFromRight = true;
            while (node != null && wasFromRight) {
                TreeNode prevNode = node;
                node = node.parent;
                wasFromRight = node != null && node.right == prevNode;
            }
        }

        @Override
        public void remove() {
            if (lastNode == null)
                throw new IllegalStateException();
            TreeSetImpl.this.remove(lastNode.x);
            lastNode = null;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public E next() {
            if (node == null)
                throw new NoSuchElementException();
            E result = node.x;
            lastNode = node;
            goToNext();
            return result;
        }


    }
}
