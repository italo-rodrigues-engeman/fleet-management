package com.indux.modules.ocf.presentation;

import com.indux.modules.ocf.domain.repository.TicketDivergenteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocf/tickets-divergentes")
public class TicketDivergenteController {
    private final TicketDivergenteRepository repository;

    public TicketDivergenteController(TicketDivergenteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(
            @RequestParam(required = false, name = "id") Long id,
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false, defaultValue = "50") Integer limit,
            JwtAuthenticationToken jwt
    ) {
        return ResponseEntity.ok(repository.find(id, matricula, limit));
    }

    public static class UpdateDivergenteDTO {
        public Boolean contato;
        public Boolean alteracao;
        public String observacao;
        public Boolean pertinente;
        public java.time.LocalDate data_alteracao;
        public java.time.LocalDate data_contato;
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> atualizar(
            @RequestParam Long id,
            @RequestBody UpdateDivergenteDTO body,
            JwtAuthenticationToken jwt
    ) {
        int updated = repository.updateFields(
                id,
                body.contato,
                body.alteracao,
                body.observacao,
                body.pertinente,
                body.data_alteracao,
                body.data_contato
        );
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}


