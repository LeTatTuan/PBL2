package idm.handleEvent;

import idm.models.DownloadInfo;
import idm.models.DownloadInfoOneChunk;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class UpdateIntefaceEvery1s implements Runnable {
    private long downloadedSize;
    private long size;
    private double transferRate;
    private double timeleft;
    private TextArea txtDownloaded;
    private long downloadedBefore1s;

    public UpdateIntefaceEvery1s(DownloadInfo downloadInfo, TextArea txtDownloaded) {
        this.downloadedSize = downloadInfo.getDownloaded();
        this.size = downloadInfo.getSize();
        this.transferRate = downloadInfo.getTransferRate();
        this.timeleft = downloadInfo.getTimeleft();
        this.txtDownloaded = txtDownloaded;
    }

    @Override
    public void run() {
        transferRate = downloadedSize / 1000;
        timeleft = (size - downloadedSize)/transferRate;
    }
}
