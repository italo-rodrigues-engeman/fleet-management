package com.indux.modules.ocf.application.service;

import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.core.infra.notifcation.whatsapp.WhatsappSender;
import com.indux.modules.ocf.domain.model.MeiosComunicacao;
import com.indux.modules.ocf.domain.model.OcorrenciaFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço dedicado para notificações de colaboradores
 * Separado das notificações originais do sistema de agendamento
 */
@Service
public class CollaboratorNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CollaboratorNotificationService.class);

    @Autowired
    private CustomMailSender mailSender;

    @Autowired
    private WhatsappSender whatsappSender;

    @Autowired
    private ModuleManagementService moduleService;

    /**
     * Envia notificação padrão para colaborador usando meiosComunicacao
     * MÉTODO DEPRECIADO - Use sendStandardNotificationToCollaboratorWithMeiosComunicacao
     */
    @Deprecated
    public void sendStandardNotificationToCollaborator(OcorrenciaFF occurrence, String email, String telefone, List<String> canais) {
        // Redirecionar para o método que usa meiosComunicacao
        sendStandardNotificationToCollaboratorWithMeiosComunicacao(occurrence, canais);
    }

    /**
     * Envia notificação padrão para colaborador usando meiosComunicacao
     */
    public void sendStandardNotificationToCollaboratorWithMeiosComunicacao(OcorrenciaFF occurrence, List<String> canais) {

            MeiosComunicacao meios = occurrence.getMeiosComunicacao();
            String nomeColaborador = occurrence.getColaborador() != null ? occurrence.getColaborador().getName() : "Colaborador";

            int totalEnviados = 0;
            for (String canal : canais) {
                switch (canal.toUpperCase()) {
                    case "EMAIL":
                        if (meios.getEmail() != null && !meios.getEmail().isEmpty()) {
                            for (String email : meios.getEmail()) {
                                if (email != null && !email.trim().isEmpty()) {
                                    sendStandardEmailToCollaborator(email, nomeColaborador, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                        
                    case "WHATSAPP":
                        if (meios.getTelefone() != null && !meios.getTelefone().isEmpty()) {
                            for (String telefone : meios.getTelefone()) {
                                if (telefone != null && !telefone.trim().isEmpty()) {
                                    sendStandardWhatsAppToCollaborator(telefone, nomeColaborador, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                        
                    case "SMS":
                        if (meios.getTelefone() != null && !meios.getTelefone().isEmpty()) {
                            for (String telefone : meios.getTelefone()) {
                                if (telefone != null && !telefone.trim().isEmpty()) {
                                    sendStandardSMSToCollaborator(telefone, nomeColaborador, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                }
            }

                

    }

    /**
     * Envia notificação de resposta para colaborador usando meiosComunicacao
     * MÉTODO DEPRECIADO - Use sendResponseNotificationToCollaboratorWithMeiosComunicacao
     */
    @Deprecated
    public void sendResponseNotificationToCollaborator(OcorrenciaFF occurrence, String email, String telefone, List<String> canais, String respostaEmpregado) {
        // Redirecionar para o método que usa meiosComunicacao
        sendResponseNotificationToCollaboratorWithMeiosComunicacao(occurrence, canais, respostaEmpregado);
    }

    /**
     * Envia notificação de resposta para colaborador usando meiosComunicacao
     */
    public void sendResponseNotificationToCollaboratorWithMeiosComunicacao(OcorrenciaFF occurrence, List<String> canais, String respostaEmpregado) {

            MeiosComunicacao meios = occurrence.getMeiosComunicacao();
            String nomeColaborador = occurrence.getColaborador() != null ? occurrence.getColaborador().getName() : "Colaborador";

            int totalEnviados = 0;
            for (String canal : canais) {
                switch (canal.toUpperCase()) {
                    case "EMAIL":
                        if (meios.getEmail() != null && !meios.getEmail().isEmpty()) {
                            for (String email : meios.getEmail()) {
                                if (email != null && !email.trim().isEmpty()) {
                                    sendResponseEmailToCollaborator(email, nomeColaborador, respostaEmpregado, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                        
                    case "WHATSAPP":
                        if (meios.getTelefone() != null && !meios.getTelefone().isEmpty()) {
                            for (String telefone : meios.getTelefone()) {
                                if (telefone != null && !telefone.trim().isEmpty()) {
                                    sendResponseWhatsAppToCollaborator(telefone, nomeColaborador, respostaEmpregado, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                        
                    case "SMS":
                        if (meios.getTelefone() != null && !meios.getTelefone().isEmpty()) {
                            for (String telefone : meios.getTelefone()) {
                                if (telefone != null && !telefone.trim().isEmpty()) {
                                    sendResponseSMSToCollaborator(telefone, nomeColaborador, respostaEmpregado, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                }
            }

    }

    /**
     * Envia notificação específica de recusa para colaborador usando meiosComunicacao
     */
    public void sendRejectionNotificationToCollaboratorWithMeiosComunicacao(OcorrenciaFF occurrence, List<String> canais, String motivoRecusa) {
            MeiosComunicacao meios = occurrence.getMeiosComunicacao();
            String nomeColaborador = occurrence.getColaborador() != null ? occurrence.getColaborador().getName() : "Colaborador";
            String motivo = motivoRecusa != null && !motivoRecusa.trim().isEmpty() ? motivoRecusa : "Ocorrência recusada";


            int totalEnviados = 0;
            for (String canal : canais) {
                switch (canal.toUpperCase()) {
                    case "EMAIL":
                        if (meios.getEmail() != null && !meios.getEmail().isEmpty()) {
                            for (String email : meios.getEmail()) {
                                if (email != null && !email.trim().isEmpty()) {

                                    sendRejectionEmailToCollaborator(email, nomeColaborador, motivo, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                        
                    case "WHATSAPP":
                        if (meios.getTelefone() != null && !meios.getTelefone().isEmpty()) {
                            for (String telefone : meios.getTelefone()) {
                                if (telefone != null && !telefone.trim().isEmpty()) {
                                    sendRejectionWhatsAppToCollaborator(telefone, nomeColaborador, motivo, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                        
                    case "SMS":
                        if (meios.getTelefone() != null && !meios.getTelefone().isEmpty()) {
                            for (String telefone : meios.getTelefone()) {
                                if (telefone != null && !telefone.trim().isEmpty()) {
                                    sendRejectionSMSToCollaborator(telefone, nomeColaborador, motivo, occurrence);
                                    totalEnviados++;
                                }
                            }
                        }
                        break;
                }
            }
                

    }

    private void sendStandardEmailToCollaborator(String email, String nomeColaborador, OcorrenciaFF occurrence) {

            String subject = "Ocorrência Encaminhada - #" + occurrence.getCodeID();
            
            // Obter nome do módulo
            String moduleName = "OCF"; // Nome padrão do módulo
            if (occurrence.getModulo_id() != null) {
                try {
                    // Buscar nome do módulo usando ModuleManagementService
                    var modulo = moduleService.getModuleByID(occurrence.getModulo_id());
                    if (modulo != null && modulo.getName() != null) {
                        moduleName = modulo.getName();
                    }
                } catch (Exception e) {
                    // Em caso de erro, manter o nome padrão
                    moduleName = "OCF";
                }
            }
            
            // Obter data de abertura formatada
            String dataAbertura = "Data não disponível";
            if (occurrence.getData_Ocorrencia() != null) {
                dataAbertura = occurrence.getData_Ocorrencia().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            
            // Mensagem personalizada
            String mensagemPersonalizada = String.format(
                "ENGEMAN INFORMA:<br/><br/>" +
                "Olá, <strong>%s</strong>!<br/><br/>" +
                "Seu chamado '<strong>%s</strong>', aberto no SAC Engeman em '<strong>%s</strong>' está sendo encaminhado para análise da equipe da matriz da Engeman, que tem mais recursos e condições para avaliar com precisão.<br/><br/>" +
                "Pedimos sua paciência e compreensão.<br/><br/>" +
                "Estamos fazendo o nosso melhor para resolver sua questão o quanto antes.<br/><br/>" +
                "Se precisar, pode continuar entrando em contato por aqui.",
                nomeColaborador, 
                occurrence.getCodeID(), 
                dataAbertura
            );
            
            String mensagemPadrao = String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "    <head></head>" +
                "    <body>" +
                "        <div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;\">" +
                "            <div style=\"background: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;\">" +
                "                <h1 style=\"margin: 0 0 10px 0;\">📋 Ocorrência Encaminhada</h1>" +
                "                <p style=\"margin: 0; font-size: 18px;\">Protocolo: <strong>#%s</strong></p>" +
                "            </div>" +
                "            <div style=\"background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px;\">" +
                "                <div style=\"background: white; padding: 15px; border-radius: 5px; margin: 10px 0; border-left: 4px solid #007bff;\">" +
                "                    <h3 style=\"margin: 0 0 15px 0; color: #007bff;\">📋 Informações da Ocorrência</h3>" +
                "                    <p style=\"margin: 5px 0;\"><strong>Modulo:</strong> #%s</p>" +
                "                    <p style=\"margin: 5px 0;\"><strong>Protocolo:</strong> #%s</p>" +
                "                    <p style=\"margin: 5px 0;\"><strong>Data de Abertura:</strong> %s</p>" +
                "                    <p style=\"margin: 5px 0;\"><strong>Status:</strong> Encaminhada para Matriz</p>" +
                "                </div>" +
                "                <div style=\"background: #e9ecef; padding: 15px; border-radius: 5px; margin: 15px 0; font-style: italic;\">" +
                "                    %s" +
                "                </div>" +
                "                <div style=\"text-align: center; margin-top: 20px; color: #6c757d; font-size: 12px;\">" +
                "                    <p>Esta notificação foi gerada automaticamente pelo sistema Kogni.</p>" +
                "                </div>" +
                "            </div>" +
                "        </div>" +
                "    </body>" +
                "</html>",
                occurrence.getCodeID(),        // %s - Protocolo (primeiro)
                moduleName,                   // %s - Nome do Módulo
                occurrence.getCodeID(),       // %s - Protocolo (segundo)
                dataAbertura,                 // %s - Data de Abertura
                mensagemPersonalizada         // %s - Mensagem personalizada
            );
            

            mailSender.sendGenericEmail(email, subject, mensagemPadrao, null);


    }

    private void sendStandardWhatsAppToCollaborator(String telefone, String nomeColaborador, OcorrenciaFF occurrence) {

            String mensagemPadrao = String.format(
                    "🟠 *ENGEMAN INFORMA* 🟠\n\n\n" +
                            "Olá, %s!\n\n\n" +
                            "Seu chamado (ticket: *%s*), aberto no SAC Engeman em *%s* está sendo encaminhado para análise da matriz da Engeman, que terá mais condições para avaliar sua demanda.\n\n\n" +
                            "A Engeman está fazendo o máximo para resolver sua questão o quanto antes. Agradecemos sua paciência e compreensão.\n\n\n" +
                            "✅ Se precisar de outra ajuda, é só abrir um novo chamado por aqui.\n\n\n" +
                            "---\n" +
                            "SAC - Serviço de Atendimento ao Colaborador Engeman\n\n\n" +
                            "⚠️ *OBS.:* Não é necessário responder a esta mensagem!",
                nomeColaborador,
                occurrence.getCodeID(), 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            

            
            String telefoneFormatado = formatarTelefoneParaWhatsApp(telefone);

            whatsappSender.sendMessage(telefoneFormatado, mensagemPadrao);


    }

    private void sendStandardSMSToCollaborator(String telefone, String nomeColaborador, OcorrenciaFF occurrence) {
        try {
            String message = String.format(
                "ENGEMAN INFORMA: Olá, %s! Seu chamado #%s foi encaminhado para análise da matriz. Pedimos paciência. Equipe de Atendimento",
                nomeColaborador,
                occurrence.getCodeID()
            );
            
            // Implementar envio de SMS aqui
        } catch (Exception e) {
        }
    }

    private void sendResponseEmailToCollaborator(String email, String nomeColaborador, String respostaEmpregado, OcorrenciaFF occurrence) {

            String subject = ":: ENGEMAN INFORMA ::";
            
            // Usar apenas o texto personalizado se for fornecido
            String body = respostaEmpregado;
            

            mailSender.sendGenericEmail(email, subject, body, null);


    }

    private void sendResponseWhatsAppToCollaborator(String telefone, String nomeColaborador, String respostaEmpregado, OcorrenciaFF occurrence) {

            // Usar apenas o texto personalizado se for fornecido
            String message = respostaEmpregado;
            
            String telefoneFormatado = formatarTelefoneParaWhatsApp(telefone);

            whatsappSender.sendMessage(telefoneFormatado, message);


    }

    private void sendResponseSMSToCollaborator(String telefone, String nomeColaborador, String respostaEmpregado, OcorrenciaFF occurrence) {
        try {
            String message = String.format(
                "ENGEMAN: Olá %s! Sua ocorrência #%d foi finalizada. Resposta: %s. Protocolo: #%d",
                nomeColaborador, occurrence.getCodeID(), respostaEmpregado, occurrence.getCodeID()
            );
            
            logger.debug("Enviando SMS de resposta - Para: {}, Ocorrência: #{}", telefone, occurrence.getCodeID());
            // Implementar envio de SMS aqui
            logger.info("SMS de resposta enviado com sucesso para: {} - Ocorrência: #{}", telefone, occurrence.getCodeID());
        } catch (Exception e) {
            logger.error("Erro ao enviar SMS de resposta para colaborador: {} - Ocorrência: #{} - Erro: {}", 
                telefone, occurrence.getCodeID(), e.getMessage(), e);
        }
    }

    private String formatarTelefoneParaWhatsApp(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return telefone;
        }
        
        // Remove todos os caracteres não numéricos
        String apenasNumeros = telefone.replaceAll("[^0-9]", "");
        
        // Se não começar com 55, adiciona
        if (!apenasNumeros.startsWith("55")) {
            apenasNumeros = "55" + apenasNumeros;
        }
        
        return apenasNumeros;
    }

    // ========== MÉTODOS PRIVADOS DE ENVIO DE RECUSA ==========

    private void sendRejectionEmailToCollaborator(String email, String nomeColaborador, String motivoRecusa, OcorrenciaFF occurrence) {
            String subject = "Ocorrência Recusada - #" + occurrence.getCodeID();
            
            // Obter nome do módulo
            String moduleName = "OCF"; // Nome padrão do módulo
            if (occurrence.getModulo_id() != null) {
                try {
                    // Buscar nome do módulo usando ModuleManagementService
                    var modulo = moduleService.getModuleByID(occurrence.getModulo_id());
                    if (modulo != null && modulo.getName() != null) {
                        moduleName = modulo.getName();
                    }
                } catch (Exception e) {
                    // Em caso de erro, manter o nome padrão
                    moduleName = "OCF";
                }
            }
            
            // Obter data de abertura formatada
            String dataAbertura = "Data não disponível";
            if (occurrence.getData_Ocorrencia() != null) {
                dataAbertura = occurrence.getData_Ocorrencia().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            
            String body = String.format(
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
                "        .greeting {" +
                "            font-size: 16px;" +
                "            margin-bottom: 20px;" +
                "        }" +
                "        .main-text {" +
                "            margin: 15px 0;" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>🚫 TICKET: %s</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                        "" +
                        "            <div class=\"info-section\">" +
                        "                <div class=\"info-row\"><span class=\"info-label\">Data de Abertura:</span><span class=\"info-value\">%s</span></div>" +
                        "                <div class=\"info-row\"><span class=\"info-label\">Status:</span><span class=\"info-value\">Recusada</span></div>" +
                        "            </div>" +
                        "" +
                "            <div class=\"greeting\">" +
                "                <p>Olá, <strong>%s</strong>!</p>" +
                "            </div>" +
                "            " +
                "            <div class=\"main-text\">" +
                "                <p>Sua ocorrência <strong>(ticket: %s)</strong>, aberta no SAC Engeman em <strong>%s</strong>, foi analisada e não foi aprovada.</p>" +
                "            </div>" +

                "            <div class=\"message-box\">" +
                "                <strong>Resposta da equipe:</strong><br/>" +
                "                %s" +
                "            </div>" +
                "" +
                "            <div class=\"main-text\">" +
                "                <p>Se você discorda desta decisão ou possui informações adicionais, entre em contato conosco para esclarecimentos.</p>" +
                "                <p>Estamos à disposição para o que precisar!</p>" +
                "            </div>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>Esta notificação foi gerada automaticamente pelo sistema ENGEMAN.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>",
                    occurrence.getCodeID(),
                    dataAbertura,
                nomeColaborador, 
                occurrence.getCodeID(),
                    dataAbertura,
                motivoRecusa
            );
            

            mailSender.sendGenericEmail(email, subject, body, null);


    }

    private void sendRejectionWhatsAppToCollaborator(String telefone, String nomeColaborador, String motivoRecusa, OcorrenciaFF occurrence) {

            // Obter data de abertura formatada
            String dataAbertura = "Data não disponível";
            if (occurrence.getData_Ocorrencia() != null) {
                dataAbertura = occurrence.getData_Ocorrencia().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            
            String message = String.format(
                    "🔴 *ENGEMAN INFORMA* 🔴\n\n\n" +
                            "Olá, %s!\n\n\n" +
                            "Seu chamado (ticket: *%s*), aberto no SAC Engeman em *%s*, foi analisado e concluído pelo nosso time de RH como *não procedente*.\n\n\n" +
                            "💬 *MENSAGEM PARA VOCÊ:*\n\n" +
                            "%s\n\n\n\n" +
                            "Se você discorda desta decisão ou possui informações adicionais, entre em contato conosco para esclarecimentos.\n\n\n" +
                            "✅ Estamos à disposição para o que precisar!\n\n\n" +
                            "---\n" +
                            "SAC - Serviço de Atendimento ao Colaborador Engeman\n\n\n" +
                            "⚠️ *OBS.:* Não é necessário responder a esta mensagem! ##ENGEMAN##",
                nomeColaborador,
                occurrence.getCodeID(),
                dataAbertura,
                motivoRecusa
            );
            
            String telefoneFormatado = formatarTelefoneParaWhatsApp(telefone);

            whatsappSender.sendMessage(telefoneFormatado, message);


    }

    private void sendRejectionSMSToCollaborator(String telefone, String nomeColaborador, String motivoRecusa, OcorrenciaFF occurrence) {
        try {
            String message = String.format(
                "ENGEMAN: Olá %s! Sua ocorrência #%s foi recusada. Motivo: %s. Protocolo: #%s",
                nomeColaborador, 
                occurrence.getCodeID(), 
                motivoRecusa.length() > 50 ? motivoRecusa.substring(0, 50) + "..." : motivoRecusa,
                occurrence.getCodeID()
            );
            
            logger.debug("Enviando SMS de recusa - Para: {}, Ocorrência: #{}", telefone, occurrence.getCodeID());
            // Implementar envio de SMS aqui
            logger.info("SMS de recusa enviado com sucesso para: {} - Ocorrência: #{}", telefone, occurrence.getCodeID());
        } catch (Exception e) {
            logger.error("Erro ao enviar SMS de recusa para colaborador: {} - Ocorrência: #{} - Erro: {}", 
                telefone, occurrence.getCodeID(), e.getMessage(), e);
        }
    }
}
