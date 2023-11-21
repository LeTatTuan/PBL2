package org.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.config.AppConfig;
import org.example.models.FileInfo;

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
import java.util.concurrent.atomic.AtomicBoolean;


public class DownloadManager {
    private FileInfo file;
    @FXML
    private TextField urlTextField;
    @FXML
    private TableView<FileInfo> tableView;
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
    public int index = 0;
    private volatile AtomicBoolean paused = new AtomicBoolean(false);
    private volatile AtomicBoolean cancelled = new AtomicBoolean(false);

    @FXML
    void downloadButtonClicked(ActionEvent event) {
        String url = this.urlTextField.getText().trim();
        String filename = url.substring(url.lastIndexOf("/") + 1);
        String status = "DOWNLOADING";
        String capacity = "...";
        String path = AppConfig.DOWNLOAD_PATH + File.separator + filename;
        FileInfo file = new FileInfo(Integer.toString(index + 1), filename, url, status, capacity, path);
        this.index = this.index + 1;
        this.tableView.getItems().add(Integer.parseInt(file.getIndex()) - 1, file);
        Thread download = new Thread(() -> {
            try {
                startDownload(file);
            } catch (Exception ignored) {}
        });
        download.start();
        this.urlTextField.setText("");
    }
    @FXML
    void pauseButtonClicked(ActionEvent event) {
    	paused.set(true);
    }
    @FXML
    void resumeButtonClicked(ActionEvent event) {
    	paused.set(false);
    }
    
    
    @FXML
    void cancelButtonClicked(ActionEvent event) {
    	cancelled.set(true);
    }

    public void startDownload(FileInfo file) {
        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            String fileUrl = file.getUrl();
            String filename = file.getName();
            String destinationPath = file.getPath();
            int numThreads = 8;
            Path tempDir = Files.createTempDirectory("Downloading_");
            List<DownloadThread> downloadThreads = new ArrayList<>();

            try {
                URL url = new URL(fileUrl);
                long contentLength = url.openConnection().getContentLength();
                long chunkSize = contentLength / numThreads;
                for (int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
                    DownloadThread downloadThread = new DownloadThread(fileUrl, startByte, endByte, tempDownloadingFile, progressBarList.get(i));
                    downloadThreads.add(downloadThread);
                    executorService.execute(downloadThread);
                }
                executorService.shutdown();


                while (true) {
                    if (executorService.isTerminated() == true) {
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
                        file.setStatus("DONE");
                        File downloadedFile = new File(file.getPath());
                        long fileSize = downloadedFile.length(); // Kích thước tệp trong byte
                        //Chuyển đổi kích thước thành đơn vị phù hợp (KB, MB, GB, vv.)
                        String capacity = formatFileSize(fileSize);
                        file.setCapacity(capacity);
                        System.out.println("File downloaded successfully.");
                        break;
                    }
                    else if(paused.get()) {
                    	 downloadThreads.forEach(DownloadThread::pause);
                         file.setStatus("PAUSED");	
                    }
                    else if(cancelled.get()) {
                        downloadThreads.forEach(DownloadThread::cancel);
                        if (!executorService.isShutdown()) {
                            // Nếu chưa shutdown, thì shutdown
                            executorService.shutdownNow(); 
                        }
                        file.setStatus("CANCELLED");
                        Platform.runLater(() -> {
                            progressBarList.forEach(progressBar -> progressBar.setProgress(0));
                        });
                  
                        break;
                    }
                
                    else if(!paused.get()) {
                    	downloadThreads.forEach(DownloadThread::resume);
                    	file.setStatus("DOWNLOADING");
                    	
                    }
                }
                cancelled.set(false);
                    
            } catch (IOException e) {
                System.err.println("Error downloading file: " + e);
                Files.delete(tempDir);
                file.setStatus("FAILED");
            }
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            file.setStatus("FAILED");
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%s %s", new DecimalFormat("#,##0.#").format(size / 1024), units[1]);
    }

    public void updateUI(FileInfo metaFile) {
        System.out.println(metaFile);
        FileInfo fileInfo = this.tableView.getItems().get(Integer.parseInt(metaFile.getIndex()) - 1);
        fileInfo.setStatus(metaFile.getStatus());
        this.tableView.refresh();
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
        System.out.println("View initialized");
        //set column N.SO
        TableColumn<FileInfo, String> sn = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(0);
        sn.setCellValueFactory(p -> {
            return p.getValue().indexProperty();
        });

        //set column filename
        TableColumn<FileInfo, String> filename = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(1);
        filename.setCellValueFactory(p -> {
            return p.getValue().nameProperty();
        });

        //set column fileurl
        TableColumn<FileInfo, String> fileurl = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(2);
        fileurl.setCellValueFactory(p -> {
            return p.getValue().urlProperty();
        });

        //set column status
        TableColumn<FileInfo, String> status = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(3);
        status.setCellValueFactory(p -> {
            return p.getValue().statusProperty();
        });

        //set column action
        TableColumn<FileInfo, String> capacity = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(4);
        capacity.setCellValueFactory(p -> {
            return p.getValue().capacityProperty();
        });
    }
}