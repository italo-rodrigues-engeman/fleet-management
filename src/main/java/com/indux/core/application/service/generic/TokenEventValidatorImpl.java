package com.indux.core.application.service.generic;

import com.indux.core.domain.model.generic.TokenEvent;
import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.repository.generic.TokenEventRepository;
import com.indux.core.domain.repository.generic.TokenEventTypeRepository;
import com.indux.core.domain.service.auth.TokenEventValidator;
import com.indux.core.infra.exception.user.TokenEventFailure;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TokenEventValidatorImpl implements TokenEventValidator {
    private final TokenEventRepository repository;
    private final TokenEventTypeRepository typeRepository;

    public TokenEventValidatorImpl(TokenEventRepository repository, TokenEventTypeRepository typeRepository) {
        this.repository = repository;
        this.typeRepository = typeRepository;
    }

    @Override
    public TokenEvent createEvent(UUID userId, String type) {
        TokenEventType eventType = typeRepository.findByName(type);
        if (eventType == null) {
            throw new IllegalArgumentException("Tipo de token desconhecido: " + type);
        }

        int maxRetries = 5;
        int attempts = 0;

        while (attempts < maxRetries) {
            int token = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);

            TokenEvent event = TokenEvent.builder()
                    .expireAt(eventType.quantityDate())
                    .userID(userId)
                    .type(eventType)
                    .token(token)
                    .build();

            try {
                return repository.save(event);
            } catch (Exception ex) {
                if (isDuplicateTokenException(ex)) {
                    attempts++;
                } else {
                    throw ex;
                }
            }
        }

        throw new RuntimeException("Falha ao gerar token único após " + maxRetries + " tentativas.");
    }

    private boolean isDuplicateTokenException(Exception ex) {
        return ex.getMessage() != null && ex.getMessage().contains("constraint") && ex.getMessage().contains("token");
    }

    private boolean isExpired(int token) {
        try {
            Optional<TokenEvent> tokenEvent = repository.findByToken(token);
            boolean expired = tokenEvent
                    .map(t -> t.getExpireAt().isBefore(Instant.now())).orElse(true);
            if (expired) {
                finalizeToken(token);
            }

            return expired;
        } catch (Exception e) {
            throw new TokenEventFailure(e.getMessage());
        }
    }

    @Override
    public boolean validateToken(int id) {
        try {
            Optional<TokenEvent> token = repository.findByToken(id);
            return token.isPresent() && !isExpired(id);
        } catch (Exception e) {
            throw new TokenEventFailure(e.getMessage());
        }
    }

    @Override
    public void finalizeToken(int token) {
        TokenEvent tokenEvent = repository.findByToken(token).orElseThrow(() -> new TokenEventFailure("Token inválido."));
        repository.delete(tokenEvent);
    }

    @Override
    public UUID getUserToken(int token) {
        TokenEvent tokenEvent = repository.findByToken(token).orElseThrow(() -> new TokenEventFailure("Token inválido."));
        return tokenEvent.getUserID();
    }
}
