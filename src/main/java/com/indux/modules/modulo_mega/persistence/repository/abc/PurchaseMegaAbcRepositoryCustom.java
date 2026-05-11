package com.indux.modules.modulo_mega.persistence.repository.abc;

import com.indux.modules.modulo_mega.application.dto.abc.AbcItemBaseRow;
import com.indux.modules.modulo_mega.application.dto.abc.AbcItemFilter;
import java.util.List;

public interface PurchaseMegaAbcRepositoryCustom {
    List<AbcItemBaseRow> findAbcItems(AbcItemFilter filter);
}
