package tn.enicarthage.speedenicar_projet.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service pour l'envoi d'emails (Simple et HTML).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Envoie un email HTML basé sur un template Thymeleaf.
     * * @param to recipient email
     * @param subject email subject
     * @param templateName nom du template (ex: "emails/consultation-ready")
     * @param variables map des variables à injecter dans le HTML
     */
    @Async
    public void sendHtml(String to,
                         String subject,
                         String templateName,
                         Map<String, Object> variables) {
        try {
            log.debug("[EmailService] Préparation de l'email pour {} avec le template : {}", to, templateName);

            // 1. Construire le contexte Thymeleaf (Données pour le HTML)
            Context ctx = new Context();
            ctx.setVariables(variables);

            /* IMPORTANT : On ne met PAS "templates/" ici.
               Spring Boot est déjà configuré pour chercher dans src/main/resources/templates/
            */
            String html = templateEngine.process(templateName, ctx);

            // 2. Créer le message MIME (Multi-part pour le HTML)
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true indique que c'est du contenu HTML

            // 3. Envoyer l'email
            mailSender.send(mime);
            log.info("[EmailService] Succès : Email '{}' envoyé à {}", subject, to);

        } catch (MessagingException e) {
            log.error("[EmailService] Erreur technique lors de l'envoi à {} : {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("[EmailService] Erreur inattendue lors de la génération de l'email pour {} : {}", to, e.getMessage());
            // Log de la stacktrace complète en mode debug si nécessaire
            log.debug("Stacktrace : ", e);
        }
    }
}