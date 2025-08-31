package info.kgeorgiy.ja.petrasiuk.walk;

import java.io.IOException;

@FunctionalInterface
public interface IOThrowableBiConsumer<T, U> {
    void accept(T t, U u) throws IOException;
}
