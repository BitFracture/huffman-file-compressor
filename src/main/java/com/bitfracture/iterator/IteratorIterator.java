package com.bitfracture.iterator;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides an iterator which chains together the data from multiple iterators. The iteratorSupplier is called each
 * time the currently observed iterator is exhausted, and the data stream is uninterrupted.
 * @param <T>  The type this iterator presents
 */
public class IteratorIterator <T> implements Iterator<T> {
    private boolean noMoreIterators = false;
    private Iterator<T> currentIterator;
    private Supplier<Optional<Iterator<T>>> iteratorSupplier;

    public IteratorIterator(Supplier<Optional<Iterator<T>>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    /**
     * Updates the internal iterator via the supplier if the current internal iterator is exhausted or has not been set.
     * @return  True if the iterator is ready, false if there are no more iterators available
     */
    private boolean updateIterator() {
        if (noMoreIterators) {
            return false;
        }
        while (Objects.isNull(currentIterator) || !currentIterator.hasNext()) {
            currentIterator = iteratorSupplier.get().orElse(null);
            if (Objects.isNull(currentIterator)) {
                noMoreIterators = true;
                return false;
            }
        }
        return true;
    }

    /**
     * @return  Whether a byte is available to read
     */
    @Override
    public boolean hasNext() {
        if (updateIterator()) {
            return currentIterator.hasNext();
        } else {
            return false;
        }
    }

    /**
     * @return  Retrieves the next byte from the InputStream
     */
    @Override
    public T next() {
        if (updateIterator()) {
            return currentIterator.next();
        } else {
            throw new IndexOutOfBoundsException("next() cannot be called on an empty iterator");
        }
    }

    /**
     * Removes a single item from the internal iterator, where applicable.
     * Warning: Implementation of this method could vary for each internal stream. This is true for the other methods
     *     in this class as well, but the default implementation of remove() is to throw an exception.
     */
    @Override
    public void remove() {
        if (updateIterator()) {
            currentIterator.remove();
        } else {
            throw new IndexOutOfBoundsException("remove() cannot be called on an empty iterator");
        }
    }
}
