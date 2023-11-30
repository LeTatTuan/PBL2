module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    opens idm to javafx.fxml;
    exports idm.controller;
    opens idm.controller to javafx.fxml;
    exports idm;
}