package com.indux.core.application.dto.user;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import com.indux.core.domain.model.user.UserProfile;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Value
@Builder
@Getter
@Setter
@AllArgsConstructor
public class SimpleUser {
    UUID id;
    String nome;
    String email;
    Boolean isPj;
    String telefone;
    String imagemUrl;
    String miniaturaUrl;
    Boolean desativado;
    @Nullable String permissao;
    @Nullable Date criadoEm;
    @Nullable Instant ultimoLogin;
    @Nullable Boolean primeiroAcesso;
    @Nullable Instant atualizadoEm;
    @Nullable UserProfile perfil;

    public static SimpleUser fromEntity(User user, boolean isAdmin, boolean needsProfile) {
        UserRole role = user.getRole();
        
        // Debug: Log para verificar o valor do telefone

        SimpleUser.SimpleUserBuilder builder = SimpleUser.builder()
                .id(user.getId())
                .nome(user.getName())
                .email(user.getEmail())
                .isPj(user.isPj())
                .telefone(formatarTelefone(user.getCellphone()))
                .imagemUrl(user.getPhotoUrl())
                .miniaturaUrl(user.getThumbPhotoUrl())
                .desativado(user.isDisable())
                .criadoEm(Date.from(user.getCreatedAt()))
                .permissao(role != null ? role.getName() : null);

        if (isAdmin) {
            builder.ultimoLogin(user.getLastLogin())
                    .primeiroAcesso(user.isFirstAcess())
                    .atualizadoEm(user.getUpdatedAt());
        }

        if (needsProfile) {
            builder.perfil(user.getProfile());
        }

        return builder.build();
    }

    public static SimpleUser fromEntity(User user) {
        return fromEntity(user, false, false);
    }
    
    /**
     * Formata o telefone para exibição
     */
    private static String formatarTelefone(Long cellphone) {
        if (cellphone == null) {
            return null;
        }
        
        String telefoneStr = cellphone.toString();
        
        // Se o telefone tem 11 dígitos (com DDD), formata como (XX) XXXXX-XXXX
        if (telefoneStr.length() == 11) {
            String formatado = String.format("(%s) %s-%s", 
                telefoneStr.substring(0, 2),
                telefoneStr.substring(2, 7),
                telefoneStr.substring(7));
            return formatado;
        }
        // Se tem 10 dígitos (sem o 9), formata como (XX) XXXX-XXXX
        else if (telefoneStr.length() == 10) {
            String formatado = String.format("(%s) %s-%s", 
                telefoneStr.substring(0, 2),
                telefoneStr.substring(2, 6),
                telefoneStr.substring(6));
            return formatado;
        }
        // Para outros casos, retorna como string
        else {
            return telefoneStr;
        }
    }
}