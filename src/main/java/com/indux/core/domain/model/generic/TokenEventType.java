package com.indux.core.domain.model.generic;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tb_token_type_event")
@Getter
@Setter
public class TokenEventType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long id;
    private String name;

    public static enum Values {
        FORGET_PASSWORD(2L),
        FIRST_ACCESS(1L);
        final long id;

        Values(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    public Instant quantityDate() {
        return switch (name) {
            case "FORGET_PASSWORD" -> Instant.now().plus(1, ChronoUnit.HOURS);
            case "FIRST_ACCESS" -> Instant.now().plus(30, ChronoUnit.DAYS);
            default -> Instant.now().plus(24, ChronoUnit.HOURS);
        };
    }

}
