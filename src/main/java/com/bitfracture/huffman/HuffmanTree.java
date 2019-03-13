package com.bitfracture.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanTree {
    private HuffmanNode head;

    private HuffmanTree() {}

    HuffmanNode getHead() {
        return this.head;
    }

    byte[] toSerial() {
        //Flattens the tree into a set of serial nodes
        List<SerialNode> serial = new ArrayList<>();
        toSerial(head, serial);

        //Pack the flattened node structure into bytes
        List<Byte> bytes = new ArrayList<>();
        Iterator<SerialNode> serialIterator = serial.iterator();
        while (serialIterator.hasNext()) {
            int branches = 0;
            SerialNode node;
            while (SerialNodeType.BRANCH.equals((node = serialIterator.next()).getType())) {
                if (branches == 127) {
                    //Represents 127 branches that don't end in a value (VERY rare and lopsided tree...)
                    bytes.add((byte)branches);
                    branches = 0;
                }
                branches++;
            }
            //Represents N<=127 branches that end in a value
            bytes.add((byte)(branches | 0b10000000));
            //Represents the value itself
            bytes.add(node.getValue());
        }

        //Translate to primitive array and return
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); byteArray[i] = bytes.get(i++));
        return byteArray;
    }

    private void toSerial(HuffmanNode node, List<SerialNode> serial) {
        if (node.getLeafValue() == null) {
            serial.add(SerialNode.ofBranch());
            toSerial(node.getLeft(), serial);
            toSerial(node.getRight(), serial);
        } else {
            serial.add(SerialNode.ofValue(node.getLeafValue()));
        }
    }

    static HuffmanTree fromSerial(byte[] serial) {
        //Unpack the tree bytes back to a flat set of serial nodes
        List<SerialNode> serialNodes = new ArrayList<>();
        for (int i = 0; i < serial.length; i++) {
            int numBranches = serial[i] & 0b01111111;
            System.out.println("Extracting: " + numBranches);
            for (int j = 0; j < numBranches; j++) {
                serialNodes.add(SerialNode.ofBranch());
            }
            if ((serial[i] & 0b10000000) > 0) {
                System.out.println("Extracting value: " + serial[i + 1]);
                serialNodes.add(SerialNode.ofValue(serial[++i]));
            }
        }

        //Recursively reconstruct the tree
        HuffmanTree newTree = new HuffmanTree();
        newTree.head = fromSerial(serialNodes.iterator());
        return newTree;
    }

    private static HuffmanNode fromSerial(Iterator<SerialNode> serial) {
        SerialNode node = serial.next();
        if (SerialNodeType.VALUE.equals(node.getType())) {
            return HuffmanNode.fromValue(node.getValue());
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
        /*StringBuilder builder = new StringBuilder();
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
        return builder.toString();*/
        return super.toString();
    }

    /**
     * Represents a single huffman node in a flat (serial) format. This is an intermediate step in serialization.
     */
    private static class SerialNode {
        private SerialNodeType type;
        private Byte nodeValue;

        private SerialNode(SerialNodeType type, Byte value) {
            this.type = type;
            this.nodeValue = value;
        }

        Byte getValue() {
            return this.nodeValue;
        }

        SerialNodeType getType() {
            return this.type;
        }

        static SerialNode ofValue(byte value) {
            return new SerialNode(SerialNodeType.VALUE, value);
        }

        static SerialNode ofBranch() {
            return new SerialNode(SerialNodeType.BRANCH, null);
        }
    }

    private enum SerialNodeType {
        BRANCH,
        VALUE
    }
}
