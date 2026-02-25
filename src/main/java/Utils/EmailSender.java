package Utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    // 🔥 REMPLACE CES INFORMATIONS PAR CELLES DE MAILTRAP 🔥
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587"; // Port TLS
    private static final String SMTP_USERNAME = "malekfekih01@gmail.com";
    private static final String SMTP_PASSWORD = "lvog vjig bcof avrw"; //cftj tefy tnei eapz


    public static void sendResetCode(String recipientEmail, String code) throws MessagingException {
        // ... même configuration que sendResetEmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Code de vérification TravelMate");
        // Dans le contenu du message, inclure le code
        message.setText("Bonjour,\n\nVous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Votre code de vérification est : " + code + "\n\n" +
                "Ce code est valable 5 minutes.\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                "Cordialement,\nL'équipe TravelMate");
        Transport.send(message);
    }

    public static void sendVerificationEmail(String recipientEmail, String code) throws MessagingException {
        // ... même configuration que sendResetEmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Code de vérification TravelMate");
        // Dans le contenu du message, inclure le code
        message.setText("Bonjour,\n\nMerci de vous être inscrit sur TravelMate.\n\n" +
                "Votre code de vérification est : " + code + "\n\n" +
                "Ce code est valable 5 minutes.\n\n" +
                "Si vous n'êtes pas à l'origine de cette inscription, ignorez cet email.\n\n" +
                "Cordialement,\nL'équipe TravelMate");
        Transport.send(message);
    }
}