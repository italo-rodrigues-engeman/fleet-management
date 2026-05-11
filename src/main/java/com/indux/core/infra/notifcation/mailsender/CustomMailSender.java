package com.indux.core.infra.notifcation.mailsender;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.filestorage.StorageService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomMailSender {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String from;
    private final StorageService storageService;
    
    // Semáforo para limitar conexões simultâneas de e-mail (máximo 3 conexões)
    private final Semaphore emailSemaphore = new Semaphore(3);

    public CustomMailSender(JavaMailSender mailSender,
                            TemplateEngine templateEngine,
                            @Value("${spring.mail.username}") String from, StorageService storageService) {
        this.mailSender = Objects.requireNonNull(mailSender);
        this.templateEngine = Objects.requireNonNull(templateEngine);
        this.from = Objects.requireNonNull(from);
        this.storageService = storageService;
    }

    /**
     * Método genérico para renderizar um template Thymeleaf e enviar como e-mail HTML.
     * @param to           destinatário
     * @param subject      assunto do e-mail
     * @param templateName nome do arquivo em /templates/email (sem extensão)
     * @param variables    variáveis Thymeleaf
     */
    @Async
    public void sendTemplateEmail(String to,
                                  String subject,
                                  String templateName,
                                  Map<String, Object> variables,
                                  Map<String, FileMetadata> attachments) {
        try {
            // Adquire permissão para enviar e-mail (controle de concorrência)
            emailSemaphore.acquire();
            
            Context ctx = new Context();
            ctx.setVariable("currentYear", Year.now().getValue());
            variables.forEach(ctx::setVariable);

            String html = templateEngine.process("email/" + templateName, ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            helper.addInline("logo",
                    new ClassPathResource("/templates/images/kogni-logo.png"));
            if (attachments != null) {
                for (Map.Entry<String, FileMetadata> entry : attachments.entrySet()) {
                    FileMetadata metadata = entry.getValue();

                    helper.addAttachment(
                            entry.getKey() + "." + metadata.getExtension(),
                            () -> storageService.loadAsStream(metadata.getPath()),
                            metadata.getMimeType()
                    );
                }
            }

            mailSender.send(message);
            log.info("E-mail '{}' enviado para {}", subject, to);

        } catch (MessagingException ex) {
            log.error("Falha ao enviar e-mail '{}' para {}: {}", subject, to, ex.getMessage());
        } catch (InterruptedException ex) {
            log.error("Thread interrompida ao aguardar envio de e-mail '{}' para {}: {}", subject, to, ex.getMessage());
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
        } finally {
            // Libera a permissão para permitir outros envios
            emailSemaphore.release();
        }
    }

    // ——— E-mails específicos ——— //
    @Async
    public void sendWelcomeEmail(String to, String name, String code, String expiration) {
        Map<String, Object> vars = Map.of(
                "title", "Bem-vindo ao Kogni",
                "name", name,
                "code", code,
                "expirationTime", expiration
        );
        sendTemplateEmail(to, vars.get("title").toString(), "welcome_email", vars, null);
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String code, String expiration) {
        Map<String, Object> vars = Map.of(
                "title", "Redefinição de Senha",
                "name", name,
                "code", code,
                "expirationTime", expiration
        );
        sendTemplateEmail(to, vars.get("title").toString(), "forget_password_email", vars, null);
    }

    @Async
    public void sendGenericEmail(String to, String subject, String bodyHtml, @Nullable Map<String, FileMetadata> attachments) {
        Map<String, Object> vars = Map.of(
                "title", subject,
                "body", bodyHtml
        );
        sendTemplateEmail(to, subject, "generic_email", vars, attachments);
    }

    @Async
    public void sendMultipleEmails(List<SimpleUser> users, String subject, @Nullable Map<String, FileMetadata> attachments) {

        for (SimpleUser recipient : users) {
            String email = recipient.getEmail();

            sendTemplateEmail(email, subject, "comunicado", Map.of(), null);
            
            // Pequeno delay entre envios para reduzir pressão no servidor SMTP
            try {
                Thread.sleep(150); // 200ms de delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrompida durante delay entre envios de e-mail");
                break;
            }
        }

    }

    private static String toTitleCase(String input) {
        if (input == null || input.isBlank()) return input;
        return Arrays.stream(input.toLowerCase().split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
