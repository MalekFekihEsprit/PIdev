package Services;

import Utils.Config;
import Utils.ConfigV;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Service d'envoi d'emails HTML avec le design TravelMate.
 * Utilise SMTP (Gmail par défaut) configuré via .env ou Config.
 */
public class EmailService {

    private static EmailService instance;

    private EmailService() {}

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    // ======================== ENVOI GÉNÉRIQUE ========================

    /**
     * Envoie un email HTML à un destinataire.
     */
    public void envoyerEmail(String destinataire, String sujet, String contenuHtml) throws MessagingException, UnsupportedEncodingException {
        if (!ConfigV.isEmailConfigured()) {
            System.out.println("⚠️ Email non configuré. Configurez SMTP_EMAIL et SMTP_PASSWORD dans .env");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", ConfigV.SMTP_HOST);
        props.put("mail.smtp.port", ConfigV.SMTP_PORT);
        props.put("mail.smtp.ssl.trust", ConfigV.SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ConfigV.SMTP_EMAIL, ConfigV.SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(ConfigV.SMTP_EMAIL, ConfigV.SMTP_FROM_NAME, "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
        message.setSubject(sujet);
        message.setContent(contenuHtml, "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("✅ Email envoyé avec succès à " + destinataire);
    }

    // ======================== EMAIL CONFIRMATION PAIEMENT ========================

    /**
     * Envoie un email de confirmation de paiement avec le design TravelMate.
     */
    public void envoyerConfirmationPaiement(String email, String nomVoyage, String destination,
                                             String dates, double montant, String devise,
                                             String transactionId, String methode) throws MessagingException, UnsupportedEncodingException {
        String sujet = "✈️ Confirmation de paiement — " + nomVoyage + " | TravelMate";
        String html = buildConfirmationPaiementHtml(nomVoyage, destination, dates, montant, devise, transactionId, methode);
        envoyerEmail(email, sujet, html);
    }

    /**
     * Envoie un email de remboursement avec le design TravelMate.
     */
    public void envoyerConfirmationRemboursement(String email, String nomVoyage,
                                                   double montant, String devise,
                                                   String transactionId) throws MessagingException, UnsupportedEncodingException {
        String sujet = "↩️ Remboursement confirmé — " + nomVoyage + " | TravelMate";
        String html = buildRemboursementHtml(nomVoyage, montant, devise, transactionId);
        envoyerEmail(email, sujet, html);
    }

    /**
     * Envoie un email de confirmation de participation avec le design TravelMate.
     */
    public void envoyerConfirmationParticipation(String email, String nomParticipant,
                                                   String nomVoyage, String destination,
                                                   String dates, String role) throws MessagingException, UnsupportedEncodingException {
        String sujet = "🎉 Inscription confirmée — " + nomVoyage + " | TravelMate";
        String html = buildParticipationHtml(nomParticipant, nomVoyage, destination, dates, role);
        envoyerEmail(email, sujet, html);
    }

    /**
     * Envoie un email d'information lorsqu'un admin ajoute un participant depuis le back-office.
     */
    public void envoyerNotificationAjoutParAdmin(String email, String nomParticipant,
                                                    String nomVoyage, String destination,
                                                    String dates, String role) throws MessagingException, UnsupportedEncodingException {
        String sujet = "📌 Vous avez été ajouté(e) au voyage " + nomVoyage + " | TravelMate";
        String html = buildAjoutParAdminHtml(nomParticipant, nomVoyage, destination, dates, role);
        envoyerEmail(email, sujet, html);
    }

    // ======================== TEMPLATES HTML ========================

    private String buildConfirmationPaiementHtml(String nomVoyage, String destination,
                                                   String dates, double montant, String devise,
                                                   String transactionId, String methode) {
        String montantFormate = String.format("%.2f %s", montant, devise);
        String iconMethode = "STRIPE".equals(methode) ? "💳" : "PAYPAL".equals(methode) ? "🅿️" : "💰";

        return "<!DOCTYPE html>\n"
                + "<html lang='fr'>\n"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>\n"
                + "<body style='margin:0;padding:0;background-color:#0a0e27;font-family:Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;'>\n"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#0a0e27;padding:40px 0;'>\n"
                + "<tr><td align='center'>\n"

                // Container principal
                + "<table role='presentation' width='600' cellpadding='0' cellspacing='0' style='background-color:#111633;border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.5);'>\n"

                // Header avec gradient orange
                + "<tr><td style='background:linear-gradient(135deg,#ff8c42,#ff6b4a);padding:40px 40px 30px;text-align:center;'>\n"
                + "  <div style='font-size:36px;margin-bottom:8px;'>✈️</div>\n"
                + "  <h1 style='color:#ffffff;font-size:28px;margin:0 0 4px;font-weight:700;letter-spacing:-0.5px;'>TravelMate</h1>\n"
                + "  <p style='color:rgba(255,255,255,0.85);font-size:14px;margin:0;'>Votre compagnon de voyage</p>\n"
                + "</td></tr>\n"

                // Bandeau succès
                + "<tr><td style='background-color:#10b981;padding:16px 40px;text-align:center;'>\n"
                + "  <span style='color:#ffffff;font-size:16px;font-weight:600;'>✅ Paiement confirmé avec succès !</span>\n"
                + "</td></tr>\n"

                // Corps du message
                + "<tr><td style='padding:36px 40px 20px;'>\n"
                + "  <p style='color:#94a3b8;font-size:15px;margin:0 0 24px;line-height:1.6;'>\n"
                + "    Bonjour,<br>Votre paiement a été traité avec succès. Voici le récapitulatif de votre transaction :\n"
                + "  </p>\n"

                // Carte voyage
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;margin-bottom:24px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0'>\n"
                + "        <tr>\n"
                + "          <td style='padding-bottom:16px;border-bottom:1px solid rgba(148,163,184,0.15);'>\n"
                + "            <span style='color:#ff8c42;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>🌍 Voyage</span>\n"
                + "            <h2 style='color:#ffffff;font-size:22px;margin:6px 0 0;font-weight:700;'>" + nomVoyage + "</h2>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr><td style='padding-top:16px;'>\n"
                + "          <table role='presentation' width='100%' cellpadding='0' cellspacing='0'>\n"
                + "            <tr>\n"
                + "              <td width='50%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>📍 Destination</span><br>\n"
                + "                <span style='color:#ffffff;font-size:15px;font-weight:500;'>" + destination + "</span>\n"
                + "              </td>\n"
                + "              <td width='50%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>📅 Dates</span><br>\n"
                + "                <span style='color:#ffffff;font-size:15px;font-weight:500;'>" + dates + "</span>\n"
                + "              </td>\n"
                + "            </tr>\n"
                + "          </table>\n"
                + "        </td></tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"

                // Carte paiement
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;margin-bottom:24px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <span style='color:#3b82f6;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>" + iconMethode + " Détails du paiement</span>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='margin-top:16px;'>\n"
                + "        <tr>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);'>\n"
                + "            <span style='color:#94a3b8;font-size:13px;'>Méthode</span>\n"
                + "          </td>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);text-align:right;'>\n"
                + "            <span style='color:#ffffff;font-size:14px;font-weight:500;'>" + methode + "</span>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);'>\n"
                + "            <span style='color:#94a3b8;font-size:13px;'>Transaction ID</span>\n"
                + "          </td>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);text-align:right;'>\n"
                + "            <span style='color:#ff8c42;font-size:12px;font-family:Consolas,monospace;'>" + transactionId + "</span>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr>\n"
                + "          <td style='padding:14px 0 0;'>\n"
                + "            <span style='color:#94a3b8;font-size:13px;'>Montant total</span>\n"
                + "          </td>\n"
                + "          <td style='padding:14px 0 0;text-align:right;'>\n"
                + "            <span style='color:#10b981;font-size:24px;font-weight:700;'>" + montantFormate + "</span>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"

                + "</td></tr>\n"

                // Séparateur
                + "<tr><td style='padding:0 40px;'>\n"
                + "  <hr style='border:none;border-top:1px solid rgba(148,163,184,0.15);margin:0;'>\n"
                + "</td></tr>\n"

                // Message de remerciement
                + "<tr><td style='padding:24px 40px 16px;text-align:center;'>\n"
                + "  <p style='color:#94a3b8;font-size:14px;line-height:1.6;margin:0;'>\n"
                + "    Merci pour votre confiance ! Nous vous souhaitons un excellent voyage. 🌟\n"
                + "  </p>\n"
                + "</td></tr>\n"

                // Footer
                + "<tr><td style='background-color:#0a0e27;padding:28px 40px;text-align:center;border-top:1px solid rgba(148,163,184,0.1);'>\n"
                + "  <p style='color:#64748b;font-size:12px;margin:0 0 8px;'>© 2025 TravelMate — Tous droits réservés</p>\n"
                + "  <p style='color:#4a5568;font-size:11px;margin:0;'>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>\n"
                + "</td></tr>\n"

                + "</table>\n"
                + "</td></tr></table>\n"
                + "</body></html>";
    }

    private String buildRemboursementHtml(String nomVoyage, double montant, String devise, String transactionId) {
        String montantFormate = String.format("%.2f %s", montant, devise);

        return "<!DOCTYPE html>\n"
                + "<html lang='fr'>\n"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>\n"
                + "<body style='margin:0;padding:0;background-color:#0a0e27;font-family:Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;'>\n"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#0a0e27;padding:40px 0;'>\n"
                + "<tr><td align='center'>\n"

                + "<table role='presentation' width='600' cellpadding='0' cellspacing='0' style='background-color:#111633;border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.5);'>\n"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#ff8c42,#ff6b4a);padding:40px 40px 30px;text-align:center;'>\n"
                + "  <div style='font-size:36px;margin-bottom:8px;'>✈️</div>\n"
                + "  <h1 style='color:#ffffff;font-size:28px;margin:0 0 4px;font-weight:700;letter-spacing:-0.5px;'>TravelMate</h1>\n"
                + "  <p style='color:rgba(255,255,255,0.85);font-size:14px;margin:0;'>Votre compagnon de voyage</p>\n"
                + "</td></tr>\n"

                // Bandeau remboursement
                + "<tr><td style='background-color:#3b82f6;padding:16px 40px;text-align:center;'>\n"
                + "  <span style='color:#ffffff;font-size:16px;font-weight:600;'>↩️ Remboursement traité</span>\n"
                + "</td></tr>\n"

                // Corps
                + "<tr><td style='padding:36px 40px 20px;'>\n"
                + "  <p style='color:#94a3b8;font-size:15px;margin:0 0 24px;line-height:1.6;'>\n"
                + "    Bonjour,<br>Votre remboursement a bien été effectué. Le montant sera crédité sur votre compte dans un délai de 5 à 10 jours ouvrables.\n"
                + "  </p>\n"

                // Détails remboursement
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <span style='color:#3b82f6;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>💰 Détails du remboursement</span>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='margin-top:16px;'>\n"
                + "        <tr>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);'>\n"
                + "            <span style='color:#94a3b8;font-size:13px;'>Voyage</span>\n"
                + "          </td>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);text-align:right;'>\n"
                + "            <span style='color:#ffffff;font-size:14px;font-weight:500;'>" + nomVoyage + "</span>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);'>\n"
                + "            <span style='color:#94a3b8;font-size:13px;'>Transaction ID</span>\n"
                + "          </td>\n"
                + "          <td style='padding:10px 0;border-bottom:1px solid rgba(148,163,184,0.1);text-align:right;'>\n"
                + "            <span style='color:#ff8c42;font-size:12px;font-family:Consolas,monospace;'>" + transactionId + "</span>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr>\n"
                + "          <td style='padding:14px 0 0;'>\n"
                + "            <span style='color:#94a3b8;font-size:13px;'>Montant remboursé</span>\n"
                + "          </td>\n"
                + "          <td style='padding:14px 0 0;text-align:right;'>\n"
                + "            <span style='color:#ec4899;font-size:24px;font-weight:700;'>-" + montantFormate + "</span>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"
                + "</td></tr>\n"

                // Séparateur
                + "<tr><td style='padding:24px 40px 0;'>\n"
                + "  <hr style='border:none;border-top:1px solid rgba(148,163,184,0.15);margin:0;'>\n"
                + "</td></tr>\n"

                // Message
                + "<tr><td style='padding:24px 40px 16px;text-align:center;'>\n"
                + "  <p style='color:#94a3b8;font-size:14px;line-height:1.6;margin:0;'>\n"
                + "    Nous espérons vous revoir bientôt parmi nos voyageurs ! 🌍\n"
                + "  </p>\n"
                + "</td></tr>\n"

                // Footer
                + "<tr><td style='background-color:#0a0e27;padding:28px 40px;text-align:center;border-top:1px solid rgba(148,163,184,0.1);'>\n"
                + "  <p style='color:#64748b;font-size:12px;margin:0 0 8px;'>© 2025 TravelMate — Tous droits réservés</p>\n"
                + "  <p style='color:#4a5568;font-size:11px;margin:0;'>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>\n"
                + "</td></tr>\n"

                + "</table>\n"
                + "</td></tr></table>\n"
                + "</body></html>";
    }

    private String buildParticipationHtml(String nomParticipant, String nomVoyage,
                                            String destination, String dates, String role) {
        return "<!DOCTYPE html>\n"
                + "<html lang='fr'>\n"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>\n"
                + "<body style='margin:0;padding:0;background-color:#0a0e27;font-family:Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;'>\n"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#0a0e27;padding:40px 0;'>\n"
                + "<tr><td align='center'>\n"

                + "<table role='presentation' width='600' cellpadding='0' cellspacing='0' style='background-color:#111633;border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.5);'>\n"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#ff8c42,#ff6b4a);padding:40px 40px 30px;text-align:center;'>\n"
                + "  <div style='font-size:36px;margin-bottom:8px;'>✈️</div>\n"
                + "  <h1 style='color:#ffffff;font-size:28px;margin:0 0 4px;font-weight:700;letter-spacing:-0.5px;'>TravelMate</h1>\n"
                + "  <p style='color:rgba(255,255,255,0.85);font-size:14px;margin:0;'>Votre compagnon de voyage</p>\n"
                + "</td></tr>\n"

                // Bandeau succès
                + "<tr><td style='background-color:#10b981;padding:16px 40px;text-align:center;'>\n"
                + "  <span style='color:#ffffff;font-size:16px;font-weight:600;'>🎉 Inscription confirmée !</span>\n"
                + "</td></tr>\n"

                // Corps
                + "<tr><td style='padding:36px 40px 20px;'>\n"
                + "  <p style='color:#94a3b8;font-size:15px;margin:0 0 24px;line-height:1.6;'>\n"
                + "    Bonjour <strong style='color:#ffffff;'>" + nomParticipant + "</strong>,<br>\n"
                + "    Votre participation au voyage a été enregistrée avec succès ! Voici les détails :\n"
                + "  </p>\n"

                // Carte détails
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;margin-bottom:24px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0'>\n"
                + "        <tr>\n"
                + "          <td style='padding-bottom:16px;border-bottom:1px solid rgba(148,163,184,0.15);'>\n"
                + "            <span style='color:#ff8c42;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>🌍 Voyage</span>\n"
                + "            <h2 style='color:#ffffff;font-size:22px;margin:6px 0 0;font-weight:700;'>" + nomVoyage + "</h2>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr><td style='padding-top:16px;'>\n"
                + "          <table role='presentation' width='100%' cellpadding='0' cellspacing='0'>\n"
                + "            <tr>\n"
                + "              <td width='33%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>📍 Destination</span><br>\n"
                + "                <span style='color:#ffffff;font-size:15px;font-weight:500;'>" + destination + "</span>\n"
                + "              </td>\n"
                + "              <td width='33%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>📅 Dates</span><br>\n"
                + "                <span style='color:#ffffff;font-size:15px;font-weight:500;'>" + dates + "</span>\n"
                + "              </td>\n"
                + "              <td width='34%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>🏷️ Rôle</span><br>\n"
                + "                <span style='color:#ff8c42;font-size:15px;font-weight:600;'>" + role + "</span>\n"
                + "              </td>\n"
                + "            </tr>\n"
                + "          </table>\n"
                + "        </td></tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"

                // Checklist
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <span style='color:#10b981;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>📋 Prochaines étapes</span>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='margin-top:14px;'>\n"
                + "        <tr><td style='padding:6px 0;color:#94a3b8;font-size:14px;'>✅ Consultez les détails du voyage dans votre espace</td></tr>\n"
                + "        <tr><td style='padding:6px 0;color:#94a3b8;font-size:14px;'>✅ Préparez vos documents de voyage</td></tr>\n"
                + "        <tr><td style='padding:6px 0;color:#94a3b8;font-size:14px;'>✅ Restez informé des mises à jour</td></tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"

                + "</td></tr>\n"

                // Séparateur
                + "<tr><td style='padding:24px 40px 0;'>\n"
                + "  <hr style='border:none;border-top:1px solid rgba(148,163,184,0.15);margin:0;'>\n"
                + "</td></tr>\n"

                // Message
                + "<tr><td style='padding:24px 40px 16px;text-align:center;'>\n"
                + "  <p style='color:#94a3b8;font-size:14px;line-height:1.6;margin:0;'>\n"
                + "    Nous sommes ravis de vous compter parmi les voyageurs ! Bon voyage ! ✨\n"
                + "  </p>\n"
                + "</td></tr>\n"

                // Footer
                + "<tr><td style='background-color:#0a0e27;padding:28px 40px;text-align:center;border-top:1px solid rgba(148,163,184,0.1);'>\n"
                + "  <p style='color:#64748b;font-size:12px;margin:0 0 8px;'>© 2025 TravelMate — Tous droits réservés</p>\n"
                + "  <p style='color:#4a5568;font-size:11px;margin:0;'>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>\n"
                + "</td></tr>\n"

                + "</table>\n"
                + "</td></tr></table>\n"
                + "</body></html>";
    }

    private String buildAjoutParAdminHtml(String nomParticipant, String nomVoyage,
                                            String destination, String dates, String role) {
        return "<!DOCTYPE html>\n"
                + "<html lang='fr'>\n"
                + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>\n"
                + "<body style='margin:0;padding:0;background-color:#0a0e27;font-family:Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;'>\n"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#0a0e27;padding:40px 0;'>\n"
                + "<tr><td align='center'>\n"

                + "<table role='presentation' width='600' cellpadding='0' cellspacing='0' style='background-color:#111633;border-radius:16px;overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.5);'>\n"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#ff8c42,#ff6b4a);padding:40px 40px 30px;text-align:center;'>\n"
                + "  <div style='font-size:36px;margin-bottom:8px;'>✈️</div>\n"
                + "  <h1 style='color:#ffffff;font-size:28px;margin:0 0 4px;font-weight:700;letter-spacing:-0.5px;'>TravelMate</h1>\n"
                + "  <p style='color:rgba(255,255,255,0.85);font-size:14px;margin:0;'>Votre compagnon de voyage</p>\n"
                + "</td></tr>\n"

                // Bandeau info (bleu au lieu de vert)
                + "<tr><td style='background-color:#3b82f6;padding:16px 40px;text-align:center;'>\n"
                + "  <span style='color:#ffffff;font-size:16px;font-weight:600;'>📌 Vous avez été ajouté(e) à un voyage !</span>\n"
                + "</td></tr>\n"

                // Corps
                + "<tr><td style='padding:36px 40px 20px;'>\n"
                + "  <p style='color:#94a3b8;font-size:15px;margin:0 0 24px;line-height:1.6;'>\n"
                + "    Bonjour <strong style='color:#ffffff;'>" + nomParticipant + "</strong>,<br><br>\n"
                + "    L'équipe <strong style='color:#ff8c42;'>TravelMate</strong> vous informe qu'un administrateur "
                + "vous a ajouté(e) en tant que participant au voyage suivant :\n"
                + "  </p>\n"

                // Carte détails
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;margin-bottom:24px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0'>\n"
                + "        <tr>\n"
                + "          <td style='padding-bottom:16px;border-bottom:1px solid rgba(148,163,184,0.15);'>\n"
                + "            <span style='color:#ff8c42;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>🌍 Voyage</span>\n"
                + "            <h2 style='color:#ffffff;font-size:22px;margin:6px 0 0;font-weight:700;'>" + nomVoyage + "</h2>\n"
                + "          </td>\n"
                + "        </tr>\n"
                + "        <tr><td style='padding-top:16px;'>\n"
                + "          <table role='presentation' width='100%' cellpadding='0' cellspacing='0'>\n"
                + "            <tr>\n"
                + "              <td width='33%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>📍 Destination</span><br>\n"
                + "                <span style='color:#ffffff;font-size:15px;font-weight:500;'>" + destination + "</span>\n"
                + "              </td>\n"
                + "              <td width='33%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>📅 Dates</span><br>\n"
                + "                <span style='color:#ffffff;font-size:15px;font-weight:500;'>" + dates + "</span>\n"
                + "              </td>\n"
                + "              <td width='34%' style='padding:8px 0;'>\n"
                + "                <span style='color:#94a3b8;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;'>🏷️ Votre rôle</span><br>\n"
                + "                <span style='color:#ff8c42;font-size:15px;font-weight:600;'>" + role + "</span>\n"
                + "              </td>\n"
                + "            </tr>\n"
                + "          </table>\n"
                + "        </td></tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"

                // Info box
                + "  <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background-color:#1e2749;border-radius:12px;'>\n"
                + "    <tr><td style='padding:24px;'>\n"
                + "      <span style='color:#3b82f6;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;'>ℹ️ Informations</span>\n"
                + "      <table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='margin-top:14px;'>\n"
                + "        <tr><td style='padding:6px 0;color:#94a3b8;font-size:14px;'>📋 Cet ajout a été effectué par l'administration TravelMate</td></tr>\n"
                + "        <tr><td style='padding:6px 0;color:#94a3b8;font-size:14px;'>📄 Aucune action de votre part n'est requise</td></tr>\n"
                + "        <tr><td style='padding:6px 0;color:#94a3b8;font-size:14px;'>📧 Pour toute question, contactez l'équipe TravelMate</td></tr>\n"
                + "      </table>\n"
                + "    </td></tr>\n"
                + "  </table>\n"

                + "</td></tr>\n"

                // Séparateur
                + "<tr><td style='padding:24px 40px 0;'>\n"
                + "  <hr style='border:none;border-top:1px solid rgba(148,163,184,0.15);margin:0;'>\n"
                + "</td></tr>\n"

                // Message
                + "<tr><td style='padding:24px 40px 16px;text-align:center;'>\n"
                + "  <p style='color:#94a3b8;font-size:14px;line-height:1.6;margin:0;'>\n"
                + "    Nous sommes ravis de vous compter parmi les voyageurs !<br>Préparez-vous, l'aventure commence bientôt ! 🌟\n"
                + "  </p>\n"
                + "</td></tr>\n"

                // Footer
                + "<tr><td style='background-color:#0a0e27;padding:28px 40px;text-align:center;border-top:1px solid rgba(148,163,184,0.1);'>\n"
                + "  <p style='color:#64748b;font-size:12px;margin:0 0 8px;'>© 2025 TravelMate — Tous droits réservés</p>\n"
                + "  <p style='color:#4a5568;font-size:11px;margin:0;'>Cet email a été envoyé automatiquement. Merci de ne pas y répondre.</p>\n"
                + "</td></tr>\n"

                + "</table>\n"
                + "</td></tr></table>\n"
                + "</body></html>";
    }
}
