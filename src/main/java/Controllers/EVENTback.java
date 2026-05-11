package Controllers;

import Entities.Evenement;
import Services.Evenementcrud;
import Utils.UserSession;
import Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import Utils.FileManager;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EVENTback implements Initializable {

    // ===== Navigation sidebar =====
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnUsers;
    @FXML private HBox btnStats;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnCategories;
    @FXML private HBox btnActivites;
    @FXML private HBox btnEvenements;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox userProfileBox;

    // ===== User info =====
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblLastUpdate;

    // ===== KPI labels =====
    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiAVenir;
    @FXML private Label lblKpiPasses;
    @FXML private Label lblKpiPlaces;
    @FXML private Label lblTotalEvenements;
    @FXML private Label lblStatEvenements;
    @FXML private Label lblStatAVenir;
    @FXML private Label lblStatPlaces;
    @FXML private Label lblCountBadge;
    @FXML private Label lblCategoriesCount;

    // ===== Table =====
    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String>  colTitre;
    @FXML private TableColumn<Evenement, String>  colDescription;
    @FXML private TableColumn<Evenement, String>  colDate;
    @FXML private TableColumn<Evenement, String>  colHeure;
    @FXML private TableColumn<Evenement, String>  colLieu;
    @FXML private TableColumn<Evenement, Integer> colNbPlaces;
    @FXML private TableColumn<Evenement, String>  colLienGroupe;
    @FXML private TableColumn<Evenement, String>  colImage;

    // ===== Action buttons =====
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnFrontOffice;

    // ===== Search =====
    @FXML private TextField searchField;

    private Evenementcrud evenementCRUD;
    private ObservableList<Evenement> evenementsData;

    // Gestion image
    private File selectedImageFile;
    private String currentImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evenementCRUD = new Evenementcrud();
        evenementsData = FXCollections.observableArrayList();

        setupTableColumns();
        loadEvenements();
        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        updateLastUpdateTime();

        if (tableEvenements != null) {
            tableEvenements.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSel, newSel) -> {
                        boolean selected = newSel != null;
                        if (btnModifier != null) btnModifier.setDisable(!selected);
                        if (btnSupprimer != null) btnSupprimer.setDisable(!selected);
                    });
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
        }
    }

    // ==================== TABLE ====================

    private void setupTableColumns() {
        if (colId != null)         colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colTitre != null)      colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (colDate != null)       colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colHeure != null)      colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        if (colLieu != null)       colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        if (colNbPlaces != null)   colNbPlaces.setCellValueFactory(new PropertyValueFactory<>("nbPlaces"));
        if (colLienGroupe != null) colLienGroupe.setCellValueFactory(new PropertyValueFactory<>("lienGroupe"));
        if (colImage != null)      colImage.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
    }

    private void loadEvenements() {
        try {
            List<Evenement> liste = evenementCRUD.afficher();
            evenementsData.clear();
            evenementsData.addAll(liste);
            if (tableEvenements != null) tableEvenements.setItems(evenementsData);
            updateKpis();
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    private void filterTable(String query) {
        if (query == null || query.isEmpty()) {
            tableEvenements.setItems(evenementsData);
            return;
        }
        String lower = query.toLowerCase();
        ObservableList<Evenement> filtered = FXCollections.observableArrayList();
        for (Evenement e : evenementsData) {
            if ((e.getTitre() != null && e.getTitre().toLowerCase().contains(lower))
                    || (e.getLieu() != null && e.getLieu().toLowerCase().contains(lower))
                    || (e.getDescription() != null && e.getDescription().toLowerCase().contains(lower))) {
                filtered.add(e);
            }
        }
        tableEvenements.setItems(filtered);
    }

    private void updateKpis() {
        int total = evenementsData.size();
        int totalPlaces = evenementsData.stream().mapToInt(Evenement::getNbPlaces).sum();

        set(lblKpiTotal, String.valueOf(total));
        set(lblTotalEvenements, String.valueOf(total));
        set(lblStatEvenements, String.valueOf(total));
        set(lblKpiPlaces, String.valueOf(totalPlaces));
        set(lblStatPlaces, String.valueOf(totalPlaces));
        set(lblCountBadge, total + " événement" + (total > 1 ? "s" : ""));
    }

    // ==================== FXML HANDLERS ====================

    @FXML
    private void handleAjouter() {
        Dialog<Evenement> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Événement");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0a0e27;");
        dialogPane.setPrefWidth(860);
        dialogPane.setPrefHeight(780);

        VBox mainContainer = new VBox(22);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #0a0e27;");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // ── Titre ──
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setMaxWidth(800);
        Label iconTitle = new Label("🎉");
        iconTitle.setStyle("-fx-font-size: 30;");
        Label textTitle = new Label("Créer un nouvel événement");
        textTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        Label badge = new Label("NOUVEAU");
        badge.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label("Les champs * sont obligatoires");
        hint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        titleBox.getChildren().addAll(iconTitle, textTitle, badge, sp, hint);
        mainContainer.getChildren().add(titleBox);

        // ── Section 1 : Informations principales ──
        VBox sec1 = createEvtSection("📋 INFORMATIONS PRINCIPALES");
        VBox fields1 = new VBox(14); fields1.setMaxWidth(600); fields1.setAlignment(Pos.CENTER_LEFT);

        TextField titreField = createEvtTextField("Ex: Concert de jazz", "Titre de l'événement");
        TextField lieuField  = createEvtTextField("Ex: Tunis, Amphithéâtre", "Lieu");
        TextField lienField  = createEvtTextField("https://t.me/...", "Lien groupe (optionnel)");

        Label errTitre = createEvtErrorLabel();
        Label errLieu  = createEvtErrorLabel();

        fields1.getChildren().addAll(
                createEvtFormField("Titre *", titreField, errTitre),
                createEvtFormField("Lieu *",  lieuField,  errLieu),
                createEvtFormField("Lien groupe", lienField, null)
        );
        sec1.getChildren().add(fields1);
        mainContainer.getChildren().add(sec1);

        // ── Section 2 : Description ──
        VBox sec2 = createEvtSection("📝 DESCRIPTION");
        VBox fields2 = new VBox(14); fields2.setMaxWidth(600); fields2.setAlignment(Pos.CENTER_LEFT);

        TextArea descField = new TextArea();
        descField.setPromptText("Description de l'événement...");
        descField.setPrefRowCount(4); descField.setWrapText(true);
        descField.setMaxWidth(600);
        descField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13; -fx-control-inner-background: #1e2749;");

        Label errDesc = createEvtErrorLabel();
        fields2.getChildren().add(createEvtFormField("Description *", descField, errDesc));
        sec2.getChildren().add(fields2);
        mainContainer.getChildren().add(sec2);

        // ── Section 3 : Date, Heure, Places ──
        VBox sec3 = createEvtSection("📅 DATE, HEURE & PLACES");
        VBox fields3 = new VBox(14); fields3.setMaxWidth(600); fields3.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(7));
        datePicker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8; -fx-font-size: 13;");
        datePicker.setPrefHeight(40); datePicker.setMaxWidth(600);

        TextField heureField  = createEvtTextField("HH:mm  (ex: 18:30)", "Heure");
        TextField placesField = createEvtTextField("Ex: 100", "Nombre de places");

        Label errDate   = createEvtErrorLabel();
        Label errHeure  = createEvtErrorLabel();
        Label errPlaces = createEvtErrorLabel();

        fields3.getChildren().addAll(
                createEvtFormField("Date *",          datePicker, errDate),
                createEvtFormField("Heure *",         heureField,  errHeure),
                createEvtFormField("Nb places *",     placesField, errPlaces)
        );
        sec3.getChildren().add(fields3);
        mainContainer.getChildren().add(sec3);

        // ── Section 4 : Image ──
        selectedImageFile = null;
        VBox sec4 = createEvtSection("🖼️ IMAGE DE L'ÉVÉNEMENT");
        VBox fields4 = new VBox(14); fields4.setMaxWidth(600); fields4.setAlignment(Pos.CENTER_LEFT);

        ImageView imagePreviewAdd = new ImageView();
        imagePreviewAdd.setFitWidth(220);
        imagePreviewAdd.setFitHeight(150);
        imagePreviewAdd.setPreserveRatio(true);
        imagePreviewAdd.setStyle("-fx-background-color: #0a0e27;");

        Label imageNameLabelAdd = new Label("Aucune image sélectionnée");
        imageNameLabelAdd.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");

        Button chooseImageBtnAdd = new Button("📁 Choisir une image");
        chooseImageBtnAdd.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 10 25; -fx-cursor: hand; -fx-font-size: 13;");
        chooseImageBtnAdd.setOnAction(e -> {
            File file = FileManager.chooseImage(chooseImageBtnAdd.getScene().getWindow());
            if (file != null) {
                selectedImageFile = file;
                try {
                    imagePreviewAdd.setImage(new Image(file.toURI().toString(), 220, 150, true, true));
                    imageNameLabelAdd.setText("✅ " + file.getName());
                    imageNameLabelAdd.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11; -fx-wrap-text: true;");
                } catch (Exception ex) {
                    showError("Erreur", "Impossible de charger l'image : " + ex.getMessage());
                }
            }
        });

        VBox imageSectionAdd = new VBox(10, imagePreviewAdd, imageNameLabelAdd, chooseImageBtnAdd);
        imageSectionAdd.setStyle("-fx-padding: 15; -fx-background-color: #1e2749; -fx-background-radius: 10; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10;");
        imageSectionAdd.setMaxWidth(600);
        fields4.getChildren().add(imageSectionAdd);
        sec4.getChildren().add(fields4);
        mainContainer.getChildren().add(sec4);

        // ── Boutons (Ajouter) ──
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));
        buttonBar.setMaxWidth(800);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10; -fx-font-size: 14;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("✅ Créer l'événement");
        saveBtn.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14;");

        Region btnSpacer = new Region(); HBox.setHgrow(btnSpacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(btnSpacer, cancelBtn, saveBtn);
        mainContainer.getChildren().add(buttonBar);

        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 10;");
        dialogPane.setContent(scroll);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        hideDefaultButtons(dialogPane);

        saveBtn.setOnAction(event -> {
            boolean valid = true;

            // Titre
            if (titreField.getText().trim().isEmpty()) {
                titreField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errTitre.setText("Titre : champ obligatoire");
                valid = false;
            } else { resetFieldStyle(titreField); errTitre.setText(""); }

            // Description
            if (descField.getText().trim().length() < 5) {
                descField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-control-inner-background: #1e2749;");
                errDesc.setText("Description : au moins 5 caractères");
                valid = false;
            } else { descField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-control-inner-background: #1e2749;"); errDesc.setText(""); }

            // Lieu
            if (lieuField.getText().trim().isEmpty()) {
                lieuField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errLieu.setText("Lieu : champ obligatoire");
                valid = false;
            } else { resetFieldStyle(lieuField); errLieu.setText(""); }

            // Date
            if (datePicker.getValue() == null) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                errDate.setText("Date : champ obligatoire");
                valid = false;
            } else { datePicker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;"); errDate.setText(""); }

            // Heure
            LocalTime parsedHeure = null;
            try {
                parsedHeure = LocalTime.parse(heureField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                resetFieldStyle(heureField); errHeure.setText("");
            } catch (Exception ex) {
                heureField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errHeure.setText("Heure : format HH:mm requis (ex: 18:30)");
                valid = false;
            }

            // Places
            int nbPlaces = 0;
            try {
                nbPlaces = Integer.parseInt(placesField.getText().trim());
                if (nbPlaces <= 0) throw new NumberFormatException();
                resetFieldStyle(placesField); errPlaces.setText("");
            } catch (NumberFormatException ex) {
                placesField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errPlaces.setText("Nb places : entier positif requis");
                valid = false;
            }

            if (valid) {
                try {
                    Evenement ev = new Evenement();
                    ev.setTitre(titreField.getText().trim());
                    ev.setDescription(descField.getText().trim());
                    ev.setLieu(lieuField.getText().trim());
                    ev.setDate(datePicker.getValue());
                    ev.setHeure(parsedHeure);
                    ev.setNbPlaces(nbPlaces);
                    ev.setLienGroupe(lienField.getText().trim().isEmpty() ? null : lienField.getText().trim());

                    if (selectedImageFile != null) {
                        String imagePath = FileManager.saveImageEvenement(selectedImageFile);
                        ev.setImagePath(imagePath);
                    }

                    evenementCRUD.ajouter(ev);
                    showInfo("Succès", "✅ Événement créé avec succès !");
                    loadEvenements();
                    dialog.close();
                } catch (SQLException | java.io.IOException e) {
                    showError("Erreur", "Impossible d'ajouter l'événement : " + e.getMessage());
                }
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleModifier() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un événement à modifier.");
            return;
        }

        Dialog<Evenement> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'événement");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0a0e27;");
        dialogPane.setPrefWidth(860);
        dialogPane.setPrefHeight(780);

        VBox mainContainer = new VBox(22);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #0a0e27;");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // ── Titre ──
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setMaxWidth(800);
        Label iconTitle = new Label("✏️");
        iconTitle.setStyle("-fx-font-size: 30;");
        Label textTitle = new Label("Modifier l'événement");
        textTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        Label badge = new Label("MODIFICATION");
        badge.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label("Les champs * sont obligatoires");
        hint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        titleBox.getChildren().addAll(iconTitle, textTitle, badge, sp, hint);
        mainContainer.getChildren().add(titleBox);

        // ── Section 1 : Informations principales ──
        VBox sec1 = createEvtSection("📋 INFORMATIONS PRINCIPALES");
        VBox fields1 = new VBox(14); fields1.setMaxWidth(600); fields1.setAlignment(Pos.CENTER_LEFT);

        TextField titreField = new TextField(selected.getTitre());
        titreField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        titreField.setPrefHeight(40); titreField.setMaxWidth(600);

        TextField lieuField = new TextField(selected.getLieu());
        lieuField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        lieuField.setPrefHeight(40); lieuField.setMaxWidth(600);

        TextField lienField = new TextField(selected.getLienGroupe() != null ? selected.getLienGroupe() : "");
        lienField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        lienField.setPrefHeight(40); lienField.setMaxWidth(600);

        Label errTitre = createEvtErrorLabel();
        Label errLieu  = createEvtErrorLabel();

        fields1.getChildren().addAll(
                createEvtFormField("Titre *", titreField, errTitre),
                createEvtFormField("Lieu *",  lieuField,  errLieu),
                createEvtFormField("Lien groupe", lienField, null)
        );
        sec1.getChildren().add(fields1);
        mainContainer.getChildren().add(sec1);

        // ── Section 2 : Description ──
        VBox sec2 = createEvtSection("📝 DESCRIPTION");
        VBox fields2 = new VBox(14); fields2.setMaxWidth(600); fields2.setAlignment(Pos.CENTER_LEFT);

        TextArea descField = new TextArea(selected.getDescription() != null ? selected.getDescription() : "");
        descField.setPrefRowCount(4); descField.setWrapText(true);
        descField.setMaxWidth(600);
        descField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13; -fx-control-inner-background: #1e2749;");

        Label errDesc = createEvtErrorLabel();
        fields2.getChildren().add(createEvtFormField("Description *", descField, errDesc));
        sec2.getChildren().add(fields2);
        mainContainer.getChildren().add(sec2);

        // ── Section 3 : Date, Heure, Places ──
        VBox sec3 = createEvtSection("📅 DATE, HEURE & PLACES");
        VBox fields3 = new VBox(14); fields3.setMaxWidth(600); fields3.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker(selected.getDate());
        datePicker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8; -fx-font-size: 13;");
        datePicker.setPrefHeight(40); datePicker.setMaxWidth(600);

        String heureStr = selected.getHeure() != null
                ? selected.getHeure().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        TextField heureField  = new TextField(heureStr);
        heureField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        heureField.setPrefHeight(40); heureField.setMaxWidth(600);

        TextField placesField = new TextField(String.valueOf(selected.getNbPlaces()));
        placesField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        placesField.setPrefHeight(40); placesField.setMaxWidth(600);

        Label errDate   = createEvtErrorLabel();
        Label errHeure  = createEvtErrorLabel();
        Label errPlaces = createEvtErrorLabel();

        fields3.getChildren().addAll(
                createEvtFormField("Date *",      datePicker, errDate),
                createEvtFormField("Heure *",     heureField,  errHeure),
                createEvtFormField("Nb places *", placesField, errPlaces)
        );
        sec3.getChildren().add(fields3);
        mainContainer.getChildren().add(sec3);

        // ── Section 4 : Image ──
        currentImagePath = selected.getImagePath();
        selectedImageFile = null;
        VBox sec4mod = createEvtSection("🖼️ IMAGE DE L'ÉVÉNEMENT");
        VBox fields4mod = new VBox(14); fields4mod.setMaxWidth(600); fields4mod.setAlignment(Pos.CENTER_LEFT);

        ImageView imagePreviewMod = new ImageView();
        imagePreviewMod.setFitWidth(220);
        imagePreviewMod.setFitHeight(150);
        imagePreviewMod.setPreserveRatio(true);
        imagePreviewMod.setStyle("-fx-background-color: #0a0e27;");

        Label imageNameLabelMod = new Label("Aucune image sélectionnée");
        imageNameLabelMod.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");

        // Charger l'image actuelle si elle existe
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            try {
                File existingFile = new File(currentImagePath);
                if (existingFile.exists()) {
                    imagePreviewMod.setImage(new Image(existingFile.toURI().toString(), 220, 150, true, true));
                    imageNameLabelMod.setText("Image actuelle : " + existingFile.getName());
                    imageNameLabelMod.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");
                }
            } catch (Exception ignored) {}
        }

        Button chooseImageBtnMod = new Button("📁 Changer l'image");
        chooseImageBtnMod.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 10 25; -fx-cursor: hand; -fx-font-size: 13;");
        chooseImageBtnMod.setOnAction(e -> {
            File file = FileManager.chooseImage(chooseImageBtnMod.getScene().getWindow());
            if (file != null) {
                selectedImageFile = file;
                try {
                    imagePreviewMod.setImage(new Image(file.toURI().toString(), 220, 150, true, true));
                    imageNameLabelMod.setText("✅ Nouvelle image : " + file.getName());
                    imageNameLabelMod.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11; -fx-wrap-text: true;");
                } catch (Exception ex) {
                    showError("Erreur", "Impossible de charger l'image : " + ex.getMessage());
                }
            }
        });

        VBox imageSectionMod = new VBox(10, imagePreviewMod, imageNameLabelMod, chooseImageBtnMod);
        imageSectionMod.setStyle("-fx-padding: 15; -fx-background-color: #1e2749; -fx-background-radius: 10; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10;");
        imageSectionMod.setMaxWidth(600);
        fields4mod.getChildren().add(imageSectionMod);
        sec4mod.getChildren().add(fields4mod);
        mainContainer.getChildren().add(sec4mod);

        // ── Boutons (Modifier) ──
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));
        buttonBar.setMaxWidth(800);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10; -fx-font-size: 14;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("✅ Enregistrer les modifications");
        saveBtn.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14;");

        Region btnSpacer = new Region(); HBox.setHgrow(btnSpacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(btnSpacer, cancelBtn, saveBtn);
        mainContainer.getChildren().add(buttonBar);

        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 10;");
        dialogPane.setContent(scroll);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        hideDefaultButtons(dialogPane);

        saveBtn.setOnAction(event -> {
            boolean valid = true;

            if (titreField.getText().trim().isEmpty()) {
                titreField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errTitre.setText("Titre : champ obligatoire");
                valid = false;
            } else { resetFieldStyle(titreField); errTitre.setText(""); }

            if (descField.getText().trim().length() < 5) {
                descField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-control-inner-background: #1e2749;");
                errDesc.setText("Description : au moins 5 caractères");
                valid = false;
            } else { descField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-control-inner-background: #1e2749;"); errDesc.setText(""); }

            if (lieuField.getText().trim().isEmpty()) {
                lieuField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errLieu.setText("Lieu : champ obligatoire");
                valid = false;
            } else { resetFieldStyle(lieuField); errLieu.setText(""); }

            if (datePicker.getValue() == null) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                errDate.setText("Date : champ obligatoire");
                valid = false;
            } else { datePicker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;"); errDate.setText(""); }

            LocalTime parsedHeure = null;
            try {
                parsedHeure = LocalTime.parse(heureField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                resetFieldStyle(heureField); errHeure.setText("");
            } catch (Exception ex) {
                heureField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errHeure.setText("Heure : format HH:mm requis (ex: 18:30)");
                valid = false;
            }

            int nbPlaces = 0;
            try {
                nbPlaces = Integer.parseInt(placesField.getText().trim());
                if (nbPlaces <= 0) throw new NumberFormatException();
                resetFieldStyle(placesField); errPlaces.setText("");
            } catch (NumberFormatException ex) {
                placesField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10;");
                errPlaces.setText("Nb places : entier positif requis");
                valid = false;
            }

            if (valid) {
                try {
                    selected.setTitre(titreField.getText().trim());
                    selected.setDescription(descField.getText().trim());
                    selected.setLieu(lieuField.getText().trim());
                    selected.setDate(datePicker.getValue());
                    selected.setHeure(parsedHeure);
                    selected.setNbPlaces(nbPlaces);
                    selected.setLienGroupe(lienField.getText().trim().isEmpty() ? null : lienField.getText().trim());

                    if (selectedImageFile != null) {
                        if (currentImagePath != null && !currentImagePath.isEmpty()) {
                            FileManager.deleteImage(currentImagePath);
                        }
                        String newImagePath = FileManager.saveImageEvenement(selectedImageFile);
                        selected.setImagePath(newImagePath);
                    }

                    evenementCRUD.modifier(selected);
                    showInfo("Succès", "✅ Événement modifié avec succès !");
                    loadEvenements();
                    dialog.close();
                } catch (SQLException | java.io.IOException e) {
                    showError("Erreur", "Impossible de modifier l'événement : " + e.getMessage());
                }
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleSupprimer() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un événement à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'événement \"" + selected.getTitre() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    evenementCRUD.supprimer(selected.getId());
                    loadEvenements();
                } catch (SQLException e) {
                    showError("Erreur", "Impossible de supprimer : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleActualiser() {
        loadEvenements();
    }

    @FXML
    private void handleFrontOffice() {
        navigateTo("/Evenementsfront.fxml", "Événements (Front Office)");
    }

    // ==================== HELPERS DIALOGUES ====================

    private VBox createEvtSection(String title) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #111633; -fx-background-radius: 15; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 15;");
        section.setMaxWidth(800);
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");
        section.getChildren().add(lbl);
        return section;
    }

    private TextField createEvtTextField(String prompt, String tooltip) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        f.setPrefHeight(40);
        f.setMaxWidth(600);
        f.setTooltip(new Tooltip(tooltip));
        return f;
    }

    private Label createEvtErrorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-wrap-text: true;");
        l.setPrefWidth(600);
        return l;
    }

    private VBox createEvtFormField(String labelText, javafx.scene.Node field, javafx.scene.Node errorLabel) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(0, 0, 8, 0));
        container.setMaxWidth(600);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13; -fx-font-weight: 500;");
        container.getChildren().addAll(lbl, field);
        if (errorLabel != null) container.getChildren().add(errorLabel);
        return container;
    }

    private void resetFieldStyle(TextField f) {
        f.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
    }

    private void hideDefaultButtons(DialogPane pane) {
        Node ok = pane.lookupButton(ButtonType.OK);
        Node cancel = pane.lookupButton(ButtonType.CANCEL);
        if (ok != null) { ok.setVisible(false); ok.setManaged(false); }
        if (cancel != null) { cancel.setVisible(false); cancel.setManaged(false); }
    }

    // ==================== NAVIGATION ====================

    private void setupNavigationButtons() {
        setupSidebarButtonHover(btnDestinations, "🌍", "Destinations");
        if (btnDestinations != null) btnDestinations.setOnMouseClicked(e -> navigateTo("/DestinationBack.fxml", "Destinations"));

        setupSidebarButtonHover(btnHebergement, "🏨", "Hébergement");
        if (btnHebergement != null) btnHebergement.setOnMouseClicked(e -> navigateTo("/HebergementBack.fxml", "Hébergements"));

        setupSidebarButtonHover(btnUsers, "👥", "Utilisateurs");
        if (btnUsers != null) btnUsers.setOnMouseClicked(e -> navigateTo("/fxml/admin_users.fxml", "Utilisateurs"));

        setupSidebarButtonHover(btnStats, "📊", "Statistiques");
        if (btnStats != null) btnStats.setOnMouseClicked(e -> navigateTo("/fxml/admin_stats.fxml", "Statistiques"));

        setupSidebarButtonHover(btnItineraires, "🗺️", "Itinéraires");
        if (btnItineraires != null) btnItineraires.setOnMouseClicked(e -> navigateTo("/ItineraireEtEtape/PageGestionItineraires.fxml", "Itinéraires"));

        setupSidebarButtonHover(btnCategories, "📑", "Catégories");
        if (btnCategories != null) btnCategories.setOnMouseClicked(e -> navigateTo("/categoriesback.fxml", "Catégories"));

        setupSidebarButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) btnActivites.setOnMouseClicked(e -> navigateTo("/activitesback.fxml", "Activités"));

        // Événements — page courante, on recharge simplement
        if (btnEvenements != null) btnEvenements.setOnMouseClicked(e -> loadEvenements());

        setupSidebarButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) btnVoyages.setOnMouseClicked(e -> navigateTo("/PageVoyageBack.fxml", "Voyages"));

        setupSidebarButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) btnBudgets.setOnMouseClicked(e -> navigateTo("/BudgetDepenseBack.fxml", "Budgets"));
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            // Utilise le premier nœud disponible pour récupérer la scène
            Stage stage = getStage();
            if (stage == null) return;
            stage.setScene(new Scene(root,
                    javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
                    javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Stage getStage() {
        if (btnEvenements != null && btnEvenements.getScene() != null)
            return (Stage) btnEvenements.getScene().getWindow();
        if (tableEvenements != null && tableEvenements.getScene() != null)
            return (Stage) tableEvenements.getScene().getWindow();
        if (userProfileBox != null && userProfileBox.getScene() != null)
            return (Stage) userProfileBox.getScene().getWindow();
        return null;
    }

    private void setupSidebarButtonHover(HBox button, String icon, String text) {
        if (button == null) return;
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.15); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(node -> {
                if (node instanceof Label lbl && !lbl.getText().equals(icon) && !lbl.getText().matches("\\d+"))
                    lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
            });
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(node -> {
                if (node instanceof Label lbl && !lbl.getText().equals(icon) && !lbl.getText().matches("\\d+"))
                    lbl.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: 500; -fx-font-size: 14;");
            });
        });
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(e -> navigateTo("/fxml/profile.fxml", "Mon Profil"));
            userProfileBox.setOnMouseEntered(e ->
                    userProfileBox.setStyle("-fx-background-color: #2d3759; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(e ->
                    userProfileBox.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        }
    }

    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            set(lblUserName, currentUser.getPrenom() + " " + currentUser.getNom());
            set(lblUserRole, currentUser.getRole());
        } else {
            set(lblUserName, "Utilisateur");
            set(lblUserRole, "Administrateur");
        }
    }

    private void updateLastUpdateTime() {
        set(lblLastUpdate, "Dernière mise à jour: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
    }

    // ==================== UTILITIES ====================

    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
