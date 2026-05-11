package com.indux.core.domain.service.occurrence;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.Form;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleBadRequest;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractOccurrenceService<T extends Form<T>> implements OccurrenceService<T> {
    protected final StorageService storageService;
    protected final NotificationService notification;
    protected final CustomMailSender mailSender;
    protected final UserService userService;
    protected final MongoTemplate mongo;

    protected AbstractOccurrenceService(StorageService storageService, NotificationService notification, CustomMailSender mailSender, UserService userService, MongoTemplate mongo) {
        this.storageService = storageService;
        this.notification = notification;
        this.mailSender = mailSender;
        this.userService = userService;
        this.mongo = mongo;
    }



    protected void checkStatus(T form) {
        if (form.getStatus() == DocumentStatus.APROVADO || form.getStatus() == DocumentStatus.REJEITADO)
            throw new ModuleBadRequest("Não é possível modificar uma solicitação já finalizada.");
    }

    protected FileMetadata storeFile(MultipartFile file, String path, String fileName, Integer etapa) {
        String mimeType = file.getContentType();
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains("."))
            ext = original.substring(original.lastIndexOf('.') + 1);

        Path store = storageService.store(file, path, fileName);
        String uri = storageService.getRootLocation().relativize(store).toString();
        uri = uri.replace("\\", "/");

        return new FileMetadata(uri, ext, mimeType, etapa);
    }

    protected void batchUpdateStatusGlobal(List<String> ids, StepLog log, DocumentStatus novaStatus, Class<T> entity) {
        Query q = new Query(Criteria.where("_id").in(ids));
        Update update = new Update()
                .set("status", novaStatus)
                .set("finalizado_em", new Date())
                .set("etapa_atual", 0)
                .push("etapa_log", log)
                .set("situacao", DocumentStatus.FINALIZADO);

        mongo.updateMulti(q, update, entity);
    }

    protected StepLog createStepLog(UUID userId, String actionName, Integer step, String observation, String groupId) {
        return StepLog.builder()
                .id(UUID.randomUUID())
                .name(actionName)
                .created_at(new Date())
                .final_at(new Date())
                .user(userId)
                .step(step)
                .observation(observation)
                .group(groupId != null ? UUID.fromString(groupId) : null)
                .build();
    }

    protected void validateStepBackwards(T occurrence) {
        if (occurrence.getCurrentStep() <= 1) {
            throw new ModuleBadRequest("Não é possível voltar para antes da primeira etapa.");
        }
    }

    protected void finalizeOccurrence(T occurrence) {
        occurrence.setCurrentStep(0);
        occurrence.setFinal_date(new Date());
        occurrence.setStatus(DocumentStatus.APROVADO);
    }

    protected void rejectOccurrence(T occurrence) {
        occurrence.setCurrentStep(0);
        occurrence.setFinal_date(new Date());
        occurrence.setStatus(DocumentStatus.REJEITADO);
    }

    protected void addLog(T occurrence, StepLog log) {
        if (occurrence.getStepLog() == null) {
            occurrence.setStepLog(new ArrayList<>());
        }
        occurrence.getStepLog().add(log);
    }

    protected abstract T getOccurrenceInternal(String id);

    protected abstract Form<T> saveOccurrence(T occurrence);

    protected void notifyNextStep(Modulo module, Integer branch, Integer currentStep) {
        notifyNextStep(module, branch, currentStep, null, null);
    }

    protected void notifyNextStep(Modulo module, Integer branch, Integer currentStep, String occurrenceCode, String moduleName) {
        int nextStep = currentStep + 1;
        if (module.getStepsQuantity() < nextStep)
            throw new ModuleFailure("Não é possível notificar uma etapa inexistente. Não há mais etapas a serem seguidas.");
        
        module.getPermissoes().stream()
                .filter(p -> p.getStepsAllowed().contains(nextStep))
                .filter(p -> p.getRegionais().contains(branch) || p.getRegionais().contains(0))
                .forEach(permissao -> {
                    userService.getUserById(String.valueOf(permissao.getResponsable()))
                            .ifPresent(dest -> {
                                Notification notificationValue = new Notification();
                                notificationValue.setTo(dest);
                                notificationValue.setTemplateName(null);
                                
                                // Assunto personalizado com informações da ocorrência
                                String subject = buildNotificationSubject("Nova ocorrência aguardando sua análise", occurrenceCode, moduleName);
                                notificationValue.setSubject(subject);
                                
                                // Mensagem personalizada com informações da ocorrência
                                String message = buildNotificationMessage(
                                    "Uma nova ocorrência foi encaminhada para a sua análise", 
                                    occurrenceCode, 
                                    moduleName,
                                    "Por favor, acesse o sistema para revisar e tomar as ações necessárias dentro do prazo estipulado."
                                );
                                notificationValue.setMessage(message);
                                
                                notificationValue.setType(NotificationType.EMAIL);
                                notification.send(notificationValue);
                            });
                });
    }

    protected void notifyApplicant(StepLog log, String title, String subject) {
        notifyApplicant(log, title, subject, null, null);
    }

    protected void notifyApplicant(StepLog log, String title, String subject, String occurrenceCode, String moduleName) {
        SimpleUser user = userService.getUserById(String.valueOf(log.getUser())).orElseThrow(() -> new NotFoundEmployee("Usuário a ser notificado não foi encontrado."));
        
        // Título personalizado com informações da ocorrência
        String personalizedTitle = buildNotificationSubject(title, occurrenceCode, moduleName);
        
        // Assunto personalizado com informações da ocorrência
        String personalizedSubject = buildNotificationMessage(subject, occurrenceCode, moduleName, null);
        
        mailSender.sendGenericEmail(user.getEmail(), personalizedTitle, personalizedSubject, null);
    }

    protected void notifyAnyStep(Modulo module, Integer branch, Integer step, String title, String body) {
        notifyAnyStep(module, branch, step, title, body, null, null);
    }

    protected void notifyAnyStep(Modulo module, Integer branch, Integer step, String title, String body, String occurrenceCode, String moduleName) {
        if (module.getStepsQuantity() < step)
            throw new ModuleFailure("Não é possível notificar uma etapa inexistente. Não há mais etapas a serem seguidas.");
        
        module.getPermissoes().stream()
                .filter(p -> p.getStepsAllowed().contains(step))
                .filter(p -> p.getRegionais().contains(branch) || p.getRegionais().contains(0))
                .findFirst()
                .flatMap(p -> userService.getUserById(String.valueOf(p.getResponsable())))
                .ifPresent(dest -> {
                    Notification notificationValue = new Notification();
                    notificationValue.setTo(dest);
                    notificationValue.setTemplateName(null);
                    
                    // Título personalizado com informações da ocorrência
                    String personalizedTitle = buildNotificationSubject(title, occurrenceCode, moduleName);
                    notificationValue.setSubject(personalizedTitle);
                    
                    // Mensagem personalizada com informações da ocorrência
                    String personalizedBody = buildNotificationMessage(body, occurrenceCode, moduleName, null);
                    notificationValue.setMessage(personalizedBody);
                    
                    notificationValue.setType(NotificationType.EMAIL);
                    notification.send(notificationValue);
                });
    }

    protected void notifyPreviousStep(StepLog log, Integer currentStep) {
        notifyPreviousStep(log, currentStep, null, null);
    }

    protected void notifyPreviousStep(StepLog log, Integer currentStep, String occurrenceCode, String moduleName) {
        int previousStep = currentStep - 1;
        if (currentStep <= 1)
            throw new ModuleFailure("Não é possível notificar uma etapa inexistente. Não há mais etapas a serem seguidas.");

        SimpleUser user = userService.getUserById(String.valueOf(log.getUser())).orElseThrow(() -> new NotFoundEmployee("Usuário a ser notificado não foi encontrado."));

        Notification notificationValue = getNotification(user, occurrenceCode, moduleName);
        notification.send(notificationValue);
    }

    private static Notification getNotification(SimpleUser user) {
        return getNotification(user, null, null);
    }

    private static Notification getNotification(SimpleUser user, String occurrenceCode, String moduleName) {
        Notification notificationValue = new Notification();
        notificationValue.setTo(user);
        notificationValue.setTemplateName(null);
        
        // Assunto personalizado com informações da ocorrência
        String subject = buildNotificationSubject("Uma ocorrência necessita de sua atenção", occurrenceCode, moduleName);
        notificationValue.setSubject(subject);
        
        // Mensagem personalizada com informações da ocorrência
        String message = buildNotificationMessage(
            "Uma nova ocorrência foi encaminhada para a sua análise", 
            occurrenceCode, 
            moduleName,
            "Por favor, acesse o sistema para revisar e tomar as ações necessárias dentro do prazo estipulado."
        );
        notificationValue.setMessage(message);
        
        notificationValue.setType(NotificationType.EMAIL);
        return notificationValue;
    }

    /**
     * Constrói o assunto personalizado para notificações incluindo código da ocorrência e módulo
     */
    private static String buildNotificationSubject(String baseSubject, String occurrenceCode, String moduleName) {
        StringBuilder subject = new StringBuilder(baseSubject);
        
        if (occurrenceCode != null && !occurrenceCode.trim().isEmpty()) {
            subject.append(" - Ocorrência nº ").append(occurrenceCode);
        }
        
        if (moduleName != null && !moduleName.trim().isEmpty()) {
            subject.append(" (").append(moduleName).append(")");
        }
        
        return subject.toString();
    }

    /**
     * Constrói a mensagem personalizada para notificações incluindo código da ocorrência e módulo
     */
    private static String buildNotificationMessage(String baseMessage, String occurrenceCode, String moduleName, String additionalInfo) {
        StringBuilder message = new StringBuilder();
        
        // Adiciona informações da ocorrência no início
        if (occurrenceCode != null && !occurrenceCode.trim().isEmpty()) {
            message.append("<strong>Ocorrência:</strong> ").append(occurrenceCode).append("<br>");
        }
        
        if (moduleName != null && !moduleName.trim().isEmpty()) {
            message.append("<strong>Módulo:</strong> ").append(moduleName).append("<br>");
        }
        
        if (occurrenceCode != null || moduleName != null) {
            message.append("<br>");
        }
        
        // Adiciona a mensagem principal
        message.append("        ").append(baseMessage).append("<br><br>\n");
        
        // Adiciona informações adicionais se fornecidas
        if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
            message.append("\n        ").append(additionalInfo).append("<br><br>\n");
        }
        
        return message.toString();
    }

    protected String generateCodOccurrence(String prefix) {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String sufixo = Long.toString(Long.parseLong(hora), 36).toUpperCase();

        return prefix + data + "-" + sufixo;
    }

    protected abstract void updateOccurrenceAfterReview(T occurrence, Object dto, String userId, boolean isEdit);

    private static final Map<DocumentStatus, Integer> STATUS_ORDINAL = Map.of(
            DocumentStatus.PENDENTE, 1,
            DocumentStatus.REVISÃO, 2,
            DocumentStatus.ABERTO, 3,
            DocumentStatus.REJEITADO, 4,
            DocumentStatus.APROVADO, 5,
            DocumentStatus.REVIEWED, 6
    );
    private static final Map<DocumentStatus, Integer> SITUACAO_ORDINAL = Map.of(
            DocumentStatus.ATRASADO, 1,
            DocumentStatus.ANDAMENTO, 2,
            DocumentStatus.FINALIZADO, 3
    );

    protected void setOrderFields(T o) {
        o.setStatusOrder(
                STATUS_ORDINAL.getOrDefault(o.getStatus(), Integer.MAX_VALUE)
        );
        o.setSituacaoOrder(
                SITUACAO_ORDINAL.getOrDefault(o.getSituacao(), Integer.MAX_VALUE)
        );
    }

    protected  <T> void updateFieldIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    protected <S, T> void updateListIfNotNull(List<S> sourceList, Function<S, T> mapper, Consumer<List<T>> listSetter) {
        if (sourceList != null) {
            listSetter.accept(sourceList.stream().map(mapper).toList());
        }
    }

}