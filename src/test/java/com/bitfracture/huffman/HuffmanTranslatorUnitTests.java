package com.bitfracture.huffman;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class HuffmanTranslatorUnitTests {
    @Test
    void testEncodeDecode() throws IOException {
        byte[] rawData = "I like fluffy bunnies because they are great!".getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream outEncode = new ByteArrayOutputStream();
        InputStream inTree = new ByteArrayInputStream(rawData);
        InputStream inEncode = new ByteArrayInputStream(rawData);

        HuffmanTree tree = HuffmanTree.fromDataStream(inTree);
        HuffmanTranslator.encode(tree, inEncode, outEncode);

        byte[] encoded = outEncode.toByteArray();
        InputStream inDecode = new ByteArrayInputStream(encoded);
        ByteArrayOutputStream outDecode = new ByteArrayOutputStream();

        HuffmanTranslator.decode(inDecode, outDecode);

        byte[] decoded = outDecode.toByteArray();

        Assertions.assertEquals(decoded.length, rawData.length);
        for (int i = 0; i < rawData.length; i++) {
            Assertions.assertEquals(rawData[i], decoded[i]);
        }
    }
}
