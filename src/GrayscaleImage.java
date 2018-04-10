/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 1
 * Description: Implementation of several basic algorithms for changing spatial
 * and gray-scale resolution
 */

import java.awt.image.BufferedImage;
import java.util.function.Function;

class GrayscaleImage {
   // I'm using ints to avoid having to cast to bytes
   private int[][] pixels;
   private int width;
   private int height;
   
   /**
    * Creates GrayscaleImage by reading a buffered image pixel by pixel.
    * Assumes the source image is already a gray-scale image, so it only
    * deals with the red value of each pixel.
    *
    * @param img source image
    */
   public GrayscaleImage(BufferedImage img) {
      width = img.getWidth();
      height = img.getHeight();
      pixels = readBufferedImage(img);
   }
   
   /**
    * Applies given filters in order.
    * Returns self just to make chaining for tests easier.
    *
    * @param actions filters to apply
    * @return self after filters are applied
    */
   @SafeVarargs
   final GrayscaleImage apply(Function<int[][], int[][]>... actions) {
      for (Function f : actions) {
         //noinspection unchecked
         pixels = (int[][]) f.apply(pixels);
         width = pixels.length;
         height = pixels[0].length;
      }
      
      return this;
   }
   
   /**
    * Generates BufferedImage from matrix of pixels.
    * Bit depth is not stored internally because all instances of GrayscaleImage
    * are temporary.
    *
    * @return generated image
    */
   public BufferedImage toImage() {
      BufferedImage out = new BufferedImage(width, height,
                                            BufferedImage.TYPE_INT_RGB);
      int gs;
      
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            gs = pixels[i][j];
            double ratio = (double) gs / ((1 << MainWindow.bitDepth) - 1);
            gs = (int) (ratio * 0xFF);
            out.setRGB(i, j, 0xFFFFFF & (gs + (gs << 8) + (gs << 16)));
         }
      }
      
      return out;
   }
   
   /**
    * Generates matrix of pixels from given buffered image by reading
    * values pixel by pixel.
    *
    * @param img source image
    * @return matrix of pixels
    */
   private static int[][] readBufferedImage(BufferedImage img) {
      int[][] pixels;
      
      if (img.getHeight() != 0 && img.getWidth() != 0) {
         pixels = new int[img.getWidth()][img.getHeight()];
         
         if (img.getHeight() == 511) {
            System.out.println("idk");
         }
         
         for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
               // assume image is grayscale (r==g && g == b)
               pixels[i][j] = img.getRGB(i, j) & 0xFF;
            }
         }
      } else {
         pixels = null;
      }
      
      return pixels;
   }
}
