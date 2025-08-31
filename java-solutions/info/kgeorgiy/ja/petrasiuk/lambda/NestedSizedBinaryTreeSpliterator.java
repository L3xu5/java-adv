package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Spliterator;

public class NestedSizedBinaryTreeSpliterator<T extends List<E>, E> extends AbstractSizedBinaryTreeSpliterator<T, E> {
    public NestedSizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        super(tree);
    }

    protected NestedSizedBinaryTreeSpliterator(Queue<E> list) {
        super(list);
    }

    @Override
    protected void build(Trees.SizedBinary<T> tree) {
        switch (tree) {
            case Trees.SizedBinary.Branch<T> branch -> {
                build(branch.left());
                build(branch.right());
            }

            case Trees.Leaf<T> leaf -> list.addAll(leaf.value());
        }
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> spliterator = new NestedSizedBinaryTreeSpliterator<>(list);
        list = new ArrayDeque<>();
        return spliterator;
    }

    @Override
    public long getExactSizeIfKnown() {
        if (tree != null) {
            return switch (tree) {
                case Trees.SizedBinary.Branch<T> _ -> -1;
                case Trees.Leaf<T> leaf -> leaf.value().size() + list.size();
            };
        }
        return -1;
    }
}
