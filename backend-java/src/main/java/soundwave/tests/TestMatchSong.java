package soundwave.tests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.file.*;

import soundwave.FFT.ApplyFFT;
import soundwave.audio.Ffmpeg;
import soundwave.audio.ReadWAVArray;
import soundwave.db.SqliteFunc;
import soundwave.fingerprint.FilterResult;
import soundwave.fingerprint.addressFingerprint;
import soundwave.fingerprint.filter;
import soundwave.fingerprint.queryFingerprint;

public class TestMatchSong {
    public static void main(String[] args) throws SQLException, IOException, InterruptedException, Exception {

        // if (args.length < 3) {
        //     System.out.println("Usage: add <dbFile>, <songsDir>, <snippetDir>");
        //     return;
        // }

        String dbFile = args[0];
        String dbSrc = "/Users/dv/projects/Soundwave/backend-java/src/main/java/soundwave/db/";
        String combinedDbPath = dbSrc + dbFile;
        SqliteFunc db = new SqliteFunc(combinedDbPath);

        // Path songsDir = Path.of(args[1]);
        // Path snippet = Path.of(args[2]);

        Path songsDir = Path.of("/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/tests/matchSongsTestFolder");
        Path snippet = Path.of("/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/tests/testAudioSnippets/1.mp3");
        Connection conn = db.connect();
        // db.reset();

            // try(DirectoryStream<Path> ds = Files.newDirectoryStream(songsDir)) {
            //     for (Path p : ds) {
            //         if (Files.isDirectory(p)) continue;


            //         Path output = Ffmpeg.toMonoWav11025(p);
            //         double[] doubleArray = ReadWAVArray.wavtoByteArray(output);
            //         double[][] spectrogram = ApplyFFT.slidingWindow(doubleArray);

            //         String title = p.getFileName().toString();
            //         int songId = db.insertSongName(title);
            //         filter f = new filter(spectrogram);
            //         FilterResult fr = f.filterLoop(spectrogram);

            //         addressFingerprint a = new addressFingerprint(fr);
            //         a.addSongFingerPrint(db, conn, songId);

            //         System.out.println("Indexed songId=" + songId + " title=" + title);

            //     }
            // }

            Path m = Ffmpeg.toMonoWav11025(snippet);
            double[] doubleArray = ReadWAVArray.wavtoByteArray(m);
            double[][] spectrogram = ApplyFFT.slidingWindow(doubleArray);

            filter f = new filter(spectrogram);
            FilterResult fr = f.filterLoop(spectrogram);

            queryFingerprint q = new queryFingerprint(fr);
            
            OutputFunction o = new OutputFunction();
            o.resetContents();

            q.getResults(db, conn, o);

    } 
}
