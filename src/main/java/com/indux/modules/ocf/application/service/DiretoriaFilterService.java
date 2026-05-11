package com.indux.modules.ocf.application.service;

import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.domain.entities.jpa.SimpleContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.SimpleProjectEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.SimpleContractRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.SimpleProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiretoriaFilterService {

    private final SubordinateService subordinateService;
    private final SimpleContractRepository contractRepository;
    private final SimpleProjectRepository projectRepository;

    public Set<String> getHcmIdsByFilialHcm(List<Long> filialHcmIds) {
        if (filialHcmIds == null || filialHcmIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long filialHcmId : filialHcmIds) {
                try {
                    // Para filiais HCM, buscar todos os projetos que pertencem a essa filial
                    List<SimpleProjectEntity> projects = projectRepository.findByFilialHcmId(filialHcmId);
                    
                    if (projects.isEmpty()) {
                        continue;
                    }
                    
                    List<String> hcmIds = projects.stream()
                        .map(project -> project.getHcmId().toString())
                        .filter(hcmId -> hcmId != null && !hcmId.isBlank())
                        .collect(Collectors.toList());
                    
                    allHcmIds.addAll(hcmIds);

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM IDs da filial HCM {}: {}", filialHcmId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por filiais HCM: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public Set<String> getHcmIdsByProjetos(List<Long> projetoIds) {
        if (projetoIds == null || projetoIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long projetoId : projetoIds) {
                try {
                    // Para projetos, buscar diretamente o HCM ID do projeto
                    SimpleProjectEntity project = projectRepository.findById(projetoId).orElse(null);
                    
                    if (project == null) {
                        continue;
                    }
                    
                    Integer hcmId = project.getHcmId();
                    
                    if (hcmId != null) {
                        allHcmIds.add(hcmId.toString());
                    }

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM ID do projeto {}: {}", projetoId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por projetos: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public Set<String> getHcmIdsByContratos(List<Long> contratoIds) {
        if (contratoIds == null || contratoIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long contratoId : contratoIds) {
                try {
                    var subordinatesResponse = subordinateService.getContratoWithProjects(contratoId);

                    if (subordinatesResponse == null) {
                        continue;
                    }

                    // Para contratos, os HCM IDs vêm diretamente dos projetos do contrato
                    List<String> hcmIds = subordinatesResponse.projetos().stream()
                        .map(project -> project.hcm() != null ? project.hcm().toString() : null)
                        .filter(hcmId -> hcmId != null && !hcmId.isBlank())
                        .collect(Collectors.toList());
                    
                    allHcmIds.addAll(hcmIds);

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM IDs do contrato {}: {}", contratoId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por contratos: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public Set<String> getHcmIdsBySetores(List<Long> setorIds) {
        if (setorIds == null || setorIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long setorId : setorIds) {
                try {
                    var subordinatesResponse = subordinateService.getAllSubordinatesRecursivelyBySetorId(setorId);

                    if (subordinatesResponse == null) {
                        continue;
                    }

                    List<Long> subordinateIds = subordinatesResponse.subordinatesByType().stream()
                        .flatMap(subordinateType -> subordinateType.subordinates().stream())
                        .map(subordinate -> subordinate.id())
                        .collect(Collectors.toList());
                    
                    subordinateIds.add(setorId);

                    Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);
                    
                    List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);
                    
                    allHcmIds.addAll(hcmIds);

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM IDs do setor {}: {}", setorId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por setores: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public Set<String> getHcmIdsByRegionais(List<Long> regionalIds) {
        if (regionalIds == null || regionalIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long regionalId : regionalIds) {
                try {
                    var subordinatesResponse = subordinateService.getAllSubordinatesRecursivelyByRegionalId(regionalId);

                    if (subordinatesResponse == null) {
                        continue;
                    }

                    List<Long> subordinateIds = subordinatesResponse.subordinatesByType().stream()
                        .flatMap(subordinateType -> subordinateType.subordinates().stream())
                        .map(subordinate -> subordinate.id())
                        .collect(Collectors.toList());
                    
                    subordinateIds.add(regionalId);

                    Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);
                    
                    List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);
                    
                    allHcmIds.addAll(hcmIds);

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM IDs da regional {}: {}", regionalId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por regionais: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public Set<String> getHcmIdsBySuperintendencias(List<Long> superintendenciaIds) {
        if (superintendenciaIds == null || superintendenciaIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long superintendenciaId : superintendenciaIds) {
                try {
                    var subordinatesResponse = subordinateService.getAllSubordinatesRecursivelyBySuperintendenciaId(superintendenciaId);

                    if (subordinatesResponse == null) {
                        continue;
                    }

                    List<Long> subordinateIds = subordinatesResponse.subordinatesByType().stream()
                        .flatMap(subordinateType -> subordinateType.subordinates().stream())
                        .map(subordinate -> subordinate.id())
                        .collect(Collectors.toList());
                    
                    subordinateIds.add(superintendenciaId);

                    Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);
                    
                    List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);
                    
                    allHcmIds.addAll(hcmIds);

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM IDs da superintendência {}: {}", superintendenciaId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por superintendências: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public Set<String> getHcmIdsByDiretorias(List<Long> diretoriaIds) {
        if (diretoriaIds == null || diretoriaIds.isEmpty()) {
            return Set.of();
        }

        try {
            Set<String> allHcmIds = new java.util.HashSet<>();

            for (Long diretoriaId : diretoriaIds) {
                try {
                    var subordinatesResponse = subordinateService.getAllSubordinatesRecursivelyByDirectorId(diretoriaId);

                    if (subordinatesResponse == null) {
                        continue;
                    }

                    List<Long> subordinateIds = subordinatesResponse.subordinatesByType().stream()
                        .flatMap(subordinateType -> subordinateType.subordinates().stream())
                        .map(subordinate -> subordinate.id())
                        .collect(Collectors.toList());
                    
                    subordinateIds.add(diretoriaId);

                    Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(subordinateIds);
                    
                    Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);
                    
                    List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);
                    
                    allHcmIds.addAll(hcmIds);

                } catch (Exception e) {
                    log.warn("Erro ao buscar HCM IDs da diretoria {}: {}", diretoriaId, e.getMessage());
                }
            }

            return allHcmIds;

        } catch (Exception e) {
            log.error("Erro ao buscar HCM IDs por diretorias: {}", e.getMessage(), e);
            return Set.of();
        }
    }

    public void debugHcmIdsComparison(Long diretoriaId, List<String> expectedHcmIds) {
        try {
            Set<String> generatedHcmIds = getHcmIdsByDiretorias(List.of(diretoriaId));
            
            // Verificar quais HCM IDs esperados estão faltando
            Set<String> missingHcmIds = new java.util.HashSet<>(expectedHcmIds);
            missingHcmIds.removeAll(generatedHcmIds);
            
            // Verificar quais HCM IDs extras foram gerados
            Set<String> extraHcmIds = new java.util.HashSet<>(generatedHcmIds);
            extraHcmIds.removeAll(expectedHcmIds);
            
        } catch (Exception e) {
            log.error("Erro no debug de comparação: {}", e.getMessage(), e);
        }
    }

    public void debugDiretoriaFilter(Long diretoriaId) {
        try {
            // Verificar se a diretoria existe
            var subordinatesResponse = subordinateService.getAllSubordinatesRecursivelyByDirectorId(diretoriaId);
            
            if (subordinatesResponse != null) {
                List<Long> subordinateIds = subordinatesResponse.subordinatesByType().stream()
                        .flatMap(subordinateType -> subordinateType.subordinates().stream())
                    .map(subordinate -> subordinate.id())
                    .collect(Collectors.toList());
                
                subordinateIds.add(diretoriaId);
                
                // Buscar contratos
                Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(subordinateIds);
                
                // Buscar projetos
                Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(subordinateIds);
                Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);
                
                List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);
                
                // Debug adicional: verificar se há contratos na base
                List<SimpleContractEntity> allContracts = contractRepository.findAll();
                
                // Debug adicional: verificar se há projetos na base
                List<SimpleProjectEntity> allProjects = projectRepository.findAll();
            }

        } catch (Exception e) {
            log.error("Erro no debug da diretoria {}: {}", diretoriaId, e.getMessage(), e);
        }
    }

    public String getCentroCustosIdByContractId(Long contractId) {
        if (contractId == null) {
            return null;
        }
        
        try {
            List<SimpleProjectEntity> projects = projectRepository.findByContractIdIn(List.of(contractId));
            
            if (projects.isEmpty()) {
                return null;
            }
            
            // Retorna o primeiro HCM ID encontrado (que é o centro_custos_id)
            String centroCustosId = projects.get(0).getHcmId().toString();
            
            return centroCustosId;
            
        } catch (Exception e) {
            log.warn("Erro ao buscar centro_custos_id para contrato {}: {}", contractId, e.getMessage());
            return null;
        }
    }

    public boolean isCentroCustosInFilialHcm(String centroCustosId, List<Long> filialHcmIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || filialHcmIds == null || filialHcmIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsByFilialHcm(filialHcmIds);
            
            boolean result = hcmIds.contains(centroCustosId);
            
            return result;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se centro_custos_id '{}' pertence às filiais HCM {}: {}", 
                centroCustosId, filialHcmIds, e.getMessage(), e);
            return false;
        }
    }

    public boolean isCentroCustosInProjetos(String centroCustosId, List<Long> projetoIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || projetoIds == null || projetoIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsByProjetos(projetoIds);
            
            boolean result = hcmIds.contains(centroCustosId);
            
            return result;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se centro_custos_id '{}' pertence aos projetos {}: {}", 
                centroCustosId, projetoIds, e.getMessage(), e);
            return false;
        }
    }

    public boolean isCentroCustosInContratos(String centroCustosId, List<Long> contratoIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || contratoIds == null || contratoIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsByContratos(contratoIds);
            
            boolean result = hcmIds.contains(centroCustosId);
            
            return result;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se centro_custos_id '{}' pertence aos contratos {}: {}", 
                centroCustosId, contratoIds, e.getMessage(), e);
            return false;
        }
    }

    public boolean isCentroCustosInSetores(String centroCustosId, List<Long> setorIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || setorIds == null || setorIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsBySetores(setorIds);
            
            boolean result = hcmIds.contains(centroCustosId);
            
            return result;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se centro_custos_id '{}' pertence aos setores {}: {}", 
                centroCustosId, setorIds, e.getMessage(), e);
            return false;
        }
    }

    public boolean isCentroCustosInRegionais(String centroCustosId, List<Long> regionalIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || regionalIds == null || regionalIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsByRegionais(regionalIds);
            
            boolean result = hcmIds.contains(centroCustosId);
            
            return result;
            
        } catch (Exception e) {
            log.error("Erro ao verificar se centro_custos_id '{}' pertence às regionais {}: {}", 
                centroCustosId, regionalIds, e.getMessage(), e);
            return false;
        }
    }

    public boolean isCentroCustosInSuperintendencias(String centroCustosId, List<Long> superintendenciaIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || superintendenciaIds == null || superintendenciaIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsBySuperintendencias(superintendenciaIds);
            
            boolean isInSuperintendencia = hcmIds.contains(centroCustosId);
            
            return isInSuperintendencia;

        } catch (Exception e) {
            log.warn("Erro ao verificar centro de custos {} nas superintendências: {}", centroCustosId, e.getMessage());
            return false;
        }
    }

    public boolean isCentroCustosInDiretorias(String centroCustosId, List<Long> diretoriaIds) {
        if (centroCustosId == null || centroCustosId.isBlank() || diretoriaIds == null || diretoriaIds.isEmpty()) {
            return false;
        }

        try {
            Set<String> hcmIds = getHcmIdsByDiretorias(diretoriaIds);
            
            boolean isInDiretoria = hcmIds.contains(centroCustosId);
            
            return isInDiretoria;

        } catch (Exception e) {
            log.warn("Erro ao verificar centro de custos {} nas diretorias: {}", centroCustosId, e.getMessage());
            return false;
        }
    }

    private Map<Long, List<SimpleContractEntity>> getContractsBySubordinateIds(List<Long> subordinateIds) {
        if (subordinateIds.isEmpty()) {
            return Map.of();
        }
        
        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);
        return contracts.stream()
                .collect(Collectors.groupingBy(SimpleContractEntity::getSubordinateId));
    }
    
    private Map<Long, List<SimpleProjectEntity>> getProjectsBySubordinateIds(List<Long> subordinateIds) {
        if (subordinateIds.isEmpty()) {
            return Map.of();
        }
        
        List<SimpleProjectEntity> projects = projectRepository.findBySubordinateIdInAndContractIdIsNull(subordinateIds);
        return projects.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getSubordinateId));
    }
    
    private Map<Long, List<SimpleProjectEntity>> getProjectsByContractIds(Map<Long, List<SimpleContractEntity>> contractsBySubordinate) {
        List<Long> contractIds = contractsBySubordinate.values().stream()
                .flatMap(List::stream)
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());
        
        if (contractIds.isEmpty()) {
            return Map.of();
        }
        
        List<SimpleProjectEntity> projects = projectRepository.findByContractIdIn(contractIds);
        return projects.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getContractId));
    }
    
    private List<String> collectAllHcmIds(Map<Long, List<SimpleProjectEntity>> projectsBySubordinate, 
                                         Map<Long, List<SimpleProjectEntity>> projectsByContract) {
        return java.util.stream.Stream.concat(
                projectsBySubordinate.values().stream().flatMap(List::stream),
                projectsByContract.values().stream().flatMap(List::stream)
        )
        .map(project -> project.getHcmId().toString())
        .distinct()
        .collect(Collectors.toList());
    }
}