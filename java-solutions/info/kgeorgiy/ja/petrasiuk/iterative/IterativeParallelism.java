package info.kgeorgiy.ja.petrasiuk.iterative;

import info.kgeorgiy.java.advanced.iterative.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Provides parallel processing capabilities for lists, utilizing either a provided {@link ParallelMapper}
 * or a custom thread pool. Implements the {@link ListIP} interface for operations like finding indices,
 * filtering, mapping, and summing indices.
 */
public class IterativeParallelism implements ListIP {
    private ParallelMapper mapper;

    /**
     * Constructs an {@code IterativeParallelism} instance with a specified {@link ParallelMapper}.
     *
     * @param mapper the parallel mapper to use for processing tasks
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Constructs an {@code IterativeParallelism} instance that uses a custom thread pool for parallel processing.
     */
    public IterativeParallelism() {
    }

    private <T> List<List<T>> splitParts(List<T> items, int totalParts) {
        List<List<T>> parts = new ArrayList<>();
        int size = items.size();
        for (int i = 0; i < totalParts; i++) {
            int start = i * size / totalParts;
            int end = (i + 1) * size / totalParts;
            parts.add(items.subList(start, end));
        }
        return parts;
    }

    private <T, R> List<R> processInParallel(
            int threads,
            List<T> items,
            Function<List<T>, R> f
    ) throws InterruptedException {
        List<Thread> threadsPool = new ArrayList<>(threads);
        List<List<T>> parts = splitParts(items, threads);
        if (mapper == null) {
            List<R> results = new ArrayList<>(Collections.nCopies(threads, null));
            for (int i = 0; i < parts.size(); i++) {
                int partIndex = i;
                Thread thread = new Thread(() -> results.set(partIndex, (f.apply(parts.get(partIndex)))));
                threadsPool.add(thread);
                thread.start();
            }
            InterruptedException wasInterrupted = null;
            for (Thread thread : threadsPool) {
                while (true) {
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException e) {
                        if (wasInterrupted != null) {
                            wasInterrupted.addSuppressed(e);
                        } else {
                            wasInterrupted = e;
                        }
                    }
                }
            }
            if (wasInterrupted != null) {
                throw wasInterrupted;
            }
            return results;
        } else {
            return mapper.map(f, parts);
        }
    }

    /**
     * Finds the index of the maximum element in the list using the provided comparator.
     *
     * @param <T>        the type of elements in the list
     * @param threads    the number of threads to use
     * @param values     the list to process
     * @param comparator the comparator to compare elements
     * @return the index of the maximum element, or -1 if the list is empty
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> int argMax(int threads, List<T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) return -1;

        List<Optional<T>> results = processInParallel(
                threads,
                values,
                part -> part.stream().max(comparator)
        );

        return results.stream()
                .flatMap(Optional::stream)
                .max(comparator)
                .map(values::indexOf)
                .orElse(-1);
    }

    /**
     * Finds the index of the minimum element in the list using the provided comparator.
     *
     * @param <T>        the type of elements in the list
     * @param threads    the number of threads to use
     * @param values     the list to process
     * @param comparator the comparator to compare elements
     * @return the index of the minimum element, or -1 if the list is empty
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> int argMin(int threads, List<T> values, Comparator<? super T> comparator) throws InterruptedException {
        return argMax(threads, values, comparator.reversed());
    }

    /**
     * Finds the index of the first element that satisfies the predicate.
     *
     * @param <T>       the type of elements in the list
     * @param threads   the number of threads to use
     * @param values    the list to process
     * @param predicate the predicate to test elements
     * @return the index of the first matching element, or -1 if no element matches
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> int indexOf(int threads, List<T> values, Predicate<? super T> predicate) throws InterruptedException {
        List<Optional<Integer>> results = processInParallel(
                threads,
                values,
                part -> part.stream()
                        .filter(predicate)
                        .findFirst()
                        .map(values::indexOf)
        );

        return results.stream()
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(-1);
    }

    /**
     * Finds the index of the last element that satisfies the predicate.
     *
     * @param <T>       the type of elements in the list
     * @param threads   the number of threads to use
     * @param values    the list to process
     * @param predicate the predicate to test elements
     * @return the index of the last matching element, or -1 if no element matches
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> int lastIndexOf(int threads, List<T> values, Predicate<? super T> predicate) throws InterruptedException {
        int result = indexOf(threads, values.reversed(), predicate);
        return result == -1 ? -1 : values.size() - result - 1;
    }

    /**
     * Computes the sum of indices of elements that satisfy the predicate.
     *
     * @param <T>       the type of elements in the list
     * @param threads   the number of threads to use
     * @param values    the list to process
     * @param predicate the predicate to test elements
     * @return the sum of indices of matching elements
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> long sumIndices(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        List<Long> results = processInParallel(
                threads,
                IntStream.range(0, values.size()).boxed().toList(),
                part -> part.stream()
                        .mapToLong(index -> predicate.test(values.get(index)) ? index : 0L)
                        .sum());

        return results.stream().reduce(0L, Long::sum);
    }

    /**
     * Returns an array of indices of elements that satisfy the predicate.
     *
     * @param <T>       the type of elements in the list
     * @param threads   the number of threads to use
     * @param values    the list to process
     * @param predicate the predicate to test elements
     * @return an array of indices of matching elements
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> int[] indices(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return processInParallel(
                threads,
                IntStream.range(0, values.size()).boxed().toList(),
                part -> part.stream()
                        .filter(index -> predicate.test(values.get(index))).toList())
                .stream()
                .flatMapToInt(list -> list.stream().mapToInt(Integer::intValue))
                .toArray();
    }

    /**
     * Filters elements from the list that satisfy the predicate.
     *
     * @param <T>       the type of elements in the list
     * @param threads   the number of threads to use
     * @param values    the list to process
     * @param predicate the predicate to test elements
     * @return a list of elements that satisfy the predicate
     * @throws InterruptedException if a thread is interrupted during execution
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return processInParallel(
                threads,
                values,
                part -> part.stream()
                        .filter(predicate).map(x -> (T) x).toList()
        ).stream()
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Applies a function to each element in the list and returns the results.
     *
     * @param <T>     the type of elements in the input list
     * @param <U>     the type of elements in the result list
     * @param threads the number of threads to use
     * @param values  the list to process
     * @param f       the function to apply to each element
     * @return a list of results after applying the function
     * @throws InterruptedException if a thread is interrupted during execution
     * @throws NullPointerException if the function is null
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        Objects.requireNonNull(f, "function must not be null");
        return processInParallel(
                threads,
                values,
                part -> part.stream().map(x -> (U) f.apply(x)).toList()
        ).stream()
                .flatMap(Collection::stream)
                .toList();
    }
}