package com.fourmath;


import java.util.Arrays;

public class ByteSuffixArray {

    private final byte[] data;
    private final int n; // string size

    public Suffix[] getSuff() {
        return suff;
    }

    private final Suffix[] suff;
    private final int lcp[];

    ByteSuffixArray(byte[] data) {
        this.data = data;
        this.n = data.length;
        this.suff = new Suffix[n];
        this.lcp = new int[n];
        constructArray();
        constructLCP();
    }

    ByteSuffixArray(int start, int end, byte[] data) {
        this.data = data;
        this.n = end - start;
        this.suff = new Suffix[n];
        this.lcp = new int[n];
        constructArray(start, end);
        constructLCP();
    }

    public static byte[] substr(int begin, int end, byte[] arr) {
        int n = end - begin;
        byte[] newArr = new byte[n];
        for (int i = 0; i < n; i++) {
            newArr[i] = arr[begin + i];
        }
        return newArr;
    }

    public int[] getLcp() {
        return lcp;
    }

    private void constructArray(int start, int end) {
        for (int i = 0; i < n; i++) {
            suff[i] = new Suffix(i, data[start + i], 0);
        }

        for (int i = 0; i < n; i++) {
            suff[i].next = (i + 1 < n ? suff[i + 1].rank : -1);
        }

        Arrays.sort(suff);

        int[] ind = new int[n];

        // This array is needed to get the index in suffixes[]
        // from original index. This mapping is needed to get
        // next suffix.
        for (int length = 4; length < 2 * n; length <<= 1) {

            // Assigning rank and index values to first suffix
            int rank = 0;
            int prev = suff[0].rank;
            suff[0].rank = rank;
            ind[suff[0].index] = 0;

            for (int i = 1; i < n; i++) {
                // If first rank and next ranks are same as
                // that of previous suffix in array,
                // assign the same new rank to this suffix
                if (suff[i].rank == prev &&
                        suff[i].next == suff[i - 1].next) {
                    prev = suff[i].rank;
                    suff[i].rank = rank;
                } else {
                    // Otherwise increment rank and assign
                    prev = suff[i].rank;
                    suff[i].rank = ++rank;
                }
                ind[suff[i].index] = i;
            }

            // Assign next rank to every suffix
            for (int i = 0; i < n; i++) {
                int nextP = suff[i].index + length / 2;
                suff[i].next = nextP < n ?
                        suff[ind[nextP]].rank : -1;
            }

            // Sort the suffixes according
            // to first k characters
            Arrays.sort(suff);
        }
    }

//    public Suffix[] getSuff() {
//        return suff;
//    }

    public void constructLCP() {
        // An auxiliary array to store inverse of suffix array
        // elements. For example if suffixArr[0] is 5, the
        // invSuff[5] would store 0.  This is used to get next
        // suffix string from suffix array
        int[] invSuff = new int[n];

        for (int i = 0; i < n; i++) {
            invSuff[suff[i].index] = i;
        }

        // Initialize length of previous LCP
        int k = 0;

        for (int i = 0; i < n; i++) {
            if (invSuff[i] == n - 1) {
                k = 0;
                continue;
            }

            int j = suff[invSuff[i] + 1].index;

            // Directly start matching from k'th index as
            // at-least k-1 characters will match
            while (i + k < n && j + k < n && data[i + k] == data[j + k])
                k++;

            lcp[invSuff[i] + 1] = k;

            if (k > 0)
                k--;
        }
    }

    public void showSuffixes() {
//        for (int i = 0; i < suff.length; i++) {
//            System.out.printf("%s: %d %d%n", data.substring(suff[i].index), suff[i].rank, suff[i].next);
//        }
    }

    public int enhancedSearch(byte[] input, int pStart, int pEnd) {

        int low = 0;
        int high = suff.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int suffStart = suff[mid].index;
            int suffEnd = data.length;
            int suffix = suffEnd - suffStart;
            int prefix = pEnd - pStart;

            int substrStart;
            int substrEnd;
            if (suffix > prefix) {
                substrStart = suffStart;
                substrEnd = suffStart + prefix;
            } else {
                substrStart = suffStart;
                substrEnd = suffEnd;
            }

            if (enhancedCompare(input, pStart, pEnd, substrStart, substrEnd) > 0) {
                low = mid + 1;
            } else if (enhancedCompare(input, pStart, pEnd, substrStart, substrEnd) < 0) {
                high = mid - 1;
            } else {
                return suff[mid].index;
            }
        }
        return -1;
    }

    private int enhancedCompare(byte[] input, int fs, int fe, int ss, int se) {

        final int l1 = fe - fs;
        final int l2 = se - ss;
        int size = Math.min(l1, l2);

        for (int i = 0; i < size; i++) {
            byte s1 = input[fs + i];
            byte s2 = data[ss + i];
            if (s1 != s2) {
                return s1 - s2;
            }
        }

        if (l1 != l2) {
            return l1 - l2;
        }
        return 0;
    }

    public int search(byte[] prefix) {

        int low = 0;
        int high = suff.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            byte[] suffix = substr(suff[mid].index, data.length, data);
            byte[] substr;
            if (suffix.length > prefix.length) {
                substr = substr(0, prefix.length, suffix);
            } else {
                substr = suffix;
            }

            if (compare(prefix, substr) > 0) {
                low = mid + 1;
            } else if (compare(prefix, substr) < 0) {
                high = mid - 1;
            } else {
                return suff[mid].index;
            }
        }
        return -1;
    }

    private int compare(byte[] f, byte[] s) {

        final int l1 = f.length;
        final int l2 = s.length;
        int size = Math.min(l1, l2);

        for (int i = 0; i < size; i++) {
            byte s1 = f[i];
            byte s2 = s[i];
            if (s1 != s2) {
                return s1 - s2;
            }
        }

        if (l1 != l2) {
            return l1 - l2;
        }

        return 0;
    }

    private void constructArray() {

        for (int i = 0; i < n; i++) {
            suff[i] = new Suffix(i, data[i], 0);
        }

        for (int i = 0; i < n; i++) {
            suff[i].next = (i + 1 < n ? suff[i + 1].rank : -1);
        }

        Arrays.sort(suff);

        int[] ind = new int[n];

        // This array is needed to get the index in suffixes[]
        // from original index. This mapping is needed to get
        // next suffix.
        for (int length = 4; length < 2 * n; length <<= 1) {

            // Assigning rank and index values to first suffix
            int rank = 0;
            int prev = suff[0].rank;
            suff[0].rank = rank;
            ind[suff[0].index] = 0;

            for (int i = 1; i < n; i++) {
                // If first rank and next ranks are same as
                // that of previous suffix in array,
                // assign the same new rank to this suffix
                if (suff[i].rank == prev &&
                        suff[i].next == suff[i - 1].next) {
                    prev = suff[i].rank;
                    suff[i].rank = rank;
                } else {
                    // Otherwise increment rank and assign
                    prev = suff[i].rank;
                    suff[i].rank = ++rank;
                }
                ind[suff[i].index] = i;
            }

            // Assign next rank to every suffix
            for (int i = 0; i < n; i++) {
                int nextP = suff[i].index + length / 2;
                suff[i].next = nextP < n ?
                        suff[ind[nextP]].rank : -1;
            }

            // Sort the suffixes according
            // to first k characters
            Arrays.sort(suff);
        }
    }


}
class Suffix implements Comparable<Suffix> {
    int rank;
    int next; // next symbol
    int index;

    Suffix(int index, int rank, int next) {
        this.index = index;
        this.rank = rank;
        this.next = next;
    }

    @Override
    public int compareTo(Suffix o) {
        if (rank != o.rank) return rank - o.rank;
        return next - o.next;
    }
}