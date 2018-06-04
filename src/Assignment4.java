/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 4
 * Description: Implementation of several lossless compression algorithms
 */

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Assignment4 {
   /**
    * Flattens image by sampling in descending horizontal zig zags.
    *
    * @param img image to flatten
    * @return flattened image
    */
   private static int[] flatten(int[][] img) {
      int i, x, y, dx, dy;
      i = 0;
      x = 0;
      dx = 1;
      dy = 1;
      
      int[] data = new int[img.length * img[0].length];
      
      /*
      Because the values of pixels tend to be similar to those surrounding it,
      it is important to try to sample them in an order such that each
      consecutive pixel is as close as possible to the previous pixel.
      My approach involves going through from top to bottom, alternative between
      moving left and right through each consecutive row.
      This means each pixel is in N4 of the previous pixel.
       */
      for (y = 0; y < img[0].length; y += dy) {
         while (dx == 1 && x < img.length || dx == -1 && x > -1) {
            data[i++] = img[x][y];
            x += dx;
         }
         
         dx *= -1;
         x += dx;
      }
      
      return data;
   }
   
   /**
    * Helper method to generate BitSet for given image.
    * Sampled the same way as flatten.
    *
    * @param img image to flatten
    * @return flattened image as BitSet
    */
   private static BitSet flattenByte(int[][] img) {
      int i, x, y, dx, dy;
      i = 0;
      x = 0;
      dx = 1;
      dy = 1;
      
      int[] data = flatten(img);
      
      BitSet bs = new BitSet(data.length * 8);
      
      for (i = 0; i < data.length; i++) {
         for (int j = 1; j < 256; j <<= 1) {
            bs.set(x++, (data[i] & j) != 0);
         }
      }
      
      return bs;
   }
   
   /**
    * Expands flattened image, assuming it was sampled in the same way as
    * flatten.
    *
    * @param data flattened image
    * @param w    width
    * @param h    height
    * @return unflattened image
    */
   private static int[][] expand(int[] data, int w, int h) {
      int[][] img = new int[w][h];
      int i, x, y, dx, dy;
      x = 0;
      dx = 1;
      dy = 1;
      i = 0;
      
      for (y = 0; y < img[0].length; y += dy) {
         while (dx == 1 && x < img.length || dx == -1 && x > -1) {
            img[x][y] = data[i++];
            x += dx;
         }
         
         dx *= -1;
         x += dx;
      }
      
      return img;
   }
   
   /**
    * Compresses image with run-line coding.
    * This will not work well with lena.jpg because, for the most part,
    * the values of adjacent pixels aren't going to be equal to each other
    *
    * @param img matrix representing image to be compressed
    * @return 1D compressed array representing image
    * output will be of format {run0, val0, run1, val1, ..., runi, vali}
    */
   public static int[] rlcEncode(int[][] img) {
      int[] unencoded = flatten(img);
      
      // buffer will be at most double the length if every consecutive element
      // is unique
      
      int run = 0;
      int val = unencoded[0];
      int count = 1;
      ArrayList<Integer> list = new ArrayList<>();
      int numSame = -1;
      
      int maxRun = 0;
      
      for (int anUnencoded : unencoded) {
         if (anUnencoded == val) {
            run += 1;
            numSame++;
         } else {
            list.add(run);
            list.add(val);
            
            if (run > maxRun) {
               maxRun = run;
            }
            
            run = 1;
            val = anUnencoded;
            
            count++;
         }
      }
      
      list.add(run);
      list.add(val);
      
      // compact buffer and return
      int[] out = new int[count * 2];
      
      for (int i = 0; i < out.length; i++) {
         out[i] = list.get(i);
      }
      
      return out;
   }
   
   /**
    * Decodes compressed image encoded with simple run-length coding.
    *
    * @param compressedImage compressed image
    * @param w               width
    * @param h               height
    * @return decompressed image
    */
   public static int[][] rlcDecode(int[] compressedImage, int w, int h) {
      int[] data = new int[w * h];
      
      int index, val, run;
      index = 0;
      
      for (int i = 0; i < compressedImage.length; i += 2) {
         run = compressedImage[i];
         val = compressedImage[i + 1];
         
         
         for (int j = 0; j < run; j++) {
            data[index++] = val;
         }
      }
      
      int[][] img = expand(data, w, h);
      return img;
   }
   
   /**
    * Performs run-length coding on image's bitplanes
    *
    * @param img image to be encoded
    * @return encoded image
    */
   public static int[] rlcEncodeBitplanes(int[][] img) {
      ArrayList<Integer> list = new ArrayList<>();
      int val, run;
      
      int[] data = flatten(img);
      
      for (int bp = 1; bp < 256; bp <<= 1) {
         run = 0;
         val = data[0] & bp;
         // add first value to show initial value
         list.add(val);
         
         for (int aData : data) {
            if ((aData & bp) == val) {
               run += 1;
            } else {
               list.add(run);
               
               val = aData & bp;
               run = 1;
            }
         }
         
         list.add(run);
      }
      
      int[] out = new int[list.size()];
      
      for (int i = 0; i < out.length; i++) {
         out[i] = list.get(i);
      }
      
      //System.out.println(list.stream().sorted().max(Integer::compare).get());
      
      return out;
   }
   
   /**
    * decodes given array coded with run-length coding performed on different
    * bit planes
    *
    * @param data compressed image
    * @param w    width
    * @param h    height
    * @return decompressed image
    */
   public static int[][] rlcDecodeBitplanes(int[] data, int w, int h) {
      int size = w * h;
      int dataIndex, outIndex;
      boolean zero;
      int count;
      dataIndex = 0;
      int[] uncompressed = new int[w * h];
      
      for (int bp = 1; bp < 256; bp <<= 1) {
         // get starting value
         zero = data[dataIndex++] == 0;
         int val = bp;
         count = 0;
         outIndex = 0;
         
         while (count < size) {
            count += data[dataIndex];
            
            for (int j = 0; j < data[dataIndex]; j++) {
               if (!zero) {
                  uncompressed[outIndex] += val;
               }
               outIndex += 1;
            }
            
            zero = !zero;
            dataIndex++;
         }
      }
      
      return expand(uncompressed, w, h);
   }
   
   /**
    * Performs huffman encoding, making use of BitSets.
    * Does not currently work properly.
    *
    * @param img  image to be encoded
    * @param size size of chunks
    * @return representation of encoded image
    */
   public static Map.Entry<HashMap<BitSet, BitSet>, BitSet> huffmanEncode(int[][] img, int size) {
      // BitSet is used the same way as BitVector, but creating it should be
      // a bit easier
      BitSet data = flattenByte(img);
      BitSet val;
      HashMap<BitSet, Integer> hist = new HashMap<>();
      
      int count = 0;
      int s = img.length * img[0].length * 8;
      
      for (int i = 0; i < s; i += size) {
         val = data.get(i, i + size);
         val.set(size);
         
         if (!hist.containsKey(val)) {
            hist.put(val, 1);
         } else {
            hist.replace(val, hist.get(val) + 1);
         }
         
         count++;
      }
      
      // get values sorted by number of occurrences
      Object[] sortedEntries = hist.entrySet()
              .stream()
              .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
              .map(a -> a.getKey())
              .toArray();
      
      // use sorted values to make dictionary of generated codes
      HashMap<BitSet, BitSet> dict = new HashMap<>();
      
      PriorityQueue<Map.Entry<BitSet, Integer>> huffmanTree = new PriorityQueue<>(
              Comparator.comparingInt(
                      a -> -a.getValue()));
      
      
      for (Object e : hist.entrySet()) {
         huffmanTree.add((Map.Entry<BitSet, Integer>) e);
      }
      
      HashMap<BitSet, BitSet> table = new HashMap<>();
      huffman(sortedEntries, table, 0);
      
      //dict.forEach((a,b) -> System.out.printf("%d %d\n",hist.get(b),a.length()));
      
      // generate new BitSet using codes
      BitSet compressed = new BitSet();
      BitSet code;
      int j;
      BitSet key;
      int i;
      j = 0;
      
      for (i = 0; i < s; i += size) {
         key = data.get(i, i + size);
         key.set(size);
         code = table.get(key);
         
         compressed.set(j, j + code.length(), false);
         
         // write code to compressed
         for (int k = 0; k < code.length() - 1; k++) {
            if (code.get(k)) {
               compressed.flip(j);
            }
            
            j++;
         }
      }
      
      Set newTable = table.entrySet().stream().map(
              a -> new AbstractMap.SimpleEntry<>(a.getValue(),
                                                 a.getKey())).collect(
              Collectors.toSet());
      
      table.clear();
      for (Object o : newTable) {
         table.put(((Map.Entry<BitSet, BitSet>) o).getKey(),
                   ((Map.Entry<BitSet, BitSet>) o).getValue());
      }
      
      return new AbstractMap.SimpleEntry<>(table, compressed);
   }
   
   /**
    * helper method to find left child in a heap
    *
    * @param i index of node
    * @return index of left child
    */
   private static int left(int i) {
      return i == 0 ? 1 : 2 * i;
   }
   
   /**
    * helper method to find right child in a heap
    *
    * @param i index of node
    * @return index of right child
    */
   private static int right(int i) {
      return i == 0 ? 2 : 2 * i + 1;
   }
   
   
   /**
    * Generates table for huffman codes from given array of BitSets
    *
    * @param arr   array representation of min-heap with regard to frequency of
    *              contained bitset
    * @param table table for codes to be recorded in
    * @param i     current index in array
    */
   private static void huffman(Object[] arr, HashMap<BitSet, BitSet> table, int i) {
      BitSet bs;
      
      
      switch (i) {
         case 0:
            bs = new BitSet(2);
            break;
         
         case 1:
            bs = new BitSet(3);
            bs.set(1);
            break;
         
         case 2:
            bs = new BitSet(3);
            bs.set(1, 3);
            break;
         
         case 3:
            bs = new BitSet(3);
            bs.set(0);
            break;
         
         case 4:
            bs = new BitSet(3);
            bs.set(0);
            bs.set(2);
            break;
         
         default:
            bs = new BitSet(i - 2);
            bs.set(0, i - 3);
            break;
      }
      
      // make sure trailing zeroes show up
      bs.set(huffmanLength(i));
      
      table.put((BitSet) arr[i], bs);
      
      if (left(i) < arr.length) {
         huffman(arr, table, left(i));
      }
      
      // reverse table
      
      if (right(i) < arr.length) {
         huffman(arr, table, right(i));
      }
   }
   
   /**
    * Determine the length of the huffman code given index in heap.
    *
    * @param i index of code
    * @return length of code
    */
   private static int huffmanLength(int i) {
      return i == 0 ? 2 : Math.max(3, i - 2);
   }
   
   /**
    * Encodes given image with rlc, rlc of bit planes, and huffman coding
    * Prints out compression ratio and time elapsed.
    *
    * @param image image to be tested
    */
   public static void testEncodings(int[][] image) {
      int[] outImage;
      long start, stop;
      
      // test rlc
      start = System.currentTimeMillis();
      outImage = rlcEncode(image);
      stop = System.currentTimeMillis();
      System.out.printf("RLC:\nCompression ratio: %f\nTime elapsed: %dms\n",
                        (float) outImage.length / image.length / image[0].length,
                        (int) (stop - start));
      
      // test bitplane rlc
      start = System.currentTimeMillis();
      outImage = rlcEncodeBitplanes(image);
      stop = System.currentTimeMillis();
      System.out.printf(
              "RLC Bit-planes:\nCompression ratio: %f\nTime elapsed: %dms\n",
              (float) outImage.length / image.length / image[0].length,
              (int) (stop - start));
      
      // test huffman
      start = System.currentTimeMillis();
      Map.Entry<HashMap<BitSet, BitSet>, BitSet> huffmanResult = huffmanEncode(
              image, 2);
      stop = System.currentTimeMillis();
      System.out.printf(
              "Huffman coding:\nCompression ratio: %f\nTime elapsed: %dms\n",
              (float) huffmanResult.getValue().length() / 8 / image.length / image[0].length,
              (int) (stop - start));
   }
   
   /**
    * tests encoding methods with image
    *
    * @param args not used
    * @throws IOException thrown if image is not found
    */
   public static void main(String[] args) throws IOException {
      int[][] img = GrayscaleImage.readBufferedImage(
              ImageIO.read(new File("lena.jpg")));
      
      testEncodings(img);
   }
}
