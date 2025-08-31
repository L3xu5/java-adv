package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractTreeSpliterator<E> implements Spliterator<E> {
    Queue<E> list;

    public AbstractTreeSpliterator() {
        list = new ArrayDeque<>();
    }

    protected AbstractTreeSpliterator(Queue<E> list) {
        this.list = list;
    }

    protected abstract void processHalf();

    @Override
    public abstract Spliterator<E> trySplit();

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (list.isEmpty()) {
            processHalf();
        }
        E first = list.poll();
        if (first == null) {
            return false;
        }
        action.accept(first);
        return true;
    }

    @Override
    public long estimateSize() {
        return list.size() * 2L;
    }

    @Override
    public long getExactSizeIfKnown() {
        return (characteristics() & SIZED) != 0 ? estimateSize() : -1;
    }

    @Override
    public int characteristics() {
        return ORDERED | SIZED | SUBSIZED;
    }
}
