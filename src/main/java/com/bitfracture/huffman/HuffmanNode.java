package com.bitfracture.huffman;

import java.util.Iterator;
import java.util.Optional;

class HuffmanNode implements Comparable<HuffmanNode> {
    private HuffmanNode left;
    private HuffmanNode right;
    private Byte leafValue;
    private int baseWeight = 0;

    private HuffmanNode() {}

    Byte getLeafValue() {
        return this.leafValue;
    }

    HuffmanNode getLeft() {
        return this.left;
    }

    HuffmanNode getRight() {
        return this.right;
    }

    int getBaseWeight() {
        return this.baseWeight;
    }

    void incrementBaseWeight() {
        if (null == this.leafValue) {
            throw new IllegalStateException("The base weight cannot be incremented on a non-leaf node");
        }
        baseWeight++;
    }

    public int getWeight() {
        int leftWeight = Optional.ofNullable(left)
                .map(HuffmanNode::getWeight)
                .orElse(0);
        int rightWeight = Optional.ofNullable(right)
                .map(HuffmanNode::getWeight)
                .orElse(0);
        return baseWeight + leftWeight + rightWeight;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return this.getWeight() - other.getWeight();
    }

    @Override
    public String toString() {
        return String.format("HuffmanNode(value=%s, weight=%d)",
                Optional.ofNullable(leafValue)
                        .map(Object::toString)
                        .orElse("N/A"),
                getWeight());
    }

    static HuffmanNode fromValue(Byte value) {
        HuffmanNode newNode = new HuffmanNode();
        newNode.leafValue = value;
        return newNode;
    }

    static HuffmanNode fromNodes(HuffmanNode right, HuffmanNode left) {
        HuffmanNode newNode = new HuffmanNode();
        newNode.left = left;
        newNode.right = right;
        return newNode;
    }

    byte seek(Iterator<Boolean> iterator) {
        if (null != leafValue) {
            return leafValue;
        }
        if (!iterator.hasNext()) {
            throw new RuntimeException("Tree seek failed because the bit stream ended unexpectedly");
        }
        if (iterator.next()) {
            return right.seek(iterator);
        } else {
            return left.seek(iterator);
        }
    }
}
