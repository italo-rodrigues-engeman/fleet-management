package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.dto.UpdateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.*;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.application.mapper.LaborRightsMapper;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class UpdateLaborContractUseCase {

    private final LaborContractRepository laborContractRepository;
    private final UnionRepository unionRepository;
    private final LaborContractMapper laborContractMapper;
    private final LaborRightsMapper laborRightsMapper;
    private final AttachmentService attachmentService;

    @Transactional
    public LaborContractResponseDTO execute(String contractId, @Valid UpdateLaborContractRequestDTO request, String usuarioAtualizacao) {
        // Buscar o contrato existente
        LaborContract existingContract = laborContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("ERRO: Contrato trabalhista não encontrado com ID '" + contractId + "'."));

        // Validar se o contrato está ativo
        if (!"ATIVO".equals(existingContract.getStatusRegistro())) {
            throw new RuntimeException("ERRO: Apenas contratos ativos podem ser editados.");
        }

        // Validar datas se fornecidas
        if (request.getDataInicioVigencia() != null && request.getDataFimVigencia() != null) {
            if (request.getDataInicioVigencia().isAfter(request.getDataFimVigencia())) {
                throw new RuntimeException("ERRO: Data de início da vigência deve ser anterior à data de fim.");
            }
        }

        // Processar anexos se fornecidos
        List<AttachmentEntity> newAttachments = processAttachments(request.getArquivosInstrumento());

        // Atualizar apenas os campos fornecidos
        updateContractFields(existingContract, request, newAttachments, usuarioAtualizacao);

        // Adicionar log de auditoria para atualização (apenas se usuário foi fornecido)
        if (usuarioAtualizacao != null && !usuarioAtualizacao.isEmpty()) {
            try {
                StepLog updateLog = createStepLog(
                    "ATUALIZACAO_" + existingContract.getTipoInstrumento().name(),
                    UUID.fromString(usuarioAtualizacao),
                    existingContract.getTipoInstrumento().name() + " atualizado"
                );
                
                if (existingContract.getStepLog() == null) {
                    existingContract.setStepLog(new ArrayList<>());
                }
                existingContract.getStepLog().add(updateLog);
            } catch (IllegalArgumentException e) {
                // Se não for um UUID válido, apenas registra o nome
            }
        }
        existingContract.setDataUltimaAtualizacao(LocalDateTime.now());
        existingContract.setUsuarioUltimaAtualizacao(usuarioAtualizacao);

        // Salvar o contrato atualizado
        LaborContract updatedContract = laborContractRepository.save(existingContract);

        // Buscar informações do sindicato para a resposta
        Union union = unionRepository.findById(updatedContract.getSindicatoTrabalhadoresId()).orElse(null);

        return laborContractMapper.toResponseDTOWithUnionInfo(updatedContract, union);
    }

    private void updateContractFields(LaborContract contract, UpdateLaborContractRequestDTO request, 
                                    List<AttachmentEntity> newAttachments, String usuarioAtualizacao) {
        
        // Atualizar campos básicos usando BeanUtils para simplificar
        updateFieldIfNotNull(contract::setTipoInstrumento, request.getTipoInstrumento());
        updateFieldIfNotNull(contract::setApelido, request.getApelido());
        updateFieldIfNotNull(contract::setNumeroRegistro, request.getNumeroRegistro());
        updateFieldIfNotNull(contract::setNumeroSolicitacao, request.getNumeroSolicitacao());
        updateFieldIfNotNull(contract::setNomeInstrumento, request.getNomeInstrumento());
        updateFieldIfNotNull(contract::setEmpresasSignatarias, request.getEmpresasSignatarias());
        updateFieldIfNotNull(contract::setDataInicioVigencia, request.getDataInicioVigencia());
        updateFieldIfNotNull(contract::setDataFimVigencia, request.getDataFimVigencia());
        updateFieldIfNotNull(contract::setDataBase, request.getDataBase());
        updateFieldIfNotNull(contract::setAbrangenciaTerritorial, request.getAbrangenciaTerritorial());
        updateFieldIfNotNull(contract::setUfPrincipal, request.getUfPrincipal());
        updateFieldIfNotNull(contract::setMunicipiosAbrangidos, request.getMunicipiosAbrangidos());
        updateFieldIfNotNull(contract::setEstadosAdicionais, request.getEstadosAdicionais());
        updateFieldIfNotNull(contract::setObservacoesTerritoriais, request.getObservacoesTerritoriais());
        updateFieldIfNotNull(contract::setCategoriaPrincipalCBO, request.getCategoriaPrincipalCBO());
        updateFieldIfNotNull(contract::setSubcategoriaCBO, request.getSubcategoriaCBO());
        updateFieldIfNotNull(contract::setFuncoesEspecificas, request.getFuncoesEspecificas());
        updateFieldIfNotNull(contract::setExcecoesInclusoes, request.getExcecoesInclusoes());
        updateFieldIfNotNull(contract::setSituacaoMTE, request.getSituacaoMTE());
        updateFieldIfNotNull(contract::setLinkAnexo, request.getLinkAnexo());
        updateFieldIfNotNull(contract::setResumoIA, request.getResumoIA());
        
        // Campo especial com validação adicional
        if (request.getNumeroIdentificacaoInterno() != null && !request.getNumeroIdentificacaoInterno().trim().isEmpty()) {
            contract.setNumeroIdentificacaoInterno(request.getNumeroIdentificacaoInterno());
        }

        // Atualizar direitos trabalhistas se fornecidos (merge profundo)
        if (request.getLaborRights() != null) {
            mergeLaborRights(contract, request.getLaborRights());
        }

        // Atualizar anexos se fornecidos
        if (!newAttachments.isEmpty()) {
            contract.setArquivoInstrumento(newAttachments);
        }
    }
    
    private <T> void updateFieldIfNotNull(java.util.function.Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
    
    /**
     * Faz merge profundo dos direitos trabalhistas, preservando campos não fornecidos
     */
    private void mergeLaborRights(LaborContract contract, LaborRightsDTO newLaborRights) {
        // Obter o laborRights existente ou criar um novo
        LaborRightsDTO existingLaborRights = contract.getLaborRights();
        if (existingLaborRights == null) {
            contract.setLaborRights(newLaborRights);
            return;
        }
        
        // Merge de cada sub-DTO
        if (newLaborRights.getSalary() != null) {
            mergeSalaryDTO(existingLaborRights.getSalary(), newLaborRights.getSalary());
        }
        
        if (newLaborRights.getBenefits() != null) {
            mergeBenefitsDTO(existingLaborRights.getBenefits(), newLaborRights.getBenefits());
        }
        
        if (newLaborRights.getHealthBenefits() != null) {
            mergeHealthBenefitsDTO(existingLaborRights.getHealthBenefits(), newLaborRights.getHealthBenefits());
        }
        
        if (newLaborRights.getWorkSchedule() != null) {
            mergeWorkScheduleDTO(existingLaborRights.getWorkSchedule(), newLaborRights.getWorkSchedule());
        }
        
        if (newLaborRights.getWorkShiftTypes() != null) {
            mergeWorkShiftTypesDTO(existingLaborRights.getWorkShiftTypes(), newLaborRights.getWorkShiftTypes());
        }
        
        if (newLaborRights.getContractTime() != null) {
            mergeContractTimeDTO(existingLaborRights.getContractTime(), newLaborRights.getContractTime());
        }
        
        if (newLaborRights.getTimeOffBenefits() != null) {
            mergeTimeOffBenefitsDTO(existingLaborRights.getTimeOffBenefits(), newLaborRights.getTimeOffBenefits());
        }
        
        if (newLaborRights.getHealthSafety() != null) {
            mergeHealthSafetyDTO(existingLaborRights.getHealthSafety(), newLaborRights.getHealthSafety());
        }
        
        if (newLaborRights.getAdditionalBenefits() != null) {
            mergeAdditionalBenefitsDTO(existingLaborRights.getAdditionalBenefits(), newLaborRights.getAdditionalBenefits());
        }
        
        if (newLaborRights.getTransportation() != null) {
            mergeTransportationDTO(existingLaborRights.getTransportation(), newLaborRights.getTransportation());
        }
        
        if (newLaborRights.getSeniorityBonus() != null) {
            mergeSeniorityBonusDTO(existingLaborRights.getSeniorityBonus(), newLaborRights.getSeniorityBonus());
        }
        
        if (newLaborRights.getWorkplaceConditions() != null) {
            mergeWorkplaceConditionsDTO(existingLaborRights.getWorkplaceConditions(), newLaborRights.getWorkplaceConditions());
        }
        
        if (newLaborRights.getDisciplinaryProcedures() != null) {
            mergeDisciplinaryProceduresDTO(existingLaborRights.getDisciplinaryProcedures(), newLaborRights.getDisciplinaryProcedures());
        }
        
        if (newLaborRights.getUnionContributions() != null) {
            mergeUnionContributionsDTO(existingLaborRights.getUnionContributions(), newLaborRights.getUnionContributions());
        }
    }
    
    private void mergeSalaryDTO(SalaryDTO existing, SalaryDTO updated) {
        if (updated.getPisoSalarial() != null) existing.setPisoSalarial(updated.getPisoSalarial());
        if (updated.getDataBase() != null) existing.setDataBase(updated.getDataBase());
        if (updated.getPorcentagemReajuste() != null) existing.setPorcentagemReajuste(updated.getPorcentagemReajuste());
        if (updated.getFuncoes() != null) existing.setFuncoes(updated.getFuncoes());
    }
    
    private void mergeBenefitsDTO(BenefitsDTO existing, BenefitsDTO updated) {
        // Merge de benefícios de alimentação
        if (updated.getCafeDaManha() != null) {
            if (existing.getCafeDaManha() != null) {
                mergeBeneficioEstruturadoDTO(existing.getCafeDaManha(), updated.getCafeDaManha());
            } else {
                existing.setCafeDaManha(updated.getCafeDaManha());
            }
        }
        
        if (updated.getAlmoco() != null) {
            if (existing.getAlmoco() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAlmoco(), updated.getAlmoco());
            } else {
                existing.setAlmoco(updated.getAlmoco());
            }
        }
        
        if (updated.getLanche() != null) {
            if (existing.getLanche() != null) {
                mergeBeneficioEstruturadoDTO(existing.getLanche(), updated.getLanche());
            } else {
                existing.setLanche(updated.getLanche());
            }
        }
        
        if (updated.getLancheParada() != null) {
            if (existing.getLancheParada() != null) {
                mergeBeneficioEstruturadoDTO(existing.getLancheParada(), updated.getLancheParada());
            } else {
                existing.setLancheParada(updated.getLancheParada());
            }
        }
        
        if (updated.getValeAlimentacao() != null) {
            if (existing.getValeAlimentacao() != null) {
                mergeBeneficioEstruturadoDTO(existing.getValeAlimentacao(), updated.getValeAlimentacao());
            } else {
                existing.setValeAlimentacao(updated.getValeAlimentacao());
            }
        }
        
        if (updated.getValeAlimentacaoParada() != null) {
            if (existing.getValeAlimentacaoParada() != null) {
                mergeBeneficioEstruturadoDTO(existing.getValeAlimentacaoParada(), updated.getValeAlimentacaoParada());
            } else {
                existing.setValeAlimentacaoParada(updated.getValeAlimentacaoParada());
            }
        }
        
        if (updated.getValeRefeicao() != null) {
            if (existing.getValeRefeicao() != null) {
                mergeBeneficioEstruturadoDTO(existing.getValeRefeicao(), updated.getValeRefeicao());
            } else {
                existing.setValeRefeicao(updated.getValeRefeicao());
            }
        }
        
        if (updated.getValeRefeicaoParada() != null) {
            if (existing.getValeRefeicaoParada() != null) {
                mergeBeneficioEstruturadoDTO(existing.getValeRefeicaoParada(), updated.getValeRefeicaoParada());
            } else {
                existing.setValeRefeicaoParada(updated.getValeRefeicaoParada());
            }
        }
        
        if (updated.getCestaBasica() != null) {
            if (existing.getCestaBasica() != null) {
                mergeBeneficioEstruturadoDTO(existing.getCestaBasica(), updated.getCestaBasica());
            } else {
                existing.setCestaBasica(updated.getCestaBasica());
            }
        }
        
        if (updated.getCestaNatalina() != null) {
            if (existing.getCestaNatalina() != null) {
                mergeBeneficioEstruturadoDTO(existing.getCestaNatalina(), updated.getCestaNatalina());
            } else {
                existing.setCestaNatalina(updated.getCestaNatalina());
            }
        }
        
        // Tipo Premiações
        if (updated.getGratificacao() != null) {
            if (existing.getGratificacao() != null) {
                mergeBeneficioEstruturadoDTO(existing.getGratificacao(), updated.getGratificacao());
            } else {
                existing.setGratificacao(updated.getGratificacao());
            }
        }
        
        if (updated.getGratificacaoAbonoParada() != null) {
            if (existing.getGratificacaoAbonoParada() != null) {
                mergeBeneficioEstruturadoDTO(existing.getGratificacaoAbonoParada(), updated.getGratificacaoAbonoParada());
            } else {
                existing.setGratificacaoAbonoParada(updated.getGratificacaoAbonoParada());
            }
        }
        
        if (updated.getPlr() != null) {
            if (existing.getPlr() != null) {
                mergeBeneficioEstruturadoDTO(existing.getPlr(), updated.getPlr());
            } else {
                existing.setPlr(updated.getPlr());
            }
        }
        
        if (updated.getPlrParada() != null) {
            if (existing.getPlrParada() != null) {
                mergeBeneficioEstruturadoDTO(existing.getPlrParada(), updated.getPlrParada());
            } else {
                existing.setPlrParada(updated.getPlrParada());
            }
        }
        
        if (updated.getFlashVirtual() != null) {
            if (existing.getFlashVirtual() != null) {
                mergeBeneficioEstruturadoDTO(existing.getFlashVirtual(), updated.getFlashVirtual());
            } else {
                existing.setFlashVirtual(updated.getFlashVirtual());
            }
        }
        
        if (updated.getPremioDesempenho() != null) {
            if (existing.getPremioDesempenho() != null) {
                mergeBeneficioEstruturadoDTO(existing.getPremioDesempenho(), updated.getPremioDesempenho());
            } else {
                existing.setPremioDesempenho(updated.getPremioDesempenho());
            }
        }
        
        // Tipo Outros Benefícios
        if (updated.getAuxilioMoradia() != null) {
            if (existing.getAuxilioMoradia() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAuxilioMoradia(), updated.getAuxilioMoradia());
            } else {
                existing.setAuxilioMoradia(updated.getAuxilioMoradia());
            }
        }
        
        if (updated.getReembolsoDespesaViagem() != null) {
            if (existing.getReembolsoDespesaViagem() != null) {
                mergeBeneficioEstruturadoDTO(existing.getReembolsoDespesaViagem(), updated.getReembolsoDespesaViagem());
            } else {
                existing.setReembolsoDespesaViagem(updated.getReembolsoDespesaViagem());
            }
        }
        
        if (updated.getAjudaDeCusto() != null) {
            if (existing.getAjudaDeCusto() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAjudaDeCusto(), updated.getAjudaDeCusto());
            } else {
                existing.setAjudaDeCusto(updated.getAjudaDeCusto());
            }
        }
    }
    
    private void mergeHealthBenefitsDTO(HealthBenefitsDTO existing, HealthBenefitsDTO updated) {
        // Merge de Plano de Saúde
        if (updated.getPlanoSaude() != null) {
            if (existing.getPlanoSaude() != null) {
                mergePlanoSaudeOdontologicoDTO(existing.getPlanoSaude(), updated.getPlanoSaude());
            } else {
                existing.setPlanoSaude(updated.getPlanoSaude());
            }
        }
        
        // Merge de Plano Odontológico
        if (updated.getPlanoOdontologico() != null) {
            if (existing.getPlanoOdontologico() != null) {
                mergePlanoSaudeOdontologicoDTO(existing.getPlanoOdontologico(), updated.getPlanoOdontologico());
            } else {
                existing.setPlanoOdontologico(updated.getPlanoOdontologico());
            }
        }
        
        // Merge de Seguro de Vida
        if (updated.getSeguroVida() != null) {
            if (existing.getSeguroVida() != null) {
                mergeSeguroVidaDTO(existing.getSeguroVida(), updated.getSeguroVida());
            } else {
                existing.setSeguroVida(updated.getSeguroVida());
            }
        }
    }
    
    private void mergePlanoSaudeOdontologicoDTO(PlanoSaudeOdontologicoDTO existing, PlanoSaudeOdontologicoDTO updated) {
        if (existing == null) {
            return;
        }
        
        // Merge do BeneficioEstruturadoDTO
        if (updated.getBeneficio() != null) {
            if (existing.getBeneficio() != null) {
                mergeBeneficioEstruturadoDTO(existing.getBeneficio(), updated.getBeneficio());
            } else {
                existing.setBeneficio(updated.getBeneficio());
            }
        }
        
        // Merge dos campos específicos
        if (updated.getCobertura() != null) existing.setCobertura(updated.getCobertura());
        if (updated.getAbrangencia() != null) existing.setAbrangencia(updated.getAbrangencia());
        if (updated.getExtensividade() != null) existing.setExtensividade(updated.getExtensividade());
        if (updated.getIdadeCorteDependentes() != null) existing.setIdadeCorteDependentes(updated.getIdadeCorteDependentes());
        if (updated.getDataCorte() != null) existing.setDataCorte(updated.getDataCorte());
    }
    
    private void mergeSeguroVidaDTO(SeguroVidaDTO existing, SeguroVidaDTO updated) {
        if (existing == null) {
            return;
        }
        
        // Merge do BeneficioEstruturadoDTO
        if (updated.getBeneficio() != null) {
            if (existing.getBeneficio() != null) {
                mergeBeneficioEstruturadoDTO(existing.getBeneficio(), updated.getBeneficio());
            } else {
                existing.setBeneficio(updated.getBeneficio());
            }
        }
        
        // Merge dos campos específicos
        if (updated.getValorPremio() != null) existing.setValorPremio(updated.getValorPremio());
    }
    
    private void mergeWorkScheduleDTO(WorkScheduleDTO existing, WorkScheduleDTO updated) {
        if (updated.getHoraExtra1() != null) {
            if (existing.getHoraExtra1() != null) {
                mergeBeneficioEstruturadoDTO(existing.getHoraExtra1(), updated.getHoraExtra1());
            } else {
                existing.setHoraExtra1(updated.getHoraExtra1());
            }
        }
        
        if (updated.getHoraExtra2() != null) {
            if (existing.getHoraExtra2() != null) {
                mergeBeneficioEstruturadoDTO(existing.getHoraExtra2(), updated.getHoraExtra2());
            } else {
                existing.setHoraExtra2(updated.getHoraExtra2());
            }
        }
        
        if (updated.getHoraExtra3() != null) {
            if (existing.getHoraExtra3() != null) {
                mergeBeneficioEstruturadoDTO(existing.getHoraExtra3(), updated.getHoraExtra3());
            } else {
                existing.setHoraExtra3(updated.getHoraExtra3());
            }
        }
        
        if (updated.getBancoHoras() != null) existing.setBancoHoras(updated.getBancoHoras());
        if (updated.getControlePonto() != null) existing.setControlePonto(updated.getControlePonto());
        if (updated.getTiposContratacaoPermitidos() != null) existing.setTiposContratacaoPermitidos(updated.getTiposContratacaoPermitidos());
    }
    
    private void mergeBeneficioEstruturadoDTO(BeneficioEstruturadoDTO existing, BeneficioEstruturadoDTO updated) {
        if (existing == null) {
            return;
        }
        if (updated.getAplicavel() != null) existing.setAplicavel(updated.getAplicavel());
        if (updated.getPeriodicidade() != null) existing.setPeriodicidade(updated.getPeriodicidade());
        if (updated.getValorPercentual() != null) existing.setValorPercentual(updated.getValorPercentual());
        if (updated.getValorTipo() != null) existing.setValorTipo(updated.getValorTipo());
        if (updated.getValorReais() != null) existing.setValorReais(updated.getValorReais());
        if (updated.getObservacao() != null) existing.setObservacao(updated.getObservacao());
    }
    
    private void mergeWorkShiftTypesDTO(WorkShiftTypesDTO existing, WorkShiftTypesDTO updated) {
        if (updated.getEscala5x2() != null) existing.setEscala5x2(updated.getEscala5x2());
        if (updated.getEscala6x1() != null) existing.setEscala6x1(updated.getEscala6x1());
        if (updated.getEscala12x36() != null) existing.setEscala12x36(updated.getEscala12x36());
        if (updated.getEscala4x4() != null) existing.setEscala4x4(updated.getEscala4x4());
        if (updated.getEscala7x7() != null) existing.setEscala7x7(updated.getEscala7x7());
        if (updated.getEscalaL5811() != null) existing.setEscalaL5811(updated.getEscalaL5811());
        if (updated.getOutro() != null) existing.setOutro(updated.getOutro());
    }
    
    private void mergeContractTimeDTO(ContractTimeDTO existing, ContractTimeDTO updated) {
        if (updated.getTempo30x30() != null) existing.setTempo30x30(updated.getTempo30x30());
        if (updated.getTempo45x45() != null) existing.setTempo45x45(updated.getTempo45x45());
        if (updated.getTempo30NaoRenovaveis() != null) existing.setTempo30NaoRenovaveis(updated.getTempo30NaoRenovaveis());
        if (updated.getOutro() != null) existing.setOutro(updated.getOutro());
    }
    
    private void mergeTimeOffBenefitsDTO(TimeOffBenefitsDTO existing, TimeOffBenefitsDTO updated) {
        if (updated.getFerias() != null) existing.setFerias(updated.getFerias());
        if (updated.getLicencas() != null) existing.setLicencas(updated.getLicencas());
        if (updated.getAvisoPrevio() != null) existing.setAvisoPrevio(updated.getAvisoPrevio());
        if (updated.getEstabilidade() != null) existing.setEstabilidade(updated.getEstabilidade());
        if (updated.getAleitamentoMaterno() != null) existing.setAleitamentoMaterno(updated.getAleitamentoMaterno());
    }
    
    private void mergeHealthSafetyDTO(HealthSafetyDTO existing, HealthSafetyDTO updated) {
        if (updated.getEquipamentosProtecao() != null) existing.setEquipamentosProtecao(updated.getEquipamentosProtecao());
        if (updated.getExamesMedicos() != null) existing.setExamesMedicos(updated.getExamesMedicos());
    }
    
    private void mergeAdditionalBenefitsDTO(AdditionalBenefitsDTO existing, AdditionalBenefitsDTO updated) {
        if (updated.getAdicionalNoturno() != null) {
            if (existing.getAdicionalNoturno() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAdicionalNoturno(), updated.getAdicionalNoturno());
            } else {
                existing.setAdicionalNoturno(updated.getAdicionalNoturno());
            }
        }
        
        if (updated.getAdicionalInsalubridade() != null) {
            if (existing.getAdicionalInsalubridade() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAdicionalInsalubridade(), updated.getAdicionalInsalubridade());
            } else {
                existing.setAdicionalInsalubridade(updated.getAdicionalInsalubridade());
            }
        }
        
        if (updated.getAdicionalPericulosidade() != null) {
            if (existing.getAdicionalPericulosidade() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAdicionalPericulosidade(), updated.getAdicionalPericulosidade());
            } else {
                existing.setAdicionalPericulosidade(updated.getAdicionalPericulosidade());
            }
        }
        
        if (updated.getAdicionalSobreaviso() != null) {
            if (existing.getAdicionalSobreaviso() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAdicionalSobreaviso(), updated.getAdicionalSobreaviso());
            } else {
                existing.setAdicionalSobreaviso(updated.getAdicionalSobreaviso());
            }
        }
        
        if (updated.getAdicionalProntidao() != null) {
            if (existing.getAdicionalProntidao() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAdicionalProntidao(), updated.getAdicionalProntidao());
            } else {
                existing.setAdicionalProntidao(updated.getAdicionalProntidao());
            }
        }
    }
    
    
    private void mergeTransportationDTO(TransportationDTO existing, TransportationDTO updated) {
        // Merge de Vale Transporte
        if (updated.getValeTransporte() != null) {
            if (existing.getValeTransporte() != null) {
                mergeBeneficioEstruturadoDTO(existing.getValeTransporte(), updated.getValeTransporte());
            } else {
                existing.setValeTransporte(updated.getValeTransporte());
            }
        }
        
        // Merge de Auxílio Transporte
        if (updated.getAuxilioTransporte() != null) {
            if (existing.getAuxilioTransporte() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAuxilioTransporte(), updated.getAuxilioTransporte());
            } else {
                existing.setAuxilioTransporte(updated.getAuxilioTransporte());
            }
        }
        
        // Merge de Fretado
        if (updated.getFretado() != null) {
            if (existing.getFretado() != null) {
                mergeBeneficioEstruturadoDTO(existing.getFretado(), updated.getFretado());
            } else {
                existing.setFretado(updated.getFretado());
            }
        }
    }
    
    private void mergeSeniorityBonusDTO(SeniorityBonusDTO existing, SeniorityBonusDTO updated) {
        if (updated.getAnuenio() != null) {
            if (existing.getAnuenio() != null) {
                mergeBeneficioEstruturadoDTO(existing.getAnuenio(), updated.getAnuenio());
            } else {
                existing.setAnuenio(updated.getAnuenio());
            }
        }
    }
    
    private void mergeWorkplaceConditionsDTO(WorkplaceConditionsDTO existing, WorkplaceConditionsDTO updated) {
        if (updated.getCondicoesTrabalho() != null) existing.setCondicoesTrabalho(updated.getCondicoesTrabalho());
        if (updated.getTeletrabalhoHomeOffice() != null) existing.setTeletrabalhoHomeOffice(updated.getTeletrabalhoHomeOffice());
        if (updated.getTreinamentos() != null) existing.setTreinamentos(updated.getTreinamentos());
    }
    
    private void mergeDisciplinaryProceduresDTO(DisciplinaryProceduresDTO existing, DisciplinaryProceduresDTO updated) {
        if (updated.getMultasPenalidades() != null) existing.setMultasPenalidades(updated.getMultasPenalidades());
        if (updated.getProjecaoAviso() != null) existing.setProjecaoAviso(updated.getProjecaoAviso());
        if (updated.getTrintidio() != null) existing.setTrintidio(updated.getTrintidio());
        if (updated.getMultaEncerramentoContratoTempoServico() != null) existing.setMultaEncerramentoContratoTempoServico(updated.getMultaEncerramentoContratoTempoServico());
        if (updated.getMultaEncerramentoContratoParada() != null) existing.setMultaEncerramentoContratoParada(updated.getMultaEncerramentoContratoParada());
    }
    
    private void mergeUnionContributionsDTO(UnionContributionsDTO existing, UnionContributionsDTO updated) {
        // Merge de Contribuição Sindical do Empregado
        if (updated.getContribuicaoSindicalEmpregado() != null) {
            if (existing.getContribuicaoSindicalEmpregado() != null) {
                mergeBeneficioEstruturadoDTO(existing.getContribuicaoSindicalEmpregado(), updated.getContribuicaoSindicalEmpregado());
            } else {
                existing.setContribuicaoSindicalEmpregado(updated.getContribuicaoSindicalEmpregado());
            }
        }
        
        // Merge de Contribuição Patronal
        if (updated.getContribuicaoPatronal() != null) {
            if (existing.getContribuicaoPatronal() != null) {
                mergeBeneficioEstruturadoDTO(existing.getContribuicaoPatronal(), updated.getContribuicaoPatronal());
            } else {
                existing.setContribuicaoPatronal(updated.getContribuicaoPatronal());
            }
        }
        
        // Merge de Contribuição Patronal Educativa
        if (updated.getContribuicaoPatronalEducativa() != null) {
            if (existing.getContribuicaoPatronalEducativa() != null) {
                mergeBeneficioEstruturadoDTO(existing.getContribuicaoPatronalEducativa(), updated.getContribuicaoPatronalEducativa());
            } else {
                existing.setContribuicaoPatronalEducativa(updated.getContribuicaoPatronalEducativa());
            }
        }
    }
        
    private List<AttachmentEntity> processAttachments(List<MultipartFile> multipartFiles) {
        return attachmentService.createAttachmentsFromMultipartFiles(multipartFiles, "labor_contracts/documents");
    }
    
    private StepLog createStepLog(String name, UUID userId, String observation) {
        return StepLog.builder()
            .id(UUID.randomUUID())
            .name(name)
            .user(userId)
            .created_at(Date.from(Instant.now()))
            .final_at(Date.from(Instant.now()))
            .step(1)
            .observation(observation)
            .build();
    }
}
