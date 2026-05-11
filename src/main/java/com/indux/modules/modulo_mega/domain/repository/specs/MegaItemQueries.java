package com.indux.modules.modulo_mega.domain.repository.specs;

import com.indux.modules.modulo_mega.application.dto.*;
import com.indux.modules.modulo_mega.domain.persistence.view.*;
import com.indux.modules.modulo_mega.domain.entities.jpa.*;
import com.indux.modules.modulo_mega.domain.repository.jpa.MegaGroupCodeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Component
public class MegaItemQueries {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MegaGroupCodeRepository megaGroupCodeRepository;

    private static final String LIKE_PATTERN = "%%%s%%";
    private static final String POSTGRES_MASK = "FM9999999999";

    // =============================================================================================
    // CONSULTA PRINCIPAL: DETALHES DO ITEM
    // =============================================================================================

    public List<MegaEntityOrder> findByitemIdAndDateRange(Integer idItem, LocalDate startDate, LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MegaEntityOrder> query = cb.createQuery(MegaEntityOrder.class);
        Root<MegaEntityOrder> root = query.from(MegaEntityOrder.class);

        List<Predicate> predicates = new ArrayList<>();
        
        // 1. Filtro de ID do Item (Identificador único necessário)
        predicates.add(cb.equal(root.get(MegaEntityOrder_.idItem), idItem));

        // 2. Filtro de Status: REMOVIDO TOTALMENTE para trazer tudo do Subselect
        // Não filtramos por "Ativo" ou qualquer outro status.

        // 3. Filtro de Datas (Lógica "OU" - traz se qualquer data coincidir)
        if (startDate != null || endDate != null) {
            predicates.add(createDateRangePredicate(cb, root, startDate, endDate));
        }

        TypedQuery<MegaEntityOrder> tq = em.createQuery(query.where(predicates.toArray(Predicate[]::new)).distinct(true));
        
        // Bypass cache para garantir dados frescos da View/Subselect
        tq.setHint("jakarta.persistence.cache.retrieveMode", "BYPASS");
        tq.setHint("jakarta.persistence.cache.storeMode", "REFRESH");
        
        return tq.getResultList();
    }

    // Método auxiliar para garantir que se o registro tocou no range de datas em QUALQUER momento, ele vem.
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

    public List<MegaEntityOrder> findByitemId(Integer idItem) {
        return findByitemIdAndDateRange(idItem, null, null);
    }

    // =============================================================================================
    // CONSULTA DE HISTÓRICO (PAGINADA) - REVISADA
    // =============================================================================================

    public Page<ItemHistoryDetailedDTO> findItemHistoryDetails(Integer idItem, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return executePagedQuery(pageable, ItemHistoryDetailedDTO.class, MegaEntityOrder.class,
            (cb, root) -> {
                // Join LEFT é essencial para não perder dados se o organograma estiver nulo
                Join<MegaEntityOrder, MegaEntityOrganogram> h = root.join("organogram", JoinType.LEFT);
                Expression<String> situation = getSituationExpression(cb, root);
                Expression<String> orderTypeBuy = getOrderTypeBuyExpression(cb, root);

                return cb.construct(ItemHistoryDetailedDTO.class,
                    root.get(MegaEntityOrder_.idItem), root.get(MegaEntityOrder_.itemName), 
                    root.get(MegaEntityOrder_.orderNumber), root.get(MegaEntityOrder_.noteNumber), 
                    root.get(MegaEntityOrder_.orderType), root.get(MegaEntityOrder_.supplierCode),
                    root.get(MegaEntityOrder_.supplierName), root.get(MegaEntityOrder_.cnpj), 
                    root.get(MegaEntityOrder_.groupCode), root.get(MegaEntityOrder_.groupName), 
                    root.get(MegaEntityOrder_.branchId), root.get(MegaEntityOrder_.branchName),
                    root.get(MegaEntityOrder_.projectCode), root.get(MegaEntityOrder_.projectName),
                    h.get(MegaEntityOrganogram_.regionalCode), h.get(MegaEntityOrganogram_.regionalName),
                    h.get(MegaEntityOrganogram_.directoryName), h.get(MegaEntityOrganogram_.superName),
                    root.get(MegaEntityOrder_.creationDate), root.get(MegaEntityOrder_.registerUser),
                    root.get(MegaEntityOrder_.registerUserEdition), root.get(MegaEntityOrder_.solDate),
                    root.get(MegaEntityOrder_.orderDate), root.get(MegaEntityOrder_.deliveryDate),
                    root.get(MegaEntityOrder_.orderStatus), root.get(MegaEntityOrder_.itemStatus),
                    root.get(MegaEntityOrder_.averagePriceFromSupplier), root.get(MegaEntityOrder_.totalItemValue),
                    root.get(MegaEntityOrder_.requesterName), root.get(MegaEntityOrder_.buyerName),
                    h.get(MegaEntityOrganogram_.contractName), root.get(MegaEntityOrder_.qtdItensTotal),
                    root.get(MegaEntityOrder_.unitOfMeasure), root.get(MegaEntityOrder_.solicitation),
                    root.get(MegaEntityOrder_.approve), root.get(MegaEntityOrder_.itemType),
                    situation, orderTypeBuy  
                );
            },
            (cb, root) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get(MegaEntityOrder_.idItem), idItem));

                // --- ALTERAÇÃO CRÍTICA ---
                // O filtro abaixo foi REMOVIDO pois escondia itens inativos/históricos.
                // predicates.add(cb.equal(root.get(MegaEntityOrder_.itemStatus), "Ativo"));

                // Lógica de Data Permissiva:
                // Se o usuário passou datas, usamos a lógica "createDateRangePredicate" (que olha tudo)
                // ao invés de olhar apenas uma "Data Efetiva" calculada que poderia esconder eventos.
                if (startDate != null || endDate != null) {
                    predicates.add(createDateRangePredicate(cb, root, startDate, endDate));
                }

                return cb.and(predicates.toArray(Predicate[]::new));
            }
        );
    }

    // =============================================================================================
    // MÉTODOS DE AUTOCOMPLETE
    // =============================================================================================

    public Page<AutocompleteDTO> findSuppliers(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.supplierCode), root.get(MegaEntityOrder_.supplierName)),
            (cb, root) -> cb.or(createLikePredicate(cb, root.get(MegaEntityOrder_.supplierName), term), createNumericLikePredicate(cb, root.get(MegaEntityOrder_.supplierCode), term))
        );
    }

    public Page<AutocompleteDTO> findBuyer(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.buyerName), root.get(MegaEntityOrder_.buyerName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.buyerName), term)
        );
    }

    public Page<AutocompleteDTO> findRegional(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.regionalCode), root.get(MegaEntityOrganogram_.regionalName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrganogram_.regionalName), term)
        );
    }

    public Page<AutocompleteDTO> findRegionalCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.regionalCode), root.get(MegaEntityOrganogram_.regionalName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrganogram_.regionalCode), term)
        );
    }

    public Page<AutocompleteDTO> findSuper(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.superId), root.get(MegaEntityOrganogram_.superName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrganogram_.superName), term)
        );
    }

    public Page<AutocompleteDTO> findSuperCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.superId), root.get(MegaEntityOrganogram_.superName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrganogram_.superId), term)
        );
    }

    public Page<AutocompleteDTO> findDirectory(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.directoryId), root.get(MegaEntityOrganogram_.directoryName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrganogram_.directoryName), term)
        );
    }

    public Page<AutocompleteDTO> findDirectoryCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.directoryId), root.get(MegaEntityOrganogram_.directoryName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrganogram_.directoryId), term)
        );
    }

    public Page<AutocompleteDTO> findRequester(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.requesterName), root.get(MegaEntityOrder_.requesterName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.requesterName), term)
        );
    }

    public Page<AutocompleteDTO> findProject(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.projectCode), root.get(MegaEntityOrder_.projectName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.projectName), term)
        );
    }

    public Page<AutocompleteDTO> findProjectCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.projectCode), root.get(MegaEntityOrder_.projectName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrder_.projectCode), term)
        );
    }

    public Page<AutocompleteDTO> findBranchMega(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.branchName), root.get(MegaEntityOrder_.branchName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.branchName), term)
        );
    }

    public Page<AutocompleteDTO> findContract(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.contractCode), root.get(MegaEntityOrganogram_.contractName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrganogram_.contractName), term)
        );
    }

    public Page<AutocompleteDTO> findContractCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.contractCode), root.get(MegaEntityOrganogram_.contractName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrganogram_.contractCode), term)
        );
    }

    public Page<AutocompleteDTO> findSectorCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.sectorId), root.get(MegaEntityOrganogram_.sectorName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrganogram_.sectorId), term)
        );
    }

    public Page<AutocompleteDTO> findSector(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrganogram.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrganogram_.sectorId), root.get(MegaEntityOrganogram_.sectorName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrganogram_.sectorName), term)
        );
    }

    public Page<AutocompleteDTO> findItensCode(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.idItem), root.get(MegaEntityOrder_.itemName)),
            (cb, root) -> createNumericLikePredicate(cb, root.get(MegaEntityOrder_.idItem), term)
        );
    }

    public Page<AutocompleteDTO> findItensName(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.idItem), root.get(MegaEntityOrder_.itemName)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.itemName), term)
        );
    }

    public Page<AutocompleteDTO> findItens(String term, Pageable pageable) {
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            
            (cb, root) -> cb.construct(
                AutocompleteDTO.class, 
                root.get(MegaEntityOrder_.idItem), 
                root.get(MegaEntityOrder_.itemName),
                root.get(MegaEntityOrder_.itemStatus)
            ),
            // Lógica de Filtro Exclusiva
            (cb, root) -> {
                if (term != null && term.matches("\\d+")) {
                    // Se for número, filtra APENAS pelo ID exato
                    return cb.equal(root.get(MegaEntityOrder_.idItem), Long.valueOf(term));
                }

                // Se não for número, filtra por Nome OU Status
                var namePredicate = createLikePredicate(cb, root.get(MegaEntityOrder_.itemName), term);
                var statusPredicate = createLikePredicate(cb, root.get(MegaEntityOrder_.itemStatus), term);
                
                return cb.or(namePredicate, statusPredicate);
            }
        );
    }


    public Page<AutocompleteDTO> findDistinctStatuses(String term, Pageable pageable) { 
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.itemStatus), root.get(MegaEntityOrder_.itemStatus)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.itemStatus), term)
        );
    }

    public Page<AutocompleteDTO> findUnitOfMeasure(String term, Pageable pageable) { 
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.unitOfMeasure), root.get(MegaEntityOrder_.unitOfMeasure)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.unitOfMeasure), term)
        );
    }

    public Page<AutocompleteDTO> findOrderStatus(String term, Pageable pageable) { 
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.orderStatus), root.get(MegaEntityOrder_.orderStatus)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.orderStatus), term)
        );
    }

    public Page<AutocompleteDTO> findItemType(String term, Pageable pageable) { 
        return executePagedQuery(pageable, AutocompleteDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(AutocompleteDTO.class, root.get(MegaEntityOrder_.itemType), root.get(MegaEntityOrder_.itemType)),
            (cb, root) -> createLikePredicate(cb, root.get(MegaEntityOrder_.itemType), term)
        );
    }

    public Page<ItemComplementDTO> findItemsWithComplement(String term, Pageable pageable) {
        return executePagedQuery(pageable, ItemComplementDTO.class, MegaEntityOrder.class,
            (cb, root) -> cb.construct(ItemComplementDTO.class, 
                root.get(MegaEntityOrder_.idItem), 
                root.get(MegaEntityOrder_.itemName), 
                cb.literal(""),
                root.get(MegaEntityOrder_.itemStatus),
                root.get("previsaoInativacao")),
            (cb, root) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // Filtro para apenas itens com status "Ativo"
                predicates.add(cb.equal(root.get(MegaEntityOrder_.itemStatus), "Ativo"));
                
                // Filtro de busca por nome do item (se fornecido)
                if (term != null && !term.isBlank()) {
                    predicates.add(createLikePredicate(cb, root.get(MegaEntityOrder_.itemName), term));
                }
                
                return cb.and(predicates.toArray(Predicate[]::new));
            }
        );
    }

    public Page<ItemGroupedDTO> searchItemsGrouped(ItemFilter filter, Pageable pageable) {
        return executePagedQuery(pageable, ItemGroupedDTO.class, MegaEntityItens.class,
            (cb, root) -> cb.construct(ItemGroupedDTO.class,
                root.get(MegaEntityItens_.idItem), root.get(MegaEntityItens_.itemName),
                root.get(MegaEntityItens_.groupCode), root.get(MegaEntityItens_.groupName),
                root.get(MegaEntityItens_.status), root.get(MegaEntityItens_.creationDate),
                root.get(MegaEntityItens_.totalQuantity), root.get(MegaEntityItens_.averagePrice),
                root.get(MegaEntityItens_.orderCount),root.get(MegaEntityItens_.unitOfMeasure),root.get(MegaEntityItens_.itemType)),
            (cb, root) -> {
                List<Predicate> p = new ArrayList<>();
                // Apenas aplica filtros se o usuário EXPLICITAMENTE mandou no DTO.
                // Se o filtro vier null, traz tudo.
                if (filter.getTipoItem() != null && !filter.getTipoItem().isBlank()) p.add(createLikePredicate(cb, root.get(MegaEntityItens_.itemType), filter.getTipoItem()));
                if (filter.getUnidadeMedida() != null && !filter.getUnidadeMedida().isBlank()) p.add(createLikePredicate(cb, root.get(MegaEntityItens_.unitOfMeasure), filter.getUnidadeMedida()));
                if (filter.getCodigoItem() != null && !filter.getCodigoItem().isEmpty()) p.add(root.get(MegaEntityItens_.idItem).in(filter.getCodigoItem()));
                if (filter.getCodigoGrupo() != null && !filter.getCodigoGrupo().isEmpty()) p.add(root.get(MegaEntityItens_.groupCode).in(filter.getCodigoGrupo()));
                if (filter.getNomeItem() != null && !filter.getNomeItem().isBlank()) p.add(createLikePredicate(cb, root.get(MegaEntityItens_.itemName), filter.getNomeItem()));
                if (filter.getStatusItem() != null) p.add(cb.equal(root.get(MegaEntityItens_.status), filter.getStatusItem()));
                if (filter.getCadastroStartDate() != null) p.add(cb.greaterThanOrEqualTo(root.get(MegaEntityItens_.creationDate), filter.getCadastroStartDate()));
                if (filter.getCadastroEndDate() != null) p.add(cb.lessThanOrEqualTo(root.get(MegaEntityItens_.creationDate), filter.getCadastroEndDate()));
                if (filter.getPrecoMedMin() != null) p.add(cb.greaterThanOrEqualTo(root.get(MegaEntityItens_.averagePrice), filter.getPrecoMedMin()));
                if (filter.getPrecoMedMax() != null) p.add(cb.lessThanOrEqualTo(root.get(MegaEntityItens_.averagePrice), filter.getPrecoMedMax()));
                if (filter.getQtdeComprasMin() != null) p.add(cb.greaterThanOrEqualTo(root.get(MegaEntityItens_.totalQuantity), filter.getQtdeComprasMin()));
                if (filter.getQtdeComprasMax() != null) p.add(cb.lessThanOrEqualTo(root.get(MegaEntityItens_.totalQuantity), filter.getQtdeComprasMax()));

                return cb.and(p.toArray(Predicate[]::new));
            }
        );
    }

    public Page<ItemGroupResponseDTO> findItemsByGroupFilters(
        List<Integer> groupCodes, 
        List<String> groupNames, 
        String term, 
        Pageable pageable) {

        return executePagedQuery(pageable, ItemGroupResponseDTO.class, MegaEntityItens.class,
            // 1. Seleção: Note que a ordem dos campos deve ser IGUAL ao construtor do record
            (cb, root) -> cb.construct(ItemGroupResponseDTO.class, 
                root.get(MegaEntityItens_.idItem), 
                root.get(MegaEntityItens_.itemName),
                root.get(MegaEntityItens_.groupCode),
                root.get(MegaEntityItens_.groupName)
            ),
            // 2. Filtros
            (cb, root) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (groupCodes != null && !groupCodes.isEmpty()) {
                    predicates.add(root.get(MegaEntityItens_.groupCode).in(groupCodes));
                }

                if (groupNames != null && !groupNames.isEmpty()) {
                    predicates.add(root.get(MegaEntityItens_.groupName).in(groupNames));
                }

                if (term != null && !term.isBlank()) {
                    predicates.add(cb.like(cb.upper(root.get(MegaEntityItens_.itemName)), 
                            "%" + term.toUpperCase() + "%"));
                }

                return cb.and(predicates.toArray(Predicate[]::new));
            }
        );
    }

    // =============================================================================================
    // MOTOR DE CONSULTA E AUXILIARES
    // =============================================================================================

    private <T, E> Page<T> executePagedQuery(Pageable p, Class<T> resCls, Class<E> entCls, BiFunction<CriteriaBuilder, Root<E>, Selection<T>> selPrv, BiFunction<CriteriaBuilder, Root<E>, Predicate> predPrv) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(resCls);
        Root<E> root = query.from(entCls);
        query.select(selPrv.apply(cb, root)).distinct(true).where(predPrv.apply(cb, root));
        if (p.getSort().isSorted()) {
            query.orderBy(p.getSort().stream().map(s -> s.isAscending() ? cb.asc(root.get(s.getProperty())) : cb.desc(root.get(s.getProperty()))).toList());
        }
        TypedQuery<T> tq = em.createQuery(query);
        tq.setHint("jakarta.persistence.cache.retrieveMode", "BYPASS");
        tq.setHint("jakarta.persistence.cache.storeMode", "REFRESH");

        if (p.isPaged()) {
            tq.setFirstResult((int) p.getOffset());
            tq.setMaxResults(p.getPageSize());
        }

        List<T> content = tq.getResultList();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<E> cr = cq.from(entCls);
        cq.select(cb.countDistinct(cr)).where(predPrv.apply(cb, cr));
        return new PageImpl<>(content, p, em.createQuery(cq).getSingleResult());
    }

   

    private Predicate createLikePredicate(CriteriaBuilder cb, Expression<String> path, String term) {
        return cb.like(cb.upper(path), String.format(LIKE_PATTERN, term.toUpperCase()));
    }

    private Predicate createNumericLikePredicate(CriteriaBuilder cb, Expression<?> path, String term) {
        return cb.like(cb.function("TO_CHAR", String.class, path, cb.literal(POSTGRES_MASK)), String.format(LIKE_PATTERN, term.toUpperCase()));
    }

    private Expression<String> getSituationExpression(CriteriaBuilder cb, Root<MegaEntityOrder> root) {
        // Classificação apenas visual (Case When), não filtra registros. OK.
        Predicate validStatus = cb.or(cb.equal(root.get(MegaEntityOrder_.orderStatus), "Pedido Atendido"), cb.equal(root.get(MegaEntityOrder_.orderStatus), "Pedido Encerrado"));
        Predicate isRegular = cb.and(isPresent(cb, root.get(MegaEntityOrder_.noteNumber)), validStatus);
        return cb.selectCase().when(isRegular, "Regular").otherwise("Baixado").as(String.class);
    }

    private Predicate isPresent(CriteriaBuilder cb, Expression<?> path) {
        Predicate isNotNull = cb.isNotNull(path);
        Class<?> type = path.getJavaType();

        if (type != null && Number.class.isAssignableFrom(type)) {
            return cb.and(isNotNull, cb.notEqual(path, 0));
        }
        
        // Se já for String, evita o .as(String.class) desnecessário
        Expression<String> stringPath = (type == String.class) ? (Expression<String>) path : path.as(String.class);
        
        return cb.and(isNotNull, 
                    cb.notEqual(stringPath, "0"), 
                    cb.notEqual(stringPath, ""));
    }

    private Expression<String> getOrderTypeBuyExpression(CriteriaBuilder cb, Root<MegaEntityOrder> root) {
        Predicate hasAP = isPresent(cb, root.get(MegaEntityOrder_.approve));
        Predicate hasNF = isPresent(cb, root.get(MegaEntityOrder_.noteNumber));
        Predicate hasOrder = isPresent(cb, root.get(MegaEntityOrder_.orderNumber));
        Predicate hasSol = isPresent(cb, root.get(MegaEntityOrder_.solicitation));
        
        Predicate noOrder = cb.not(hasOrder);
        Predicate noSol = cb.not(hasSol);
        Predicate noNF = cb.not(hasNF);
        Predicate noAP = cb.not(hasAP);

        return cb.selectCase()
            .when(cb.and(hasOrder, hasSol), "Pedido Padrão")
            .when(cb.and(hasOrder, noSol), "Pedido Livre")
            // Simplificação das condições de Lançamento Livre (Agrupando a lógica de NF e AP)
            .when(cb.and(noOrder, noSol, cb.or(hasAP, hasNF)), "Lançamento Livre")
            .otherwise("Outros").as(String.class);
    }
}