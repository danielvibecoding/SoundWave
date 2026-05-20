package soundwave.fingerprint;

public class FilterResult {
    private final short[] freq;




    private final int[] time;


    private final int numPeaks;

    public FilterResult(int[] time, short[] freq, int numPeaks) {
        this.freq = freq;
        this.time = time;
        this.numPeaks = numPeaks;
    }

    public short[] getFreq() {
        return freq;
    }

    public int[] getTime() {
        return time;
    }

    public int getNumPeaks() {
        return numPeaks;
    }
}