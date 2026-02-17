package soundwave.fingerprint;

public class PeakResult {
    public final double[][] powArray;
    public final int[][] freqArray;

    public PeakResult(double[][] powArray, int[][] freqArray) {
        this.powArray = powArray;
        this.freqArray = freqArray;
    }

}