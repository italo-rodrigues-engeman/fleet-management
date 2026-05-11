package com.indux.modules.advance_suppliers.application.repository;

import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersFilter;
import com.indux.modules.advance_suppliers.domain.entities.AdvanceSuppliers;
import com.indux.modules.advance_suppliers.domain.repository.CustomAdvanceSuppliersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class AdvanceSuppliersRepositoryImpl implements CustomAdvanceSuppliersRepository {
    private final MongoTemplate mongo;

    public AdvanceSuppliersRepositoryImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Page<AdvanceSuppliers> findByFilter(AdvanceSuppliersFilter filter, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();


        if (filter.status() != null && !filter.status().isEmpty()) {
            criteria.add(Criteria.where("status").in(filter.status()));
        }

        if (filter.situacoes() != null && !filter.situacoes().isEmpty()) {
            criteria.add(Criteria.where("situacao").in(filter.situacoes()));
        }

        if (filter.etapasAtuais() != null
                && !filter.etapasAtuais().isEmpty()
                && (filter.situacoes() == null || !filter.situacoes().contains("FINALIZADO"))) {
            criteria.add(Criteria.where("etapa_atual").in(filter.etapasAtuais()));
        }

        if (filter.criadoDe() != null || filter.criadoAte() != null) {
            Criteria dateCrit = Criteria.where("created_at");

            if (filter.criadoDe() != null) {
                Date dateFrom = Date.from(filter.criadoDe());
                dateCrit = dateCrit.gte(dateFrom);
            }

            if (filter.criadoAte() != null) {
                Date dateTo = Date.from(filter.criadoAte());
                dateCrit = dateCrit.lte(dateTo);
            }

            criteria.add(dateCrit);
        }

        if (filter.id() != null) {
            criteria.add(Criteria.where("codigo").is(filter.id()));
        }
        if (filter.codeId() != null) {
            criteria.add(Criteria.where("codeID").is(filter.codeId()));
        }

        if (filter.regionais() != null && !filter.regionais().isEmpty() && !filter.regionais().contains(0)) {
            criteria.add(Criteria.where("regionalId").in(filter.regionais()));
        }

        if (filter.projetos() != null && !filter.projetos().isEmpty() && !filter.projetos().contains(0)) {
            criteria.add(Criteria.where("projectId").in(filter.projetos()));
        }

        if (filter.nomeSolicitante() != null) {
            criteria.add(Criteria.where("applicant.nome").regex(filter.nomeSolicitante(), "i"));
        }

        if (filter.nomeFornecedor() != null) {
            criteria.add(Criteria.where("supplierName").regex(filter.nomeFornecedor(), "i"));
        }

        if (filter.cpf() != null) {
            criteria.add(Criteria.where("cpf").regex(filter.cpf(), "i"));
        }
        if (filter.cnpj() != null) {
            criteria.add(Criteria.where("cnpj").regex(filter.cnpj(), "i"));
        }

        if (filter.numeroContrato() != null) {
            criteria.add(Criteria.where("contractNumber").regex(filter.numeroContrato(), "i"));
        }

        if (filter.numeroPedido() != null) {
            criteria.add(Criteria.where("orderNumber").regex(filter.numeroPedido(), "i"));
        }

        if (filter.tipoDeEnvio() != null) {
            criteria.add(Criteria.where("type").is(filter.tipoDeEnvio()));
        }

        if (filter.valor() != null) {
            try {
                BigDecimal val = new BigDecimal(filter.valor());
                criteria.add(Criteria.where("value").is(val));
            } catch (NumberFormatException ignored) {
            }
        }

        if (filter.avisoRecebimento() != null) {
            criteria.add(Criteria.where("receiptNotice").regex(filter.avisoRecebimento(), "i"));
        }

        if (filter.dataPagamento() != null) {
            criteria.add(Criteria.where("paymentDate").is(filter.dataPagamento()));
        }

        if (filter.dataPrevistaNotaFiscal() != null) {
            criteria.add(Criteria.where("invoiceDate").is(filter.dataPrevistaNotaFiscal()));
        }

        if (filter.aprovacaoGestor() != null) {
            criteria.add(Criteria.where("managerApprove").is(filter.aprovacaoGestor()));
        }

        if (filter.aprovacaoFinanceiro() != null) {
            criteria.add(Criteria.where("financeApprove").is(filter.aprovacaoFinanceiro()));
        }

        Criteria combined = criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));

        Query countQuery = Query.query(combined);
        long total = mongo.count(countQuery, AdvanceSuppliers.class, "adiantamento_de_fornecedores");

        Query pagedQuery = Query.query(combined).with(pageable);
        List<AdvanceSuppliers> results = mongo.find(pagedQuery, AdvanceSuppliers.class, "adiantamento_de_fornecedores");

        return new PageImpl<>(results, pageable, total);
    }
}

