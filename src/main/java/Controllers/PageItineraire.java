package Controllers;

import Services.itineraireCRUD;
import Entites.Itineraire;
import Utils.AlertUtil;
import Utils.ExportUtil;
import Utils.MyBD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PageItineraire {
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnRechercher;
    @FXML private Button btnReinitialiserFiltres;
    @FXML private Label lblTotalItinerairesTable;
    @FXML private VBox emptyPlaceholder;
    @FXML private Button btnCreerPremierItineraire;
    @FXML private VBox itinerariesContainer;
    @FXML private ComboBox<String> triCombo;
    @FXML private TextField searchField;
    @FXML private Label lblDestinationsCount;
    @FXML private Label lblJoursCount;
    @FXML private Label lblResultCount; // NOUVEAU: Pour afficher le nombre de résultats
    @FXML private Label lblListeTitre; // NOUVEAU: Pour rendre le titre dynamique

    private itineraireCRUD itineraireCRUD;
    private List<Itineraire> itineraires;
    private List<Itineraire> itinerairesOriginaux;
    private MainLayoutController mainLayoutController;

    // Classes pour les informations de voyage
    private class VoyageInfo {
        String titre;
        String destination;
        int nbJours;
        LocalDate dateDebut;
        LocalDate dateFin;

        VoyageInfo(String titre, String destination, int nbJours, LocalDate dateDebut, LocalDate dateFin) {
            this.titre = titre;
            this.destination = destination;
            this.nbJours = nbJours;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }
    }

    private java.util.Map<Integer, VoyageInfo> voyageInfoCache = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        itineraireCRUD = new itineraireCRUD();
        mainLayoutController = MainLayoutController.getInstance();

        // Initialiser les options de tri
        if (triCombo != null) {
            triCombo.getItems().addAll(
                    "Par défaut",
                    "Nom (A → Z)",
                    "Nom (Z → A)",
                    "Date (plus récent)",
                    "Date (plus ancien)",
                    "Nombre de jours (croissant)",
                    "Nombre de jours (décroissant)"
            );
            triCombo.getSelectionModel().selectFirst();
            triCombo.setOnAction(e -> appliquerTousFiltres());
        }

        // Recherche en temps réel
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                appliquerTousFiltres();
            });
        }

        // Charger les destinations dans le filtre
        chargerDestinations();

        // Configurer les boutons de filtre
        if (btnRechercher != null) {
            btnRechercher.setOnAction(e -> appliquerTousFiltres());
        }

        if (btnReinitialiserFiltres != null) {
            btnReinitialiserFiltres.setOnAction(e -> reinitialiserFiltres());
        }

        // Ajouter des listeners pour les DatePicker
        if (dateDebut != null) {
            dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && dateFin.getValue() != null && newVal.isAfter(dateFin.getValue())) {
                    AlertUtil.showWarning("Attention", "La date de début ne peut pas être après la date de fin");
                    dateDebut.setValue(oldVal);
                }
            });
        }

        if (dateFin != null) {
            dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && dateDebut.getValue() != null && newVal.isBefore(dateDebut.getValue())) {
                    AlertUtil.showWarning("Attention", "La date de fin ne peut pas être avant la date de début");
                    dateFin.setValue(oldVal);
                }
            });
        }

        // Listener pour le combo destination - applique automatiquement les filtres
        if (comboDestination != null) {
            comboDestination.valueProperty().addListener((obs, oldVal, newVal) -> {
                appliquerTousFiltres();
            });
        }

        refreshItineraries();
        System.out.println("PageItineraire initialisée avec mainLayoutController: " + (mainLayoutController != null));
    }

    // Charger les destinations depuis la base de données
    private void chargerDestinations() {
        if (comboDestination == null) return;

        ObservableList<String> destinations = FXCollections.observableArrayList();
        destinations.add("Toutes les destinations");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT DISTINCT d.nom_destination FROM destination d ORDER BY d.nom_destination";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                destinations.add(rs.getString("nom_destination"));
            }

            comboDestination.setItems(destinations);
            comboDestination.getSelectionModel().selectFirst();

            // Mettre à jour le compteur de destinations
            if (lblDestinationsCount != null) {
                lblDestinationsCount.setText(destinations.size() - 1 + " destinations");
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

    // Obtenir les informations de voyage avec cache
    private VoyageInfo getVoyageInfo(int idVoyage) {
        if (voyageInfoCache.containsKey(idVoyage)) {
            return voyageInfoCache.get(idVoyage);
        }

        String titre = "Voyage";
        String destination = "";
        int nbJours = 5;
        LocalDate dateDebut = null;
        LocalDate dateFin = null;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT v.titre_voyage, v.date_debut, v.date_fin, d.nom_destination " +
                    "FROM voyage v " +
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                    "WHERE v.id_voyage = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, idVoyage);
            rs = stmt.executeQuery();

            if (rs.next()) {
                titre = rs.getString("titre_voyage");
                destination = rs.getString("nom_destination");

                Timestamp tsDebut = rs.getTimestamp("date_debut");
                Timestamp tsFin = rs.getTimestamp("date_fin");

                if (tsDebut != null) {
                    dateDebut = tsDebut.toLocalDateTime().toLocalDate();
                }
                if (tsFin != null) {
                    dateFin = tsFin.toLocalDateTime().toLocalDate();
                }

                if (dateDebut != null && dateFin != null) {
                    long diff = java.time.Duration.between(dateDebut.atStartOfDay(), dateFin.atStartOfDay()).toDays();
                    nbJours = (int) diff + 1;
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

        VoyageInfo info = new VoyageInfo(titre, destination, nbJours, dateDebut, dateFin);
        voyageInfoCache.put(idVoyage, info);
        return info;
    }

    // Appliquer tous les filtres (recherche, destination, dates)
    private void appliquerTousFiltres() {
        if (itinerairesOriginaux == null) return;

        // Commencer avec tous les itinéraires
        List<Itineraire> filtres = new ArrayList<>(itinerairesOriginaux);

        // 1. Filtrer par recherche textuelle
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        if (!searchText.isEmpty()) {
            filtres = filtres.stream()
                    .filter(i -> {
                        VoyageInfo info = getVoyageInfo(i.getId_voyage());
                        return i.getNom_itineraire().toLowerCase().contains(searchText) ||
                                (i.getDescription_itineraire() != null && i.getDescription_itineraire().toLowerCase().contains(searchText)) ||
                                (info.titre != null && info.titre.toLowerCase().contains(searchText)) ||
                                (info.destination != null && info.destination.toLowerCase().contains(searchText));
                    })
                    .collect(Collectors.toList());
        }

        // 2. Filtrer par destination
        String destinationFilter = comboDestination != null ? comboDestination.getValue() : null;
        if (destinationFilter != null && !destinationFilter.equals("Toutes les destinations")) {
            filtres = filtres.stream()
                    .filter(i -> {
                        VoyageInfo info = getVoyageInfo(i.getId_voyage());
                        return destinationFilter.equals(info.destination);
                    })
                    .collect(Collectors.toList());
        }

        // 3. Filtrer par dates
        LocalDate debut = dateDebut != null ? dateDebut.getValue() : null;
        LocalDate fin = dateFin != null ? dateFin.getValue() : null;

        if (debut != null || fin != null) {
            filtres = filtres.stream()
                    .filter(i -> {
                        VoyageInfo info = getVoyageInfo(i.getId_voyage());

                        if (info.dateDebut == null || info.dateFin == null) {
                            return debut == null && fin == null;
                        }

                        boolean apresDebut = debut == null || !info.dateFin.isBefore(debut);
                        boolean avantFin = fin == null || !info.dateDebut.isAfter(fin);

                        return apresDebut && avantFin;
                    })
                    .collect(Collectors.toList());
        }

        // Appliquer les résultats filtrés
        itineraires = filtres;
        appliquerTri();
    }

    // Appliquer le tri sur la liste déjà filtrée
    private void appliquerTri() {
        if (itineraires == null || itineraires.isEmpty() || triCombo == null) {
            updateItineraryDisplay();
            return;
        }

        String selectedTri = triCombo.getSelectionModel().getSelectedItem();
        if (selectedTri == null || selectedTri.equals("Par défaut")) {
            updateItineraryDisplay();
            return;
        }

        try {
            switch (selectedTri) {
                case "Nom (A → Z)":
                    itineraires = itineraireCRUD.trierParNom(itineraires, true);
                    break;
                case "Nom (Z → A)":
                    itineraires = itineraireCRUD.trierParNom(itineraires, false);
                    break;
                case "Date (plus récent)":
                    itineraires = trierParDate(itineraires, false);
                    break;
                case "Date (plus ancien)":
                    itineraires = trierParDate(itineraires, true);
                    break;
                case "Nombre de jours (croissant)":
                    itineraires = trierParNombreJours(itineraires, true);
                    break;
                case "Nombre de jours (décroissant)":
                    itineraires = trierParNombreJours(itineraires, false);
                    break;
                default:
                    break;
            }
            updateItineraryDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Trier par date
    private List<Itineraire> trierParDate(List<Itineraire> list, boolean ascendant) {
        return list.stream()
                .sorted((i1, i2) -> {
                    VoyageInfo info1 = getVoyageInfo(i1.getId_voyage());
                    VoyageInfo info2 = getVoyageInfo(i2.getId_voyage());

                    if (info1.dateDebut == null && info2.dateDebut == null) return 0;
                    if (info1.dateDebut == null) return ascendant ? 1 : -1;
                    if (info2.dateDebut == null) return ascendant ? -1 : 1;

                    int comparison = info1.dateDebut.compareTo(info2.dateDebut);
                    return ascendant ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    // Trier par nombre de jours
    private List<Itineraire> trierParNombreJours(List<Itineraire> list, boolean ascendant) {
        return list.stream()
                .sorted((i1, i2) -> {
                    VoyageInfo info1 = getVoyageInfo(i1.getId_voyage());
                    VoyageInfo info2 = getVoyageInfo(i2.getId_voyage());

                    int comparison = Integer.compare(info1.nbJours, info2.nbJours);
                    return ascendant ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    public void refreshItineraries() {
        try {
            itinerairesOriginaux = itineraireCRUD.afficher();
            itineraires = new ArrayList<>(itinerairesOriginaux);

            // Mettre à jour les statistiques globales
            mettreAJourStatistiques();

            appliquerTousFiltres();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les itinéraires: " + e.getMessage());
        }
    }

    // Mettre à jour les statistiques
    private void mettreAJourStatistiques() {
        if (itinerairesOriginaux == null) return;

        int totalJours = 0;
        for (Itineraire itineraire : itinerairesOriginaux) {
            VoyageInfo info = getVoyageInfo(itineraire.getId_voyage());
            totalJours += info.nbJours;
        }

        if (lblJoursCount != null) {
            lblJoursCount.setText(totalJours + " jours d'aventure");
        }
    }

    private void updateItineraryDisplay() {
        if (itineraires == null || itineraires.isEmpty()) {
            // Afficher le placeholder vide
            emptyPlaceholder.setVisible(true);
            emptyPlaceholder.setManaged(true);
            if (itinerariesContainer != null) {
                itinerariesContainer.setVisible(false);
                itinerariesContainer.setManaged(false);
            }

            // Mettre à jour le titre dynamique
            if (lblListeTitre != null) {
                String destinationFilter = comboDestination != null ? comboDestination.getValue() : null;
                if (destinationFilter != null && !destinationFilter.equals("Toutes les destinations")) {
                    lblListeTitre.setText("Aucun itinéraire pour " + destinationFilter);
                } else {
                    lblListeTitre.setText("Aucun itinéraire trouvé");
                }
            }

            if (lblTotalItinerairesTable != null) {
                lblTotalItinerairesTable.setText("0 itinéraire");
            }
            if (lblResultCount != null) {
                lblResultCount.setText("0");
            }
        } else {
            // Afficher les itinéraires
            emptyPlaceholder.setVisible(false);
            emptyPlaceholder.setManaged(false);
            if (itinerariesContainer != null) {
                itinerariesContainer.setVisible(true);
                itinerariesContainer.setManaged(true);

                // Mettre à jour le titre dynamique
                if (lblListeTitre != null) {
                    String destinationFilter = comboDestination != null ? comboDestination.getValue() : null;
                    if (destinationFilter != null && !destinationFilter.equals("Toutes les destinations")) {
                        lblListeTitre.setText("Itinéraires à " + destinationFilter);
                    } else {
                        lblListeTitre.setText("Tous les itinéraires");
                    }
                }

                lblTotalItinerairesTable.setText(itineraires.size() + " itinéraire" + (itineraires.size() > 1 ? "s" : ""));
                if (lblResultCount != null) {
                    lblResultCount.setText(String.valueOf(itineraires.size()));
                }
                generateItineraryCards();
            }
        }
    }

    private void generateItineraryCards() {
        itinerariesContainer.getChildren().clear();
        for (Itineraire itineraire : itineraires) {
            try {
                VBox card = createItineraryCard(itineraire);
                itinerariesContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createItineraryCard(Itineraire itineraire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraryCard.fxml"));
            VBox card = loader.load();

            ItineraryCardController controller = loader.getController();

            if (controller == null) {
                System.err.println("Erreur: ItineraryCardController est null");
                return createSimpleCard(itineraire);
            }

            VoyageInfo voyageInfo = getVoyageInfo(itineraire.getId_voyage());
            String affichage = voyageInfo.titre;
            if (voyageInfo.destination != null && !voyageInfo.destination.isEmpty()) {
                affichage += " - " + voyageInfo.destination;
            }

            controller.setData(
                    itineraire,
                    affichage,
                    voyageInfo.nbJours,
                    getEmojiForItinerary(itineraire),
                    this::handleModifierItineraire,
                    this::handleSupprimerItineraire,
                    this::handleJourClick,
                    this::handleExporterItineraire
            );

            return card;

        } catch (IOException e) {
            e.printStackTrace();
            return createSimpleCard(itineraire);
        }
    }

    private VBox createSimpleCard(Itineraire itineraire) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 16;");

        Label nameLabel = new Label(itineraire.getNom_itineraire());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label descLabel = new Label(itineraire.getDescription_itineraire());
        descLabel.setWrapText(true);

        card.getChildren().addAll(nameLabel, descLabel);
        return card;
    }

    private void handleModifierItineraire(Itineraire itineraire) {
        try {
            System.out.println("Tentative de modification de l'itinéraire: " + itineraire.getNom_itineraire());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageModifierItineraire.fxml"));
            Parent modifierView = loader.load();

            PageModifierItineraire modifierController = loader.getController();
            modifierController.setItineraire(itineraire);
            modifierController.setParentController(this);

            if (mainLayoutController != null) {
                System.out.println("Chargement de la page de modification");
                mainLayoutController.loadPageDirect(modifierView);
            } else {
                System.err.println("mainLayoutController est null!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    private void handleSupprimerItineraire(Itineraire itineraire) {
        if (AlertUtil.showConfirmation("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer l'itinéraire \"" + itineraire.getNom_itineraire() + "\" ?")) {
            try {
                itineraireCRUD.supprimer(itineraire.getId_itineraire());
                refreshItineraries();
                AlertUtil.showInfo("Succès", "Itinéraire supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de supprimer l'itinéraire: " + e.getMessage());
            }
        }
    }

    private String getEmojiForItinerary(Itineraire itineraire) {
        String nom = itineraire.getNom_itineraire().toLowerCase();
        if (nom.contains("plage") || nom.contains("mer")) return "🏖️";
        if (nom.contains("montagne") || nom.contains("ski")) return "🏔️";
        if (nom.contains("rome") || nom.contains("histoire")) return "🏛️";
        if (nom.contains("paris") || nom.contains("ville")) return "🌆";
        return "🗺️";
    }

    @FXML
    private void handleCreerClick() {
        try {
            System.out.println("Création d'un nouvel itinéraire");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutItineraire.fxml"));

            if (loader.getLocation() == null) {
                AlertUtil.showError("Erreur", "Fichier FXML non trouvé: /PageAjoutItineraire.fxml");
                return;
            }

            Parent ajoutView = loader.load();

            PageAjoutItineraire ajoutController = loader.getController();
            ajoutController.setParentController(this);

            if (mainLayoutController != null) {
                System.out.println("Chargement de la page d'ajout");
                mainLayoutController.loadPageDirect(ajoutView);
            } else {
                System.err.println("mainLayoutController est null!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercherClick() {
        appliquerTousFiltres();
    }

    // Réinitialiser tous les filtres
    @FXML
    private void reinitialiserFiltres() {
        if (comboDestination != null) {
            comboDestination.getSelectionModel().selectFirst();
        }
        if (dateDebut != null) {
            dateDebut.setValue(null);
        }
        if (dateFin != null) {
            dateFin.setValue(null);
        }
        if (searchField != null) {
            searchField.clear();
        }
        if (triCombo != null) {
            triCombo.getSelectionModel().selectFirst();
        }

        // Recharger les itinéraires originaux
        if (itinerairesOriginaux != null) {
            itineraires = new ArrayList<>(itinerairesOriginaux);
            appliquerTri();
        }
    }

    @FXML
    private void handleTrierParDate() {
        if (triCombo != null) {
            triCombo.getSelectionModel().select("Date (plus récent)");
            appliquerTousFiltres();
        }
    }

    @FXML
    private void handleExporter() {
        if (itineraires != null && !itineraires.isEmpty()) {
            Services.etapeCRUD etapeCRUD = new Services.etapeCRUD();
            ExportUtil.exporterTousItineraires(itineraires, etapeCRUD, itinerariesContainer.getScene().getWindow());
        } else {
            AlertUtil.showWarning("Aucun itinéraire", "Il n'y a aucun itinéraire à exporter.");
        }
    }

    private void handleJourClick(int numeroJour, int idItineraire, String nomItineraire) {
        try {
            System.out.println("Clic sur le jour " + numeroJour + " de l'itinéraire: " + nomItineraire);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageEtape.fxml"));
            Parent detailsJourView = loader.load();

            PageEtape detailsJourController = loader.getController();
            detailsJourController.setJourInfo(numeroJour, idItineraire, nomItineraire);

            if (mainLayoutController != null) {
                System.out.println("Chargement de la page des étapes");
                mainLayoutController.loadPageDirect(detailsJourView);
            } else {
                System.err.println("mainLayoutController est null!");
                mainLayoutController = MainLayoutController.getInstance();
                if (mainLayoutController != null) {
                    mainLayoutController.loadPageDirect(detailsJourView);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir les détails du jour: " + e.getMessage());
        }
    }

    private void handleExporterItineraire(Itineraire itineraire) {
        try {
            Services.etapeCRUD etapeCRUD = new Services.etapeCRUD();
            List<Entites.etape> etapes = etapeCRUD.getEtapesByItineraire(itineraire.getId_itineraire());
            ExportUtil.exporterItineraire(itineraire, etapes, itinerariesContainer.getScene().getWindow());
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les étapes: " + e.getMessage());
        }
    }
}