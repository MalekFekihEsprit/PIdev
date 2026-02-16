package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class NavigationUtil {

    public static void chargerPage(BorderPane container, String fxmlFile, String pageTitle) {
        try {
            // Charger la nouvelle page
            FXMLLoader pageLoader = new FXMLLoader(NavigationUtil.class.getResource(fxmlFile));
            BorderPane newPage = pageLoader.load();

            // Ajouter la navbar à la nouvelle page
            FXMLLoader navbarLoader = new FXMLLoader(NavigationUtil.class.getResource("/Navbar.fxml"));
            Parent navbar = navbarLoader.load();
            newPage.setTop(navbar);

            // Configurer la navbar
            //NavbarController navbarController = navbarLoader.getController();
            //navbarController.setCurrentPage(pageTitle);
            //navbarController.setMainBorderPane(container);

            // Remplacer le contenu
            container.setCenter(newPage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void retourAListe(BorderPane container) {
        chargerPage(container, "/PageItineraire.fxml", "Itinéraires");
    }
}