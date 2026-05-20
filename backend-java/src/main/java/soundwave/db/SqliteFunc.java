package soundwave.db;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soundwave.tests.OutputFunction;



public class SqliteFunc {
    private String filename;
    private Connection conn;

    public static final String[] INIT_SQL = {
        """
        CREATE TABLE IF NOT EXISTS songs (
            song_id INTEGER PRIMARY KEY AUTOINCREMENT,
            title   TEXT
        );
        """,
        """
        CREATE TABLE IF NOT EXISTS fingerprints (
            addr     INTEGER NOT NULL,
            song_id  INTEGER NOT NULL,
            abs_time INTEGER NOT NULL,
            FOREIGN KEY(song_id) REFERENCES songs(song_id)
        );
        """,
        """
        CREATE INDEX IF NOT EXISTS idx_fingerprints_addr
        ON fingerprints(addr);
        """
    };

    public SqliteFunc(String filename) {
        this.filename = filename;
    }

    public Connection connect() {
        String url = "jdbc:sqlite:" + filename;

        try {
            Connection conn = DriverManager.getConnection(url);

            try (var stmt = conn.createStatement()) {
                System.out.println("Connection to SQLite has been established.");

                for (String sql : INIT_SQL) {
                    stmt.execute(sql);
                }
                this.conn = conn;
                return conn;
            } 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    public int insertSongName(String songName) throws SQLException {
        String insertSql = 
        "INSERT INTO songs(title) VALUES (?)";

        try (var pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, songName);
            pstmt.executeUpdate();

            try (var keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key returned for song insert.");
            }
        }
    }

    public void insertBatch(int songId, int[] addrBatch, int[] timeBatch, int batchIndex) throws SQLException {
        String insertSql = "INSERT INTO fingerprints(addr, song_id, abs_time) VALUES(?,?,?)";

        try (var pstmt = conn.prepareStatement(insertSql)) {
            for(int i = 0; i < batchIndex; i ++) {
                pstmt.setInt(1, addrBatch[i]);
                pstmt.setInt(2, songId);
                pstmt.setInt(3, timeBatch[i]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            pstmt.clearBatch();
        } catch (BatchUpdateException bue) {
            System.err.println("Batch failed!");
            int[] updateCounts = bue.getUpdateCounts();
            // Analyze how many succeeded before failure
            System.err.println("Successful statements: " + updateCounts.length);
        }
    }

    /*
        Version 1.0 this shit did not work bro
    
    */
    // public ResultSet lookup(int[] addrBatch, int[] timeBatch, int batchIndex) throws SQLException {
    //     String querySql = "SELECT addr, song_id, abs_time FROM fingerprints WHERE addr IN (";

    //     try (var pstmt = conn.prepareStatement(querySql)) {
    //         for (int i = 0; i < batchIndex; i ++) {
    //             pstmt.setInt(1, addrBatch[i]);
    //         }

    //         try (ResultSet rs = pstmt.executeQuery()) {
    //             while (rs.next()) {
                
    //             }
    //             return rs;
    //         }
    //     }

    // }

    public void lookup(int[] addrChunk, int count, Map<Integer, Integer> rAddr, Map<Long,Integer> songIdIntegerVotes, OutputFunction o) throws SQLException, IOException {
        if (count <= 0) return;

        StringBuilder sb = new StringBuilder(
            "SELECT addr, song_id, abs_time FROM fingerprints WHERE addr IN ("
        );
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        sb.append(")");

        int totalMatches = 0;


        o.logMatchesHeader();


        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < count; i++) ps.setInt(i + 1, addrChunk[i]);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totalMatches++;
                    
                    int addr = rs.getInt("addr");
                    int songId = rs.getInt("song_id");
                    int tSong = rs.getInt("abs_time");

                    int offset = tSong - rAddr.get(addr);


                    o.logMatches(addr, songId, tSong);



                    // vote key being here pisses me the fuck off but its whatever atp
                    long votingKey = voteKey(songId, offset);

                    songIdIntegerVotes.merge(votingKey, 1, Integer::sum);
                }
                System.out.println("TOTAL MATCHES: " + totalMatches);
            }
        }
        o.Footer();

    }


    public Map<Integer, String> lookupSongNames(int[] songIdBatch) throws SQLException {

        StringBuilder sb = new StringBuilder(
            "SELECT song_id, title FROM songs WHERE song_id IN ("
        );

        for (int i = 0; i < songIdBatch.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        sb.append(")");

        Map<Integer, String> songNames = new HashMap<>();


        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < songIdBatch.length; i ++) {
                ps.setInt(1 + i, songIdBatch[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int songId = rs.getInt("song_id");
                    String title = rs.getString("title");
                    songNames.put(songId, title);
                }
            return songNames;
            }
        }
    }
    

    public void deleteSong(int songId) throws SQLException {
        var delFromFingerPrints = "DELETE FROM fingerprints WHERE song_id = ?";
        var delFromSongs = "DELETE FROM songs WHERE song_id = ?";

        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (var pstmt1 = conn.prepareStatement(delFromFingerPrints);
                var pstmt2 = conn.prepareStatement(delFromSongs)) {
            pstmt1.setInt(1, songId);
            pstmt1.executeUpdate();

            pstmt2.setInt(1, songId);
            int rows = pstmt2.executeUpdate();

            conn.commit();

            if (rows == 0) {
                System.out.println("No song found with song_id=" + songId);
            } else {
                System.out.println("Rows deleted=" + rows);

            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAuto);
        }

    }

    public List<Song> getAllSongs() throws SQLException {
        String sql = "SELECT song_id, title FROM songs ORDER BY song_id";

        List<Song> songs = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int songId = rs.getInt("song_id");
                String title = rs.getString("title");

                songs.add(new Song(songId, title));
            }
        }
        return songs;
    }

    public static void deleteTable(String song) {

    }

    public Connection reset() throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS fingerprints;");
            stmt.executeUpdate("DROP TABLE IF EXISTS songs;");
        }
        return connect(); // rerun CREATE TABLE/INDEX statements
    }


    public int getNumSongs(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM songs;";
        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }


    private long voteKey(int songId, int offset) {
        return ((long) songId << 32) | (offset & 0xffffffffL);
    }
}

