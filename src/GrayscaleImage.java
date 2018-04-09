/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 1
 * Description: Implementation of several basic algorithms for changing spatial
 * and gray-scale resolution
 */

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class GrayscaleImage {
   // I'm using ints to avoid having to cast to bytes
   private int[][] pixels;
   private int width;
   private int height;
   
   public GrayscaleImage(BufferedImage img) {
      width = img.getWidth();
      height = img.getHeight();
      pixels = readBufferedImage(img);
   }
   
   public GrayscaleImage(GrayscaleImage sourceImage) {
      width = sourceImage.width;
      height = sourceImage.height;
      pixels = new int[width][height];
      
      for (int i = 0; i < width; i++) {
         System.arraycopy(sourceImage.pixels[i], 0, pixels[i], 0, height);
      }
   }
   
   GrayscaleImage apply(Function<int[][], int[][]>... actions) {
      GrayscaleImage copy = new GrayscaleImage(this);
      
      for (Function f : actions) {
         copy.pixels = (int[][]) f.apply(copy.pixels);
         copy.width = copy.pixels.length;
         copy.height = copy.pixels[0].length;
      }
      
      return copy;
   }
   
   public BufferedImage toImage(int bitDepth) {
      BufferedImage out = new BufferedImage(width, height,
                                            BufferedImage.TYPE_INT_RGB);
      int grayscale;
      
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            grayscale = pixels[i][j];
            double ratio = (double) grayscale / ((1 << MainWindow.bitDepth) - 1);
            grayscale = (int) (ratio * 0xFF);
            out.setRGB(i, j,
                       0xFFFFFF & (grayscale + (grayscale << 8) + (grayscale << 16)));
         }
      }
      
      return out;
   }
   
   public int[][] getPixels() {
      return pixels;
   }
   
   public static int[][] readBufferedImage(BufferedImage img) {
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
