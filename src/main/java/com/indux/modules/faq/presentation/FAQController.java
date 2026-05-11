package com.indux.modules.faq.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.domain.model.modules.AttachmentRecord;
import com.indux.modules.faq.application.dto.*;
import com.indux.modules.faq.application.service.FAQService;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import com.indux.modules.faq.domain.repository.mongo.PerguntaMongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faq")
public class FAQController {
    
    private final FAQService faqService;
    private final PerguntaMongoRepository perguntaMongoRepository;
    
    public FAQController(FAQService faqService, PerguntaMongoRepository perguntaMongoRepository) {
        this.faqService = faqService;
        this.perguntaMongoRepository = perguntaMongoRepository;
    }

    // Método helper para converter AttachmentForm para AttachmentRecord
    private List<AttachmentRecord> convertToAttachmentRecords(List<AttachmentForm> attachmentForms) {
        if (attachmentForms == null) return null;
        return attachmentForms.stream()
                .map(form -> new AttachmentRecord(form.getNome(), form.getFile()))
                .collect(Collectors.toList());
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("respostas[*].contrato");
    }
    
    // ========== SETOR ENDPOINTS ==========
    
    @PostMapping("/setores")
    public ResponseEntity<GenericMessage> createSetor(@RequestBody CreateSetorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.createSetor(dto));
    }
    
    @GetMapping("/setores")
    public ResponseEntity<List<SetorDTO>> getAllSetores() {
        return ResponseEntity.ok(faqService.getAllSetores());
    }
    
    @GetMapping("/setores/{id}")
    public ResponseEntity<SetorDTO> getSetorById(@PathVariable Long id) {
        Optional<SetorDTO> setor = faqService.getSetorById(id);
        return setor.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/setores/{id}")
    public ResponseEntity<GenericMessage> updateSetor(@PathVariable Long id, @RequestBody CreateSetorDTO dto) {
        return ResponseEntity.ok(faqService.updateSetor(id, dto));
    }
    
    @DeleteMapping("/setores/{id}")
    public ResponseEntity<GenericMessage> deleteSetor(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.deleteSetor(id));
    }
    
    // ========== TEMA ENDPOINTS ==========
    
    @PostMapping("/temas")
    public ResponseEntity<GenericMessage> createTema(@RequestBody CreateTemaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.createTema(dto));
    }
    
    @GetMapping("/temas")
    public ResponseEntity<List<TemaDTO>> getAllTemas() {
        return ResponseEntity.ok(faqService.getAllTemas());
    }
    
    @GetMapping("/temas/setor/{setorId}")
    public ResponseEntity<List<TemaDTO>> getTemasBySetor(@PathVariable Long setorId) {
        return ResponseEntity.ok(faqService.getTemasBySetor(setorId));
    }
    
    @GetMapping("/temas/{id}")
    public ResponseEntity<TemaDTO> getTemaById(@PathVariable Long id) {
        Optional<TemaDTO> tema = faqService.getTemaById(id);
        return tema.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/temas/{id}")
    public ResponseEntity<GenericMessage> updateTema(@PathVariable Long id, @RequestBody CreateTemaDTO dto) {
        return ResponseEntity.ok(faqService.updateTema(id, dto));
    }
    
    @DeleteMapping("/temas/{id}")
    public ResponseEntity<GenericMessage> deleteTema(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.deleteTema(id));
    }
    
    @PutMapping("/temas/reordenar")
    public ResponseEntity<GenericMessage> reordenarTemas(@RequestBody ReordenarTemasDTO dto) {
        return ResponseEntity.ok(faqService.reorganizarTemas(dto));
    }
    
    // ========== PERGUNTA ENDPOINTS ==========
    
    @PostMapping(value = "/perguntas", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericMessage> createPergunta(
            @ModelAttribute CreatePerguntaForm form,
            JwtAuthenticationToken jwt,
            jakarta.servlet.http.HttpServletRequest request) {
        
        // VALIDAÇÃO: Verificar se há público definido (pergunta OU respostas)
        boolean temPublicoPergunta = form.getPublico() != null && !form.getPublico().trim().isEmpty();
        boolean temPublicoRespostas = false;
        
        if (form.getRespostas() != null && !form.getRespostas().isEmpty()) {
            for (int i = 0; i < form.getRespostas().size(); i++) {
                var resp = form.getRespostas().get(i);
                if (resp.getPublico() != null && !resp.getPublico().trim().isEmpty()) {
                    temPublicoRespostas = true;
                    break;
                }
            }
        }
        
        if (!temPublicoPergunta && !temPublicoRespostas) {
            return ResponseEntity.badRequest()
                .body(new GenericMessage("É obrigatório informar o campo 'publico' na pergunta OU em pelo menos uma resposta.", 400));
        }
        
        // VALIDAÇÃO: Verificar se há filialHcmId definida (pergunta OU respostas)
        boolean temFilialHcmPergunta = form.getFilialHcmId() != null && !form.getFilialHcmId().isEmpty();
        boolean temFilialHcmRespostas = false;
        
        if (form.getRespostas() != null && !form.getRespostas().isEmpty()) {
            for (var resp : form.getRespostas()) {
                if (resp.getFilialHcmId() != null && !resp.getFilialHcmId().isEmpty()) {
                    temFilialHcmRespostas = true;
                    break;
                }
            }
        }
        
        if (!temFilialHcmPergunta && !temFilialHcmRespostas) {
            return ResponseEntity.badRequest()
                .body(new GenericMessage("É obrigatório informar o campo 'filialHcmId' na pergunta OU em pelo menos uma resposta.", 400));
        }
        
        // Converter Form para DTO lógico de criação
        // Os anexos já estão no form como AttachmentRecord
        List<CreatePerguntaDTO.RespostaItem> respostaItems = new ArrayList<>();
        if (form.getRespostas() != null) {
            for (int idx = 0; idx < form.getRespostas().size(); idx++) {
                CreatePerguntaForm.RespostaForm r = form.getRespostas().get(idx);
                // Os anexos das respostas já estão no form como AttachmentRecord
                List<Long> contratosLista = r.getContratos();
                if (contratosLista == null || contratosLista.isEmpty()) {
                    String raw = request.getParameter("respostas[" + idx + "].contrato");
                    if (raw != null && !raw.isBlank()) {
                        contratosLista = new ArrayList<>();
                        String cleaned = raw.replaceAll("[\\{\\}\\[\\]\\s]", "");
                        for (String part : cleaned.split(",")) {
                            if (!part.isBlank()) {
                                try { contratosLista.add(Long.parseLong(part)); } catch (Exception ignored) {}
                            }
                        }
                    }
                }
                respostaItems.add(new CreatePerguntaDTO.RespostaItem(
                        r.getConteudo(), r.getRegional(), null, contratosLista, r.getPublico(), convertToAttachmentRecords(r.getAnexo()),
                        r.getDiretoriaId(), r.getSuperintendenciaId(), r.getProjetoId(), r.getFilialHcmId(), r.getSetorOrganizationId(),
                        r.getDiretoriaIds(), r.getSuperintendenciaIds(), r.getRegionalIds(), r.getSetorOrganizationIds(), r.getProjetoIds()
                ));
            }
        }

        // Processar campo contrato se for uma string no formato "{2,10,9,94,99,27}"
        List<Long> contratosProcessados = null; // null indica que não foi alterado
        
        // Primeiro tentar usar o campo contratos do form
        if (form.getContratos() != null) {
            // Campo foi enviado, processar
            if (!form.getContratos().isEmpty()) {
                contratosProcessados = new ArrayList<>(form.getContratos());
            } else {
                // Campo foi enviado como array vazio, limpar
                contratosProcessados = new ArrayList<>();
            }
        } else {
            // Tentar extrair do campo contrato se estiver no formato string
            String contratoRaw = request.getParameter("contrato");
            if (contratoRaw != null && !contratoRaw.isBlank()) {
                try {
                    contratosProcessados = new ArrayList<>();
                    // Remover chaves e espaços, depois dividir por vírgula
                    String cleaned = contratoRaw.replaceAll("[\\{\\}\\[\\]\\s]", "");
                    for (String part : cleaned.split(",")) {
                        if (!part.isBlank()) {
                            try { 
                                Long rateioId = Long.parseLong(part.trim());
                                contratosProcessados.add(rateioId);
                            } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        CreatePerguntaDTO dto = new CreatePerguntaDTO(
                form.getSetorId(),
                form.getTemaId(),
                form.getCategoria(),
                form.getTipo(),
                form.getDataCriacao(),
                form.getDataFim(),
                form.getTitulo(),
                form.getRegionalId(),
                null, // Removido contratoId - usar apenas o campo contratos com rateio_id
                contratosProcessados,
                form.getPublico(),
                form.getAprovador(),
                form.getObservacoes(),
                convertToAttachmentRecords(form.getAnexos()),

                respostaItems,
                form.getSituacao(),
                form.getDiretoriaId(),
                form.getSuperintendenciaId(),
                form.getProjetoId(),
                form.getFilialHcmId(),  // Updated to handle list
                form.getSetorOrganizationId(),
                form.getDiretoriaIds(),
                form.getSuperintendenciaIds(),
                form.getRegionalIds(),
                form.getSetorOrganizationIds(),
                form.getProjetoIds()
        );
        
        // Chamar o service e verificar o resultado
        GenericMessage result = faqService.createPergunta(dto, jwt.getName());
        
        // Retornar o status HTTP correto baseado no código de resposta
        if (result.status() >= 200 && result.status() < 300) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else {
            return ResponseEntity.status(result.status()).body(result);
        }
    }
    
             @GetMapping("/perguntas")
    public ResponseEntity<Page<PerguntaResumoDTO>> getAllPerguntas(
            @PageableDefault(size = 10) Pageable pageable,
            PerguntaFiltrosDTO filtros
    ) {
        return ResponseEntity.ok(faqService.listarPerguntasResumo(pageable, filtros));
    }
    
    @GetMapping("/perguntas/tema/{temaId}")
    public ResponseEntity<Page<PerguntaResumoDTO>> getPerguntasByTema(
            @PathVariable Long temaId,
            @PageableDefault(size = 10) Pageable pageable,
            PerguntaTemaFiltrosDTO filtros
    ) {
        return ResponseEntity.ok(faqService.getPerguntasByTema(temaId, pageable, filtros));
    }
    
    @GetMapping("/perguntas/{id}")
    public ResponseEntity<Map<String, Object>> getPerguntaById(@PathVariable Long id) {
        return perguntaMongoRepository.findByCodigoSequencial(id)
                .map(doc -> {
                    Integer etapaAtual = doc.getCurrentStep() != null
                            ? doc.getCurrentStep()
                            : ((doc.getStepLog() != null && !doc.getStepLog().isEmpty())
                                ? doc.getStepLog().get(doc.getStepLog().size() - 1).getStep()
                                : switch (doc.getStatus() == null ? "" : doc.getStatus()) {
                                    case "APROVADO" -> 0;
                                    case "REVISAO" -> 1;
                                    case "PENDENTE" -> 2;
                                    default -> null;
                                });
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("id", doc.getId());
                    resp.put("codigoSequencial", doc.getCodigoSequencial());
                    resp.put("setorId", doc.getSetorId());
                    String setorNome = null;
                    if (doc.getSetorId() != null) {
                        try {
                            setorNome = faqService.getSetorNomeById(doc.getSetorId()).orElse(null);
                        } catch (Exception ignored) {}
                    }
                    resp.put("setorNome", setorNome);
                    resp.put("temaId", doc.getTemaId());
                    // temaNome
                    String temaNome = null;
                    if (doc.getTemaId() != null) {
                        try {
                            temaNome = faqService.getTemaNomeById(doc.getTemaId()).orElse(null);
                        } catch (Exception ignored) {}
                    }
                    resp.put("temaNome", temaNome);
                    resp.put("categoria", doc.getCategoria());
                    resp.put("tipo", doc.getTipo());
                    resp.put("dataCriacao", doc.getDataCriacao());
                    resp.put("dataFim", doc.getDataFim());
                    resp.put("titulo", doc.getTitulo());
                    // Removido contratoId - usar apenas o campo contratos com rateio_id
                    
                    // Resolver nome do contrato principal a partir do primeiro contrato da lista
                    String contratoNome = null;
                    if (doc.getContratos() != null && !doc.getContratos().isEmpty()) {
                        Long primeiroRateioId = doc.getContratos().get(0);
                        if (primeiroRateioId != null) {
                            try {
                                contratoNome = faqService
                                        .getContratoNomeByRateio(primeiroRateioId)
                                        .orElse(null);
                            } catch (Exception ignored) {}
                        }
                    }
                    if (contratoNome != null) {
                        resp.put("contratoNome", contratoNome);
                    }
                    
                    // Transformar array de contratos para incluir nomes
                    List<Map<String, Object>> contratosComNomes = new ArrayList<>();
                    if (doc.getContratos() != null && !doc.getContratos().isEmpty()) {
                        for (Long rateioId : doc.getContratos()) {
                            Map<String, Object> contratoInfo = new HashMap<>();
                            contratoInfo.put("rateio_id", rateioId);
                            String contratoNomeItem = null;
                            if (rateioId != null) {
                                try { 
                                    contratoNomeItem = faqService.getContratoNomeByRateio(rateioId).orElse(null); 
                                } catch (Exception ignored) {}
                            }
                            contratoInfo.put("nome", contratoNomeItem);
                            contratosComNomes.add(contratoInfo);
                        }
                    }
                    resp.put("contratos", contratosComNomes);
                    resp.put("publico", doc.getPublico());
                    resp.put("aprovador", doc.getAprovador());
                    resp.put("observacoes", doc.getObservacoes());
                    resp.put("anexos", doc.getAnexos());
                    // Campos individuais de organograma da pergunta
                    resp.put("regionalId", doc.getRegionalId());
                    resp.put("diretoriaId", doc.getDiretoriaId());
                    resp.put("superintendenciaId", doc.getSuperintendenciaId());
                    resp.put("setorOrganizationId", doc.getSetorOrganizationId());
                    resp.put("projetoId", doc.getProjetoId());
                    resp.put("filialHcmId", doc.getFilialHcmId());
                    resp.put("restrito", doc.getRestrito());
                    resp.put("valor", doc.getValor());
                    resp.put("tipoValor", doc.getTipoValor());
                    // Enriquecer respostas com regionalName, contratoNome e contratosNomes
                    List<Map<String, Object>> respostasEnriquecidas = new ArrayList<>();
                    if (doc.getRespostas() != null) {
                        for (PerguntaMongo.RespostaEmbedded r : doc.getRespostas()) {
                            Map<String, Object> rmap = new HashMap<>();
                            rmap.put("conteudo", r.getConteudo());
                            // Campos individuais da resposta
                            rmap.put("regional", r.getRegional());
                            rmap.put("diretoriaId", r.getDiretoriaId());
                            rmap.put("superintendenciaId", r.getSuperintendenciaId());
                            rmap.put("setorOrganizationId", r.getSetorOrganizationId());
                            rmap.put("projetoId", r.getProjetoId());
                            rmap.put("filialHcmId", r.getFilialHcmId());
                            rmap.put("restrito", r.getRestrito());
                            rmap.put("valor", r.getValor());
                            rmap.put("tipoValor", r.getTipoValor());
                            // Removido contrato individual - usar apenas o campo contratos com rateio_id
                            rmap.put("publico", r.getPublico());
                            rmap.put("anexo", r.getAnexo());
                            
                            // Resolver nome do contrato principal da resposta a partir do primeiro contrato da lista
                            String rContratoNome = null;
                            if (r.getContratos() != null && !r.getContratos().isEmpty()) {
                                Long primeiroRateioId = r.getContratos().get(0);
                                if (primeiroRateioId != null) {
                                    try { 
                                        rContratoNome = faqService.getContratoNomeByRateio(primeiroRateioId).orElse(null); 
                                    } catch (Exception ignored) {}
                                }
                            }
                            if (rContratoNome != null) {
                                rmap.put("contratoNome", rContratoNome);
                            }
                            
                            // Transformar array de contratos das respostas para incluir nomes
                            List<Map<String, Object>> contratosComNomesResposta = new ArrayList<>();
                            if (r.getContratos() != null && !r.getContratos().isEmpty()) {
                                for (Long rateioId : r.getContratos()) {
                                    Map<String, Object> contratoInfo = new HashMap<>();
                                    contratoInfo.put("rateio_id", rateioId);
                                    String contratoNomeResposta = null;
                                    if (rateioId != null) {
                                        try { 
                                            contratoNomeResposta = faqService.getContratoNomeByRateio(rateioId).orElse(null); 
                                        } catch (Exception ignored) {}
                                    }
                                    contratoInfo.put("nome", contratoNomeResposta);
                                    contratosComNomesResposta.add(contratoInfo);
                                }
                            }
                            rmap.put("contratos", contratosComNomesResposta);
                            
                            // Arrays de organograma nas respostas (IDs + nomes)
                            // Diretorias: verificar lista primeiro, depois campo individual
                            List<Map<String, Object>> rDiretorias = new ArrayList<>();
                            if (r.getDiretoriaIds() != null && !r.getDiretoriaIds().isEmpty()) {
                                for (Long idOrg : r.getDiretoriaIds()) {
                                    Map<String, Object> it = new HashMap<>();
                                    it.put("id", idOrg);
                                    String nome = null;
                                    if (idOrg != null) { try { nome = faqService.getOrganizationNomeById(idOrg).orElse(null); } catch (Exception ignored) {} }
                                    it.put("nome", nome);
                                    rDiretorias.add(it);
                                }
                            } else if (r.getDiretoriaId() != null) {
                                // Se não há lista mas há campo individual, incluir
                                Map<String, Object> it = new HashMap<>();
                                it.put("id", r.getDiretoriaId());
                                String nome = null;
                                try { nome = faqService.getOrganizationNomeById(r.getDiretoriaId()).orElse(null); } catch (Exception ignored) {}
                                it.put("nome", nome);
                                rDiretorias.add(it);
                            }
                            rmap.put("diretorias", rDiretorias);

                            // Superintendências: verificar lista primeiro, depois campo individual
                            List<Map<String, Object>> rSuperintendencias = new ArrayList<>();
                            if (r.getSuperintendenciaIds() != null && !r.getSuperintendenciaIds().isEmpty()) {
                                for (Long idOrg : r.getSuperintendenciaIds()) {
                                    Map<String, Object> it = new HashMap<>();
                                    it.put("id", idOrg);
                                    String nome = null;
                                    if (idOrg != null) { try { nome = faqService.getOrganizationNomeById(idOrg).orElse(null); } catch (Exception ignored) {} }
                                    it.put("nome", nome);
                                    rSuperintendencias.add(it);
                                }
                            } else if (r.getSuperintendenciaId() != null) {
                                Map<String, Object> it = new HashMap<>();
                                it.put("id", r.getSuperintendenciaId());
                                String nome = null;
                                try { nome = faqService.getOrganizationNomeById(r.getSuperintendenciaId()).orElse(null); } catch (Exception ignored) {}
                                it.put("nome", nome);
                                rSuperintendencias.add(it);
                            }
                            rmap.put("superintendencias", rSuperintendencias);

                            // Regionais: verificar lista primeiro, depois campo individual
                            List<Map<String, Object>> rRegionais = new ArrayList<>();
                            if (r.getRegionalIds() != null && !r.getRegionalIds().isEmpty()) {
                                for (Long idReg : r.getRegionalIds()) {
                                    Map<String, Object> it = new HashMap<>();
                                    it.put("id", idReg);
                                    String nome = null;
                                    if (idReg != null) { try { nome = faqService.getRegionalNameById(idReg).orElse(null); } catch (Exception ignored) {} }
                                    it.put("nome", nome);
                                    rRegionais.add(it);
                                }
                            } else if (r.getRegional() != null) {
                                Map<String, Object> it = new HashMap<>();
                                it.put("id", r.getRegional());
                                String nome = null;
                                try { nome = faqService.getRegionalNameById(r.getRegional()).orElse(null); } catch (Exception ignored) {}
                                it.put("nome", nome);
                                rRegionais.add(it);
                            }
                            rmap.put("regionais", rRegionais);

                            // Setores: verificar lista primeiro, depois campo individual
                            List<Map<String, Object>> rSetores = new ArrayList<>();
                            if (r.getSetorOrganizationIds() != null && !r.getSetorOrganizationIds().isEmpty()) {
                                for (Long idOrg : r.getSetorOrganizationIds()) {
                                    Map<String, Object> it = new HashMap<>();
                                    it.put("id", idOrg);
                                    String nome = null;
                                    if (idOrg != null) { try { nome = faqService.getOrganizationNomeById(idOrg).orElse(null); } catch (Exception ignored) {} }
                                    it.put("nome", nome);
                                    rSetores.add(it);
                                }
                            } else if (r.getSetorOrganizationId() != null) {
                                Map<String, Object> it = new HashMap<>();
                                it.put("id", r.getSetorOrganizationId());
                                String nome = null;
                                try { nome = faqService.getOrganizationNomeById(r.getSetorOrganizationId()).orElse(null); } catch (Exception ignored) {}
                                it.put("nome", nome);
                                rSetores.add(it);
                            }
                            rmap.put("setoresOrganization", rSetores);

                            // Projetos: verificar lista primeiro, depois campo individual
                            List<Map<String, Object>> rProjetos = new ArrayList<>();
                            if (r.getProjetoIds() != null && !r.getProjetoIds().isEmpty()) {
                                for (Long idProj : r.getProjetoIds()) {
                                    Map<String, Object> it = new HashMap<>();
                                    it.put("id", idProj);
                                    String nome = null;
                                    if (idProj != null) { try { nome = faqService.getProjetoNomeById(idProj).orElse(null); } catch (Exception ignored) {} }
                                    it.put("nome", nome);
                                    rProjetos.add(it);
                                }
                            } else if (r.getProjetoId() != null) {
                                Map<String, Object> it = new HashMap<>();
                                it.put("id", r.getProjetoId());
                                String nome = null;
                                try { nome = faqService.getProjetoNomeById(r.getProjetoId()).orElse(null); } catch (Exception ignored) {}
                                it.put("nome", nome);
                                rProjetos.add(it);
                            }
                            rmap.put("projetos", rProjetos);
                            
                            // Enriquecer filiais HCM com nomes
                            List<Map<String, Object>> filiaisComNomes = new ArrayList<>();
                            if (r.getFilialHcmId() != null && !r.getFilialHcmId().isEmpty()) {
                                for (Long filialId : r.getFilialHcmId()) {
                                    Map<String, Object> filialInfo = new HashMap<>();
                                    filialInfo.put("id", filialId);
                                    String filialNome = null;
                                    if (filialId != null && filialId != 0L) {
                                        try { 
                                            filialNome = faqService.getFilialNomeById(filialId.intValue()).orElse(null); 
                                        } catch (Exception ignored) {}
                                    } else if (filialId != null && filialId == 0L) {
                                        filialNome = "Todas";
                                    }
                                    filialInfo.put("nome", filialNome);
                                    filiaisComNomes.add(filialInfo);
                                }
                            }
                            rmap.put("filiaisHcm", filiaisComNomes);
                            
                            respostasEnriquecidas.add(rmap);
                        }
                    }
                    resp.put("respostas", respostasEnriquecidas);
                    resp.put("status", doc.getStatus());
                    resp.put("stepLog", doc.getStepLog());
                    resp.put("etapa_atual", etapaAtual);
                    resp.put("situacao", doc.getSituacao());
                    
                    // Arrays de organograma na pergunta (IDs + nomes)
                    // Diretorias: verificar lista primeiro, depois campo individual
                    List<Map<String, Object>> diretorias = new ArrayList<>();
                    if (doc.getDiretoriaIds() != null && !doc.getDiretoriaIds().isEmpty()) {
                        for (Long idOrg : doc.getDiretoriaIds()) {
                            Map<String, Object> it = new HashMap<>();
                            it.put("id", idOrg);
                            String nome = null;
                            if (idOrg != null) { try { nome = faqService.getOrganizationNomeById(idOrg).orElse(null); } catch (Exception ignored) {} }
                            it.put("nome", nome);
                            diretorias.add(it);
                        }
                    } else if (doc.getDiretoriaId() != null) {
                        // Se não há lista mas há campo individual, incluir
                        Map<String, Object> it = new HashMap<>();
                        it.put("id", doc.getDiretoriaId());
                        String nome = null;
                        try { nome = faqService.getOrganizationNomeById(doc.getDiretoriaId()).orElse(null); } catch (Exception ignored) {}
                        it.put("nome", nome);
                        diretorias.add(it);
                    }
                    resp.put("diretorias", diretorias);

                    // Superintendências: verificar lista primeiro, depois campo individual
                    List<Map<String, Object>> superintendencias = new ArrayList<>();
                    if (doc.getSuperintendenciaIds() != null && !doc.getSuperintendenciaIds().isEmpty()) {
                        for (Long idOrg : doc.getSuperintendenciaIds()) {
                            Map<String, Object> it = new HashMap<>();
                            it.put("id", idOrg);
                            String nome = null;
                            if (idOrg != null) { try { nome = faqService.getOrganizationNomeById(idOrg).orElse(null); } catch (Exception ignored) {} }
                            it.put("nome", nome);
                            superintendencias.add(it);
                        }
                    } else if (doc.getSuperintendenciaId() != null) {
                        Map<String, Object> it = new HashMap<>();
                        it.put("id", doc.getSuperintendenciaId());
                        String nome = null;
                        try { nome = faqService.getOrganizationNomeById(doc.getSuperintendenciaId()).orElse(null); } catch (Exception ignored) {}
                        it.put("nome", nome);
                        superintendencias.add(it);
                    }
                    resp.put("superintendencias", superintendencias);

                    // Regionais: verificar lista primeiro, depois campo individual
                    List<Map<String, Object>> regionais = new ArrayList<>();
                    if (doc.getRegionalIds() != null && !doc.getRegionalIds().isEmpty()) {
                        for (Long idReg : doc.getRegionalIds()) {
                            Map<String, Object> it = new HashMap<>();
                            it.put("id", idReg);
                            String nome = null;
                            if (idReg != null) { try { nome = faqService.getRegionalNameById(idReg).orElse(null); } catch (Exception ignored) {} }
                            it.put("nome", nome);
                            regionais.add(it);
                        }
                    } else if (doc.getRegionalId() != null) {
                        Map<String, Object> it = new HashMap<>();
                        it.put("id", doc.getRegionalId());
                        String nome = null;
                        try { nome = faqService.getRegionalNameById(doc.getRegionalId()).orElse(null); } catch (Exception ignored) {}
                        it.put("nome", nome);
                        regionais.add(it);
                    }
                    resp.put("regionais", regionais);

                    // Setores: verificar lista primeiro, depois campo individual
                    List<Map<String, Object>> setores = new ArrayList<>();
                    if (doc.getSetorOrganizationIds() != null && !doc.getSetorOrganizationIds().isEmpty()) {
                        for (Long idOrg : doc.getSetorOrganizationIds()) {
                            Map<String, Object> it = new HashMap<>();
                            it.put("id", idOrg);
                            String nome = null;
                            if (idOrg != null) { try { nome = faqService.getOrganizationNomeById(idOrg).orElse(null); } catch (Exception ignored) {} }
                            it.put("nome", nome);
                            setores.add(it);
                        }
                    } else if (doc.getSetorOrganizationId() != null) {
                        Map<String, Object> it = new HashMap<>();
                        it.put("id", doc.getSetorOrganizationId());
                        String nome = null;
                        try { nome = faqService.getOrganizationNomeById(doc.getSetorOrganizationId()).orElse(null); } catch (Exception ignored) {}
                        it.put("nome", nome);
                        setores.add(it);
                    }
                    resp.put("setoresOrganization", setores);

                    // Projetos: verificar lista primeiro, depois campo individual
                    List<Map<String, Object>> projetos = new ArrayList<>();
                    if (doc.getProjetoIds() != null && !doc.getProjetoIds().isEmpty()) {
                        for (Long idProj : doc.getProjetoIds()) {
                            Map<String, Object> it = new HashMap<>();
                            it.put("id", idProj);
                            String nome = null;
                            if (idProj != null) { try { nome = faqService.getProjetoNomeById(idProj).orElse(null); } catch (Exception ignored) {} }
                            it.put("nome", nome);
                            projetos.add(it);
                        }
                    } else if (doc.getProjetoId() != null) {
                        Map<String, Object> it = new HashMap<>();
                        it.put("id", doc.getProjetoId());
                        String nome = null;
                        try { nome = faqService.getProjetoNomeById(doc.getProjetoId()).orElse(null); } catch (Exception ignored) {}
                        it.put("nome", nome);
                        projetos.add(it);
                    }
                    resp.put("projetos", projetos);
                    
                    // Enriquecer filiais HCM da pergunta com nomes
                    List<Map<String, Object>> filiaisComNomesPergunta = new ArrayList<>();
                    if (doc.getFilialHcmId() != null && !doc.getFilialHcmId().isEmpty()) {
                        for (Long filialId : doc.getFilialHcmId()) {
                            Map<String, Object> filialInfo = new HashMap<>();
                            filialInfo.put("id", filialId);
                            String filialNome = null;
                            if (filialId != null && filialId != 0L) {
                                try { 
                                    filialNome = faqService.getFilialNomeById(filialId.intValue()).orElse(null); 
                                } catch (Exception ignored) {}
                            } else if (filialId != null && filialId == 0L) {
                                filialNome = "Todas";
                            }
                            filialInfo.put("nome", filialNome);
                            filiaisComNomesPergunta.add(filialInfo);
                        }
                    }
                    resp.put("filiaisHcm", filiaisComNomesPergunta);
                    
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/perguntas/{id}")
    public ResponseEntity<GenericMessage> updatePergunta(@PathVariable Long id, @RequestBody CreatePerguntaDTO dto) {
        return ResponseEntity.ok(faqService.updatePergunta(id, dto));
    }
    
    @DeleteMapping("/perguntas/{id}")
    public ResponseEntity<GenericMessage> deletePergunta(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.deletePergunta(id));
    }
    
    // ========== RESPOSTA ENDPOINTS ==========
    
    @PostMapping("/respostas")
    public ResponseEntity<GenericMessage> createResposta(@RequestBody CreateRespostaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.createResposta(dto));
    }
    
    @GetMapping("/respostas")
    public ResponseEntity<List<RespostaDTO>> getAllRespostas() {
        return ResponseEntity.ok(faqService.getAllRespostas());
    }
    
    @GetMapping("/respostas/pergunta/{perguntaId}")
    public ResponseEntity<List<RespostaDTO>> getRespostasByPergunta(@PathVariable Long perguntaId) {
        return ResponseEntity.ok(faqService.getRespostasByPergunta(perguntaId));
    }
    
    // Endpoint comentado - não faz mais sentido buscar por contrato individual

    @GetMapping("/respostas/{id}")
    public ResponseEntity<RespostaDTO> getRespostaById(@PathVariable Long id) {
        Optional<RespostaDTO> resposta = faqService.getRespostaById(id);
        return resposta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/respostas/{id}")
    public ResponseEntity<GenericMessage> updateResposta(@PathVariable Long id, @RequestBody CreateRespostaDTO dto) {
        return ResponseEntity.ok(faqService.updateResposta(id, dto));
    }
    
    @DeleteMapping("/respostas/{id}")
    public ResponseEntity<GenericMessage> deleteResposta(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.deleteResposta(id));
    }

    // ========== REVISÃO DE PERGUNTAS ==========

    // Record removido - usar FAQReviewRequest

    @PostMapping(value = "/perguntas/{codigoSequencial}/revisao", 
                 consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<GenericMessage> enviarPerguntaParaRevisao(
            @PathVariable Long codigoSequencial,
            jakarta.servlet.http.HttpServletRequest request,
            JwtAuthenticationToken jwt
    ) {
        String motivo = null;
        
        // Verificar se é JSON
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            try {
                // Ler o corpo JSON manualmente
                java.io.BufferedReader reader = request.getReader();
                StringBuilder jsonBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBody.append(line);
                }
                
                // Parse simples do JSON para extrair o motivo
                String json = jsonBody.toString();
                if (json.contains("\"motivo\"")) {
                    int start = json.indexOf("\"motivo\"") + 9;
                    start = json.indexOf("\"", start) + 1;
                    int end = json.indexOf("\"", start);
                    if (end > start) {
                        motivo = json.substring(start, end);
                    }
                } else if (json.contains("\"motivoRevisao\"")) {
                    int start = json.indexOf("\"motivoRevisao\"") + 16;
                    start = json.indexOf("\"", start) + 1;
                    int end = json.indexOf("\"", start);
                    if (end > start) {
                        motivo = json.substring(start, end);
                    }
                }
                
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(new GenericMessage("Erro ao processar JSON: " + e.getMessage(), 400));
            }
        } else {
            // Processar como form-data
            motivo = request.getParameter("motivoRevisao");
            if (motivo == null) {
                motivo = request.getParameter("motivo");
            }
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new GenericMessage("Campo 'motivo' ou 'motivoRevisao' é obrigatório.", 400));
        }
        
        return ResponseEntity.ok(faqService.enviarPerguntaParaRevisao(codigoSequencial, motivo, jwt.getName()));
    }

    // Endpoint antigo removido - usar /reviewed com parâmetro edit

    @PostMapping("/perguntas/{codigoSequencial}/aprovar")
    public ResponseEntity<GenericMessage> aprovarPergunta(
            @PathVariable Long codigoSequencial,
            @RequestBody FAQApprovalRequest body,
            JwtAuthenticationToken jwt
    ) {
        return ResponseEntity.ok(faqService.aprovarPergunta(codigoSequencial, body.observacaoAprove(), jwt.getName()));
    }

    @PutMapping("/perguntas/{codigoSequencial}/reviewed")
    public ResponseEntity<GenericMessage> sendDataReviewed(
            @PathVariable Long codigoSequencial,
            @ModelAttribute FAQReviewRequest body,
            JwtAuthenticationToken jwt,
            @RequestParam(required = false, name = "edit", defaultValue = "false") boolean edit
    ) {
        return ResponseEntity.ok(faqService.editAfterReview(codigoSequencial, body, jwt.getName(), edit));
    }
}