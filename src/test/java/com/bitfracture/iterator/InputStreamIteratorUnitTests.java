package com.bitfracture.iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

class InputStreamIteratorUnitTests {
    @Test
    void testInputStreamIterator() {
        byte[] inputData = "This is a really complicated file\r\nWith new lines\netc.".getBytes();
        InputStream testInputStream = new ByteArrayInputStream(inputData);

        Iterator<Byte> inputIterator = new InputStreamIterator(testInputStream);

        for (Byte byt : inputData) {
            Assertions.assertTrue(inputIterator.hasNext());
            Assertions.assertEquals(byt, inputIterator.next());
        }

        Assertions.assertFalse(inputIterator.hasNext());
    }
}
