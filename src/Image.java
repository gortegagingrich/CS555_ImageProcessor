import java.awt.image.BufferedImage;
import java.util.function.Function;

public interface Image {
   
   
   Image apply(Function<int[][], int[][]>... actions);
   
   BufferedImage toImage();
}
