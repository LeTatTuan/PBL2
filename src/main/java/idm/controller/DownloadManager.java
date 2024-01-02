package idm.controller;

import idm.handleEvent.UpdateUIEverySecond;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import idm.config.AppConfig;
import idm.models.DownloadInfoOneChunk;
import idm.models.DownloadInfo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class DownloadManager {
    @FXML
    private TextField urlTextField;
    @FXML
    private TextArea txtDownload;
    @FXML
    private TableView<DownloadInfoOneChunk> tableView;
    private ObservableList<DownloadInfoOneChunk> downloadInfoList;
    @FXML
    private Button BtShowHide;
    @FXML
    private ProgressBar totalProgressBar;
    @FXML
    private ProgressBar progressBar1;
    @FXML
    private ProgressBar progressBar2;
    @FXML
    private ProgressBar progressBar3;
    @FXML
    private ProgressBar progressBar4;
    @FXML
    private ProgressBar progressBar5;
    @FXML
    private ProgressBar progressBar6;
    @FXML
    private ProgressBar progressBar7;
    @FXML
    private ProgressBar progressBar8;
    private List<ProgressBar> progressBarList;
    private long downloadedSize = 0;
    private DownloadInfo downloadInfo;
    private volatile AtomicBoolean paused = new AtomicBoolean(false);
    private volatile AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile AtomicBoolean shClick = new AtomicBoolean(false);
    private volatile AtomicBoolean hide = new AtomicBoolean(false);
    private volatile AtomicBoolean show = new AtomicBoolean(true);
    List<DownloadThread> downloadThreads = new ArrayList<>();
    ExecutorService executorService = Executors.newCachedThreadPool();
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @FXML
    void downloadButtonClicked() {
        cancelled.set(false);
        try {
            String url = this.urlTextField.getText().trim();
            //String filename = url.substring(url.lastIndexOf("/") + 1);
            URL Url = new URL(url);
            String filename = Paths.get(Url.getPath()).getFileName().toString();
            String path = AppConfig.DOWNLOAD_PATH + File.separator + filename;
            String status = "Receiving data...";
            long size = Url.openConnection().getContentLengthLong();
            long downloaded = 0;
            double transferRate = 0;
            this.downloadInfo = new DownloadInfo(url, filename, path, status, size, downloaded, transferRate);
            this.txtDownload.setText(downloadInfo.toString());
            Thread startThread = new Thread(() -> {
                try {
                    startDownload(downloadInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            startThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.urlTextField.setText("");
    }

    @FXML
    void pauseButtonClicked() {
        //updateIntefaceEvery1s.paused();
        paused.set(true);
        downloadThreads.forEach(DownloadThread::pause);
        downloadInfo.setStatus("Paused");
        for (DownloadInfoOneChunk downloadInfoChunk : downloadInfoList) {
            downloadInfoChunk.setInfo("Paused");
        }
    }

    @FXML
    void resumeButtonClicked() {
        paused.set(false);
        downloadThreads.forEach(DownloadThread::resume);
        downloadInfo.setStatus("Receiving data...");
        for (DownloadInfoOneChunk downloadInfoChunk : downloadInfoList) {
            downloadInfoChunk.setInfo("Receiving data...");
        }
    }

    @FXML
    void cancelButtonClicked() {
        cancelled.set(true);
        downloadInfo.setStatus("Cancelled");
        downloadInfo.setDownloaded(0);
        downloadInfo.setTransferRate(0);
        downloadInfo.setTimeLeft(0);
        txtDownload.setText(downloadInfo.toString());
        scheduler.shutdown();
        totalProgressBar.setProgress(0.0);
        downloadThreads.forEach(DownloadThread::cancel);
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        Platform.runLater(() -> {
            progressBarList.forEach(progressBar -> progressBar.setProgress(0));
        });
    }

    @FXML
    void showhideButtonClicked() {
        shClick.set(true);
        show.set(!show.get());
        hide.set(!hide.get());
        if (shClick.get()) {
            if (hide.get() && !show.get()) {
                Platform.runLater(() -> {
                    tableView.setVisible(false);
                    BtShowHide.setText("Show Details");
                    progressBarList.forEach(progressBar -> progressBar.setVisible(false));
                    shClick.set(false);//quan trong
                });
            }
            if (!hide.get() && show.get()) {
                Platform.runLater(() -> {
                    tableView.setVisible(true);
                    BtShowHide.setText("Hide Details");
                    progressBarList.forEach(progressBar -> progressBar.setVisible(true));
                    shClick.set(false);//quan trong
                });
            }
        }
        if (hide.get()) {
            progressBarList.forEach(progressBar -> progressBar.setVisible(true));
            tableView.setVisible(true);
        }
    }


    public void startDownload(DownloadInfo downloadInfo) {
        try {
            for (DownloadInfoOneChunk downloadInfoChunk : downloadInfoList) {
                downloadInfoChunk.setInfo("Receiving data...");
            }
            scheduler = Executors.newScheduledThreadPool(1);
            long downloadedBefore1s = 0;
            UpdateUIEverySecond updateIntefaceEvery1s = new UpdateUIEverySecond(downloadInfo, downloadedBefore1s, paused, cancelled);
            // Lên lịch gọi hàm sau mỗi 1 giây
            scheduler.scheduleAtFixedRate(() -> {
                Platform.runLater(() -> {
                    updateIntefaceEvery1s.run();
                    this.txtDownload.setText(downloadInfo.toString());
                    this.totalProgressBar.setProgress((double) this.downloadInfo.getDownloaded() / this.downloadInfo.getSize());
                });
            }, 0, 1, TimeUnit.SECONDS);


            String fileUrl = downloadInfo.getUrl();
            String filename = downloadInfo.getFileName();
            String destinationPath = downloadInfo.getPath();
            int numThreads = 8;
            Path tempDir = Files.createTempDirectory("Downloading_");
            try {
                URL url = new URL(fileUrl);
                long contentLength = url.openConnection().getContentLengthLong();
                long chunkSize = contentLength / numThreads;
                executorService = Executors.newCachedThreadPool();
                tableView.setItems(downloadInfoList);
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
                    DownloadThread downloadThread = new DownloadThread(downloadInfo.getUrl(), startByte, endByte, tempDownloadingFile, progressBarList.get(i), downloadInfoList.get(i));
                    downloadThreads.add(downloadThread);
                    executorService.execute(downloadThread);
                }
                executorService.shutdown();
                while (true) {
                    if (executorService.isTerminated()) {
                        int duplicateCounter = 1;
                        Path finalPath = Paths.get(destinationPath);
                        while (Files.exists(finalPath)) {
                            String newFilename = getFilename(filename, duplicateCounter);
                            finalPath = Paths.get(finalPath.getParent().toString(), newFilename);
                            duplicateCounter++;
                        }
                        try (OutputStream out = Files.newOutputStream(finalPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                            for (int i = 0; i < numThreads; i++) {
                                Path tempFile = tempDir.resolve("part-" + i + ".tmp");
                                Files.copy(tempFile, out);
                                Files.delete(tempFile);
                            }
                        }
                        Files.delete(tempDir);
                        if (!cancelled.get()) {
                            System.out.println("File downloaded successfully.");
                            for (DownloadInfoOneChunk downloadInfoChunk : downloadInfoList) {
                                downloadInfoChunk.setInfo("Received data successfully");
                            }
                            downloadInfo.setStatus("Received data");
                            downloadInfo.setDownloaded(contentLength);
                            downloadInfo.setTimeLeft(0.0);
                            this.txtDownload.setText(downloadInfo.toString());
                            totalProgressBar.setProgress(1);
                            scheduler.shutdown();
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error downloading file: " + e);
                Files.delete(tempDir);
                downloadInfo.setStatus("FAILED");
            }
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            downloadInfo.setStatus("FAILED");
        }
        cancelled.set(false);
        paused.set(false);
        shClick.set(false);
        hide.set(false);
        show.set(true);
    }

    private static String getFilename(String filename, int duplicateCounter) {
        String newFilename;
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex != -1) {
            String baseName = filename.substring(0, dotIndex);
            String extension = filename.substring(dotIndex);
            newFilename = baseName + "(" + duplicateCounter + ")" + extension;
        } else {
            newFilename = filename + "(" + duplicateCounter + ")";
        }
        return newFilename;
    }

    private void updateTotalDownloaded(String newValue, String oldValue) {
        long totalDownloaded = 0;
        try {
            if (oldValue.equals("0")) {
                return;
            }
            totalDownloaded = downloadInfo.formatFileSizeKBToLong(newValue) - downloadInfo.formatFileSizeKBToLong(oldValue);
            //KB ko chua phan thap phan
            this.downloadedSize += totalDownloaded;
            // Bytes
            this.downloadInfo.setDownloaded(this.downloadedSize * 1024);
        } catch (NumberFormatException e) {
            System.out.println("check error: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        progressBarList = new ArrayList<>();
        progressBarList.add(progressBar1);
        progressBarList.add(progressBar2);
        progressBarList.add(progressBar3);
        progressBarList.add(progressBar4);
        progressBarList.add(progressBar5);
        progressBarList.add(progressBar6);
        progressBarList.add(progressBar7);
        progressBarList.add(progressBar8);
        for (ProgressBar progressBar : progressBarList) {
            progressBar = new ProgressBar();
        }
        downloadInfoList = FXCollections.observableArrayList();
        for (int i = 0; i < 8; i++) {
            downloadInfoList.add(new DownloadInfoOneChunk(Integer.toString(i + 1), "0", "Receiving data..."));
        }

        //set column N.
        TableColumn<DownloadInfoOneChunk, String> sn = (TableColumn<DownloadInfoOneChunk, String>) this.tableView.getColumns().get(0);
        sn.setCellValueFactory(p -> {
            return p.getValue().indexProperty();
        });

        //set column Downloaded
        TableColumn<DownloadInfoOneChunk, String> downloaded = (TableColumn<DownloadInfoOneChunk, String>) this.tableView.getColumns().get(1);
        downloaded.setCellValueFactory(p -> {
            return p.getValue().DownloadedProperty();
        });
        // set column Info
        TableColumn<DownloadInfoOneChunk, String> info = (TableColumn<DownloadInfoOneChunk, String>) this.tableView.getColumns().get(2);
        info.setCellValueFactory(p -> {
            return p.getValue().InfoProperty();
        });
        for (DownloadInfoOneChunk downloadInfoOneChunk : downloadInfoList) {
            downloadInfoOneChunk.DownloadedProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    updateTotalDownloaded(newValue, oldValue);
                });
            });
        }
    }
}

