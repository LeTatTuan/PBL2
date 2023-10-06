package org.example;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;

public class DownloadExample {
    public static void main(String[] args) throws Exception {
        String url = "https://nodejs.org/dist/v16.13.1/node-v16.13.1-x64.msi";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // Đọc dữ liệu từ entity và xử lý nó theo nhu cầu.
                String content = EntityUtils.toString(entity);
                // Sau đó, bạn có thể lưu nội dung vào một tệp cục bộ hoặc thực hiện các xử lý khác.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
