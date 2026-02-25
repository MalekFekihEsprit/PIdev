package Utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
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

    public static void sendWarningEmailWithAttachment(String recipientEmail, File attachment) throws MessagingException {
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
        message.setSubject("Alerte de sécurité - Tentatives de connexion échouées");
        
        // Corps du message
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Bonjour,\n\nQuelqu'un a essayé de se connecter à votre compte à trois reprises avec un mot de passe incorrect. La photo de la personne a été jointe.\n\nSi c'était vous, ignorez ce message. Sinon, vérifiez la sécurité de votre compte.\n\nCordialement,\nL'équipe TravelMate");
        
        // Pièce jointe
        MimeBodyPart attachPart = new MimeBodyPart();
        try {
            attachPart.attachFile(attachment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Assembler
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachPart);
        
        message.setContent(multipart);
        Transport.send(message);
    }
}