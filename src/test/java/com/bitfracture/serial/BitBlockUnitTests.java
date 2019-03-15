package com.bitfracture.serial;

import com.bitfracture.iterator.InputStreamIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

class BitBlockUnitTests {
    @Test
    void testBitBlockSerialize() {
        List<Boolean> bits = new ArrayList<>();
        Random random = new SecureRandom();
        for (int i = 0; i < 1000; i++) {
            bits.add(random.nextBoolean());
        }

        BitBlock block = new BitBlock();
        block.pushAll(bits.iterator());

        byte[] blockSerial = block.toSerial();
        BitBlock newBlock = BitBlock.fromSerial(new InputStreamIterator(new ByteArrayInputStream(blockSerial)));

        Iterator<Boolean> orgIter = bits.iterator();
        Iterator<Boolean> blockIter = newBlock.iterator();

        orgIter.forEachRemaining(original -> Assertions.assertEquals(original, blockIter.next()));
        Assertions.assertFalse(blockIter.hasNext());
    }
}
