package info.kgeorgiy.ja.petrasiuk.arrayset;

import java.util.*;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements NavigableSet<E> {
    private final List<E> array;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        if (collection == null) {
            this.array = Collections.emptyList();
        } else {
            NavigableSet<E> tempSet = new TreeSet<>(comparator);
            tempSet.addAll(collection);
            this.array = tempSet.stream().toList();
        }
    }

    private ArraySet(List<E> array, Comparator<? super E> comparator) {
        this.array = array;
        this.comparator = comparator;
    }

    private ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    private int getIndex(E e, IndexType type) {
        int index = Collections.binarySearch(array, e, comparator);
        if (index < 0) {
            index = -index - 1;
            switch (type) {
                case LOWER, FLOOR -> index--;
            }
        } else {
            switch (type) {
                case LOWER -> index--;
                case HIGHER -> index++;
            }
        }
        return index;
    }

    @Override
    public E lower(E e) {
        int index = getIndex(e, IndexType.LOWER);
        return index >= 0 ? array.get(index) : null;
    }

    @Override
    public E floor(E e) {
        int index = getIndex(e, IndexType.FLOOR);
        return index >= 0 ? array.get(index) : null;
    }

    @Override
    public E ceiling(E e) {
        int index = getIndex(e, IndexType.CEILING);
        return index < array.size() ? array.get(index) : null;
    }

    @Override
    public E higher(E e) {
        int index = getIndex(e, IndexType.HIGHER);
        return index < array.size() ? array.get(index) : null;
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(array, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return array.iterator();
    }

    @Override
    public Object[] toArray() {
        return array.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return array.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(array.reversed(), comparator.reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return array.reversed().iterator();
    }

    private NavigableSet<E> sub(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromIndex = getIndex(fromElement, fromInclusive ? IndexType.CEILING : IndexType.HIGHER);
        int toIndex = getIndex(toElement, toInclusive ? IndexType.FLOOR : IndexType.LOWER) + 1;
        return new ArraySet<>(array.subList(fromIndex, Math.max(toIndex, fromIndex)), comparator);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (comparator != null && comparator.compare(fromElement, toElement) > 0
                || comparator == null && fromElement.compareTo(toElement) > 0) {
            throw new IllegalArgumentException("Cannot subset from element " + fromElement + " to element " + toElement);
        }
        return sub(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (array.isEmpty()) {
            return new ArraySet<E>(comparator);
        }
        return sub(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (array.isEmpty()) {
            return new ArraySet<E>(comparator);
        }
        return sub(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (array.isEmpty()) {
            throw new NoSuchElementException("Array is empty");
        }
        return array.getFirst();
    }

    @Override
    public E last() {
        if (array.isEmpty()) {
            throw new NoSuchElementException("Array is empty");
        }
        return array.getLast();
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }
}
