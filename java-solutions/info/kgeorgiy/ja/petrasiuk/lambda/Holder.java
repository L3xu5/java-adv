package info.kgeorgiy.ja.petrasiuk.lambda;

import java.util.function.Supplier;
import java.util.stream.Collector;

public interface Holder<T, R, H extends Holder<T, R, H>> {
    static <T, H extends Holder<T, R, H>, R> Collector<T, H, R> getCollector(Supplier<H> supplier) {
        return Collector.of(
                supplier,
                H::set,
                H::merge,
                H::get
        );
    }

    void set(T field);

    R get();

    default H merge(H other) {
        throw new UnsupportedOperationException("Merge is not supported");
    }
}
