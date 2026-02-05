package Entities;

public class Participation {
    private int id_participation;
    private int id; //id_user
    private String role_participation;
    private int id_voyage;

    public Participation() {}
    public Participation( int id, String role_participation, int id_voyage) {

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

    public int getId_participation() {
        return id_participation;
    }

    public int getId() {
        return id;
    }

    public String getRole_participation() {
        return role_participation;
    }

    public int getId_voyage() {
        return id_voyage;
    }

    public void setId_participation(int id_participation) {
        this.id_participation = id_participation;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRole_participation(String role_participation) {
        this.role_participation = role_participation;
    }

    public void setId_voyage(int id_voyage) {
        this.id_voyage = id_voyage;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", id=" + id +
                ", role_participation='" + role_participation + '\'' +
                ", id_voyage=" + id_voyage +
                '}';
    }
}
