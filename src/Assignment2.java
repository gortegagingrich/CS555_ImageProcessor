/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 2
 * Description: Implementation of several basic spatial filters
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by gabriel on 4/17/18.
 */
public class Assignment2 {
   
   /**
    * Performs global histogram equalization.
    * Modifies and returns passed matrix of pixels.
    *
    * @param img matrix of pixels representing image
    * @return Modified img
    */
   public static int[][] globalHE(int[][] img) {
      double[] sourceValCounts = new double[(255 >> (8 - MainWindow.bitDepth)) + 1];
      
      // count grayscale values
      for (int i = 0; i < img.length; i++) {
         for (int v : img[i]) {
            sourceValCounts[v] += 1;
         }
      }
      
      int total = 0;
      
      for (int i = 0; i < sourceValCounts.length; i++) {
         total += sourceValCounts[i];
         sourceValCounts[i] = total;
      }
      
      for (int i = 0; i < sourceValCounts.length; i++) {
         sourceValCounts[i] = (0xFF >> (8 - MainWindow.bitDepth)) * sourceValCounts[i] / total;
      }
      // adjust grayscale values in image
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            img[i][j] = (int) sourceValCounts[img[i][j]];
         }
      }
      
      return img;
   }
   
   /**
    * Performs local histogram equalization on image.
    * Does not modify original matrix of pixels
    *
    * @param img matrix of pixels representing image
    * @param n   size of local region
    * @return new matrix of pixels
    */
   public static int[][] localHE(int[][] img, int n) {
      int[][] out = new int[img.length][img[0].length];
   
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            out[i][j] = localHEHelper(img, i - n / 2, j - n / 2, n);
         }
      }
      
      return out;
   }
   
   /**
    * For the defined region, calculates an equalized histogram and returns
    * the converted value for the center pixel.
    *
    * @param img matrix of pixels representing image
    * @param x   x-position of top left pixel of region
    * @param y   y-position of top left pixel of region
    * @param n   region size
    * @return new value for center pixel
    */
   private static int localHEHelper(int[][] img, int x, int y, int n) {
      // <gray value, count>
      // using a map to reduce size due to needing to create the set MxN times
      HashMap<Integer, Double> hist = new HashMap<>();
      int count = 0;
      
      // count values
      for (int i = x; i < x + n; i++) {
         for (int j = y; j < y + n; j++) {
            
            // if it's a valid position in the image
            if (i > -1 && i < img.length && j > -1 && j < img[i].length) {
               // keep a running count of valid pixels
               // should usually be n*n in the end
               count++;
               
               int val = img[i][j];
               
               if (hist.containsKey(val)) {
                  hist.replace(val, hist.get(val) + 1);
               } else {
                  hist.put(val, 1.0);
               }
            }
         }
      }
      
      // make get sorted entries
      Object[] entries = hist.entrySet()
              .stream()
              .sorted(Comparator.comparingInt(Entry::getKey)).toArray();
      
      double sum = 0;
      
      // determine what new gray level should be mapped to the original values
      for (Object e : entries) {
         sum += ((Entry<Integer, Double>) e).getValue();
         ((Entry<Integer, Double>) e).setValue(
                 sum / count * (0xFF >> (8 - MainWindow.bitDepth)));
      }
      
      // print histogram
      //hist.forEach((i, j) -> System.out.printf("%d: %d\n", i, j));
      
      int i = 0;
      
      while (((Entry<Integer, Double>) entries[i]).getKey() != img[x + n / 2][y + n / 2]) {
         i += 1;
      }
      
      return ((Entry<Integer, Double>) entries[i]).getValue().intValue();
   }
   
   /**
    * Disables bitplanes using bitwise AND.
    * Modifies image passed to function.
    *
    * @param img    matrix of pixels representing image
    * @param planes mask used to disable bitplanes
    * @return updated original matrix
    */
   public static int[][] setBitPlanes(int[][] img, int planes) {
      int i, j;
      
      for (i = 0; i < img.length; i++) {
         for (j = 0; j < img[i].length; j++) {
            img[i][j] &= planes;
         }
      }
      
      return img;
   }
   
   /**
    * Sharpening filter using Laplacian kernel.
    * Does not modify original matrix of pixels.
    *
    * @param img  matrix of pixels representing image
    * @param size size of laplacian kernel
    * @return Filtered matrix of pixels
    */
   public static int[][] laplacianSharpen(int[][] img, int size) {
      // apply laplacian filter to image
      int[][] filteredImage = convol(img, generateLaplacian(size), -1);
      
      // normalize filtered image
      filteredImage = normalize(filteredImage, 255);
      
      // add filter to image
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            filteredImage[i][j] += img[i][j];
         }
      }
      
      // normalize filtered image
      filteredImage = normalize(filteredImage, 255);
      
      return filteredImage;
   }
   
   /**
    * Performs convolution on given image with given kernel and constant c.
    * Does not modify original matrix of pixels.
    *
    * @param img    matrix of pixels representing image
    * @param kernel kernel used to convol image
    * @return new matrix of pixels with convolution performed
    */
   public static int[][] convol(int[][] img, int[][] kernel, double c) {
      int accumulator;
      int[][] out;
      int x, y;
      
      int max = 0, min = 0;
      
      // needs to create a new matrix
      out = new int[img.length][img[0].length];
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            accumulator = applyKernel(img, kernel, i, j, c);
            
            if (accumulator > max) {
               max = accumulator;
            }
            
            if (accumulator < min) {
               min = accumulator;
            }
            
            out[i][j] = accumulator;
         }
      }
      
      return out;
   }
   
   /**
    * Generates simple Laplacian kernel.
    * Of the form: {{1,1,1},{1,-8,1},{1,1,1}}
    *
    * @param size
    * @return
    */
   public static int[][] generateLaplacian(int size) {
      int[][] kernel = new int[size][size];
      int center = size / 2;
      
      for (int i = 0; i < size; i++) {
         for (int j = 0; j < size; j++) {
            kernel[i][j] = (i == j && i == center) ? -1 * (size * size - 1) : 1;
         }
      }
      
      return kernel;
   }
   
   /**
    * Normalizes gray-levels in given image
    * Modifies matrix passed to function.
    *
    * @param img        Matrix of pixels representing image
    * @param upperBound Max value of output (normally 0xFF)
    * @return Normalized original matrix of pixels
    */
   private static int[][] normalize(int[][] img, int upperBound) {
      int max = Integer.MIN_VALUE;
      int min = Integer.MAX_VALUE;
      
      // find max and min
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            if (img[i][j] > max) {
               max = img[i][j];
            }
            
            if (img[i][j] < min) {
               min = img[i][j];
            }
         }
      }
      
      // use max and min to normalize
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            img[i][j] = (int) ((img[i][j] - min) * ((double) (upperBound) / (max - min)));
         }
      }
      
      return img;
   }
   
   /**
    * Gets center value when kernel is applied to a pixel in an image.
    *
    * @param img    Matrix of pixels representing image
    * @param kernel Kernel used for operation
    * @param x      x position of pixel in img
    * @param y      y position of pixel in img
    * @return new value for pixel based on applying kernel
    */
   private static int applyKernel(int[][] img, int[][] kernel, int x, int y, double c) {
      double accumulator = 0;
      int xx = x - kernel.length / 2;
      int yy = y - kernel[0].length / 2;
      
      for (int i = 0; i < kernel.length; i++) {
         for (int j = 0; j < kernel[i].length; j++) {
            // if in bounds for image
            if (xx + i > -1 && xx + i < img.length && yy + j > -1 && yy + j < img[xx + i].length) {
               accumulator += c * kernel[i][j] * img[xx + i][yy + j];
            }
         }
      }
      
      return (int) accumulator;
   }
   
   /**
    * Median filter.
    * Each pixel in output image is the median of local region in original.
    * Does not modify original img
    *
    * @param img  Matrix of pixels representing image
    * @param size size of local region
    * @return new Matrix of pixels representing filtered image
    */
   public static int[][] medianFilter(int[][] img, int size) {
      int[][] out = new int[img.length][img[0].length];
      
      for (int i = 0; i < out.length; i++) {
         for (int j = 0; j < out[i].length; j++) {
            out[i][j] = findMedian(img, i, j, size);
         }
      }
      
      return out;
   }
   
   /**
    * Finds median value of a local region.
    *
    * @param img  Matrix of pixels representing image
    * @param x    x position of center pixel of region
    * @param y    y position of center pixel of region
    * @param size size of local region
    * @return median value of local region
    */
   private static int findMedian(int[][] img, int x, int y, int size) {
      int med = 0, xx, yy;
      int[] acc = new int[size * size];
      
      for (int i = 0; i < size; i++) {
         for (int j = 0; j < size; j++) {
            xx = x + i - size / 2;
            yy = y + j - size / 2;
            
            if (xx > -1 && xx < img.length && yy > -1 && yy < img[0].length) {
               acc[med++] = img[xx][yy];
            }
         }
      }
      
      Arrays.sort(acc);
      med = Math.max(0, size * size / 2);
      
      return acc[med];
   }
   
   /**
    * Performs high-boost filtering on image.
    * Modifies original matrix of pixels passed to function.
    *
    * @param img  Matrix of pixels representing image
    * @param size size of local region used for smoothing
    * @param a    coefficient multiplied with mask (unsharp filter if a==1)
    * @return filtered img
    */
   public static int[][] highBoostingFilter(int[][] img, int size, int a) {
      // blur image
      int[][] filtered = smooth(img, size);
      
      // subtract from original to get mask
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            // subtract from original to get mask
            filtered[i][j] = img[i][j] - filtered[i][j];
            // add mask back to original
            img[i][j] += a * filtered[i][j];
         }
      }
      
      img = normalize(img, 0xFF >> (MainWindow.bitDepth - 8));
      
      return img;
   }
   
   /**
    * Smoothing filter using local mean.
    * Does not modify original matrix of pixels.
    *
    * @param img  Matrix of pixels representing image
    * @param size size of local region
    * @return new image convoled with kernel of 1s
    */
   public static int[][] smooth(int[][] img, int size) {
      // initialize kernel to all 1's
      int[][] kernel = new int[size][size];
      
      for (int[] row : kernel) {
         for (int i = 0; i < row.length; i++) {
            row[i] = 1;
         }
      }
      
      // convol with kernel and coefficient based on size to find local average
      return convol(img, kernel, 1.0 / (size * size));
   }
}
