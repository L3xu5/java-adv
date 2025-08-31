package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.HardLambda;
import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collector;

public class Lambda implements HardLambda {


    @Override
    public <T> Spliterator<T> nestedBinaryTreeSpliterator(Trees.Binary<List<T>> tree) {
        return new NestedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> nestedSizedBinaryTreeSpliterator(Trees.SizedBinary<List<T>> tree) {
        return new NestedSizedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> nestedNaryTreeSpliterator(Trees.Nary<List<T>> tree) {
        return new NestedNaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Collector<T, ?, List<T>> head(int k) {
        return Holder.getCollector(() -> new HeadHolder<>(k));
    }

    @Override
    public <T> Collector<T, ?, List<T>> tail(int k) {
        return Holder.getCollector(() -> new TailHolder<>(k));
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> kth(int k) {
        return Holder.getCollector(() -> new KthHolder<>(k));
    }

    @Override
    public <T> Spliterator<T> binaryTreeSpliterator(Trees.Binary<T> tree) {
        return new BinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> sizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        return new SizedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> naryTreeSpliterator(Trees.Nary<T> tree) {
        return new NaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> first() {
        return Holder.getCollector(FirstHolder::new);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> last() {
        return Holder.getCollector(LastHolder::new);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> middle() {
        return Holder.getCollector(MiddleHolder::new);
    }

    @Override
    public Collector<CharSequence, ?, String> commonSuffix() {
        return Holder.getCollector(CommonSuffixHolder::new);
    }

    @Override
    public Collector<CharSequence, ?, String> commonPrefix() {
        return Holder.getCollector(CommonPrefixHolder::new);
    }
}
