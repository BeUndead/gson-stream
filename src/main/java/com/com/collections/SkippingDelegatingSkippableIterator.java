package com.com.collections;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of {@link SkippableIterator} which delegates to a {@code SkippableIterator}
 * provided at {@linkplain #SkippingDelegatingSkippableIterator(SkippableIterator, long)} construction}
 * but will {@link SkippableIterator#skip()} the number of elements instructed at construction
 * before performing other operations.
 *
 * @param <T> {@inheritDoc}
 */
@NotThreadSafe
final class SkippingDelegatingSkippableIterator<T> implements SkippableIterator<T> {

    /**
     * The {@link SkippableIterator} which this {@link SkippingDelegatingSkippableIterator}
     * delegates to.
     */
    private final SkippableIterator<T> delegate;
    /**
     * The remaining number of elements to {@link #skip()}.
     */
    private long toSkip;

    /**
     * Constructor; generates a new {@link SkippingDelegatingSkippableIterator} which will
     * {@link SkippableIterator#skip()} {@code toSkip} elements of the provided {@link
     * SkippableIterator iterator} whenever an invocation occurs.
     *
     * @param iterator the {@code SkippableIterator} to delegate to
     * @param toSkip   the number of elements to {@code skip}
     */
    SkippingDelegatingSkippableIterator(final SkippableIterator<T> iterator,
                                        final long toSkip) {
        this.delegate = Objects.requireNonNull(iterator, "'iterator' must not be 'null'");
        if (toSkip < 0) {
            throw new IllegalArgumentException("'toSkip' must be non-negative, but was " + toSkip);
        }
        this.toSkip = toSkip;
    }


    // ===================================
    // SkippableIterator<T> implementation
    // ===================================

    @Override
    public void skip() {
        this.doSkipping();
        this.delegate.skip();
    }

    @Override
    public boolean hasNext() {
        this.doSkipping();
        return this.delegate.hasNext();
    }

    @Override
    public T next() {
        this.doSkipping();
        return this.delegate.next();
    }

    @Override
    public void remove() {
        this.doSkipping();
        this.delegate.remove();
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        this.doSkipping();
        this.delegate.forEachRemaining(action);
    }


    // ========
    // Internal
    // ========

    /**
     * Performs the skipping of {@link #toSkip} elements from the {@link #delegate}
     * {@link SkippableIterator}.
     */
    private void doSkipping() {
        while (--this.toSkip >= 0 && this.delegate.hasNext()) {
            this.delegate.skip();
        }
    }
}
