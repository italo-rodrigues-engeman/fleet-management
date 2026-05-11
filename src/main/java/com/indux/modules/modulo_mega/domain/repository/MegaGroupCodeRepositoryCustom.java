package com.indux.modules.modulo_mega.domain.repository;

import com.indux.modules.modulo_mega.application.dto.AutocompleteDTO;
import com.indux.modules.modulo_mega.application.dto.groups.GroupFilter;
import com.indux.modules.modulo_mega.domain.entities.jpa.MegaGroupCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MegaGroupCodeRepositoryCustom {
    Page<AutocompleteDTO> autocomplete(String term, Pageable pageable);
}
