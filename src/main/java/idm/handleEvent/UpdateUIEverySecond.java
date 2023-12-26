package idm.handleEvent;

import java.util.concurrent.atomic.AtomicBoolean;

import idm.models.DownloadInfo;
import javafx.scene.control.TextArea;

public class UpdateIntefaceEvery1s implements Runnable {
    private final DownloadInfo downloadInfo;
    private long downloadedBefore1s;
    private volatile AtomicBoolean pause = new AtomicBoolean(false);
    private volatile AtomicBoolean cancel = new AtomicBoolean(false);

    public UpdateIntefaceEvery1s(DownloadInfo downloadInfo, long downloadedBefore1s, AtomicBoolean pause, AtomicBoolean cancel) {
     this.downloadInfo = downloadInfo;
     this.downloadedBefore1s = downloadedBefore1s;
     this.pause = pause;
     this.cancel = cancel;
    }

    @Override
    public void run() {
        if (cancel.get()) {
            return;
        }
        if (!pause.get()) {
            long downloadedSize = downloadInfo.getDownloaded();
            long size = downloadInfo.getSize();
            double transferRate = (double) (downloadedSize - downloadedBefore1s) / 1000;
            double transferRateMB = transferRate / 1024;
            double timeLeft = (size - downloadedSize) / (transferRate * 1024);
            downloadInfo.setTransferRate(transferRateMB);
            downloadInfo.setTimeLeft(timeLeft);
            this.downloadedBefore1s = downloadedSize;
        }
    }

}
