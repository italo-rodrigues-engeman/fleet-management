package com.indux.modules.crm.persistence.gateway;

import com.indux.modules.crm.application.dto.filter.EngemanAgentFilter;
import com.indux.modules.crm.application.dto.response.EngemanAgentResponse;
import com.indux.modules.crm.application.gateway.EngemanAgentFilterGateway;
import com.indux.modules.crm.application.mapper.EngemanAgentMapper;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.domain.gateway.EngemanAgentGateway;
import com.indux.modules.crm.persistence.model.CommissionModel;
import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import com.indux.modules.crm.persistence.repository.EngemanAgent.EngemanAgentRepository;
import com.indux.modules.crm.persistence.repository.commission.CommissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EngemanAgentGatewayImpl implements EngemanAgentGateway, EngemanAgentFilterGateway {

    private final EngemanAgentRepository repository;
    private final CommissionRepository commissionRepository;
    private final EngemanAgentMapper mapper;

    public EngemanAgentGatewayImpl(EngemanAgentRepository repository, CommissionRepository commissionRepository, EngemanAgentMapper mapper) {
        this.repository = repository;
        this.commissionRepository = commissionRepository;
        this.mapper = mapper;
    }

    @Override
    public EngemanAgent save(EngemanAgent entity) {
        Optional<EngemanAgentModel> exists = repository.findByCpf(entity.getCpf());

        if (exists.isPresent()) {
            throw new RuntimeException("Representante já cadastrado");
        }

        EngemanAgentModel model = mapper.fromEntity(entity);

        List<CommissionModel> commissions = model.getCommissions();
        model.setCommissions(null);
        EngemanAgentModel savedModel = repository.save(model);

        if (commissions != null && !commissions.isEmpty()) {
            final EngemanAgentModel finalSavedModel = savedModel;
            commissions.forEach(commission -> {
                commission.setEngemanAgent(finalSavedModel);
            });

            List<CommissionModel> savedCommissions = commissionRepository.saveAll(commissions);

            savedModel.setCommissions(savedCommissions);
            savedModel = repository.save(savedModel);
        }

        return mapper.fromModel(savedModel);
    }

    @Override
    public EngemanAgent update(EngemanAgent agent, String id) {
        EngemanAgentModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agente não encontrado"));

        if (agent.getName() != null) exists.setName(agent.getName());
        if (agent.getCpf() != null) exists.setCpf(agent.getCpf());
        if (agent.getCep() != null) exists.setCep(agent.getCep());
        if (agent.getAddress() != null) exists.setAddress(agent.getAddress());
        if (agent.getState() != null) exists.setState(agent.getState());
        if (agent.getCity() != null) exists.setCity(agent.getCity());
        if (agent.getMainEmail() != null) exists.setMainEmail(agent.getMainEmail());
        if (agent.getAlternativeEmail() != null) exists.setAlternativeEmail(agent.getAlternativeEmail());
        if (agent.getMainNumber() != null) exists.setMainNumber(agent.getMainNumber());
        if (agent.getAlternativeNumber() != null) exists.setAlternativeNumber(agent.getAlternativeNumber());
        if (agent.getStatus() != null) exists.setStatus(agent.getStatus());

        if (agent.getCommissions() != null) {
            List<CommissionModel> updatedCommissions = mapper.fromEntity(agent).getCommissions();

            updatedCommissions.forEach(commission -> commission.setEngemanAgent(exists));

            updatedCommissions = commissionRepository.saveAll(updatedCommissions);

            exists.setCommissions(updatedCommissions);
        }

        if (agent.getAttachments() != null && !agent.getAttachments().isEmpty()) {
            if (exists.getAttachments() == null) {
                exists.setAttachments(agent.getAttachments());
            } else {
                exists.getAttachments().addAll(agent.getAttachments());
            }
        }

        repository.save(exists);
        return mapper.fromModel(exists);
    }

    @Override
    public EngemanAgent getById(String id) {
        EngemanAgentModel model = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Representante não encontrado"));
        return mapper.fromModel(model);
    }

    @Override
    public Page<EngemanAgent> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::fromModel);
    }

    @Override
    public Page<EngemanAgentResponse> filter(EngemanAgentFilter filter, Pageable pageable) {
        return repository.filter(filter, pageable).map(mapper::modelToResponse);
    }

    @Override
    public void toggleStatus(String id) {
        EngemanAgentModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Representante não encontrado"));

        exists.toggleStatus();
        repository.save(exists);
    }
}