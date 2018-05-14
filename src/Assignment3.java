import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Assignment3 {
   
   /**
    * @param img
    * @param size size of window
    * @return
    */
   public static int[][] arithMeanFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           (a, b) -> a + b,
                           (a, b) -> a / b,
                           0);
   }
   
   private static int[][] genericFilter(int[][] img, int size,
                                        BinaryOperator<Double> inc,
                                        BinaryOperator<Double> adjust,
                                        double defValue) {
      double runningSum;
      int x, y;
      double coef;
      int[][] out = new int[img.length][img[0].length];
      
      size /= 2;
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            // init running sum to 0
            runningSum = defValue;
            coef = 0;
            
            for (int k = -size; k <= size; k++) {
               for (int l = -size; l <= size; l++) {
                  x = i + k;
                  y = j + l;
                  
                  if (x > -1 && x < img.length && y > -1 && y < img[i].length) {
                     runningSum = inc.apply(runningSum, (double) img[x][y]);
                     coef += 1;
                  }
               }
            }
            
            out[i][j] = adjust.apply(runningSum, coef).intValue();
         }
      }
      
      return out;
   }
   
   public static int[][] geomMeanFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           (a, b) -> a * b,
                           (a, b) -> Math.pow(a, 1 / b),
                           1);
   }
   
   public static int[][] harmonicMeanFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           (a, b) -> a + 1 / b,
                           (a, b) -> b / a,
                           0);
   }
   
   public static int[][] contraharmonicMeanFilter(int[][] img, int size) {
      ArrayList<BinaryOperator<Double>> list = new ArrayList<>();
      
      list.add((a, b) -> a + b * b);
      list.add((a, b) -> a + b);
      
      return genericFilter(img, size,
                           list,
                           a -> a[0] / a[1],
                           0);
      
   }
   
   private static int[][] genericFilter(int[][] img, int size,
                                        List<BinaryOperator<Double>> step,
                                        Function<double[], Double> adjust,
                                        double defValue) {
      double[] vals = new double[step.size()];
      
      int[][] out = new int[img.length][img[0].length];
      size /= 2;
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            // set vals to -1
            for (int k = 0; k < vals.length; k++) {
               vals[k] = defValue;
            }
            
            for (int x = -size; x <= size; x++) {
               for (int y = -size; y <= size; y++) {
                  // set vals to result of corresponding binary operator
                  if (i + x > -1 && i + x < img.length && j + y > -1 && j + y < img[i].length) {
                     for (int k = 0; k < vals.length; k++) {
                        vals[k] = step.get(k).apply(vals[k],
                                                    (double) img[i + x][j + y]);
                     }
                  }
               }
            }
            
            out[i][j] = adjust.apply(vals).intValue();
         }
      }
      
      return out;
   }
   
   public static int[][] maxFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           (a, b) -> b > a ? b : a,
                           (a, b) -> a,
                           Double.MIN_VALUE);
   }
   
   public static int[][] minFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           (a, b) -> b < a ? b : a,
                           (a, b) -> a,
                           Double.MAX_VALUE);
   }
   
   public static int[][] midpointFilter(int[][] img, int size) {
      ArrayList<BinaryOperator<Double>> list = new ArrayList<>();
      
      // first stored value is max
      list.add((a, b) -> b > a ? b : a);
      // second stored value is min
      list.add((a, b) -> b < a ? b : a);
      
      return genericFilter(img, size, list,
                           x -> (x[0] + x[1]) * 0.5,
                           0);
   }
   
   public static int[][] alphaTrimmedMeanFilter(int[][] img, int size) {
      ArrayList<BinaryOperator<Double>> list = new ArrayList<>();
      final double localSize = size * size - 2;
      
      // first stored value is max
      list.add((a, b) -> b > a ? b : a);
      // second stored value is min
      list.add((a, b) -> b < a ? b : a);
      // third stored value is sum
      list.add((a, b) -> a + b);
      // count values added
      list.add((a, b) -> a + 1);
      
      return genericFilter(img, size, list,
                           x -> Math.min(0xFF >> (8 - MainWindow.bitDepth),
                                         (x[2] - x[0] - x[1]) / (x[3] - 2)),
                           0);
   }
}
