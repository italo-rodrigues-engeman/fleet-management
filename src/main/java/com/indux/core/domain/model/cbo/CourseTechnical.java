package com.indux.core.domain.model.cbo;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "course_technical")
public class CourseTechnical {
    @Id
    private String id;
    @Field("nome")
    private String name;
}
