package com.fourmath;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {

        String filename = "shakespeare.txt";
        File file = new File(filename);
        byte[] input = Files.readAllBytes(file.toPath());
        LZ77 lz77 = new LZ77();

//        var result = lz77.encode(input);
//        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename + ".lz"))) {
//            out.write(result);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        file = new File(filename + ".lz");
        input = Files.readAllBytes(file.toPath());
        var result = lz77.decode(input);
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename.substring(0, filename.length() - 3)))) {
            out.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        GigaZipFile file = new GigaZipFile();
//        //TODO: file empty
//        Scanner sc = new Scanner(System.in);
//
//        System.out.println("What to do?");
//        System.out.println("1) Compress");
//        System.out.println("2) Decompress");
//
//        int choice = Integer.parseInt(sc.nextLine());
//
//        if (choice == 1) {
//            System.out.print("File name: ");
//            String filename = sc.nextLine();
//            file.compress(filename);
//        } else {
//            System.out.print("File name: ");
//            String filename = sc.nextLine();
//            file.decompress(filename);
//        }
    }
}
