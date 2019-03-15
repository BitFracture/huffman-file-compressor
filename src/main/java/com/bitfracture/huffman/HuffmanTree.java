package com.bitfracture.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A HuffmanTree is a binary tree where leaf nodes represent a byte, and each branch leading to that leaf represents a
 * bit in the encoded form for that byte. Traversing from the root node to a value, adding a 0 for each left, and 1
 * for each right, will result in the encoded form. Nodes closest to the root represent values that are most common in
 * the data set being encoded, such that they have the shortest encoded form.
 */
public class HuffmanTree {
    private HuffmanNode head;

    private HuffmanTree() {}

    HuffmanNode getHead() {
        return this.head;
    }

    byte[] toSerial() {
        //Get the serial tree and pack each node into bytes
        List<Byte> bytes = new ArrayList<>();
        Iterator<SerialHuffmanNode> serialIterator = toSerialTree().iterator();
        while (serialIterator.hasNext()) {
            int branches = 0;
            SerialHuffmanNode node;
            while (SerialHuffmanNode.SerialNodeType.BRANCH.equals((node = serialIterator.next()).getType())) {
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

    private List<SerialHuffmanNode> toSerialTree() {
        List<SerialHuffmanNode> serial = new ArrayList<>();
        toSerialTreeHelper(head, serial);
        return serial;
    }

    private void toSerialTreeHelper(HuffmanNode node, List<SerialHuffmanNode> serial) {
        if (node.getLeafValue() == null) {
            serial.add(SerialHuffmanNode.ofBranch());
            toSerialTreeHelper(node.getLeft(), serial);
            toSerialTreeHelper(node.getRight(), serial);
        } else {
            serial.add(SerialHuffmanNode.ofValue(node.getLeafValue()));
        }
    }

    static HuffmanTree fromSerial(byte[] serial) {
        //Unpack the tree bytes back to a flat set of serial nodes
        List<SerialHuffmanNode> serialNodes = new ArrayList<>();
        for (int i = 0; i < serial.length; i++) {
            int numBranches = serial[i] & 0b01111111;
            System.out.println("Extracting: " + numBranches);
            for (int j = 0; j < numBranches; j++) {
                serialNodes.add(SerialHuffmanNode.ofBranch());
            }
            if ((serial[i] & 0b10000000) > 0) {
                System.out.println("Extracting value: " + serial[i + 1]);
                serialNodes.add(SerialHuffmanNode.ofValue(serial[++i]));
            }
        }

        //Recursively reconstruct the tree
        HuffmanTree newTree = new HuffmanTree();
        newTree.head = fromSerialHelperTree(serialNodes.iterator());
        return newTree;
    }

    private static HuffmanNode fromSerialHelperTree(Iterator<SerialHuffmanNode> serial) {
        SerialHuffmanNode node = serial.next();
        if (SerialHuffmanNode.SerialNodeType.VALUE.equals(node.getType())) {
            return HuffmanNode.fromValue(node.getValue());
        } else {
            HuffmanNode left = fromSerialHelperTree(serial);
            HuffmanNode right = fromSerialHelperTree(serial);
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
}
