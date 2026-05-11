package com.indux.core.domain.model.user;

import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.modules.Modulo;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_favorite_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavoriteModule {
    @EmbeddedId
    private UserModuleKey id;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("moduleId")
    @JoinColumn(name = "module_id", nullable = false)
    private Modulo module;
    @Column(name = "favoritado_por", nullable = false, updatable = false)
    private Instant favoritedAt = Instant.now();
}
