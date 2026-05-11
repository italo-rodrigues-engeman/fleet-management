package com.indux.modules.modulo_mega.domain.repository;

import com.indux.modules.modulo_mega.domain.entity.abc.AbcFilter;
import com.indux.modules.modulo_mega.domain.entity.abc.AbcItem;
import java.util.List;

public interface AbcItemGateway {
    List<AbcItem> findItemsForAbcAnalysis(AbcFilter filter);
}
