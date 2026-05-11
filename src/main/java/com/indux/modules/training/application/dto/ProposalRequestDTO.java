package com.indux.modules.training.application.dto;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record ProposalRequestDTO(
        @RequestParam("data")
        LocalDate date,
        @RequestParam("nome")
        String name,
        @RequestParam("obs")
        String observation,
        @RequestParam("anexoCriar")
        MultipartFile file
) {
}
