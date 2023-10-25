package org.example;

import org.example.config.AppConfig;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;

public class FileDownloader {
    public static void main(String[] args) throws IOException {
        String fileUrl = "https://download-cdn.jetbrains.com/python/pycharm-community-2023.2.2.exe"; // Replace with the URL of the file you want to download
        String DOWNLOAD_PATH = "D:\\University\\ThreeYear\\semester5\\PBL4\\Downloads";
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/")+1);
        String destinationPath = DOWNLOAD_PATH + File.separator + filename; // Replace with the local file path where you want to save the downloaded file
        int numThreads = 8; // Number of threads to use for downloading

        Path tempDir = Files.createTempDirectory("mytempdir_");
        try {
            URL url = new URL(fileUrl);
            long contentLength = url.openConnection().getContentLengthLong();

            // Calculate the range of bytes each thread should download
            long chunkSize = contentLength / numThreads;

            // Create a directory for temporary files
            boolean[] segmentStatus = new boolean[numThreads];


            // Create and start multiple threads to download the file in parallel
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                segmentStatus[i] = true;
                int segment = i;
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
                        File temp = new File(tempFile.toString());
                        System.out.println(temp.length());
                    } catch (ConnectException e) {
                        System.out.println("Mat ket noi!!!!");
                        segmentStatus[segment] = false;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                threads[i].start();
            }

            System.out.println("check!!!!");
            // Wait for all threads to finish
            for (Thread thread : threads) {
                thread.join();
            }

            for(int i =0; i < numThreads; i++) {
                System.out.print(segmentStatus[i] + " ");
                if(!segmentStatus[i]) {
                    System.out.println();
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempFile = tempDir.resolve("part-" + i + ".tmp");
                    try {
                        URL partialUrl = new URL(fileUrl);
                        HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
                        connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);

                        try (InputStream in = connection.getInputStream()) {
                            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        System.out.println("mat ket noi2!!!");
                    }
                }
            }

            Path finalPath = Paths.get(destinationPath);
            Files.createDirectories(finalPath.getParent());
            File dir = new File(finalPath.getParent().toString());
            System.out.println(filename);
            for(String arr : dir.list()) {
                if(arr.compareTo(filename) == 0) {
                    new File(destinationPath).delete();
                }
            }

            // Merge downloaded parts into the final file
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

