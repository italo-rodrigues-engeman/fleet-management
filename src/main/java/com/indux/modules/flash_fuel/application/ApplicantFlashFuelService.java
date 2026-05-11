package com.indux.modules.flash_fuel.application;

import com.indux.modules.flash_fuel.domain.dtos.ApplicantRequest;
import com.indux.modules.flash_fuel.domain.entities.ApplicantFlashFuel;
import com.indux.modules.flash_fuel.domain.repository.ApplicantFlashFuelRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ApplicantFlashFuelService {
    private final ApplicantFlashFuelRepository repository;

    public ApplicantFlashFuelService(ApplicantFlashFuelRepository repository) {
        this.repository = repository;
    }

    public ApplicantFlashFuel findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requerente não encontrado encontrado no id: " + id));
    }

    public ApplicantFlashFuel save(ApplicantRequest applicant) {
        return repository.save(ApplicantFlashFuel.fromDTO(applicant));
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Requerente não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }


    public ApplicantFlashFuel update(Long id, BigDecimal value) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Requerente não encontrado com id: " + id);
        }
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requerente não encontrado com id: " + id));
        entity.setTetoValor(value);
        return repository.save(entity);
    }

    public List<ApplicantFlashFuel> findAll() {
        return repository.findAll();
    }

    public void create(ApplicantFlashFuel applicant) {
        if (applicant.getId() != null && repository.existsByMatricula(applicant.getMatricula())) {
            throw new RuntimeException("Requerente já existe com a matricula: " + applicant.getId());
        }
        repository.save(applicant);
    }
}
