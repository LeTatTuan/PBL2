package idm.models;

import java.text.DecimalFormat;

public class DownloadInfo {
    private String url;
    private String fileName;
    private String path;
    private String status;
    private long size;
    private long downloaded;
    private double transferRate;
    private double timeleft;

    public DownloadInfo(String url, String fileName, String path, String status, long size, long downloaded, double transferRate) {
        this.url = url;
        this.fileName = fileName;
        this.path = path;
        this.status = status;
        this.size = size;
        this.downloaded = downloaded;
        this.transferRate = transferRate;
        this.timeleft = (size - downloaded)/transferRate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public double getTransferRate() {
        return transferRate;
    }

    public void setTransferRate(double transferRate) {
        this.transferRate = transferRate;
    }

    public double getTimeleft() {
        return timeleft;
    }

    public void setTimeleft(double timeleft) {
        this.timeleft = timeleft;
    }
    public String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%s %s", new DecimalFormat("#,##0.#").format(size / 1024), units[2]);
    }
    public long formatFileSizeToLong(String value) {
        String downloaded = value.replace(",", "");
        String result = downloaded.substring(0, downloaded.length() - 3).replace(" ", "");
        return Long.parseLong(result);
    }
    @Override
    public String toString() {
        String infor = url + "\n"
                 + "Status              " + status + "\n"
                 + "File size           " + formatFileSize(size) + "\n"
                 + "Downloaded          " + formatFileSize(downloaded) + "\n"
                 + "Transfer rate       " + String.format("%.3f", transferRate) + " MB/sec" + "\n"
                 + "Time left           " + String.format("%.0f", timeleft) + " sec";
        return infor;
    }
}
