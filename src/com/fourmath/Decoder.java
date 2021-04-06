package com.fourmath;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Decoder {

    private HuffmanTree huffmanTree;
    private final byte[] input;
    private byte[] dataArr;
    private ArrayList<Byte> decompressedData = new ArrayList<>();
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
        byte headerInfo = input[0];
        // 0000 0011
        // 0000 0011 => 0x3
        int frequencySize = headerInfo & 0x3;
        // 0000 1000
        // 0000 0010
        int cutoff = headerInfo >> 2;
        int tableSize = ((input[4] & 0xff) << 24 |
                         (input[3] & 0xff) << 16 |
                         (input[2] & 0xff) << 8 |
                         (input[1] & 0xff));

        // Table reading
        int headerSize = 5 + tableSize * (frequencySize + 2);
        for (int i = 5; i < headerSize; i += frequencySize + 2) {
            byte byteData = input[i];
            int frequency = 0;
            for (int j = frequencySize; j >= 0; j--) {
                frequency |= ((input[i + j + 1] & 0xff) << (j * 8));
            }
            table.put(byteData, frequency);
        }

        dataArr = new byte[input.length - headerSize];
        for (int i = headerSize; i < input.length; i++) {
            dataArr[i - headerSize] = input[i];
        }

        huffmanTree = new HuffmanTree(table);

        int direction;
        int end = 0;

        HuffmanTree.Node node = huffmanTree.root;

        for (int i = 0; i < dataArr.length; i++) {

            if (i == dataArr.length - 1) {
               end = cutoff;
            }

            for (int j = 7; j >= end; j--) {
                // 1110 1100 1000
                // 0000 0001 >> 7 & 0x1 -> 1
                // 0000 0001 >> 6 & 0x1 -> 1
                // 0000 0001 >> 5 & 0x1 -> 1
                // 0000 0001 >> 4 & 0x1 -> 0

                direction = (dataArr[i] >> j) & 0x1;
                if (direction == 0) {
                    node = node.left;
                } else {
                    node = node.right;
                }

                if (node.right == null && node.left == null) {
                    decompressedData.add(node.byteData);
                    node = huffmanTree.root;
                }
            }
        }

    }

    public void writeDecompressedFile(String filename) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename.substring(0, filename.length() - 8)))) {
            for (byte chunk : decompressedData) {
                out.write(chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
