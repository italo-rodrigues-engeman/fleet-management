package com.indux.modules.modulo_mega.application.services;

import com.indux.modules.modulo_mega.application.dto.items.MegaItemFilter;
import com.indux.modules.modulo_mega.application.dto.items.MegaItemResponse;
import com.indux.modules.modulo_mega.application.mapper.MegaItemMapper;
import com.indux.modules.modulo_mega.domain.repository.jpa.MegaItemRepository;
import com.indux.modules.modulo_mega.domain.repository.specs.MegaItemSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class MegaItemTableService {

    private final MegaItemRepository repository;
    private final MegaItemMapper mapper;

    public MegaItemTableService(MegaItemRepository repository, MegaItemMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<MegaItemResponse> getAll(Pageable pageable, MegaItemFilter filter) {
        return repository.findAll(MegaItemSpecification.filterBy(filter), pageable)
                .map(mapper::toResponse);
    }
}
