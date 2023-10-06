package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;

public class FileDownloader {
    public static void main(String[] args) throws IOException {
        String fileUrl = "https://nodejs.org/dist/v16.13.1/node-v16.13.1-x64.msi"; // Replace with the URL of the file you want to download
        String destinationPath = "D:\\University\\ThreeYear\\semester5\\PBL4\\Downloads\\node-v16.13.1-x64.msi"; // Replace with the local file path where you want to save the downloaded file
        int numThreads = 8; // Number of threads to use for downloading

        Path tempDir = Files.createTempDirectory("mytempdir_");
        try {
            URL url = new URL(fileUrl);
            long contentLength = url.openConnection().getContentLengthLong();

            // Calculate the range of bytes each thread should download
            long chunkSize = contentLength / numThreads;

            // Create a directory for temporary files


            // Create and start multiple threads to download the file in parallel
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                long startByte = i * chunkSize;
                long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                Path tempFile = tempDir.resolve("part-" + i + ".tmp");

                threads[i] = new Thread(() -> {
                    try {
                        URL partialUrl = new URL(fileUrl);
                        HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
                        connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);

                        try (InputStream in = connection.getInputStream()) {
                            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                threads[i].start();
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                thread.join();
            }

            // Merge downloaded parts into the final file
            Path finalPath = Paths.get(destinationPath);
            Files.createDirectories(finalPath.getParent());
            try (OutputStream outputStream = Files.newOutputStream(finalPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                for (int i = 0; i < numThreads; i++) {
                    Path tempFile = tempDir.resolve("part-" + i + ".tmp");
                    Files.copy(tempFile, outputStream);
                    Files.delete(tempFile);
                }
            }

            Files.delete(tempDir);

            System.out.println("File downloaded successfully.");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error downloading file: " + e.getMessage());
            Files.delete(tempDir);
        }
    }
}

