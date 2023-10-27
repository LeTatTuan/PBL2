package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.config.AppConfig;
import org.example.models.FileInfo;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ALL")
public class DownloadManager {

    @FXML
    private TextField urlTextField;
    @FXML
    private TableView<FileInfo> tableView;
    @FXML
    public int index = 0;

    List<DownloadThread> threads = new ArrayList<DownloadThread>();

    @FXML
    void downloadButtonClicked(ActionEvent event) {

        String url = this.urlTextField.getText().trim();
        String filename = url.substring(url.lastIndexOf("/")+1);
        String status = "DOWNLOADING";
        String action = "OPEN";
        String path = AppConfig.DOWNLOAD_PATH + File.separator + filename;
        FileInfo file = new FileInfo(Integer.toString(index + 1), filename, url,status, action, path);
        this.index = this.index+1;
        this.tableView.getItems().add(Integer.parseInt(file.getIndex()) - 1,file);
        new Thread(() -> {
            try {
                startDownload(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        this.urlTextField.setText("");
    }
    public void startDownload(FileInfo file) {
        try {
            String fileUrl = file.getUrl();
            String filename = file.getName();
            String destinationPath = file.getPath();
            int numThreads = 8;
            Path tempDir = Files.createTempDirectory("Downloading_");
            try {
                URL url = new URL(fileUrl);
                long contentLength = url.openConnection().getContentLength();

                long chunkSize = contentLength / numThreads;
                for(int i = 0; i < numThreads; i++) {
                    long startByte = i * chunkSize;
                    long endByte = (i == numThreads - 1) ? contentLength - 1 : startByte + chunkSize - 1;
                    Path tempDownloadingFile = tempDir.resolve("part-" + i + ".tmp");
                    threads.add(new DownloadThread(fileUrl, startByte, endByte, tempDownloadingFile));
                }

                for( DownloadThread thread : threads) {
                    if (thread.getState() == Thread.State.NEW) {
                        thread.start();
                    }
                }

                for (DownloadThread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.getStackTrace();
                    }
                }

//                Kiểm tra file đc tải đã tồn tại chưa
//                        nếu đã tồn tại thì thêm (1), (2),... vào sau tên file
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

                try(OutputStream out = Files.newOutputStream(finalPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    for(int i = 0; i < numThreads; i++) {
                        Path tempFile = tempDir.resolve("part-" + i + ".tmp");
                        Files.copy(tempFile, out);
                        Files.delete(tempFile);
                    }
                }
                Files.delete(tempDir);
                file.setStatus("DONE");
                System.out.println("File downloaded successfully.");
                threads.clear();
            } catch (IOException e) {
                System.err.println("Error downloading file: " + e.getMessage());
                Files.delete(tempDir);
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            file.setStatus("FAILED");
            e.printStackTrace();
        }
    }

    public void updateUI(FileInfo metaFile) {
        FileInfo fileInfo =  this.tableView.getItems().get(Integer.parseInt(metaFile.getIndex()) - 1);
        fileInfo.setStatus(metaFile.getStatus());
        this.tableView.refresh();
    }

    @FXML
    public void initialize() {
        System.out.println("View initialized");
        //set column N.SO
        TableColumn<FileInfo, String> sn = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(0);
        sn.setCellValueFactory(p -> {
            return  p.getValue().indexProperty();
        });

        //set column filename
        TableColumn<FileInfo, String> filename = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(1);
        filename.setCellValueFactory(p -> {
            return  p.getValue().nameProperty();
        });

        //set column fileurl
        TableColumn<FileInfo, String> fileurl = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(2);
        fileurl.setCellValueFactory(p -> {
            return  p.getValue().urlProperty();
        });

        //set column status
        TableColumn<FileInfo, String> status = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(3);
        status.setCellValueFactory(p -> {
            return  p.getValue().statusProperty();
        });

        //set column action
        TableColumn<FileInfo, String> action = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(4);
        action.setCellValueFactory(p -> {
            return  p.getValue().actionProperty();
        });
    }
}
