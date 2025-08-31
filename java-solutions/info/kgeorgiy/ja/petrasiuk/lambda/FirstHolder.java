package info.kgeorgiy.ja.petrasiuk.lambda;

public class FirstHolder<T> extends AbstractSingleHolder<T, FirstHolder<T>> {
    @Override
    public void set(T field) {
        if (!super.isPresent()) {
            super.set(field);
        }
    }

    @Override
    public FirstHolder<T> merge(FirstHolder<T> other) {
        if (this.isPresent()) {
            return this;
        }
        return other;
    }
}
