package org.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.config.AppConfig;
import org.example.models.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DownloadManager {

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

    List<ProgressBar> progressBarList = new ArrayList<>();
    public int index = 0;

    @FXML
    void downloadButtonClicked(ActionEvent event) {

        String url = this.urlTextField.getText().trim();
        String filename = url.substring(url.lastIndexOf("/") + 1);
        String status = "DOWNLOADING";
        String action = "OPEN";
        String path = AppConfig.DOWNLOAD_PATH + File.separator + filename;
        System.out.println("check path: " + path);
        FileInfo file = new FileInfo(Integer.toString(index + 1), filename, url, status, action, path);
        this.tableView.getItems().add(Integer.parseInt(file.getIndex()) - 1, file);
        Thread dowloadThread = new Thread(() -> {
            try {
                new DownloadExcute(file, this.progressBarList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        dowloadThread.start();
        this.urlTextField.setText("");
        this.index = this.index + 1;
    }

    public void updateUI(FileInfo metaFile) {
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

        for(ProgressBar pg : progressBarList) {
            pg = new ProgressBar();
            pg.setProgress(0.0);
        }

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
        TableColumn<FileInfo, String> action = (TableColumn<FileInfo, String>) this.tableView.getColumns().get(4);
        action.setCellValueFactory(p -> {
            return p.getValue().actionProperty();
        });
    }
}
