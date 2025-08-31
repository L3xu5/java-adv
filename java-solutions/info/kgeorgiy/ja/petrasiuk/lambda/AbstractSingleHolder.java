package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.Optional;

public abstract class AbstractSingleHolder<T, H extends AbstractSingleHolder<T, H>> implements Holder<T, Optional<T>, H> {
    protected T field;

    @Override
    public void set(T field) {
        this.field = field;
    }

    @Override
    public Optional<T> get() {
        return Optional.ofNullable(field);
    }

    protected boolean isPresent() {
        return field != null;
    }
}
