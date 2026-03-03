package Controllers;

import Entities.Destination;
import Entities.User;
import Utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DestinationDeleteReasonController implements Initializable {

    @FXML private Label lblWarning;
    @FXML private Label lblItemInfo;
    @FXML private Label lblCreatorName;
    @FXML private Label lblNote;
    @FXML private Label lblCharCount;
    @FXML private ComboBox<String> cmbReasons;
    @FXML private TextArea txtCustomReason;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private Destination destination;
    private User currentUser;
    private Consumer<DeleteReasonResult> onConfirm;

    private final String[] PREDEFINED_REASONS = {
            "Contenu inapproprié",
            "Informations incorrectes",
            "Doublon",
            "Destination non valide",
            "Violation des conditions d'utilisation",
            "Demande de l'utilisateur",
            "Autre"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupReasons();
        setupTextAreaListener();
        setupButtons();
        currentUser = UserSession.getInstance().getCurrentUser();
    }

    private void setupReasons() {
        cmbReasons.getItems().addAll(PREDEFINED_REASONS);
        cmbReasons.setValue(PREDEFINED_REASONS[0]); // Default selection
    }

    private void setupTextAreaListener() {
        txtCustomReason.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 200) {
                txtCustomReason.setText(newVal.substring(0, 200));
            }
            lblCharCount.setText(newVal.length() + "/200");
        });
    }

    private void setupButtons() {
        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        });

        btnConfirm.setOnAction(event -> {
            if (validateInput()) {
                if (onConfirm != null) {
                    String reason = cmbReasons.getValue();
                    String customReason = reason.equals("Autre") ? txtCustomReason.getText().trim() : null;

                    DeleteReasonResult result = new DeleteReasonResult(
                            reason,
                            customReason,
                            currentUser
                    );

                    onConfirm.accept(result);
                }

                Stage stage = (Stage) btnConfirm.getScene().getWindow();
                stage.close();
            }
        });
    }

    private boolean validateInput() {
        String selectedReason = cmbReasons.getValue();

        if (selectedReason == null || selectedReason.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une raison");
            return false;
        }

        if (selectedReason.equals("Autre") && txtCustomReason.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez préciser la raison");
            return false;
        }

        return true;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
        updateDisplay();
    }

    public void setOnConfirm(Consumer<DeleteReasonResult> onConfirm) {
        this.onConfirm = onConfirm;
    }

    private void updateDisplay() {
        if (destination != null) {
            lblItemInfo.setText("Destination: " + destination.getNom_destination() +
                    " (" + destination.getPays_destination() + ")");

            // Use added_by_name for display (already joined from user table)
            String creator = destination.getAdded_by_name();
            lblCreatorName.setText(creator != null ? creator : "Utilisateur inconnu");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class to hold the result
    public static class DeleteReasonResult {
        private final String reason;
        private final String customReason;
        private final User admin;

        public DeleteReasonResult(String reason, String customReason, User admin) {
            this.reason = reason;
            this.customReason = customReason;
            this.admin = admin;
        }

        public String getReason() { return reason; }
        public String getCustomReason() { return customReason; }
        public User getAdmin() { return admin; }

        public String getFullReason() {
            if (reason != null && reason.equals("Autre") && customReason != null) {
                return customReason;
            }
            return reason;
        }
    }
}