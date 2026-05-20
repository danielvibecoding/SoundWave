package soundwave.fingerprint;

import java.nio.file.DirectoryStream.Filter;

public class filter {
    // logarithmic band filters
    // range from very low, low, low-mid, mid, mid-high, high
    // 0 - 511, for now bands do not overlap

    private static final short[][] bandRanges = {
        {0, 10}, {10, 20}, {20, 40}, {40, 80}, {80, 160}, {160, 512}
    };

    private static final int numBands = 6;
    private static final int emptyValue = -1;
    private double[][] powArray;
    private short[][] freqArray;
    private int numPeaks = 0;
    private int specLength;

    public filter(double[][] spectrogram) {
        this.specLength = spectrogram[0].length;
        this.powArray = new double[numBands][specLength];
        this.freqArray = new short[numBands][specLength];
    }

    public FilterResult filterLoop(double[][] spectrogram) {
        int specLength = spectrogram[0].length;
        System.out.println("SPECLENGTH: " + specLength);
        // double[][] powArray = new double[numBands][specLength];
        // short[][] freqArray = new short[numBands][specLength];

 

        // SoundWave.1.0 Implementation
        // I want to figure out if this method is good enough
        // Divide the bins into buckets
        // Locate highest bin in each bucket, use that to calculate an average
        // Remove anything below that average
        for (int specIndex = 0; specIndex < specLength; specIndex ++) {
            double sum = 0;
            for (int rangeIndex = 0; rangeIndex < numBands; rangeIndex ++) {
                short low = bandRanges[rangeIndex][0];
                short high = bandRanges[rangeIndex][1];
                short frequency = 0;
                double maximum = Double.NEGATIVE_INFINITY;

                for (short bandIndex = low; bandIndex < high; bandIndex ++) {
                    if (spectrogram[bandIndex][specIndex] > maximum) {
                        maximum = spectrogram[bandIndex][specIndex];
                        frequency = bandIndex;
                    }

                }
                powArray[rangeIndex][specIndex] = maximum;
                freqArray[rangeIndex][specIndex] = frequency;
                numPeaks ++;
                sum += maximum;
            }   
            double average = sum/numBands;

            for (int resultArrayIndex = 0; resultArrayIndex < numBands; resultArrayIndex ++) {
                if (powArray[resultArrayIndex][specIndex] < average) {
                    powArray[resultArrayIndex][specIndex] = Double.NaN;
                    freqArray[resultArrayIndex][specIndex] = emptyValue;
                    numPeaks --;
                }


            }
        }

        return returnResultant();
    }

    private FilterResult returnResultant() {
        short[] resFreq = new short[numPeaks];
        int[] resTime = new int[numPeaks];

        int currentPeak = 0;


        for (int i = 0; i < specLength; i ++) {
            for (int j = 0; j < numBands; j ++) {
                if (freqArray[j][i] == emptyValue) {
                    continue;
                }

                if (currentPeak < numPeaks) {
                    resFreq[currentPeak] = freqArray[j][i];
                    resTime[currentPeak] = i;

                    currentPeak ++;
                    continue;
                }
                System.out.println("I should not be out here");

            }
        }

        return new FilterResult(resTime, resFreq, numPeaks);
    }
}
