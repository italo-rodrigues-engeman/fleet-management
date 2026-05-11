package com.indux.core.application.service.generic;

import com.indux.core.domain.model.employee.Filial;
import com.indux.core.domain.repository.generic.BranchRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    private final BranchRepository repository;

    public BranchService(BranchRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "branches", key = "'all'")
    public List<Filial> getAllFiliais() {
        return repository.findAll();
    }

    public Filial getBranchByID(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Não foi possível encontrar uma filial."));
    }

    public Page<Filial> searchBranches(String search, Pageable pageable) {
        return repository.findWithSearch(search, pageable);
    }
}
