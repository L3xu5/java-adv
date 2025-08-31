package info.kgeorgiy.ja.petrasiuk.iterative;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a job that processes a subset of items by applying a given function and storing the results.
 * Implements {@link Runnable} to be executed by a thread in a parallel mapper.
 */
public class Job<T, R> implements Runnable {
    private final List<? extends T> items;
    private final Function<? super T, ? extends R> f;
    private final List<R> result;
    private final DoneMonitor monitor;

    /**
     * Constructs a new {@code Job} with the specified items, function, result list, and monitor.
     *
     * @param items   the list of items to process
     * @param f       the function to apply to each item
     * @param result  the list to store the results
     * @param monitor the monitor to track completion and exceptions
     */
    public Job(List<? extends T> items, Function<? super T, ? extends R> f, List<R> result, DoneMonitor monitor) {
        this.items = items;
        this.f = f;
        this.result = result;
        this.monitor = monitor;
    }

    /**
     * Executes the job by applying the function to each item in the input list and storing the results.
     * Any runtime exceptions are collected and reported to the monitor. Notifies the monitor when the job is complete.
     */
    @Override
    public void run() {
        RuntimeException exception = null;
        for (int i = 0; i < items.size(); i++) {
            try {
                result.set(i, f.apply(items.get(i)));
            } catch (RuntimeException e) {
                if (exception != null) {
                    exception.addSuppressed(e);
                } else {
                    exception = e;
                }
            }
        }
        if (exception != null) {
            monitor.addException(exception);
        }
        monitor.addPart();
    }
}