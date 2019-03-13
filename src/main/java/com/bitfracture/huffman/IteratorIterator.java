package com.bitfracture.huffman;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IteratorIterator <T> implements Iterator<T> {
    private boolean noMoreIterators = false;
    private Iterator<T> currentIterator;
    private Supplier<Optional<Iterator<T>>> iteratorSupplier;

    public IteratorIterator(Supplier<Optional<Iterator<T>>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    private boolean updateIterator() {
        if (noMoreIterators) {
            return false;
        }
        if (Objects.isNull(currentIterator) || !currentIterator.hasNext()) {
            currentIterator = iteratorSupplier.get().orElse(null);
            if (Objects.isNull(currentIterator)) {
                noMoreIterators = true;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        if (updateIterator()) {
            return currentIterator.hasNext();
        } else {
            return false;
        }
    }

    @Override
    public T next() {
        if (updateIterator()) {
            return currentIterator.next();
        } else {
            throw new IndexOutOfBoundsException("next() cannot be called on an empty iterator");
        }
    }

    @Override
    public void remove() {
        if (updateIterator()) {
            currentIterator.remove();
        } else {
            throw new IndexOutOfBoundsException("remove() cannot be called on an empty iterator");
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        while (hasNext()) {
            action.accept(next());
        }
    }
}
