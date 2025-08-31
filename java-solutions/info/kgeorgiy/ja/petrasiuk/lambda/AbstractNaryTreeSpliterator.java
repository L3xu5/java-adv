package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.Queue;

public abstract class AbstractNaryTreeSpliterator<T, E> extends AbstractTreeSpliterator<E> {
    Trees.Nary<T> tree;

    public AbstractNaryTreeSpliterator(final Trees.Nary<T> tree) {
        super();
        this.tree = tree;
    }

    protected AbstractNaryTreeSpliterator(Queue<E> list) {
        super(list);
    }

    protected abstract void build(Trees.Nary<T> tree);

    protected void processHalf() {
        if (tree == null) {
            return;
        }
        switch (tree) {
            case Trees.Nary.Node<T> node -> {
                if (node.children().size() == 1) {
                    build(node.children().getFirst());
                    tree = null;
                } else {
                    build(new Trees.Nary.Node<>(node.children().subList(0, node.children().size() / 2)));
                    tree = new Trees.Nary.Node<>(node.children().subList(node.children().size() / 2,
                            node.children().size()));
                }
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
