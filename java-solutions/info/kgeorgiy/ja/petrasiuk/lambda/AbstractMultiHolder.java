package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiHolder<T, R, H extends AbstractMultiHolder<T, R, H>> implements Holder<T, R, H> {
    protected final List<T> list;

    AbstractMultiHolder() {
        this.list = new ArrayList<>();
    }

    @Override
    public void set(T field) {
        list.add(field);
    }

    @Override
    abstract public R get();
}
