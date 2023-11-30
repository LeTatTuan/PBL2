package idm.controller;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import idm.models.DownloadInfoOneChunk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

public class DownloadThread implements Runnable {
    private String url;
    private long startByte;
    private long endByte;
    private Path tempDownloadingFile;
    private ProgressBar progressBar;
    private DownloadInfoOneChunk downloadInfoOneChunk;

    public DownloadThread(String url, long startByte, long endByte, Path tempDownloadingFile, ProgressBar progressBar, DownloadInfoOneChunk downloadInfoOneChunk) {
        this.url = url;
        this.startByte = startByte;
        this.endByte = endByte;
        this.tempDownloadingFile = tempDownloadingFile;
        this.progressBar = progressBar;
        this.downloadInfoOneChunk = downloadInfoOneChunk;
    }

    @Override
    public void run() {
        try {
            URL partialUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempDownloadingFile.toFile().getPath()));
            ) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesOneChunk = endByte - startByte + 1;
                long totalBytesRead = 0;
                int countN = 1;
                boolean isSetInfor = false;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if(totalBytesOneChunk == totalBytesRead) isSetInfor = true;
                    updateProgress(totalBytesRead, totalBytesOneChunk, isSetInfor);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(long workDone, long max, boolean isSetInfor) {
        if (Platform.isFxApplicationThread()) {
            progressBar.setProgress((double) workDone / max);
            downloadInfoOneChunk.setDownloaded(downloadInfoOneChunk.formatFileSize(workDone));
            if (isSetInfor) {
                downloadInfoOneChunk.setInfo("Received data successfully...");
            }
        } else {
            Platform.runLater(() -> {
                progressBar.setProgress((double) workDone / max);
                downloadInfoOneChunk.setDownloaded(downloadInfoOneChunk.formatFileSize(workDone));
                if (isSetInfor) {
                    downloadInfoOneChunk.setInfo("Received data successfully...");
                }
            });
        }
    }
}
