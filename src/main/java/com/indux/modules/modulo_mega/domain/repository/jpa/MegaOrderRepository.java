package com.indux.modules.modulo_mega.domain.repository.jpa;

import com.indux.modules.modulo_mega.domain.entities.jpa.MegaOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MegaOrderRepository extends JpaRepository<MegaOrder, String> {
}
