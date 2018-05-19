public class FloatFilters {
    public static float[][] mean(float[][] img, int radius) {
        float[][] out = new float[img.length][img[0].length];
        float[][] kernel = new float[radius * 2 + 1][radius*2 + 1];

        // build kernel
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel.length; j++) {
                kernel[i][j] = 1f / kernel.length / kernel.length;
            }
        }

        // convol
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                out[i][j] = convol(img,kernel,i-radius,j-radius);
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

    private static float convol(float[][] img, float[][] kernel, int x, int y) {
        float sum = 0;

        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel[i].length; j++) {
                if (x+i>-1 && x+i<img.length && y+j>-1 && y+j<img[x+i].length){
                    sum += kernel[i][j] * img[i+x][j+y];
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
