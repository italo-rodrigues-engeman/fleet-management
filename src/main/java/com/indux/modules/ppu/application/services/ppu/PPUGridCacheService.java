package com.indux.modules.ppu.application.services.ppu;

import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.domain.service.generic.RegionalService;
import com.indux.modules.clients.application.service.GetClientByIdUseCase;
import com.indux.modules.ppu.domain.entities.ppu.PPUEnhancedGridProjection;
import com.indux.modules.ppu.domain.entities.ppu.PPUEnhancedGridProjectionImpl;
import com.indux.modules.ppu.domain.entities.ppu.PPUProjection;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PPUGridCacheService {

    private final PPURepository ppuRepository;
    private final ContractProjectRepository contractProjectRepository;
    private final GetClientByIdUseCase getClientByIdUseCase;
    private final RegionalService regionalService;
    
    private final Map<Integer, String> contractNameCache = new ConcurrentHashMap<>();
    private final Map<Long, String> clientNameCache = new ConcurrentHashMap<>();
    private final Map<Long, String> regionalNameCache = new ConcurrentHashMap<>();

    public Page<PPUEnhancedGridProjection> findAllEnhancedGrid(Pageable pageable) {

        Page<PPUProjection> basicPage = ppuRepository.findAllBy(pageable);
        
        List<PPUEnhancedGridProjection> enrichedList = basicPage.getContent()
            .parallelStream()
            .map(this::enrichPPUProjection)
            .collect(Collectors.toList());
        
        return new PageImpl<>(enrichedList, pageable, basicPage.getTotalElements());
    }

       private PPUEnhancedGridProjection enrichPPUProjection(PPUProjection basic) {
        String contractName = getContractName(basic.getContract());
        String clientName = getClientName(basic.getClientId());
        String regionalName = getRegionalName(basic.getRegionalId());
        
        return PPUEnhancedGridProjectionImpl.fromBasicProjection(
            basic, regionalName, clientName, contractName);
    }

    @Cacheable(value = "contractNames", key = "#contract['id']", unless = "#result == null")
    public String getContractName(Map<String, Object> contract) {
        if (contract == null || contract.get("id") == null) {
            return "Contrato não informado";
        }
        
        try {
            Integer contractId = (Integer) contract.get("id");
            return contractNameCache.computeIfAbsent(contractId, id -> {
                return contractProjectRepository.findById(id)
                    .map(contractProject -> {
                        String projectName = contractProject.getProjectName();
                        return projectName != null ? projectName : "Nome não disponível";
                    })
                    .orElse("Contrato não encontrado");
            });
        } catch (Exception e) {
            return "Erro ao buscar contrato";
        }
    }

    @Cacheable(value = "clientNames", key = "#clientId", unless = "#result == null")
    public String getClientName(Long clientId) {
        if (clientId == null) {
            return "Cliente não informado";
        }
        
        return clientNameCache.computeIfAbsent(clientId, id -> {
            try {
                return getClientByIdUseCase.execute(id).getName();
            } catch (Exception e) {
                return "Cliente não encontrado";
            }
        });
    }

    @Cacheable(value = "regionalNames", key = "#regionalId", unless = "#result == null")
    public String getRegionalName(Long regionalId) {
        if (regionalId == null) {
            return "Regional não informada";
        }
        
        return regionalNameCache.computeIfAbsent(regionalId, id -> {
            try {
                return regionalService.findByIdWithFiliais(id)
                    .map(Regional::getRegional)
                    .orElse("Regional não encontrada");
            } catch (Exception e) {
                return "Regional não encontrada";
            }
        });
    }

    public void clearRequestCache() {
        contractNameCache.clear();
        clientNameCache.clear();
        regionalNameCache.clear();
    }

    public Map<String, Integer> getCacheStats() {
        return Map.of(
            "contractNameCache", contractNameCache.size(),
            "clientNameCache", clientNameCache.size(),
            "regionalNameCache", regionalNameCache.size()
        );
    }
}