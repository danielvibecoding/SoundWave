package soundwave.fingerprint;

public class filter {
    // logarithmic band filters
    // range from very low, low, low-mid, mid, mid-high, high
    // 0 - 511, for now bands do not overlap

    private static final int[][] bandRanges = {
        {0, 10}, {10, 20}, {20, 40}, {40, 80}, {80, 160}, {160, 512}
    };

    private static final int numBands = 6;
    private static final int emptyValue = -1;

    public static PeakResult filterLoop(double[][] spectrogram) {
        int specLength = spectrogram.length;
        double[][] powArray = new double[numBands][specLength];
        int[][] freqArray = new int[numBands][specLength];

        // SoundWave.1.0 Implementation
        // I want to figure out if this method is good enough
        // Divide the bins into buckets
        // Locate highest bin in each bucket, use that to calculate an average
        // Remove anything below that average
        for (int specIndex = 0; specIndex < specLength; specIndex ++) {
            double sum = 0;
            for (int rangeIndex = 0; rangeIndex < numBands; rangeIndex ++) {
                int low = bandRanges[rangeIndex][0];
                int high = bandRanges[rangeIndex][1];
                int frequency = 0;
                double maximum = Double.NEGATIVE_INFINITY;

                for (int bandIndex = low; bandIndex < high; bandIndex ++) {
                    if (spectrogram[bandIndex][specIndex] > maximum) {
                        maximum = spectrogram[bandIndex][specIndex];
                        frequency = bandIndex;
                    }

                }
                powArray[rangeIndex][specIndex] = maximum;
                freqArray[rangeIndex][specIndex] = frequency;
                sum += maximum;
            }   
            double average = sum/numBands;

            for (int resultArrayIndex = 0; resultArrayIndex < numBands; resultArrayIndex ++) {
                if (powArray[resultArrayIndex][specIndex] < average) {
                    powArray[resultArrayIndex][specIndex] = Double.NaN;
                    freqArray[resultArrayIndex][specIndex] = emptyValue;
                }
            }
        }
        return new PeakResult(powArray, freqArray);
    }
}
