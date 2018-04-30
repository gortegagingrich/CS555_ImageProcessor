import java.util.HashMap;

/**
 * Created by gabriel on 4/17/18.
 */
public class Assignment2 {
   public static final int[][] LAPLACIAN_KERNEL = new int[][]{
           {0, 1, 0},
           {1, -4, 1},
           {0, 1, 1}
   };
   
   public static final int[][] IDENTITY_KERNEL = new int[][]{
           {0, 0, 0},
           {0, 1, 0},
           {0, 0, 0}
   };
   
   public static int[][] globalHE(int[][] img) {
      double[] sourceValCounts = new double[256 >> (8 - MainWindow.bitDepth)];
      
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
      int[][] filteredImage = convol(img, generateLaplaceKernel(3), 1, 1);
      
      int max, min;
      max = Integer.MIN_VALUE;
      min = Integer.MAX_VALUE;
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            img[i][j] += filteredImage[i][j];
            
            if (img[i][j] < min) {
               min = img[i][j];
            }
         }
      }
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            img[i][j] -= min;
            
            if (img[i][j] > max) {
               max = img[i][j];
            }
         }
      }
      
      min = 0xFF;
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            img[i][j] = (int) (img[i][j] * 255.0 / max);
            
            if (img[i][j] < min) {
               min = img[i][j];
            }
         }
      }
      
      return img;
   }
   
   /**
    * @param img
    * @param kernel
    * @param cx     x position of center of kernel
    * @param cy     y position of center of kernel
    * @return
    */
   public static int[][] convol(int[][] img, int[][] kernel, int cx, int cy) {
      int accumulator;
      int[][] out;
      int x, y;
      
      int max = 0, min = 0;
      
      // needs to create a new matrix
      out = new int[img.length][img[0].length];
      
      for (int i = cx; i < img.length - cx; i++) {
         for (int j = cy; j < img[i].length - cy; j++) {
            accumulator = 0;
            
            for (int m = 0; m < kernel.length; m++) {
               for (int n = 0; n < kernel[m].length; n++) {
                  // find corresponding pixel in image
                  x = m - cx + i;
                  y = n - cy + j;
                  
                  // determine if it's a valid pixel
                  if (x > -1 && x < img.length
                          && y > -1 && y < img[x].length) {
                     accumulator += img[x][y] * kernel[m][n];
                  }
               }
            }
            
            if (accumulator > max) {
               max = accumulator;
            }
            
            if (accumulator < min) {
               min = accumulator;
            }
            
            out[i][j] = accumulator;
         }
      }
      
      // fix grayscale values
      double range = max - min;
      
      for (int i = 0; i < out.length; i++) {
         for (int j = 0; j < out[i].length; j++) {
            out[i][j] = (int) ((out[i][j] - min) * 255.0 / range);
         }
      }
      
      return out;
   }
   
   public static int[][] generateLaplaceKernel(int size) {
      int[][] kernel = new int[size][size];
      
      for (int i = 0; i < size; i++) {
         for (int j = 0; j < size; j++) {
            kernel[i][j] = -1;
         }
      }
      
      kernel[size / 2][size / 2] = size * size - 1;
      
      return kernel;
   }
}
