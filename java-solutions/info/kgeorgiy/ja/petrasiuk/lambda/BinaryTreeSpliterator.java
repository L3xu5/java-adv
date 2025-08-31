package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;

public class BinaryTreeSpliterator<T> extends AbstractBinaryTreeSpliterator<T, T> {
    public BinaryTreeSpliterator(Trees.Binary<T> tree) {
        super(tree);
    }

    private BinaryTreeSpliterator(Queue<T> list) {
        super(list);
    }

    protected void build(Trees.Binary<T> tree) {
        switch (tree) {
            case Trees.Binary.Branch<T> branch -> {
                build(branch.left());
                build(branch.right());
            }
            case Trees.Leaf<T> leaf -> list.add(leaf.value());
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        Spliterator<T> spliterator = new BinaryTreeSpliterator<>(list);
        list = new ArrayDeque<>();
        return spliterator;
    }

    @Override
    public int characteristics() {
        return super.characteristics() | IMMUTABLE;
    }
}
