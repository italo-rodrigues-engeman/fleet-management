package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrganizationFilialRepositoryImpl implements OrganizationFilialRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Map<String, Object>> findFilialOrganization(List<Integer> filialHCM, String search) {
        
       List<ProjectEntity> allProjects = entityManager.createQuery(
            "SELECT DISTINCT p FROM ProjectEntity p " +
            "LEFT JOIN FETCH p.filial f " +
            "LEFT JOIN FETCH p.contract c " +
            "LEFT JOIN FETCH p.subordinate ps " +
            "LEFT JOIN FETCH c.subordinate cs", 
            ProjectEntity.class)
            .getResultList();
        
        Set<Integer> filialFilter = new HashSet<>();
        if (filialHCM != null && !filialHCM.isEmpty()) {
            filialFilter.addAll(filialHCM);
        }
        
        List<Map<String, Object>> finalResults = new ArrayList<>();
        
        for (ProjectEntity project : allProjects) {
            if (!filialFilter.isEmpty()) {
                boolean hasMatchingFilial = project.getFilial().stream()
                    .anyMatch(f -> filialFilter.contains(f.getFilialId()));
                if (!hasMatchingFilial) {
                    continue;
                }
            }
            
            for (FilialHcmEntity filial : project.getFilial()) {
                if (!filialFilter.isEmpty() && !filialFilter.contains(filial.getFilialId())) {
                    continue;
                }
                
                if (search != null && !search.trim().isEmpty()) {
                    String searchTerm = search.trim();
                    String nomeFilial = filial.getNomeFilial() != null ? filial.getNomeFilial().toLowerCase() : "";
                    String searchTermLower = searchTerm.toLowerCase();
                    
                    boolean matchesId = false;
                    try {
                        Integer searchId = Integer.valueOf(searchTerm);
                        if (filial.getFilialId().equals(searchId)) {
                            matchesId = true;
                        }
                    } catch (NumberFormatException e) {
                        String idFilialStr = String.valueOf(filial.getFilialId());
                        matchesId = idFilialStr.contains(searchTerm);
                    }
                    
                    boolean matchesName = nomeFilial.contains(searchTermLower);
                    
                    if (!matchesId && !matchesName) {
                        continue;
                    }
                }
                
                Long contratoId = null;
                String nomeContrato = null;
                Long orgStartId = null;
                
                if (project.getContract() != null) {
                    contratoId = project.getContract().getId();
                    nomeContrato = project.getContract().getName();
                    if (project.getContract().getSubordinate() != null) {
                        orgStartId = project.getContract().getSubordinate().getId();
                    }
                }
                
                if (orgStartId == null && project.getSubordinate() != null) {
                    orgStartId = project.getSubordinate().getId();
                }
                
                Map<String, Object> hierarchy = buildHierarchy(orgStartId);
                
                Map<String, Object> resultRow = new LinkedHashMap<>();
                resultRow.put("idFilial", filial.getFilialId());
                resultRow.put("nomeFilial", filial.getNomeFilial());
                resultRow.put("idProjeto", project.getId());
                resultRow.put("nomeProjeto", project.getMega() != null ? project.getMega().getCusStDescricao() : null);
                resultRow.put("idContrato", contratoId);
                resultRow.put("nomeContrato", nomeContrato);
                resultRow.put("idSetor", hierarchy.get("id_setor"));
                resultRow.put("nomeSetor", hierarchy.get("nome_setor"));
                resultRow.put("idRegional", hierarchy.get("id_regional"));
                resultRow.put("nomeRegional", hierarchy.get("nome_regional"));
                resultRow.put("idSuperintendencia", hierarchy.get("id_superintendencia"));
                resultRow.put("nomeSuperintendencia", hierarchy.get("nome_superintendencia"));
                resultRow.put("idDiretoria", hierarchy.get("id_diretoria"));
                resultRow.put("nomeDiretoria", hierarchy.get("nome_diretoria"));
                
                finalResults.add(resultRow);
            }
        }
        
        Set<String> uniqueKeys = new HashSet<>();
        List<Map<String, Object>> deduplicatedResults = new ArrayList<>();
        
        for (Map<String, Object> row : finalResults) {
            String key = row.get("idFilial") + "_" + row.get("idProjeto");
            if (!uniqueKeys.contains(key)) {
                uniqueKeys.add(key);
                deduplicatedResults.add(row);
            }
        }
        
        deduplicatedResults.sort(Comparator.comparing(r -> (Integer) r.get("idFilial")));
        
        return deduplicatedResults;
    }

    private Map<String, Object> buildHierarchy(Long orgStartId) {
        Map<String, Object> hierarchy = new HashMap<>();
        if (orgStartId == null) {
            return hierarchy;
        }
        
        Set<Long> visited = new HashSet<>();
        Map<Integer, List<OrganizationNode>> nodesByType = new HashMap<>();
        
        Long currentId = orgStartId;
        int depth = 0;
        final int MAX_DEPTH = 25;
        
        while (currentId != null && depth < MAX_DEPTH && !visited.contains(currentId)) {
            visited.add(currentId);
            
            OrganizationEntity orgEntity = entityManager.find(OrganizationEntity.class, currentId);
            if (orgEntity == null) {
                break;
            }
            
            int tipo = mapOrganizationTypeToInteger(orgEntity.getType());
            nodesByType.computeIfAbsent(tipo, k -> new ArrayList<>())
                      .add(new OrganizationNode(orgEntity.getId(), orgEntity.getPosition(), depth));
            
            if (orgEntity.getSubordinate() != null) {
                currentId = orgEntity.getSubordinate().getId();
            } else {
                currentId = null;
            }
            
            depth++;
        }
        
        for (int tipo = 0; tipo <= 3; tipo++) {
            List<OrganizationNode> nodes = nodesByType.getOrDefault(tipo, new ArrayList<>());
            if (!nodes.isEmpty()) {
                nodes.sort(Comparator.comparingInt(n -> n.depth));
                OrganizationNode firstNode = nodes.get(0);
                
                switch (tipo) {
                    case 3: // SETOR
                        hierarchy.put("id_setor", firstNode.id);
                        hierarchy.put("nome_setor", firstNode.name);
                        break;
                    case 2: // REGIONAL
                        hierarchy.put("id_regional", firstNode.id);
                        hierarchy.put("nome_regional", firstNode.name);
                        break;
                    case 1: // SUPERINTENDENCIA
                        hierarchy.put("id_superintendencia", firstNode.id);
                        hierarchy.put("nome_superintendencia", firstNode.name);
                        break;
                    case 0: // DIRETORIA
                        hierarchy.put("id_diretoria", firstNode.id);
                        hierarchy.put("nome_diretoria", firstNode.name);
                        break;
                }
            }
        }
        
        return hierarchy;
    }
    

    private int mapOrganizationTypeToInteger(com.indux.modules.organization_chart.domain.entities.models.OrganizationType type) {
        if (type == null) return -1;
        
        switch (type) {
            case DIRETORIA: return 0;
            case SUPERINTENDENCIA: return 1;
            case REGIONAL: return 2;
            case SETOR: return 3;
            default: return -1;
        }
    }
    

    private static class OrganizationNode {
        Long id;
        String name;
        int depth;
        
        OrganizationNode(Long id, String name, int depth) {
            this.id = id;
            this.name = name;
            this.depth = depth;
        }
    }
}