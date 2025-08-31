package info.kgeorgiy.ja.petrasiuk.lambda;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.Queue;

public abstract class AbstractSizedBinaryTreeSpliterator<T, E> extends AbstractTreeSpliterator<E> {
    protected Trees.SizedBinary<T> tree;

    public AbstractSizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        super();
        this.tree = tree;
    }

    public AbstractSizedBinaryTreeSpliterator(Queue<E> list) {
        super(list);
    }

    protected abstract void build(Trees.SizedBinary<T> tree);

    protected void processHalf() {
        if (tree == null) {
            return;
        }
        switch (tree) {
            case Trees.SizedBinary.Branch<T> branch -> {
                build(branch.left());
                tree = branch.right();
            }
            case Trees.Leaf<T> leaf -> {
                build(leaf);
                tree = null;
            }
        }
    }

    @Override
    public long estimateSize() {
        return (tree != null ? tree.size() : 0) + list.size();
    }

    public long getExactSizeIfKnown() {
        return estimateSize();
    }
}


//   ERROR: Test HardLambdaTest.test53_nestedNaryTreeSpliterator() failed: >>> HardLambdaTest Hard for info.kgeorgiy.ja.petrasiuk.lambda.Lambda / === test53_nestedNaryTreeSpliterator() / .toList() / java.lang.IllegalStateException: Accept exceeded fixed size of 1
//
//    info.kgeorgiy.java.advanced.base.ContextException: >>> HardLambdaTest Hard for info.kgeorgiy.ja.petrasiuk.lambda.Lambda / === test53_nestedNaryTreeSpliterator() / .toList() / java.lang.IllegalStateException: Accept exceeded fixed size of 1
//        at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.Context.checked(Context.java:79)
//        at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.Context.context(Context.java:67)
//        at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.Context.context(Context.java:50)
//        at info.kgeorgiy.java.advanced.lambda/info.kgeorgiy.java.advanced.lambda.LambdaTest.lambda$testSpliterator$6(LambdaTest.java:61)
//        at java.base/java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:1024)
//        at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:734)
//        at java.base/java.util.stream.ReferencePipeline$Head.forEachOrdered(ReferencePipeline.java:817)
//        at info.kgeorgiy.java.advanced.lambda/info.kgeorgiy.java.advanced.lambda.LambdaTest.testSpliterator(LambdaTest.java:61)
//        at info.kgeorgiy.java.advanced.lambda/info.kgeorgiy.java.advanced.lambda.HardLambdaTest.test53_nestedNaryTreeSpliterator(HardLambdaTest.java:40)
//        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
//        at java.base/java.util.ArrayList.forEach(ArrayList.java:1597)
//        at java.base/java.util.ArrayList.forEach(ArrayList.java:1597)
//    Caused by: java.lang.IllegalStateException: Accept exceeded fixed size of 1
//        at java.base/java.util.stream.Nodes$FixedNodeBuilder.accept(Nodes.java:1232)
//        at info.kgeorgiy.ja.petrasiuk.lambda.AbstractTreeSpliterator.tryAdvance(AbstractTreeSpliterator.java:33)
//        at java.base/java.util.Spliterator.forEachRemaining(Spliterator.java:332)
//        at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:570)
//        at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:560)
//        at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:636)
//        at java.base/java.util.stream.AbstractPipeline.evaluateToArrayNode(AbstractPipeline.java:291)
//        at java.base/java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:656)
//        at java.base/java.util.stream.ReferencePipeline.toArray(ReferencePipeline.java:662)
//        at java.base/java.util.stream.ReferencePipeline.toList(ReferencePipeline.java:667)
//        at info.kgeorgiy.java.advanced.lambda/info.kgeorgiy.java.advanced.lambda.LambdaTest$TestSpliterator.lambda$checker$0(LambdaTest.java:124)
//        at java.base/java.lang.Iterable.forEach(Iterable.java:75)
//        at info.kgeorgiy.java.advanced.lambda/info.kgeorgiy.java.advanced.lambda.LambdaTest.lambda$testSpliterator$5(LambdaTest.java:61)
//        at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.Context.lambda$context$0(Context.java:51)
//        at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.Context.checked(Context.java:75)
//        ... 11 more
//ERROR: Tests: failed