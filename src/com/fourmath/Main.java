package com.fourmath;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        GigaZipFile file = new GigaZipFile();
        //TODO: file empty
        Scanner sc = new Scanner(System.in);

        System.out.println("What to do?");
        System.out.println("1) Compress");
        System.out.println("2) Decompress");

        int choice = Integer.parseInt(sc.nextLine());

        if (choice == 1) {
            System.out.print("File name: ");
            String filename = sc.nextLine();
            file.compress(filename);
        } else {
            System.out.print("File name: ");
            String filename = sc.nextLine();
            file.decompress(filename);
        }
//        file.compress("js.html");
//        file.decompress("js.html.gigazip");
    }
}
