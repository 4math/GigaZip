package com.fourmath;

import java.util.ArrayList;

// at first u must create a suffix array from the file
// Example:
//SuffixArray suffixArray = new SuffixArray(input, (1 << 8) - 1, (1 << 16) - 1);
//                                          ^       ^             ^
//                                      byte file;  LAB size;     search buffer size
//
// then, u can use searchBestMatch(int start) method, where "start" is the pointer to the start of LAB
//
// searchBestMatch(int start) method will return u an array:
// {bestMatchLength, bestOffset}
// if bestMatchLength == -1 or bestOffset == -1 then no match found for the current start of LAB => cursor++;

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
        LinkedList listOfIndexes = new LinkedList();
        Node ptr;

        int pos = binarySearch(arrayOfIndexes, start);
        if (pos == 0) {
            return new int[]{-1, -1};
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
        loop:
        for (; len < lookAheadBufferSize; len++) {
            ptr = listOfIndexes.head;
            int howMany = listOfIndexes.size;
            for (int i = 0; i < howMany; i++) {
                if (start + len >= bFile.length ||
                        ptr.next.index + len >= bFile.length ||
                        ptr.next.index + len >= start ||
                        bFile[ptr.next.index + len] != bFile[start + len]) {
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
