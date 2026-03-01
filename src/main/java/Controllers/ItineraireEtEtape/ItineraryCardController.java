package Controllers.ItineraireEtEtape;

import Entities.Itineraire;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.util.function.Consumer;

public class ItineraryCardController {

    @FXML private Label iconLabel;
    @FXML private Label nameLabel;
    @FXML private Label destLabel;
    @FXML private Label descLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button exportButton;
    @FXML private VBox joursContainer;
    @FXML private Label pasJoursLabel;

    private Itineraire itineraire;
    private Consumer<Itineraire> onEdit;
    private Consumer<Itineraire> onDelete;
    private Consumer<Itineraire> onExport;
    private TriConsumer<Integer, Integer, String> onJourClick;

    private LocalDate dateDebut;
    private int nbJours;

    public void setData(Itineraire itineraire,
                        String destinationText,
                        int nbJours,
                        String emoji,
                        Consumer<Itineraire> onEdit,
                        Consumer<Itineraire> onDelete,
                        TriConsumer<Integer, Integer, String> onJourClick,
                        Consumer<Itineraire> onExport) {

        this.itineraire = itineraire;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onJourClick = onJourClick;
        this.onExport = onExport;
        this.nbJours = nbJours;

        iconLabel.setText(emoji);
        nameLabel.setText(itineraire.getNom_itineraire());
        destLabel.setText(destinationText);
        descLabel.setText(itineraire.getDescription_itineraire() != null ?
                itineraire.getDescription_itineraire() : "Aucune description");

        editButton.setOnAction(e -> {
            if (onEdit != null) onEdit.accept(itineraire);
        });

        deleteButton.setOnAction(e -> {
            if (onDelete != null) onDelete.accept(itineraire);
        });

        if (exportButton != null) {
            exportButton.setOnAction(e -> {
                if (onExport != null) onExport.accept(itineraire);
            });
            exportButton.setTooltip(new Tooltip("Exporter cet itinéraire"));
        }

        chargerDateDebut();
        generateJours(nbJours);
    }

    /**
     * Charge la date de début du voyage depuis la base de données
     */
    private void chargerDateDebut() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Utils.MyBD.getInstance().getConn();
            String query = "SELECT v.date_debut FROM itineraire i " +
                    "JOIN voyage v ON i.id_voyage = v.id_voyage " +
                    "WHERE i.id_itineraire = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, itineraire.getId_itineraire());
            rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_debut");
                if (ts != null) {
                    dateDebut = ts.toLocalDateTime().toLocalDate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Génère les cercles pour chaque jour du voyage
     */
    private void generateJours(int nbJours) {
        joursContainer.getChildren().clear();

        if (nbJours > 0) {
            pasJoursLabel.setVisible(false);
            pasJoursLabel.setManaged(false);

            int joursParLigne = 5;
            int nombreLignes = (int) Math.ceil((double) nbJours / joursParLigne);

            for (int ligneIndex = 0; ligneIndex < nombreLignes; ligneIndex++) {
                HBox ligne = new HBox(15);
                ligne.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                int debut = ligneIndex * joursParLigne + 1;
                int fin = Math.min(debut + joursParLigne - 1, nbJours);

                for (int jourNum = debut; jourNum <= fin; jourNum++) {
                    if (jourNum == 1) {
                        HBox firstDayBox = new HBox(5);
                        firstDayBox.setAlignment(javafx.geometry.Pos.CENTER);
                        firstDayBox.getChildren().add(createJourCircle(jourNum));

                        Label flightIcon = new Label("✈️");
                        flightIcon.setStyle("-fx-font-size: 12; -fx-text-fill: #3b82f6;");
                        flightIcon.setTooltip(new Tooltip("Jour d'arrivée/départ"));
                        firstDayBox.getChildren().add(flightIcon);

                        ligne.getChildren().add(firstDayBox);
                    } else {
                        ligne.getChildren().add(createJourCircle(jourNum));
                    }
                }

                joursContainer.getChildren().add(ligne);
            }
        } else {
            pasJoursLabel.setVisible(true);
            pasJoursLabel.setManaged(true);
        }
    }

    /**
     * Crée un cercle pour un jour spécifique
     */
    private VBox createJourCircle(int jourNum) {
        VBox jourCircle = new VBox();
        jourCircle.setAlignment(javafx.geometry.Pos.CENTER);
        jourCircle.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");

        Label jourLabel = new Label("J" + jourNum);
        jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 12;");

        // Effet hover
        jourCircle.setOnMouseEntered(event -> {
            jourCircle.setStyle("-fx-background-color: #ff8c42; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");
            jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: white; -fx-font-size: 12;");
        });

        jourCircle.setOnMouseExited(event -> {
            jourCircle.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");
            jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 12;");
        });

        // Action au clic
        final int jourFinal = jourNum;
        jourCircle.setOnMouseClicked(e -> {
            if (onJourClick != null) {
                onJourClick.accept(jourFinal, itineraire.getId_itineraire(), itineraire.getNom_itineraire());
            }
        });

        jourCircle.getChildren().add(jourLabel);
        return jourCircle;
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Interface pour les fonctions à 3 paramètres
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}