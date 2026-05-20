package soundwave.tests;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import soundwave.FFT.ApplyFFT;
import soundwave.audio.Ffmpeg;
import soundwave.audio.ReadWAVArray;
import soundwave.db.SqliteFunc;
import soundwave.fingerprint.FilterResult;
import soundwave.fingerprint.addressFingerprint;
import soundwave.fingerprint.filter;

public class TestDeleteSong {
     public static void main(String[] args) throws SQLException, IOException, InterruptedException, Exception {
        if (args.length == 0) {
            args = new String[]{"Test.db", "WonderWall.mp3", "Wonderwall"};
        }

        if (args.length < 3) {
            System.out.println("Usage: add <dbFile> <audioFile> <title...>");
            return;
        }


        String dbFile = args[0];
        Path audioFile = Path.of(args[1]);
        String title = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        
        String src = "/Users/dv/projects/Soundwave/backend-java/src/main/java/soundwave/testFiles/";
        Path audioPath = Paths.get(src);
        Path combinedPath = audioPath.resolve(audioFile);

        String dbSrc = "/Users/dv/projects/Soundwave/backend-java/src/main/java/soundwave/db/";
        String combinedDbPath = dbSrc + dbFile;

        
        SqliteFunc db = new SqliteFunc(combinedDbPath);


        Path output = Ffmpeg.toMonoWav11025(combinedPath);
        double[] doubleArray = ReadWAVArray.wavtoByteArray(output);
        double[][] spectrogram = ApplyFFT.slidingWindow(doubleArray);

        try (Connection conn = db.connect()) {
            db.deleteSong(4);
        }
    }
}
