package com.indux.core.application.service.auth;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.generic.TokenEvent;
import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.model.user.UserProfile;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.service.auth.ResetPasswordService;
import com.indux.core.domain.service.auth.TokenEventValidator;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.exception.user.TokenEventFailure;
import com.indux.core.infra.exception.user.ValidatePasswordFailure;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ResetPasswordImpl implements ResetPasswordService {
    private final UserRepository repository;
    private final TokenEventValidator tokenEvent;
    private final BCryptPasswordEncoder encoder;
    private final NotificationService notification;

    public ResetPasswordImpl(UserRepository repository, TokenEventValidator tokenEvent, BCryptPasswordEncoder encoder, NotificationService notification) {
        this.repository = repository;
        this.tokenEvent = tokenEvent;
        this.encoder = encoder;
        this.notification = notification;
    }

    @Override
    public void requestReset(String cpf) throws MessagingException {
        String cpfClean = AuthenticationValidator.sanitizeCpf(cpf);
        User user = repository.findByCpf(cpf);
        if (user == null) throw new NotFoundEmployee("Usuário não encontrado.");

        TokenEvent token = tokenEvent.createEvent(user.getId(), TokenEventType.Values.FORGET_PASSWORD.name());
        sendNotification(SimpleUser.fromEntity(user), token);
    }

    @Override
    public GenericMessage validateToken(int token) {
        boolean validate = tokenEvent.validateToken(token);
        if (!validate) throw new ValidatePasswordFailure("Token invalido");
        return new GenericMessage("Token válido", 200);
    }

    @Override
    public GenericMessage resetPassword(int token, String newPassword) {
        //Validações sem necessidade de acessar o banco de dados principal.
        if (!tokenEvent.validateToken(token)) throw new TokenEventFailure("A solicitação não é mais válida.");
        List<String> errors = AuthenticationValidator.validatePassword(newPassword);
        if (!errors.isEmpty()) throw new ValidatePasswordFailure(errors.toString());

        User user = repository.findById(tokenEvent.getUserToken(token)).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado"));
        user.setPassword(encoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        if (user.isFirstAcess()) {
            user.setFirstAcess(false);
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        ;

        tokenEvent.finalizeToken(token);
        return new GenericMessage("Senha alterada com sucesso.", 200);
    }

    @Async
    private void sendNotification(SimpleUser to, TokenEvent token) {
        Notification notificationValue = new Notification();
        notificationValue.setTo(to);
        notificationValue.setTemplateName(TokenEventType.Values.FORGET_PASSWORD.name());
        notificationValue.setMessage(token.getToken().toString());
        notificationValue.setVariables(Map.of(
                "expiration", "1 hora"
        ));
        notification.sendToAll(notificationValue, Set.of(NotificationType.EMAIL, NotificationType.WHATSAPP));
    }
}
