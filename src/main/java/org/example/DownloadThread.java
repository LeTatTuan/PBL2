package org.example;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

public class DownloadThread implements Runnable {
    private String fileUrl;
    private long startByte;
    private long endByte;
    private Path tempDownloadingFile;
    private ProgressBar progressBar;
    public DownloadThread(String fileUrl, long startByte, long endByte, Path tempDownloadingFile, ProgressBar progressBar) {
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
            RandomAccessFile out = new RandomAccessFile(tempDownloadingFile.toFile().getPath(), "rw");
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            long totalBytesOneChunk = endByte - startByte + 1;
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
    }
    private void updateProgress(long workDone, long max) {
        if (Platform.isFxApplicationThread()) {
            progressBar.setProgress((double) workDone / max);
        } else {
            Platform.runLater(() -> progressBar.setProgress((double) workDone / max));
        }
    }
}
