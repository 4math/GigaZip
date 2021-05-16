package com.fourmath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        CompressionType cType = CompressionType.Deflate;
        boolean verbose = false;
        GigaZipFile file;

        if (args.length != 0 && args[0].equals("-huffman")) {
            cType = CompressionType.Huffman;
        } else if (args.length != 0 && args[0].equals("-lz77")) {
            cType = CompressionType.LZ77;
        } else if (args.length != 0 && args[0].equals("-verbose")) {
            verbose = true;
        }

        file = new GigaZipFile(cType, verbose);

        outer:
        while (true) {
            Scanner sc = new Scanner(System.in);

            if (verbose) {
                System.out.println("\nWhat to do?");
                System.out.println("comp: compress file(Algorithm depends on flag, default is Deflate)");
                System.out.println("decomp: decompress file(Algorithm depends on flag, default is Deflate with .gigazip extension)");
                System.out.println("size: prints size of the given file in bytes");
                System.out.println("equal: compares two files content");
                System.out.println("about: prints information about authors");
                System.out.println("exit: exits the program");
            }

            String choice = sc.nextLine();

            switch (choice) {
                case "comp" -> {
                    System.out.print("source file name: ");
                    String input = sc.nextLine();
                    System.out.print("archive name: ");
                    String output = sc.nextLine();
                    try {
                        file.compress(input, output);
                    } catch (IOException e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "decomp" -> {
                    System.out.print("archive name: ");
                    String input = sc.nextLine();
                    System.out.print("file name: ");
                    String output = sc.nextLine();
                    try {
                        file.decompress(input, output);
                    } catch (IOException e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "size" -> {
                    System.out.print("file name: ");
                    String filename = sc.nextLine();
                    try {
                        File f = new File(filename);
                        if (verbose && !f.exists()) throw new Exception("File does not exist!");
                        System.out.println("size: " + f.length() + " B");
                    } catch (Exception e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "equal" -> {
                    System.out.print("first file name: ");
                    String first = sc.nextLine();
                    System.out.print("second file name: ");
                    String second = sc.nextLine();
                    try {
                        File f1 = new File(first);
                        if (verbose && !f1.exists()) throw new Exception("First file does not exist!");
                        File f2 = new File(second);
                        if (verbose && !f2.exists()) throw new Exception("Second file does not exist!");
                        boolean isEqual = sameContent(f1, f2);
                        System.out.println(isEqual);
                    } catch (Exception e) {
                        if (verbose) {
                            System.out.println(e.toString());
                        }
                    }
                }
                case "about" -> {

                }
                case "exit" -> {
                    break outer;
                }
                default -> {
                    if (verbose)
                        System.out.println("wrong command");
                }
            }
        }
    }

    private static boolean sameContent(File f1, File f2) throws IOException {
        if (f1.length() != f2.length()) return false;
        return Arrays.equals(Files.readAllBytes(f1.toPath()), Files.readAllBytes(f2.toPath()));
    }
}
