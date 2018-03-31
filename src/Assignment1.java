public class Assignment1 {
    /**
     * @param img matrix of pixels for input image
     * @return matrix of pixels for output image
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

    public static int[][] changeBitDepth(int[][] img, int depth) {
        int[][] out = new int[img.length][img[0].length];

        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out.length; j++) {
                out[i][j] = convertGrayscaleBit(img[i][j], depth);
            }
        }

        return out;
    }

    private static int convertGrayscaleBit(int rgb, int depth) {

        rgb = 0xFF & rgb >> (8 - depth) << (8 - depth);
        //System.out.println(rgb);

        return rgb | (rgb << 8) | (rgb << 16);
    }

    /**
     * Reads each pixel from source image generates a 2D integer array from the averaged rgb values;
     *
     * @param img source image
     * @return matrix of pixels
     */
    public static int[][] toGrayscale(int[][] img) {
        int[][] pixels;

        int rgb, r, g, b;
        pixels = new int[img.length][img[0].length];

        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                rgb = img[i][j];
                r = (rgb >> 16) & 0xFF;
                g = (rgb >> 8) & 0xFF;
                b = rgb & 0xFF;
                rgb = (r + g + b) / 3;

                rgb = rgb | (rgb << 8) | (rgb << 16);

                pixels[i][j] = rgb;
            }
        }
        return pixels;
    }
}
