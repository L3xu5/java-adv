package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;

public class NaryTreeSpliterator<T> extends AbstractNaryTreeSpliterator<T, T> {

    public NaryTreeSpliterator(Trees.Nary<T> tree) {
        super(tree);
    }

    protected NaryTreeSpliterator(Queue<T> list) {
        super(list);
    }

    @Override
    protected void build(Trees.Nary<T> tree) {
        switch (tree) {
            case Trees.Nary.Node<T> node -> {
                for (Trees.Nary<T> subnode : node.children()) {
                    build(subnode);
                }
            }
            case Trees.Leaf<T> leaf -> list.add(leaf.value());
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        Spliterator<T> spliterator = new NaryTreeSpliterator<>(list);
        list = new ArrayDeque<>();
        return spliterator;
    }

    @Override
    public int characteristics() {
        return super.characteristics() | IMMUTABLE;
    }
}
