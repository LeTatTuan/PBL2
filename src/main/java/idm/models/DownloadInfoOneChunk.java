package idm.models;

import javafx.beans.property.SimpleStringProperty;

import java.text.DecimalFormat;

public class DownloadInfoOneChunk {

    private final SimpleStringProperty index = new SimpleStringProperty();
    private final SimpleStringProperty downloaded = new SimpleStringProperty();
    private final SimpleStringProperty info = new SimpleStringProperty();
    public DownloadInfoOneChunk(String index, String downloaded, String info) {
        this.index.set(index);
        this.downloaded.set(downloaded);
        this.info.set(info);
    }

    public String getIndex() {
        return index.get();
    }

    public SimpleStringProperty indexProperty() {return index;}
    public void setIndex(String index) {
        this.index.set(index);
    }

    public String getDownloaded() {
        return downloaded.get();
    }

    public SimpleStringProperty DownloadedProperty() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded.set(downloaded);
    }

    public String getInfo() {
        return info.get();
    }

    public SimpleStringProperty InfoProperty() {
        return info;
    }

    public void setInfo(String info) {this.info.set(info);}
    public String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%s %s", new DecimalFormat("#,##0.#").format(size / 1024), units[2]);
    }
    @Override
    public String toString() {
        return "DownloadInfo{" +
                "index=" + index +
                ", download=" + downloaded +
                ", info=" + info +
                '}';
    }
}
