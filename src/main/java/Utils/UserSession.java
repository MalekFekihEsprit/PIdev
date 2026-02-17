package Utils;

import Entities.User;

public class UserSession { // Classe singleton pour stocker l'utilisateur connecté et partager les données de session à travers les différentes scènes
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clear() {
        currentUser = null;
    }
}