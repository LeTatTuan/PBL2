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

public class DownloadExcute {
    private FileInfo file;
    List<ProgressBar> progressBarList;

    public DownloadExcute(FileInfo file, List<ProgressBar> progressBarList) {
        this.file = file;
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

                Thread[] threads = new Thread[numThreads];
                long chunkSize = contentLength / numThreads;
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    DownloadTask downloadTask = new DownloadTask(fileUrl, startByte, endByte, destinationPath);
                    int finalI = i;

                    downloadTask.setOnSucceeded(event -> {
                        Platform.runLater(() -> {
                            progressBarList.get(finalI).progressProperty().unbind(); // Unbind after completion
                        });
                    });

                    Platform.runLater(() -> {
                        progressBarList.get(finalI).progressProperty().bind(downloadTask.progressProperty());
                    });
                    threads[i] = new Thread(downloadTask);
                    threads[i].start();
                }

                for(Thread thread : threads) {
                    thread.join();
                }
                this.file.setStatus("DONE");
                System.out.println("File downloaded successfully.");
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

class DownloadTask extends Task<Void> {
    private final String fileUrl;
    private final long startByte;
    private  final long endByte;
    private final  String destinationPath;

    public DownloadTask(String fileUrl, long startByte, long endByte, String destinationPath) {
        this.fileUrl = fileUrl;
        this.startByte = startByte;
        this.endByte = endByte;
        this.destinationPath = destinationPath;
    }

    @Override
    public Void call() {
        try {
            URL partialUrl = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            RandomAccessFile out = new RandomAccessFile(destinationPath, "rw");
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            long totalBytesOneChunk = endByte - startByte + 1;

            out.seek(startByte);

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                updateProgress(totalBytesRead, totalBytesOneChunk);
            }
            updateProgress(1,1);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

