package com.indux.core.domain.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.indux.core.domain.model.auth.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_perfil_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @Column(name = "user_id")
    @JsonIgnore
    private UUID userId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    @Column(columnDefinition = "TEXT", name = "descricao", nullable = true)
    private String description;
    @Column(length = 255, nullable = true)
    private String instagram;
    @Column(length = 255, nullable = true)
    private String twitter;
    @Column(length = 255, nullable = true)
    private String facebook;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}