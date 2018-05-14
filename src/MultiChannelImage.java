import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MultiChannelImage {
   // used for determining how to split channels
   public final static int RGB = 0, HSV = 1, DEFAULT = 2;
   private final static Map<Integer, Integer> NUM_CHANNELS;
   private final static Map<Integer, ScaleFunction<int[], BufferedImage, Integer, Integer>> DECOMPOSE;
   
   private final static Map<Integer, Recompose> RECOMPOSE;
   
   static {
      NUM_CHANNELS = new HashMap<>();
      NUM_CHANNELS.put(RGB, 3);
      NUM_CHANNELS.put(HSV, 3);
      NUM_CHANNELS.put(DEFAULT, 1);
      
      DECOMPOSE = new HashMap<>();
      DECOMPOSE.put(HSV, MultiChannelImage::getHSV);
      DECOMPOSE.put(DEFAULT, (a, b, c) -> new int[]{a.getRGB(b, c)});
      DECOMPOSE.put(RGB, (a, b, c) -> {
         int rgb = a.getRGB(b, c);
         return new int[]{(rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8, (rgb & 0xFF)};
         
      });
      
      RECOMPOSE = new HashMap<>();
      RECOMPOSE.put(RGB, channels -> {
         int r, g, b;
         
         r = channels[0] << 16;
         g = channels[1] << 8;
         b = channels[2];
         
         return r + g + b;
      });
      RECOMPOSE.put(HSV, hsv -> Color.HSBtoRGB(hsv[0] / 255f, hsv[1] / 255f,
                                               hsv[2] / 255f));
   }
   
   private int[][][] channels;
   private int width, height;
   
   public MultiChannelImage(BufferedImage img, int type) {
      width = img.getWidth();
      height = img.getHeight();
      channels = decomposeImage(img, type);
   }
   
   
   private static int[] getHSV(BufferedImage img, int x, int y) {
      float[] hsv;
      int[] out;
      int rgb, r, g, b;
      
      rgb = img.getRGB(x, y);
      r = (rgb & 0xFF0000) >> 16;
      g = (rgb & 0xFF00) >> 8;
      b = (rgb & 0xFF);
      hsv = Color.RGBtoHSB(r, g, b, null);
      
      out = new int[3];
      
      for (int i = 0; i < 3; i++) {
         out[i] = (int) (hsv[i] * 0xFF);
      }
      
      return out;
   }
   
   public MultiChannelImage apply(Function<int[][], int[][]>... actions) {
      for (int i = 0; i < channels.length; i++) {
         for (Function<int[][], int[][]> f : actions) {
            channels[i] = f.apply(channels[i]);
         }
      }
      
      width = channels[0].length;
      height = channels[0][0].length;
      
      return this;
   }
   
   public BufferedImage[] toImages() {
      BufferedImage[] images = new BufferedImage[channels.length];
      
      for (int i = 0; i < channels.length; i++) {
         images[i] = new BufferedImage(width, height,
                                       BufferedImage.TYPE_INT_RGB);
      }
      
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            for (int c = 0; c < channels.length; c++) {
               int col = channels[c][i][j];
               images[c].setRGB(i, j, col + (col << 8) + (col << 16));
            }
         }
      }
      
      return images;
   }
   
   public static int[][][] decomposeImage(BufferedImage img, int channels) {
      int[][][] out = new int[NUM_CHANNELS.get(
              channels)][img.getWidth()][img.getHeight()];
      
      // for each channel
      for (int i = 0; i < out.length; i++) {
         // for each pixel
         for (int j = 0; j < out[i].length; j++) {
            for (int k = 0; k < out[i][j].length; k++) {
               // get value for channel
               out[i][j][k] = DECOMPOSE.get(channels).apply(img, j, k)[i];
            }
         }
      }
      
      return out;
   }
   
   public static BufferedImage recomposeImage(MultiChannelImage img, int type) {
      int[][][] channels = img.channels;
      BufferedImage out = new BufferedImage(img.width, img.height,
                                            BufferedImage.TYPE_INT_RGB);
      
      // assume rgb
      for (int i = 0; i < img.width; i++) {
         for (int j = 0; j < img.height; j++) {
            int r = channels[0][i][j];
            int g = channels[1][i][j];
            int b = channels[2][i][j];
            
            out.setRGB(i, j, (r << 16) + (g << 8) + b);
            out.setRGB(i, j, RECOMPOSE.get(type).apply(new int[]{r, g, b}));
         }
      }
      
      return out;
   }
}
