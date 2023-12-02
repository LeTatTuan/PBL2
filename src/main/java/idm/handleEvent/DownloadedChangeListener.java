package idm.handleEvent;
import java.io.IOException;
import java.net.URL;

public class DownloadedChangeListener{
    public static void main(String[] args) {
        //String url = "https://vod-progressive.akamaized.net/exp=1701574332~acl=%2Fvimeo-prod-src-reg-us-east1%2Fvideos%2F3683051887~hmac=d1fd0070f19dfe71c4fb9b76927b98b4569f66afcf3bf5804cc0e6ffd65fb575/vimeo-prod-src-reg-us-east1/videos/3683051887?download=1&filename=189813+%28Original%29.mp4&source=1";
        //String url = "https://download.microsoft.com/download/6/2/A/62A76ABB-9990-4EFC-A4FE-C7D698DAEB96/9600.17050.WINBLUE_REFRESH.140317-1640_X64FRE_SERVER_EVAL_EN-US-IR3_SSS_X64FREE_EN-US_DV9.ISO";
        String url = "https://phanmem123-my.sharepoint.com/personal/data_phanmem123_onmicrosoft_com/_layouts/15/download.aspx?SourceUrl=%2Fpersonal%2Fdata%5Fphanmem123%5Fonmicrosoft%5Fcom%2FDocuments%2F%5BPhanmem123%2Ecom%5D%20MATLAB%20R2019a%2Erar";
        try{
            URL Url = new URL(url);
            long size = Url.openConnection().getContentLengthLong();
            System.out.println(size);
        }catch (IOException e) {
        }
    }
}