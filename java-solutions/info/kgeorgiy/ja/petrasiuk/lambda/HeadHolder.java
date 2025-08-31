package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.List;

public class HeadHolder<T> extends AbstractMultiHolder<T, List<T>, HeadHolder<T>> {
    private final int headLength;

    HeadHolder(int headLength) {
        super();
        this.headLength = headLength;
    }

    @Override
    public void set(T field) {
        if (list.size() < headLength) {
            super.set(field);
        }
    }

    @Override
    public List<T> get() {
        return list;
    }
}
