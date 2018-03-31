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
}
