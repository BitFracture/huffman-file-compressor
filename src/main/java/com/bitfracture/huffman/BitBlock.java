package com.bitfracture.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.function.Consumer;

public class BitBlock implements Iterable<Boolean> {
    private static final int BLOCK_SIZE_BYTES = 1024;
    private static final int INTEGER_SIZE_BYTES = 4;
    private static final int BYTE_WIDTH = 8;
    private static final int MAX_RAW_BYTES = BLOCK_SIZE_BYTES - INTEGER_SIZE_BYTES;
    private static final int MAX_BIT_COUNT = MAX_RAW_BYTES * BYTE_WIDTH;

    private byte[] raw = new byte[MAX_RAW_BYTES];
    private int bitCount = 0;

    /**
     * Sets N bits within the block from an iterable source of data. If false is returned, the iterator is not empty,
     * and another block may continue where this one left off.
     *
     * @param bits  An iterable collection of bits to add to the block
     * @return  Whether all bits in the iterator were written (false means some still remain, and the block is full)
     */
    public boolean pushAll(Iterator<Boolean> bits) {
        while (bits.hasNext()) {
            if (isFull()) {
                return false;
            }
            this.push(bits.next());
        }
        return true;
    }

    /**
     * Sets a single bit within the appropriate byte in the raw array.
     *
     * @param bit  The value of the bit to set
     * @return  Whether the bit was written (false means no change to data, the block is full)
     */
    public boolean push(boolean bit) {
        if (isFull()) {
            return false;
        }
        int byteAddr = bitCount / BYTE_WIDTH;
        int bitOffset = bitCount++ % BYTE_WIDTH;
        int applyMask = 0b00000001 << bitOffset;
        int clearMask = ~applyMask;
        raw[byteAddr] = (byte)((raw[byteAddr] & clearMask) | (bit ? applyMask : 0));
        return true;
    }

    /**
     * @return  True indicates there are no bits stored in this block
     */
    public boolean isEmpty() {
        return bitCount <= 0;
    }

    /**
     * @return  True indicates no more bits may fit into this block
     */
    public boolean isFull() {
        return bitCount >= MAX_BIT_COUNT;
    }

    public static BitBlock fromSerialStream(InputStream serial) throws IOException {
        BitBlock newBitBlock = new BitBlock();
        byte[] intBuffer = new byte[INTEGER_SIZE_BYTES];
        serial.read(intBuffer, 0, INTEGER_SIZE_BYTES);
        newBitBlock.bitCount = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
        serial.read(newBitBlock.raw, 0, newBitBlock.bitCount);
        return newBitBlock;
    }

    public static BitBlock fromSerial(Iterator<Byte> serial) {
        BitBlock newBitBlock = new BitBlock();
        byte[] intBuffer = new byte[INTEGER_SIZE_BYTES];
        for (int i = 0; i < INTEGER_SIZE_BYTES; i++) {
            intBuffer[i] = serial.next();
        }
        newBitBlock.bitCount = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int rawLen = newBitBlock.bitCount / BYTE_WIDTH + ((newBitBlock.bitCount % BYTE_WIDTH) > 0 ? 1 : 0);
        for (int i = 0; i < rawLen; i++) {
            newBitBlock.raw[i] = serial.next();
        }
        return newBitBlock;
    }

    public byte[] toSerial() {
        int rawLen = bitCount / BYTE_WIDTH + ((bitCount % BYTE_WIDTH) > 0 ? 1 : 0);
        byte[] data = new byte[rawLen + INTEGER_SIZE_BYTES];

        //Write the bit length as the first 4 bytes
        byte[] intSerial = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(bitCount).array();
        System.arraycopy(intSerial, 0, data, 0, INTEGER_SIZE_BYTES);

        //Write the data bytes next
        System.arraycopy(raw, 0, data, INTEGER_SIZE_BYTES, rawLen);
        return data;
    }

    /**
     * Provides a way to iterate through the available bits in the order they were inserted
     *
     * @return  The bit iterator
     */
    @Override
    public Iterator<Boolean> iterator() {
        return new BitIterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BitBlock(");
        iterator().forEachRemaining(bit -> builder.append(bit ? "1" : "0"));
        builder.append(")");
        return builder.toString();
    }

    /**
     * Provides the concrete implementation for iterating through bits as boolean values.
     */
    private class BitIterator implements Iterator<Boolean> {
        int seekIndex = 0;

        /**
         * @return  Whether there is another iteration (Boolean) to get
         */
        @Override
        public boolean hasNext() {
            return seekIndex < bitCount;
        }

        /**
         * @return  The next iteration (Boolean)
         */
        @Override
        public Boolean next() {
            if (!hasNext()) {
                throw new IndexOutOfBoundsException("The BitIterator has reached the end of the data");
            }
            int byteAddr = seekIndex / BYTE_WIDTH;
            int bitOffset = seekIndex++ % BYTE_WIDTH;
            int isolationMask = 0b00000001 << bitOffset;
            return (raw[byteAddr] & isolationMask) > 0;
        }

        /**
         * The underlying data structure contains immutable bytes, so this method does nothing.
         */
        @Override
        public void remove() {}

        /**
         * Passes each iteration to the given consumer implementation.
         *
         * @param action  Consumer of each Boolean value
         */
        @Override
        public void forEachRemaining(Consumer<? super Boolean> action) {
            while (hasNext()) {
                action.accept(next());
            }
        }
    }
}
