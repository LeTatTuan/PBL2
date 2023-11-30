package idm.handleEvent;

import idm.controller.DownloadManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DownloadedChangeListener implements ChangeListener<String> {

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Xử lý khi giá trị của cột thay đổi
        System.out.println("Value of the downloaded property changed: " + newValue);

        // Cập nhật tổng hoặc thực hiện các hành động khác tùy thuộc vào yêu cầu của bạn
        updateSumOrOtherActions();
    }

    private void updateSumOrOtherActions() {
        // Thực hiện các hành động khác tùy thuộc vào yêu cầu của bạn
        // Ví dụ: Cập nhật tổng
        double sum = calculateSum();
        System.out.println("Sum of downloaded values: " + sum);
    }

    private double calculateSum() {
        // Tính tổng các giá trị của cột từ TableView
        DownloadManager downloadManager = new DownloadManager();
        return ((downloadManager).getTableView().getItems().stream())
                .mapToDouble(item -> parseLong(item.getDownloaded()))
                .sum();
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value.substring(0, value.length() - 3));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}