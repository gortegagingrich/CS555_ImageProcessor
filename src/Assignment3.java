import java.util.function.BinaryOperator;

public class Assignment3 {

    /**
     *
     * @param img
     * @param size size of window
     * @return
     */
    public static int[][] arithMeanFilter(int[][] img, int size) {
        return Assignment3.genericMean(img, size,
                (a,b) -> a + b,
                (a,b) -> a / b);
    }

    public static int[][] geomMeanFilter(int[][] img, int size) {
        return Assignment3.genericMean(img, size,
                (a,b) -> (a == 0) ? 1 * b: a * b,
                (a,b) -> Math.pow(a,1/b));
    }

    public static int[][] harmonicMeanFilter(int[][] img, int size) {
        return Assignment3.genericMean(img, size,
                (a,b) -> a + 1 / b,
                (a,b) -> b / a);
    }

    private static int[][] genericMean(int[][] img, int size, BinaryOperator<Double> inc, BinaryOperator<Double> adjust) {
        double runningSum;
        int x, y;
        double coef;
        int[][] out = new int[img.length][img[0].length];

        size /= 2;

        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                // init running sum to 0
                runningSum = 0;
                coef = 0;

                for (int k = -size; k <= size; k++) {
                    for (int l = -size; l <= size; l++) {
                        x = i+k;
                        y = j+l;

                        if (x > -1 && x < img.length && y > -1 && y < img[i].length) {
                            runningSum = inc.apply(runningSum, (double)img[x][y]);
                            coef += 1;
                        }
                    }
                }

                out [i][j] = adjust.apply(runningSum, coef).intValue();
            }
        }

        return out;
    }
}
