package com.bitfracture.huffman;

class SerialHuffmanNode {
    private SerialNodeType type;
    private Byte nodeValue;

    private SerialHuffmanNode(SerialNodeType type, Byte value) {
        this.type = type;
        this.nodeValue = value;
    }

    Byte getValue() {
        return this.nodeValue;
    }

    SerialNodeType getType() {
        return this.type;
    }

    static SerialHuffmanNode ofValue(byte value) {
        return new SerialHuffmanNode(SerialNodeType.VALUE, value);
    }

    static SerialHuffmanNode ofBranch() {
        return new SerialHuffmanNode(SerialNodeType.BRANCH, null);
    }

    enum SerialNodeType {
        BRANCH,
        VALUE
    }
}
