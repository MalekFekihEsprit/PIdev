package Controllers.ItineraireEtEtape;

import Services.CulturalAdviceService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CulturalPopupController {

    @FXML private Label titleLabel;
    @FXML private Label destinationLabel;
    @FXML private Label sourceLabel;
    @FXML private VBox respectContainer;
    @FXML private VBox safetyContainer;
    @FXML private VBox understandContainer;
    @FXML private Label noInfoLabel;

    private CulturalAdviceService.CulturalInfo culturalInfo;

    @FXML
    public void initialize() {
        // Initialisation
    }

    public void setCulturalInfo(CulturalAdviceService.CulturalInfo info, String voyageName) {
        this.culturalInfo = info;

        titleLabel.setText("Conseils culturels - " + voyageName);
        destinationLabel.setText("Destination: " + info.getDestination());

        if (info.isFromDefaultRules()) {
            sourceLabel.setText("ℹ️ Conseils génériques - Pour des informations précises, consultez un guide officiel");
            sourceLabel.setStyle("-fx-text-fill: #f59e0b;");
        } else {
            sourceLabel.setText("Source: Gemini API");
            sourceLabel.setStyle("-fx-text-fill: #10b981;");
        }

        displayContent();
    }

    private void displayContent() {
        // Cacher tous les conteneurs par défaut
        respectContainer.setVisible(false);
        respectContainer.setManaged(false);
        safetyContainer.setVisible(false);
        safetyContainer.setManaged(false);
        understandContainer.setVisible(false);
        understandContainer.setManaged(false);

        // Vérifier s'il y a des informations
        if (culturalInfo.hasContent()) {
            String fullText = culturalInfo.getRespectSection();

            if (fullText != null && !fullText.isEmpty()) {
                // Analyser le texte pour détecter les sections
                if (fullText.contains("**SALUER**") || fullText.contains("SALUER")) {
                    respectContainer.setVisible(true);
                    respectContainer.setManaged(true);
                    addFormattedText(respectContainer, fullText);
                } else {
                    // Si pas de sections, tout mettre dans respect
                    respectContainer.setVisible(true);
                    respectContainer.setManaged(true);
                    addBulletPoints(respectContainer, fullText);
                }
            }

            noInfoLabel.setVisible(false);
            noInfoLabel.setManaged(false);
        } else {
            noInfoLabel.setVisible(true);
            noInfoLabel.setManaged(true);
        }
    }

    /**
     * Ajoute du texte formaté avec des sections
     */
    private void addFormattedText(VBox container, String text) {
        container.getChildren().clear();

        String[] sections = text.split("\\*\\*");
        for (String section : sections) {
            section = section.trim();
            if (section.isEmpty()) continue;

            // Si c'est un titre (SALUER, S'HABILLER, etc.)
            if (section.matches("^[A-ZÀ-Ÿ]{3,}.*")) {
                Label titleLabel = new Label(section);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff8c42; -fx-font-size: 13; -fx-padding: 8 0 4 0;");
                container.getChildren().add(titleLabel);
            }
            // Sinon c'est du contenu avec des puces
            else {
                String[] lines = section.split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;

                    // Enlever le • s'il existe déjà
                    String cleanLine = line.replace("•", "").trim();

                    Label bulletLabel = new Label("• " + cleanLine);
                    bulletLabel.setWrapText(true);
                    bulletLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 12; -fx-padding: 2 0 2 10;");
                    container.getChildren().add(bulletLabel);
                }
            }
        }
    }

    /**
     * Ajoute des points de puce au texte simple
     */
    private void addBulletPoints(VBox container, String text) {
        container.getChildren().clear();

        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // Enlever le • s'il existe déjà
            String cleanLine = line.replace("•", "").trim();

            Label bulletLabel = new Label("• " + cleanLine);
            bulletLabel.setWrapText(true);
            bulletLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 12; -fx-padding: 2 0 2 10;");
            container.getChildren().add(bulletLabel);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleOpenWikivoyage() {
        try {
            String url = "https://en.wikivoyage.org/wiki/" +
                    culturalInfo.getDestination().replace(" ", "_");
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}