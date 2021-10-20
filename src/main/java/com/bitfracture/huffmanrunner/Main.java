package com.bitfracture.huffmanrunner;

import com.bitfracture.huffman.HuffmanTranslator;
import com.bitfracture.huffman.HuffmanTree;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);

        //Determine if we are compressing or decompressing a file today
        System.out.print("Would you like to [c]ompress or [d]ecompress a file? ");
        Boolean isCompressing = null;
        while (Objects.isNull(isCompressing)) {
            String choiceText = keyboard.nextLine().toLowerCase();
            if ("compress".startsWith(choiceText)) {
                isCompressing = true;
            } else if ("decompress".startsWith(choiceText)) {
                isCompressing = false;
            } else {
                System.out.print("Please choose [c]ompress or [d]ecompress: ");
            }
        }

        //Call the compress or decompress mains, provide a friendly response if an exception occurs
        try {
            if (isCompressing) {
                compress(keyboard);
            } else {
                decompress(keyboard);
            }
        } catch (Exception e) {
            System.out.println(String.format("The operation failed! \nException type: %s\nMessage: %s",
                    e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    private static void compress(Scanner keyboard) throws IOException {
        //Get an input file name to compress
        System.out.print("Type or drop a file path to compress: ");
        String inputFilePath = keyboard.nextLine();
        String outputFilePath = inputFilePath + ".huff";

        //Create a Huffman tree from the data in the entire file (most optimal, also most time consuming)
        System.out.println("Generating a Huffman Tree from the entire input file (This may take some time)");
        InputStream rawFileIn = new FileInputStream(inputFilePath);
        HuffmanTree encodingTree = HuffmanTranslator.generateTree(rawFileIn);
        rawFileIn.close();

        //Translate the raw file using the huffman translator
        System.out.println("Compressing the file");
        rawFileIn = new FileInputStream(inputFilePath);
        OutputStream encodedFileOut = new FileOutputStream(outputFilePath);
        HuffmanTranslator.encode(encodingTree, rawFileIn, encodedFileOut);
        rawFileIn.close();
        encodedFileOut.close();
        System.out.println(String.format("File saved to \"%s\"", outputFilePath));
    }

    private static void decompress(Scanner keyboard) throws IOException {
        //Get an input file name to compress
        System.out.print("Type or drop a file path to decompress: ");
        String inputFilePath = keyboard.nextLine();
        String outputFilePath = inputFilePath + ".raw";
        if (inputFilePath.endsWith(".huff")) {
            //If this file has a .huff, we probably added it (see compress(...)) so let's just remove it
            outputFilePath = inputFilePath.replaceAll("\\.huff\\z", "");
        }

        //Decode the file using the huffman translator
        OutputStream decodeStream = new FileOutputStream("decode.txt");
        HuffmanTranslator.decode(new FileInputStream(outputFilePath), decodeStream);
    }
}
