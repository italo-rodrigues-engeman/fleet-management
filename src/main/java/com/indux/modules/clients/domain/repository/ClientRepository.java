package com.indux.modules.clients.domain.repository;

import com.indux.modules.clients.domain.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    boolean existsByCnpj(String cnpj);

    @Query(value = "SELECT DISTINCT c.* FROM tb_clientes c " +
           "LEFT JOIN tb_fiscais_cliente f ON c.id = f.cliente_id " +
           "WHERE (:clientId IS NULL OR c.id = :clientId) " +
           "AND (:enderecoPlanta IS NULL OR c.endereco_cliente::text ILIKE '%' || :enderecoPlanta || '%') " +
           "AND (:name IS NULL OR c.nome::text ILIKE '%' || :name || '%') " +
           "AND (:fiscal IS NULL OR f.nome::text ILIKE '%' || :fiscal || '%') " +
           "AND (:cnpj IS NULL OR regexp_replace(c.cnpj::text, '[^0-9]', '', 'g') ILIKE '%' || regexp_replace(:cnpj, '[^0-9]', '', 'g') || '%') " +
           "AND (:cidade IS NULL OR c.cidade::text ILIKE '%' || :cidade || '%') " +
           "AND (:estado IS NULL OR c.estado::text ILIKE '' || :estado || '') " +
           "AND (:tipoCliente IS NULL OR c.mercado::text ILIKE '' || :tipoCliente || '') " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:dueDiligentes IS NULL OR c.due_diligentes = :dueDiligentes) " +
           "AND (:ramo IS NULL OR c.ramo::text ILIKE '%' || :ramo || '%') " +
           "ORDER BY c.id", 
           countQuery = "SELECT COUNT(DISTINCT c.id) FROM tb_clientes c " +
           "LEFT JOIN tb_fiscais_cliente f ON c.id = f.cliente_id " +
           "WHERE (:clientId IS NULL OR c.id = :clientId) " +
           "AND (:enderecoPlanta IS NULL OR c.endereco_cliente::text ILIKE '%' || :enderecoPlanta || '%') " +
           "AND (:name IS NULL OR c.nome::text ILIKE '%' || :name || '%') " +
           "AND (:fiscal IS NULL OR f.nome::text ILIKE '%' || :fiscal || '%') " +
           "AND (:cnpj IS NULL OR regexp_replace(c.cnpj::text, '[^0-9]', '', 'g') ILIKE '%' || regexp_replace(:cnpj, '[^0-9]', '', 'g') || '%') " +
           "AND (:cidade IS NULL OR c.cidade::text ILIKE '%' || :cidade || '%') " +
           "AND (:estado IS NULL OR c.estado::text ILIKE '' || :estado || '') " +
           "AND (:tipoCliente IS NULL OR c.mercado::text ILIKE '' || :tipoCliente || '') " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:dueDiligentes IS NULL OR c.due_diligentes = :dueDiligentes) " +
           "AND (:ramo IS NULL OR c.ramo::text ILIKE '%' || :ramo || '%')",
           nativeQuery = true)
    Page<Client> findAllWithFilters(
            @Param("enderecoPlanta") String enderecoPlanta,
            @Param("fiscal") String fiscal,
            @Param("cnpj") String cnpj,
            @Param("cidade") String cidade,
            @Param("estado") String estado,
            @Param("tipoCliente") String tipoCliente,
            @Param("status") String status,
            @Param("name") String name,
            @Param("clientId") Long clientId,
            @Param("dueDiligentes") Boolean dueDiligentes,
            @Param("ramo") String ramo,
            Pageable pageable);
} 