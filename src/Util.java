import java.util.function.BiFunction;

public class Util {
   public static float[][] add(float[][] a, float[][] b) {
      return binaryMatrixOperation(a, b, (x, y) -> x + y);
   }
   
   public static float[][] sub(float[][] a, float[][] b) {
      return binaryMatrixOperation(a, b, (x, y) -> x - y);
   }
   
   public static float[][] mul(float[][] a, float[][] b) {
      return binaryMatrixOperation(a, b, (x, y) -> x * y);
   }
   
   public static float[][] div(float[][] a, float[][] b) {
      return binaryMatrixOperation(a, b, (x, y) -> x / y);
   }
   
   public static float[][] div(final float a, float[][] b) {
      return binaryMatrixOperation(b, null, (x, y) -> a / x);
   }
   
   public static float[][] add(float[][] a, final float c) {
      return binaryMatrixOperation(a, null, (x, y) -> x + c);
   }
   
   public static float[][] mul(float[][] a, final float c) {
      return binaryMatrixOperation(a, null, (x, y) -> x * c);
   }
   
   private static float[][] binaryMatrixOperation(float[][] a, float[][] b, BiFunction<Float, Float, Float> fn) {
      float[][] out = new float[a.length][a[0].length];
      
      for (int x = 0; x < out.length; x++) {
         for (int y = 0; y < out[x].length; y++) {
            out[x][y] = fn.apply(a[x][y], b == null ? 0 : b[x][y]);
         }
      }
      
      return out;
   }
}
