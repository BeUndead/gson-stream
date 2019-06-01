package com.com.gson.stream;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * Implementation of {@link TypeAdapterFactory} which returns {@link TypeAdapter TypeAdapters} for
 * dealing with {@link Stream Streams}.
 *
 * @see StreamTypeAdapter
 */
@ThreadSafe
public final class StreamTypeAdapterFactory implements TypeAdapterFactory {

    /**
     * Implementation will return a {@link StreamTypeAdapter} if the provided {@link TypeToken type}
     * is assignable to {@link Stream}.  If the given {@code type} is generic, and the
     * <strong>element</strong> type of the {@code Stream} can be determined, a specific
     * {@code StreamTypeAdapter} will be returned; otherwise one which uses {@link Object} as the
     * element type is instead returned.
     *
     * @param gson {@inheritDoc}
     * @param type {@inheritDoc}
     * @param <T>  {@inheritDoc}
     *
     * @return a {@code StreamTypeAdapter} if the given token is for a {@code Stream}; otherwise
     *         {@code null}
     */
    // Use of TypeToken makes it difficult to pull out the element type of a Stream.
    // Use StreamTypeAdapter directly if better type-safety is desired.
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        if (!Stream.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        final Type theType = type.getType();
        if (!(theType instanceof ParameterizedType)) {
            // Cannot determine the element type, so use Object instead
            return new StreamTypeAdapter(gson, TypeToken.get(Object.class));
        }

        final ParameterizedType theParameterizedType = (ParameterizedType) theType;
        return new StreamTypeAdapter(gson, TypeToken.get(theParameterizedType.getActualTypeArguments()[0]));
    }
}
