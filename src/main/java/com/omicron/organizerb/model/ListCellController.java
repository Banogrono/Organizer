package com.omicron.organizerb.model;

import com.omicron.organizerb.controller.OrganizerController;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public interface ListCellController {

    static ListCellController newInstance(String fxmlPath) {
        FXMLLoader loader = Utility.getFXMLLoader(fxmlPath);
        try {
            loader.load();
            return loader.getController();
        } catch (IOException ex) {
            return null;
        }
    }

    void setControllerReference(Controller controller);

}
