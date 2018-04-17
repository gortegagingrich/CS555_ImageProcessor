/**
 * Created by gabriel on 4/17/18.
 */
public class Assignment2 {
   public static int[][] setBitPlanes(int[][] img, int planes) {
      int i, j;

      for (i = 0; i < img.length; i++) {
         for (j = 0; j < img[i].length; j++) {
            img[i][j] &= planes;
         }
      }

      return img;
   }
}
