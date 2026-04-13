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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender  mailSender;
    private final TemplateEngine  templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendHtml(String to,
                         String subject,
                         String templateName,
                         Map<String, Object> variables) {
        try {
            // 1. Construire le HTML via Thymeleaf
            Context ctx = new Context();
            ctx.setVariables(variables);
            String html = templateEngine.process("emails/" + templateName, ctx);

            // 2. Créer le message MIME
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);  // true = HTML

            // 3. Envoyer
            mailSender.send(mime);
            log.info("[EmailService] Email '{}' envoyé à {}", subject, to);

        } catch (MessagingException e) {
            log.error("[EmailService] Échec envoi à {} — sujet '{}' : {}", to, subject, e.getMessage());
        } catch (Exception e) {
            log.error("[EmailService] Erreur inattendue pour {} : {}", to, e.getMessage(), e);
        }
    }
}