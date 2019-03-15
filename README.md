# Huffman File Compressor

A Huffman file compression utility designed for educational purposes.


## Build and Run

Install Oracle JDK or OpenJDK. Although this project is designed on OpenJDK 1.8, we highly recommend all developers
produce new code in the latest LTS version of Oracle JDK or OpenJDK, which is currently version 11.

Install [Apache Maven](https://maven.apache.org/) to build the project from source, or use a Maven-compatible IDE
[IntelliJ IDEA](https://www.jetbrains.com/idea/download/), [NetBeans](https://netbeans.apache.org/download/index.html),
[Eclipse](https://www.eclipse.org/downloads/), etc.). The instructions here will assume you are using Maven directly.

Build the application JAR file using Maven:
 1. Open a terminal or command line interface of your choice
 2. Navigate to where you've stored the contents of this repository, ex: `cd \home\admin\git\huffman-translator`
 3. Use Maven to build the JAR file `mvn clean package`
 4. Find the JAR file in your target directory and run it, ex: `java -jar target/huffman-translator.jar`


## Program Use and High-level Theory

There are two modes to this program, compression and decompression. Compression mode will analyze the contents of a
file, determining the frequency of each byte that comprises it. Using a Huffman Tree, these bytes are organized into
a structure that translates the most frequent bytes into shorter sequences of bits (like 01), whereas the less-frequent
bytes become longer sequences (like 001110101). The data in the chosen file is translated into these alternative bit
sequences which are streamed into a file along with the tree data itself.

Decompression mode will extract the Huffman Tree structure out of a compressed file, and will decode the bit stream back
into its original bytes.

Most files today already employ methods of compression out-of-box, which makes this primitive compression method rather
ineffective. The reasoning behind this is that compressed files contain less redundancy. That is, the actual information
is much more dense, which tends to produce a highly even distribution of bytes. Applying a Huffman-style compression to
a file within an even byte distribution may slightly reduce its size; however, once the tree structure is added to the
data, it actually causes the file to get larger.

Huffman compression is very effective at compressing written text. For example, an [ASCII text document of the US
Constitution](https://www.usconstitution.net/const.txt) is about 44.0 KiB. When compressed (including the added bytes
for the tree descriptor) it is about 25.0 KiB.


## Architecture

This section will be filled out at a later date.
