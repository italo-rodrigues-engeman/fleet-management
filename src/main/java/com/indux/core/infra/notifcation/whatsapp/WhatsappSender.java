package com.indux.core.infra.notifcation.whatsapp;

import com.indux.core.infra.notifcation.whatsapp.client.WhatsappClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WhatsappSender {
    private final WhatsappClient client;

    public WhatsappSender(WhatsappClient client) {
        this.client = client;
    }

    public void sendWelcomeMessage(String to, String name, String code, String expiration) {
        Map<String, Object> vars = Map.of(
                "name", name,
                "code", code,
                "expirationTime", expiration
        );
        String body = TextTemplateProcessor.renderTemplate("templates/whatsapp/welcome_message.txt", vars);
        client.sendMessage(to, body);
    }

    public void sendForgetPassword(String to, String code, String expiration) {
        Map<String, Object> vars = Map.of(
                "expiration", expiration,
                "code", code
        );
        String body = TextTemplateProcessor.renderTemplate("templates/whatsapp/forgot_password.txt", vars);
        client.sendMessage(to, body);
    }
    
    public void sendMessage(String phone, String message) {
        client.sendMessage(phone, message);
    }
}
