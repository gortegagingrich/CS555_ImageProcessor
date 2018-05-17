public class FloatFilters {
    public static float[][] mean(float[][] img, int radius) {
        float[][] out = new float[img.length][img[0].length];
        float[][] kernel = new float[radius * 2 + 1][radius*2 + 1];

        // build kernel
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel.length; j++) {
                kernel[i][j] = 1f / kernel.length / kernel.length;
            }
        }

        // convol
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                out[i][j] = convol(img,kernel,i-radius,j-radius);
            }
        }

        return out;
    }

    private static float convol(float[][] img, float[][] kernel, int x, int y) {
        float sum = 0;

        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel[i].length; j++) {
                if (x+i>-1 && x+i<img.length && y+j>-1 && y+j<img[x+i].length){
                    sum += kernel[i][j] * img[i+x][j+y];
                }
            }
        }

        return sum;
    }
}
