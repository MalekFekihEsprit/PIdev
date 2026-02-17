package Entities;

import javafx.scene.paint.Color;

public enum CategorieDepense {
    HEBERGEMENT("🏨", "Hébergement", "#10b981"),
    TRANSPORT("🚗", "Transport", "#3b82f6"),
    RESTAURATION("🍽️", "Restauration", "#f59e0b"),
    ACTIVITES("🎭", "Activités", "#8b5cf6"),
    SHOPPING("🛍️", "Shopping", "#ec4899"),
    AUTRE("📦", "Autre", "#64748b");

    private final String icone;
    private final String libelle;
    private final String couleur;

    CategorieDepense(String icone, String libelle, String couleur) {
        this.icone = icone;
        this.libelle = libelle;
        this.couleur = couleur;
    }

    public String getIcone() { return icone; }
    public String getLibelle() { return libelle; }
    public String getCouleur() { return couleur; }

    public static CategorieDepense fromString(String text) {
        for (CategorieDepense c : CategorieDepense.values()) {
            if (c.libelle.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return AUTRE;
    }

    @Override
    public String toString() {
        return icone + " " + libelle;
    }
}