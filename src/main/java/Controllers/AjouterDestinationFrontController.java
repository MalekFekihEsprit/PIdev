package Controllers;

public class AjouterDestinationFrontController extends AjouterDestinationController {

    // Override any methods if needed for front office specific behavior
    // The main AjouterDestinationController already works for both back and front

    @Override
    public void setParentController(DestinationBackController controller) {
        // In front office, we don't have a parent controller to refresh
        // This method will be called but we don't need to do anything
    }
}