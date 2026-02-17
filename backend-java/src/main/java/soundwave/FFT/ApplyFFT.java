// files for the printing of the spectrogram


package soundwave.FFT;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import java.io.*;


import java.nio.charset.StandardCharsets;


import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

import javax.sound.sampled.*;

public class ApplyFFT {
    public static void slidingWindow(double[] samples) throws Exception {
        int windowSize = 1024;
        int N = samples.length; 

        double[][] spectrogram = new double[512][N/1024];

        if (samples == null || samples.length == 0) {
            System.out.println("The array is null or empty. Cannot process.");
        }
    

        for (int start = 0, j = 0; start + windowSize <= N; start += windowSize, j ++) {
            double[] realArray = new double[windowSize];
            System.arraycopy(samples, start, realArray, 0, windowSize);

            applyHamming(realArray);

            double[] imArray = new double[windowSize];

            FFT(realArray, imArray);
            doubleArraysToSpectrogram(realArray, imArray, j, spectrogram);
        } 
        save(spectrogram);
    }
    
    private static void applyHamming(double[] realArray) {
        int windowSize = realArray.length;
        for (int n = 0; n < windowSize; n ++) {
            double hammingWindow = 0.54-(0.46 * Math.cos(2 * Math.PI * n/(windowSize - 1)));
            realArray[n] *= hammingWindow;
        }
        return;
    }

    private static void FFT(double[] re, double[] im) {
        bitReversal(re);
        bitReversal(im);

        for (int len = 2; len <= re.length; len *= 2) {
            int half = len/2;
            
            double angleStep = (-2.0 * Math.PI)/len;
            double wLenCos = Math.cos(angleStep);
            double wLenSin = Math.sin(angleStep);

            for (int blockStart = 0; blockStart < re.length; blockStart += len) {
                double wCos = 1.0;
                double wSin = 0.0;

                for (int j = 0; j < half; j++) {
                    int u = blockStart + j;
                    int v = u + half;

                    double tRe = (re[v]*wCos) - (im[v]*wSin);
                    double tIm = (re[v]*wSin) + (im[v]*wCos);

                    double uRe = re[u];
                    double uIm = im[u];

                    // butterfly output
                    re[u] = uRe + tRe;
                    im[u] = uIm + tIm;
                    re[v] = uRe - tRe;
                    im[v] = uIm - tIm;

                    // w = w*wLen
                    double nextWCos = wCos*wLenCos - wSin*wLenSin;
                    double nextWSin = wCos*wLenSin + wSin*wLenCos;
                    wCos = nextWCos;
                    wSin = nextWSin;

                }
            }
        }
    }

    private static void bitReversal(double[] realArray) {
        int j = 0;
        int N = realArray.length;
        for (int i = 1; i < N; i ++) {
            int bit = N/2;
            while ((j & bit) != 0) {
                j ^= bit;
                bit = bit/2;
            }
            j ^= bit;
            if (i < j) {
                double temp = realArray[i];
                realArray[i] = realArray[j];
                realArray[j] = temp;
            }

        }
    }

    private static void doubleArraysToSpectrogram(double re[], double im[], int j, double spectrogram[][]) {
        for (int i = 0; i < 512; i++) {
            double power = re[i] * re[i] + im[i] * im[i];
            double db = 10.0 * Math.log10(power + 1e-12);
            spectrogram[i][j] = db;
        }
    }

    public static void save(double[][] spec) throws Exception {
        int T = spec.length;
        int K = spec[0].length;

        // 1) find min/max for normalization (or clamp to fixed range)
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int t = 0; t < T; t++) {
            for (int k = 0; k < K; k++) {
                double v = spec[t][k];
                if (v < min) min = v;
                if (v > max) max = v;
            }
        }
        double range = Math.max(1e-9, max - min);

        // 2) image: width=time, height=freq
        BufferedImage img = new BufferedImage(T, K, BufferedImage.TYPE_BYTE_GRAY);

        for (int t = 0; t < T; t++) {
            for (int k = 0; k < K; k++) {
                double v = spec[t][k];

                // normalize to 0..255
                int gray = (int)Math.round(255.0 * (v - min) / range);
                if (gray < 0) gray = 0;
                if (gray > 255) gray = 255;

                // flip vertical so low freq at bottom
                int y = (K - 1) - k;

                int rgb = (gray << 16) | (gray << 8) | gray;
                img.setRGB(t, y, rgb);
            }
        }
        String filename = "/Users/dv/projects/SoundWave/backend-java/src/main/java/soundwave/output/output.png";

        // Path filename = Paths.get(path);
        ImageIO.write(img, "png", new File(filename));
        System.out.println("bins=" + spec.length + " frames=" + spec[0].length);

    
    }
}





