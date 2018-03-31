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
        pixels = getGrayscale(img);
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
            for (int j = 0; j < height; j++) {
                pixels[i][j] = sourceImage.pixels[i][j];
            }
        }
    }

    /**
     * Reads each pixel from source image generates a 2D integer array from the averaged rgb values;
     *
     * @param img source image
     * @return matrix of pixels
     */
    private int[][] getGrayscale(BufferedImage img) {
        int[][] pixels = new int[img.getWidth()][img.getHeight()];

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int rgb = img.getRGB(i,j);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                rgb = (r + g + b) / 3;

                rgb = rgb | (rgb << 8) | (rgb << 16);

                pixels[i][j] = rgb;
            }
        }

        return pixels;
    }

    /**
     * Function for sequentially applying filters
     *
     * Creates copy of image to avoid side effects.
     * For each passed action, copy.pixels <- action.apply(pixels)
     *
     * @param actions
     * @return a new ProcessableImage created from this instance with given functions applied.
     */
    public ProcessableImage apply(Function<int[][],int[][]>... actions) {
        ProcessableImage copy = new ProcessableImage(this);

        for (Function f: actions) {
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
        BufferedImage out = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        int grayscale;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grayscale = pixels[i][j];
                out.setRGB(i,j,grayscale);
            }
        }

        return out;
    }

    /**
     *
     *
     * @param img matrix of pixels for input image
     * @return matrix of pixels for output image
     */
    public static int[][] scaleNearestNeighbor(int[][] img, int width, int height) {
        width = width > 0 ? width : 1;
        height = height > 0 ? height : 1;

        int sourceWidth, sourceHeight;
        int x,y;
        int[][] out;

        out = new int[width][height];
        sourceWidth = img.length;
        sourceHeight = img[0].length;

        for (int i = 0; i < width; i++) {
            x = (int)((double)i / width * sourceWidth);

            for (int j = 0; j < height; j++) {
                y = (int)((double)j / height * sourceHeight);
                out[i][j] = img[x][y];
            }
        }

        return out;
    }
}
