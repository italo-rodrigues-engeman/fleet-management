package com.indux.modules.modulo_mega.domain.repository.specs;

import com.indux.modules.modulo_mega.application.dto.AbcItemDTO;
import com.indux.modules.modulo_mega.application.dto.AbcFilterDTO;
import com.indux.modules.modulo_mega.domain.persistence.view.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Component
@Deprecated
public class AbcQueries {

    @PersistenceContext
    private EntityManager em;
    
    private static final String LIKE_PATTERN = "%%%s%%";

    @Deprecated
    public List<AbcItemDTO> findItemsForAbcAnalysis(AbcFilterDTO filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AbcItemDTO> query = cb.createQuery(AbcItemDTO.class);
        Root<MegaEntityOrder> root = query.from(MegaEntityOrder.class);


        

        // =============================================================================================
        // 1. PREPARAÇÃO DAS EXPRESSÕES LÓGICAS (Building Blocks)
        // =============================================================================================
        
        // --- LÓGICA DE SITUAÇÃO ---
        Predicate finalStatus = cb.or(
            cb.equal(root.get(MegaEntityOrder_.orderStatus), "Pedido Atendido"),
            cb.equal(root.get(MegaEntityOrder_.orderStatus), "Pedido Encerrado")
        );
        Predicate isDownRule = cb.and(
            cb.isNull(root.get(MegaEntityOrder_.noteNumber)),
            finalStatus
        );

        // --- LÓGICA DE TIPO DE PEDIDO (ORDER TYPE) ---
        // NÃO usar isPresent/isNotPresent aqui, usar isNull/isNotNull diretamente
        Predicate hasAP = cb.isNotNull(root.get(MegaEntityOrder_.approve));
        Predicate hasNF = cb.isNotNull(root.get(MegaEntityOrder_.noteNumber));
        Predicate hasOrder = cb.isNotNull(root.get(MegaEntityOrder_.orderNumber));
        Predicate hasSol = cb.isNotNull(root.get(MegaEntityOrder_.solicitation));

        Predicate noAP = cb.isNull(root.get(MegaEntityOrder_.approve));
        Predicate noNF = cb.isNull(root.get(MegaEntityOrder_.noteNumber));
        Predicate noOrder = cb.isNull(root.get(MegaEntityOrder_.orderNumber));
        Predicate noSol = cb.isNull(root.get(MegaEntityOrder_.solicitation));

        // Regra Padrão: 
        Predicate isStandard = cb.and(hasOrder, hasSol);

        // Regra Livre:  
        Predicate isFree = cb.and(hasOrder, noSol);

        // Regra Lançamento: 
        Predicate isLaunch = cb.and(noOrder, noSol);


        // =============================================================================================
        // 2. CRIAÇÃO DAS COLUNAS CALCULADAS (PROJEÇÃO)
        // =============================================================================================

        // Coluna "Situação"
        Expression<String> situationExpression = cb.selectCase()
            .when(isDownRule, "Baixado")
            .otherwise("Regular")
            .as(String.class);

        // Coluna "Tipo Pedido"
        Expression<String> orderTypeExpression = cb.selectCase()
            .when(isStandard, "Padrão")
            .when(isFree, "Livre")
            .when(isLaunch, "Lançamento")
            .otherwise("Indefinido")
            .as(String.class);

        // =============================================================================================
        // 3. FILTROS (WHERE)
        // =============================================================================================
        List<Predicate> predicates = new ArrayList<>();

        LocalDate startDate = filter.getStartDate();
        LocalDate endDate = filter.getEndDate();

        if (startDate == null && endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusYears(1);
        }

        if (startDate != null || endDate != null) {
            predicates.add(createDateRangePredicate(cb, root, startDate, endDate));
        }

       

        

        if (isNotEmpty(filter.getIdItem())) predicates.add(root.get(MegaEntityOrder_.idItem).in(filter.getIdItem()));
        if (isNotEmpty(filter.getStatus())) predicates.add(root.get(MegaEntityOrder_.orderStatus).in(filter.getStatus()));
        if (isNotEmpty(filter.getCategory())) predicates.add(root.get(MegaEntityOrder_.itemType).in(filter.getCategory()));

        // Filtro de Situação
        if (isNotEmpty(filter.getSituation())) {
            List<Predicate> sitPreds = new ArrayList<>();
            for (String sit : filter.getSituation()) {
                if ("Baixado".equalsIgnoreCase(sit)) sitPreds.add(isDownRule);
                else if ("Regular".equalsIgnoreCase(sit)) sitPreds.add(cb.not(isDownRule));
            }
            if (!sitPreds.isEmpty()) predicates.add(cb.or(sitPreds.toArray(Predicate[]::new)));
        }

        // Filtro de Order Type
        if (isNotEmpty(filter.getOrderType())) {
            List<Predicate> typePreds = new ArrayList<>();
            
            for (String type : filter.getOrderType()) {
                if (type == null) continue; // Proteção extra contra null na lista
                
                String t = type.trim().toUpperCase(); // Adicionado trim() para segurança
                
                // 1. PADRÃO
                if (t.contains("PADRÃO") || t.contains("PADRAO")) {
                    typePreds.add(isStandard);
                } 
                // 2. LIVRE (Correção: removida a exigência de conter "PEDIDO")
                else if (t.contains("PEDIDO") && t.contains("LIVRE")) {
                    typePreds.add(isFree);
                } 
                // 3. LANÇAMENTO
                else if (t.contains("LANÇAMENTO") || t.contains("LANCAMENTO") ) {
                    typePreds.add(isLaunch);
                } 
                // 4. INDEFINIDO
                else if (t.contains("INDEFINIDO")) {
                    // Cria a negação de todas as regras conhecidas
                    typePreds.add(cb.not(cb.or(isStandard, isFree, isLaunch)));
                }
            }
            
            // Aplica o filtro se houver algum predicado selecionado
            if (!typePreds.isEmpty()) {
                predicates.add(cb.or(typePreds.toArray(Predicate[]::new)));
            }
        }

        // Filtro de Hierarquia
        applyHierarchyFilter(cb, query, root, filter, predicates);
        

        query.where(predicates.toArray(Predicate[]::new));

        // =============================================================================================
        // 4. SELECT E GROUP BY
        // =============================================================================================
        
        
        Expression<BigDecimal> sumTotalValue = cb.sum(root.get(MegaEntityOrder_.totalItemValue));
        Expression<BigDecimal> sumQuantity = cb.sum(root.get(MegaEntityOrder_.qtdItensTotal));
        Expression<Long> countOrigin = cb.count(root.get(MegaEntityOrder_.idItem));

        query.select(cb.construct(AbcItemDTO.class,
                root.get(MegaEntityOrder_.groupCode),
                root.get(MegaEntityOrder_.groupName),
                root.get(MegaEntityOrder_.idItem),
                root.get(MegaEntityOrder_.itemName),
                root.get(MegaEntityOrder_.itemType),
                sumQuantity,        
                sumTotalValue,      
                countOrigin, 
                root.get(MegaEntityOrder_.itemStatus),

                // Preço Médio
                cb.selectCase()
                    .when(cb.or(cb.isNull(sumQuantity), cb.equal(sumQuantity, BigDecimal.ZERO)), BigDecimal.ZERO)
                    .otherwise(cb.quot(sumTotalValue, sumQuantity))
                    .as(BigDecimal.class),

                // *** NOVOS CAMPOS ***
                situationExpression, // Situação (String)
                orderTypeExpression, // Tipo Pedido (String)

                // Campos de Organograma (nulos)
                cb.literal(null), cb.literal(null), cb.literal(null), cb.literal(null), 
                cb.literal(null), cb.literal(null), cb.literal(null), cb.literal(null), 
                cb.literal(null), cb.literal(null), cb.literal(null), cb.literal(null), 
                cb.literal(null), cb.literal(null), cb.literal(null) 
        ));

        query.groupBy(
                root.get(MegaEntityOrder_.groupCode),
                root.get(MegaEntityOrder_.groupName),
                root.get(MegaEntityOrder_.idItem),
                root.get(MegaEntityOrder_.itemName),
                root.get(MegaEntityOrder_.itemType),
                root.get(MegaEntityOrder_.itemStatus),
                root.get(MegaEntityOrder_.noteNumber),
                root.get(MegaEntityOrder_.approve),
                root.get(MegaEntityOrder_.orderStatus),
                
                root.get(MegaEntityOrder_.orderNumber),
                root.get(MegaEntityOrder_.solicitation)
        );

        query.orderBy(cb.desc(sumTotalValue));

        return em.createQuery(query).getResultList();
    }

     private Predicate createDateRangePredicate(CriteriaBuilder cb, Root<MegaEntityOrder> root, LocalDate start, LocalDate end) {
        // A função coalesce tenta pegar a primeira data não nula na ordem de prioridade definida
        Expression<LocalDate> effectiveDate = cb.coalesce(root.get(MegaEntityOrder_.orderDate),
                cb.coalesce(root.get(MegaEntityOrder_.invoiceDate),
                        cb.coalesce(root.get(MegaEntityOrder_.approvalDate),
                                root.get(MegaEntityOrder_.creationDate))));

            List<Predicate> predicates = new ArrayList<>();

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(effectiveDate, start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(effectiveDate, end));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        }

    private void applyHierarchyFilter(CriteriaBuilder cb, CriteriaQuery<AbcItemDTO> query, Root<MegaEntityOrder> root, AbcFilterDTO filter, List<Predicate> predicates) {
            if (!hasHierarchyFilter(filter)) {
                return;
            }

            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<MegaEntityOrganogram> subRoot = subquery.from(MegaEntityOrganogram.class);
            subquery.select(cb.literal(1));
            
            List<Predicate> subPredicates = new ArrayList<>();
            
            // JOIN com MegaEntityOrder para conectar as tabelas
            subPredicates.add(cb.equal(subRoot.get(MegaEntityOrganogram_.projectCode), 
                                    root.get(MegaEntityOrder_.projectCode)));
            
            // Aplicar os FILTROS DE HIERARQUIA
            if (isNotEmpty(filter.getProjectCode())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.projectCode).in(filter.getProjectCode()));
            }
            
            if (isNotEmpty(filter.getContractCode())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.contractCode).in(filter.getContractCode()));
            }
            
            if (isNotEmpty(filter.getSectorId())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.sectorId).in(filter.getSectorId()));
            }
            
            if (isNotEmpty(filter.getRegionalCode())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.regionalCode).in(filter.getRegionalCode()));
            }
            
            if (isNotEmpty(filter.getSuperId())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.superId).in(filter.getSuperId()));
            }
            
            if (isNotEmpty(filter.getDirectoryId())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.directoryId).in(filter.getDirectoryId()));
            }
            
                        
            if (isNotEmpty(filter.getContractName())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.contractName).in(filter.getContractName()));
            }

            if (isNotEmpty(filter.getSectorName())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.sectorName).in(filter.getSectorName()));
            }

            if (isNotEmpty(filter.getRegionalName())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.regionalName).in(filter.getRegionalName()));
            }

            if (isNotEmpty(filter.getSuperName())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.superName).in(filter.getSuperName()));
            }

            if (isNotEmpty(filter.getDirectoryName())) {
                subPredicates.add(subRoot.get(MegaEntityOrganogram_.directoryName).in(filter.getDirectoryName()));
            }
            
            subquery.where(subPredicates.toArray(Predicate[]::new));
            predicates.add(cb.exists(subquery));
        }

    // --- MÉTODOS AUXILIARES ---
    private boolean hasHierarchyFilter(AbcFilterDTO f) {
        return isNotEmpty(f.getProjectCode()) || isNotEmpty(f.getContractCode()) || 
           isNotEmpty(f.getSectorId()) || isNotEmpty(f.getRegionalCode()) || 
           isNotEmpty(f.getSuperId()) || isNotEmpty(f.getDirectoryId()) ||
           
           isNotEmpty(f.getContractName()) || isNotEmpty(f.getSectorName()) ||
           isNotEmpty(f.getRegionalName()) || isNotEmpty(f.getSuperName()) || 
           isNotEmpty(f.getDirectoryName());
    }

    private boolean isNotEmpty(List<?> list) { 
        return list != null && !list.isEmpty(); 
    }
    
    private boolean isNotBlank(String str) { 
        return str != null && !str.isBlank(); 
    }
}