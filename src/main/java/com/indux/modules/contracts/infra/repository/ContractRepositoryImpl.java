package com.indux.modules.contracts.infra.repository;

import com.indux.modules.contracts.domain.model.Contract;
import com.indux.modules.contracts.domain.repository.ContractCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractCustomRepository {

    private final EntityManager entityManager;

    public Page<Contract> findAllWithFilters(
            String cliente,
            String nomeProjeto,
            String regional,
            Boolean ativo,
            String os,
            String codSap,
            String gestorInterno,
            String gestorCliente,
            Pageable pageable) {

        // Build WHERE conditions
        StringBuilder whereConditions = buildWhereConditions(
                cliente, nomeProjeto, regional, ativo, os, codSap, gestorInterno, gestorCliente);

        // Main query
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.* FROM tb_contratos c WHERE 1=1");
        sql.append(whereConditions);
        sql.append(" ORDER BY c.id");

        Query query = entityManager.createNativeQuery(sql.toString());
        setQueryParameters(query, cliente, nomeProjeto, regional, ativo, os, codSap, gestorInterno, gestorCliente);

        // Set pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Object[]> results = query.getResultList();
        
        List<Contract> contracts = results.stream()
                .map(this::mapToContract)
                .collect(Collectors.toList());

        // Count query
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(c.id) FROM tb_contratos c WHERE 1=1");
        countSql.append(whereConditions);
        
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        setQueryParameters(countQuery, cliente, nomeProjeto, regional, ativo, os, codSap, gestorInterno, gestorCliente);

        Long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(contracts, pageable, total);
    }

    private StringBuilder buildWhereConditions(
            String cliente,
            String nomeProjeto,
            String regional,
            Boolean ativo,
            String os,
            String codSap,
            String gestorInterno,
            String gestorCliente) {
        
        StringBuilder conditions = new StringBuilder();
        
        if (cliente != null) {
            conditions.append(" AND c.cliente::text ILIKE :cliente");
        }
        if (nomeProjeto != null) {
            conditions.append(" AND c.nome_projeto::text ILIKE :nomeProjeto");
        }
        if (regional != null) {
            conditions.append(" AND c.regional::text ILIKE :regional");
        }
        if (ativo != null) {
            conditions.append(" AND c.ativo = :ativo");
        }
        if (os != null) {
            conditions.append(" AND c.os::text ILIKE :os");
        }
        if (codSap != null) {
            conditions.append(" AND c.cod_sap::text ILIKE :codSap");
        }
        if (gestorInterno != null) {
            conditions.append(" AND c.gestor_interno_contrato::text ILIKE :gestorInterno");
        }
        if (gestorCliente != null) {
            conditions.append(" AND c.gestor_cliente_contrato::text ILIKE :gestorCliente");
        }
        
        return conditions;
    }

    private void setQueryParameters(Query query,
                                 String cliente,
                                 String nomeProjeto,
                                 String regional,
                                 Boolean ativo,
                                 String os,
                                 String codSap,
                                 String gestorInterno,
                                 String gestorCliente) {
        
        if (cliente != null) {
            query.setParameter("cliente", "%" + cliente + "%");
        }
        if (nomeProjeto != null) {
            query.setParameter("nomeProjeto", "%" + nomeProjeto + "%");
        }
        if (regional != null) {
            query.setParameter("regional", "%" + regional + "%");
        }
        if (ativo != null) {
            query.setParameter("ativo", ativo);
        }
        if (os != null) {
            query.setParameter("os", "%" + os + "%");
        }
        if (codSap != null) {
            query.setParameter("codSap", "%" + codSap + "%");
        }
        if (gestorInterno != null) {
            query.setParameter("gestorInterno", "%" + gestorInterno + "%");
        }
        if (gestorCliente != null) {
            query.setParameter("gestorCliente", "%" + gestorCliente + "%");
        }
    }

    public Contract findById(Long id) {
        String sql = "SELECT c.* FROM tb_contratos c WHERE c.id = :id";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id", id);
        
        List<Object[]> results = query.getResultList();
        
        if (results.isEmpty()) {
            return null;
        }
        
        return mapToContract(results.get(0));
    }

    private Contract mapToContract(Object[] row) {
        Contract contract = new Contract();
        
        contract.setId(getLongValue(row[0]));
        contract.setOs(getStringValue(row[1]));
        contract.setOsMega(getStringValue(row[2]));
        contract.setAnoOs(getStringValue(row[3]));
        contract.setCliente(getStringValue(row[4]));
        contract.setCodSap(getStringValue(row[5]));
        contract.setDataAssinatura(getLocalDateValue(row[6]));
        contract.setDataFim(getLocalDateValue(row[7]));
        contract.setPorcentagemReajuste(getBigDecimalValue(row[8]));
        contract.setMesReajuste(getStringValue(row[9]));
        contract.setAtivo(getBooleanValue(row[10]));
        contract.setTipoAditivo(getStringValue(row[11]));
        contract.setRegional(getStringValue(row[12]));
        contract.setGestorInternoContrato(getStringValue(row[13]));
        contract.setGestorClienteContrato(getStringValue(row[14]));
        contract.setValorContrato(getBigDecimalValue(row[15]));
        contract.setNomeProjeto(getStringValue(row[16]));
        contract.setDataConhecida(getBooleanValue(row[17]));
        contract.setMegaId(getLongValue(row[18]));
        contract.setFilialId(getLongValue(row[19]));
        contract.setRateioId(getLongValue(row[20]));
        contract.setNomeCentroCustos(getStringValue(row[21]));
        contract.setDescricaoEscopo(getStringValue(row[22]));
        contract.setIdDisciplina(getLongValue(row[23]));
        contract.setIdCliente(getLongValue(row[24]));
        contract.setGestorInternoCustos(getStringValue(row[25]));
        contract.setCoordenadorContrato(getStringValue(row[26]));
        contract.setEmailGestor(getStringValue(row[27]));
        contract.setEmailCoordenador(getStringValue(row[28]));
        contract.setContatoGestor(getStringValue(row[29]));
        contract.setContatoCoordenador(getStringValue(row[30]));
        contract.setDataInicio(getLocalDateValue(row[31]));
        contract.setNome(getStringValue(row[32]));
        contract.setClienteId(getLongValue(row[33]));
        contract.setPlataformaId(getLongValue(row[34]));
        contract.setIcj(getStringValue(row[35]));
        
        return contract;
    }

    private String getStringValue(Object value) {
        if (value == null) return null;
        String strValue = value.toString();
        return "NaN".equals(strValue) ? null : strValue;
    }

    private Long getLongValue(Object value) {
        if (value == null) return null;
        String strValue = value.toString();
        if ("NaN".equals(strValue)) return null;
        try {
            return Long.valueOf(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean getBooleanValue(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        String strValue = value.toString();
        if ("NaN".equals(strValue)) return null;
        return Boolean.valueOf(strValue);
    }

    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) return null;
        String strValue = value.toString();
        if ("NaN".equals(strValue)) return null;
        try {
            return new BigDecimal(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.time.LocalDate getLocalDateValue(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDate) return (java.time.LocalDate) value;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        String strValue = value.toString();
        if ("NaN".equals(strValue)) return null;
        try {
            return java.time.LocalDate.parse(strValue);
        } catch (Exception e) {
            return null;
        }
    }


} 