package tests;

import java.net.URL;

public class FxmlTest {
    public static void main(String[] args) {
        // Try different paths
        String[] paths = {
                "/AjouterDestination.fxml",
                "/fxml/AjouterDestination.fxml",
                "/controllers/AjouterDestination.fxml",
                "/views/AjouterDestination.fxml",
                "AjouterDestination.fxml"
        };

        for (String path : paths) {
            URL url = FxmlTest.class.getResource(path);
            System.out.println(path + " -> " + (url != null ? "FOUND: " + url : "NOT FOUND"));
        }
    }
}