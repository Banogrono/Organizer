module com.omicron.organizerb {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires static lombok;

    opens com.omicron.organizerb to javafx.fxml;
    exports com.omicron.organizerb;
    exports com.omicron.organizerb.controller;
    opens com.omicron.organizerb.controller to javafx.fxml;

}