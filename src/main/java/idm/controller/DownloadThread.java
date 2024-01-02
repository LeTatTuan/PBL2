package idm.controller;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import idm.models.DownloadInfoOneChunk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadThread implements Runnable {
    private final String url;
    private final long startByte;
    private final long endByte;
    private final Path tempDownloadingFile;
    private final ProgressBar progressBar;
    private final DownloadInfoOneChunk downloadInfoOneChunk;
    private final AtomicBoolean paused;
    private final AtomicBoolean cancelled;

    private final Object pauseLock = new Object();

    public DownloadThread(String url, long startByte, long endByte, Path tempDownloadingFile, ProgressBar progressBar, DownloadInfoOneChunk downloadInfoOneChunk) {
        this.url = url;
        this.startByte = startByte;
        this.endByte = endByte;
        this.tempDownloadingFile = tempDownloadingFile;
        this.progressBar = progressBar;
        this.downloadInfoOneChunk = downloadInfoOneChunk;
        this.paused = new AtomicBoolean(false);
        this.cancelled = new AtomicBoolean(false);
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        synchronized (pauseLock) {
            paused.set(false);
            pauseLock.notifyAll(); // Thức tỉnh tất cả các luồng đã bị tạm dừng
        }
    }

    public void cancel() {
        // Gọi từ bên ngoài để huỷ luồng tải
        cancelled.set(true);
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
                    synchronized (pauseLock) {
                        while (paused.get()) {
                            // Nếu đang paused, tạm dừng luồng và chờ cho đến khi được thức tỉnh
                            try {
                                pauseLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (cancelled.get()) {
                        return;
                    }
                    out.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (Math.abs(totalBytesRead - totalBytesOneChunk) < 1e-3)
                        isSetInfor = true;
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
            if (!paused.get()) {
                progressBar.setProgress((double) workDone / max);
                downloadInfoOneChunk.setDownloaded(downloadInfoOneChunk.formatFileSizeToByte(workDone));
                if (isSetInfor) {
                    downloadInfoOneChunk.setInfo("Received data successfully");
                }
            }
        } else {
            Platform.runLater(() -> {
                if (!paused.get()) {
                    progressBar.setProgress((double) workDone / max);
                    downloadInfoOneChunk.setDownloaded(downloadInfoOneChunk.formatFileSizeToByte(workDone));
                }
                if (isSetInfor) {
                    downloadInfoOneChunk.setInfo("Received data successfully");
                }
            });
        }
    }
}
