package com.bitfracture.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

public class InputStreamIterator implements Iterator<Byte> {
    private InputStream inputStream;
    private Integer holding;

    public InputStreamIterator(InputStream stream) {
        this.inputStream = stream;
    }

    @Override
    public boolean hasNext() {
        try {
            if (null == holding) {
                holding = inputStream.read();
            }
            return holding > 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Byte next() {
        try {
            if (null == holding) {
                holding = inputStream.read();
            }
            if (holding < 0) {
                throw new RuntimeException("There is no data left to iterate through");
            }
            byte value = (byte) holding.intValue();
            holding = null;
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Input stream failed while iterating", e);
        }
    }

    @Override
    public void remove() {}

    @Override
    public void forEachRemaining(Consumer<? super Byte> action) {
        while (hasNext()) {
            action.accept(next());
        }
    }
}
