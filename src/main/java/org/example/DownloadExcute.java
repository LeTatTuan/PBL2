package org.example;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.example.models.FileInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadExcute {
    private FileInfo file;
    DownloadManager manager;
    List<ProgressBar> progressBarList;

    public DownloadExcute(FileInfo file, DownloadManager manager, List<ProgressBar> progressBarList) {
        this.file = file;
        this.manager = manager;
        this.progressBarList = progressBarList;
        this.downloading();
    }
    public void downloading() {
        try {
            String fileUrl = this.file.getUrl();
            String filename = this.file.getName();
            String destinationPath = this.file.getPath();

            Path finalPath = Paths.get(destinationPath);
            Files.createDirectories(finalPath.getParent());
            File dir = new File(finalPath.getParent().toString());
            for(String arr : dir.list()) {
                if(arr.compareTo(filename) == 0) {
                    new File(destinationPath).delete();
                }
            }

            int numThreads = 8;
            try {
                URL url = new URL(fileUrl);
                long contentLength = url.openConnection().getContentLength();
                if(contentLength > 200*1024*1024) {
                    numThreads = 8;
                }
                if (contentLength <= 0) {
                    System.out.println("Khong the xac dinh kich thuoc tep tin.");
                    return;
                }
                //ExecutorService executor = Executors.newFixedThreadPool(numThreads);
                Thread[] threads = new Thread[numThreads];
                long chunkSize = contentLength / numThreads;
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    threads[i] = new Thread(new DownloadTask(fileUrl, startByte, endByte, destinationPath, progressBarList.get(i)));
                    threads[i].start();
                }

                for(Thread thread : threads) {
                    thread.join();
                }

                this.file.setStatus("DONE");
                System.out.println("File downloaded successfully.");
                for(ProgressBar progressBar : progressBarList) {
                    progressBar.setProgress(0.0);
                }
            } catch (IOException e) {
                this.file.setStatus("FAILED");
                System.err.println("Error downloading file: " + e.getMessage());
            }
        } catch (Exception e) {
            this.file.setStatus("FAILED");
            System.err.println("Error downloading file: " + e.getMessage());
        }
    }
}

class DownloadTask implements Runnable {
    private final String fileUrl;
    private final long startByte;
    private  final long endByte;
    private final  String tempDownloadingFile;
    private  final  ProgressBar progressBar;

    public DownloadTask(String fileUrl, long startByte, long endByte, String tempDownloadingFile, ProgressBar progressBar) {
        this.fileUrl = fileUrl;
        this.startByte = startByte;
        this.endByte = endByte;
        this.tempDownloadingFile = tempDownloadingFile;
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        try {
            URL partialUrl = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            RandomAccessFile out = new RandomAccessFile(tempDownloadingFile, "rw");
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            long totalBytes = endByte - startByte + 1;
            out.seek(startByte);
            System.out.println("Range bytes = " + startByte + " - " + endByte);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                int progress = (int)(totalBytesRead * 100.0/totalBytes);
                progressBar.setProgress(progress);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

