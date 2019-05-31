package com.com.gson.stream;

import com.com.collections.SkippableIteratorStream;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Implementation of {@link TypeAdapter} which handles <strong>lazily-loaded</strong> {@link Stream Streams}.
 *
 * @param <T> the <strong>element-type</strong> of the {@code Stream} that this {@code TypeAdapter} is for
 */
public final class StreamTypeAdapter<T> extends TypeAdapter<Stream<T>> {

    /**
     * {@link TypeAdapter} for the <strong>elements</strong> of the {@code Stream}.
     */
    private final TypeAdapter<T> componentAdapter;

    /**
     * Constructor; generates a new {@link StreamTypeAdapter} using the provided {@link Gson gson}
     * to create a {@link #componentAdapter component TypeAdapter}.
     *
     * @param gson  the underlying {@code Gson} instance to use
     * @param token the {@link TypeToken} of the <strong>element-type</strong> of the {@link Stream}
     *              that this {@code TypeAdapter} is for
     *
     * @throws NullPointerException if the provided {@code gson} or {@code token} is {@code null}
     */
    public StreamTypeAdapter(final Gson gson, final TypeToken<T> token) {
        this(Objects.requireNonNull(gson, "'gson' must not be 'null'")
                    .getAdapter(Objects.requireNonNull(token, "'token' must not be 'null'")));
    }

    /**
     * Constructor; generates a new {@link StreamTypeAdapter} with the provided
     * {@link #componentAdapter component TypeAdapter}.
     *
     * @param componentAdapter the {@code TypeAdapter} of components of the {@link Stream Streams}
     *
     * @throws NullPointerException if the provided {@code componentAdapter} is {@code null}
     */
    public StreamTypeAdapter(final TypeAdapter<T> componentAdapter) {
        this.componentAdapter = componentAdapter;
    }


    // =====================================
    // TypeAdapter<Stream<T>> implementation
    // =====================================

    /**
     * {@inheritDoc}
     * <p>
     * Implementation will write all elements of the {@link Stream} to the provided {@link
     * JsonWriter}.  A {@code null} {@code value} will serialize (if {@linkplain
     * JsonWriter#getSerializeNulls() requested by the provided {@code out}) to
     * {@link JsonWriter#nullValue() the json null}.
     */
    @Override
    public void write(final JsonWriter out,
                      final @Nullable Stream<T> value) throws IOException {

        if (value == null) {
            if (out.getSerializeNulls()) {
                out.nullValue();
            }
            return;
        }

        out.beginArray();
        try {
            value.forEach(item -> {
                try {
                    this.componentAdapter.write(out, item);
                } catch (final IOException ioEx) {
                    throw new LocalStreamingException(ioEx);
                }
            });
        } catch (final LocalStreamingException lsEx) {
            throw lsEx.ioException;
        }
        out.endArray();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation generates a {@link Stream} where the elements are lazily loaded by reading from the provided
     * {@link JsonReader} when they are requested.
     */
    @Override
    public @Nullable Stream<T> read(final JsonReader in) throws IOException {

        try {
            // Use beginArray BEFORE giving to JsonReaderSkippableIterator to optimise for expected use-case
            // of non-null arrays.
            in.beginArray();
        } catch (final IllegalStateException iSEx) {

            // An IllegalStateException here will intentionally propagate out
            in.nextNull();
            return null;
        }

        return new SkippableIteratorStream<>(new JsonReaderSkippableIterator<T>(this.componentAdapter, in));
    }


    // ========
    // Internal
    // ========


    /**
     * Extension of {@link RuntimeException} which allows us to re-throw an {@link Exception} without modification
     * or risk on pollution when defining a lambda which does <strong>not</strong> have such {@code Exceptions} in its
     * signature.
     * <p>
     * Instances of this {@code RuntimeException} are <strong>never</strong> expected to be thrown by {@code public}
     * methods of this class (without being handled).
     */
    private static class LocalStreamingException extends RuntimeException {

        /**
         * The {@link IOException} which was the <strong>cause</strong> of this {@link LocalStreamingException}.
         */
        private final IOException ioException;

        /**
         * Constructor; generates a new {@link LocalStreamingException} with the provided {@link IOException} as its
         * {@link #getCause() cause}.
         *
         * @param ioException the {@code IOException} which caused this {@code LocalStreamingException}
         */
        private LocalStreamingException(final IOException ioException) {
            super(null, ioException, false, false); // Suppress stack trace creation...  It won't be used.

            this.ioException = ioException;
        }
    }
}
