package com.supplychainx.audit.service;

import com.supplychainx.audit.entity.StockAlert;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service pour envoyer des emails d'alerte
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@supplychainx.com}")
    private String fromEmail;

    @Value("${app.alert.email.to:admin@supplychainx.com}")
    private String alertEmailTo;

    @Value("${app.alert.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Envoyer un email d'alerte de stock
     */
    public void sendStockAlert(StockAlert alert) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Alert email would have been sent for: {}", alert.getEntityName());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(alertEmailTo);
            helper.setSubject(buildSubject(alert));
            helper.setText(buildEmailBody(alert), true);

            mailSender.send(message);
            log.info("Stock alert email sent successfully for {} {}", 
                    alert.getEntityType(), alert.getEntityId());

        } catch (MessagingException e) {
            log.error("Failed to send alert email for {} {}: {}", 
                    alert.getEntityType(), alert.getEntityId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send alert email", e);
        }
    }

    /**
     * Construire le sujet de l'email
     */
    private String buildSubject(StockAlert alert) {
        String priority = alert.isCritical() ? "[URGENT] " : "";
        return String.format("%sSupplyChainX Alert - %s: %s",
                priority,
                alert.getAlertType(),
                alert.getEntityName());
    }

    /**
     * Construire le corps de l'email en HTML
     */
    private String buildEmailBody(StockAlert alert) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String priorityStyle = alert.isCritical() ? "color: red; font-weight: bold;" : "color: orange;";

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #f4f4f4; padding: 20px; text-align: center; }
                        .alert-critical { color: #dc3545; }
                        .alert-warning { color: #ffc107; }
                        .content { padding: 20px; background-color: #ffffff; }
                        .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                        table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
                        th { background-color: #f4f4f4; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2 style="%s">⚠️ Alerte de Stock - SupplyChainX</h2>
                        </div>
                        <div class="content">
                            <h3>Détails de l'alerte</h3>
                            <table>
                                <tr>
                                    <th>Type d'alerte</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Type d'entité</th>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <th>Nom</th>
                                    <td><strong>%s</strong></td>
                                </tr>
                                <tr>
                                    <th>Stock actuel</th>
                                    <td>%d</td>
                                </tr>
                                <tr>
                                    <th>Stock minimum</th>
                                    <td>%d</td>
                                </tr>
                                <tr>
                                    <th>Date de création</th>
                                    <td>%s</td>
                                </tr>
                            </table>
                            
                            <h3>Message</h3>
                            <p>%s</p>
                            
                            %s
                        </div>
                        <div class="footer">
                            <p>Cet email a été généré automatiquement par SupplyChainX.</p>
                            <p>Merci de ne pas répondre à cet email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                priorityStyle,
                alert.getAlertType(),
                alert.getEntityType(),
                alert.getEntityName(),
                alert.getCurrentStock() != null ? alert.getCurrentStock() : 0,
                alert.getMinimumStock() != null ? alert.getMinimumStock() : 0,
                alert.getCreatedAt().format(formatter),
                alert.getMessage(),
                alert.isCritical() ?
                        "<div style='background-color: #fff3cd; border-left: 4px solid #dc3545; padding: 10px; margin: 20px 0;'>" +
                        "<strong>⚠️ ATTENTION:</strong> Cette alerte est critique et nécessite une action immédiate!" +
                        "</div>" : ""
        );
    }

    /**
     * Envoyer un email de test
     */
    public void sendTestEmail(String to) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Test email would have been sent to: {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("SupplyChainX - Test Email");
            helper.setText("<h1>Email Configuration Test</h1><p>If you receive this email, your email configuration is working correctly.</p>", true);

            mailSender.send(message);
            log.info("Test email sent successfully to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send test email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send test email", e);
        }
    }
}
