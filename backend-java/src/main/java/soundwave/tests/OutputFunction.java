package soundwave.tests;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soundwave.db.Song;
import soundwave.db.SqliteFunc;
import soundwave.fingerprint.QueryResult;

public class OutputFunction {
    private final String FILE_NAME = "/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/tests/stats.txt";

    public void resetContents() throws IOException {
        PrintWriter writer = new PrintWriter("output.txt"); 
    }

    public void logMatchesHeader() throws IOException {
        try(PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println("----RETURNED MATCHES----");
        }
    }

    public void logMatches(int addr, int songId, int tSong) throws IOException {
        try(PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println("ADDR: " + addr + " SongID: " + songId + " Abs_time: " + tSong);
        }
    }

    public void Footer() throws IOException {
        try(PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println("----END----\n");
        }
    }


    public void logVotesHeader() throws IOException {
        try(PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println("----VOTES----");
        }
    }

    public void logTopVotes(Map<Long, Integer> voteList) throws IOException {
        voteList.entrySet().stream()
        .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
        .limit(20)
        .forEach(entry -> {
            long key = entry.getKey();
            int votes = entry.getValue();

            int songId = unpackSongId(key);
            int offset = unpackOffset(key);

            try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
                writer.println(                
                "songId=" + songId +
                ", offset=" + offset +
                ", votes=" + votes);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });  
    }

    // TODO add confidence score
    public void logStatsAndWinners(int numFingerPrints, int totalMatches, QueryResult q) throws IOException {
        int[] top5 = q.top5();
        Map<Integer, String> winnerNames = q.winnerNames();
        Map<Integer, Double> scores = q.scores();
        System.out.println("HI");
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(
                "----ADDITIONAL STATISTICS----\n" +
                "NUM FINGERPRINTS ADDRESSED: " + numFingerPrints + "\n" +
                "TOTAL MATCHES: " + totalMatches + "\n" 
                // "CONFIDENCE SCORE: " + confidenceScore + "\n"

                + "\n" + "----RESULTS----"
                


            );



            for (int i = 0; i < top5.length; i ++) {
                writer.println(getOrdinalSuffix(i+1) + " Song Name: " + winnerNames.get(top5[i]) + " Score: " + scores.get(top5[i]));
            }

        }

    }

    public static String getOrdinalSuffix(int number) {
        int remainder100 = number % 100;
        int remainder10 = number % 10;

        // Handle exceptions for 11, 12, 13 (they end in -th)
        if (remainder100 >= 11 && remainder100 <= 13) {
            return number + "th";
        }

        // Standard cases based on the last digit
        switch (remainder10) {
            case 1:  return number + "st";
            case 2:  return number + "nd";
            case 3:  return number + "rd";
            default: return number + "th";
        }
    }
    


    public void logAllSongs(SqliteFunc sql) throws IOException, SQLException {
        List<Song> songs = sql.getAllSongs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME , true))) {
            writer.println("----SONGS----");

            for (Song s : songs) {
                writer.println(s.songId() + ": " + s.title());
            }
        }
    }

    private int unpackSongId(long key) {
        return (int) (key >> 32);
    }

    private int unpackOffset(long key) {
        return (int) key;
    }


    
}
