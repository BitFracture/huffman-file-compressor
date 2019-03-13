package com.bitfracture.huffman;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class HuffmanTreeUnitTests {
    @Test
    void testTreeCreation() {
        byte[] original = new byte[] {34, -30, 4, -45, 4, 33, 33, 127, 44, 33, -30, -50};

        HuffmanTree tree = HuffmanTree.fromDataStream(new ByteArrayInputStream(original));
        Map<Byte, List<Boolean>> translator = tree.toMap();
        List<Boolean> bits = new ArrayList<>();

        for (byte byt : original) {
            bits.addAll(translator.get(byt));
        }
        byte[] serialTree = tree.toSerial();

        HuffmanTree newTree = HuffmanTree.fromSerial(serialTree);

        List<Byte> decoded = new ArrayList<>();
        Iterator<Boolean> iter = bits.iterator();
        while (iter.hasNext()) {
            decoded.add(newTree.decode(iter));
        }

        for (int i = 0; i < original.length; i++) {
            Assertions.assertEquals(original[i], decoded.get(i).byteValue());
        }
    }
}
