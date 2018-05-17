import java.awt.image.BufferedImage;
import java.util.function.Function;

public class GenericImage {
    private Function<float[], Integer> p2RGB;
    private Function<int[], float[]> rgb2P;
    private Object[][] channels;
    private int height, width, channelCount;

    public GenericImage(BufferedImage img, Function<float[], Integer> p2r, Function<int[], float[]> r2p, int channelCount) {
        rgb2P = r2p;
        p2RGB = p2r;

        width = img.getWidth();
        height = img.getHeight();

        channels = new Object[width][height];
        this.channelCount = channelCount;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                channels[x][y] = rgb2P.apply(splitRGB(img.getRGB(x,y)));
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
                cs = (float[])channels[x][y];

                for (int i = 0; i < channelCount; i++) {
                    int gray = (int)(0xFF * cs[i]);
                    images[i].setRGB(x,y,gray + (gray <<8) + (gray << 16));
                }
            }
        }

        return images;
    }

    public BufferedImage toImage() {
        BufferedImage out = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                out.setRGB(x,y,p2RGB.apply((float[])channels[x][y]));
            }
        }

        return out;
    }
}
