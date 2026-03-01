package Controllers.ItineraireEtEtape;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private HBox navDestinations;
    @FXML private HBox navItineraires;
    @FXML private HBox navActivites;
    @FXML private HBox navVoyages;
    @FXML private HBox navBudgets;
    @FXML private Label breadcrumbCurrent;
    @FXML private Label breadcrumbAccueil;

    private String currentPage = "Itinéraires";

    private static MainLayoutController instance;

    @FXML
    public void initialize() {
        instance = this;
        System.out.println("MainLayoutController initialisé");
        loadPage("/ItineraireEtEtape/PageItineraire.fxml");
        updateActiveNav();
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void loadPage(String fxmlFile) {
        try {
            System.out.println("Chargement de la page: " + fxmlFile);
            Parent page = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPageDirect(Parent page) {
        System.out.println("Chargement direct d'une page");
        contentArea.getChildren().setAll(page);
    }

    @FXML
    private void handleNavigation(MouseEvent event) {
        Object source = event.getSource();
        String fxmlFile = "";
        String pageTitle = "";

        if (source == navDestinations) {
            fxmlFile = "/PageDestination.fxml";  // À créer si besoin
            pageTitle = "Destinations";
        } else if (source == navItineraires) {
            // CHANGE ICI : redirige vers la page de gestion
            fxmlFile = "/ItineraireEtEtape/PageGestionItineraires.fxml";
            pageTitle = "Gestion Itinéraires";
        } else if (source == navActivites) {
            fxmlFile = "/PageActivite.fxml";  // À créer si besoin
            pageTitle = "Activités";
        } else if (source == navVoyages) {
            fxmlFile = "/PageVoyage.fxml";  // À créer si besoin
            pageTitle = "Voyages";
        } else if (source == navBudgets) {
            fxmlFile = "/PageBudget.fxml";  // À créer si besoin
            pageTitle = "Budgets";
        } else if (source == breadcrumbAccueil) {
            fxmlFile = "/ItineraireEtEtape/PageGestionItineraires.fxml";
            pageTitle = "Gestion Itinéraires";
        }

        if (!fxmlFile.isEmpty()) {
            currentPage = pageTitle;
            loadPage(fxmlFile);
            updateActiveNav();
            if (breadcrumbCurrent != null) {
                breadcrumbCurrent.setText(pageTitle);
            }
        }
    }

    private void updateActiveNav() {
        resetNavStyles();

        HBox activeNav = null;
        switch (currentPage) {
            case "Destinations":
                activeNav = navDestinations;
                break;
            case "Itinéraires":
                activeNav = navItineraires;
                break;
            case "Activités":
                activeNav = navActivites;
                break;
            case "Voyages":
                activeNav = navVoyages;
                break;
            case "Budgets":
                activeNav = navBudgets;
                break;
        }

        if (activeNav != null) {
            activeNav.setStyle("-fx-background-color: rgba(255,140,66,0.1); -fx-background-radius: 10; -fx-padding: 6 12; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 10;");
            Label label = (Label) activeNav.getChildren().get(1);
            label.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 12;");
        }
    }

    private void resetNavStyles() {
        HBox[] navs = {navDestinations, navItineraires, navActivites, navVoyages, navBudgets};
        for (HBox nav : navs) {
            if (nav != null) {
                nav.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 6 12; -fx-cursor: hand;");
                Label label = (Label) nav.getChildren().get(1);
                label.setStyle("-fx-text-fill: #475569; -fx-font-weight: 500; -fx-font-size: 12;");
            }
        }
    }

}