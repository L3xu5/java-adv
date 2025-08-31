package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.List;

public class TailHolder<T> extends AbstractMultiHolder<T, List<T>, TailHolder<T>> {
    private final int tailLength;

    TailHolder(int tailLength) {
        super();
        this.tailLength = tailLength;
    }


    @Override
    public void set(T field) {
        if (tailLength != 0 && list.size() == tailLength) {
            list.removeFirst();
        }
        if (list.size() < tailLength) {
            super.set(field);
        }
    }

    @Override
    public List<T> get() {
        return list.stream().toList();
    }
}