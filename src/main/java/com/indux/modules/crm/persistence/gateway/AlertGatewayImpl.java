package com.indux.modules.crm.persistence.gateway;

import com.indux.modules.crm.application.gateway.AlertGateway;
import com.indux.modules.crm.domain.entity.Alert;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.persistence.model.AlertModel;
import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import com.indux.modules.crm.persistence.repository.alert.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlertGatewayImpl implements AlertGateway {

    private final AlertRepository alertRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Alert> findPendingAlertsForDate(LocalDate date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("date").is(date)
            .andOperator(new Criteria().orOperator(
                Criteria.where("sent").is(false),
                Criteria.where("sent").exists(false),
                Criteria.where("sent").is(null)
            ))
        );
        
        List<AlertModel> models = mongoTemplate.find(query, AlertModel.class);
        
        return models.stream().map(model -> {
            Alert alert = new Alert();
            alert.setId(model.getId());
            alert.setDate(model.getDate());
            alert.setFutureActionDescription(model.getFutureActionDescription());
            alert.setSent(model.getSent() != null ? model.getSent() : false);
            
            if (model.getEngemanAgent() != null) {
                EngemanAgent agent = new EngemanAgent();
                agent.setId(model.getEngemanAgent().getId());
                agent.setName(model.getEngemanAgent().getName());
                agent.setMainEmail(model.getEngemanAgent().getMainEmail());
                alert.setEngemanAgent(agent);
            }
            
            return alert;
        }).collect(Collectors.toList());
    }

    @Override
    public void markAsSent(String alertId) {
        alertRepository.findById(alertId).ifPresent(model -> {
            model.setSent(true);
            alertRepository.save(model);
        });
    }
}
