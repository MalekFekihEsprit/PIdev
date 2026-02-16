package Controllers;

import Entites.Itineraire;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class ItineraryCardController {

    @FXML private Label iconLabel;
    @FXML private Label nameLabel;
    @FXML private Label destLabel;
    @FXML private Label descLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox joursContainer;
    @FXML private Label pasJoursLabel;

    private Itineraire itineraire;
    private Consumer<Itineraire> onEdit;
    private Consumer<Itineraire> onDelete;
    private TriConsumer<Integer, Integer, String> onJourClick;

    public void setData(Itineraire itineraire,
                        String destinationText,
                        int nbJours,
                        String emoji,
                        Consumer<Itineraire> onEdit,
                        Consumer<Itineraire> onDelete,
                        TriConsumer<Integer, Integer, String> onJourClick) {

        this.itineraire = itineraire;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onJourClick = onJourClick;

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

        generateJours(nbJours);
    }

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
                    VBox jourCircle = createJourCircle(jourNum);
                    ligne.getChildren().add(jourCircle);
                }

                joursContainer.getChildren().add(ligne);
            }
        } else {
            pasJoursLabel.setVisible(true);
            pasJoursLabel.setManaged(true);
        }
    }

    private VBox createJourCircle(int jourNum) {
        VBox jourCircle = new VBox();
        jourCircle.setAlignment(javafx.geometry.Pos.CENTER);
        jourCircle.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");

        Label jourLabel = new Label("J" + jourNum);
        jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 12;");

        jourCircle.setOnMouseEntered(event -> {
            jourCircle.setStyle("-fx-background-color: #ff8c42; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");
            jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: white; -fx-font-size: 12;");
        });

        jourCircle.setOnMouseExited(event -> {
            jourCircle.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");
            jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 12;");
        });

        final int jourFinal = jourNum;
        jourCircle.setOnMouseClicked(e -> {
            if (onJourClick != null) {
                onJourClick.accept(jourFinal, itineraire.getId_itineraire(), itineraire.getNom_itineraire());
            }
        });

        jourCircle.getChildren().add(jourLabel);
        return jourCircle;
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}