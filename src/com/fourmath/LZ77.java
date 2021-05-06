package com.fourmath;

import java.util.ArrayList;
import java.util.Arrays;

public class LZ77 {

    public byte[] encode(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>(10 * 1000 * 1000);

        int uniqueElementPtr = -1;
        int uniqueElementCount = 0;

        int lookAheadBufferSize = 64;
        int searchBufferSize = 1 << 14; // 16384

        int cursor = 0;

        int lookAheadBufferEndPtr = lookAheadBufferSize;
        int searchBufferStartPtr = -searchBufferSize;

        int matchLength;
        int offset;
        int bestMatchLength;
        int bestOffset;
        int lookAheadMatchIdx;

        while (cursor < input.length) {

            int start = Math.max(searchBufferStartPtr, 0);
            int end = Math.min(lookAheadBufferEndPtr, input.length);

            ByteSuffixArray bsa = new ByteSuffixArray(start, end, input);
            int[] lcp = bsa.getLcp();
            Suffix[] suff = bsa.getSuff();

            bestMatchLength = 0;
            bestOffset = searchBufferSize + 1;


            for (int i = cursor; i < end; i++) {
                int left = -1, right = -1;

                for (int j = 0; j < suff.length; j++) {
                    if (suff[j].index == i) {
                        if (j - 1 >= 0) {
                            left = lcp[j - 1];
                        }
                        if (j + 1 < lcp.length) {
                            right = lcp[j + 1];
                        }

                        if (left == -1 && right == -1 || right == 0 && left == 0) {

//                            if (uniqueElementCount > 31) {
//                                uniqueElementCount = 0;
//                                uniqueElementPtr = -1;
//                            }
//
//                            uniqueElementCount++;
//
//                            // Literal run encoding
//                            if (uniqueElementPtr == -1) {
//                                output.add((byte) 0);
//                                uniqueElementPtr = output.size() - 1;
//                            } else {
//                                output.set(uniqueElementPtr, (byte) (uniqueElementCount - 1));
//                            }
//
//                            output.add(input[i]);

                        } else if (left > right) {
                            bestOffset = i - suff[j - 1].index;
                            bestMatchLength = left;
                            i += left;
                        } else {
                            bestOffset = i - suff[j + 1].index;
                            bestMatchLength = right;
                            i += right;
                        }
                    }
                }
            }



//            int start = Math.max(searchBufferStartPtr, 0);
//            int end = Math.min(lookAheadBufferEndPtr, input.length);
////            byte[] dictionary = ByteSuffixArray.substr(start, cursor, input);
//            ByteSuffixArray bsa = new ByteSuffixArray(start, cursor, input);
//
//            bestMatchLength = 0;
//            bestOffset = searchBufferSize + 1;
//
//            for (int i = 1; i < end && i < cursor - start; i++) {
//                int endLine = Math.min(cursor + i, input.length);
////                byte[] prefix = ByteSuffixArray.substr(cursor, endLine, input);
//                int search = bsa.enhancedSearch(input, cursor, endLine);
//                if (search != -1) {
//                    int n = endLine - cursor;
//                    if (n > bestMatchLength) {
//                        bestMatchLength = n;
//                        bestOffset = cursor - search;
//                    }
//                }
//            }

            // Searching for the longest match prefix
//            bestMatchLength = 0;
//            bestOffset = searchBufferSize + 1;
//
//            for (int i = cursor - 1; i >= searchBufferStartPtr && i >= 0; i--) {
//                lookAheadMatchIdx = cursor;
//
//                if (input[i] == input[lookAheadMatchIdx]) {
//                    matchLength = 0;
//                    offset = cursor - i;
//
//                    for (int searchMatchIdx = i;
//                         searchMatchIdx < cursor &&
//                                 lookAheadMatchIdx < lookAheadBufferEndPtr &&
//                                 lookAheadMatchIdx < input.length &&
//                                 input[searchMatchIdx] == input[lookAheadMatchIdx]; searchMatchIdx++) {
//                        lookAheadMatchIdx++;
//                        matchLength++;
//                    }
//
//                    if (matchLength > bestMatchLength) {
//                        bestMatchLength = matchLength;
//                        bestOffset = offset;
//                    }
//
//                }
//            }

            // TODO: bit boundaries check
//            if (bestMatchLength >= 3 && bestMatchLength <= 8 && bestOffset <= (searchBufferSize >> 1)) {
//                // first byte contains information about match length and offset, second about offset only
//                // M2 - M0 O12 - O8   O7 - O0
//                // Match have all values, excluding 000 and 111, since they are reserved for other references
//                bestOffset--;
//
//                byte matchOffsetByte = (byte) ((bestMatchLength - 2) << 5);
//                matchOffsetByte |= (byte) (bestOffset >> 8);
//                output.add(matchOffsetByte);
//
//                byte offsetByte = (byte) bestOffset;
//                output.add(offsetByte);
//
//                // -1 so that the next symbol won't be skipped by window
//                cursor += bestMatchLength - 1;
//                lookAheadBufferEndPtr += bestMatchLength - 1;
//                searchBufferStartPtr += bestMatchLength - 1;
//
//                uniqueElementCount = 0;
//                uniqueElementPtr = -1;
//
//            } else if (bestMatchLength > 8) {
//                bestMatchLength--;
//                bestOffset--;
//
//                byte firstByte = (byte) 0xe0; // 111 at the start
//                firstByte |= (byte) ((bestMatchLength) >> 2);
//                output.add(firstByte);
//
//                byte secondByte = (byte) (((bestMatchLength) & 0x3) << 6); // 0x7 = 0000 0111, 0x3 = 0000 0011
//                secondByte |= (byte) (bestOffset >> 8);
//                output.add(secondByte);
//
//                byte thirdByte = (byte) (bestOffset);
//                output.add(thirdByte);
//
//                cursor += bestMatchLength;
//                lookAheadBufferEndPtr += bestMatchLength;
//                searchBufferStartPtr += bestMatchLength;
//
//                uniqueElementCount = 0;
//                uniqueElementPtr = -1;
//
//            } else {
//
//                if (uniqueElementCount > 31) {
//                    uniqueElementCount = 0;
//                    uniqueElementPtr = -1;
//                }
//
//                uniqueElementCount++;
//
//                // Literal run encoding
//                if (uniqueElementPtr == -1) {
//                    output.add((byte) 0);
//                    uniqueElementPtr = output.size() - 1;
//                } else {
//                    output.set(uniqueElementPtr, (byte) (uniqueElementCount - 1));
//                }
//
//                output.add(input[cursor]);
//            }

            cursor += lookAheadBufferSize;
            lookAheadBufferEndPtr += lookAheadBufferSize;
            searchBufferStartPtr += lookAheadBufferSize;
//            lookAheadBufferEndPtr++;
//            searchBufferStartPtr++;
//            cursor++;
        }

        return copyArray(output);
    }

    public byte[] decode(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>(10 * 1000 * 1000);
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
                // 0xe0 == 1110 0000
                // 0xc0 == 1100 0000
                int matchLen = (((buffer & 0xff) & 0x1f) << 2 | ((input[cursor + 1] & 0xc0) >> 6)) + 1;
                cursor++; // 2nd byte
                int offset = (input[cursor] & 0x3f) << 8; // 0x3f = 0011 1111
                cursor++; // 3rd byte
                offset |= (input[cursor]) & 0xff;
                offset++;

                int size = output.size();
                for (int i = 0; i < matchLen; i++) {
                    output.add(output.get(size - offset + i));
                }
                cursor++; // next byte
            } else {
                // Short match
                int matchLen = ((buffer & 0xe0) >> 5) + 2;
                int offset = (input[cursor] & 0x1f) << 8;
                cursor++; // second byte
                offset |= ((input[cursor])) & 0xff;
                offset++;

                int size = output.size();
                for (int i = 0; i < matchLen; i++) {
                    output.add(output.get(size - offset + i));
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
