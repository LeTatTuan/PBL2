package idm.handleEvent;

import idm.models.DownloadInfo;
import javafx.scene.control.TextArea;

public class UpdateIntefaceEvery1s implements Runnable {
    private DownloadInfo downloadInfo;
    private long downloadedBefore1s;

    public UpdateIntefaceEvery1s(DownloadInfo downloadInfo, long downloadedBefore1s) {
     this.downloadInfo = downloadInfo;
     this.downloadedBefore1s = downloadedBefore1s;
    }

    @Override
    public void run() {
        long downloadedSize = downloadInfo.getDownloaded();
        long size = downloadInfo.getSize();
        double transferRate;
        double timeleft;

        //bytes - bytes
        transferRate = (downloadedSize - downloadedBefore1s) / 1000;
        System.out.println("TransferRate: " + transferRate + "KB/s");
        timeleft = (size - downloadedSize)/(transferRate*1024);
        System.out.println("Timeleft: " + timeleft + "s");
        downloadInfo.setTransferRate(transferRate);
        downloadInfo.setTimeleft(timeleft);
        this.downloadedBefore1s = downloadedSize;
        System.out.println(downloadInfo.formatFileSize(this.downloadedBefore1s));
    }
}
