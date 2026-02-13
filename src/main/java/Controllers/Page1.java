package Controllers;

import Entities.Pers;
import Services.PersCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;

public class Page1 {

    @FXML
    private Button btnajouter;

    @FXML
    private TextField tfage;

    @FXML
    private TextField tfnom;

    @FXML
    private TextField tfprenom;

    @FXML
    void savepres(ActionEvent event) {
        // Save
        Pers p = new Pers(tfnom.getText(), tfprenom.getText(), Integer.parseInt(tfage.getText()));
        PersCRUD pc = new PersCRUD();
        try {
            pc.ajouter(p);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Personne ajoutée !");

        // Redirection
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
        try {
            Parent root = loader.load(); 
            Details dc = loader.getController();
            dc.setResnom(tfnom.getText());
            dc.setResprenom(tfprenom.getText());
            dc.setResage(tfage.getText());
            btnajouter.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
