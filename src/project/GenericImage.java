package project;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class GenericImage {
    private Function<float[], Integer> p2RGB;
    private float[][][] channels;
    private int height, width, channelCount;

    public GenericImage(BufferedImage img, Function<float[], Integer> p2r, Function<int[], float[]> r2p, int channelCount) {
        Function<int[], float[]> rgb2P = r2p;
        p2RGB = p2r;

        width = img.getWidth();
        height = img.getHeight();

        channels = new float[channelCount][width][height];
        this.channelCount = channelCount;

        int[] rgb;
        float[] hsv;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                rgb = splitRGB(img.getRGB(x,y));
                hsv = rgb2P.apply(rgb);

                for (int i = 0; i < channelCount; i++) {
                    channels[i][x][y] = hsv[i];
                }
            }
        }
    }

    public static int[] splitRGB(int rgb) {
        int r,g,b;

        r = (rgb & 0xFF0000) >> 16;
        g = (rgb & 0xFF00) >> 8;
        b = (rgb & 0xFF);

        return new int[] {r,g,b};
    }

    public static int mergeRGB(int[] rgb) {
        return (rgb[0] << 16) + (rgb[1] << 8) + rgb[0];
    }

    public BufferedImage[] getChannels(int max) {
        float[] cs;
        BufferedImage[] images = new BufferedImage[channelCount];

        for (int i = 0; i < channelCount; i++) {
            images[i] = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                for (int i = 0; i < channelCount; i++) {
                    int gray = (int)(0xFF * channels[i][x][y]);

                    images[i].setRGB(x,y,gray + (gray <<8) + (gray << 16));
                }
            }
        }

        return images;
    }

    public BufferedImage toImage() {
        BufferedImage out = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        float[] hsv = new float[channelCount];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int i = 0; i < hsv.length; i++) {
                    hsv[i] = channels[i][x][y];
                }

                out.setRGB(x,y,p2RGB.apply(hsv));
            }
        }

        return out;
    }

    /**
     *
     * @param filter filtering function to apply to each listed channel
     * @param channel indexes of channels to filter
     * @return
     */
    public GenericImage apply(Function<float[][], float[][]> filter, int... channel) {
        for (int i: channel) {
            channels[i] = filter.apply(channels[i]);
        }

        return this;
    }
}
