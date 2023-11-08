package org.example;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.example.models.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;

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
            int numThreads = 8;
            Path tempDir = Files.createTempDirectory("Downloading_");
            try {
                URL url = new URL(fileUrl);
                long contentLength = url.openConnection().getContentLength();

                long chunkSize = contentLength / numThreads;
                Thread[] threads = new Thread[numThreads];
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
                    DownloadTask downloadTask = new DownloadTask(fileUrl, tempDownloadingFile, startByte, endByte);
                    progressBarList.get(i).progressProperty().bind(downloadTask.progressProperty());
                    threads[i] = new Thread(downloadTask);
                    System.out.println(threads[i].getPriority());
                    threads[i].start();
                }
                for(Thread thread : threads) {
                    thread.join();
                }

                Path finalPath = Paths.get(destinationPath);
                Files.createDirectories(finalPath.getParent());
                File dir = new File(finalPath.getParent().toString());
                for(String arr : dir.list()) {
                    if(arr.compareTo(filename) == 0) {
                        new File(destinationPath).delete();
                    }
                }

                try(OutputStream out = Files.newOutputStream(finalPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    for(int i = 0; i < numThreads; i++) {
                        Path tempFile = tempDir.resolve("part-" + i + ".tmp");
                        Files.copy(tempFile, out);
                        Files.delete(tempFile);
                    }
                }
                Files.delete(tempDir);
                this.file.setStatus("DONE");
                System.out.println("File downloaded successfully.");
            } catch (IOException | InterruptedException e) {
                this.file.setStatus("FAILED");
                System.err.println("Error downloading file: " + e.getMessage());
                Files.delete(tempDir);
            }
        } catch (Exception e) {
            this.file.setStatus("FAILED");
            System.err.println("Error downloading file: " + e.getMessage());
        }
    }
}

class DownloadTask extends Task<Void> {
    private final String fileUrl;
    private final Path tempDownloadingFile;
    private final long startByte;
    private  final long endByte;

    public DownloadTask(String fileUrl, Path tempDownloadingFile, long startByte, long endByte) {
        this.fileUrl = fileUrl;
        this.tempDownloadingFile = tempDownloadingFile;
        this.startByte = startByte;
        this.endByte = endByte;
    }

    @Override
    protected Void call() {
        try {
            URL partialUrl = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);

            try(InputStream in = connection.getInputStream()) {
                Files.copy(in,tempDownloadingFile, StandardCopyOption.REPLACE_EXISTING);
            }
            updateProgress(1, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
