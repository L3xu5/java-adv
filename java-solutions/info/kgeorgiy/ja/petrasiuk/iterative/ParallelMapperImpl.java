package info.kgeorgiy.ja.petrasiuk.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Implements parallel mapping of a function over a list of items using a thread pool.
 * The class manages a fixed number of worker threads that process tasks concurrently.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Job<?, ?>> jobs = new ArrayDeque<>();
    private final List<Thread> threads = new ArrayList<>();
    private volatile boolean isClosed = false;

    /**
     * Constructs a new {@code ParallelMapperImpl} with the specified number of worker threads.
     * Each thread processes tasks from a shared job queue until the mapper is closed.
     *
     * @param threadsCount the number of worker threads to create
     * @throws IllegalArgumentException if {@code threadsCount} is less than 1
     */
    public ParallelMapperImpl(int threadsCount) {
        for (int i = 0; i < threadsCount; i++) {
            Thread worker = new Thread(() -> {
                while (!isClosed) {
                    Job<?, ?> job;
                    synchronized (jobs) {
                        while (jobs.isEmpty() && !isClosed) {
                            try {
                                jobs.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                        job = jobs.poll();
                    }
                    if (job != null) {
                        job.run();
                    }
                }
            });
            threads.add(worker);
            worker.start();
        }
    }

    /**
     * Applies the specified function to each item in the input list in parallel, returning a list
     * of results in the same order as the input list.
     *
     * @param <T>   the type of the input items
     * @param <R>   the type of the results
     * @param f     the function to apply to each item
     * @param items the list of items to process
     * @return a list containing the results of applying the function to each item
     * @throws InterruptedException  if the current thread is interrupted while waiting for completion
     * @throws IllegalStateException if the mapper has been closed
     * @throws RuntimeException      if an exception occurs during task execution
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> items) throws InterruptedException {
        if (isClosed) {
            throw new IllegalStateException("Mapper is closed");
        }
        List<R> result = new ArrayList<>(Collections.nCopies(items.size(), null));
        DoneMonitor monitor = new DoneMonitor(threads.size());
        for (int i = 0; i < threads.size(); i++) {
            final int start = i * items.size() / threads.size();
            final int end = (i + 1) * items.size() / threads.size();
            synchronized (jobs) {
                jobs.add(new Job<T, R>(items.subList(start, end), f, result.subList(start, end), monitor));
                jobs.notify();
            }
        }
        monitor.waitUntilDone();
        RuntimeException exception = monitor.getException();
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    /**
     * Closes the mapper, stopping all worker threads and preventing further task submissions.
     * This method interrupts all threads and waits for them to terminate.
     * Once closed, the mapper cannot be reused.
     */
    @Override
    public void close() {
        if (isClosed) { // :NOTE: можно вынести копипасту в функцию
            throw new IllegalStateException("Mapper is already closed");
        }
        isClosed = true;
        synchronized (jobs) {
            jobs.notifyAll();
        }
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException ignored) {
                    // :NOTE: Thread.currentThread().interrupt();
                }
            }
        }

    }
}