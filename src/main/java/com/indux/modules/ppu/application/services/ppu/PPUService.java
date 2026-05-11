package com.indux.modules.ppu.application.services.ppu;

import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.clients.application.dto.ClientDTO;
import com.indux.modules.clients.application.service.GetClientByIdUseCase;
import com.indux.modules.ppu.application.dtos.PPUFilter;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.PPUWithClientNameDTO;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOCoordinatorRequest;
import com.indux.modules.ppu.application.dtos.requests.PPURequest;
import com.indux.modules.ppu.application.projection.PPUPlatforms;
import com.indux.modules.ppu.application.assembler.PPUAssembler;
import com.indux.modules.ppu.application.services.ppu.total_balance.TotalBalanceUseCase;
import com.indux.modules.ppu.domain.entities.jpa.DatabaseSequencePPU;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.repositories.jpa.DatabaseSequencePPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.entities.ppu.AuditableLineConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PPUService {
    private final PPURepository repository;
    private final DatabaseSequencePPURepository databaseSequence;
    private final ContractProjectRepository contractProjectRepository;
    private final FetchPPUUseCase fetchPPUUseCase;
    private final GetClientByIdUseCase getClientByIdUseCase;
    private final TotalBalanceUseCase totalBalanceUseCase; //todo: refactor para refletir o caso real.
    private final PPUAssembler ppuAssembler;

    public PPUService(PPURepository repository, DatabaseSequencePPURepository databaseSequence, ContractProjectRepository contractProjectRepository, FetchPPUUseCase fetchPPUUseCase, GetClientByIdUseCase getClientByIdUseCase, TotalBalanceUseCase totalBalanceUseCase, PPUAssembler ppuAssembler) {
        this.repository = repository;
        this.databaseSequence = databaseSequence;
        this.contractProjectRepository = contractProjectRepository;
        this.fetchPPUUseCase = fetchPPUUseCase;
        this.getClientByIdUseCase = getClientByIdUseCase;
        this.totalBalanceUseCase = totalBalanceUseCase;
        this.ppuAssembler = ppuAssembler;
    }


    /**
     * Cria uma PPU
     *
     * @return Código da PPU e legível para o usuário.
     */
    @Transactional
    public String create(PPUEntity entity, Integer contractId) {
        Map<String, Object> contract = contractProjectRepository.findById(contractId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Contrato não encontrado com ID: " + contractId))
                .toDTO();
        var sequence = databaseSequence.save(new DatabaseSequencePPU());
        entity.setContract(contract);
        entity.setContractId(contract.get("id") != null ? Long.parseLong(contract.get("id").toString()) : 0L);
        entity.setCodeID(sequence.getId());
        PPUEntity saved = repository.save(entity);
        sequence.setDocumentId(saved.getId());
        databaseSequence.save(sequence);
        return saved.getId();
    }

    /**
     * Busca a PPU ativa para a plataforma do usuário atual e a atribui colaboradores para cada serviços
     *
     * @param userID ID do usuário.
     * @param when   Data solicitada.
     * @return A PPUEntity ativa com funcionários atribuídos aos seus serviços.
      * @throws IllegalArgumentException Se nenhuma PPU for encontrada para a plataforma do usuário.
     */
    public PPUResponse fetchCurrentPPUBoarded(UUID userID, LocalDate when) throws ExecutionException, InterruptedException {
        return fetchPPUUseCase.fetchForSupervisor(userID, when);
    }

    /**
     * Busca a PPU ativa para a plataforma do usuário atual e a atribui colaboradores para cada serviços
     *
     * @param userID  ID do usuário.
     * @param when    Data solicitada.
     * @param request Informações passadas por um coordenador.
     * @return A PPUEntity ativa com funcionários atribuídos aos seus serviços.
     * @throws IOException              Se houver um problema ao buscar os dados de embarque.
     */
    public PPUResponse fetchCurrentPPUCoordinator(UUID userID, LocalDate when, CreateRDOCoordinatorRequest request) throws IOException {
        return fetchPPUUseCase.fetchForCoordinator(userID, when, request);
    }

    /**
     * Atualiza uma PPU existente com os novos dados fornecidos.
     * A atualização é parcial, ou seja, apenas os campos não nulos serão atualizados.
     *
     * @param id           ID da PPU a ser atualizada
     * @param updateRecord Dados para atualização
     * @return PPU atualizada
     */
    @Transactional
    public PPUEntity updatePPU(String id, PPURequest updateRecord, String user) {
        PPUEntity existingPPU = findPPUById(id);
        updateContractIfProvided(existingPPU, updateRecord);
        ppuAssembler.applyUpdate(existingPPU, updateRecord);
        existingPPU.setUpdatedBy(user);
        existingPPU.setUpdatedAt(LocalDateTime.now());
        return repository.save(existingPPU);
    }

    private PPUEntity findPPUById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada com ID: " + id));
    }

    private void updateContractIfProvided(PPUEntity ppu, PPURequest updateRecord) {
        if (updateRecord.contratoID() != null) {
            Map<String, Object> contract = contractProjectRepository.findById(updateRecord.contratoID())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Contrato não encontrado com ID: " + updateRecord.contratoID())).toDTO();
            ppu.setContract(contract);
        }
    }

    public Page<PPUEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<PPUWithClientNameDTO> filterPPUsWithClientName(PPUFilter filter, Pageable pageable) {
        Page<PPUEntity> ppuPage = repository.findByFilters(
                filter.getBranch(),
                filter.getContractId(),
                filter.getPlatform(),
                filter.getStatus(),
                filter.getCreatedBy(),
                pageable);
        return ppuPage.map(this::enrichPPUWithClientName);
    }

    private PPUWithClientNameDTO enrichPPUWithClientName(PPUEntity ppu) {
        try {
            if (ppu.getClientId() != null) {
                ClientDTO client = getClientByIdUseCase.execute(ppu.getClientId());
                return PPUWithClientNameDTO.fromPPUEntity(ppu, client.getName());
            } else {
                return PPUWithClientNameDTO.fromPPUEntity(ppu, "Cliente não informado");
            }
        } catch (Exception e) {
            return PPUWithClientNameDTO.fromPPUEntity(ppu, "Cliente não encontrado");
        }
    }

    public PPUEntity findById(String id, Boolean fetchTotalBalance) {
        var ppu = repository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada com ID: " + id));
        if (fetchTotalBalance) {
            var totalBalance = totalBalanceUseCase.fetchTotalBalance(ppu);
            ppu.setTotalBalances(totalBalance);
            return ppu;
        }
        return ppu;
    }

    public PPUPlatforms findPlatformsByProject(Long id){
        return repository.getByProjectId(id);
    }

    public PPUEntity save(PPUEntity ppuEntity) {
        return repository.save(ppuEntity);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public void updateNames(){
        var all = repository.findAll();
        for(var ppu : all){
            for(var service : ppu.getServices()){
                service.setAuditableLines(List.of(AuditableLineConfig.builder().label(service.getName()).build()));
            }
            for(var equip : ppu.getEquipments()) {
                equip.setAuditableLines(List.of(AuditableLineConfig.builder().label(equip.getName()).build()));
            }
            for(var cable : ppu.getSteelCables()){
                cable.setAuditableLines(List.of(AuditableLineConfig.builder().label(cable.getName()).build()));
            }
            for(var kit : ppu.getAccessoryKits()){
                kit.setAuditableLines(List.of(AuditableLineConfig.builder().label(kit.getName()).build()));
            }
        }
        repository.saveAll(all);
    }

}
