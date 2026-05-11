package com.indux.core.domain.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserModuleKey implements Serializable {
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "module_id", nullable = false, columnDefinition = "UUID")
    private UUID moduleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserModuleKey)) return false;
        UserModuleKey that = (UserModuleKey) o;
        return userId.equals(that.userId) && moduleId.equals(that.moduleId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, moduleId);
    }
}