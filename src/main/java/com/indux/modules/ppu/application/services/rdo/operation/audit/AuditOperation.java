package com.indux.modules.ppu.application.services.rdo.operation.audit;

import com.indux.modules.ppu.domain.entities.rdo.audit.AuditDivergence;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

public interface AuditOperation {
    List<AuditDivergence> execute(String rdoId, MultipartFile file, String userId) throws ParseException;
    List<AuditDivergence> execute(String rdoId, MultipartFile file, String userId, LocalDate start, LocalDate end) throws ParseException;
}
