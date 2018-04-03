import java.awt.image.BufferedImage;
import java.util.function.Function;

public class ProcessableImage {
    // I'm using ints to avoid having to cast to bytes
    private int[][] pixels;
    private int width;
    private int height;

    /**
     * Creates grayscale ProcessableImage from given BufferedImage
     *
     * @param img source image
     */
    public ProcessableImage(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        pixels = readBufferedImage(img);
    }

    /**
     * Creates clone of given ProcessableImage
     *
     * @param sourceImage source image
     */
    public ProcessableImage(ProcessableImage sourceImage) {
        width = sourceImage.width;
        height = sourceImage.height;
        pixels = new int[width][height];

        for (int i = 0; i < width; i++) {
            System.arraycopy(sourceImage.pixels[i], 0, pixels[i], 0, height);
        }
    }

    /**
     * Function for sequentially applying filters
     * <p>
     * Creates copy of image to avoid side effects.
     * For each passed action, copy.pixels <- action.apply(pixels)
     *
     * @return a new ProcessableImage created from this instance with given functions applied.
     */
    ProcessableImage apply() {
        return new ProcessableImage(this);
    }

    /**
     * Function for sequentially applying filters
     * <p>
     * Creates copy of image to avoid side effects.
     * For each passed action, copy.pixels <- action.apply(pixels)
     *
     * @param actions ordered list of filters to apply to the image
     * @return a new ProcessableImage created from this instance with given functions applied.
     */
    ProcessableImage apply(Function<int[][], int[][]>... actions) {
        ProcessableImage copy = new ProcessableImage(this);

        for (Function f : actions) {
            copy.pixels = (int[][]) f.apply(copy.pixels);
            copy.width = copy.pixels.length;
            copy.height = copy.pixels[0].length;
        }

        return copy;
    }

    /**
     * Generates BufferedImage from the array of pixels.
     *
     * @return BufferedImage represented by pixel array
     */
    public BufferedImage toImage() {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int grayscale;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grayscale = pixels[i][j];
                out.setRGB(i, j, grayscale);
            }
        }

        return out;
    }

    public static int[][] readBufferedImage(BufferedImage img) {
        int[][] pixels;

        if (img.getHeight() != 0 && img.getWidth() != 0) {
            pixels = new int[img.getWidth()][img.getHeight()];

            for (int i = 0; i < pixels.length; i++) {
                for (int j = 0; j < pixels[i].length; j++) {
                    pixels[i][j] = img.getRGB(i, j);
                }
            }
        } else {
            pixels = null;
        }

        return pixels;
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

                //b = ((a + b) / 2) & 0xFF;

                b = ((a & 0xFF) + (b & 0xFF)) / 2;

                out[i][j] = b | b << 8 | b << 16;
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
            int y = Math.min((int) ((double) i / out[0].length * in[0].length), in[0].length - 1);

            if (y >= in.length) {
                System.out.println(y);
            }

            for (int j = 0; j < out.length; j++) {
                double x = (double) j / out.length;  // relative locationof destination pixel
                double x0 = Math.min(in.length - 1, Math.floor(x * in.length)); // position of pixel to left
                double dx0 = x - x0 / in.length; // weight of pixel to left
                double x1 = Math.min(in.length - 1, Math.ceil(x * in.length)); // position of pixel to right
                double dx1 = x1 / in.length - x; // weight of pixel to right

                out[j][i] = (x0 == x1) ?
                        (in[(int) x0][y] & 0xFF) :
                        (int) ((1 - dx0 / (dx0 + dx1)) * (in[(int) x0][y] & 0xFF) + (1 - dx1 / (dx0 + dx1)) * (in[(int) x1][y] & 0xFF)) & 0xFF;
                out[j][i] = out[j][i] + (out[j][i] << 8) + (out[j][i] << 16);
                out[j][i] &= 0xFFFFFF;
            }
        }
    }

    private static void lerpVerical(int[][] in, int[][] out) {
        for (int i = 0; i < out.length; i++) {
            int y = Math.min((int) ((double) i / out.length * in.length), in.length - 1);

            for (int j = 0; j < out[0].length; j++) {
                double x = (double) j / out[0].length;  // relative locationof destination pixel
                double x0 = Math.min(in[0].length - 1, Math.floor(x * in[0].length)); // position of pixel to left
                double dx0 = x - x0 / in[0].length; // weight of pixel to left
                double x1 = Math.min(in[0].length - 1, Math.ceil(x * in[0].length)); // position of pixel to right
                double dx1 = x1 / in[0].length - x; // weight of pixel to right

                out[i][j] = (x0 == x1) ?
                        (in[y][(int) x0] & 0xFF) :
                        (int) ((1 - dx0 / (dx0 + dx1)) * (in[y][(int) x0] & 0xFF) + (1 - dx1 / (dx0 + dx1)) * (in[y][(int) x1] & 0xFF)) & 0xFF;
                out[i][j] = out[i][j] + (out[i][j] << 8) + (out[i][j] << 16);
                out[i][j] &= 0xFFFFFF;
            }
        }
    }
}
