package com.indux.modules.calibration.domain.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "calibration_equipament")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EquipamentEntity {
    @Id
    private String id;
    
    private String name;
    private Boolean status;
    
    @DBRef
    private ManufacturerEntity manufacturer;

    @DBRef
    private List<PropertiesEntity> properties;
    
    public void setId(String id) {
        this.id = id;
    }
}