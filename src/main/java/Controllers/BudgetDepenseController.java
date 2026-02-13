package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BudgetDepenseController {

    @FXML
    private TableColumn<?, ?> colCategorie;

    @FXML
    private TableColumn<?, ?> colDate;

    @FXML
    private TableColumn<?, ?> colDevise;

    @FXML
    private TableColumn<?, ?> colLibelle;

    @FXML
    private TableColumn<?, ?> colMontant;

    @FXML
    private TableColumn<?, ?> colPaiement;

    @FXML
    private Label lblBudgetNom;

    @FXML
    private Label lblDepense;

    @FXML
    private Label lblMontantTotal;

    @FXML
    private Label lblPct;

    @FXML
    private Label lblRestant;

    @FXML
    private ProgressBar progressBudget;

    @FXML
    private TableView<?> tableDepenses;

}
