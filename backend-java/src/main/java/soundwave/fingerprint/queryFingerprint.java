package soundwave.fingerprint;

import java.awt.List;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Query;

import java.util.PriorityQueue;

import soundwave.db.SqliteFunc;
import soundwave.tests.OutputFunction;

public class queryFingerprint {
    short[] resFreq;
    int[] resTime; 
    int numPeaks;
    int maxCouplings = 5;
    int batchIndex = 0;

    public queryFingerprint(FilterResult A) {
        this.resFreq = A.getFreq();
        this.resTime = A.getTime();
        this.numPeaks = A.getNumPeaks();
    }

    private static final int MAX_QUERY_SIZE = 5000;

    private static final int[] addrBatch = new int[MAX_QUERY_SIZE];
    private static final int[] timeBatch = new int[MAX_QUERY_SIZE];

    public QueryResult getResults(SqliteFunc sql, Connection conn, OutputFunction o) throws SQLException, IOException{
        System.out.println("Number of songs added to db " + sql.getNumSongs(conn));


        Map<Integer, Integer> recordedAddresses = new HashMap<>();
        Map<Long, Integer> songIdIntegerVotes = new HashMap<>();
        int numFingerPrints = 0;

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
                numFingerPrints++;

                if (batchIndex < MAX_QUERY_SIZE) {
                    addrBatch[batchIndex] = hash;
                    timeBatch[batchIndex] = firstTime;
                    recordedAddresses.put(addrBatch[batchIndex], timeBatch[batchIndex]);
                    batchIndex++;


                } else {
                    System.out.println("addrChunk count: " + batchIndex);
                    System.out.println("rAddr size: " + recordedAddresses.size());

                    // this shit is kinda flawed now that i think about it
                    sql.lookup(addrBatch, batchIndex, recordedAddresses, songIdIntegerVotes, o);
                    batchIndex = 0;
                    addrBatch[batchIndex] = hash;
                    timeBatch[batchIndex] = firstTime;
                    recordedAddresses.put(addrBatch[batchIndex], timeBatch[batchIndex]);
                    batchIndex++;
                    // votingAlgorithm(recordedAddresses, rs, songIdIntegerVotes);
                }
            }
        }

        // flush out exissting results
        sql.lookup(addrBatch, batchIndex, recordedAddresses, songIdIntegerVotes, o);
        // votingAlgorithm(recordedAddresses, rs, songIdIntegerVotes);
        o.logVotesHeader();
        o.logTopVotes(songIdIntegerVotes);
        o.Footer();

        System.out.println("number of fingerprints: " + numFingerPrints);
        System.out.println("songIdintegervotes size: " + songIdIntegerVotes.size());
        QueryResult q = findWinners(songIdIntegerVotes, sql, numFingerPrints);
        printResults(q);
        o.logAllSongs(sql);
        o.Footer();
        
        printResults(q);
        o.logStatsAndWinners(numFingerPrints, songIdIntegerVotes.size(), q);

        return q;
    }

    @Deprecated
    // remove this shit when i can confirm i can
    private void votingAlgorithm(Map<Integer, Integer> rAddr, ResultSet rs, Map<Long, Integer> songIdIntegerVotes) throws SQLException {


        // iterate through the results and update the mapped offsets
        // this shit deffo works at least the idea behind it
        while(rs.next()) {
            int addr = rs.getInt("addr");
            int songId = rs.getInt("song_id");
            int tSong = rs.getInt("abs_time");

            int offset = tSong - rAddr.get(addr);
            long votingKey = voteKey(songId, offset);

            songIdIntegerVotes.merge(votingKey, 1, Integer::sum);
        }
    }


    private QueryResult findWinners(Map<Long, Integer> votes, SqliteFunc sql, int numFingerPrints) throws SQLException {
        Map<Integer, Integer> winners = new HashMap<>();
        Map<Integer, String> winnerNames = new HashMap<>();


        for (Map.Entry<Long, Integer> entry : votes.entrySet()) {
            // for now just skip useless count ups
            if (entry.getValue() < 4) {
                continue;
            }
            
            // so i stripped away the offset such that i find all the ones with same songID
            // then I only keep the maximum merge
            // this should be subject to change i believe 

            int songId = unpackSongId(entry.getKey());


            int voteCount = entry.getValue();





            winners.merge(songId, voteCount, Math::max);
        }


        System.out.println(winners.size());


        int[] top5 = firstN(winners, 5);


        Map<Integer, Double> scores = new HashMap<>();


        // loop through to find these top 5 winners in the set and get their vote counts
        // to calculate a score we put in the confidenceScore hash
        for (int i = 0; i < top5.length; i++) {
            double score = ((double) winners.get(top5[i])/numFingerPrints) * 100;
            scores.put(top5[i], score);
        }

        winnerNames = sql.lookupSongNames(top5);
        return new QueryResult(winnerNames, scores, top5);
    }

    private int unpackSongId(long key) {
    return (int) (key >> 32);
}

    private void printResults(QueryResult q) {
        int[] top5 = q.top5();
        Map<Integer, String> winnerNames = q.winnerNames();
        Map<Integer, Double> scores = q.scores();

        for (int i = 0; i < top5.length; i ++) {
            System.out.println(i + 1 + "Song Name: " + winnerNames.get(top5[i]) + " Score: " + scores.get(top5[i]));
        }

    }

    // helper function that uses a pq to find first n values
    private static int[] firstN(Map<Integer, Integer> map, int n) {
        PriorityQueue<Entry<Integer, Integer>> pq= new PriorityQueue<>(
            n + 1, Map.Entry.comparingByValue()
        );

        int bound = n + 1;
        for (Entry<Integer, Integer> en : map.entrySet()) {
            pq.offer(en);
            if (pq.size() == bound) {
                pq.poll();
            }
        }

        int i = n;
        int[] array = new int[n];

        while (--i >= 0) {
            array[i] = pq.remove().getKey();
        }
        return array;
    }

    private int packAddr(short fAnchor, short fPoint, int deltaT) {
        return ((fAnchor & 0x1FF) << 23)   // 9 bits -> bits 31..23
            | ((fPoint  & 0x1FF) << 14)   // 9 bits -> bits 22..14
            |  (deltaT  & 0x3FFF);        // 14 bits -> bits 13..0
    }


    // returns a list of offsetted voting keys multiple songs could have different offsets hence different keys
    

    // private void AddToHashMap(Map<Long, Integer> matches, ResultSet rs) throws SQLException {
    //     while (rs.next()) {
    //         long result = compressResult(rs.getInt(1), rs.getInt(2));
    //         matches.merge(result, 1, Integer::sum);
    //     }
    // }

    private long voteKey(int songId, int offset) {
        return ((long) songId << 32) | (offset & 0xffffffffL);
    }
}
