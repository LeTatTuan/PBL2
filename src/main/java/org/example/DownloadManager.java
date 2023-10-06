package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.config.AppConfig;
import org.example.models.FileInfo;
import java.io.File;


public class DownloadManager {

    @FXML
    private TextField urlTextField;
    @FXML
    private TableView<FileInfo> tableView;
    public int index = 0;
    @FXML
    void downloadButtonClicked(ActionEvent event) {

        String url = this.urlTextField.getText().trim();
        String filename = url.substring(url.lastIndexOf("/")+1);
        String status = "STARTING";
        String action = "OPEN";
        String path = AppConfig.DOWNLOAD_PATH + File.separator + filename;
        System.out.println("check path: " + path);
        FileInfo file = new FileInfo(Integer.toString(index + 1), filename, url,status, action, path);
        this.index = this.index+1;
        DownloadThread thread = new DownloadThread(file, this);
        this.tableView.getItems().add(Integer.parseInt(file.getIndex()) - 1,file);
        thread.start();
        this.urlTextField.setText("");
    }

    public void updateUI(FileInfo metaFile) {
        System.out.println(metaFile);
        FileInfo fileInfo =  this.tableView.getItems().get(Integer.parseInt(metaFile.getIndex()) - 1);
        fileInfo.setStatus(metaFile.getStatus());
        this.tableView.refresh();
        System.out.println("---------------");
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
