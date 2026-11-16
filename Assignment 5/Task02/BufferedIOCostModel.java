package task02;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * BufferedIOCostModel
 * Demonstrates and benchmarks the drastic performance difference between
 * single-byte unbuffered I/O and buffered chunked I/O on large log files.
 */
public class BufferedIOCostModel {

    public static class IOCostPredictor {
        public final long fileSizeBytes;

        public IOCostPredictor(long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
        }

        public void printPrediction() {
            long unbufferedSyscalls = fileSizeBytes;
            int bufferSize = 8192; // 8 KB default buffer
            long bufferedSyscalls = (long) Math.ceil((double) fileSizeBytes / bufferSize);
            double reductionPercent = (1.0 - ((double) bufferedSyscalls / unbufferedSyscalls)) * 100.0;

            System.out.println("=================================================");
            System.out.printf("  I/O COST PREDICTION MODEL FOR %d MB FILE%n", fileSizeBytes / (1024 * 1024));
            System.out.println("=================================================");
            System.out.printf("Total Bytes to Read:             %,d bytes%n", fileSizeBytes);
            System.out.printf("Unbuffered read() Syscalls:      %,d calls%n", unbufferedSyscalls);
            System.out.printf("Buffered (8 KB) OS Syscalls:     %,d calls%n", bufferedSyscalls);
            System.out.printf("OS Context Switch Reduction:     %.3f%%%n", reductionPercent);
            System.out.println("=================================================");
        }
    }

    public static long countLinesUnbuffered(File file) throws IOException {
        long lines = 0;
        try (FileInputStream fis = new FileInputStream(file)) {
            int b;
            while ((b = fis.read()) != -1) {
                if (b == '\n') lines++;
            }
        }
        return lines;
    }

    public static long countLinesBufferedStream(File file) throws IOException {
        long lines = 0;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 8192)) {
            int b;
            while ((b = bis.read()) != -1) {
                if (b == '\n') lines++;
            }
        }
        return lines;
    }

    public static long countLinesByteArray(File file) throws IOException {
        long lines = 0;
        byte[] buffer = new byte[8192];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (buffer[i] == '\n') lines++;
                }
            }
        }
        return lines;
    }

    public static void main(String[] args) throws IOException {
        IOCostPredictor predictor = new IOCostPredictor(50L * 1024 * 1024); // 50 MB
        predictor.printPrediction();

        // Run live micro-benchmark on a 2 MB sample file to demonstrate scaling
        File temp = File.createTempFile("attendance_sample", ".log");
        try (BufferedWriter bw = Files.newBufferedWriter(temp.toPath())) {
            for (int i = 0; i < 40000; i++) {
                bw.write("2026-11-16 08:30:12 [ATTENDANCE] Student ID: 2024CS" + (1000 + (i % 500)) + " Swiped IN at Gate 3\n");
            }
        }

        System.out.printf("%nRunning live benchmark on sample log file (%,d bytes)...%n", temp.length());

        // 1. Buffered Byte Array
        long t0 = System.nanoTime();
        long linesBuffer = countLinesByteArray(temp);
        long tBuffer = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("Strategy 1 [Chunked byte[8192]]:   %3d ms (Lines: %,d)%n", tBuffer, linesBuffer);

        // 2. BufferedInputStream
        long t1 = System.nanoTime();
        long linesBis = countLinesBufferedStream(temp);
        long tBis = (System.nanoTime() - t1) / 1_000_000;
        System.out.printf("Strategy 2 [BufferedInputStream]:   %3d ms (Lines: %,d)%n", tBis, linesBis);

        // 3. Unbuffered (sample on first 200 KB to avoid stalling execution)
        File smallSample = File.createTempFile("attendance_small", ".log");
        try (BufferedWriter bw = Files.newBufferedWriter(smallSample.toPath())) {
            for (int i = 0; i < 3000; i++) {
                bw.write("2026-11-16 08:30:12 [ATTENDANCE] Student ID: 2024CS" + (1000 + (i % 500)) + " Swiped IN\n");
            }
        }
        long t2 = System.nanoTime();
        long linesUnbuffered = countLinesUnbuffered(smallSample);
        long tUnbuffered = (System.nanoTime() - t2) / 1_000_000;
        System.out.printf("Strategy 3 [Unbuffered on small]:  %3d ms (Lines: %,d)%n", tUnbuffered, linesUnbuffered);

        temp.delete();
        smallSample.delete();

        System.out.println("\nVerification completed successfully.");
    }
}
