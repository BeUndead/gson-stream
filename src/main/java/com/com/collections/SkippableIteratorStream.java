package com.com.collections;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link Stream} which works for {@link SkippableIterator SkippableIterators}.
 *
 * @param <T> {@inheritDoc}
 */
@NotThreadSafe
public final class SkippableIteratorStream<T> implements Stream<T> {

    /**
     * The underlying {@link SkippableIterator} which this {@link SkippableIteratorStream} is to
     * stream over.
     */
    private final SkippableIterator<T> iterator;
    /**
     * A delegate {@link Stream} providing the base functionality of this {@link
     * SkippableIteratorStream}.
     */
    private final Stream<T> stream;

    /**
     * Constructor; generates a new {@link SkippableIteratorStream} which will stream over the
     * provided {@link SkippableIterator iterator}.
     *
     * @param iterator the {@code SkippableIterator} that this {@code SkippableIteratorStream} is
     *                 to stream over
     */
    public SkippableIteratorStream(final SkippableIterator<T> iterator) {
        this.iterator = Objects.requireNonNull(iterator, "'iterator' must not be 'null'");

        this.stream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this.iterator, Spliterator.ORDERED),
                false);

        this.stream.onClose(this::closeIterator);
    }


    // =====================
    // Stream implementation
    // =====================

    /**
     * {@inheritDoc}
     * <p>
     * Implementation uses a {@link SkippingDelegatingSkippableIterator} in order to delay the
     * {@link SkippableIterator#skip() skipping} until necessary as part of the pipeline.
     */
    @Override
    public Stream<T> skip(final long n) {
        return new SkippableIteratorStream<>(new SkippingDelegatingSkippableIterator<>(this.iterator, n));
    }


    // =================
    // Stream delegation
    // =================

    @Override
    public Stream<T> filter(final Predicate<? super T> predicate) {
        return this.stream.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(final Function<? super T, ? extends R> mapper) {
        return this.stream.map(mapper);
    }

    @Override
    public IntStream mapToInt(final ToIntFunction<? super T> mapper) {
        return this.stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(
            final ToLongFunction<? super T> mapper) {return this.stream.mapToLong(mapper);}

    @Override
    public DoubleStream mapToDouble(
            final ToDoubleFunction<? super T> mapper) {return this.stream.mapToDouble(mapper);}

    @Override
    public <R> Stream<R> flatMap(
            final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return this.stream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(
            final Function<? super T, ? extends IntStream> mapper) {
        return this.stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(
            final Function<? super T, ? extends LongStream> mapper) {
        return this.stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(
            final Function<? super T, ? extends DoubleStream> mapper) {
        return this.stream.flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {return this.stream.distinct();}

    @Override
    public Stream<T> sorted() {return this.stream.sorted();}

    @Override
    public Stream<T> sorted(final Comparator<? super T> comparator) {
        return this.stream.sorted(comparator);
    }

    @Override
    public Stream<T> peek(final Consumer<? super T> action) {
        return this.stream.peek(action);
    }

    @Override
    public Stream<T> limit(final long maxSize) {return this.stream.limit(maxSize);}

    @Override
    public void forEach(final Consumer<? super T> action) {this.stream.forEach(action);}

    @Override
    public void forEachOrdered(
            final Consumer<? super T> action) {this.stream.forEachOrdered(action);}

    @Override
    public Object[] toArray() {return this.stream.toArray();}

    @Override
    public <A> A[] toArray(final IntFunction<A[]> generator) {
        return this.stream.toArray(generator);
    }

    @Override
    public T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return this.stream.reduce(identity,
                                  accumulator);
    }

    @Override
    public Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return this.stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator,
                        final BinaryOperator<U> combiner) {
        return this.stream.reduce(identity,
                                  accumulator,
                                  combiner);
    }

    @Override
    public <R> R collect(final Supplier<R> supplier,
                         final BiConsumer<R, ? super T> accumulator,
                         final BiConsumer<R, R> combiner) {
        return this.stream.collect(supplier,
                                   accumulator,
                                   combiner);
    }

    @Override
    public <R, A> R collect(final Collector<? super T, A, R> collector) {
        return this.stream.collect(collector);
    }

    @Override
    public Optional<T> min(final Comparator<? super T> comparator) {
        return this.stream.min(comparator);
    }

    @Override
    public Optional<T> max(final Comparator<? super T> comparator) {
        return this.stream.max(comparator);
    }

    @Override
    public long count() {return this.stream.count();}

    @Override
    public boolean anyMatch(final Predicate<? super T> predicate) {
        return this.stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(final Predicate<? super T> predicate) {
        return this.stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(final Predicate<? super T> predicate) {
        return this.stream.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {return this.stream.findFirst();}

    @Override
    public Optional<T> findAny() {return this.stream.findAny();}

    @Override
    public Iterator<T> iterator() {return this.stream.iterator();}

    @Override
    public Spliterator<T> spliterator() {return this.stream.spliterator();}

    @Override
    public boolean isParallel() {return this.stream.isParallel();}

    @Override
    public Stream<T> sequential() {return this.stream.sequential();}

    @Override
    public Stream<T> parallel() {return this.stream.parallel();}

    @Override
    public Stream<T> unordered() {return this.stream.unordered();}

    @Override
    public Stream<T> onClose(final Runnable closeHandler) {
        return this.stream.onClose(closeHandler);
    }

    @Override
    public void close() {
        this.stream.close();
    }


    // ========
    // Internal
    // ========

    /**
     * {@link SkippableIterator#skip() Skips} all remaining elements of the {@link #iterator
     * SkippableIterator} provided at {@linkplain #SkippableIteratorStream(SkippableIterator)
     * construction}.
     */
    private void closeIterator() {
        while (this.iterator.hasNext()) {
            this.iterator.skip();
        }
    }
}
