package com.bitfracture.huffmanrunner;

import com.bitfracture.huffman.HuffmanTranslator;
import com.bitfracture.huffman.HuffmanTree;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        //Get an input file name to compress
        Scanner scanner = new Scanner(System.in);
        System.out.print("Type or drop a file to compress: ");
        String inputFilePath = scanner.nextLine();
        String outputFilePath = inputFilePath + ".huff";

        //Create a Huffman tree from the data in the entire file (most optimal, also most time consuming)
        InputStream rawFileIn = new FileInputStream(inputFilePath);
        HuffmanTree encodingTree = HuffmanTranslator.generateTree(rawFileIn);
        rawFileIn.close();

        //Translate the raw file using the huffman translator
        rawFileIn = new FileInputStream(inputFilePath);
        OutputStream encodedFileOut = new FileOutputStream(outputFilePath);
        HuffmanTranslator.encode(encodingTree, rawFileIn, encodedFileOut);
        rawFileIn.close();
        encodedFileOut.close();

        //Decode the file using the huffman translator
        OutputStream decodeStream = new FileOutputStream("decode.txt");
        HuffmanTranslator.decode(new FileInputStream(outputFilePath), decodeStream);
    }
}
