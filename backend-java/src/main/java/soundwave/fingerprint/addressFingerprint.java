package soundwave.fingerprint;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import soundwave.db.SqliteFunc;
import soundwave.fingerprint.FilterResult;

public class addressFingerprint {
    short[] resFreq;
    int[] resTime; 
    int numPeaks;
    int maxCouplings = 5;
    int batchIndex = 0;


    private static final int INSERT_BATCH_SIZE = 5000;

    private static final int[] addrBatch = new int[INSERT_BATCH_SIZE];
    private static final int[] timeBatch = new int[INSERT_BATCH_SIZE];


    public addressFingerprint(FilterResult A) {
        this.resFreq = A.getFreq();
        this.resTime = A.getTime();
        this.numPeaks = A.getNumPeaks();
    }

    // Version 1.0 with a 3 buffer gap between each peak

    public void addSongFingerPrint(SqliteFunc sql, Connection conn, int songId) throws SQLException {
        for(int i = 0; i < numPeaks; i++) {
            short firstFreq = resFreq[i];
            int firstTime = resTime[i];

            int j = i + 3;
            int k = 0;

            // this for now will consider edge case later, i think this should be fine
            // the consideration seems to make sense
            // k is reset after each cycle of this loop
            for (; k < maxCouplings && j < numPeaks; j++, k++) {
                short secondFreq = resFreq[j];
                int secondTime = resTime[j];

                int deltaT = secondTime - firstTime;

                int hash = packAddr(firstFreq, secondFreq, deltaT);

                if (batchIndex < INSERT_BATCH_SIZE) {
                    addrBatch[batchIndex] = hash;
                    timeBatch[batchIndex] = firstTime;
                    batchIndex++;
                } else {
                    sql.insertBatch(songId, addrBatch, timeBatch, batchIndex);
                    batchIndex = 0;
                    addrBatch[batchIndex] = hash;
                    timeBatch[batchIndex] = firstTime;
                }
            }

        }
            // maybe this will flush out the batch
            sql.insertBatch(songId, addrBatch, timeBatch, batchIndex);
            batchIndex = 0;
    }

    private int packAddr(short fAnchor, short fPoint, int deltaT) {
        return ((fAnchor & 0x1FF) << 23)   // 9 bits -> bits 31..23
            | ((fPoint  & 0x1FF) << 14)   // 9 bits -> bits 22..14
            |  (deltaT  & 0x3FFF);        // 14 bits -> bits 13..0
    }



    // static public void addSongFingerprint(FilterResult A) {
    //     int addrQuery[] = new int[5000];
    //     int valQuery[] = new int[5000];
    //     int f[][] = A.getFreq();

    //     // int firstI = 0;
    //     // int firstJ = 0;

    //     int secondI = 0;
    //     int secondJ = 0;

    //     // third I, third J

    //     int secondPointer = 0;
    //     int peaksScanned = 0;
    //     int maxCouplings = 5;
    //     int couplings = 1;
    //     int queryIndex = 0;
    //     int buffer = 3;
    //     int currentBuffer = 0;
        
    //     for (int firstI = 0; firstI < A.getLength(); firstI ++) {
    //         for (int firstJ = 0; firstJ < A.getHeight(); firstJ++) {
    //             if (f[firstJ][firstI] == emptyValue) {
    //                 continue;
    //             }

                





    //         }


    //     }


    //     while (peaksScanned < A.numPeaks) {




    //         if (currentBuffer < buffer) {
                
    //         }


    //         if (second >= numPeaks) {
    //             maxCouplings --;
    //         }



    //         if (couplings <= maxCouplings) {
    //             first ++;
    //             second = first + 1;
    //             couplings = 1;
    //             continue;
    //         }






    //         int t0Freq = freq[first];
    //         int tNFreq = freq[second];
    //         int diffTime = time[second] - time[first];
    //         int absTime = time[first];

    //         // then its about creating query logic here
    //         // need to really think about this a bit later
    //     }
    // }

    // static public void matchFingerprint(String Song) {

    // }

    // static private void generateFingerprint(FilterResult A) {
    //     int k = 0;
    //     int freq[] = new int[A.numPeaks];
    //     int time[] = new int[A.numPeaks];

    //     for (int i = 0; i < A.lenght; i ++) {
    //         for (int j = 0; j < A.height; j ++) {
    //             if (A.freqArray[j][i] != -1) {
    //                 freq[k] = A.freqArray[i][j];
    //                 time[k] = i;
    //                 k ++;
    //             }
    //         }
    //     }
    // }
}
