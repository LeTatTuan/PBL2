package idm.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
            long size = Url.openConnection().getContentLength();
            long downloaded = 0;
            double transferRate = 0;
            this.downloadInfo = new DownloadInfo(url, filename, path, status, size, downloaded, transferRate);
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
                long contentLength = url.openConnection().getContentLength();
                long chunkSize = contentLength / numThreads;
                downloadInfoList = FXCollections.observableArrayList();
                tableView.setItems(downloadInfoList);
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
                    DownloadInfoOneChunk downloadInfoOneChunk = new DownloadInfoOneChunk(Integer.toString(i + 1), "0", "Receiving data...");
                    downloadInfoList.add(downloadInfoOneChunk);
                    DownloadThread downloadThread = new DownloadThread(downloadInfo.getUrl(), startByte, endByte, tempDownloadingFile, progressBarList.get(i), downloadInfoOneChunk);
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

    private String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%s %s", new DecimalFormat("#,##0.#").format(size / 1024), units[1]);
    }

    public TableView<DownloadInfoOneChunk> getTableView() {
        return tableView;
    }

    private void handleRowSelection(DownloadInfoOneChunk selectedRow) {
        if (selectedRow != null) {
            // Cập nhật TextArea với thông tin từ hàng được chọn
            txtDownload.setText("Selected Row Info:\n" +
                    "Index: " + selectedRow.getIndex() + "\n" +
                    "Downloaded: " + selectedRow.getDownloaded() + "\n" +
                    "Info: " + selectedRow.getInfo());
        } else {
            // Nếu không có hàng được chọn, xóa nội dung TextArea
            txtDownload.clear();
        }
    }

    // Phương thức cập nhật tổng cột Downloaded
    private void updateTotalDownloaded() {
        int totalDownloaded = 0;
        for (DownloadInfoOneChunk item : downloadInfoList) {
            try {
                totalDownloaded += Integer.parseInt(item.getDownloaded());
            } catch (NumberFormatException e) {
                // Handle the case where the value is not a valid integer
            }
        }
        txtDownload.setText("Total Downloaded: " + totalDownloaded);
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

        // Thêm ChangeListener cho mỗi hàng trong TableView
        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DownloadInfoOneChunk>() {
            @Override
            public void changed(ObservableValue<? extends DownloadInfoOneChunk> observable, DownloadInfoOneChunk oldValue, DownloadInfoOneChunk newValue) {
                // Xử lý sự kiện khi hàng được chọn thay đổi
                handleRowSelection(newValue);
            }
        });

        // Thêm ChangeListener cho thuộc tính DownloadedProperty() của mỗi hàng
        for (DownloadInfoOneChunk item : downloadInfoList) {
            item.DownloadedProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    // Xử lý sự kiện khi giá trị của cột Downloaded thay đổi
                    updateTotalDownloaded();
                }
            });
        }

    }
}