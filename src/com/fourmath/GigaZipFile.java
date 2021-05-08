package com.fourmath;

import java.io.*;
import java.nio.file.Files;

public class GigaZipFile {

    private final String ext = ".gigazip";

    public void compress(String filename) throws IOException {
        File file = new File(filename);
        byte[] input = Files.readAllBytes(file.toPath());
        Encoder encoder = new Encoder(input);
        encoder.writeCompressedFile(filename + ext);
        System.out.println("Success!");
    }

    public void decompress(String filename) throws IOException {
        File file = new File(filename);
        byte[] input = Files.readAllBytes(file.toPath());
        Decoder decoder = new Decoder(input);
        decoder.writeDecompressedFile(filename);
        System.out.println("Success");
    }

}
