package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.Queue;

public abstract class AbstractBinaryTreeSpliterator<T, E> extends AbstractTreeSpliterator<E> {
    protected Trees.Binary<T> tree;

    public AbstractBinaryTreeSpliterator(final Trees.Binary<T> tree) {
        super();
        this.tree = tree;
    }

    protected AbstractBinaryTreeSpliterator(Queue<E> list) {
        super(list);
    }

    protected abstract void build(Trees.Binary<T> tree);

    protected void processHalf() {
        if (tree == null) {
            return;
        }
        switch (tree) {
            case Trees.Binary.Branch<T> branch -> {
                build(branch.left());
                tree = branch.right();
            }
            case Trees.Leaf<T> leaf -> {
                build(leaf);
                tree = null;
            }
        }
    }

    @Override
    public long getExactSizeIfKnown() {
        if (tree != null && tree instanceof Trees.Leaf<T>) {
            return list.size() + 1;
        }
        return -1;
    }
}
