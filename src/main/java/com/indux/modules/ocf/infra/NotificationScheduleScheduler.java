package com.indux.modules.ocf.infra;

import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.user.UserService;
import com.indux.modules.ocf.application.service.OcfNotificationScheduleConfigService;
import com.indux.modules.ocf.domain.model.NotificationSchedule;
import com.indux.modules.ocf.domain.model.OcorrenciaFF;
import com.indux.modules.ocf.domain.repository.NotificationScheduleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class NotificationScheduleScheduler {
    
    
    private final NotificationScheduleRepository notificationScheduleRepository;
    private final MongoTemplate mongoTemplate;
    private final ModuleManagementService moduleService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final com.indux.core.domain.repository.generic.RegionalRepository regionalRepository;
    private final OcfNotificationScheduleConfigService configService;
    
    @Value("${module.ocf.id}")
    private String FINANCE_MODULE_ID;
    
    public NotificationScheduleScheduler(NotificationScheduleRepository notificationScheduleRepository,
                                       MongoTemplate mongoTemplate,
                                       ModuleManagementService moduleService,
                                       NotificationService notificationService,
                                       UserService userService,
                                       com.indux.core.domain.repository.generic.RegionalRepository regionalRepository,
                                       OcfNotificationScheduleConfigService configService) {
        this.notificationScheduleRepository = notificationScheduleRepository;
        this.mongoTemplate = mongoTemplate;
        this.moduleService = moduleService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.regionalRepository = regionalRepository;
        this.configService = configService;
    }
    
    /**
     * A cada 1 minuto:
     * Processa todos os agendamentos de notificação ativos e envia notificações
     * para ocorrências que excederam o tempo limite configurado
     */
    @Scheduled(cron = "0 * * * * *") // Executa a cada minuto
    public void processNotificationSchedules() {
        try {
            // Verificar se as notificações estão habilitadas por configuração
            if (!configService.isNotificationEnabled()) {
                return;
            }
            
            // Processar períodos de inatividade que acabaram
            processPendingInactivityStates();
            
            // Buscar todos os agendamentos ativos
            List<NotificationSchedule> agendamentos = notificationScheduleRepository.findByAtivoTrue();
            
            if (agendamentos.isEmpty()) {
                return;
            }
            
            // Processar cada agendamento
            for (NotificationSchedule agendamento : agendamentos) {
                processSchedule(agendamento);
            }
            
        } catch (Exception e) {
            // Erro silencioso - não falha a operação principal
        }
    }
    
    /**
     * Processa períodos de inatividade que acabaram e envia notificações acumuladas
     */
    private void processPendingInactivityStates() {
        try {
            List<com.indux.modules.ocf.domain.model.NotificationInactivityState> pendingStates = 
                    configService.getPendingInactivityStates();
            
            for (com.indux.modules.ocf.domain.model.NotificationInactivityState state : pendingStates) {
                if (state.getFimInatividade() != null) {
                    // Período de inatividade acabou, processar notificações acumuladas
                    processAccumulatedNotifications(state);
                    configService.markInactivityStateAsProcessed(state.getId());
                }
            }
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    /**
     * Processa notificações que foram acumuladas durante período de inatividade
     */
    private void processAccumulatedNotifications(com.indux.modules.ocf.domain.model.NotificationInactivityState state) {
        try {
            // Buscar todos os agendamentos ativos
            List<NotificationSchedule> agendamentos = notificationScheduleRepository.findByAtivoTrue();
            
            for (NotificationSchedule agendamento : agendamentos) {
                // Buscar ocorrências que ficaram pendentes durante a inatividade
                List<OcorrenciaFF> ocorrenciasPendentes = findOccurrencesForSchedule(agendamento);
                
                for (OcorrenciaFF ocorrencia : ocorrenciasPendentes) {
                    // Verificar se a ocorrência ficou muito tempo parada durante a inatividade
                    if (shouldSendAccumulatedNotification(ocorrencia, agendamento, state)) {
                        sendAccumulatedNotification(ocorrencia, agendamento, state);
                    }
                }
            }
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    /**
     * Verifica se deve enviar notificação acumulada
     */
    private boolean shouldSendAccumulatedNotification(OcorrenciaFF ocorrencia, NotificationSchedule agendamento, 
                                                     com.indux.modules.ocf.domain.model.NotificationInactivityState state) {
        try {
            // Verificar se a ocorrência já foi notificada por este agendamento
            if (agendamento.getOcorrenciasNotificadas() != null && 
                agendamento.getOcorrenciasNotificadas().contains(ocorrencia.getId())) {
                return false;
            }
            
            // Verificar se a ocorrência estava parada antes do início da inatividade
            long tempoParadoAntesInatividade = calculateTimeStoppedBeforeInactivity(ocorrencia, state.getInicioInatividade());
            long limiteMinutos = Math.round(agendamento.getTempoLimiteHoras() * 60.0);
            
            // Se já estava no limite antes da inatividade, deve notificar
            return tempoParadoAntesInatividade >= limiteMinutos;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calcula quanto tempo a ocorrência estava parada antes do início da inatividade
     */
    private long calculateTimeStoppedBeforeInactivity(OcorrenciaFF ocorrencia, LocalDateTime inicioInatividade) {
        try {
            if (ocorrencia.getStepLog() == null || ocorrencia.getStepLog().isEmpty()) {
                return calculateTimeFromCreatedAtBeforeInactivity(ocorrencia, inicioInatividade);
            }
            
            int etapaAtual = ocorrencia.getCurrentStep();
            Date ultimaAtualizacaoEtapa = findLastStepLogInCurrentStep(ocorrencia.getStepLog(), etapaAtual);
            
            if (ultimaAtualizacaoEtapa == null) {
                return calculateTimeFromCreatedAtBeforeInactivity(ocorrencia, inicioInatividade);
            }
            
            LocalDateTime ultimaAtualizacao = ultimaAtualizacaoEtapa.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            
            // Calcular tempo até o início da inatividade
            return java.time.Duration.between(ultimaAtualizacao, inicioInatividade).toMinutes();
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Fallback: calcula tempo baseado no created_at até o início da inatividade
     */
    private long calculateTimeFromCreatedAtBeforeInactivity(OcorrenciaFF ocorrencia, LocalDateTime inicioInatividade) {
        try {
            Date createdDate = ocorrencia.getCreated_at();
            LocalDateTime criadaEm = createdDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            
            return java.time.Duration.between(criadaEm, inicioInatividade).toMinutes();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Envia notificação acumulada com informação sobre o período de inatividade
     */
    private void sendAccumulatedNotification(OcorrenciaFF ocorrencia, NotificationSchedule agendamento, 
                                           com.indux.modules.ocf.domain.model.NotificationInactivityState state) {
        try {
            // Enviar notificação para cada canal configurado
            for (String canal : agendamento.getCanais()) {
                sendAccumulatedNotificationForChannel(ocorrencia, agendamento, canal, state);
            }
            
            // Marcar a ocorrência como notificada
            markOccurrenceAsNotified(agendamento, ocorrencia.getId());
            
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    /**
     * Envia notificação acumulada para um canal específico
     */
    private void sendAccumulatedNotificationForChannel(OcorrenciaFF ocorrencia, NotificationSchedule agendamento, 
                                                      String canal, com.indux.modules.ocf.domain.model.NotificationInactivityState state) {
        try {
            // Enviar notificação para cada usuário configurado
            for (String userId : agendamento.getUserIds()) {
                userService.getUserById(userId).ifPresent(user -> {
                    String occurrenceCode = String.valueOf(ocorrencia.getCodeID());
                    
                    // Buscar nome real do módulo
                    String moduleName = "N/A";
                    try {
                        UUID moduleUUID = UUID.fromString(FINANCE_MODULE_ID);
                        var moduleInfo = moduleService.getModuleByID(moduleUUID);
                        if (moduleInfo != null && moduleInfo.getName() != null) {
                            moduleName = moduleInfo.getName();
                        }
                    } catch (Exception e) {
                        moduleName = "Ocorrência Financeira";
                    }
                    
                    // Calcular tempo total de atraso incluindo período de inatividade
                    long tempoTotalAtraso = calculateTotalDelayTime(ocorrencia, agendamento, state);
                    String tempoAtrasoFormatado = formatDelayTime(tempoTotalAtraso);
                    
                    // Obter dados do colaborador
                    String nomeColaborador = ocorrencia.getColaborador() != null ? 
                        ocorrencia.getColaborador().getName() : "N/A";
                    String matriculaColaborador = ocorrencia.getColaborador() != null ? 
                        ocorrencia.getColaborador().getMatricula() : "N/A";
                    
                    // Obter nome da regional
                    String regionalNome = "N/A";
                    if (ocorrencia.getColaborador() != null) {
                        Long filialId = ocorrencia.getColaborador().getFilial_id();
                        if (filialId != null) {
                            try {
                                var regionalOpt = regionalRepository.findByFilialId(filialId);
                                if (regionalOpt.isPresent()) {
                                    regionalNome = regionalOpt.get().getRegional();
                                } else {
                                    regionalNome = "Matriz";
                                }
                            } catch (Exception e) {
                                regionalNome = "Matriz";
                            }
                        }
                    }
                    
                    // Obter costCenterName do contrato
                    String costCenterName = "N/A";
                    if (ocorrencia.getColaborador() != null && 
                        ocorrencia.getColaborador().getContrato() != null && 
                        ocorrencia.getColaborador().getContrato().getCostCenterName() != null) {
                        costCenterName = ocorrencia.getColaborador().getContrato().getCostCenterName();
                    }
                    
                    // Formatar data de criação da ocorrência
                    String dataCriacaoFormatada = "N/A";
                    if (ocorrencia.getCreated_at() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy '-' HH:mm");
                        dataCriacaoFormatada = sdf.format(ocorrencia.getCreated_at());
                    }
                    
                    // Criar assunto e mensagem personalizados para notificação acumulada
                    String assunto;
                    String mensagem;
                    
                    if ("WHATSAPP".equalsIgnoreCase(canal)) {
                        assunto = String.format(" 🚨 *KOGNI - Módulo %s* 🚨 (NOTIFICAÇÃO ACUMULADA)", moduleName);
                        mensagem = String.format(
                                "🚨 *ALERTA DE ATRASO ACUMULADO: %s*🚨\n\n" +
                                        "\uD83C\uDD7F Protocolo: *%s*\n\n" +
                                        "Olá, *%s*,\n\n" +
                                        "⚠️ *ATENÇÃO:* Esta notificação foi acumulada durante período de inatividade do sistema.\n\n" +
                                        "Responda ao chamado do colaborador: *%s - %s*\n\n" +
                                        "📅Aberto: *%s*\n\n" +
                                        "Regional: *%s*\n\n" +
                                        "Contrato: *%s*\n\n" +
                                        "💬%s\n\n" +
                                        "---\n" +
                                        "SAC - Serviço de Atendimento ao Colaborador Engeman\n\n" +
                                        "⚠️ *OBS.:* Notificação acumulada - período de inatividade: %s até %s ##ENGEMAN##",
                                tempoAtrasoFormatado,
                                occurrenceCode,
                                user.getNome(),
                                matriculaColaborador,
                                nomeColaborador,
                                dataCriacaoFormatada,
                                regionalNome,
                                costCenterName,
                                agendamento.getTextoNotificacao(),
                                formatDateTime(state.getInicioInatividade()),
                                formatDateTime(state.getFimInatividade())
                        );
                    } else {
                        assunto = "🚨 Alerta de Atraso Acumulado - Ticket #" + occurrenceCode;
                        mensagem = String.format(
                            "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <meta charset=\"UTF-8\">" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }" +
                            "        .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                            "        .header h1 { margin: 0; }" +
                            "        .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }" +
                            "        .alert-box { padding: 15px; border-radius: 5px; margin: 15px 0; background: #fff3cd; border-left: 4px solid #ffc107; }" +
                            "        .info-section { background: white; padding: 15px; border-radius: 5px; margin: 10px 0; border-left: 4px solid #dc3545; }" +
                            "        .info-row { display: flex; margin: 8px 0; }" +
                            "        .info-label { font-weight: bold; min-width: 120px; color: #495057; }" +
                            "        .info-value { color: #212529; }" +
                            "        .message-box { background: #e9ecef; padding: 15px; border-radius: 5px; margin: 15px 0; font-style: italic; }" +
                            "        .footer { text-align: center; margin-top: 20px; color: #6c757d; font-size: 12px; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class=\"container\">" +
                            "        <div class=\"header\">" +
                            "            <h1>🚨 Alerta de Atraso Acumulado!</h1>" +
                            "            <p style=\"margin: 0; font-size: 18px;\">Tempo Total: <strong>%s</strong></p>" +
                            "        </div>" +
                            "        <div class=\"content\">" +
                            "            <div class=\"alert-box\">" +
                            "                <strong>⚠️ ATENÇÃO:</strong> Esta notificação foi acumulada durante período de inatividade do sistema.<br/>" +
                            "                Período de inatividade: <strong>%s</strong> até <strong>%s</strong>" +
                            "            </div>" +
                            "            <p>Olá, <strong>%s</strong>,</p>" +
                            "            <div class=\"alert-box\">" +
                            "                <h3>📋 Informações do Ticket</h3>" +
                            "                <div class=\"info-section\">" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Modulo:</span><span class=\"info-value\"><strong>%s</strong></span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Protocolo:</span><span class=\"info-value\"><strong>#%s</strong></span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Colaborador:</span><span class=\"info-value\">%s - %s</span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Aberto:</span><span class=\"info-value\">%s</span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Regional:</span><span class=\"info-value\">%s</span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Contrato:</span><span class=\"info-value\">%s</span></div>" +
                            "                </div>" +
                            "            </div>" +
                            "            <div class=\"message-box\">" +
                            "                <strong>Mensagem:</strong><br/>" +
                            "                %s" +
                            "            </div>" +
                            "        </div>" +
                            "        <div class=\"footer\">" +
                            "            <p>Esta notificação foi gerada automaticamente pelo sistema Kogni após período de inatividade.</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>",
                            tempoAtrasoFormatado,
                            formatDateTime(state.getInicioInatividade()),
                            formatDateTime(state.getFimInatividade()),
                            user.getNome(),
                            moduleName,
                            occurrenceCode,
                            matriculaColaborador,
                            nomeColaborador,
                            dataCriacaoFormatada,
                            regionalNome,
                            costCenterName,
                            agendamento.getTextoNotificacao()
                        );
                    }
                    
                    // Criar notificação
                    Notification notification = new Notification();
                    notification.setTo(user);
                    notification.setSubject(assunto);
                    notification.setMessage(mensagem);
                    
                    // Definir tipo de notificação baseado no canal
                    NotificationType notificationType = getNotificationType(canal);
                    notification.setType(notificationType);
                    
                    // Enviar notificação
                    notificationService.send(notification);
                });
            }
            
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    /**
     * Calcula o tempo total de atraso incluindo período de inatividade
     */
    private long calculateTotalDelayTime(OcorrenciaFF ocorrencia, NotificationSchedule agendamento, 
                                       com.indux.modules.ocf.domain.model.NotificationInactivityState state) {
        long tempoLimiteMinutos = (long) (agendamento.getTempoLimiteHoras() * 60);
        long tempoParadoAntesInatividade = calculateTimeStoppedBeforeInactivity(ocorrencia, state.getInicioInatividade());
        long duracaoInatividadeMinutos = java.time.Duration.between(state.getInicioInatividade(), state.getFimInatividade()).toMinutes();
        
        return tempoParadoAntesInatividade + duracaoInatividadeMinutos - tempoLimiteMinutos;
    }
    
    /**
     * Formata tempo de atraso em minutos/horas/dias
     */
    private String formatDelayTime(long atrasoMinutos) {
        if (atrasoMinutos < 60) {
            return atrasoMinutos + " minutos";
        } else if (atrasoMinutos < 1440) { // Menos de 24 horas
            long horas = atrasoMinutos / 60;
            long minutosRestantes = atrasoMinutos % 60;
            if (minutosRestantes == 0) {
                return horas + " horas";
            } else {
                return horas + " horas e " + minutosRestantes + " minutos";
            }
        } else {
            long dias = atrasoMinutos / 1440;
            long horasRestantes = (atrasoMinutos % 1440) / 60;
            if (horasRestantes == 0) {
                return dias + " dias";
            } else {
                return dias + " dias e " + horasRestantes + " horas";
            }
        }
    }
    
    /**
     * Formata LocalDateTime para string legível
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    private void debugOccurrencesInStep(Integer etapa) {
        // Método de debug removido
    }
    
    private void processSchedule(NotificationSchedule agendamento) {
        try {
            // Buscar ocorrências na etapa especificada que não estão finalizadas
            List<OcorrenciaFF> ocorrencias = findOccurrencesForSchedule(agendamento);
            
            if (ocorrencias.isEmpty()) {
                return;
            }
            
            // Processar cada ocorrência
            for (OcorrenciaFF ocorrencia : ocorrencias) {
                processOccurrenceForSchedule(ocorrencia, agendamento);
            }
            
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    private List<OcorrenciaFF> findOccurrencesForSchedule(NotificationSchedule agendamento) {
        // Status que devem ser ignorados (não enviar notificação)
        Set<String> ignoreStatus = Set.of(
                "REJEITADO",
                "APROVADO", 
                "FINALIZADO"
        );
        
        // Buscar todas as ocorrências na etapa especificada que não estão finalizadas
        List<OcorrenciaFF> todasOcorrencias = findOccurrencesInStep(agendamento.getEtapa(), ignoreStatus);
        
        // Filtrar apenas as que estão paradas há mais tempo que o limite
        List<OcorrenciaFF> ocorrenciasFiltradas = new ArrayList<>();
        
        for (OcorrenciaFF ocorrencia : todasOcorrencias) {
            if (isOccurrenceStoppedTooLong(ocorrencia, agendamento.getTempoLimiteHoras())) {
                ocorrenciasFiltradas.add(ocorrencia);
            }
        }
        
        return ocorrenciasFiltradas;
    }
    
    /**
     * Busca todas as ocorrências na etapa especificada que não estão finalizadas
     */
    private List<OcorrenciaFF> findOccurrencesInStep(Integer etapa, Set<String> ignoreStatus) {
        // Testar diferentes queries para encontrar a correta
        Query query1 = Query.query(
                Criteria.where("currentStep").is(etapa)
                        .and("situacao").nin(ignoreStatus)
        );
        List<OcorrenciaFF> result1 = mongoTemplate.find(query1, OcorrenciaFF.class);
        
        Query query2 = Query.query(
                Criteria.where("etapa_atual").is(etapa)
                        .and("situacao").nin(ignoreStatus)
        );
        List<OcorrenciaFF> result2 = mongoTemplate.find(query2, OcorrenciaFF.class);
        
        // Retornar o resultado que encontrou ocorrências
        if (!result1.isEmpty()) return result1;
        if (!result2.isEmpty()) return result2;
        
        return new ArrayList<>();
    }
    
    /**
     * Verifica se a ocorrência está parada há mais tempo que o limite configurado
     */
    private boolean isOccurrenceStoppedTooLong(OcorrenciaFF ocorrencia, Double tempoLimiteHoras) {
        long tempoParadoMinutos = calculateTimeStoppedInCurrentStep(ocorrencia);
        long limiteMinutos = Math.round(tempoLimiteHoras * 60.0);
        
        boolean deveNotificar = tempoParadoMinutos >= limiteMinutos;
        
        
        return deveNotificar;
    }
    
    /**
     * Calcula quanto tempo a ocorrência está parada na etapa atual baseado no stepLog
     */
    private long calculateTimeStoppedInCurrentStep(OcorrenciaFF ocorrencia) {
        if (ocorrencia.getStepLog() == null || ocorrencia.getStepLog().isEmpty()) {
            return calculateTimeFromCreatedAt(ocorrencia);
        }
        
        // Encontrar o último stepLog da etapa atual
        int etapaAtual = ocorrencia.getCurrentStep();
        Date ultimaAtualizacaoEtapa = findLastStepLogInCurrentStep(ocorrencia.getStepLog(), etapaAtual);
        
        if (ultimaAtualizacaoEtapa == null) {
            return calculateTimeFromCreatedAt(ocorrencia);
        }
        
        // Calcular tempo desde a última atualização na etapa atual
        LocalDateTime ultimaAtualizacao = ultimaAtualizacaoEtapa.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        
        return java.time.Duration.between(ultimaAtualizacao, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Encontra o último stepLog da etapa atual
     */
    private Date findLastStepLogInCurrentStep(List<com.indux.core.domain.model.modules.form.StepLog> stepLogs, int etapaAtual) {
        Date ultimaAtualizacao = null;
        
        for (com.indux.core.domain.model.modules.form.StepLog stepLog : stepLogs) {
            if (stepLog.getStep() == etapaAtual) {
                // Usar final_at se disponível, senão created_at
                Date dataStep = stepLog.getFinal_at() != null ? stepLog.getFinal_at() : stepLog.getCreated_at();
                
                if (ultimaAtualizacao == null || dataStep.after(ultimaAtualizacao)) {
                    ultimaAtualizacao = dataStep;
                }
            }
        }
        
        return ultimaAtualizacao;
    }
    
    /**
     * Fallback: calcula tempo baseado no created_at da ocorrência
     */
    private long calculateTimeFromCreatedAt(OcorrenciaFF ocorrencia) {
        Date createdDate = ocorrencia.getCreated_at();
        LocalDateTime criadaEm = createdDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        
        return java.time.Duration.between(criadaEm, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Calcula o tempo de atraso e formata em minutos/horas/dias
     */
    private String calculateDelayTime(OcorrenciaFF ocorrencia, NotificationSchedule agendamento) {
        long tempoLimiteMinutos = (long) (agendamento.getTempoLimiteHoras() * 60);
        long tempoParadoMinutos = calculateTimeStoppedInCurrentStep(ocorrencia);
        long atrasoMinutos = tempoParadoMinutos - tempoLimiteMinutos;
        
        if (atrasoMinutos < 60) {
            return atrasoMinutos + " minutos";
        } else if (atrasoMinutos < 1440) { // Menos de 24 horas
            long horas = atrasoMinutos / 60;
            long minutosRestantes = atrasoMinutos % 60;
            if (minutosRestantes == 0) {
                return horas + " horas";
            } else {
                return horas + " horas e " + minutosRestantes + " minutos";
            }
        } else {
            long dias = atrasoMinutos / 1440;
            long horasRestantes = (atrasoMinutos % 1440) / 60;
            if (horasRestantes == 0) {
                return dias + " dias";
            } else {
                return dias + " dias e " + horasRestantes + " horas";
            }
        }
    }

    /**
     * Calcula o tempo que a ocorrência está parada na etapa atual e formata
     */
    private String calculateTimeStoppedFormatted(OcorrenciaFF ocorrencia) {
        long tempoParadoMinutos = calculateTimeStoppedInCurrentStep(ocorrencia);
        
        if (tempoParadoMinutos < 60) {
            return tempoParadoMinutos + " minutos";
        } else if (tempoParadoMinutos < 1440) { // Menos de 24 horas
            long horas = tempoParadoMinutos / 60;
            long minutosRestantes = tempoParadoMinutos % 60;
            if (minutosRestantes == 0) {
                return horas + " horas";
            } else {
                return horas + " horas e " + minutosRestantes + " minutos";
            }
        } else {
            long dias = tempoParadoMinutos / 1440;
            long horasRestantes = (tempoParadoMinutos % 1440) / 60;
            if (horasRestantes == 0) {
                return dias + " dias";
            } else {
                return dias + " dias e " + horasRestantes + " horas";
            }
        }
    }
    
    private void processOccurrenceForSchedule(OcorrenciaFF ocorrencia, NotificationSchedule agendamento) {
        // Verificar se a ocorrência já foi notificada por este agendamento
        if (agendamento.getOcorrenciasNotificadas() != null && 
            agendamento.getOcorrenciasNotificadas().contains(ocorrencia.getId())) {
            return;
        }
        
        try {
            // Enviar notificação para cada canal configurado
            for (String canal : agendamento.getCanais()) {
                sendNotificationForChannel(ocorrencia, agendamento, canal);
            }
            
            // Marcar a ocorrência como notificada
            markOccurrenceAsNotified(agendamento, ocorrencia.getId());
            
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    private void sendNotificationForChannel(OcorrenciaFF ocorrencia, NotificationSchedule agendamento, String canal) {
        try {
            // Enviar notificação para cada usuário configurado
            for (String userId : agendamento.getUserIds()) {
                // Buscar dados do usuário
                userService.getUserById(userId).ifPresent(user -> {
                    String occurrenceCode = String.valueOf(ocorrencia.getCodeID());
                    
                    // Buscar nome real do módulo
                    String moduleName = "N/A";
                    try {
                        UUID moduleUUID = UUID.fromString(FINANCE_MODULE_ID);
                        var moduleInfo = moduleService.getModuleByID(moduleUUID);
                        if (moduleInfo != null && moduleInfo.getName() != null) {
                            moduleName = moduleInfo.getName();
                        }
                    } catch (Exception e) {
                        moduleName = "Ocorrência Financeira"; // Fallback
                    }
                    
                    // Calcular tempo que a ocorrência está parada (usando stepLog)
                    String tempoParado = calculateTimeStoppedFormatted(ocorrencia);
                    
                    // Obter dados do colaborador
                    String nomeColaborador = ocorrencia.getColaborador() != null ? 
                        ocorrencia.getColaborador().getName() : "N/A";
                    String matriculaColaborador = ocorrencia.getColaborador() != null ? 
                        ocorrencia.getColaborador().getMatricula() : "N/A";
                    
                    // Obter nome da regional usando filial_id do EmployeeDTO
                    String regionalNome = "N/A";
                    
                    if (ocorrencia.getColaborador() != null) {
                        Long filialId = ocorrencia.getColaborador().getFilial_id();
                        
                        if (filialId != null) {
                            try {
                                // Buscar regional pelo filial_id usando RegionalRepository
                                var regionalOpt = regionalRepository.findByFilialId(filialId);
                                
                                if (regionalOpt.isPresent()) {
                                    regionalNome = regionalOpt.get().getRegional();
                                } else {
                                    regionalNome = "Matriz"; // Fallback se não encontrar
                                }
                            } catch (Exception e) {
                                regionalNome = "Matriz"; // Fallback em caso de erro
                            }
                        }
                    }
                    
                    // Obter costCenterName do contrato
                    String costCenterName = "N/A";
                    if (ocorrencia.getColaborador() != null && 
                        ocorrencia.getColaborador().getContrato() != null && 
                        ocorrencia.getColaborador().getContrato().getCostCenterName() != null) {
                        costCenterName = ocorrencia.getColaborador().getContrato().getCostCenterName();
                    }
                    
                    // Formatar data de criação da ocorrência
                    String dataCriacaoFormatada = "N/A";
                    if (ocorrencia.getCreated_at() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy '-' HH:mm");
                        dataCriacaoFormatada = sdf.format(ocorrencia.getCreated_at());
                    }
                    
                    // Criar assunto e mensagem personalizados baseado no canal
                    String assunto;
                    String mensagem;
                    
                    if ("WHATSAPP".equalsIgnoreCase(canal)) {
                        // Layout para WhatsApp - texto simples
                        assunto = String.format(" 🚨 *KOGNI - Módulo %s* 🚨", moduleName);
                        mensagem = String.format(
                                "🚨 *ALERTA DE ATRASO: %s*🚨\n\n" +          // %s 1
                                        "\uD83C\uDD7F Protocolo: *%s*\n\n" +         // %s 2
                                        "Olá, *%s*,\n\n" +                            // %s 3
                                        "Responda ao chamado do colaborador: *%s - %s*\n\n" + // %s 4 e %s 5
                                        "Aberto: *%s*\n\n" +              // %s 6
                                        "Regional: *%s*\n\n" +                        // %s 7
                                        "Contrato: *%s*\n\n\n" +                      // %s 8
                                        "💬%s\n\n" +                              // %s 9
                                        "---\n" +
                                        "SAC - Serviço de Atendimento ao Colaborador Engeman\n\n" +
                                        "⚠️ *OBS.:* Não é necessário responder a esta mensagem! ##ENGEMAN##",
                                tempoParado,
                                occurrenceCode,
                                user.getNome(),
                                matriculaColaborador,
                                nomeColaborador,
                                dataCriacaoFormatada,
                                regionalNome,
                                costCenterName,
                                agendamento.getTextoNotificacao()
                        );
                    } else {
                        // Layout para Email - HTML formatado
                        assunto = "🚨 Alerta de Atraso - Ticket #" + occurrenceCode;
                        mensagem = String.format(
                            "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <meta charset=\"UTF-8\">" +
                            "    <style>" +
                            "        body {" +
                            "            font-family: Arial, sans-serif;" +
                            "            line-height: 1.6;" +
                            "            color: #333;" +
                            "            margin: 0;" +
                            "            padding: 0;" +
                            "            background-color: #f4f4f4;" +
                            "        }" +
                            "        .container {" +
                            "            max-width: 600px;" +
                            "            margin: 0 auto;" +
                            "            padding: 20px;" +
                            "            background-color: #ffffff;" +
                            "            border-radius: 8px;" +
                            "            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);" +
                            "        }" +
                            "        .header {" +
                            "            background: #dc3545;" +
                            "            color: white;" +
                            "            padding: 20px;" +
                            "            text-align: center;" +
                            "            border-radius: 8px 8px 0 0;" +
                            "        }" +
                            "        .header h1 {" +
                            "            margin: 0;" +
                            "        }" +
                            "        .content {" +
                            "            background: #f8f9fa;" +
                            "            padding: 20px;" +
                            "            border-radius: 0 0 8px 8px;" +
                            "        }" +
                            "        .alert-box {" +
                            "            padding: 15px;" +
                            "            border-radius: 5px;" +
                            "            margin: 15px 0;" +
                            "        }" +
                            "        .info-section {" +
                            "            background: white;" +
                            "            padding: 15px;" +
                            "            border-radius: 5px;" +
                            "            margin: 10px 0;" +
                            "            border-left: 4px solid #dc3545;" +
                            "        }" +
                            "        .info-row {" +
                            "            display: flex;" +
                            "            margin: 8px 0;" +
                            "        }" +
                            "        .info-label {" +
                            "            font-weight: bold;" +
                            "            min-width: 120px;" +
                            "            color: #495057;" +
                            "        }" +
                            "        .info-value {" +
                            "            color: #212529;" +
                            "        }" +
                            "        .message-box {" +
                            "            background: #e9ecef;" +
                            "            padding: 15px;" +
                            "            border-radius: 5px;" +
                            "            margin: 15px 0;" +
                            "            font-style: italic;" +
                            "        }" +
                            "        .footer {" +
                            "            text-align: center;" +
                            "            margin-top: 20px;" +
                            "            color: #6c757d;" +
                            "            font-size: 12px;" +
                            "        }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class=\"container\">" +
                            "        <div class=\"header\">" +
                            "            <h1>🚨 Alerta de Atraso!</h1>" +
                            "            <p style=\"margin: 0; font-size: 18px;\">Tempo: <strong>%s</strong></p>" +
                            "        </div>" +
                            "        <div class=\"content\">" +
                            "            <p>Olá, <strong>%s</strong>,</p>" +
                            "            <div class=\"alert-box\">" +
                            "                <h3>📋 Informações do Ticket</h3>" +
                            "                <div class=\"info-section\">" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Modulo:</span><span class=\"info-value\"><strong>%s</strong></span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Protocolo:</span><span class=\"info-value\"><strong>#%s</strong></span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Colaborador:</span><span class=\"info-value\">%s - %s</span></span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Aberto:</span><span class=\"info-value\">%s</span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Regional:</span><span class=\"info-value\">%s</span></div>" +
                            "                    <div class=\"info-row\"><span class=\"info-label\">Contrato:</span><span class=\"info-value\">%s</span></div>" +
                            "                </div>" +
                            "            </div>" +
                            "            <div class=\"message-box\">" +
                            "                <strong>Mensagem:</strong><br/>" +
                            "                %s" +
                            "            </div>" +
                            "        </div>" +
                            "        <div class=\"footer\">" +
                            "            <p>Esta notificação foi gerada automaticamente pelo sistema Kogni.</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>",
                            tempoParado,
                            user.getNome(),
                            moduleName,
                            occurrenceCode,
                            matriculaColaborador,
                            nomeColaborador,
                            dataCriacaoFormatada,
                            regionalNome,
                            costCenterName,
                            agendamento.getTextoNotificacao()
                        );
                    }
                    
                    // Criar notificação
                    Notification notification = new Notification();
                    notification.setTo(user);
                    notification.setSubject(assunto);
                    notification.setMessage(mensagem);
                    
                    // Definir tipo de notificação baseado no canal
                    NotificationType notificationType = getNotificationType(canal);
                    notification.setType(notificationType);
                    
                    // Enviar notificação
                    notificationService.send(notification);
                });
            }
            
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
    private NotificationType getNotificationType(String canal) {
        return switch (canal.toUpperCase()) {
            case "EMAIL" -> NotificationType.EMAIL;
            case "WHATSAPP" -> NotificationType.WHATSAPP;
            case "TEAMS" -> NotificationType.TEAMS;
            case "WEBSOCKET" -> NotificationType.WEBSOCKET;
            default -> NotificationType.EMAIL; // Default para EMAIL
        };
    }
    
    private void markOccurrenceAsNotified(NotificationSchedule agendamento, String ocorrenciaId) {
        try {
            // Inicializar a lista se for null
            if (agendamento.getOcorrenciasNotificadas() == null) {
                agendamento.setOcorrenciasNotificadas(new ArrayList<>());
            }
            
            // Adicionar a ocorrência à lista se ainda não estiver
            if (!agendamento.getOcorrenciasNotificadas().contains(ocorrenciaId)) {
                agendamento.getOcorrenciasNotificadas().add(ocorrenciaId);
                
                // Salvar no banco de dados
                notificationScheduleRepository.save(agendamento);
            }
            
        } catch (Exception e) {
            // Erro silencioso
        }
    }
    
}
