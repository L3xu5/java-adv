package info.kgeorgiy.ja.petrasiuk.lambda;

public class KthHolder<T> extends AbstractSingleHolder<T, KthHolder<T>> {
    private final int kth;
    private int current;

    public KthHolder(int kth) {
        this.kth = kth;
        this.current = 0;
    }

    @Override
    public void set(T field) {
        if (current == kth) {
            super.set(field);
        }
        current++;
    }
}
