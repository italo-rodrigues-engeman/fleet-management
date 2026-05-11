package com.indux.core.domain.service.generic;

import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.repository.generic.RegionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionalServiceImpl implements RegionalService {

    private final RegionalRepository regionalRepository;

    @Override
    public Optional<Regional> findByIdWithFiliais(Long id) {
        return regionalRepository.findByIdWithFiliais(id);
    }

    @Override
    public List<Regional> findAllWithFiliais() {
        return regionalRepository.findAllWithFiliais();
    }

    @Override
    public Optional<Regional> findByRegional(String regional) {
        return regionalRepository.findByRegional(regional);
    }

    @Override
    public List<Regional> findByRegionalContainingIgnoreCase(String regional) {
        return regionalRepository.findByRegionalContainingIgnoreCase(regional);
    }

    @Override
    public List<Regional> findAll() {
        return regionalRepository.findAll();
    }

    @Override
    public Regional save(Regional regional) {
        return regionalRepository.save(regional);
    }

    @Override
    public void deleteById(Long id) {
        regionalRepository.deleteById(id);
    }

    @Override
    public List<Regional> findFiliaisByRegionalId(Long regionalId) {
        Optional<Regional> regional = regionalRepository.findByIdWithFiliais(regionalId);
        return regional.map(r -> List.of(r)).orElse(List.of());
    }

    @Override
    public List<Regional> findFiliaisByRegionalName(String regionalName) {
        Optional<Regional> regional = regionalRepository.findByRegional(regionalName);
        return regional.map(r -> List.of(r)).orElse(List.of());
    }
} 