package com.fourmath;


public class DeflateEncoding implements Compressor {

    @Override
    public byte[] encode(byte[] input) {
        HuffmanEncoding he = new HuffmanEncoding();
        LZ77 lz77 = new LZ77();

        byte[] result = lz77.encode(input);
        result = he.encode(result);

        return result;
    }

    @Override
    public byte[] decode(byte[] input) {
        HuffmanEncoding he = new HuffmanEncoding();
        LZ77 lz77 = new LZ77();

        byte[] result = he.decode(input);
        result = lz77.decode(result);

        return result;
    }
}
