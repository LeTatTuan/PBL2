package idm.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DownloadManager {
    @FXML
    private TextField urlTextField;
    @FXML
    private TextArea txtDownload;
    @FXML
    private TableView<DownloadInfoOneChunk> tableView;
    private ObservableList<DownloadInfoOneChunk> downloadInfoList;
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

    @FXML
    void downloadButtonClicked(ActionEvent event) {
        try {
            String url = this.urlTextField.getText().trim();
            String filename = url.substring(url.lastIndexOf("/") + 1);
            String path = AppConfig.DOWNLOAD_PATH + File.separator + filename;
            URL Url = new URL(url);
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

                }
            });
            startThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.urlTextField.setText("");
    }

    public void startDownload(DownloadInfo downloadInfo) {
        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            String fileUrl = downloadInfo.getUrl();
            String filename = downloadInfo.getFileName();
            String destinationPath = downloadInfo.getPath();
            int numThreads = 8;
            Path tempDir = Files.createTempDirectory("Downloading_");
            try {
                URL url = new URL(fileUrl);
                long contentLength = url.openConnection().getContentLengthLong();
                long chunkSize = contentLength / numThreads;
//                downloadInfoList = FXCollections.observableArrayList();
                tableView.setItems(downloadInfoList);
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
//                    DownloadInfoOneChunk downloadInfoOneChunk = new DownloadInfoOneChunk(Integer.toString(i + 1), "0", "Receiving data...");
//                    downloadInfoList.add(downloadInfoOneChunk);
                    DownloadThread downloadThread = new DownloadThread(downloadInfo.getUrl(), startByte, endByte, tempDownloadingFile, progressBarList.get(i), downloadInfoList.get(i));
                    executorService.execute(downloadThread);
                }

                executorService.shutdown();

                while (true) {
                    if (executorService.isTerminated()) {
                        int duplicateCounter = 1;
                        Path finalPath = Paths.get(destinationPath);
                        while (Files.exists(finalPath)) {
                            String newFilename = filename;
                            int dotIndex = filename.lastIndexOf(".");
                            if (dotIndex != -1) {
                                String baseName = filename.substring(0, dotIndex);
                                String extension = filename.substring(dotIndex);
                                newFilename = baseName + "(" + duplicateCounter + ")" + extension;
                            } else {
                                newFilename = filename + "(" + duplicateCounter + ")";
                            }
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
                        System.out.println("File downloaded successfully.");
                        downloadInfo.setStatus("Received data");
                        downloadInfo.setDownloaded(contentLength);
                        this.txtDownload.setText(downloadInfo.toString());
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
    }

    // Phương thức cập nhật tổng cột Downloaded
    private void updateTotalDownloaded(String newValue, String oldValue) {
        long totalDownloaded = 0;
        try {
            String downloadedS = newValue.replace(".", "");
            String downloadedS1 = downloadedS.substring(0, downloadedS.length() - 3).replace(" ", "");
            String downloadedT = "";
            String downloadedT1 = "0";
            if(oldValue != "0") {
                downloadedT = oldValue.replace(".", "");
                downloadedT1 = downloadedT.substring(0, downloadedT.length() - 3).replace(" ", "");
            }
            totalDownloaded = Long.parseLong(downloadedS1) - Long.parseLong(downloadedT1);
            //KB
            this.downloadedSize += totalDownloaded;
            // Bytes
            this.downloadInfo.setDownloaded(this.downloadedSize*1024);
            this.txtDownload.setText(this.downloadInfo.toString());
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
        System.out.println("View initialized");

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
                     updateTotalDownloaded(newValue,oldValue);
                });
            });
        }
    }
}