package com.fourmath;

import java.util.ArrayList;

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

        SuffixArray suffixArray = new SuffixArray(input);

        while (cursor < input.length) {   //-2?

            // Searching for the longest match prefix
            bestMatchLength = 0;
            bestOffset = searchBufferSize + 1;

            /*for (int i = cursor - 1; i >= searchBufferStartPtr && i >= 0; i--) {
                lookAheadMatchIdx = cursor;

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
            }*/

            //
            if (cursor < input.length - 2) {
                int[] data = suffixArray.searchBestMatch(cursor);
                bestMatchLength = data[0];
                bestOffset = data[1];
                if (bestOffset == -1) {
                    bestMatchLength = 0;
                    bestOffset = 0;
                } else {
                    if (bestMatchLength > 64) {
                        bestMatchLength = 64;
                    }
                    bestOffset = cursor - data[1];
                    if (bestOffset > 16383) {
                        bestOffset = 0;
                        bestMatchLength = 0;
                    }
                }
            }
            else {
                bestOffset = 0;
                bestMatchLength = 0;
            }
            //

            // TODO: bit boundaries check
            if (bestMatchLength >= 3 && bestMatchLength <= 8 && bestOffset <= (searchBufferSize >> 1)) {
                // first byte contains information about match length and offset, second about offset only
                // M2 - M0 O12 - O8   O7 - O0
                // Match have all values, excluding 000 and 111, since they are reserved for other references
                bestOffset--;

                byte matchOffsetByte = (byte) ((bestMatchLength - 2) << 5);
                matchOffsetByte |= (byte) (bestOffset >> 8);
                output.add(matchOffsetByte);

                byte offsetByte = (byte) bestOffset;
                output.add(offsetByte);

                // -1 so that the next symbol won't be skipped by window
                cursor += bestMatchLength - 1;
                lookAheadBufferEndPtr += bestMatchLength - 1;
                searchBufferStartPtr += bestMatchLength - 1;

                uniqueElementCount = 0;
                uniqueElementPtr = -1;

            } else if (bestMatchLength > 8) {
                System.out.println(cursor);
                bestMatchLength--;
                bestOffset--;

                byte firstByte = (byte) 0xe0; // 111 at the start
                firstByte |= (byte) ((bestMatchLength) >> 2);
                output.add(firstByte);

                byte secondByte = (byte) (((bestMatchLength) & 0x3) << 6); // 0x7 = 0000 0111, 0x3 = 0000 0011
                secondByte |= (byte) (bestOffset >> 8);
                output.add(secondByte);

                byte thirdByte = (byte) (bestOffset);
                output.add(thirdByte);

                cursor += bestMatchLength;
                lookAheadBufferEndPtr += bestMatchLength;
                searchBufferStartPtr += bestMatchLength;

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

class SuffixArray {

    ArrayList<Integer>[][][] sa;
    byte[] bFile;

    SuffixArray(byte[] bFile) {
        this.bFile = bFile;
        sa = new ArrayList[256][256][256];
        for (int i = 0; i < bFile.length - 2; i++) {
            if (sa[bFile[i] & 0xff][bFile[i + 1] & 0xff][bFile[i + 2] & 0xff] == null) {
                sa[bFile[i] & 0xff][bFile[i + 1] & 0xff][bFile[i + 2] & 0xff] = new ArrayList<Integer>();
            }
            sa[bFile[i] & 0xff][bFile[i + 1] & 0xff][bFile[i + 2] & 0xff].add(i);
        }
    }

    public ArrayList<Integer> searchIndexes(int start) {
        return sa[bFile[start] & 0xff][bFile[start + 1] & 0xff][bFile[start + 2] & 0xff];
    }

    public int[] searchBestMatch(int start) {
        ArrayList<Integer> arrayOfIndexes = searchIndexes(start);
        LinkedList listOfIndexes = new LinkedList();
        Node ptr = listOfIndexes.head;
        for (int i = 0; i < arrayOfIndexes.size(); i++) {
            if (arrayOfIndexes.get(i) < start) {
                ptr.setNext(new Node(arrayOfIndexes.get(i), ptr.next));
                ptr = ptr.next;
                listOfIndexes.size++;
            }
            else {
                break;
            }
        }
        int len = 3;
        loop: for (len = 3; len < 65; len++) {
            ptr = listOfIndexes.head;
            int howMany = listOfIndexes.size;
            for (int i = 0; i < howMany; i++) {
                if (start + len >= bFile.length || ptr.next.indx + len >= bFile.length || ptr.next.indx + len >= start || bFile[ptr.next.indx + len] != bFile[start + len]) {
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
        answer[1] = listOfIndexes.head.next.indx;
        return answer;
    }

}

class LinkedList {

    Node head;
    Node tail;
    int size;

    public LinkedList() {
        head = new Node(-1);
        tail = new Node(-1);
        size = 0;
        head.setNext(tail);
    }

}

class Node {

    int indx;
    Node next;

    public Node(int indx) {
        next = null;
        this.indx = indx;
    }

    public Node(int indx, Node next) {
        this.indx = indx;
        this.next = next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

}
