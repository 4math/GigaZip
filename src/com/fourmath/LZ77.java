package com.fourmath;

import java.io.IOException;
import java.util.ArrayList;

public class LZ77 {

    public int lookAheadBufferSize = (1 << 8) - 1; // 255 max ??
    public int searchBufferSize = (1 << 16) - 1; // max 65536 ??
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
        // _  _ _ _ _ _ _ _ _  _ _ _ _ _ _ _ _  _ _ _ _ _ _ _ _ <- 3 bytes + 1 bit
        // ^  _______________  ________________________________
        // info bit (1)     ^                                 ^
        //                  length (8)                        distance (16)

        byte[] reference = {0, 0, 0};
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

    public byte[] encode(byte[] input) {

        SuffixArray sa = new SuffixArray(input, lookAheadBufferSize, searchBufferSize);
        int cursor = 0;

        while (cursor < input.length) {
            int bestMatchLength;
            int bestOffset;

            if (cursor < input.length - 2) {
                int[] data = sa.searchBestMatch(cursor);
                bestMatchLength = data[0];
                bestOffset = data[1];
                if (bestOffset == -1) {
                    bestMatchLength = 0;
                    bestOffset = 0;
                } else {
                    if (bestMatchLength > lookAheadBufferSize) {
                        bestMatchLength = lookAheadBufferSize;
                    }
                    bestOffset = cursor - data[1];
                    if (bestOffset > searchBufferSize) {
                        bestOffset = 0;
                        bestMatchLength = 0;
                    }
                }
            }
            else {
                bestOffset = 0;
                bestMatchLength = 0;
            }

            if (bestMatchLength > 3) {
                pushReference(bestOffset, bestMatchLength);
                cursor += bestMatchLength;
            } else {
                pushData(input[cursor]);
                cursor++;
            }
        }


        return copyArray(output);
    }

    public byte[] decode(byte[] input) {

        k = 0;
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

                byte[] rawReference = {input[++idx], input[++idx], input[++idx]};
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
