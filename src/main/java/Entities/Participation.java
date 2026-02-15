package Entities;

public class Participation {
    private int id_participation;
    private int id; //id_user
    private String role_participation;
    private int id_voyage;

    // Champs supplémentaires pour l'affichage (pas dans la BD)
    private String email;
    private String nom;
    private String prenom;

    public Participation() {}

    public Participation(int id, String role_participation, int id_voyage) {
        this.id = id;
        this.role_participation = role_participation;
        this.id_voyage = id_voyage;
    }


    public Participation(int id_participation, int id, String role_participation, int id_voyage) {
        this.id_participation = id_participation;
        this.id = id;
        this.role_participation = role_participation;
        this.id_voyage = id_voyage;
    }

    // Getters et Setters existants
    public int getId_participation() { return id_participation; }
    public void setId_participation(int id_participation) { this.id_participation = id_participation; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRole_participation() { return role_participation; }
    public void setRole_participation(String role_participation) { this.role_participation = role_participation; }

    public int getId_voyage() { return id_voyage; }
    public void setId_voyage(int id_voyage) { this.id_voyage = id_voyage; }

    // Nouveaux getters et setters pour l'affichage
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", id=" + id +
                ", email=" + email +
                ", role_participation='" + role_participation + '\'' +
                ", id_voyage=" + id_voyage +
                '}';
    }
}