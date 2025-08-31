package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Spliterator;

public class NestedNaryTreeSpliterator<T extends List<E>, E> extends AbstractNaryTreeSpliterator<T, E> {
    public NestedNaryTreeSpliterator(Trees.Nary<T> tree) {
        super(tree);
    }

    protected NestedNaryTreeSpliterator(Queue<E> list) {
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
            case Trees.Leaf<T> leaf -> list.addAll(leaf.value());
        }
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> spliterator = new NestedNaryTreeSpliterator<>(list);
        list = new ArrayDeque<>();
        return spliterator;
    }

    @Override
    public long getExactSizeIfKnown() {
        if (tree != null) {
            return switch (tree) {
                case Trees.Nary.Node<T> _ -> -1;
                case Trees.Leaf<T> leaf -> leaf.value().size() + list.size();
            };
        }
        return -1;
    }
}
