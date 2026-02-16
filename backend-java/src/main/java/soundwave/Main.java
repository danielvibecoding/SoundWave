package soundwave;
import soundwave.audio.Ffmpeg;
import soundwave.audio.ReadWAVArray;
import soundwave.FFT.ApplyFFT;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
// dynamic lists
import java.util.*;
// ArrayList<integers> xs = new ArrayList<>();
// xs.add(10);
// xs.add(20);
// int first = xs.get(0);

//List<String>

// Hashmaps
// HashMap<Integer, String> m = new HashMap<>();
// m.put(123, "song");
// String v = m.get(123);
// boolean has = m.containsKey(123)


// Strings
// String s = null;
// s.equals("hi") works s == "hi" doesnt work


// Null
// String s = null;
// int x = 0; (primitives always have a value)



/* try {
    int n = Integer.parseInt("123");
} catch (NumberFormatException e) {
    System.out.println("bad number")
}



*/
// This is for soundwave/fingerprint/Peak.java
// every .java file starts with a package line that matches the folder
// package soundwave.fingerprint;

// public record Peak(double timeSec, double freqHz, double magnitude) {}

public class Main {
    int num = 5;
    char letter = 'n';
    String name = "hello";
    boolean ok = true;

        // Path source = Paths.get("/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/testFiles" + );
        // Path target = Paths.get("/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/output");
        // to be done here copy from testFiles into output then perform operation with path in mind

    public static void main(String[] args) throws IOException, InterruptedException, Exception {
        if (args.length > 2) {
            System.err.println("Usage: java Main <filename>");
        }


        String filename = args[0];
        String src = "/Users/dv/projects/Soundwave/backend-java/src/main/java/soundwave/testFiles/";
        Path path = Paths.get(src);
        Path combinedPath = path.resolve(filename);

        Path output = Ffmpeg.toMonoWav11025(combinedPath);
        double[] doubleArray = ReadWAVArray.wavtoByteArray(output);
        // Files.write(Paths.get("/Users/dv/projects/Soundwave/backend-java/src/main/java/soundwave/output/result.txt"), doubleArray);
        ApplyFFT.slidingWindow(doubleArray);
        // SpecPlot.saveSpectrogramPNG(specDb)

        System.out.println("SoundWave Java backend is alive.");
    }
}

// if its a general helper function (doesnt depend on a specific thing) make static

// something a specific object does (depends on its data not static)

// if () {}... else {}
// for (int i = 0; i < 10; i++) {}
// while (cond) {}

// static double square (double v) {
// return v*v
// }

// int [] arr = new int[10]
// arr[0] = 7