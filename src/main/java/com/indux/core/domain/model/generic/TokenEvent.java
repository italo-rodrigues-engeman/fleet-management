package com.indux.core.domain.model.generic;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.util.UUID;

@EnableJpaAuditing
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "tb_tokens_event")
public class TokenEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id")
    private UUID id;
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(name = "expire_at")
    private Instant expireAt;
    private UUID userID;
    private boolean used;
    @Column(name = "token", unique = true)
    private Integer token;
    @ManyToOne(fetch = FetchType.EAGER)  // Remover o CascadeType.ALL
    @JoinTable(
            name = "tb_token_event_join",
            joinColumns = @JoinColumn(name = "token_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private TokenEventType type;

}
