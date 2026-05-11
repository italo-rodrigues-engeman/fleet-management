package com.indux.core.domain.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.indux.core.domain.model.user.UserFavoriteModule;
import com.indux.core.domain.model.user.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_usuarios")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    @Column(name = "nome")
    private String name;
    @Column(name = "cpf", unique = true)
    private String cpf;
    @Column(name = "email")
    private String email;
    @Column(name = "senha")
    @JsonIgnore
    private String password;
    @Column(name = "pj")
    public boolean isPj;
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role", nullable = false)
    private UserRole role;
    @Column(name = "telefone")
    private Long cellphone;
    @Column(name = "desativado")
    private boolean isDisable;
    @Column(name = "primeiro_acesso")
    private boolean firstAcess; // se true = criar nova senha.
    @Column(name = "ultimo_login")
    private Instant lastLogin;
    @Column(name = "image_url", nullable = true)
    private String photoUrl;
    @Column(name = "image_url_miniatura", nullable = true)
    private String thumbPhotoUrl;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UserProfile profile;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserFavoriteModule> favorites = new ArrayList<>();

    public User(String name, String cpf, String email, String password, boolean isPj, UserRole role, Long telefone) {
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.isPj = isPj;
        this.role = role;
        this.cellphone = telefone;
    }

    public boolean isCorretLogin(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.password);
    }

}
