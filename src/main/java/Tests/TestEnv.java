package Tests;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestEnv {
    public static void main(String[] args) {
        // Test 1: Variable d'environnement
        String envVar = System.getenv("AI_AGENT");
        System.out.println("System.getenv(\"AI_AGENT\") = " + envVar);

        // Test 2: Propriété système
        String sysProp = System.getProperty("AI_AGENT");
        System.out.println("System.getProperty(\"AI_AGENT\") = " + sysProp);

        // Test 3: Lecture directe du fichier .env
        try {
            String fromFile = Files.lines(Paths.get(".env"))
                    .filter(l -> l.startsWith("AI_AGENT="))
                    .map(l -> l.split("=", 2)[1].trim())
                    .findFirst()
                    .orElse(null);
            System.out.println("Lecture directe .env = " + fromFile);
        } catch (Exception e) {
            System.out.println("Erreur lecture .env: " + e);
        }

        // Test 4: Répertoire courant
        System.out.println("user.dir = " + System.getProperty("user.dir"));
    }
}