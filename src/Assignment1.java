public class Assignment1 {
   /**
    * @param img matrix of pixels for input image
    * @return matrix of pixels for output image
    */
   public static int[][] scaleNearestNeighbor(int[][] img, int width, int height) {
      width = width > 0 ? width : 1;
      height = height > 0 ? height : 1;
      
      int sourceWidth, sourceHeight;
      int x, y;
      int[][] out;
      
      out = new int[width][height];
      sourceWidth = img.length;
      sourceHeight = img[0].length;
      
      for (int i = 0; i < width; i++) {
         x = (int) ((double) i / width * sourceWidth);
         
         for (int j = 0; j < height; j++) {
            y = (int) ((double) j / height * sourceHeight);
            out[i][j] = img[x][y];
         }
      }
      
      return out;
   }
   
   public static int[][] changeBitDepth(int[][] img, int outDepth) {
      int[][] out = new int[img.length][img[0].length];
      
      int inDepth = MainWindow.bitDepth;
      
      for (int i = 0; i < out.length; i++) {
         for (int j = 0; j < out[i].length; j++) {
            out[i][j] = convertGrayscaleBit(img[i][j], inDepth, outDepth);
         }
      }
      
      MainWindow.bitDepth = outDepth;
      
      return out;
   }
   
   private static int convertGrayscaleBit(int rgb, int depthIn, int depthOut) {
      double ratio = (double) rgb / ((1 << (depthIn)) - 1);
      int max = (1 << depthOut) - 1;
      rgb = (int) Math.round(ratio * max);
      
      return rgb;
   }
   
   public static int[][] linearInterpolation(int[][] img, int width, int height, boolean horizontal) {
      int[][] out;
      
      width = Math.max(width, 1);
      height = Math.max(height, 1);
      
      out = new int[width][height];
      
      if (horizontal) {
         lerpHorizontal(img, out);
      } else {
         lerpVerical(img, out);
      }
      
      return out;
   }
   
   public static int[][] bilinearInterpolation(int[][] img, int width, int height) {
      int[][] out;
      
      width = Math.max(width, 1);
      height = Math.max(height, 1);
      
      out = new int[width][height];
      
      int[][] h = new int[width][height];
      int[][] v = new int[width][height];
      
      lerpHorizontal(img, h);
      lerpVerical(img, v);
      
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            int a = h[i][j];
            int b = v[i][j];
            
            out[i][j] = (a + b) / 2;
         }
      }
      
      return out;
   }
   
   /**
    * linearly interpolate looking at pixels in the row
    *
    * @param in  input image
    * @param out output image
    */
   private static void lerpHorizontal(int[][] in, int[][] out) {
      
      for (int i = 0; i < out[0].length; i++) {
         int y = Math.min((int) ((double) i / out[0].length * in[0].length),
                          in[0].length - 1);
         
         if (y >= in.length) {
            System.out.println(y);
         }
         
         for (int j = 0; j < out.length; j++) {
            double x = (double) j / out.length;  // relative location of destination pixel
            double x0 = Math.min(in.length - 1, Math.floor(
                    x * in.length)); // position of pixel to left
            double dx0 = x - x0 / in.length; // weight of pixel to left
            double x1 = Math.min(in.length - 1, Math.ceil(
                    x * in.length)); // position of pixel to right
            double dx1 = x1 / in.length - x; // weight of pixel to right
            
            out[j][i] = (x0 == x1) ?
                        (in[(int) x0][y]) :
                        (int) ((1 - dx0 / (dx0 + dx1)) * in[(int) x0][y] + (1 - dx1 / (dx0 + dx1)) * in[(int) x1][y]);
         }
      }
   }
   
   private static void lerpVerical(int[][] in, int[][] out) {
      for (int i = 0; i < out.length; i++) {
         int y = Math.min((int) ((double) i / out.length * in.length),
                          in.length - 1);
         
         for (int j = 0; j < out[0].length; j++) {
            double x = (double) j / out[0].length;  // relative locationof destination pixel
            double x0 = Math.min(in[0].length - 1, Math.floor(
                    x * in[0].length)); // position of pixel to left
            double dx0 = x - x0 / in[0].length; // weight of pixel to left
            double x1 = Math.min(in[0].length - 1, Math.ceil(
                    x * in[0].length)); // position of pixel to right
            double dx1 = x1 / in[0].length - x; // weight of pixel to right
            
            out[i][j] = (x0 == x1) ?
                        (in[y][(int) x0] & 0xFF) :
                        (int) ((1 - dx0 / (dx0 + dx1)) * (in[y][(int) x0] & 0xFF) + (1 - dx1 / (dx0 + dx1)) * (in[y][(int) x1] & 0xFF)) & 0xFF;
         }
      }
   }
}
