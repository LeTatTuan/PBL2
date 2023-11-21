package org.example;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadThread implements Runnable {
    private String fileUrl;
    private long startByte;
    private long endByte;
    private Path tempDownloadingFile;
    private ProgressBar progressBar;
    private final AtomicBoolean paused;
    private final AtomicBoolean cancelled;

    private final Object pauseLock = new Object();

    public DownloadThread(String fileUrl, long startByte, long endByte, Path tempDownloadingFile, ProgressBar progressBar) {
        this.fileUrl = fileUrl;
        this.startByte = startByte;
        this.endByte = endByte;
        this.tempDownloadingFile = tempDownloadingFile;
        this.progressBar = progressBar;
        this.paused = new AtomicBoolean(false); // Khởi tạo AtomicBoolean

        this.cancelled = new AtomicBoolean(false); // Khởi tạo AtomicBoolean


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
            	synchronized (pauseLock) {
                    while (paused.get()) {
                        try {
                            pauseLock.wait(); // Tạm dừng luồng khi bị pause
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
