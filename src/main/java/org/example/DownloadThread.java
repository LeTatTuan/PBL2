package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@SuppressWarnings("ALL")
public class DownloadThread extends  Thread {
    private String fileUrl;
    private long startByte;
    private long endByte;
    private Path tempDownloadingFile;
    public DownloadThread(String fileUrl, long startByte, long endByte, Path tempDownloadingFile) {
        this.fileUrl = fileUrl;
        this.startByte = startByte;
        this.endByte = endByte;
        this.tempDownloadingFile = tempDownloadingFile;
    }

    @Override
    public void run() {
        try {
            URL partialUrl = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) partialUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);

            try(InputStream in = connection.getInputStream()) {
                Files.copy(in,tempDownloadingFile, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
