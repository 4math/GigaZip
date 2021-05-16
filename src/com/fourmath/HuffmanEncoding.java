package com.fourmath;

import java.util.ArrayList;
import java.util.HashMap;

public class HuffmanEncoding implements Compressor {

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
            headerByteArray[ptr++] = (byte)(tableSize / (tableFrequencySize + 1) >>> (i * 8));
        }

        for (byte b: tableArray) {
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
