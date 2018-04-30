import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by gabriel on 4/17/18.
 */
public class Assignment2 {
   public static final int[][] LAPLACIAN_KERNEL = new int[][]{
           {1, 1, 1},
           {1, -8, 1},
           {1, 1, 1}
   };
   
   public static final int[][] IDENTITY_KERNEL = new int[][]{
           {0, 0, 0},
           {0, 1, 0},
           {0, 0, 0}
   };
   
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
   
   public static int[][] localHE(int[][] img, int n) {
      int[][] out = new int[img.length][img[0].length];
      
      for (int i = 0; i < img.length - n; i++) {
         for (int j = 0; j < img[i].length - n; j++) {
            out[i][j] = localHEHelper(img, i, j, n);
         }
      }
      
      return out;
   }
   
   private static int localHEHelper(int[][] img, int x, int y, int n) {
      HashMap<Integer, Integer> hist = new HashMap<>(); // using a map to reduce size
      
      return img[x + n / 2][y + n / 2];
   }
   
   public static int[][] setBitPlanes(int[][] img, int planes) {
      int i, j;
      
      for (i = 0; i < img.length; i++) {
         for (j = 0; j < img[i].length; j++) {
            img[i][j] &= planes;
         }
      }
      
      return img;
   }
   
   public static int[][] laplacianSharpen(int[][] img) {
      // apply laplacian filter to image
      int[][] filteredImage = convol(img, LAPLACIAN_KERNEL, -1);
      
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
    * @param img
    * @param kernel
    * @return
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
    * @param img
    * @param kernel
    * @param x      pixel x position in image
    * @param y      pixel y position in image
    * @return
    */
   private static int applyKernel(int[][] img, int[][] kernel, int x, int y, double c) {
      int accumulator = 0;
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
      
      return accumulator;
   }
   
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
            img[i][j] = (int) ((img[i][j] - min) * ((double) (upperBound) / (max)));
         }
      }
      
      return img;
   }
   
   public static int[][] medianFilter(int[][] img, int size) {
      int[][] out = new int[img.length][img[0].length];
      
      for (int i = 0; i < out.length; i++) {
         for (int j = 0; j < out[i].length; j++) {
            out[i][j] = findMedian(img, i, j, size);
         }
      }
      
      return out;
   }
   
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
