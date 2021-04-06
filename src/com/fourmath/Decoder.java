package com.fourmath;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Decoder {

    private final byte[] input;
    private final ArrayList<Byte> decompressedData = new ArrayList<>();
    private final HashMap<Byte, Integer> table = new HashMap<>();

    Decoder(byte[] input) {
        this.input = input;
        try {
            init();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void init() {
        byte headerPreamble = input[0];
        // 0000 0011
        // 0000 0011 => 0x3
        // Size can be either 0, or 1, or 3, meaning 1, 2 or 4 byte size for frequency integer
        int frequencySize = headerPreamble & 0x3;
        // 0000 1000
        // 0000 0010
        // We shift to get the number of zeros at the last byte which should not be read as data
        // Cutoff is needed, since compressed data does not fit into one byte exactly
        int cutOff = headerPreamble >> 2;
        int tableSize = ((input[4] & 0xff) << 24 |
                         (input[3] & 0xff) << 16 |
                         (input[2] & 0xff) << 8 |
                         (input[1] & 0xff));

        // Reading table for decoding
        // First five bytes are used for header size and on 6-th byte starts table for decoding
        // Therefore we add 5 to headerSize to include preamble.
        // The other part of addition describes how much entries table has at all.
        // For example
        // 11 BE 00 00 00 | 09 D6 00 | 0A 60 01
        // 11 in first byte says we have frequencySize = 1 => 2 bytes to store frequency
        // Since on every entry we should include one coding for byte, e.g. 09 and frequencySize + 1
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
        // Is needed to control the last byte, which might not be fitted into the whole byte
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

    }

    public void writeDecompressedFile(String filename) {
        File decompressedFile = new File(filename.substring(0, filename.length() - 8));
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(decompressedFile))) {
            for (byte chunk : decompressedData) {
                out.write(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
