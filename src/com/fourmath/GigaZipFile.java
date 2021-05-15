package com.fourmath;

import java.io.*;
import java.nio.file.Files;

enum CompressionType {
    Huffman,
    LZ77,
    Deflate
}

interface Compressor {
    byte[] encode(byte[] input);
    byte[] decode(byte[] input);
}

public class GigaZipFile {

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
            System.out.printf("Original file size: %d B | %.3f KB | %.3f MB%n", file.length(),
                    file.length() / 1e3, file.length() / 1e6);
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
            System.out.printf("Original file size: %d B | %.3f KB | %.3f MB%n", file.length(),
                    file.length() / 1e3, file.length() / 1e6);
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
            System.out.printf("Compressed file size: %d B | %.3f KB | %.3f MB%n", size,
                    (double) size / 1e3, (double) size / 1e6);
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

    private void decompress(String filename, byte[] input, Compressor compressor)  {
        long start = System.nanoTime();
        byte[] result = compressor.decode(input);
        long end = System.nanoTime();
        long time = (end - start);
        int size = result.length;
        if (verbose) {
            System.out.println("Decoding time: " + time / 1e6 + "ms");
            System.out.printf("Decompressed file size: %d B | %.3f KB | %.3f MB%n", size,
                    (float) size / 1e3, (float) size / 1e6);
            double timeSec = time / 1e9;
            System.out.printf("Decompression speed: %.2f MB/s%n", (size / 1e6) / timeSec);
        }
        try (DataOutputStream out = new DataOutputStream( new FileOutputStream(filename))) {
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
        switch(words[words.length - 1]) {
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
