package com.bitfracture.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class HuffmanTree {
    private HuffmanNode head;

    private HuffmanTree() {}

    HuffmanNode getHead() {
        return this.head;
    }

    byte[] toSerial() {
        List<Byte> serial = new ArrayList<>();
        toSerial(head, serial);
        byte[] bytes = new byte[serial.size()];
        for (int i = 0; i < serial.size(); bytes[i] = serial.get(i++));
        return bytes;
    }

    private void toSerial(HuffmanNode node, List<Byte> serial) {
        if (node.getLeafValue() == null) {
            serial.add(SerialNodeType.NODE.getByteValue());
            toSerial(node.getLeft(), serial);
            toSerial(node.getRight(), serial);
        } else {
            serial.add(SerialNodeType.VALUE.getByteValue());
            serial.add(node.getLeafValue());
        }
    }

    static HuffmanTree fromSerial(byte[] serial) {
        HuffmanTree newTree = new HuffmanTree();
        List<Byte> serial2 = new ArrayList<>();
        for (int i = 0; i < serial.length; serial2.add(serial[i++]));
        newTree.head = fromSerial(serial2.iterator());
        return newTree;
    }

    private static HuffmanNode fromSerial(Iterator<Byte> serial) {
        SerialNodeType type = SerialNodeType.fromByte(serial.next()).orElseThrow(
                () -> new RuntimeException("The Huffman Tree node type could not be interpreted"));
        if (type.equals(SerialNodeType.VALUE)) {
            return HuffmanNode.fromValue(serial.next());
        } else {
            HuffmanNode left = fromSerial(serial);
            HuffmanNode right = fromSerial(serial);
            return HuffmanNode.fromNodes(right, left);
        }
    }

    static HuffmanTree fromDataStream(InputStream bytes) {
        //Build a map from byte value to HuffmanNode to accumulate totals (see incrementBaseWeight)
        Map<Byte, HuffmanNode> initialNodes = new HashMap<>();

        try {
            for (int raw; (raw = bytes.read()) >= 0;) {
                Byte byt = (byte)raw;
                HuffmanNode newNode = HuffmanNode.fromValue(byt);
                initialNodes.putIfAbsent(byt, newNode);
                initialNodes.get(byt).incrementBaseWeight();
            }
        } catch (IOException e) {
            throw new RuntimeException("Data failed to read due to a IO error", e);
        }

        //Place all HuffmanNode leaves into the initial priority queue to be ordered by weight
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        initialNodes.forEach((byteVal, node) -> queue.add(node));
        System.out.println(String.format("Accumulated %d initial nodes", queue.size()));

        //While the queue has more than one item, join the lowest two nodes using a parent node
        while (queue.size() > 1) {
            queue.add(HuffmanNode.fromNodes(queue.remove(), queue.remove()));
        }

        HuffmanTree newTree = new HuffmanTree();
        newTree.head = queue.peek();
        return newTree;
    }

    byte decode(Iterator<Boolean> iterator) {
        return head.seek(iterator);
    }

    Map<Byte, List<Boolean>> toMap() {
        Map<Byte, List<Boolean>> map = new HashMap<>();
        toMap(head, map, new ArrayList<>());
        return map;
    }

    /**
     * Traverse the entire tree recursively, accumulating bits to build a reverse-lookup map from byte to bit encoding.
     *
     * @param node  The local root node to examine
     * @param map  The current accumulation of bits to their encoded form
     * @param accumulator  The accumulation of bits so far on this branch
     */
    private void toMap(HuffmanNode node, Map<Byte, List<Boolean>> map, List<Boolean> accumulator) {
        if (null == node.getLeafValue()) {
            List<Boolean> leftAccum = new ArrayList<>(accumulator);
            List<Boolean> rightAccum = new ArrayList<>(accumulator);
            leftAccum.add(false);
            rightAccum.add(true);
            toMap(node.getLeft(), map, leftAccum);
            toMap(node.getRight(), map, rightAccum);
        } else {
            map.put(node.getLeafValue(), accumulator);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HuffmanTree(");
        byte[] serial = toSerial();
        for (int i = 0; i < serial.length; i++) {
            SerialNodeType type = SerialNodeType.fromByte(serial[i]).orElseThrow(
                    () -> new RuntimeException("Can't determine the given serial tree node type"));
            if (SerialNodeType.VALUE.equals(type)) {
                builder.append(String.format("V(%d)", serial[++i]));
            } else {
                builder.append("B");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private enum SerialNodeType {
        NODE((byte)0b00000010),
        VALUE((byte)0b00000011);

        private final Byte byteValue;

        private SerialNodeType(Byte byteValue) {
            this.byteValue = byteValue;
        }

        Byte getByteValue() {
            return this.byteValue;
        }

        static Optional<SerialNodeType> fromByte(Byte byteValue) {
            return Stream.of(SerialNodeType.values())
                    .filter(type -> type.getByteValue().equals(byteValue))
                    .findFirst();
        }
    }
}
