package com.indux.modules.ppu.application.services.rdo.helper;

import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public class LoggerUserHelper {

    public static RDOLoggerUser createUser(JwtAuthenticationToken token){
        var name = (String) token.getToken().getClaims().get("name");
        return new RDOLoggerUser(name, UUID.fromString(token.getName()));
    }
}
