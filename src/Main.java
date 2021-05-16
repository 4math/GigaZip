import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        CompressionType cType = CompressionType.Deflate;
        boolean verbose = false;
        GigaZipFile file;

        if (args.length != 0 && args[0].equals("-huffman")) {
            cType = CompressionType.Huffman;
        } else if (args.length != 0 && args[0].equals("-lz77")) {
            cType = CompressionType.LZ77;
        } else if (args.length != 0 && args[0].equals("-verbose")) {
            verbose = true;
        }

        file = new GigaZipFile(cType, verbose);

        outer: while (true) {
            Scanner sc = new Scanner(System.in);

            if (verbose) {
                System.out.println("\nWhat to do?");
                System.out.println("comp: compress file(Algorithm depends on flag, default is Deflate)");
                System.out.println(
                        "decomp: decompress file(Algorithm depends on flag, default is Deflate with .gigazip extension)");
                System.out.println("size: prints size of the given file in bytes");
                System.out.println("equal: compares two files content");
                System.out.println("about: prints information about authors");
                System.out.println("exit: exits the program");
            }

            String choice = sc.nextLine();

            switch (choice) {
                case "comp" -> {
                    System.out.print("source file name: ");
                    String input = sc.nextLine();
                    System.out.print("archive name: ");
                    String output = sc.nextLine();
                    try {
                        file.compress(input, output);
                    } catch (IOException e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "decomp" -> {
                    System.out.print("archive name: ");
                    String input = sc.nextLine();
                    System.out.print("file name: ");
                    String output = sc.nextLine();
                    try {
                        file.decompress(input, output);
                    } catch (IOException e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "size" -> {
                    System.out.print("file name: ");
                    String filename = sc.nextLine();
                    try {
                        File f = new File(filename);
                        if (verbose && !f.exists())
                            throw new Exception("File does not exist!");
                        System.out.println("size: " + f.length() + " B");
                    } catch (Exception e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "equal" -> {
                    System.out.print("first file name: ");
                    String first = sc.nextLine();
                    System.out.print("second file name: ");
                    String second = sc.nextLine();
                    try {
                        File f1 = new File(first);
                        if (verbose && !f1.exists())
                            throw new Exception("First file does not exist!");
                        File f2 = new File(second);
                        if (verbose && !f2.exists())
                            throw new Exception("Second file does not exist!");
                        boolean isEqual = sameContent(f1, f2);
                        System.out.println(isEqual);
                    } catch (Exception e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "about" -> {
                }
                case "exit" -> {
                    break outer;
                }
                default -> {
                    if (verbose)
                        System.out.println("wrong command");
                }
            }
        }
    }

    private static boolean sameContent(File f1, File f2) throws IOException {
        if (f1.length() != f2.length())
            return false;
        return Arrays.equals(Files.readAllBytes(f1.toPath()), Files.readAllBytes(f2.toPath()));
    }
}

class DeflateEncoding implements Compressor {

    @Override
    public byte[] encode(byte[] input) {
        HuffmanEncoding he = new HuffmanEncoding();
        LZ77 lz77 = new LZ77();

        byte[] result = lz77.encode(input);
        result = he.encode(result);

        return result;
    }

    @Override
    public byte[] decode(byte[] input) {
        HuffmanEncoding he = new HuffmanEncoding();
        LZ77 lz77 = new LZ77();

        byte[] result = he.decode(input);
        result = lz77.decode(result);

        return result;
    }
}

enum CompressionType {
    Huffman, LZ77, Deflate
}

interface Compressor {
    byte[] encode(byte[] input);

    byte[] decode(byte[] input);
}

class GigaZipFile {

    private final CompressionType compressionType;
    private final boolean verbose;

    GigaZipFile(CompressionType cType, boolean verbose) {
        compressionType = cType;
        this.verbose = verbose;
    }

    public void compress(String inputFilename, String outputFilename) throws IOException {
        File file = new File(inputFilename);
        byte[] input = Files.readAllBytes(file.toPath());
        if (verbose) {
            System.out.printf("Original file size: %d B | %.3f KB | %.3f MB%n", file.length(), file.length() / 1e3,
                    file.length() / 1e6);
        }

        switch (compressionType) {
            case LZ77 -> {
                Compressor comp = new LZ77();
                compress(outputFilename, input, comp);
            }
            case Huffman -> {
                Compressor comp = new HuffmanEncoding();
                compress(outputFilename, input, comp);
            }
            case Deflate -> {
                Compressor comp = new DeflateEncoding();
                compress(outputFilename, input, comp);
            }
        }
    }

    public void decompress(String inputFilename, String outputFilename) throws IOException {
        File file = new File(inputFilename);

        if (verbose && !inputFilename.endsWith(compExtension())) {
            throw new IOException("Incorrect file format! " + maybe(inputFilename));
        }

        byte[] input = Files.readAllBytes(file.toPath());

        if (verbose) {
            System.out.printf("Original file size: %d B | %.3f KB | %.3f MB%n", file.length(), file.length() / 1e3,
                    file.length() / 1e6);
        }

        switch (compressionType) {
            case LZ77 -> {
                Compressor comp = new LZ77();
                decompress(outputFilename, input, comp);
            }
            case Huffman -> {
                Compressor comp = new HuffmanEncoding();
                decompress(outputFilename, input, comp);
            }
            case Deflate -> {
                Compressor comp = new DeflateEncoding();
                decompress(outputFilename, input, comp);
            }
        }
    }

    private void compress(String filename, byte[] input, Compressor compressor) {
        long start = System.nanoTime();
        byte[] result = compressor.encode(input);
        long end = System.nanoTime();
        long time = (end - start);
        int size = result.length;
        if (verbose) {
            System.out.println("Compression time: " + time / 1e6 + "ms");
            System.out.printf("Compressed file size: %d B | %.3f KB | %.3f MB%n", size, (double) size / 1e3,
                    (double) size / 1e6);
            double timeSec = time / 1e9;
            System.out.printf("Compression speed: %.2f MB/s%n", (size / 1e6) / timeSec);
            System.out.printf("Compression ratio: %.2f %%%n", ((input.length - size) / (double) input.length) * 100);
        }

        try (DataOutputStream out = new DataOutputStream(
                new FileOutputStream(verbose ? filename + compExtension() : filename))) {
            out.write(result);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private void decompress(String filename, byte[] input, Compressor compressor) {
        long start = System.nanoTime();
        byte[] result = compressor.decode(input);
        long end = System.nanoTime();
        long time = (end - start);
        int size = result.length;
        if (verbose) {
            System.out.println("Decoding time: " + time / 1e6 + "ms");
            System.out.printf("Decompressed file size: %d B | %.3f KB | %.3f MB%n", size, (float) size / 1e3,
                    (float) size / 1e6);
            double timeSec = time / 1e9;
            System.out.printf("Decompression speed: %.2f MB/s%n", (size / 1e6) / timeSec);
        }
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            out.write(result);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private String compExtension() {
        return switch (compressionType) {
            case LZ77 -> ".lz";
            case Huffman -> ".huff";
            case Deflate -> ".gigazip";
        };
    }

    private String maybe(String filename) {
        String[] words = filename.split("\\.");
        StringBuilder sb = new StringBuilder();
        sb.append("Maybe you meant to use: ");
        switch (words[words.length - 1]) {
            case "lz" -> sb.append("\"-lz\" flag?");
            case "huff" -> sb.append("\"-huffman\" flag?");
            case "gigazip" -> sb.append("\"-deflate\" flag?");
            default -> {
                sb.setLength(0);
                sb.append("None of decompression options are available");
            }
        }
        return sb.toString();
    }

}

class HuffmanEncoding implements Compressor {

    public byte[] encode(byte[] input) {
        HuffmanTree huffmanTree = new HuffmanTree(input);
        byte[] data = huffmanTree.encode();
        byte[] header = headerByteArray(huffmanTree);

        byte[] result = new byte[header.length + data.length];

        for (int i = 0; i < header.length; i++) {
            result[i] = header[i];
        }

        for (int i = 0; i < data.length; i++) {
            result[header.length + i] = data[i];
        }

        return result;
    }

    public byte[] decode(byte[] input) {
        ArrayList<Byte> decompressedData = new ArrayList<>(input.length);
        HashMap<Byte, Integer> table = new HashMap<>(); // frequency table of bytes

        byte headerPreamble = input[0];
        // 0000 0011
        // 0000 0011 => 0x3
        // Size can be either 0, or 1, or 3, meaning 1, 2 or 4 byte size for frequency
        // integer
        int frequencySize = headerPreamble & 0x3;
        // 0000 1000
        // 0000 0010
        // We shift to get the number of zeros at the last byte which should not be read
        // as data
        // Cutoff is needed, since compressed data does not fit into one byte exactly
        int cutOff = headerPreamble >> 2;
        int tableSize = ((input[4] & 0xff) << 24 | (input[3] & 0xff) << 16 | (input[2] & 0xff) << 8
                | (input[1] & 0xff));

        // Reading table for decoding
        // First five bytes are used for header size and on 6-th byte starts table for
        // decoding
        // Therefore we add 5 to headerSize to include preamble.
        // The other part of addition describes how much entries table has at all.
        // For example
        // 11 BE 00 00 00 | 09 D6 00 | 0A 60 01
        // 11 in first byte says we have frequencySize = 1 => 2 bytes to store frequency
        // Since on every entry we should include one coding for byte, e.g. 09 and
        // frequencySize + 1
        // Then size becomes as it is
        int headerSize = 5 + tableSize * (frequencySize + 2);

        for (int i = 5; i < headerSize; i += frequencySize + 2) {
            byte byteData = input[i];
            int frequency = 0;
            for (int j = frequencySize; j >= 0; j--) {
                // frequency cannot be negative, since Java does not support unsigned int,
                // it is needed to convert byte to int
                frequency |= ((input[i + j + 1] & 0xff) << (j * 8));
            }
            table.put(byteData, frequency);
        }

        // Reading raw data after table
        byte[] dataArr = new byte[input.length - headerSize];
        for (int i = headerSize; i < input.length; i++) {
            dataArr[i - headerSize] = input[i];
        }

        // Dynamically creating huffmanTree to decode data
        HuffmanTree huffmanTree = new HuffmanTree(table);

        // Shows to which child goes extracted bit
        int direction;
        // Is needed to control the last byte, which might not be fitted into the whole
        // byte
        int end = 0;

        HuffmanTree.Node node = huffmanTree.root;

        for (int i = 0; i < dataArr.length; i++) {

            if (i == dataArr.length - 1) {
                end = cutOff;
            }

            for (int j = 7; j >= end; j--) {
                // 1110 1100 1000
                // 0000 0001 >> 7 & 0x1 -> 1
                // 0000 0001 >> 6 & 0x1 -> 1
                // 0000 0001 >> 5 & 0x1 -> 1
                // 0000 0000 >> 4 & 0x1 -> 0

                direction = (dataArr[i] >> j) & 0x1;
                if (direction == 0) {
                    node = node.left;
                } else {
                    node = node.right;
                }

                assert node != null;
                if (node.right == null && node.left == null) {
                    decompressedData.add(node.byteData);
                    node = huffmanTree.root;
                }
            }
        }

        return copyArray(decompressedData);
    }

    private byte[] headerByteArray(HuffmanTree huffmanTree) {
        byte cutoff = huffmanTree.cutoff.byteValue();

        byte tableFrequencySize;
        if (huffmanTree.maxFrequency < 1 << 8) {
            tableFrequencySize = 1;
        } else if (huffmanTree.maxFrequency < 1 << 16) {
            tableFrequencySize = 2;
        } else {
            tableFrequencySize = 4;
        }

        byte[] tableArray = huffmanTree.tableToByteArray(tableFrequencySize);

        int preTableSize = 5;
        int tableSize = tableArray.length;
        int headerSize = preTableSize + tableSize;

        byte[] headerByteArray = new byte[headerSize];
        int ptr = 0;

        // 0000 0000
        // cutoff = 2
        // 0000 0010
        byte cutoffAndFrequencySize = cutoff;
        // 0000 1000
        cutoffAndFrequencySize <<= 2;
        // frequency = 4 => 3
        // 0000 1011
        cutoffAndFrequencySize |= tableFrequencySize - 1;

        headerByteArray[ptr++] = cutoffAndFrequencySize;

        for (int i = 0; i < 4; i++) {
            headerByteArray[ptr++] = (byte) (tableSize / (tableFrequencySize + 1) >>> (i * 8));
        }

        for (byte b : tableArray) {
            headerByteArray[ptr++] = b;
        }

        return headerByteArray;
    }

    private byte[] copyArray(ArrayList<Byte> arr) {
        byte[] copy = new byte[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            copy[i] = arr.get(i);
        }
        return copy;
    }
}

class HuffmanTree {
    Node root = null;
    HashMap<Byte, String> table = new HashMap<>();
    HashMap<Byte, Integer> frequency = new HashMap<>();
    byte[] input;
    Integer cutoff = 0;
    Integer maxFrequency = Integer.MIN_VALUE;

    HuffmanTree(byte[] input) {
        this.input = input;
        calculateFrequency();
        createTree();
        createTable();
    }

    HuffmanTree(HashMap<Byte, Integer> frequency) {
        this.frequency = frequency;
        createTree();
        createTable();
    }

    public byte[] encode() {
        StringBuilder sb = new StringBuilder();

        for (byte key : input) {
            sb.append(table.get(key));
        }

        if (sb.length() % 8 != 0) {
            cutoff = 8 - sb.length() % 8;
            sb.append("0".repeat(cutoff));
        }

        int size = sb.length() / 8;
        byte[] byteArr = new byte[size];

        for (int i = 0; i < size; i++) {
            int b = Integer.parseInt(sb.substring(i * 8, (i + 1) * 8), 2);
            byteArr[i] = (byte) b;
        }

        return byteArr;
    }

    void calculateFrequency() {
        for (byte key : input) {
            if (frequency.containsKey(key)) {
                frequency.put(key, frequency.get(key) + 1);
            } else {
                frequency.put(key, 1);
            }
            maxFrequency = Math.max(maxFrequency, frequency.get(key));
        }
    }

    void createTree() {
        TreeSet<Node> treeSet = new TreeSet<>();

        int nodeId = 0;

        for (byte key : frequency.keySet()) {
            treeSet.add(new Node(key, frequency.get(key), nodeId++));
        }

        while (treeSet.size() != 1) {
            Node right = treeSet.pollFirst();
            Node left = treeSet.pollFirst();
            Node localRoot = new Node(right.weight + left.weight, right, left, nodeId++);
            treeSet.add(localRoot);
        }

        root = treeSet.pollFirst();
    }

    void createTable() {
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();

            if (node.right == null && node.left == null) {
                table.put(node.byteData, node.binaryView);
            }

            if (node.left != null) {
                queue.add(node.left);
                node.left.binaryView = node.binaryView + "0";
            }

            if (node.right != null) {
                queue.add(node.right);
                node.right.binaryView = node.binaryView + "1";
            }
        }

//        System.out.println(table);
    }

    byte[] tableToByteArray(byte frequencySize) {
        byte[] tableByteArray = new byte[frequency.size() * (1 + frequencySize)];

        int ptr = 0;
        for (byte key : frequency.keySet()) {
            tableByteArray[ptr++] = key;

            if (frequencySize == 1) {
                byte amount = frequency.get(key).byteValue();
                tableByteArray[ptr++] = amount;
            } else if (frequencySize == 2) {
                short amount = frequency.get(key).shortValue();
                tableByteArray[ptr++] = (byte) (amount & 0xff);
                tableByteArray[ptr++] = (byte) ((amount >> 8) & 0xff);
            } else if (frequencySize == 4) {
                int amount = frequency.get(key);
                tableByteArray[ptr++] = (byte) (amount & 0xff);
                tableByteArray[ptr++] = (byte) ((amount >> 8) & 0xff);
                tableByteArray[ptr++] = (byte) ((amount >> 16) & 0xff);
                tableByteArray[ptr++] = (byte) ((amount >> 24) & 0xff);
            }
        }

        return tableByteArray;
    }

    static class Node implements Comparable<Node> {
        byte byteData;
        int weight;
        int id;
        Node left, right;
        String binaryView = "";

        Node(byte byteData, int weight, int id) {
            this.byteData = byteData;
            this.weight = weight;
            this.left = null;
            this.right = null;
            this.id = id;
        }

        Node(int weight, Node left, Node right, int id) {
            this.byteData = 0;
            this.weight = weight;
            this.left = left;
            this.right = right;
            this.id = id;
        }

        @Override
        public int compareTo(Node node) {
            int x = weight - node.weight;
            if (x != 0)
                return x;

            x = byteData - node.byteData;
            if (x != 0)
                return x;

            return id - node.id;
        }
    }
}

class LZ77 implements Compressor {

    public int lookAheadBufferSize = (1 << 8) - 1; // max 255
    public int searchBufferSize = (1 << 16); // max 65536
    ArrayList<Byte> output = new ArrayList<>(10 * 1000 * 1000);
    private byte carry = 0;
    private int k = 0;

    private void pushData(byte data) {
        // _ _ _ _ _ _ _ _ _
        // ^ _______________ <- data
        // info bit
        k = (++k % 8);
        if (k == 0) {
            output.add(carry);
            output.add(data);
            carry = 0;
            return;
        }

        byte b = carry;
        b |= ((data & 0xff) >> k);
        carry = data;
        carry <<= 8 - k;

        output.add(b);
    }

    private void pushReference(int distance, int length) {
        // _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ <- 3 bytes + 1 bit
        // ^ _______________ ________________________________
        // info bit (1) ^ ^
        // length (8) distance (16)

        byte[] reference = { 0, 0, 0 };
        reference[0] = (byte) (length & 0xff);
        reference[1] = (byte) ((distance >> 8) & 0xff);
        reference[2] = (byte) (distance & 0xff);

        carry |= 0x1 << (7 - k);
        k = (++k % 8);
        if (k == 0) {
            output.add(carry);
            for (int i = 0; i < 3; i++)
                output.add(reference[i]);
            carry = 0;
            return;
        }

        for (int i = 0; i < 3; i++) {
            byte b = carry;
            b |= ((reference[i] & 0xff) >> k);
            carry = reference[i];
            carry <<= 8 - k;
            output.add(b);
        }
    }

    @Override
    public byte[] encode(byte[] input) {

        SuffixArray sa = new SuffixArray(input, lookAheadBufferSize, searchBufferSize);

        int cursor = 0;

        while (cursor < input.length) {
            int bestMatchLength = 0;
            int bestOffset = 0;

            if (cursor < input.length - 2) {
                int[] data = sa.searchBestMatch(cursor);
                bestMatchLength = data[0];
                bestOffset = data[1];
                if (bestOffset == -1) {
                    bestMatchLength = 0;
                    bestOffset = 0;
                } else {
                    bestOffset = cursor - data[1];
                }
            }

            if (bestMatchLength > 3) {
                pushReference(bestOffset, bestMatchLength);
                cursor += bestMatchLength;
            } else {
                pushData(input[cursor]);
                cursor++;
            }
        }

        output.add(carry);
        return copyArray(output);
    }

    @Override
    public byte[] decode(byte[] input) {

        int k = 0;
        int idx = 0;
        int outputIdx = 0;
        byte b = input[idx];

        while (true) {
            byte infoBit = (byte) ((b >> (8 - ++k)) & 0x1);
            k %= 8;
            if (k == 0) {
                if (idx + 1 >= input.length)
                    break;

                b = input[++idx];
            }
            if (infoBit == 0x0) {
                if (idx + 1 >= input.length)
                    break;

                byte nextB = input[++idx];
                byte dataByte = (byte) ((b & 0xff) << k);
                dataByte |= (nextB & 0xff) >> (8 - k);
                b = nextB;

                output.add(dataByte);
                outputIdx++;
            } else {
                if (idx + 3 >= input.length)
                    break;

                byte[] rawReference = { input[++idx], input[++idx], input[++idx] };
                byte[] reference = new byte[3];

                for (int i = 0; i < 3; i++) {
                    reference[i] = (byte) (b << k);
                    reference[i] |= (rawReference[i] & 0xff) >> (8 - k);
                    b = rawReference[i];
                }

                int length = reference[0] & 0xff;
                int distance = (reference[1] & 0xff) << 8 | (reference[2] & 0xff);

                for (int i = 0; i < length; i++) {
                    output.add(output.get(outputIdx - distance + i));
                }

                outputIdx += length;
            }
        }

        return copyArray(output);
    }

    private byte[] copyArray(ArrayList<Byte> arr) {
        byte[] copy = new byte[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            copy[i] = arr.get(i);
        }
        return copy;
    }
}

//At first you must create a suffix array from the file
//Example:
//SuffixArray suffixArray = new SuffixArray(input, (1 << 8) - 1, (1 << 16) - 1);
//                                          ^       ^             ^
//                                      byte file;  LAB size;     search buffer size
//
//then, you can use searchBestMatch(int start) method, where "start" is the pointer to the start of LAB
//
//searchBestMatch(int start) method will return you an array:
//{bestMatchLength, bestOffset}
//if bestMatchLength == -1 or bestOffset == -1 then no match found for the current start of LAB => cursor++;

class SuffixArray {

    ArrayList<Integer>[][][] sa;
    byte[] bFile;
    int lookAheadBufferSize;
    int searchBufferSize;

    SuffixArray(byte[] bFile, int lookAheadBufferSize, int searchBufferSize) {
        this.bFile = bFile;
        this.lookAheadBufferSize = lookAheadBufferSize;
        this.searchBufferSize = searchBufferSize;
        sa = new ArrayList[256][256][256];
        for (int i = 0; i < bFile.length - 2; i++) {
            if (sa[bFile[i] & 0xff][bFile[i + 1] & 0xff][bFile[i + 2] & 0xff] == null) {
                sa[bFile[i] & 0xff][bFile[i + 1] & 0xff][bFile[i + 2] & 0xff] = new ArrayList<>();
            }
            sa[bFile[i] & 0xff][bFile[i + 1] & 0xff][bFile[i + 2] & 0xff].add(i);
        }
    }

    public static int binarySearch(ArrayList<Integer> arr, int x) {
        int l = 0, r = arr.size() - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;
            if (arr.get(m) == x)
                return m;
            if (arr.get(m) < x)
                l = m + 1;
            else
                r = m - 1;
        }
        return -1;
    }

    public ArrayList<Integer> searchIndexes(int start) {
        return sa[bFile[start] & 0xff][bFile[start + 1] & 0xff][bFile[start + 2] & 0xff];
    }

    /**
     * @return matchLen, offset
     */
    public int[] searchBestMatch(int start) {
        ArrayList<Integer> arrayOfIndexes = searchIndexes(start);
        CustomLinkedList listOfIndexes = new CustomLinkedList();
        Node ptr;

        int pos = binarySearch(arrayOfIndexes, start);
        if (pos == 0) {
            return new int[] { -1, -1 };
        } else {
            pos--;
        }

        for (int i = pos; i > -1; i--) {
            if (start - arrayOfIndexes.get(i) < searchBufferSize) {
                listOfIndexes.head.setNext(new Node(arrayOfIndexes.get(i), listOfIndexes.head.next));
                listOfIndexes.size++;
            } else {
                break;
            }
        }

        int len = 3;
        loop: for (; len < lookAheadBufferSize; len++) {
            ptr = listOfIndexes.head;
            int howMany = listOfIndexes.size;
            for (int i = 0; i < howMany; i++) {
                if (start + len >= bFile.length || ptr.next.index + len >= bFile.length || ptr.next.index + len >= start
                        || bFile[ptr.next.index + len] != bFile[start + len]) {
                    if (listOfIndexes.size == 1) {
                        break loop;
                    }
                    ptr.setNext(ptr.next.next);
                    listOfIndexes.size--;
                    continue;
                }
                ptr = ptr.next;
            }
        }
        int[] answer = new int[2];
        answer[0] = len;
        answer[1] = listOfIndexes.head.next.index;
        return answer;
    }

}

class CustomLinkedList {

    Node head;
    Node tail;
    int size;

    public CustomLinkedList() {
        head = new Node(-1);
        tail = new Node(-1);
        size = 0;
        head.setNext(tail);
    }

}

class Node {

    int index;
    Node next;

    public Node(int indx) {
        next = null;
        this.index = indx;
    }

    public Node(int indx, Node next) {
        this.index = indx;
        this.next = next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

}
