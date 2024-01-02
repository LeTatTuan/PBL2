package idm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestURL {
    public static void main(String[] args) {
        //String downloadLink = "https://software.download.prss.microsoft.com/dbazure/Win11_23H2_English_x64v2.iso?t=5d7a7100-6dac-42ba-b805-de2513dfb42c&e=1704265579&h=296e1465e536a33613844c8c57f604e94edc0b79c0a3381e11f2445bf810e1fe";
        //String downloadLink = "https://phanmem123-my.sharepoint.com/personal/data_phanmem123_onmicrosoft_com/_layouts/15/download.aspx?SourceUrl=%2Fpersonal%2Fdata%5Fphanmem123%5Fonmicrosoft%5Fcom%2FDocuments%2F%5BPhanmem123%2Ecom%5D%20MATLAB%20R2019a%2Erar";
        //String downloadLink = "https://nodejs.org/dist/v16.13.1/node-v16.13.1-x64.msi";
        String downloadLink = "https:\u002f\u002fphanmem123-my.sharepoint.com:443\u002f_api\u002fv2.0\u002fdrives\u002fb!ADH3KsGuqE6pSbGoXMdl54Q7HEa6h89Ho2ou4HX4OIltIwSfuv8CTpMrWmp0ayh3\u002fitems\u002f01YYJER2G27EP7IQ7AXNCJUIASXTUJSR2I?version=Published";
        try {
            URL url = new URL(downloadLink);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println(Paths.get(url.getPath()).getFileName().toString());
            // Set request method to HEAD to get only the header information
            connection.setRequestMethod("HEAD");

            // Get header fields
            Map<String, List<String>> headers = connection.getHeaderFields();

            // Print header information
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();

                if (key == null) {
                    // The key is null for the status line
                    System.out.println("Status: " + values.get(0));
                } else {
                    System.out.println(key + ": " + values.toString());
                }
            }

            // Close the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
