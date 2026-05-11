package com.indux.core.infra.config.security;

import com.indux.core.domain.model.auth.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
public class TokenProvider {
    private final JwtEncoder encoder;

    public TokenProvider(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generateToken(User user) {

        List<String> roles = List.of(user.getRole().getName());

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", roles)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateToken(String subject, List<String> roles, Map<String, Object> customClaims) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .claim("roles", roles);

        customClaims.forEach(claimsBuilder::claim);

        JwtClaimsSet claims = claimsBuilder.build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
