package com.fourmath;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Encoder {
    HuffmanTree huffmanTree;
    byte[] input;

    Encoder(byte[] input) {
        huffmanTree = new HuffmanTree(input);
        this.input = input;
    }

    private byte[] headerByteArray() {
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

    public void writeCompressedFile(String filename) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            byte[] data = huffmanTree.encode();
            byte[] header = headerByteArray();
            out.write(header);
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
