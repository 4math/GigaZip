package com.fourmath;

import java.util.ArrayList;

public class LZ77 {

    public byte[] encode(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>();

        int uniqueElementPtr = -1;
        int uniqueElementCount = 0;

        int lookAheadBufferSize = 15;
        int searchBufferSize = 8192;

        int cursor = 0;

        int lookAheadBufferEndPtr = lookAheadBufferSize;
        int searchBufferStartPtr = -searchBufferSize;

        int matchLength;
        int offset;

        while (cursor < input.length) {

            // Searching for the longest match prefix
            offset = 0;
            int bestMatchLength = 0;
            int bestOffset = searchBufferSize + 1;

            for (int i = cursor - 1; i >= searchBufferStartPtr && i >= 0; i--) {
                int lookAheadMatchIdx = cursor;

                if (input[i] == input[lookAheadMatchIdx]) {
                    matchLength = 0;
                    offset = cursor - i;

                    for (int searchMatchIdx = i;
                         searchMatchIdx < cursor &&
                                 lookAheadMatchIdx < lookAheadBufferEndPtr &&
                                 lookAheadMatchIdx < input.length &&
                                 input[searchMatchIdx] == input[lookAheadMatchIdx]; searchMatchIdx++) {
                        lookAheadMatchIdx++;
                        matchLength++;
                    }

                    if (matchLength > bestMatchLength) {
                        bestMatchLength = matchLength;
                        bestOffset = offset;
                    }

                }
            }

            // TODO: bit boundaries check
            if (bestMatchLength >= 3 && bestMatchLength <= 8) {
                // first byte contains information about match length and offset, second about offset only
                // M2 - M0 O12 - O8   O7 - O0
                // Match have all values, excluding 000 and 111, since they are reserved for other references
                byte matchOffsetByte = (byte) ((bestMatchLength - 2) << 5);
                matchOffsetByte |= (byte) (bestOffset >> 8);
                output.add(matchOffsetByte);

                byte offsetByte = (byte) (bestOffset - 1);
                output.add(offsetByte);

                // -1 so that the next symbol won't be skipped by window
                cursor += bestMatchLength - 1;
                lookAheadBufferEndPtr += bestMatchLength - 1;
                searchBufferStartPtr += bestMatchLength - 1;
                uniqueElementCount = 0;
                uniqueElementPtr = -1;

            } else if (bestMatchLength > 8) {
                byte firstByte = (byte) 0xe0; // 111 at the start
                firstByte |= (byte) ((bestMatchLength - 1) >> 3);
                output.add(firstByte);

                byte secondByte = (byte) (((bestMatchLength - 1) & 0x7) << 5); // 0x7 = 0000 0111
                secondByte |= (byte) (bestOffset >> 8);
                output.add(secondByte);

                byte thirdByte = (byte) (bestOffset - 1);
                output.add(thirdByte);

                cursor += bestMatchLength - 1;
                lookAheadBufferEndPtr += bestMatchLength - 1;
                searchBufferStartPtr += bestMatchLength - 1;
                uniqueElementCount = 0;
                uniqueElementPtr = -1;

            } else {

                if (uniqueElementCount > 31) {
                    uniqueElementCount = 0;
                    uniqueElementPtr = -1;
                }
                uniqueElementCount++;
                // Literal run encoding
                if (uniqueElementPtr == -1) {
                    output.add((byte) 0);
                    uniqueElementPtr = output.size() - 1;
                } else {
                    output.set(uniqueElementPtr, (byte) (uniqueElementCount - 1));
                }

                output.add(input[cursor]);
            }

            lookAheadBufferEndPtr++;
            searchBufferStartPtr++;
            cursor++;
        }

        return copyArray(output);
    }

    public byte[] decode(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>();
        int cursor = 0;
        byte buffer;
        // TODO: check if buffer reads reference
        while (cursor < input.length) {
            buffer = input[cursor];
            if ((buffer >> 5) == 0) {
                //literal run
                int size = (buffer & 0x1f) + 1; // 0x1f == 0001 1111
                for (int i = cursor + 1; i < cursor + size + 1; i++) {
                    output.add(input[i]);
                }
                cursor += size + 1;
            } else if (((buffer & 0xff) >> 5) == 7) {
                // Long match
                int matchLen = ((buffer & 0xff) & 0x1f) << 3 | ((input[cursor + 1] & 0xe0) >> 5) + 1; // 0xe0 == 1110 0000
                cursor++; // 2nd byte
                int offset = (input[cursor] & 0x1f) << 8;
                offset |= (input[cursor + 1] & 0xff) + 1;
//                int offset = (input[cursor] & 0x1f) + input[cursor + 1] + 1; // TODO: incorrect int
                cursor++; // 3rd byte
                int size = output.size();
                for (int i = 0; i < matchLen; i++) {
//                    output.add(input[cursor - 1 - offset + i]);
                    output.add(output.get(size - offset + i));
                }
                cursor++; // next byte
            } else {
                // Short match
                int matchLen = ((buffer & 0xe0) >> 5) + 2;
                int offset = (input[cursor] & 0x1f) << 8;
                offset |= (input[cursor + 1] & 0xff) + 1;
//                int offset = (buffer & 0x1f) + input[cursor + 1] + 1; // Incorrect int
                cursor++; // second byte
                int size = output.size();
                for (int i = 0; i < matchLen; i++) {
//                    output.add(input[cursor - 1 - offset + i]); // -1 byte of reference
                    output.add(output.get(size - offset + i)); // -1 byte of reference
                }
                cursor++; // next byte;
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
