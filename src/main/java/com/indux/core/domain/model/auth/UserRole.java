package com.indux.core.domain.model.auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_permissoes_kogni")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}
