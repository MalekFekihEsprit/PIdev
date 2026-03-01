package Controllers.ItineraireEtEtape;

import Services.FlightAPIService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightPopupController {

    @FXML private Label titleLabel;
    @FXML private Label destinationLabel;
    @FXML private Label dateLabel;
    @FXML private VBox flightsContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private Label noFlightsLabel;

    private FlightAPIService flightService = new FlightAPIService();
    private String destination;
    private LocalDate date;
    private String voyageName;

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        noFlightsLabel.setVisible(false);
        noFlightsLabel.setManaged(false);
    }

    public void setFlightInfo(String destination, LocalDate date, String voyageName) {
        this.destination = destination;
        this.date = date;
        this.voyageName = voyageName;

        titleLabel.setText("Vols pour " + voyageName);
        destinationLabel.setText("Destination: " + destination);
        dateLabel.setText("Date: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        loadFlights();
    }

    private void loadFlights() {
        loadingIndicator.setVisible(true);
        flightsContainer.getChildren().clear();
        errorLabel.setVisible(false);
        noFlightsLabel.setVisible(false);

        new Thread(() -> {
            try {
                System.out.println("Recherche de vols pour " + destination + " le " + date);
                List<FlightAPIService.FlightInfo> flights = flightService.searchFlights(destination, date);
                System.out.println("Nombre de vols trouvés: " + flights.size());

                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);

                    if (flights.isEmpty()) {
                        noFlightsLabel.setVisible(true);
                        noFlightsLabel.setManaged(true);
                        noFlightsLabel.setText("✈️ Aucun vol trouvé pour " + destination + " le " +
                                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } else {
                        displayFlights(flights);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    errorLabel.setText("❌ Erreur de chargement: " + e.getMessage());
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                });
            }
        }).start();
    }

    private void displayFlights(List<FlightAPIService.FlightInfo> flights) {
        flightsContainer.getChildren().clear();

        for (FlightAPIService.FlightInfo flight : flights) {
            VBox flightCard = createFlightCard(flight);
            flightsContainer.getChildren().add(flightCard);
        }
    }

    private VBox createFlightCard(FlightAPIService.FlightInfo flight) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 5, 0, 0, 2);");

        // En-tête avec compagnie et numéro de vol
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label airlineLabel = new Label("✈️ " + flight.getAirline());
        airlineLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 14;");

        Label flightNumberLabel = new Label("Vol " + flight.getFlightNumber());
        flightNumberLabel.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 2 8; -fx-text-fill: #475569; -fx-font-size: 11;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label statusLabel = new Label(flight.getStatus());
        String statusColor = switch(flight.getStatus().toLowerCase()) {
            case "active", "en vol", "scheduled" -> "#10b981";
            case "delayed", "retard" -> "#ef4444";
            case "landed", "arrivé" -> "#3b82f6";
            default -> "#64748b";
        };
        statusLabel.setStyle("-fx-background-color: " + statusColor + "20; -fx-text-fill: " + statusColor + "; -fx-background-radius: 12; -fx-padding: 2 8; -fx-font-size: 11; -fx-font-weight: 600;");

        headerBox.getChildren().addAll(airlineLabel, flightNumberLabel, spacer, statusLabel);

        // Route
        HBox routeBox = new HBox(15);
        routeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        routeBox.setStyle("-fx-padding: 8 0 4 0;");

        VBox departureBox = new VBox(2);
        Label depAirport = new Label(flight.getDepartureAirport());
        depAirport.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 13;");
        Label depTimeLabel = new Label("🕒 " + flight.getFormattedDepartureTime());
        depTimeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        departureBox.getChildren().addAll(depAirport, depTimeLabel);

        Label arrowLabel = new Label("→");
        arrowLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");

        VBox arrivalBox = new VBox(2);
        Label arrAirport = new Label(flight.getArrivalAirport());
        arrAirport.setStyle("-fx-font-weight: 600; -fx-text-fill: #10b981; -fx-font-size: 13;");
        Label arrTimeLabel = new Label("🕒 " + flight.getFormattedArrivalTime());
        arrTimeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        arrivalBox.getChildren().addAll(arrAirport, arrTimeLabel);

        routeBox.getChildren().addAll(departureBox, arrowLabel, arrivalBox);

        card.getChildren().addAll(headerBox, routeBox);

        return card;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) flightsContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRefresh() {
        loadFlights();
    }
}