package koeko.controllers.controllers_tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import koeko.database_management.DbTableSettings;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ControllerUtils {
    public static Parent openFXMLResource(FXMLLoader fxmlLoader) {
        Parent parent = null;
        try {
            fxmlLoader.setResources(ResourceBundle.getBundle("bundles.LangBundle", new Locale(DbTableSettings.getLanguage())));
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parent;
    }
}
