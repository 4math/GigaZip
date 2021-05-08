package com.fourmath;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class HuffmanTree {
	Node root = null;
    HashMap<Byte, String> table = new HashMap<>();
    HashMap<Byte, Integer> frequency = new HashMap<>();
    byte[] input;
    Integer cutoff = 0;
    Integer maxFrequency = Integer.MIN_VALUE;

    HuffmanTree(byte[] input) {
        this.input = input;
        calculateFrequency();
        createTree();
        createTable();
    }

    HuffmanTree(HashMap<Byte, Integer> frequency) {
        this.frequency = frequency;
        createTree();
        createTable();
    }

    public byte[] encode() {
        StringBuilder sb = new StringBuilder();

        for (byte key : input) {
            sb.append(table.get(key));
        }

        if (sb.length() % 8 != 0) {
            cutoff = 8 - sb.length() % 8;
            sb.append("0".repeat(cutoff));
        }

        int size = sb.length() / 8;
        byte[] byteArr = new byte[size];

        for (int i = 0; i < size; i++) {
            int b = Integer.parseInt(sb.substring(i * 8, (i + 1) * 8), 2);
            byteArr[i] = (byte) b;
        }

        return byteArr;
    }

    void calculateFrequency() {
        for (byte key : input) {
            if (frequency.containsKey(key)) {
                frequency.put(key, frequency.get(key) + 1);
            } else {
                frequency.put(key, 1);
            }
            maxFrequency = Math.max(maxFrequency, frequency.get(key));
        }
    }

    void createTree() {
        TreeSet<Node> treeSet = new TreeSet<>();

        int nodeId = 0;

        for (byte key : frequency.keySet()) {
            treeSet.add(new Node(key, frequency.get(key), nodeId++));
        }

        while (treeSet.size() != 1) {
            Node right = treeSet.pollFirst();
            Node left = treeSet.pollFirst();
            Node localRoot = new Node(right.weight + left.weight, right, left, nodeId++);
            treeSet.add(localRoot);
        }

        root = treeSet.pollFirst();
    }

    void createTable() {
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();

            if (node.right == null && node.left == null) {
                table.put(node.byteData, node.binaryView);
            }

            if (node.left != null) {
                queue.add(node.left);
                node.left.binaryView = node.binaryView + "0";
            }

            if (node.right != null) {
                queue.add(node.right);
                node.right.binaryView = node.binaryView + "1";
            }
        }

        System.out.println(table);
    }

    byte[] tableToByteArray(byte frequencySize) {
        byte[] tableByteArray = new byte[frequency.size() * (1 + frequencySize)];

        int ptr = 0;
        for (byte key : frequency.keySet()) {
            tableByteArray[ptr++] = key;

            if (frequencySize == 1) {
                byte amount = frequency.get(key).byteValue();
                tableByteArray[ptr++] = amount;
            } else if (frequencySize == 2) {
                short amount = frequency.get(key).shortValue();
                tableByteArray[ptr++] = (byte)(amount & 0xff);
                tableByteArray[ptr++] = (byte)((amount >> 8) & 0xff);
            } else if (frequencySize == 4) {
                int amount = frequency.get(key);
                tableByteArray[ptr++] = (byte)(amount & 0xff);
                tableByteArray[ptr++] = (byte)((amount >> 8) & 0xff);
                tableByteArray[ptr++] = (byte)((amount >> 16) & 0xff);
                tableByteArray[ptr++] = (byte)((amount >> 24) & 0xff);
            }
        }

        return tableByteArray;
    }

    static class Node implements Comparable<Node> {
        byte byteData;
        int weight;
        int id;
        Node left, right;
        String binaryView = "";

        Node(byte byteData, int weight, int id) {
            this.byteData = byteData;
            this.weight = weight;
            this.left = null;
            this.right = null;
            this.id = id;
        }

        Node(byte byteData, int weight, Node left, Node right, int id) {
            this.byteData = byteData;
            this.weight = weight;
            this.left = left;
            this.right = right;
            this.id = id;
        }

        Node(int weight, Node left, Node right, int id) {
            this.byteData = 0;
            this.weight = weight;
            this.left = left;
            this.right = right;
            this.id = id;
        }

        @Override
        public int compareTo(Node node) {
            int x = weight - node.weight;
            if (x != 0) return x;

            x = byteData - node.byteData;
            if (x != 0) return x;

            return id - node.id;
        }
    }
}
