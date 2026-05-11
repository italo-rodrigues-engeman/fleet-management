package com.indux.modules.purchase_occurrence.application;

import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrenceCauses;
import com.indux.modules.purchase_occurrence.domain.repository.PurchaseOccurrenceCausesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseOccurrenceCausesService {
    private final PurchaseOccurrenceCausesRepository repository;

    public PurchaseOccurrenceCausesService(PurchaseOccurrenceCausesRepository repository) {
        this.repository = repository;
    }

    public List<PurchaseOccurrenceCauses> fetchAll() {
        return repository.findAll();
    }

    public void create(PurchaseOccurrenceCauses dto) {
        var item = new PurchaseOccurrenceCauses();
        item.setDescricao(dto.getDescricao());
        repository.save(item);
    }

    public void deleteComplaintType(Long id) {
        if (id == null) throw new IllegalArgumentException("Você deve setar a causa que deseja deletar.");
        repository.deleteById(id);
    }

}
