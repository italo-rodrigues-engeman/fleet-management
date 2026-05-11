package com.indux.modules.clients.presentation.controller;

import com.indux.modules.clients.application.dto.ClientDTO;
import com.indux.modules.clients.application.dto.CreateClientRequestDTO;
import com.indux.modules.clients.application.dto.UpdateClientRequestDTO;
import com.indux.modules.clients.application.service.CreateClientUseCase;
import com.indux.modules.clients.application.service.GetClientByIdUseCase;
import com.indux.modules.clients.application.service.ListClientsUseCase;
import com.indux.modules.clients.application.service.UpdateClientUseCase;
import com.indux.modules.clients.infra.duediligence.DueDiligence;
import com.indux.modules.clients.infra.duediligence.DueDiligenceResponse;
import com.indux.modules.clients.presentation.dto.PageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ListClientsUseCase listClientsUseCase;
    private final CreateClientUseCase createClientUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final GetClientByIdUseCase getClientByIdUseCase;
    private final DueDiligence dueDiligence;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientDTO> create(@ModelAttribute @Valid CreateClientRequestDTO request) {
        return ResponseEntity.ok(createClientUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ClientDTO>> list(
            @RequestParam(required = false) String enderecoPlanta,
            @RequestParam(required = false) String fiscal,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoCliente,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, name = "idFilter") Long idFilter,
            @RequestParam(required = false) Boolean dueDiligentes,
            @RequestParam(required = false) String ramo,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(PageResponseDTO.from(listClientsUseCase.execute(
                enderecoPlanta,
                fiscal,
                cnpj,
                cidade,
                estado,
                tipoCliente,
                status != null ? com.indux.modules.clients.domain.model.ClientStatus.valueOf(status) : null,
                name,
                idFilter,
                dueDiligentes,
                ramo,
                pageable
        )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getClientByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> update(
            @PathVariable Long id,
            @RequestBody UpdateClientRequestDTO request) {
        return ResponseEntity.ok(updateClientUseCase.execute(id, request));
    }

    @GetMapping("/duediligence/{cnpj}")
    public ResponseEntity<DueDiligenceResponse> getDueDiligence(@PathVariable String cnpj) {
        DueDiligenceResponse response = dueDiligence.call(cnpj);
        return ResponseEntity.ok(response);
    }
} 