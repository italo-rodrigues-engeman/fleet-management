package com.indux.modules.request_budgets.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.request_budgets.application.dto.BudgetComercialRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetEngenhariaRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetOperacaoRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetSmsRequestDTO;
import com.indux.modules.request_budgets.application.dto.BudgetVersionResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetComercialRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetEngenhariaRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetSmsRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetOperacaoRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetVersionRequest;
import com.indux.modules.request_budgets.application.dto.Filtro1RequestDTO;
import com.indux.modules.request_budgets.application.dto.Filtro2RequestDTO;
import com.indux.modules.request_budgets.application.mapper.BudgetComercialMapper;
import com.indux.modules.request_budgets.application.mapper.BudgetEngenhariaMapper;
import com.indux.modules.request_budgets.application.mapper.BudgetOperacaoMapper;
import com.indux.modules.request_budgets.application.mapper.BudgetSmsMapper;
import com.indux.modules.request_budgets.application.mapper.BudgetVersionMapper;
import com.indux.modules.request_budgets.domain.model.BudgetComercial;
import com.indux.modules.request_budgets.domain.model.BudgetEngenharia;
import com.indux.modules.request_budgets.domain.model.BudgetOperacao;
import com.indux.modules.request_budgets.domain.model.BudgetSms;
import com.indux.modules.request_budgets.domain.model.BudgetVersion;
import com.indux.modules.request_budgets.domain.model.BudgetScoringConfig;
import com.indux.modules.request_budgets.domain.repository.BudgetRepository;
import com.indux.modules.request_budgets.domain.repository.BudgetScoringConfigRepository;
import com.indux.modules.request_budgets.domain.repository.BudgetVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetVersionService {

    private static final String DEFAULT_SCORING_ID = "default";

    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetScoringConfigRepository scoringConfigRepository;
    private final BudgetVersionMapper budgetVersionMapper;
    private final BudgetComercialMapper budgetComercialMapper;
    private final BudgetEngenhariaMapper budgetEngenhariaMapper;
    private final BudgetSmsMapper budgetSmsMapper;
    private final BudgetOperacaoMapper budgetOperacaoMapper;

    public BudgetVersionResponseDTO createBudgetVersion(String budgetId, CreateBudgetVersionRequest request, String userId) {
        // Verificar se o orçamento existe
        if (!budgetRepository.existsById(budgetId)) {
            throw new ModuleNotFoundFailure("Orçamento não encontrado");
        }

        BudgetVersion version = BudgetVersion.builder()
                .budgetId(budgetId)
                .tipoProposta(request.getTipoProposta())
                .dataEntrega(request.getDataEntrega())
                .metodoEntrega(request.getMetodoEntrega())
                .propostaLocal(request.getPropostaLocal())
                .cidades(request.getCidades())
                .estados(request.getEstados())
                .manutencao(request.getManutencao())
                .operacao(request.getOperacao())
                .atividadesDiversas(request.getAtividadesDiversas())
                .construcaoMontagem(request.getConstrucaoMontagem())
                .fabricacao(request.getFabricacao())
                .projetos(request.getProjetos())
                .diversos(request.getDiversos())
                .detalhes(request.getDetalhes())
                .outros(request.getOutros())
                .comissao(request.getComissao())
                .modalidadeConcorrencia(request.getModalidadeConcorrencia())
                .tipoOportunidade(request.getTipoOportunidade())
                .caracteristicasOportunidade(request.getCaracteristicasOportunidade())
                .outroEmail(request.getOutroEmail())
                .portal(request.getPortal())
                .acessoInfo(request.getAcessoInfo())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Processar documentos da oportunidade
        if (request.getAnexoMd() != null && !request.getAnexoMd().isEmpty()) {
            version.setAnexoMd(request.getAnexoMd());
        }
        version.setMdInfo(request.getMdInfo());

        if (request.getAnexoPpu() != null && !request.getAnexoPpu().isEmpty()) {
            version.setAnexoPpu(request.getAnexoPpu());
        }
        version.setPpuInfo(request.getPpuInfo());

        if (request.getAnexoSms() != null && !request.getAnexoSms().isEmpty()) {
            version.setAnexoSms(request.getAnexoSms());
        }
        version.setSmsInfo(request.getSmsInfo());

        if (request.getAnexoGerais() != null && !request.getAnexoGerais().isEmpty()) {
            version.setAnexoGerais(request.getAnexoGerais());
        }
        version.setGeraisInfo(request.getGeraisInfo());

        if (request.getHabilitacaoAnexo() != null && !request.getHabilitacaoAnexo().isEmpty()) {
            version.setHabilitacaoAnexo(request.getHabilitacaoAnexo());
        }
        version.setHabilitacaoInfo(request.getHabilitacaoInfo());

        // Inicializar listas vazias para as seções
        version.setComercial(new ArrayList<>());
        version.setEngenharia(new ArrayList<>());
        version.setSms(new ArrayList<>());
        version.setOperacoes(new ArrayList<>());

        BudgetVersion savedVersion = budgetVersionRepository.save(version);

        // Calcular pontuação e verificar se deve atualizar status para ETAPA_2
        checkAndUpdateStatusByScore(budgetId, savedVersion, userId);

        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    public BudgetVersionResponseDTO addComercial(String versionId, CreateBudgetComercialRequest request, String userId) {
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        BudgetComercial comercial = BudgetComercial.builder()
                .comentariosComercial(request.getComentariosComercial())
                .anexoComercial(request.getAnexoComercial())
                .dataHoraComercial(request.getDataHoraComercial())
                .responsavelComercial(request.getResponsavelComercial())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (version.getComercial() == null) {
            version.setComercial(new ArrayList<>());
        }
        version.getComercial().add(comercial);
        version.setUpdatedBy(UUID.fromString(userId));
        version.setUpdatedAt(LocalDateTime.now());

        BudgetVersion savedVersion = budgetVersionRepository.save(version);
        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    public BudgetVersionResponseDTO addEngenharia(String versionId, CreateBudgetEngenhariaRequest request, String userId) {
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        BudgetEngenharia engenharia = BudgetEngenharia.builder()
                .comentariosEngenharia(request.getComentariosEngenharia())
                .anexoEngenharia(request.getAnexoEngenharia())
                .responsavelEngenharia(request.getResponsavelEngenharia())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (version.getEngenharia() == null) {
            version.setEngenharia(new ArrayList<>());
        }
        version.getEngenharia().add(engenharia);
        version.setUpdatedBy(UUID.fromString(userId));
        version.setUpdatedAt(LocalDateTime.now());

        BudgetVersion savedVersion = budgetVersionRepository.save(version);
        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    public BudgetVersionResponseDTO addSms(String versionId, CreateBudgetSmsRequest request, String userId) {
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        BudgetSms sms = BudgetSms.builder()
                .comentariosSms(request.getComentariosSms())
                .anexoSms(request.getAnexoSms())
                .datahoraSms(request.getDatahoraSms())
                .responsavelSms(request.getResponsavelSms())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (version.getSms() == null) {
            version.setSms(new ArrayList<>());
        }
        version.getSms().add(sms);
        version.setUpdatedBy(UUID.fromString(userId));
        version.setUpdatedAt(LocalDateTime.now());

        BudgetVersion savedVersion = budgetVersionRepository.save(version);
        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    public BudgetVersionResponseDTO addOperacao(String versionId, CreateBudgetOperacaoRequest request, String userId) {
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        BudgetOperacao operacao = BudgetOperacao.builder()
                .comentariosOperacao(request.getComentariosOperacao())
                .anexoOperacao(request.getAnexoOperacao())
                .datahoraOperacao(request.getDatahoraOperacao())
                .responsavelOperacao(request.getResponsavelOperacao())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (version.getOperacoes() == null) {
            version.setOperacoes(new ArrayList<>());
        }
        version.getOperacoes().add(operacao);
        version.setUpdatedBy(UUID.fromString(userId));
        version.setUpdatedAt(LocalDateTime.now());

        BudgetVersion savedVersion = budgetVersionRepository.save(version);
        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    public BudgetVersionResponseDTO getBudgetVersionById(String id) {
        BudgetVersion version = budgetVersionRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));
        return budgetVersionMapper.toResponseDTO(version);
    }

    public List<BudgetVersionResponseDTO> getBudgetVersionsByBudgetId(String budgetId) {
        return budgetVersionRepository.findByBudgetId(budgetId).stream()
                .map(budgetVersionMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public BudgetVersionResponseDTO approveVersion(String budgetId, String versionId, String userId) {
        // Validar existência da versão
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        // Validar vínculo com o orçamento
        if (version.getBudgetId() == null || !version.getBudgetId().equals(budgetId)) {
            throw new ModuleNotFoundFailure("Versão não pertence ao orçamento informado");
        }

        // Atualizar status do orçamento para etapa 2
        var budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado"));

        budget.setStatus("ETAPA_2");
        budget.setUpdatedBy(UUID.fromString(userId));
        budget.setUpdatedAt(LocalDateTime.now());

        // Opcional: registrar no stepLog a transição para filtro1/etapa 2
        var log = new com.indux.modules.request_budgets.domain.model.Budget.StepLogEmbedded();
        log.setId(UUID.randomUUID().toString());
        log.setName("filtro1");
        log.setStep(2);
        log.setCreated_at(java.util.Date.from(java.time.Instant.now()));
        log.setUser(userId);
        // stepCounter opcional; manter nulo ou calcular a partir do histórico

        if (budget.getStepLog() == null) {
            budget.setStepLog(new java.util.ArrayList<>());
        }
        budget.getStepLog().add(log);

        budgetRepository.save(budget);

        // Retornar a versão aprovada (sem alterar seus campos específicos)
        return budgetVersionMapper.toResponseDTO(version);
    }

    public BudgetVersionResponseDTO updateFiltro1(String versionId, Filtro1RequestDTO request, String userId) {
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        version.setFiltro1(request.getFiltro1());
        version.setDatahoraFiltro1(request.getDatahoraFiltro1());
        version.setResponsavelFiltro1(request.getResponsavelFiltro1());
        version.setMotivoFiltro1(request.getMotivoFiltro1());
        version.setResponsavelFilrtro1(request.getResponsavelFilrtro1());
        version.setNumeroAc(request.getNumeroAc());
        version.setOracamentista(request.getOracamentista());
        version.setDataDesignacao(request.getDataDesignacao());
        version.setJustificativaEngeman(request.getJustificativaEngeman());
        version.setJustificativaSolicitante(request.getJustificativaSolicitante());
        version.setUpdatedBy(UUID.fromString(userId));
        version.setUpdatedAt(LocalDateTime.now());

        // Processar anexos do filtro 1
        if (request.getAnexoFiltro1() != null && !request.getAnexoFiltro1().isEmpty()) {
            version.setAnexoFiltro1(request.getAnexoFiltro1());
        }

        BudgetVersion savedVersion = budgetVersionRepository.save(version);
        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    public BudgetVersionResponseDTO updateFiltro2(String versionId, Filtro2RequestDTO request, String userId) {
        BudgetVersion version = budgetVersionRepository.findById(versionId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Versão do orçamento não encontrada"));

        version.setFiltro2(request.getFiltro2());
        version.setDatahoraFiltro2(request.getDatahoraFiltro2());
        version.setResponsavelFiltro2(request.getResponsavelFiltro2());
        version.setJustificativaFiltro2(request.getJustificativaFiltro2());
        version.setMotivoFiltro2(request.getMotivoFiltro2());
        version.setDataFiltro2(request.getDataFiltro2());
        version.setJustificativaSolicitanteFiltro2(request.getJustificativaSolicitanteFiltro2());
        version.setUpdatedBy(UUID.fromString(userId));
        version.setUpdatedAt(LocalDateTime.now());

        // Processar anexos do filtro 2
        if (request.getAnexoFiltro2() != null && !request.getAnexoFiltro2().isEmpty()) {
            version.setAnexoFiltro2(request.getAnexoFiltro2());
        }

        BudgetVersion savedVersion = budgetVersionRepository.save(version);
        return budgetVersionMapper.toResponseDTO(savedVersion);
    }

    /**
     * Calcula a pontuação da versão e atualiza o status do orçamento para Filtro 2
     * se a pontuação for maior ou igual à pontuacaoReferencia.
     */
    private void checkAndUpdateStatusByScore(String budgetId, BudgetVersion version, String userId) {
        try {
            // Buscar o orçamento
            var budget = budgetRepository.findById(budgetId)
                    .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado"));

            // Buscar a configuração de scoring
            BudgetScoringConfig scoringConfig = scoringConfigRepository.findById(DEFAULT_SCORING_ID)
                    .orElse(null);

            if (scoringConfig == null || scoringConfig.getPontuacaoReferencia() == null) {
                // Se não houver configuração ou pontuacaoReferencia, não faz nada
                return;
            }

            // Calcular pontuação
            int totalScore = calculateScore(budget, version, scoringConfig);

            // Se a pontuação for >= pontuacaoReferencia, atualizar status para Filtro 2
            if (totalScore >= scoringConfig.getPontuacaoReferencia()) {
                // Verificar se já está no Filtro 2 para evitar atualizações desnecessárias
                boolean alreadyInFiltro2 = budget.getStepLog() != null && 
                    budget.getStepLog().stream()
                        .anyMatch(log -> "filtro2".equals(log.getName()) && log.getStep() != null && log.getStep() == 3);
                
                if (!alreadyInFiltro2) {
                    budget.setStatus("ETAPA_2");
                    budget.setUpdatedBy(UUID.fromString(userId));
                    budget.setUpdatedAt(LocalDateTime.now());

                    // Registrar no stepLog a transição para filtro2
                    var log = new com.indux.modules.request_budgets.domain.model.Budget.StepLogEmbedded();
                    log.setId(UUID.randomUUID().toString());
                    log.setName("filtro2");
                    log.setStep(3);
                    log.setCreated_at(java.util.Date.from(java.time.Instant.now()));
                    log.setUser(userId);

                    if (budget.getStepLog() == null) {
                        budget.setStepLog(new java.util.ArrayList<>());
                    }
                    budget.getStepLog().add(log);

                    budgetRepository.save(budget);
                }
            }
        } catch (Exception e) {
            // Log do erro mas não interrompe o fluxo
            // Em produção, usar um logger adequado
            System.err.println("Erro ao calcular pontuação e atualizar status: " + e.getMessage());
        }
    }

    /**
     * Calcula a pontuação total baseado nos dados do Budget e BudgetVersion.
     */
    private int calculateScore(com.indux.modules.request_budgets.domain.model.Budget budget, 
                                BudgetVersion version, 
                                BudgetScoringConfig config) {
        int totalScore = 0;

        // Pontuação por mercado (usando mercadoId como string)
        if (budget.getMercadoId() != null && config.getMercadoScores() != null) {
            String mercadoKey = String.valueOf(budget.getMercadoId());
            totalScore += config.getMercadoScores().getOrDefault(mercadoKey, 0);
        }

        // Pontuação por setor (usando setorId como string)
        if (budget.getSetorId() != null && config.getSetorScores() != null) {
            String setorKey = String.valueOf(budget.getSetorId());
            totalScore += config.getSetorScores().getOrDefault(setorKey, 0);
        }

        // Pontuação por tempo de contrato
        if (budget.getTempoContrato() != null && config.getTempoContratoScores() != null) {
            totalScore += config.getTempoContratoScores().getOrDefault(budget.getTempoContrato(), 0);
        }

        // Pontuação por porte estimado (convertendo BigDecimal para String)
        if (budget.getPorteEstimado() != null && config.getPorteEstimadoScores() != null) {
            // Assumindo que porteEstimado é um valor numérico que precisa ser categorizado
            // Por enquanto, vamos tentar usar o valor como string
            String porteKey = budget.getPorteEstimado().toString();
            totalScore += config.getPorteEstimadoScores().getOrDefault(porteKey, 0);
        }

        // Pontuação por modalidade de concorrência
        if (version.getModalidadeConcorrencia() != null && config.getModalidadeConcorrenciaScores() != null) {
            totalScore += config.getModalidadeConcorrenciaScores().getOrDefault(version.getModalidadeConcorrencia(), 0);
        }

        // Pontuação por tipo de oportunidade
        if (version.getTipoOportunidade() != null && config.getTipoScores() != null) {
            totalScore += config.getTipoScores().getOrDefault(version.getTipoOportunidade(), 0);
        }

        // Pontuação por características da oportunidade
        if (version.getCaracteristicasOportunidade() != null && config.getCaracteristicaScores() != null) {
            totalScore += config.getCaracteristicaScores().getOrDefault(version.getCaracteristicasOportunidade(), 0);
        }

        // Pontuação por estados (soma de todos os estados)
        if (version.getEstados() != null && !version.getEstados().isEmpty() && config.getEstadoScores() != null) {
            for (String estado : version.getEstados()) {
                totalScore += config.getEstadoScores().getOrDefault(estado, 0);
            }
        }

        // Pontuação por keywords (se houver)
        // Nota: Keywords precisariam ser buscadas de algum lugar, por enquanto não incluímos

        return totalScore;
    }

}

