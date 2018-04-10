/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 1
 * Description: Implementation of several basic algorithms for changing spatial
 * and gray-scale resolution
 */

public class Assignment1 {
   /**
    * Uses nearest neighbor interpolation to change the spatial resolution
    * of the given matrix of pixels
    *
    * @param img matrix of pixels for input image
    * @return spatially scaled matrix of pixels
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

   /**
    * Changes the grayscale resolution of the given matrix of pixels.
    * Uses static variable in MainWindow to get source depth.
    * Output is the same object to speed up the process because
    * the spatial resolution is maintained
    *
    * @param img input image as matrix of pixels
    * @param outDepth destination bit depth
    * @return representation of image with gray-scale resolution changed
    */
   public static int[][] changeBitDepth(int[][] img, int outDepth) {
      for (int i = 0; i < img.length; i++) {
         for (int j = 0; j < img[i].length; j++) {
            img[i][j] = convertGrayscaleBit(img[i][j], MainWindow.bitDepth, outDepth);
         }
      }
      
      MainWindow.bitDepth = outDepth;
      
      return img;
   }

   /**
    * Convert's gray-scale resolution of given pixel
    *
    * @param gsIn input gray-scale value
    * @param depthIn input gray-scale depth
    * @param depthOut output gray-scale depth
    * @return new gray-scale value
    */
   private static int convertGrayscaleBit(int gsIn, int depthIn, int depthOut) {
      double ratio = (double) gsIn / ((1 << (depthIn)) - 1);
      int max = (1 << depthOut) - 1;
      gsIn = (int) Math.round(ratio * max);
      
      return gsIn;
   }

   /**
    * Scales given matrix of pixels with either horizontal
    * or vertical linear interpolation
    *
    * @param img matrix of pixels
    * @param width output width
    * @param height output height
    * @param horizontal true -> horizontal linear interpolation;
    *                   false -> vertical linear interpolation
    * @return spatially scaled matrix of pixels
    */
   public static int[][] linearInterpolation(int[][] img, int width, int height, boolean horizontal) {
      int[][] out;
      
      width = Math.max(width, 1);
      height = Math.max(height, 1);
      
      out = new int[width][height];
      
      if (horizontal) {
         horizontalLinearInterp(img, out);
      } else {
         verticalLinearInterp(img, out);
      }
      
      return out;
   }

   /**
    * Spatially scales matrix of pixels first with horizontal
    * linear interpolation and then with vertical linear
    * interpolation.
    *
    * @param img input matrix of pixels
    * @param width output width
    * @param height output height
    * @return spatially scaled matrix of pixels
    */
   public static int[][] bilinearInterpolation(int[][] img, int width, int height) {
      // ensure output image is 1x1 or larger
      width = Math.max(width, 1);
      height = Math.max(height, 1);

      // perform linear interpolation horizontally and then vertically
      int[][] h = new int[width][img[0].length];
      int[][] v = new int[width][height];
      
      horizontalLinearInterp(img, h);
      verticalLinearInterp(h, v);
      
      return v;
   }
   
   /**
    * linearly interpolate looking at pixels in the row
    *
    * @param in  input matrix of pixels
    * @param out output matrix of pixels
    */
   private static void horizontalLinearInterp(int[][] in, int[][] out) {
      
      for (int i = 0; i < out[0].length; i++) {
         int y = Math.min((int) ((double) i / out[0].length * in[0].length),
                          in[0].length - 1);
         
         if (y >= in.length) {
            System.out.println(y);
         }

         double x;
         double x0;
         double dx0;
         double x1;
         double dx1;

         for (int j = 0; j < out.length; j++) {
            // relative location of destination pixel
            x = (double) j / out.length;
            // position of pixel to left
            x0 = Math.min(in.length - 1, Math.floor(
                    x * in.length));
            // weight of pixel to left
            dx0 = x - x0 / in.length;
            // position of pixel to right
            x1 = Math.min(in.length - 1, Math.ceil(
                    x * in.length));
            // weight of pixel to right
            dx1 = x1 / in.length - x;

            out[j][i] = (x0 == x1) ?
                        (in[(int) x0][y]) :
                        (int) ((1 - dx0 / (dx0 + dx1)) * in[(int) x0][y] + (1 - dx1 / (dx0 + dx1)) * in[(int) x1][y]);
         }
      }
   }

   /**
    * linearly interpolate looking at pixels in the column
    *
    * @param in  input matrix of pixels
    * @param out output matrix of pixels
    */
   private static void verticalLinearInterp(int[][] in, int[][] out) {
      int x;
      double y;
      double y0;
      double dy0;
      double y1;
      double dy1;

      for (int i = 0; i < out.length; i++) {
         x = Math.min((int) ((double) i / out.length * in.length),
                          in.length - 1);

         for (int j = 0; j < out[0].length; j++) {
            y = (double) j / out[0].length;  // relative location of destination pixel
            y0 = Math.min(in[0].length - 1, Math.floor(
                    y * in[0].length)); // position of pixel above
            dy0 = y - y0 / in[0].length; // weight of pixel above
            y1 = Math.min(in[0].length - 1, Math.ceil(
                    y * in[0].length)); // position of pixel below
            dy1 = y1 / in[0].length - y; // weight of pixel below
            
            out[i][j] = (y0 == y1) ?
                        (in[x][(int) y0]) :
                        (int) ((1 - dy0 / (dy0 + dy1)) * (in[x][(int) y0]) + (1 - dy1 / (dy0 + dy1)) * (in[x][(int) y1]));
         }
      }
   }
}
