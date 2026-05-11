package com.indux.modules.modulo_mega.domain.repository;

import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntityInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MegaEntityInvoiceRepository extends JpaRepository<MegaEntityInvoice, String> {
    // Busca otimizada: traz apenas notas do item específico
    List<MegaEntityInvoice> findByIdItem(Integer idItem);
}