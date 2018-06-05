/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Project
 * Description: Implementation of similar reflectance prior based facial
 * illumination transfer
 */

package project;

import java.util.function.BinaryOperator;

public class FloatFilters {
   public static float[][] guided(float[][] guide, float[][] img, float[][] r, float eps) {
      // based on Xuanwu Yin's c++ implementation
      // original: https://github.com/scimg/GuidedFilter/blob/master/GuidedFilter/GuidedFilter/GuidedFilter.cpp
      
      float[][] meanB, meanP, bgr;
      
      bgr = img;
      meanB = mean(guide, r);
      meanP = mean(img, r);
      
      // covariance of (guide, image)
      
      float[][] covBP;
      float[][] tmpBP, meanBP;
      
      tmpBP = Util.mul(bgr, img);
      meanBP = mean(tmpBP, r);
      tmpBP = Util.mul(meanB, meanP);
      covBP = Util.sub(meanBP, tmpBP);
      
      // variance of guide
      float[][] varBB;
      float[][] tmpBB, meanBB;
      
      tmpBB = Util.mul(bgr, bgr);
      meanBB = mean(tmpBB, r);
      tmpBB = Util.mul(meanB, meanB);
      varBB = Util.sub(meanBB, tmpBB);
      
      // a and b
      float[][] Ab, B, Abb, BB;
      
      varBB = Util.add(varBB, eps);
      Ab = Util.div(covBP, varBB);
      Abb = mean(Ab, r);
      
      Ab = Util.mul(Abb, bgr);
      
      return Util.add(Ab, meanP);
   }
   
   public static float[][] mean(float[][] img, int radius) {
      float[][] out = new float[img.length][img[0].length];
      float[][] kernel = new float[radius * 2 + 1][radius * 2 + 1];
      
      // build kernel
      for (int i = 0; i < kernel.length; i++) {
         for (int j = 0; j < kernel.length; j++) {
            kernel[i][j] = 1f / kernel.length / kernel.length;
         }
      }
      
      // convol
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            out[i][j] = convol(img, kernel, i - radius, j - radius);
         }
      }
      
      return out;
   }
   
   public static float[][] getReflectanceMask(float[][] img) {
      float[][] a = new float[img.length][img[0].length];
      float[][] b = new float[img.length][img[0].length];
      
      // copy img to a and b and threshold
      for (int i = 0; i < a.length; i++) {
         System.arraycopy(img[i], 0, a[i], 0, img[i].length);
         System.arraycopy(img[i], 0, b[i], 0, img[i].length);
         
         for (int j = 0; j < img[i].length; j++) {
            a[i][j] = a[i][j] > 225 / 255f ? 1 : 0;
            b[i][j] = b[i][j] > 150 / 255 ? 0 : 0;
         }
      }
      
      // apply max filter several times to grow mask
      for (int i = 0; i < 4; i++) {
         a = max(a, 5);
         b = max(a, 5);
      }
      
      // combine
      a = Util.or(a, b);
      a = Util.mul(a, 5);
      a = Util.add(a, 5);
      
      return a;
   }
   
   private static float[][] max(float[][] img, int size) {
      return genericFilter(img, size,
                           // add value of each pixel to a running sum
                           (a, b) -> a + b,
                           // sum should be divided by number of values added
                           (a, b) -> a / b,
                           // running sum should be initialized to 0
                           0);
   }
   
   private static float[][] genericFilter(float[][] img, int size,
                                          BinaryOperator<Double> inc,
                                          BinaryOperator<Double> adjust,
                                          double defValue) {
      double runningSum;
      int x, y;
      double coef;
      float[][] out = new float[img.length][img[0].length];
      
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
   
   public static float[][] mean(float[][] img, float[][] radius) {
      float[][] out = new float[img.length][img[0].length];
      
      for (int x = 0; x < img.length; x++) {
         for (int y = 0; y < img[x].length; y++) {
            float sum = 0;
            int count = 0;
            
            
            for (float i = x - radius[x][y]; i < x + radius[x][y]; i++) {
               for (float j = y - radius[x][y]; j < y + radius[x][y]; j++) {
                  if (i > -1 && i < img.length && j > -1 && j < img[(int) i].length) {
                     sum += img[(int) i][(int) j];
                     count += 1;
                  }
               }
            }
            
            out[x][y] = sum / count;
         }
      }
      
      return out;
   }
   
   private static float convol(float[][] img, float[][] kernel, int x, int y) {
      float sum = 0;
      
      for (int i = 0; i < kernel.length; i++) {
         for (int j = 0; j < kernel[i].length; j++) {
            if (x + i > -1 && x + i < img.length && y + j > -1 && y + j < img[x + i].length) {
               sum += kernel[i][j] * img[i + x][j + y];
            }
         }
      }
      
      return sum;
   }
   
   public static float[][] normalize(float[][] img) {
      float max;
      
      max = img[0][0];
      
      for (float[] r : img) {
         for (float f : r) {
            
            if (f > max) {
               max = f;
            }
         }
      }
      
      System.out.println(max);
      
      return Util.mul(img, 1f / max);
   }
}
