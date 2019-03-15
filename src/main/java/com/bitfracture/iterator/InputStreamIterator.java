package com.bitfracture.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Iterators are more universal and are simpler to use than InputStreams. This iterator implementation abstracts away
 * the nuances of an input stream into a Byte Iterator. Once a stream has been wrapped in this Iterator, the iterator
 * owns that stream until it is empty, and no other consumers should be permitted to access the stream.
 */
public class InputStreamIterator implements Iterator<Byte> {
    private InputStream inputStream;
    private Integer holding;

    public InputStreamIterator(InputStream stream) {
        this.inputStream = stream;
    }

    /**
     * @return  Whether a byte is available to read
     */
    @Override
    public boolean hasNext() {
        try {
            if (Objects.isNull(holding)) {
                holding = inputStream.read();
            }
            return holding > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @return  Retrieves the next byte from the InputStream
     * @throws  RuntimeException If there is no data remaining
     */
    @Override
    public Byte next() {
        try {
            if (Objects.isNull(holding)) {
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
}
