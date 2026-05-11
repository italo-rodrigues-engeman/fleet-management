package com.indux.modules.modulo_mega.application.services;

import com.indux.modules.modulo_mega.application.dto.groups.GroupFilter;
import com.indux.modules.modulo_mega.application.dto.groups.MegaGroupResponse;
import com.indux.modules.modulo_mega.application.mapper.MegaGroupMapper;
import com.indux.modules.modulo_mega.domain.repository.specs.MegaGroupSpecification;
import com.indux.modules.modulo_mega.domain.repository.jpa.MegaGroupCodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class MegaGroupService {
    private final MegaGroupCodeRepository repository;
    private final MegaGroupMapper mapper;

    public MegaGroupService(MegaGroupCodeRepository repository, MegaGroupMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<MegaGroupResponse> getAll(Pageable pageable, GroupFilter filter) {
        return repository.findAll(MegaGroupSpecification.filterBy(filter), pageable)
                .map(mapper::toResponse);
    }

}
