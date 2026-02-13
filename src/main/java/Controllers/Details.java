package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Details {

    @FXML
    private TextField resage;

    @FXML
    private TextField resnom;

    @FXML
    private TextField resprenom;

    public void setResnom(String resnom) {
        this.resnom.setText(resnom);
    }
    public void setResprenom(String resprenom) {
        this.resprenom.setText(resprenom);
    }
    public void setResage(String resage) {
        this.resage.setText(resage);
    }
}
