package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;

public class SizedBinaryTreeSpliterator<T> extends AbstractSizedBinaryTreeSpliterator<T, T> {
    public SizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        super(tree);
    }

    private SizedBinaryTreeSpliterator(Queue<T> list) {
        super(list);
    }

    @Override
    protected void build(Trees.SizedBinary<T> tree) {
        switch (tree) {
            case Trees.SizedBinary.Branch<T> branch -> {
                build(branch.left());
                build(branch.right());
            }
            case Trees.Leaf<T> leaf -> list.add(leaf.value());
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        Spliterator<T> spliterator = new SizedBinaryTreeSpliterator<>(list);
        list = new ArrayDeque<>();
        return spliterator;
    }

    @Override
    public int characteristics() {
        return super.characteristics() | IMMUTABLE;
    }
}
