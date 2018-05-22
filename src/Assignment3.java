import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class Assignment3 {
   
   /**
    * Smoothing filter that uses arithmetic mean of local region
    *
    * @param img image to filter
    * @param size size of local region
    * @return filtered image
    */
   public static int[][] arithMeanFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           // add value of each pixel to a running sum
                           (a, b) -> a + b,
                           // sum should be divided by number of values added
                           (a, b) -> a / b,
                           // running sum should be initialized to 0
                           0);
   }

   /**
    * Generic filter filter that takes operations to perform at certain
    * steps.  This only exists because I got tired of writing out the
    * same set of control structures over and over.
    *
    * @param img image to filter
    * @param size size of local region (gets passed to function)
    * @param inc BinaryOperator that gets called at every pixel in each local
    *            region.
    *            The two arguments it takes are an internal double containing
    *            a running sum, and the value of the current pixel
    * @param adjust BinaryOperator that gets called after iterating through
    *               each local region.
    *               The two arguments it takes are the number of valid pixels
    *               and the running sum.
    * @param defValue Default value for the running sum
    *                 This will usually be 0, but it can be 1 in some cases
    * @return filtered image
    */
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

   /**
    * Filter that uses the geometric means of local regions
    *
    * @param img image to filter
    * @param size size of local region
    * @return filtered image
    */
   public static int[][] geomMeanFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           // uses a geometric sum instead
                           (a, b) -> a * b,
                           // final value should be (geometricSum)^(1/numVals)
                           (a, b) -> Math.pow(a, 1 / b),
                           // running geometric sum should be initialized to 1
                           1);
   }

   /**
    * Filter that uses the harmonic mean of local regions.
    *
    * @param img image to filter
    * @param size size of the local regions
    * @return filtered image
    */
   public static int[][] harmonicMeanFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           // add 1/value to running sum
                           (a, b) -> a + 1 / b,
                           // final value is numVals / runningSum
                           (a, b) -> b / a,
                           // running sum should be initialized to 0
                           0);
   }

   /**
    * Filter that uses the contraharmonic mean of local regions
    *
    * @param img image to be filtered
    * @param size size of local regions
    * @return filtered image
    */
   public static int[][] contraharmonicMeanFilter(int[][] img, int size) {
      ArrayList<BinaryOperator<Object>> list = new ArrayList<>();

      // Needs to keep track of two values
      // first is sum + val^2
      list.add((a, b) -> (Double)a + (Double)b * (Double)b);
      // second is sum + val
      list.add((a, b) -> (Double)a + (Double)b);
      
      return genericFilter(img, size,
                           list,
                           // final result is the first running "sum"
                           // divided by the second
                           a -> (Double)a[0] / (Double)a[1],
                           () -> 0.0);
      
   }

   /**
    * Different generic filter that allows for multiple functions to be called
    * for each pixel in each local region.
    * The values are kept in an array with an order corresponding to the list
    * of BinaryOperators.
    * Structurally similar to other genericFilter method.
    *
    * @param img image to filter
    * @param size size of local region
    * @param step list of BinaryOperators to be applied for each pixel
    *             in each local region.
    * @param adjust Function that takes a double[] containing the running
    *               "sums" corresponding to each BinaryOperator in step.
    * @param defValue default value of each running "sum"
    * @return filtered image
    */
   private static int[][] genericFilter(int[][] img, int size,
                                        List<BinaryOperator<Object>> step,
                                        Function<Object[], Double> adjust,
                                        Supplier<Object> defValue) {
      Object[] vals = new Object[step.size()];
      
      int[][] out = new int[img.length][img[0].length];
      size /= 2;
      
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            // set vals to -1
            for (int k = 0; k < vals.length; k++) {
               vals[k] = defValue.get();
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

   /**
    * Filter that uses local maxima
    *
    * @param img image to be filtered
    * @param size size of local regions
    * @return filtered image
    */
   public static int[][] maxFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           // keeps track of max instead of sum
                           (a, b) -> b > a ? b : a,
                           // does not adjust max
                           (a, b) -> a,
                           Double.MIN_VALUE);
   }

   /**
    * Filter using local minima
    *
    * @param img image to be filtered
    * @param size size of local regions
    * @return filtered image
    */
   public static int[][] minFilter(int[][] img, int size) {
      return genericFilter(img, size,
                           // keeps track of min instead of sum
                           (a, b) -> b < a ? b : a,
                           // does not adjust min
                           (a, b) -> a,
                           Double.MAX_VALUE);
   }

   /**
    * Filter using local midpoints
    *
    * @param img image to be filtered
    * @param size size of local region
    * @return filtered image
    */
   public static int[][] midpointFilter(int[][] img, int size) {
      ArrayList<BinaryOperator<Object>> list = new ArrayList<>();
      
      // first stored value is max
      list.add((a, b) -> (Double)b > (Double)a ? b : a);
      // second stored value is min
      list.add((a, b) -> (Double)b < (Double)a ? b : a);
      
      return genericFilter(img, size, list,
                           // final value is midpoint of max and min
                           x -> ((Double)x[0] + (Double)x[1]) * 0.5,
              () -> 0.0);
   }

   /**
    * Filter using local alpha-trimmed means
    *
    * @param img image to filter
    * @param size size of local region
    * @param d number of values to trim
    * @return
    */
   public static int[][] alphaTrimmedMeanFilter(int[][] img, int size, final int d) {
      ArrayList<BinaryOperator<Object>> list = new ArrayList<>();
      final double localSize = size * size - d;

      // add an item to given list
      list.add((x,a) -> {
         ((ArrayList<Double>)x).add((Double)a);
         return x;
      });
      
      return genericFilter(img, size, list,
                           // sorts list, and finds means of middle values
                           x -> {
                              ArrayList<Double> l = (ArrayList<Double>)x[0];
                              l.sort(Double::compare);
                              int d0 = d - d/2;
                              double sum = 0;
                              for (int i = d0; i < l.size() - (d - d0); i++) {
                                 sum += l.get(i);
                              }
                              return sum / (l.size() - d);
                           },
              () -> new ArrayList<Double>());
   }
}
