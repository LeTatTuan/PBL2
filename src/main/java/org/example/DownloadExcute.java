//package org.example;
//
//import org.example.models.FileInfo;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.file.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class DownloadExcute {
//    private FileInfo file;
//    DownloadManager manager;
//
//    public DownloadExcute(FileInfo file, DownloadManager manager) {
//        this.file = file;
//        this.manager = manager;
//        this.downloading();
//    }
//
//    public void downloading() {
//        this.file.setStatus("DOWNLOADING");
//        this.manager.updateUI(this.file, );
//        try {
//            String fileUrl = this.file.getUrl();
//            String filename = this.file.getName();
//            String destinationPath = this.file.getPath();
//            int numThreads = 8;
//            Path tempDir = Files.createTempDirectory("tempDownloading_");
//            try {
//                URL url = new URL(fileUrl);
//                long contentLength = url.openConnection().getContentLength();
//
//                long chunkSize = contentLength / numThreads;
//
//                Thread[] threads = new Thread[numThreads];
//                for (int i = 0; i < numThreads; i++) {
//                    long startByte = i * chunkSize;
//                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
//                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
//
//                    threads[i] = new Thread(() -> {
//                        try {
//                            URL partialUrl = new URL(fileUrl);
//                            HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
//                            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
//
//                            try(InputStream in = connection.getInputStream()) {
//                                Files.copy(in,tempDownloadingFile, StandardCopyOption.REPLACE_EXISTING);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                    threads[i].start();
//                }
//
//                for(Thread thread : threads) {
//                    thread.join();
//                }
//
//                Path finalPath = Paths.get(destinationPath);
//                Files.createDirectories(finalPath.getParent());
//                File dir = new File(finalPath.getParent().toString());
//                for(String arr : dir.list()) {
//                    if(arr.compareTo(filename) == 0) {
//                        new File(destinationPath).delete();
//                    }
//                }
//
//                try(OutputStream out = Files.newOutputStream(finalPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
//                    for(int i = 0; i < numThreads; i++) {
//                        Path tempFile = tempDir.resolve("part-" + i + ".tmp");
//                        Files.copy(tempFile, out);
//                        Files.delete(tempFile);
//                    }
//                }
//                Files.delete(tempDir);
//                this.file.setStatus("DONE");
//                System.out.println("File downloaded successfully.");
//            } catch (IOException | InterruptedException e) {
//                this.file.setStatus("FAILED");
//                System.err.println("Error downloading file: " + e.getMessage());
//                Files.delete(tempDir);
//            }
//        } catch (Exception e) {
//            this.file.setStatus("FAILED");
//            System.err.println("Error downloading file: " + e.getMessage());
//        }
//        this.manager.updateUI(this.file);
//    }
//}
//
