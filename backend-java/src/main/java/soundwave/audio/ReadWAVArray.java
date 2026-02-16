package soundwave.audio;

import java.io.*;


import java.nio.charset.StandardCharsets;


import java.nio.file.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.*;

// import java.sound.sampled.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class ReadWAVArray {
    // private byte[] entireFileData;

    public static double[] wavtoByteArray(Path input) {
        try {
            File file = input.toFile();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            int read;
            byte[] buff = new byte[1024];

            while ((read = audioStream.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();

            byte[] audioBytes = out.toByteArray();
            return initDoubleArray(audioBytes);
        } catch(IOException | UnsupportedAudioFileException e) {
            System.err.println("Failed to move file: " + e.getMessage());
            e.printStackTrace();
        }
        double[] weMessedUp = new double[0];
        return weMessedUp;
    }

    private static double[] initDoubleArray(byte[] audioBytes) {
        int length = audioBytes.length;
        int numSamples = length/2;
        double[] samples = new double[numSamples];

        ByteBuffer bb = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < numSamples; i++) {
            short s = bb.getShort();
            double x = s/32768.0;
            samples[i] = x;
        }
        return samples;
    }


}