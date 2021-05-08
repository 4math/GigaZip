package com.fourmath;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        GigaZipFile file = new GigaZipFile();
        //TODO: file empty
        outer:
        while (true) {
            Scanner sc = new Scanner(System.in);

            System.out.println("What to do?");
            System.out.println("1) Compress Huffman");
            System.out.println("2) Decompress Huffman");
            System.out.println("3) Compress LZ77");
            System.out.println("4) Decompress LZ77");
            System.out.println("5) Exit");

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> {
                    System.out.print("File name: ");
                    String filename = sc.nextLine();
                    file.compress(filename);
                    System.out.println("Done");
                }
                case 2 -> {
                    System.out.print("File name: ");
                    String filename = sc.nextLine();
                    file.decompress(filename);
                    System.out.println("Done");
                }
                case 3 -> {
                    System.out.print("File name: ");
                    String filename = sc.nextLine();
                    File f = new File(filename);
                    byte[] input = Files.readAllBytes(f.toPath());
                    LZ77 lz77 = new LZ77();
                    long start = System.nanoTime();
                    var result = lz77.encode(input);
                    long end = System.nanoTime();
                    System.out.println("Encode time: " + (end - start) / (1000 * 1000) + "ms");
                    try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename + ".lz"))) {
                        out.write(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Done");
                }
                case 4 -> {
                    System.out.print("File name: ");
                    String filename = sc.nextLine();
                    File f = new File(filename);
                    byte[] input = Files.readAllBytes(f.toPath());
                    LZ77 lz77 = new LZ77();
                    long start = System.nanoTime();
                    var result = lz77.decode(input);
                    long end = System.nanoTime();
                    System.out.println("Decode time: " + (end - start) / (1000 * 1000) + "ms");
                    try (DataOutputStream out = new DataOutputStream(
                            new FileOutputStream("c" + filename.substring(0, filename.length() - 3)))) {
                        out.write(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case 5 -> {
                    break outer;
                }
            }
        }
    }
}
