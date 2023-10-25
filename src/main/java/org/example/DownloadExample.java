package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadExample {
    public static void main(String[] args) {
        try {
            // Mở kết nối và gửi yêu cầu HTTP
            String url = "https://nodejs.org/dist/v16.13.1/node-v16.13.1-x64.msi";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                // Đọc và in dữ liệu từ luồng đầu vào
                String line;

                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                inputStream.close();
                connection.disconnect();
            } else {
                // Xử lý trường hợp mã trạng thái không phải HTTP_OK (200)
                System.err.println("Lỗi: Mã trạng thái HTTP " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
