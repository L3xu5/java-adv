package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Spliterator;

public class NestedBinaryTreeSpliterator<T extends List<E>, E> extends AbstractBinaryTreeSpliterator<T, E> {
    public NestedBinaryTreeSpliterator(Trees.Binary<T> tree) {
        super(tree);
    }

    public NestedBinaryTreeSpliterator(Queue<E> list) {
        super(list);
    }

    protected void build(Trees.Binary<T> tree) {
        switch (tree) {
            case Trees.Binary.Branch<T> branch -> {
                build(branch.left());
                build(branch.right());
            }

            case Trees.Leaf<T> leaf -> list.addAll(leaf.value());
        }
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> spliterator = new NestedBinaryTreeSpliterator<>(list);
        list = new ArrayDeque<>();
        return spliterator;
    }

    @Override
    public int characteristics() {
        return super.characteristics();
    }

    @Override
    public long getExactSizeIfKnown() {
        if (tree != null) {
            return switch (tree) {
                case Trees.Binary.Branch<T> _ -> -1;
                case Trees.Leaf<T> leaf -> leaf.value().size() + list.size();
            };
        }
        return -1;
    }
}
