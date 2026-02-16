package soundwave.audio;

import java.io.*;
// imports input output stuff, fileinputstream
// not used so far for this program

import java.nio.charset.StandardCharsets;
// standard charsets, converts text output from ffmpeg into a Java String reliabily
// new String(bytes, StandardCharsets.UTF_8)

import java.nio.file.*;



import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// public static Path toMonoWav11025

import java.util.*;

public class Ffmpeg {

    // Convert any input audio file to mono WAV at 11025 Hz (good for Shazam-style work)
    public static Path toMonoWav11025(Path input) throws IOException, InterruptedException {

        Path src = input;
        Path dest = Paths.get("/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/output/" + src.getFileName());

        try {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to move file: " + e.getMessage());
            e.printStackTrace();
        }
        
        Path out = dest.resolveSibling(stripExt(dest.getFileName().toString()) + ".wav");
        System.out.println("OUT:" + out);
        // If "ffmpeg" isn't found, replace "ffmpeg" with "/opt/homebrew/bin/ffmpeg"
        List<String> cmd = List.of(
                "ffmpeg",
                "-y",                 // overwrite output if exists
                "-hide_banner",
                "-loglevel", "error", // only show errors
                "-i", input.toString(),
                "-ac", "1",           // mono
                "-ar", "11025",       // sample rate
                "-c:a", "pcm_s16le",  // signed 16 bit little endian
                out.toString()
        );

        try {
           Files.deleteIfExists(dest);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            e.printStackTrace();
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // merge stderr+stdout
        // Actually launches ffmpeg and returns a process object
        Process p = pb.start();

        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // wait for it to finish
        int code = p.waitFor();

        if (code != 0) {
            throw new RuntimeException("ffmpeg failed (exit " + code + "):\n" + output);
        }

        return out;
    }

    private static String stripExt(String name) {
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }
}
// FOR MAIN FUNCTION
// public class Main {
//     public static void main(String[] args) throws Exception {
//         Path input = Path.of(args[0]);
//         Path wav = Ffmpeg.toMonoWav11025(input);
//         System.out.println("Converted to: " + wav);
//     }
// }

// vectors * factor + avg vector * 30%