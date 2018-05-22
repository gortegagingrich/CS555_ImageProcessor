import Jama.Matrix;

import java.util.Arrays;

public class FloatFilters {
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
   
   public static float[][] guided(float[][] guide, float[][] img, int r, float eps) {
      // based on Xuanwu Yin's c++ implementation
      // original: https://github.com/scimg/GuidedFilter/blob/master/GuidedFilter/GuidedFilter/GuidedFilter.cpp
      
      float[][] meanB, meanP, bgr;
      
      bgr = img;
      meanB = mean(guide, r);
      meanP = mean(img, r);
      
      // covariance of (guide, image)
      
      float[][] covBP, covGP, covRP;
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
   
   public float[][] wls(float[][] in, float lambda, float alpha, float[][] l) {
      float smallNum = 0.0001f;
      int r, c, k;
      r = in.length;
      c = in[0].length;
      k = r * c;
      
      double[][] inDouble, lDouble;
      
      inDouble = new double[r][c];
      lDouble = new double[r][c];
      
      Matrix IN = new Matrix(inDouble);
      Matrix L = new Matrix(lDouble);
      
      // compute affinities
      float[][] dyMat;
      float[] dy;
      dyMat = diff(l, 1, 1);
      float[][] temp0 = pow(dyMat, alpha);
      temp0 = Util.add(temp0, smallNum);
      dyMat = Util.div(-lambda, temp0);
      dyMat = padMatrix(dyMat, 0, 1, false);
      dy = flatten(dyMat);
      
      float[][] dxMat;
      float[] dx;
      dxMat = diff(l, 1, 2);
      temp0 = pow(dxMat, alpha);
      temp0 = Util.add(temp0, smallNum);
      dxMat = Util.div(-lambda, temp0);
      dxMat = padMatrix(dxMat, 1, 0, false);
      dx = flatten(dxMat);
      
      // construct 5-point spatially inhomogeneous laplacian matrix
      float[][] B = new float[2][];
      B[0] = dx;
      B[1] = dy;
      int[] d = new int[]{-r, -1};
      Matrix A = spdiags(B, d, k, k);
      
      float[] e = dx;
      float[] w = padArray(dx, r, true);
      w = Arrays.copyOfRange(w, 0, w.length - r - 1);
      float[] s = dy;
      float[] n = padArray(dy, 1, true);
      n = Arrays.copyOfRange(n, 0, n.length - 2);
      
      // D = 1 - (e + w + s + n);
      
      return null;
   }
   
   private float[] padArray(float[] dx, int r, boolean pre) {
      float[] out = new float[dx.length + r];
      
      for (int i = 0; i < out.length; i++) {
         if (pre && i >= r) {
            out[i] = dx[i - r];
         } else if (!pre && i < dx.length) {
            out[i] = dx[i];
         } else {
            out[i] = 0;
         }
      }
      
      return out;
   }
   
   private static Matrix spdiags(float[][] b, int[] d, int k, int k1) {
      
      return null;
   }
   
   private float[] flatten(float[][] mat) {
      int i = 0;
      float[] out = new float[mat.length * mat[0].length];
      
      for (float[] r : mat) {
         for (float c : r) {
            out[i++] = c;
         }
      }
      
      return out;
   }
   
   private float[][] padMatrix(float[][] mat, int r, int c, boolean pre) {
      float[][] out = new float[mat.length + r][mat.length + c];
      int startR, startC, endR, endC;
      
      startR = pre ? r : 0;
      endR = mat.length + (pre ? r : 0);
      
      startC = pre ? c : 0;
      endC = mat[0].length + (pre ? c : 0);
      
      for (int i = 0; i < out.length; i++) {
         for (int j = 0; j < out[i].length; j++) {
            if (i >= startR && i <= endR && j >= startC && j <= endC) {
               out[i][j] = mat[i - startR][j - startC];
            } else {
               out[i][j] = 0;
            }
         }
      }
      
      return out;
   }
   
   private float[][] pow(float[][] mat, float p) {
      float[][] out = new float[mat.length][mat[0].length];
      
      for (int i = 0; i < out.length; i++) {
         for (int j = 0; j < out[i].length; j++) {
            out[i][j] = (float) Math.pow(mat[i][j], p);
         }
      }
      
      return out;
   }
   
   private float[][] abs(float[][] mat) {
      return mat;
   }
   
   private float[][] xor(float[][] mat, float c) {
      return mat;
   }
   
   private float[][] diff(float[][] mat, int a, int b) {
      return mat;
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
   
   private static float[][] normalize(float[][] img) {
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
