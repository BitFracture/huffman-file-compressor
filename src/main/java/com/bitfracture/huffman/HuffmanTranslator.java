package com.bitfracture.huffman;

import com.bitfracture.iterator.InputStreamIterator;
import com.bitfracture.iterator.IteratorIterator;
import com.bitfracture.serial.BitBlock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HuffmanTranslator {
    private static final byte[] HEADER = new byte[] {0x48, 0x55, 0x46, 0x46};

    /**
     * Uses Huffman Tree encoding to build a binary tree using 'treeSourceData'.
     *
     * @param treeSourceData  The data used to construct the Huffman Tree
     */
    public static HuffmanTree generateTree(InputStream treeSourceData) {
        return HuffmanTree.fromDataStream(treeSourceData);
    }

    /**
     * Encodes the data in 'encodeData' using the given 'encodingTree'.
     * A 4-byte header 'HUFF' and the serialized Huffman Tree are prepended to the data send to encodedData, as they
     * are used in the decode process.
     *
     * @param rawData  The data to encode using the Huffman Tree (same as treeSourceData for smallest compression)
     * @param encodedData  The data stream after being encoded
     * @throws IOException
     */
    public static void encode(HuffmanTree encodingTree, InputStream rawData, OutputStream encodedData)
            throws IOException {
        byte[] tree = encodingTree.toSerial();
        byte[] treeLen = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(tree.length).array();
        encodedData.write(HEADER);
        encodedData.write(treeLen);
        encodedData.write(tree);

        //Encode the actual data
        Map<Byte, List<Boolean>> encoder = encodingTree.toMap();
        BitBlock bitBlock = new BitBlock();
        Iterator<Boolean> iter;
        for (int raw; (raw = rawData.read()) >= 0;) {
            iter = encoder.get((byte)raw).iterator();
            while (iter.hasNext()) {
                if (!bitBlock.pushAll(iter)) {
                    //Flush this full BitBlock to disk
                    encodedData.write(bitBlock.toSerial());
                    bitBlock = new BitBlock();
                }
            }
        }
        //Flush the last BitBlock to disk
        if (!bitBlock.isEmpty()) {
            encodedData.write(bitBlock.toSerial());
        }
    }

    public static void decode(InputStream encodeData, OutputStream rawData) throws IOException {
        Iterator<Byte> inputIterator = new InputStreamIterator(encodeData);

        //Require that this file starts with the header 'HUFF'
        for (int i = 0; i < 4; i++) {
            if (HEADER[i] != inputIterator.next()) {
                throw new RuntimeException("Invalid file header");
            }
        }

        //Determine how many serial bytes comprise the tree structure
        byte[] treeLenBytes = new byte[4];
        for (int i = 0; i < 4; treeLenBytes[i++] = inputIterator.next());
        int treeLen = ByteBuffer.wrap(treeLenBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        //Retrieve the serialized tree
        byte[] treeBytes = new byte[treeLen];
        for (int i = 0; i < treeLen; treeBytes[i++] = inputIterator.next());
        HuffmanTree tree = HuffmanTree.fromSerial(treeBytes);

        Iterator<Boolean> iteratorWrapper = new IteratorIterator<>(() -> {
            if (inputIterator.hasNext()) {
                return Optional.of(BitBlock.fromSerial(inputIterator).iterator());
            } else {
                return Optional.empty();
            }
        });
        while (iteratorWrapper.hasNext()) {
            rawData.write(unsignedByteAsInt(tree.decode(iteratorWrapper)));
        }
    }

    private static int unsignedByteAsInt(byte byt) {
        return byt & 0xFF;
    }
}
