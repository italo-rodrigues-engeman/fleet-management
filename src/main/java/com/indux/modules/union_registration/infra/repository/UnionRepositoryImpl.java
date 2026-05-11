package com.indux.modules.union_registration.infra.repository;

import com.indux.modules.union_registration.application.dto.ListUnionsFilterDTO;
import com.indux.modules.union_registration.domain.model.Union;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UnionRepositoryImpl implements com.indux.modules.union_registration.domain.repository.UnionRepositoryCustom {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public Page<Union> findAllWithFilters(ListUnionsFilterDTO filters, Pageable pageable) {
        Query query = new Query();
        
        if (filters == null) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        if (filters.getNomeCompletoSindicato() != null && !filters.getNomeCompletoSindicato().trim().isEmpty()) {
            query.addCriteria(Criteria.where("nomeCompletoSindicato").regex(filters.getNomeCompletoSindicato(), "i"));
        }
        
        if (filters.getCnpj() != null && !filters.getCnpj().trim().isEmpty()) {
            query.addCriteria(Criteria.where("cnpj").regex(filters.getCnpj(), "i"));
        }
        
        if (filters.getTipo() != null && !filters.getTipo().trim().isEmpty()) {
            query.addCriteria(Criteria.where("tipo").regex(filters.getTipo(), "i"));
        }
        
        if (filters.getCategoriaRepresentada() != null && !filters.getCategoriaRepresentada().trim().isEmpty()) {
            query.addCriteria(Criteria.where("categoriaRepresentada").regex(filters.getCategoriaRepresentada(), "i"));
        }
        
        if (filters.getUfSede() != null && !filters.getUfSede().trim().isEmpty()) {
            query.addCriteria(Criteria.where("ufSede").regex(filters.getUfSede(), "i"));
        }
        
        if (filters.getMunicipioSede() != null && !filters.getMunicipioSede().trim().isEmpty()) {
            query.addCriteria(Criteria.where("municipioSede").regex(filters.getMunicipioSede(), "i"));
        }
        
        if (filters.getSituacaoMte() != null && !filters.getSituacaoMte().trim().isEmpty()) {
            query.addCriteria(Criteria.where("situacaoMte").regex(filters.getSituacaoMte(), "i"));
        }
        
        if (filters.getPresidenteAtual() != null && !filters.getPresidenteAtual().trim().isEmpty()) {
            query.addCriteria(Criteria.where("presidenteAtual").regex(filters.getPresidenteAtual(), "i"));
        }
        
        if (filters.getStatusRegistro() != null && !filters.getStatusRegistro().trim().isEmpty()) {
            query.addCriteria(Criteria.where("statusRegistro").regex(filters.getStatusRegistro(), "i"));
        }
        
        if (filters.getCidade() != null && !filters.getCidade().trim().isEmpty()) {
            query.addCriteria(Criteria.where("cidade").regex(filters.getCidade(), "i"));
        }
        
        if (filters.getUf() != null && !filters.getUf().trim().isEmpty()) {
            query.addCriteria(Criteria.where("uf").regex(filters.getUf(), "i"));
        }
        
        if (filters.getUfAtendida() != null && !filters.getUfAtendida().trim().isEmpty()) {
            query.addCriteria(Criteria.where("ufsAtendidas").in(filters.getUfAtendida()));
        }
        
        // Contar total de documentos
        long total = mongoTemplate.count(query, Union.class);
        
        // Aplicar paginação
        query.with(pageable);
        
        // Buscar documentos
        List<Union> unions = mongoTemplate.find(query, Union.class);
        
        return new PageImpl<>(unions, pageable, total);
    }
}
