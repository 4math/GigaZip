# GigaZip

A university project in which compression algorithm was realized. We have worked on Deflate which is a combination of Huffman coding and LZ77 (LZSS). 

The project was done in Intellij IDEA 2020.

# Project structure

- [examples](./examples) folder contains examples which were used in benchmarking.
- [slides](./slides) folder contains presentation slides (in Latvian).
- [/src/com/fourmath]([./src/com/fourmath) folder contains all the source files

# Results

Table below presents the results of our compression implementation and Windows built-in zip compression which also uses Deflate. We can clearly see that our implementation compresses less than WinZip from 2 % to 9 %, however, this result was achieved in a shorter amount of time in comparison to WinZip development. All the files can be found in [examples](./examples) folder.

| Nr   | Filename           | File size (KB) | GigaZip compressed file size (KB) | GigaZip compression ratio | WinZip compressed file size (KB) | WinZip compression ratio | Difference |
| ---- | ------------------ | -------------- | --------------------------------- | ------------------------- | -------------------------------- | ------------------------ | ---------- |
| 1    | java.html          | 80.48          | 26.63                             | 66.91 %                   | 21.03                            | 75%                      | 8.09 %     |
| 4    | rtu.html           | 83.07          | 24.33                             | 70.72 %                   | 19.17                            | 78%                      | 7.28 %     |
| 2    | rainis.html        | 206.7          | 45.13                             | 78.17 %                   | 37.04                            | 83%                      | 4.83 %     |
| 3    | js.html            | 344.5          | 82.12                             | 76.16 %                   | 68.32                            | 81%                      | 4.84 %     |
| 7    | thehungergames.txt | 523            | 244.9                             | 53.18 %                   | 203.27                           | 62%                      | 8.82 %     |
| 8    | montecarlo.html    | 612.9          | 79.25                             | 87.07 %                   | 67.47                            | 90%                      | 2.93 %     |
| 9    | english.html       | 907.5          | 198                               | 78.18 %                   | 169.06                           | 82%                      | 3.82 %     |
| 6    | bush.html          | 987.8          | 213.4                             | 78.40 %                   | 182.44                           | 82%                      | 3.60 %     |
| 5    | cat.bmp            | 2268           | 1046                              | 53.88 %                   | 862.1                            | 62%                      | 8.12 %     |
| 10   | sky.bmp            | 8998           | 2180                              | 75.76 %                   | 1998.85                          | 78%                      | 2.24  %    |

Table below represents the compression and decompression speeds. We can clearly see that compression speed is quite low and it is adequate to compress only small files till 3 - 5 MB. Performance is the problem of our implementation. 

| Nr.  | Filename           | File size (KB) | GigaZip  compression time (ms) | Compression speed  (MB/s) | Decompression speed (MB/s) |
| ---- | ------------------ | -------------- | ------------------------------ | ------------------------- | -------------------------- |
| 1    | java.html          | 80.48          | 165.9                          | 0.16                      | 4.77                       |
| 4    | rtu.html           | 83.07          | 68.41                          | 0.36                      | 3.66                       |
| 2    | rainis.html        | 206.7          | 163.5                          | 0.28                      | 11.89                      |
| 3    | js.html            | 344.5          | 153.3                          | 0.54                      | 17.65                      |
| 7    | thehungergames.txt | 523            | 254.5                          | 0.96                      | 18.05                      |
| 8    | montecarlo.html    | 612.9          | 154.3                          | 0.51                      | 19.12                      |
| 9    | english.html       | 907.5          | 259.9                          | 0.76                      | 23.5                       |
| 6    | bush.html          | 987.8          | 291.6                          | 0.73                      | 31.75                      |
| 5    | cat.bmp            | 2268           | 1085                           | 0.96                      | 28.73                      |
| 10   | sky.bmp            | 8998           | 5731                           | 0.38                      | 24.38                      |

# Algorithms - Deflate

We have used Deflate algorithm to compress our data and it consists of 2 algorithm combination - Huffman coding and LZ77 (LZSS).

## Huffman coding

We have used the information from this source: https://cs.stanford.edu/people/eroberts/courses/soco/projects/data-compression/lossless/huffman/index.htm to understand and implement the Huffman tree. 

## LZ77 (LZSS)

Deflate also uses LZ77, however, improved version LZSS does not require symbol in a reference and encodes only two values: relative offset and match length. We have implemented 2 versions of encoding, but prefix bit encoding was used in the final version due to its higher compression ratio.  

### Reference encoding

The idea was taken from https://laptrinhx.com/byte-aligned-lz77-compression-3128524495/.

There are 3 types of references:

| Reference name          | 1st byte         | **2nd byte**  | **3rd byte** |
| ----------------------- | ---------------- | ------------- | ------------ |
| Unique symbol reference | `000`M3-M7       |               |              |
| Short reference         | `001 – 110`R3-R7 | R8-R15        |              |
| Long reference          | `111`M3-M7       | M8-M9 R10-R15 | R16-R23      |

R shows bit indices which are used for offset encoding. M shows bit indices which are used for match length. First 3 bits are used to recognize the type of the reference.

Short reference also use first 3 bits to encode the match length from 3 to 8 where 001 means 3 and 110 - 8. 

Maximum search buffer size is 16384 bytes and maximum look-ahead buffer size is 128 bytes. 

This encoding does not provide the best compression, because buffers are quite small and if there are small number of unique symbols, from 1 to 7 symbols per one unique reference, then there is an overhead. 

### Prefix bit encoding

Second version uses one additional bit to encode whether it is data or a reference. 

Table below shows the encoding of data.

|      | 1st byte       |
| ---- | -------------- |
| 0    | 1 byte of data |

Table below shows the encoding of reference.

|      | 1st byte          | 2nd byte        | 3rd byte        |
| ---- | ----------------- | --------------- | --------------- |
| 1    | match length byte | 1st offset byte | 2nd offset byte |

Maximum search buffer size is 65536 bytes and maximum look-ahead buffer size is 256 bytes. 

## Suffix array



# References

- https://laptrinhx.com/byte-aligned-lz77-compression-3128524495/
- https://algo.developpez.com/actu/308570/Comprendre-l-algorithme-de-compression-LZ77-un-billet-de-fbonhomm/
- https://courses.cs.duke.edu//spring16/compsci590.6/compression4.pdf
- https://cs.stanford.edu/people/eroberts/courses/soco/projects/data-compression/lossless/huffman/index.htm
- https://arxiv.org/pdf/0903.4251.pdf
- http://www.allisons.org/ll/AlgDS/Strings/Suffix/
