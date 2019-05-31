package com.com.gson.stream;

import com.com.collections.SkippableIterator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of {@link Iterator} which locates and continuously {@link
 * TypeAdapter#fromJson(Reader) reads} the elements of the {@link JsonReader} using the constructed
 * {@link #componentAdapter} and converts the elements to a lazily loaded {@code Iterator}.
 * <p>
 * An instance of this class is single use; it is up to the user to determine what to do with the
 * resulting elements.
 */
final class JsonReaderSkippableIterator<T> implements SkippableIterator<T> {

    private TypeAdapter<T> componentAdapter;
    /**
     * The {@link JsonReader} containing the elements this {@link Iterator} should iterate over.
     */
    private final JsonReader reader;

    private Boolean lastHasNextResult = null;

    /**
     * Constructor; generates a new {@link JsonReaderSkippableIterator} using the provided {@link
     * JsonReader} (and their conversion using the {@link #componentAdapter}) as the source of
     * elements.
     *
     * @param componentAdapter the {@link TypeAdapter} for individual components of the {@link
     *                         Iterator}
     * @param reader           the reader to use as the source of elements.  It is assumed that
     *                         {@link JsonReader#beginArray()} has been called on this {@code
     *                         reader} <strong>prior</strong> to being given
     *                         <p>
     *                         {@link JsonReader#endArray()} will be called on a call to
     *                         {@link #hasNext()} which returns {@code false}
     */
    JsonReaderSkippableIterator(final TypeAdapter<T> componentAdapter,
                                final JsonReader reader) {
        this.componentAdapter = componentAdapter;
        this.reader = reader;
        // beginArray is called by the StreamTypeAdapter to ensure that null values can be
        // appropriately mapped to a null Stream.
    }


    // ==========================
    // Iterator<T> implementation
    // ==========================

    @Override
    public boolean hasNext() {
        if (this.lastHasNextResult != null) {
            return this.lastHasNextResult;
        }
        try {
            final boolean result = this.reader.hasNext();
            if (!result) {
                // When there are no results left, call 'endArray' so that the reader can neatly be
                // used if there are other elements (not a part of this array) to be used.
                this.reader.endArray();
            }
            return (this.lastHasNextResult = result);
        } catch (final IOException ioEx) {
            throw new RuntimeException(ioEx);
        }
    }

    @Override
    public T next() {
        if (this.hasNext()) {
            try {
                this.lastHasNextResult = null;
                return this.componentAdapter.read(this.reader);
            } catch (final IOException ioEx) {
                throw new RuntimeException(ioEx);
            }
        }

        throw new NoSuchElementException();
    }


    // ===================================
    // SkippableIterator<T> implementation
    // ===================================

    @Override
    public void skip() {
        if (this.hasNext()) {
            try {
                this.lastHasNextResult = null;
                this.reader.skipValue();
                return;
            } catch (final IOException ioEx) {
                throw new RuntimeException(ioEx);
            }
        }

        throw new NoSuchElementException();
    }
}
