package com.indux.modules.whatsapp_media.presentation;

import com.indux.modules.whatsapp_media.domain.entity.WhatsappMediaRecord;
import com.indux.modules.whatsapp_media.service.WhatsappMediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/whatsapp-media")
public class WhatsappMediaController {
    private final WhatsappMediaService whatsappMediaService;

    public WhatsappMediaController(WhatsappMediaService whatsappMediaService) {
        this.whatsappMediaService = whatsappMediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@ModelAttribute WhatsappMediaRecord file){
        String fileName = whatsappMediaService.upload(file.file());
        return ResponseEntity.ok(Map.of("caminho", fileName));
    }
}
