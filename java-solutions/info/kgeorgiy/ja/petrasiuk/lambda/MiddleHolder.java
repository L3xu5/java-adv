package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.Optional;

public class MiddleHolder<T> extends AbstractMultiHolder<T, Optional<T>, MiddleHolder<T>> {
    @Override
    public Optional<T> get() {
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list.get(list.size() / 2));
    }
}
