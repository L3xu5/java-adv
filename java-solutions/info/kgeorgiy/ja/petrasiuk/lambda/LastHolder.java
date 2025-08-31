package info.kgeorgiy.ja.petrasiuk.lambda;

public class LastHolder<T> extends AbstractSingleHolder<T, LastHolder<T>> {
    @Override
    public LastHolder<T> merge(LastHolder<T> other) {
        if (other.isPresent()) {
            return other;
        }
        return this;
    }
}
