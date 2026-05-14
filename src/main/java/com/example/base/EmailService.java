package com.example.base;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Yksinkertainen tekstiviesti
    public void sendSimpleEmail(String to, String subject,
                                String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@eventapp.com");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println(
                    "Sähköpostin lähetys epäonnistui: "
                            + e.getMessage());
        }
    }

    // HTML-viesti
    public void sendHtmlEmail(String to, String subject,
                              String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@eventapp.com");
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println(
                    "HTML-sähköpostin lähetys epäonnistui: "
                            + e.getMessage());
        }
    }

    // Ilmoitus ylläpitäjälle uudesta käyttäjästä
    public void sendNewUserNotification(String adminEmail,
                                        String newUsername,
                                        String newEmail) {
        try {
            String subject = "Uusi kayttaja rekisteroityi - EventApp";
            String html = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #6C63FF;">Uusi kayttaja rekisteroityi</h2>
                <table style="border-collapse: collapse; width: 100%%;">
                    <tr>
                        <td style="padding: 8px; border: 1px solid #ddd; background: #f5f5f5;">
                            <strong>Kayttajanimi:</strong>
                        </td>
                        <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; border: 1px solid #ddd; background: #f5f5f5;">
                            <strong>Sahkoposti:</strong>
                        </td>
                        <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                    </tr>
                </table>
                <p style="color: #666; margin-top: 20px;">
                    Tama on automaattinen viesti EventApp:sta.
                </p>
            </body>
            </html>
            """.formatted(newUsername, newEmail);

            sendHtmlEmail(adminEmail, subject, html);

        } catch (Exception e) {
            System.err.println("Admin-ilmoituksen lahettaminen epaonnistui: "
                    + e.getMessage());
            // Ei kaadeta sovellusta sahkopostivirheeseen
        }
    }

    // Salasanan vaihto sähköpostilla
    public void sendPasswordResetEmail(String to,
                                       String resetLink) {
        String subject = "Salasanan vaihto – EventApp";
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif;
                         padding: 20px;">
                <h2 style="color: #6C63FF;">
                    Salasanan vaihto
                </h2>
                <p>Olet pyytänyt salasanan vaihtoa
                   EventApp-tilillesi.</p>
                <p>Klikkaa alla olevaa linkkiä
                   vaihtaaksesi salasanasi:</p>
                <a href="%s"
                   style="background: #6C63FF;
                          color: white;
                          padding: 12px 24px;
                          text-decoration: none;
                          border-radius: 6px;
                          display: inline-block;
                          margin: 16px 0;">
                    Vaihda salasana
                </a>
                <p style="color: #666;">
                    Linkki on voimassa 24 tuntia.
                    Jos et pyytänyt salasanan vaihtoa,
                    voit ohittaa tämän viestin.
                </p>
            </body>
            </html>
            """.formatted(resetLink);

        sendHtmlEmail(to, subject, html);
    }
}