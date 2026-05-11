package com.indux.modules.calibration.domain.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "calibration_properties")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PropertiesEntity {
    @Id
    private String id;
    
    private String name;
    private Boolean status;
    
    public void setId(String id) {
        this.id = id;
    }
}