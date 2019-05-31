package com.com.collections;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Extension of {@link Iterator} which additionally allows {@link #skip() skipping} elements.
 * The expectation is that {@link #hasNext()} is called <strong>before</strong> calling {@code
 * skip}.
 *
 * @param <T> {@inheritDoc}
 */
public interface SkippableIterator<T> extends Iterator<T>, Closeable {

    /**
     * Skips the {@link #next()} element of this {@link Iterator}.
     * <p>
     * It is expected that {@link #hasNext()} is called <strong>before</strong> invocation of this
     * method.
     *
     * @throws java.util.NoSuchElementException if there is no element to skip
     */
    default void skip() {
        this.next();
    }


    // ========================
    // Closeable implementation
    // ========================

    /**
     * {@inheritDoc}
     * <p>
     * Implementation {@link #skip() skips} any remaining elements in the {@link SkippableIterator}.
     */
    @Override
    default void close() {
        while (this.hasNext()) {
            this.skip();
        }
    }
}
