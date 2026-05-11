package com.indux.core.presentation;

import com.indux.core.application.dto.cbo.AllFuncao;
import com.indux.core.application.dto.cbo.FilterFuncao;
import com.indux.core.application.dto.cbo.FuncaoDTO;
import com.indux.core.application.dto.cbo.UpdateTraining;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.service.cbo.FuncaoHCMService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcaoHCM")
public class FuncaoHCMController {

    public final FuncaoHCMService funcaoHCMService;

    public FuncaoHCMController(FuncaoHCMService funcaoHCMService) {
        this.funcaoHCMService = funcaoHCMService;
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> createFuncaoHCM(
            @ModelAttribute FuncaoDTO funcaoDTO) {
        funcaoHCMService.createFuncaoHCM(funcaoDTO);
        return ResponseEntity.ok(new GenericMessage("Função HCM criada com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping("/getByHCM/{idHCM}")
    public ResponseEntity<FuncaoDTO>  getByHCM(
            @PathVariable("idHCM") String idHCM){
        return ResponseEntity.ok(funcaoHCMService.GetByIdHCM(idHCM));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<FuncaoDTO>  getById(
            @PathVariable("id") String id){
        return ResponseEntity.ok(funcaoHCMService.GetById(id));
    }

    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> updateFuncaoHCM(
            @PathVariable("id") String id,
            @ModelAttribute FuncaoDTO funcaoDTO
    ){
        funcaoHCMService.updateFuncaoHCM(id, funcaoDTO);
        return ResponseEntity.ok(new GenericMessage("Função HCM edidata com sucesso", HttpStatus.OK.value()));

    }

    @PutMapping("/training")
    public ResponseEntity<GenericMessage> updateTrainingHCM(
            @RequestBody UpdateTraining  updateTraining,
            JwtAuthenticationToken jwt
    ){
        var name = jwt.getToken().getClaims().get("name");
        funcaoHCMService.updateTraining(updateTraining,name);
        return ResponseEntity.ok(new GenericMessage("Função HCM edidata com sucesso", HttpStatus.OK.value()));
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<AllFuncao>> getAllFuncao(
            @RequestParam(required = false) List<Long> filialHCM,
            @RequestParam(required = false) Long idAuto,
            @RequestParam(required = false) List<String> codCBO,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> solicitante,
            @RequestParam(required = false) String nome,
            Pageable pageable
    ){
        FilterFuncao filter = new FilterFuncao(filialHCM, idAuto, codCBO, status, solicitante, nome);
        return ResponseEntity.ok(funcaoHCMService.getAllFuncaoHCM(pageable, filter));
    }

}
