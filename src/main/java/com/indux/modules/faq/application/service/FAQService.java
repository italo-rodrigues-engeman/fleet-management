package com.indux.modules.faq.application.service;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.domain.model.employee.ContractProject;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.AttachmentRecord;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.faq.application.dto.*;
import com.indux.modules.faq.domain.entities.*;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import com.indux.modules.faq.domain.repository.*;
import com.indux.modules.faq.domain.repository.mongo.PerguntaMongoRepository;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.SimpleProjectEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationHcmRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.SimpleProjectRepository;
import com.indux.modules.whatsapp_media.service.WhatsappMediaService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FAQService {
    
    private final SetorRepository setorRepository;
    private final TemaRepository temaRepository;
    private final PerguntaRepository perguntaRepository;
    private final DatabaseSequenceFaqPerguntaRepository seqPerguntaRepository;
    private final PerguntaMongoRepository perguntaMongoRepository;
    private final RespostaRepository respostaRepository;
    
    private final RegionalRepository regionalRepository;
    private final ContractProjectRepository contractProjectRepository;
    private final StorageService storageService;
    private final WhatsappMediaService whatsappMediaService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationFilialRepository organizationFilialRepository;
    private final SimpleProjectRepository simpleProjectRepository;
    private final OrganizationHcmRepository organizationHcmRepository;

    public Optional<String> getRegionalNameById(Long regionalId) {
        try {
            // Primeiro tenta buscar na tabela tb_organograma
            var organization = organizationRepository.findById(regionalId);
            if (organization.isPresent() && organization.get().getType() == OrganizationType.REGIONAL) {
                // Retorna o cargo (nome da regional) - ex: "Macaé"
                return Optional.of(organization.get().getPosition());
            }
            
            // Se não encontrar, tenta buscar na tabela tb_regional (compatibilidade)
            return regionalRepository.findById(regionalId)
                    .map(Regional::getRegional);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getContratoNomeByRateio(Long contratoRateioId) {
        try {
            return contractProjectRepository.findWithFilialByRateio(contratoRateioId.intValue())
                    .map(ContractProject::getCostCenterName);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getTemaNomeById(Long temaId) {
        try {
            return temaRepository.findById(temaId).map(Tema::getNome);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getSetorNomeById(Long setorId) {
        try {
            return setorRepository.findById(setorId).map(Setor::getNome);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getFilialNomeById(Integer filialId) {
        try {
            return organizationFilialRepository.findById(filialId)
                    .map(FilialHcmEntity::getNomeFilial);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getOrganizationNomeById(Long organizationId) {
        try {
            return organizationRepository.findById(organizationId)
                    .map(com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity::getPosition);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getProjetoNomeById(Long projetoId) {
        try {
            SimpleProjectEntity project = simpleProjectRepository.findById(projetoId).orElse(null);
            if (project == null || project.getHcmId() == null) {
                return Optional.empty();
            }
            return organizationHcmRepository.findById(project.getHcmId())
                    .map(com.indux.modules.organization_chart.domain.entities.jpa.HcmEntity::getNomeCc);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Método para armazenar arquivo
    private FileMetadata storeFile(MultipartFile file, String path, String fileName, Integer etapa) {
        String mimeType = file.getContentType();
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains("."))
            ext = original.substring(original.lastIndexOf('.') + 1);

        Path store = storageService.store(file, path, fileName);
        String uri = storageService.getRootLocation().relativize(store).toString();
        uri = uri.replace("\\", "/");

        return new FileMetadata(uri, ext, mimeType, etapa);
    }

    // Método para criar anexos usando o mesmo padrão do FlashFuel
    private List<AttachmentEntity> createAttachment(List<AttachmentRecord> dtos, String categoria, String publico) {
        List<AttachmentEntity> result = new ArrayList<>();
        if (dtos == null) return result;

        // Verificar se deve usar WhatsAppMedia (categoria FAQ e público EXTERNO)
        boolean useWhatsAppMedia = "FAQ".equalsIgnoreCase(categoria) && "EXTERNO".equalsIgnoreCase(publico);

        for (AttachmentRecord dto : dtos) {
            var id = UUID.randomUUID().toString();
            var item = new AttachmentEntity();
            item.setNome(dto.nome());
            item.setId(id);

            String filename = item.getId() + " - " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .toString().replace(":", "-");

            FileMetadata store;
            if (useWhatsAppMedia) {
                // Usar WhatsAppMedia para anexos acessíveis externamente
                String whatsappPath = whatsappMediaService.upload(dto.file());
                store = new FileMetadata(whatsappPath, extractExtension(dto.file().getOriginalFilename()),
                                       dto.file().getContentType(), 1);
            } else {
                // Usar armazenamento padrão do sistema
                store = storeFile(dto.file(), "faq/anexos", filename, 1);
            }

            item.setFile(store);
            result.add(item);
        }
        return result;
    }

    // Método helper para extrair extensão do arquivo
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // ===== Helpers =====
    private Long normalizeZeroToNull(Long value) {
        return (value != null && value == 0L) ? null : value;
    }

    private boolean isPresent(Long value) {
        return value != null && value != 0L;
    }

    private String normalizeEmptyToNull(String value) {
        return (value != null && value.trim().isEmpty()) ? null : value;
    }

    // ========== SETOR ==========
    
    public GenericMessage createSetor(CreateSetorDTO dto) {
        if (setorRepository.existsByNome(dto.nome())) {

            return new GenericMessage("Setor já existe com este nome.", 400);
        }
        
        Setor setor = new Setor(dto.nome(), dto.status());
        setor.setDescricao(dto.descricao());
        setorRepository.save(setor);
        

        return new GenericMessage("Setor criado com sucesso.", 201);
    }

             public Page<PerguntaResumoDTO> listarPerguntasResumo(Pageable pageable, PerguntaFiltrosDTO filtros) {
        // Usar implementação externa para filtros
        Page<PerguntaMongo> pageDocs = perguntaMongoRepository.findWithFilters(pageable, filtros);

        List<PerguntaMongo> docs = pageDocs.getContent();
        
        // Coletar ids únicos de setor/tema na página
        Set<Long> setorIds = new HashSet<>();
        Set<Long> temaIds = new HashSet<>();
        for (var d : docs) {
            if (d.getSetorId() != null) setorIds.add(d.getSetorId());
            if (d.getTemaId() != null) temaIds.add(d.getTemaId());
        }

        // Pré-carregar nomes em mapas
        Map<Long, String> nomeSetorPorId = new HashMap<>();
        if (!setorIds.isEmpty()) {
            setorRepository.findAllById(setorIds).forEach(s -> nomeSetorPorId.put(s.getId(), s.getNome()));
        }
        Map<Long, String> nomeTemaPorId = new HashMap<>();
        if (!temaIds.isEmpty()) {
            temaRepository.findAllById(temaIds).forEach(t -> nomeTemaPorId.put(t.getId(), t.getNome()));
        }

        var dtos = docs.stream()
                .map(doc -> mapToPerguntaResumoDTO(doc, nomeSetorPorId, nomeTemaPorId))
                .toList();

        // Retornar Page com total correto do MongoDB
        return new PageImpl<>(dtos, pageable, pageDocs.getTotalElements());
    }
    
    public List<SetorDTO> getAllSetores() {
        return setorRepository.findAll().stream()
                .map(SetorDTO::fromEntity)
                .toList();
    }
    
    public Optional<SetorDTO> getSetorById(Long id) {
        return setorRepository.findById(id)
                .map(SetorDTO::fromEntity);
    }
    
    public GenericMessage updateSetor(Long id, CreateSetorDTO dto) {
        Setor setor = setorRepository.findById(id)
                .orElse(null);
        
        if (setor == null) {

            return new GenericMessage("Setor não encontrado.", 404);
        }
        
        setor.setNome(dto.nome());
        setor.setStatus(dto.status());
        setor.setDescricao(dto.descricao());
        setorRepository.save(setor);
        

        return new GenericMessage("Setor atualizado com sucesso.", 200);
    }
    
    public GenericMessage deleteSetor(Long id) {
        if (!setorRepository.existsById(id)) {

            return new GenericMessage("Setor não encontrado.", 404);
        }
        
        // Verificar se existem temas ligados ao setor
        try {
            List<Tema> temasSetor = temaRepository.findBySetorId(id);
            if (!temasSetor.isEmpty()) {
                return new GenericMessage("Não é possível excluir o setor. Existem " + temasSetor.size() + " tema(s) vinculado(s) a ele. Remova os temas primeiro.", 400);
            }
        } catch (Exception e) {
            // Log do erro mas continuar com a verificação
            return new GenericMessage("Erro ao verificar dependências do setor.", 500);
        }

        // Se não houver temas, permitir a exclusão
        setorRepository.deleteById(id);

        return new GenericMessage("Setor excluído com sucesso.", 200);
    }
    
    // ========== TEMA ==========
    
    public GenericMessage createTema(CreateTemaDTO dto) {
        Setor setor = setorRepository.findById(dto.setorId())
                .orElse(null);
        
        if (setor == null) {

            return new GenericMessage("Setor não encontrado.", 404);
        }
        
        if (temaRepository.existsByNome(dto.nome())) {

            return new GenericMessage("Tema já existe com este nome.", 400);
        }
        
        // Define a ordenação automaticamente (última posição + 1)
        Integer novaOrdenacao = temaRepository.findMaxOrdenacao().orElse(0) + 1;

        Tema tema = new Tema(dto.nome(), setor, dto.descricao(), dto.status(), novaOrdenacao);
        temaRepository.save(tema);
        

        return new GenericMessage("Tema criado com sucesso.", 201);
    }
    
    public List<TemaDTO> getAllTemas() {
        return temaRepository.findAllByOrderByOrdenacaoAsc().stream()
                .map(TemaDTO::fromEntity)
                .toList();
    }
    
    public List<TemaDTO> getTemasBySetor(Long setorId) {
        return temaRepository.findBySetorIdOrderByOrdenacaoAsc(setorId).stream()
                .map(TemaDTO::fromEntity)
                .toList();
    }
    
    public Optional<TemaDTO> getTemaById(Long id) {
        return temaRepository.findById(id)
                .map(TemaDTO::fromEntity);
    }
    
    public GenericMessage updateTema(Long id, CreateTemaDTO dto) {
        Tema tema = temaRepository.findById(id)
                .orElse(null);
        
        if (tema == null) {

            return new GenericMessage("Tema não encontrado.", 404);
        }
        
        Setor setor = setorRepository.findById(dto.setorId())
                .orElse(null);
        
        if (setor == null) {

            return new GenericMessage("Setor não encontrado.", 404);
        }
        
        tema.setNome(dto.nome());
        tema.setSetor(setor);
        tema.setDescricao(dto.descricao());
        tema.setStatus(dto.status());

        // Atualiza ordenação se fornecida
        if (dto.ordenacao() != null) {
            tema.setOrdenacao(dto.ordenacao());
        }

        temaRepository.save(tema);
        

        return new GenericMessage("Tema atualizado com sucesso.", 200);
    }
    
    public GenericMessage deleteTema(Long id) {
        if (!temaRepository.existsById(id)) {

            return new GenericMessage("Tema não encontrado.", 404);
        }
        
        // Excluir todas as perguntas relacionadas ao tema (MongoDB)
        try {
            List<PerguntaMongo> perguntasTema =
                perguntaMongoRepository.findByTemaId(id);

            if (!perguntasTema.isEmpty()) {
                perguntaMongoRepository.deleteAll(perguntasTema);
            }
        } catch (Exception e) {
            return new GenericMessage("Não foi possivel excluir as perguntas.", 500);
        }

        // Excluir o tema
        temaRepository.deleteById(id);

        // Reorganiza a ordenação dos temas restantes
        reorganizarOrdenacaoTemas();

        return new GenericMessage("Tema excluído com sucesso.", 200);
    }
    
    public GenericMessage reorganizarTemas(ReordenarTemasDTO dto) {
        try {
            // Obtém todos os temas ordenados
            List<Tema> todosTemas = temaRepository.findAllByOrderByOrdenacaoAsc();

            // Cria um mapa para facilitar acesso aos temas por ID
            Map<Long, Tema> temasMap = todosTemas.stream()
                    .collect(Collectors.toMap(Tema::getId, tema -> tema));

            // Aplica as novas ordens solicitadas
            for (ReordenarTemasDTO.TemaOrdem temaOrdem : dto.temas()) {
                Tema tema = temasMap.get(temaOrdem.id());
                if (tema != null) {
                    tema.setOrdenacao(temaOrdem.novaOrdem());
                }
            }

            // Reorganiza todos os temas para garantir sequência contínua
            reorganizarOrdenacaoTemasInteligente(todosTemas);

            return new GenericMessage("Temas reorganizados com sucesso.", 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao reorganizar temas: " + e.getMessage(), 500);
        }
    }

    private void reorganizarOrdenacaoTemasInteligente(List<Tema> temas) {
        // Ordena os temas pela nova ordenação solicitada
        temas.sort((t1, t2) -> {
            Integer ordem1 = t1.getOrdenacao() != null ? t1.getOrdenacao() : Integer.MAX_VALUE;
            Integer ordem2 = t2.getOrdenacao() != null ? t2.getOrdenacao() : Integer.MAX_VALUE;
            return ordem1.compareTo(ordem2);
        });

        // Aplica sequência contínua (1, 2, 3, 4...)
        for (int i = 0; i < temas.size(); i++) {
            Tema tema = temas.get(i);
            tema.setOrdenacao(i + 1);
            temaRepository.save(tema);
        }
    }

    private void reorganizarOrdenacaoTemas() {
        List<Tema> todosTemas = temaRepository.findAllByOrderByOrdenacaoAsc();
        for (int i = 0; i < todosTemas.size(); i++) {
            Tema tema = todosTemas.get(i);
            if (tema.getOrdenacao() != (i + 1)) {
                tema.setOrdenacao(i + 1);
                temaRepository.save(tema);
            }
        }
    }

    // ========== PERGUNTA ==========
    
    public GenericMessage createPergunta(CreatePerguntaDTO dto, String userId) {
        // valida tema/setor
        if (dto.temaId() == null || temaRepository.findById(dto.temaId()).isEmpty()) {
            return new GenericMessage("Tema não encontrado.", 404);
        }

        // VALIDAÇÃO: Verificar se há público definido (pergunta OU respostas)
        boolean temPublicoPergunta = dto.publico() != null && !dto.publico().trim().isEmpty();
        boolean temPublicoRespostas = false;
        
        if (dto.respostas() != null && !dto.respostas().isEmpty()) {
            temPublicoRespostas = dto.respostas().stream()
                .anyMatch(resposta -> resposta.publico() != null && !resposta.publico().trim().isEmpty());
        }
        

        
        if (!temPublicoPergunta && !temPublicoRespostas) {
            return new GenericMessage("É obrigatório informar o campo 'publico' na pergunta OU em pelo menos uma resposta.", 400);
        }
        
        // VALIDAÇÃO: Verificar se há filialHcmId definida (pergunta OU respostas)
        boolean temFilialHcmPergunta = dto.filialHcmId() != null && !dto.filialHcmId().isEmpty();
        boolean temFilialHcmRespostas = false;
        
        if (dto.respostas() != null && !dto.respostas().isEmpty()) {
            temFilialHcmRespostas = dto.respostas().stream()
                .anyMatch(resposta -> resposta.filialHcmId() != null && !resposta.filialHcmId().isEmpty());
        }
        
        if (!temFilialHcmPergunta && !temFilialHcmRespostas) {
            return new GenericMessage("É obrigatório informar o campo 'filialHcmId' na pergunta OU em pelo menos uma resposta.", 400);
        }
        
        // Aplicar dados globalmente quando categoria for FAQ
        boolean isCategoriaFAQ = "FAQ".equalsIgnoreCase(dto.categoria());
        
        // Se for FAQ, aplicar dados da pergunta globalmente para todas as respostas
        if (isCategoriaFAQ && dto.respostas() != null && !dto.respostas().isEmpty()) {
            for (CreatePerguntaDTO.RespostaItem resposta : dto.respostas()) {
                // Aplicar regional da pergunta apenas se não estiver definida na resposta
                if (resposta.regional() == null && dto.regionalId() != null) {
                    try {
                        java.lang.reflect.Field regionalField = resposta.getClass().getDeclaredField("regional");
                        regionalField.setAccessible(true);
                        regionalField.set(resposta, dto.regionalId());
                    } catch (Exception e) {
                        // Silenciar erro de reflection
                    }
                }
                
                // Aplicar contratos da pergunta apenas se não estiverem definidos na resposta
                if ((resposta.contratos() == null || resposta.contratos().isEmpty()) && 
                    (dto.contratos() != null && !dto.contratos().isEmpty())) {
                    try {
                        java.lang.reflect.Field contratosField = resposta.getClass().getDeclaredField("contratos");
                        contratosField.setAccessible(true);
                        contratosField.set(resposta, new ArrayList<>(dto.contratos()));
                    } catch (Exception e) {
                        // Silenciar erro de reflection
                    }
                }
                
                // Aplicar público da pergunta apenas se não estiver definido na resposta
                if (resposta.publico() == null || resposta.publico().trim().isEmpty()) {
                    if (dto.publico() != null && !dto.publico().trim().isEmpty()) {
                        try {
                            java.lang.reflect.Field publicoField = resposta.getClass().getDeclaredField("publico");
                            publicoField.setAccessible(true);
                            publicoField.set(resposta, dto.publico());
                        } catch (Exception e) {
                            // Silenciar erro de reflection
                        }
                    }
                }
            }
        }

        // Gerar próximo código sequencial
        var seq = seqPerguntaRepository.save(new DatabaseSequenceFaqPergunta());

        // Montar documento Mongo
        var doc = new PerguntaMongo();
        doc.setCodigoSequencial(seq.getId());
        doc.setSetorId(dto.setorId());
        doc.setTemaId(dto.temaId());
        doc.setCategoria(dto.categoria());
        doc.setTipo(dto.tipo());
        doc.setDataCriacao(dto.dataCriacao());
        doc.setDataFim(dto.dataFim());
        doc.setTitulo(dto.titulo());
        // Definir regional e contratos da pergunta (podem ser null se definidos apenas nas respostas)
        doc.setRegionalId(normalizeZeroToNull(dto.regionalId()));
        doc.setContratoId(dto.contratoId());
        if (dto.contratos() != null && !dto.contratos().isEmpty()) {
            doc.setContratos(new ArrayList<>(dto.contratos()));
        }
        // Definir público da pergunta (pode ser null se for definido apenas nas respostas)
        // Se o público da pergunta for string vazia, definir como null
        String publicoPergunta = (dto.publico() != null && !dto.publico().trim().isEmpty()) ? dto.publico() : null;
        doc.setPublico(publicoPergunta);
        doc.setAprovador(dto.aprovador());
        doc.setObservacoes(dto.observacoes());
        doc.setSituacao(dto.situacao() != null ? dto.situacao() : true); // padrão true se não informado
        // Persistir anexos via StorageService e manter caminhos salvos
        if (dto.anexos() != null && !dto.anexos().isEmpty()) {
            // Usar público da pergunta tratado ou fallback para "INTERNO" se não definido
            String publicoParaAnexos = publicoPergunta != null ? publicoPergunta : "INTERNO";
            List<AttachmentEntity> attachments = createAttachment(dto.anexos(), dto.categoria(), publicoParaAnexos);
            doc.setAnexos(attachments);
        }
        
        // Set new fields - normalizar valores 0 para null
        doc.setDiretoriaId(normalizeZeroToNull(dto.diretoriaId()));
        doc.setSuperintendenciaId(normalizeZeroToNull(dto.superintendenciaId()));
        doc.setProjetoId(normalizeZeroToNull(dto.projetoId()));
        if (dto.filialHcmId() != null && !dto.filialHcmId().isEmpty()) {
            doc.setFilialHcmId(new ArrayList<>(dto.filialHcmId()));
        }
        doc.setSetorOrganizationId(normalizeZeroToNull(dto.setorOrganizationId()));
        // Mapear listas de organograma (se presentes)
        if (dto.diretoriaIds() != null && !dto.diretoriaIds().isEmpty()) doc.setDiretoriaIds(new ArrayList<>(dto.diretoriaIds()));
        if (dto.superintendenciaIds() != null && !dto.superintendenciaIds().isEmpty()) doc.setSuperintendenciaIds(new ArrayList<>(dto.superintendenciaIds()));
        if (dto.regionalIds() != null && !dto.regionalIds().isEmpty()) doc.setRegionalIds(new ArrayList<>(dto.regionalIds()));
        if (dto.setorOrganizationIds() != null && !dto.setorOrganizationIds().isEmpty()) doc.setSetorOrganizationIds(new ArrayList<>(dto.setorOrganizationIds()));
        if (dto.projetoIds() != null && !dto.projetoIds().isEmpty()) doc.setProjetoIds(new ArrayList<>(dto.projetoIds()));

        // Regras: salvar somente o último ID e o tipo
        if (dto.filialHcmId() != null && !dto.filialHcmId().isEmpty()) {
            boolean hasZero = dto.filialHcmId().stream().anyMatch(id -> id != null && id == 0L);
                if (hasZero) {
                doc.setRestrito(false);
                // Se listas de organograma foram enviadas, salvar a lista diretamente em valor
                if (dto.diretoriaIds() != null && !dto.diretoriaIds().isEmpty()) {
                    doc.setValor(new ArrayList<>(dto.diretoriaIds()));
                    doc.setTipoValor("DIRETORIA");
                } else if (dto.superintendenciaIds() != null && !dto.superintendenciaIds().isEmpty()) {
                    doc.setValor(new ArrayList<>(dto.superintendenciaIds()));
                    doc.setTipoValor("SUPERINTENDENCIA");
                } else if (dto.regionalIds() != null && !dto.regionalIds().isEmpty()) {
                    doc.setValor(new ArrayList<>(dto.regionalIds()));
                    doc.setTipoValor("REGIONAL");
                } else if (dto.setorOrganizationIds() != null && !dto.setorOrganizationIds().isEmpty()) {
                    doc.setValor(new ArrayList<>(dto.setorOrganizationIds()));
                    doc.setTipoValor("SETOR");
                } else if (dto.projetoIds() != null && !dto.projetoIds().isEmpty()) {
                    doc.setValor(new ArrayList<>(dto.projetoIds()));
                    doc.setTipoValor("PROJETO");
                } else {
                    // Escolher o último nível selecionado conforme ordem: diretoria → superintendência → regional → setor → contrato → projeto
                    Long chosen = null;
                    String tipo = null;
                    if (isPresent(dto.projetoId())) { chosen = dto.projetoId(); tipo = "PROJETO"; }
                    else if (dto.contratos() != null && !dto.contratos().isEmpty()) { chosen = dto.contratos().get(dto.contratos().size()-1); tipo = "CONTRATO"; }
                    else if (isPresent(dto.setorOrganizationId())) { chosen = dto.setorOrganizationId(); tipo = "SETOR"; }
                    else if (isPresent(dto.regionalId())) { chosen = dto.regionalId(); tipo = "REGIONAL"; }
                    else if (isPresent(dto.superintendenciaId())) { chosen = dto.superintendenciaId(); tipo = "SUPERINTENDENCIA"; }
                    else if (isPresent(dto.diretoriaId())) { chosen = dto.diretoriaId(); tipo = "DIRETORIA"; }
                    java.util.List<Long> values = new ArrayList<>();
                    if (chosen != null) { values.add(chosen); }
                    doc.setValor(values);
                    doc.setTipoValor(tipo);
                }
            } else {
                doc.setRestrito(true);
                // salvar toda a lista de filialHcmId em value
                java.util.List<Long> values = new ArrayList<>(dto.filialHcmId());
                doc.setValor(values);
                doc.setTipoValor("FILIAL_HCM");
            }
        }

        if (dto.respostas() != null && !dto.respostas().isEmpty()) {
            List<PerguntaMongo.RespostaEmbedded> emb = new ArrayList<>();
            for (CreatePerguntaDTO.RespostaItem r : dto.respostas()) {
                var e = new PerguntaMongo.RespostaEmbedded();
                e.setConteudo(r.conteudo());
                // Definir regional e contratos da resposta (podem ser null se definidos apenas na pergunta)
                e.setRegional(normalizeZeroToNull(r.regional()));
                e.setContrato(r.contrato());
                if (r.contratos() != null && !r.contratos().isEmpty()) {
                    e.setContratos(new ArrayList<>(r.contratos()));
                }
                e.setPublico(r.publico());
                if (r.anexo() != null && !r.anexo().isEmpty()) {
                    // Usar público da resposta ou fallback para "INTERNO" se não definido
                    String publicoParaAnexos = r.publico() != null ? r.publico() : "INTERNO";
                    List<AttachmentEntity> anexosResposta = createAttachment(r.anexo(), dto.categoria(), publicoParaAnexos);
                    e.setAnexo(anexosResposta);
                }
                // Set new fields for resposta - normalizar valores 0 para null
                e.setDiretoriaId(normalizeZeroToNull(r.diretoriaId()));
                e.setSuperintendenciaId(normalizeZeroToNull(r.superintendenciaId()));
                e.setProjetoId(normalizeZeroToNull(r.projetoId()));
                // listas
                if (r.diretoriaIds() != null && !r.diretoriaIds().isEmpty()) e.setDiretoriaIds(new ArrayList<>(r.diretoriaIds()));
                if (r.superintendenciaIds() != null && !r.superintendenciaIds().isEmpty()) e.setSuperintendenciaIds(new ArrayList<>(r.superintendenciaIds()));
                if (r.regionalIds() != null && !r.regionalIds().isEmpty()) e.setRegionalIds(new ArrayList<>(r.regionalIds()));
                if (r.setorOrganizationIds() != null && !r.setorOrganizationIds().isEmpty()) e.setSetorOrganizationIds(new ArrayList<>(r.setorOrganizationIds()));
                if (r.projetoIds() != null && !r.projetoIds().isEmpty()) e.setProjetoIds(new ArrayList<>(r.projetoIds()));
                if (r.filialHcmId() != null && !r.filialHcmId().isEmpty()) {
                    e.setFilialHcmId(new ArrayList<>(r.filialHcmId()));
                }
                e.setSetorOrganizationId(normalizeZeroToNull(r.setorOrganizationId()));

                // Regras de restrição por resposta (salva somente o último ID e o tipo)
                if (r.filialHcmId() != null && !r.filialHcmId().isEmpty()) {
                    boolean hasZeroR = r.filialHcmId().stream().anyMatch(fid -> fid != null && fid == 0L);
                    if (hasZeroR) {
                        e.setRestrito(false);
                        // Se listas foram enviadas, salvar diretamente
                        if (r.diretoriaIds() != null && !r.diretoriaIds().isEmpty()) {
                            e.setValor(new ArrayList<>(r.diretoriaIds()));
                            e.setTipoValor("DIRETORIA");
                        } else if (r.superintendenciaIds() != null && !r.superintendenciaIds().isEmpty()) {
                            e.setValor(new ArrayList<>(r.superintendenciaIds()));
                            e.setTipoValor("SUPERINTENDENCIA");
                        } else if (r.regionalIds() != null && !r.regionalIds().isEmpty()) {
                            e.setValor(new ArrayList<>(r.regionalIds()));
                            e.setTipoValor("REGIONAL");
                        } else if (r.setorOrganizationIds() != null && !r.setorOrganizationIds().isEmpty()) {
                            e.setValor(new ArrayList<>(r.setorOrganizationIds()));
                            e.setTipoValor("SETOR");
                        } else if (r.projetoIds() != null && !r.projetoIds().isEmpty()) {
                            e.setValor(new ArrayList<>(r.projetoIds()));
                            e.setTipoValor("PROJETO");
                        } else {
                            Long chosenR = null;
                            String tipoR = null;
                            if (isPresent(r.projetoId())) { chosenR = r.projetoId(); tipoR = "PROJETO"; }
                            else if (r.contratos() != null && !r.contratos().isEmpty()) { chosenR = r.contratos().get(r.contratos().size()-1); tipoR = "CONTRATO"; }
                            else if (isPresent(r.setorOrganizationId())) { chosenR = r.setorOrganizationId(); tipoR = "SETOR"; }
                            else if (isPresent(r.regional())) { chosenR = r.regional(); tipoR = "REGIONAL"; }
                            else if (isPresent(r.superintendenciaId())) { chosenR = r.superintendenciaId(); tipoR = "SUPERINTENDENCIA"; }
                            else if (isPresent(r.diretoriaId())) { chosenR = r.diretoriaId(); tipoR = "DIRETORIA"; }
                            java.util.List<Long> valuesR = new ArrayList<>();
                            if (chosenR != null) valuesR.add(chosenR);
                            e.setValor(valuesR);
                            e.setTipoValor(tipoR);
                        }
                    } else {
                        e.setRestrito(true);
                        // salvar toda a lista de filialHcmId em value
                        java.util.List<Long> valuesR = new ArrayList<>(r.filialHcmId());
                        e.setValor(valuesR);
                        e.setTipoValor("FILIAL_HCM");
                    }
                }
                emb.add(e);
            }
            doc.setRespostas(emb);

        }

        // Status inicial e StepLog (estilo OCF)
        doc.setStatus("PENDENTE");
        doc.setCurrentStep(2); // após criação, fluxo vai para etapa 2 (aprovação)
        
        // Gerar contador sequencial para o step log desta pergunta específica
        Integer stepCounter = 1; // Primeira etapa sempre começa com 1
        
        List<PerguntaMongo.StepLogEmbedded> logs = new ArrayList<>();
        var first = new PerguntaMongo.StepLogEmbedded();
        first.setId(UUID.randomUUID().toString());
        first.setName("Criada");
        first.setStep(1); // log de quem criou (etapa 1), como OCF
        first.setCreated_at(new Date());
        first.setUser(userId);
        first.setObservation("Pergunta criada e aguardando aprovação");
        first.setStepCounter(stepCounter); // Contador sequencial
        doc.setStepLog(logs);
        logs.add(first);

        var saved = perguntaMongoRepository.save(doc);
        seq.setDocumentId(saved.getId());
        seqPerguntaRepository.save(seq);

        return new GenericMessage("Pergunta criada com sucesso.", 201);
    }
    
    public List<PerguntaDTO> getAllPerguntas() {
        return perguntaRepository.findAll().stream()
                .map(PerguntaDTO::fromEntity)
                .toList();
    }
    
        public Page<PerguntaResumoDTO> getPerguntasByTema(Long temaId, Pageable pageable, PerguntaTemaFiltrosDTO filtros) {
        // Usar implementação externa para filtros por tema
        var pageDocs = perguntaMongoRepository.findWithFiltersByTema(temaId, pageable, filtros);

        Set<Long> setorIds = new HashSet<>();
        Set<Long> temaIds = new HashSet<>();
        for (var d : pageDocs.getContent()) {
            if (d.getSetorId() != null) setorIds.add(d.getSetorId());
            if (d.getTemaId() != null) temaIds.add(d.getTemaId());
        }
        Map<Long, String> nomeSetorPorId = new HashMap<>();
        if (!setorIds.isEmpty()) {
            setorRepository.findAllById(setorIds).forEach(s -> nomeSetorPorId.put(s.getId(), s.getNome()));
        }
        Map<Long, String> nomeTemaPorId = new HashMap<>();
        if (!temaIds.isEmpty()) {
            temaRepository.findAllById(temaIds).forEach(t -> nomeTemaPorId.put(t.getId(), t.getNome()));
        }
        List<PerguntaMongo> docs = pageDocs.getContent();
        if (filtros.hasPublicoFilter()) {
            String needle = filtros.getPublico().toLowerCase();
            docs = docs.stream()
                    .filter(d -> {
                        // Match no público da pergunta
                        String p = d.getPublico();
                        boolean perguntaMatch = p != null && p.toLowerCase().contains(needle);
                        // Ou match em qualquer resposta
                        boolean respostaMatch = false;
                        if (!perguntaMatch && d.getRespostas() != null) {
                            respostaMatch = d.getRespostas().stream().anyMatch(r -> {
                                String pr = r.getPublico();
                                return pr != null && pr.toLowerCase().contains(needle);
                            });
                        }
                        return perguntaMatch || respostaMatch;
                    })
                    .toList();
        }

        var dtos = docs.stream().map(doc -> {
            String nomeSetor = doc.getSetorId() == null ? null : nomeSetorPorId.get(doc.getSetorId());
            String nomeTema = doc.getTemaId() == null ? null : nomeTemaPorId.get(doc.getTemaId());
            int total = doc.getRespostas() == null ? 0 : doc.getRespostas().size();
                    String regionalName = null;
        if (doc.getRegionalId() != null) {
            regionalName = regionalRepository.findById(doc.getRegionalId())
                    .map(Regional::getRegional)
                    .orElse(null);
        }
                    String contratoNome = null;
        if (doc.getContratoId() != null) {
            contratoNome = contractProjectRepository.findWithFilialByRateio(doc.getContratoId().intValue())
                    .map(ContractProject::getCostCenterName)
                    .orElse(null);
        }
                         return new PerguntaResumoDTO(
                     doc.getCodigoSequencial(),
                     nomeSetor,
                     nomeTema,
                     doc.getTitulo(),
                     total,
                     doc.getStatus(),
                     doc.getCategoria(),
                     regionalName,
                     doc.getCurrentStep(),
                     doc.getDataCriacao(),
                     doc.getDataFim(),
                     doc.getSituacao(),
                     doc.getId()
             );
        }).toList();
        return new PageImpl<>(dtos, pageable, dtos.size());
    }
    
    public Optional<PerguntaDTO> getPerguntaById(Long id) {
        return perguntaRepository.findById(id)
                .map(PerguntaDTO::fromEntity);
    }
    
    public GenericMessage updatePergunta(Long id, CreatePerguntaDTO dto) {
        Pergunta pergunta = perguntaRepository.findById(id)
                .orElse(null);
        
        if (pergunta == null) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }
        
        Tema tema = temaRepository.findById(dto.temaId())
                .orElse(null);
        
        if (tema == null) {
            return new GenericMessage("Tema não encontrado.", 404);
        }
        
        pergunta.setNome(dto.titulo());
        pergunta.setTema(tema);
        perguntaRepository.save(pergunta);
        
        return new GenericMessage("Pergunta atualizada com sucesso.", 200);
    }

    // Método antigo removido - usar editAfterReview

    public GenericMessage deletePergunta(Long id) {
        if (!perguntaRepository.existsById(id)) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }
        
        perguntaRepository.deleteById(id);
        return new GenericMessage("Pergunta excluída com sucesso.", 200);
    }
    
    // ========== RESPOSTA ==========
    
    public GenericMessage createResposta(CreateRespostaDTO dto) {
        Pergunta pergunta = perguntaRepository.findById(dto.perguntaId())
                .orElse(null);
        
        if (pergunta == null) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }
        
        if (respostaRepository.existsByNome(dto.nome())) {
            return new GenericMessage("Resposta já existe com este nome.", 400);
        }
        
        Resposta resposta = new Resposta(dto.nome(), pergunta, dto.contrato(), dto.status());
        respostaRepository.save(resposta);
        
        return new GenericMessage("Resposta criada com sucesso.", 201);
    }
    
    public List<RespostaDTO> getAllRespostas() {
        return respostaRepository.findAll().stream()
                .map(RespostaDTO::fromEntity)
                .toList();
    }
    
    public List<RespostaDTO> getRespostasByPergunta(Long perguntaId) {
        return respostaRepository.findByPerguntaId(perguntaId).stream()
                .map(RespostaDTO::fromEntity)
                .toList();
    }
    
    public List<RespostaDTO> getRespostasByContrato(Integer contrato) {
        return respostaRepository.findByContrato(contrato).stream()
                .map(RespostaDTO::fromEntity)
                .toList();
    }
    
    public Optional<RespostaDTO> getRespostaById(Long id) {
        return respostaRepository.findById(id)
                .map(RespostaDTO::fromEntity);
    }
    
    public GenericMessage updateResposta(Long id, CreateRespostaDTO dto) {
        Resposta resposta = respostaRepository.findById(id)
                .orElse(null);
        
        if (resposta == null) {
            return new GenericMessage("Resposta não encontrada.", 404);
        }
        
        Pergunta pergunta = perguntaRepository.findById(dto.perguntaId())
                .orElse(null);
        
        if (pergunta == null) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }
        
        resposta.setNome(dto.nome());
        resposta.setPergunta(pergunta);
        resposta.setContrato(dto.contrato());
        resposta.setStatus(dto.status());
        respostaRepository.save(resposta);
        
        return new GenericMessage("Resposta atualizada com sucesso.", 200);
    }
    
    public GenericMessage deleteResposta(Long id) {
        if (!respostaRepository.existsById(id)) {
            return new GenericMessage("Resposta não encontrada.", 404);
        }
        
        respostaRepository.deleteById(id);
        return new GenericMessage("Resposta excluída com sucesso.", 200);
    }

    // ========== FLUXO DE REVISÃO (FAQ) ==========

    public GenericMessage enviarPerguntaParaRevisao(Long codigoSequencial, String motivo, String userId) {
        var opt = perguntaMongoRepository.findByCodigoSequencial(codigoSequencial);
        if (opt.isEmpty()) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }

        var doc = opt.get();
        
        // Verificar se pode ir para revisão
        // Permitir revisão de perguntas aprovadas ou em outras etapas
        if (doc.getCurrentStep() != null && doc.getCurrentStep() == 0 && "APROVADO".equals(doc.getStatus())) {
            // Pergunta aprovada pode ir para revisão (reset para etapa 1)
            doc.setCurrentStep(1);
        } else if (doc.getCurrentStep() != null && doc.getCurrentStep() <= 1 && !"APROVADO".equals(doc.getStatus())) {
            // Não pode voltar além da primeira etapa (exceto se for aprovada)
            return new GenericMessage("Não é possível voltar além da primeira etapa.", 400);
        } else if (doc.getCurrentStep() == null) {
            // Se não tem currentStep, definir como 1
            doc.setCurrentStep(1);
        }
        
        doc.setStatus("REVISAO");
        doc.setCurrentStep(1);

        var step = new PerguntaMongo.StepLogEmbedded();
        step.setId(UUID.randomUUID().toString());
        step.setName("Solicitação de Revisão");
        step.setStep(1);
        step.setCreated_at(new Date());
        step.setUser(userId);
        step.setObservation(motivo);
        // NOTA: Revisão NÃO gera contador sequencial
        
        List<PerguntaMongo.StepLogEmbedded> steps = doc.getStepLog();
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
        doc.setStepLog(steps);

        perguntaMongoRepository.save(doc);
        return new GenericMessage("Pergunta enviada para revisão.", 200);
    }

    public GenericMessage aprovarPergunta(Long codigoSequencial, String observacaoAprove, String userId) {
        var opt = perguntaMongoRepository.findByCodigoSequencial(codigoSequencial);
        if (opt.isEmpty()) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }

        var doc = opt.get();
        doc.setStatus("APROVADO");
        doc.setCurrentStep(0);

        var step = new PerguntaMongo.StepLogEmbedded();
        step.setId(UUID.randomUUID().toString());
        step.setName("Aprovada");
        step.setStep(0);
        step.setCreated_at(new Date());
        step.setUser(userId);
        step.setObservation(observacaoAprove);
        
        // Gerar contador sequencial para o step log desta pergunta específica
        Integer stepCounter = doc.getStepLog() != null ? 
            doc.getStepLog().stream()
                .map(PerguntaMongo.StepLogEmbedded::getStepCounter)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1 : 1;
        step.setStepCounter(stepCounter);

        List<PerguntaMongo.StepLogEmbedded> steps = doc.getStepLog();
        if (steps == null) steps = new ArrayList<>();
        steps.add(step);
        doc.setStepLog(steps);

        perguntaMongoRepository.save(doc);
        return new GenericMessage("Pergunta aprovada com sucesso.", 200);
    }

    public GenericMessage editAfterReview(Long codigoSequencial, FAQReviewRequest dto, String userId, boolean isEdit) {
        var opt = perguntaMongoRepository.findByCodigoSequencial(codigoSequencial);
        if (opt.isEmpty()) {
            return new GenericMessage("Pergunta não encontrada.", 404);
        }
        
        var doc = opt.get();
        
        // Verificar se está em revisão (exceto se for edição)
        if (!isEdit && !"REVISAO".equals(doc.getStatus())) {
            return new GenericMessage("Só é possível editar perguntas que estão em revisão.", 400);
        }
        
        // Atualizar campos parciais
        updatePerguntaAfterReview(doc, dto, userId, isEdit);
        
        // Salvar a pergunta
        perguntaMongoRepository.save(doc);
        
        return new GenericMessage(
            isEdit ? "Pergunta editada durante revisão." : "Revisão finalizada e pergunta enviada para aprovação.", 
            200
        );
    }
    
    private void updatePerguntaAfterReview(PerguntaMongo doc, FAQReviewRequest dto, String userId, boolean isEdit) {
        // Atualizações parciais - sempre atualiza se o campo foi enviado
        if (dto.setorId() != null) doc.setSetorId(dto.setorId());
        if (dto.temaId() != null) doc.setTemaId(dto.temaId());
        if (dto.categoria() != null && !dto.categoria().trim().isEmpty()) doc.setCategoria(dto.categoria());
        if (dto.tipo() != null && !dto.tipo().trim().isEmpty()) doc.setTipo(dto.tipo());
        if (dto.dataCriacao() != null) doc.setDataCriacao(dto.dataCriacao());
        if (dto.dataFim() != null) doc.setDataFim(dto.dataFim());
        if (dto.titulo() != null && !dto.titulo().trim().isEmpty()) doc.setTitulo(dto.titulo());
        // Sempre atualizar regionalId - se for 0, normaliza para null (limpa)
        if (dto.regionalId() != null) {
            doc.setRegionalId(normalizeZeroToNull(dto.regionalId()));
        } else {
            // Se não foi enviado, manter existente (não limpar)
            // Mas se foi enviado explicitamente como null, já foi tratado acima
        }
        
        // Contratos: se foi enviado (mesmo que vazio), atualizar
        if (dto.contratos() != null) {
            // Se lista vazia, limpar contratos existentes
            if (dto.contratos().isEmpty()) {
                doc.setContratos(null);
            } else {
                doc.setContratos(dto.contratos());
            }
        }
        // Se contratos não foi enviado (null), preservar existente
        // Normalizar string vazia para null no campo público
        // Sempre atualiza se o campo foi enviado (mesmo que seja string vazia)
        if (dto.publico() != null) {
            String publicoNormalizado = normalizeEmptyToNull(dto.publico());
            doc.setPublico(publicoNormalizado);
        }
        if (dto.aprovador() != null && !dto.aprovador().trim().isEmpty()) doc.setAprovador(dto.aprovador());
        if (dto.observacoes() != null) doc.setObservacoes(dto.observacoes());
        if (dto.situacao() != null) doc.setSituacao(dto.situacao());
        
        // Handle new fields - normalizar valores 0 para null
        if (dto.diretoriaId() != null) doc.setDiretoriaId(normalizeZeroToNull(dto.diretoriaId()));
        if (dto.superintendenciaId() != null) doc.setSuperintendenciaId(normalizeZeroToNull(dto.superintendenciaId()));
        if (dto.projetoId() != null) doc.setProjetoId(normalizeZeroToNull(dto.projetoId()));
        // FilialHcmId: se foi enviado (mesmo que vazio), atualizar
        if (dto.filialHcmId() != null) {
            // Se lista vazia, limpar filialHcmId existentes
            if (dto.filialHcmId().isEmpty()) {
                doc.setFilialHcmId(null);
            } else {
                doc.setFilialHcmId(dto.filialHcmId());
            }
        }
        // Se filialHcmId não foi enviado (null), preservar existente
        if (dto.setorOrganizationId() != null) doc.setSetorOrganizationId(normalizeZeroToNull(dto.setorOrganizationId()));
        
        // Atualizar listas de organograma se enviadas
        // Nota: FAQReviewRequest não tem essas listas, mas vamos garantir que sejam preservadas se não enviadas

        // Recalcular restrições e tipo (update): sempre que filial/org mudarem
        // Ordem de prioridade: DIRETORIA > SUPERINTENDENCIA > REGIONAL > SETOR > CONTRATO > PROJETO
        // Verificar se algum campo de organograma foi enviado
        boolean temCamposOrganograma = isPresent(dto.diretoriaId()) || isPresent(dto.superintendenciaId()) || 
                                       isPresent(dto.regionalId()) || isPresent(dto.setorOrganizationId()) || 
                                       isPresent(dto.projetoId()) || (dto.contratos() != null && !dto.contratos().isEmpty());
        
        if (dto.filialHcmId() != null) {
            if (!dto.filialHcmId().isEmpty()) {
                boolean hasZero = dto.filialHcmId().stream().anyMatch(id -> id != null && id == 0L);
                if (hasZero) {
                    doc.setRestrito(false);
                    Long chosen = null; 
                    String tipo = null;
                    // Ordem correta: diretoria primeiro (maior nível hierárquico)
                    if (isPresent(dto.diretoriaId())) { 
                        chosen = dto.diretoriaId(); 
                        tipo = "DIRETORIA"; 
                    }
                    else if (isPresent(dto.superintendenciaId())) { 
                        chosen = dto.superintendenciaId(); 
                        tipo = "SUPERINTENDENCIA"; 
                    }
                    else if (isPresent(dto.regionalId())) { 
                        chosen = dto.regionalId(); 
                        tipo = "REGIONAL"; 
                    }
                    else if (isPresent(dto.setorOrganizationId())) { 
                        chosen = dto.setorOrganizationId(); 
                        tipo = "SETOR"; 
                    }
                    else if (dto.contratos() != null && !dto.contratos().isEmpty()) { 
                        chosen = dto.contratos().get(dto.contratos().size()-1); 
                        tipo = "CONTRATO"; 
                    }
                    else if (isPresent(dto.projetoId())) { 
                        chosen = dto.projetoId(); 
                        tipo = "PROJETO"; 
                    }
                    java.util.List<Long> values = new ArrayList<>();
                    if (chosen != null) {
                        values.add(chosen); // Apenas um valor
                    }
                    doc.setValor(values);
                    doc.setTipoValor(tipo);
                } else {
                    doc.setRestrito(true);
                    java.util.List<Long> values = new ArrayList<>(dto.filialHcmId());
                    doc.setValor(values);
                    doc.setTipoValor("FILIAL_HCM");
                }
            } else {
                // Lista vazia: se há campos de organograma, recalcular; senão, limpar
                if (temCamposOrganograma) {
                    doc.setRestrito(false);
                    Long chosen = null; 
                    String tipo = null;
                    // Ordem correta: diretoria primeiro
                    if (isPresent(dto.diretoriaId())) { 
                        chosen = dto.diretoriaId(); 
                        tipo = "DIRETORIA"; 
                    }
                    else if (isPresent(dto.superintendenciaId())) { 
                        chosen = dto.superintendenciaId(); 
                        tipo = "SUPERINTENDENCIA"; 
                    }
                    else if (isPresent(dto.regionalId())) { 
                        chosen = dto.regionalId(); 
                        tipo = "REGIONAL"; 
                    }
                    else if (isPresent(dto.setorOrganizationId())) { 
                        chosen = dto.setorOrganizationId(); 
                        tipo = "SETOR"; 
                    }
                    else if (dto.contratos() != null && !dto.contratos().isEmpty()) { 
                        chosen = dto.contratos().get(dto.contratos().size()-1); 
                        tipo = "CONTRATO"; 
                    }
                    else if (isPresent(dto.projetoId())) { 
                        chosen = dto.projetoId(); 
                        tipo = "PROJETO"; 
                    }
                    if (chosen != null) {
                        java.util.List<Long> values = new ArrayList<>();
                        values.add(chosen); // Apenas um valor
                        doc.setValor(values);
                        doc.setTipoValor(tipo);
                    } else {
                        doc.setRestrito(null);
                        doc.setValor(null);
                        doc.setTipoValor(null);
                    }
                } else {
                    // Sem campos de organograma: limpar restrições
                    doc.setRestrito(null);
                    doc.setValor(null);
                    doc.setTipoValor(null);
                }
            }
        } else {
            // Se não veio filial, recalcular se há campos de organograma, ou limpar se estava restrito=true
            if (temCamposOrganograma) {
                // Recalcular com base nos campos enviados
                doc.setRestrito(false);
                Long chosen = null; 
                String tipo = null;
                // Ordem correta: diretoria primeiro
                if (isPresent(dto.diretoriaId())) { 
                    chosen = dto.diretoriaId(); 
                    tipo = "DIRETORIA"; 
                }
                else if (isPresent(dto.superintendenciaId())) { 
                    chosen = dto.superintendenciaId(); 
                    tipo = "SUPERINTENDENCIA"; 
                }
                else if (isPresent(dto.regionalId())) { 
                    chosen = dto.regionalId(); 
                    tipo = "REGIONAL"; 
                }
                else if (isPresent(dto.setorOrganizationId())) { 
                    chosen = dto.setorOrganizationId(); 
                    tipo = "SETOR"; 
                }
                else if (dto.contratos() != null && !dto.contratos().isEmpty()) { 
                    chosen = dto.contratos().get(dto.contratos().size()-1); 
                    tipo = "CONTRATO"; 
                }
                else if (isPresent(dto.projetoId())) { 
                    chosen = dto.projetoId(); 
                    tipo = "PROJETO"; 
                }
                if (chosen != null) {
                    java.util.List<Long> values = new ArrayList<>();
                    values.add(chosen); // Apenas um valor
                    doc.setValor(values);
                    doc.setTipoValor(tipo);
                } else {
                    // Se nenhum campo válido foi encontrado, limpar
                    doc.setValor(null);
                    doc.setTipoValor(null);
                }
            } else {
                // Se não há campos de organograma e não veio filial, limpar se estava restrito=true
                if (doc.getRestrito() != null && doc.getRestrito()) {
                    doc.setRestrito(null);
                    doc.setValor(null);
                    doc.setTipoValor(null);
                }
                // Se estava restrito=false, manter valores existentes (não alterar)
            }
        }
        
        // Processar anexos
        if (dto.anexosRemover() != null && !dto.anexosRemover().isEmpty()) {
            // Remover anexos especificados
            doc.setAnexos(doc.getAnexos().stream()
                .filter(anexo -> !dto.anexosRemover().contains(anexo.getId()))
                .collect(Collectors.toList()));
        }
        
        if (dto.anexos() != null && !dto.anexos().isEmpty()) {
            // Usar público da pergunta ou fallback para "INTERNO" se não definido
            String publicoParaAnexos = doc.getPublico() != null ? doc.getPublico() : "INTERNO";
            List<AttachmentEntity> newAttachments = createAttachmentFromMultipartFiles(dto.anexos(), doc.getCategoria(), publicoParaAnexos);
            if (doc.getAnexos() == null) {
                doc.setAnexos(new ArrayList<>());
            }
            doc.getAnexos().addAll(newAttachments);
        }
        
        // Processar respostas
        if (dto.respostas() != null) {
            List<PerguntaMongo.RespostaEmbedded> respostasAtualizadas = new ArrayList<>();
            
            for (int i = 0; i < dto.respostas().size(); i++) {
                FAQReviewRequest.RespostaItem r = dto.respostas().get(i);
                
                // Verificar se existe uma resposta na posição i
                PerguntaMongo.RespostaEmbedded respostaExistente = null;
                if (doc.getRespostas() != null && i < doc.getRespostas().size()) {
                    respostaExistente = doc.getRespostas().get(i);
                }
                
                var e = new PerguntaMongo.RespostaEmbedded();
                if (r.conteudo() != null && !r.conteudo().trim().isEmpty()) {
                    e.setConteudo(r.conteudo());
                } else if (respostaExistente != null) {
                    e.setConteudo(respostaExistente.getConteudo());
                }
                // Definir regional e público da resposta
                // Sempre atualizar (mesmo que null) - se foi enviado como null, limpar
                e.setRegional(normalizeZeroToNull(r.regional()));
                // Normalizar público: string vazia vira null
                String publicoRespostaNormalizado = normalizeEmptyToNull(r.publico());
                e.setPublico(publicoRespostaNormalizado);
                
                // Definir contratos da resposta: se foi enviado (mesmo que vazio), atualizar
                if (r.contratos() != null) {
                    // Se lista vazia, limpar contratos existentes
                    if (r.contratos().isEmpty()) {
                        e.setContratos(null);
                    } else {
                        e.setContratos(r.contratos());
                    }
                } else if (respostaExistente != null) {
                    // Se contratos não foi enviado (null), preservar existente
                    e.setContratos(respostaExistente.getContratos());
                }
                
                // Handle new fields for resposta - normalizar valores 0 para null
                // Se campo foi enviado (mesmo que null), atualizar; caso contrário, preservar existente
                // Nota: Em records Java, não podemos distinguir entre "não enviado" e "enviado como null"
                // Por isso, sempre atualizamos se o campo existe no DTO (mesmo que null)
                e.setDiretoriaId(normalizeZeroToNull(r.diretoriaId()));
                e.setSuperintendenciaId(normalizeZeroToNull(r.superintendenciaId()));
                e.setProjetoId(normalizeZeroToNull(r.projetoId()));
                e.setSetorOrganizationId(normalizeZeroToNull(r.setorOrganizationId()));
                // FilialHcmId: se foi enviado (mesmo que vazio), atualizar
                if (r.filialHcmId() != null) {
                    // Se lista vazia, limpar filialHcmId existentes
                    if (r.filialHcmId().isEmpty()) {
                        e.setFilialHcmId(null);
                    } else {
                        e.setFilialHcmId(r.filialHcmId());
                    }
                } else if (respostaExistente != null) {
                    // Se filialHcmId não foi enviado (null), preservar existente
                    e.setFilialHcmId(respostaExistente.getFilialHcmId());
                }

                // Recalcular restrições e tipo na resposta quando filialHcmId foi enviado
                // Ordem de prioridade: DIRETORIA > SUPERINTENDENCIA > REGIONAL > SETOR > CONTRATO > PROJETO
                // Verificar se algum campo de organograma foi enviado na resposta
                Long regionalNormalizado = normalizeZeroToNull(r.regional());
                boolean temCamposOrganogramaResposta = isPresent(r.diretoriaId()) || isPresent(r.superintendenciaId()) || 
                                                      isPresent(regionalNormalizado) || isPresent(r.setorOrganizationId()) || 
                                                      isPresent(r.projetoId()) || (r.contratos() != null && !r.contratos().isEmpty());
                
                if (r.filialHcmId() != null) {
                    if (!r.filialHcmId().isEmpty()) {
                        boolean hasZeroR = r.filialHcmId().stream().anyMatch(fid -> fid != null && fid == 0L);
                        if (hasZeroR) {
                            e.setRestrito(false);
                            // Ordem correta: diretoria primeiro (maior nível hierárquico)
                            Long chosenR = null;
                            String tipoR = null;
                            if (isPresent(r.diretoriaId())) { 
                                chosenR = r.diretoriaId(); 
                                tipoR = "DIRETORIA"; 
                            }
                            else if (isPresent(r.superintendenciaId())) { 
                                chosenR = r.superintendenciaId(); 
                                tipoR = "SUPERINTENDENCIA"; 
                            }
                            else if (isPresent(regionalNormalizado)) { 
                                chosenR = regionalNormalizado; 
                                tipoR = "REGIONAL"; 
                            }
                            else if (isPresent(r.setorOrganizationId())) { 
                                chosenR = r.setorOrganizationId(); 
                                tipoR = "SETOR"; 
                            }
                            else if (r.contratos() != null && !r.contratos().isEmpty()) { 
                                chosenR = r.contratos().get(r.contratos().size()-1); 
                                tipoR = "CONTRATO"; 
                            }
                            else if (isPresent(r.projetoId())) { 
                                chosenR = r.projetoId(); 
                                tipoR = "PROJETO"; 
                            }
                            java.util.List<Long> valuesR = new ArrayList<>();
                            if (chosenR != null) {
                                valuesR.add(chosenR); // Apenas um valor
                            }
                            e.setValor(valuesR);
                            e.setTipoValor(tipoR);
                        } else {
                            e.setRestrito(true);
                            // salvar toda a lista em value
                            java.util.List<Long> valuesR = new ArrayList<>(r.filialHcmId());
                            e.setValor(valuesR);
                            e.setTipoValor("FILIAL_HCM");
                        }
                    } else {
                        // Lista vazia: se há campos de organograma, recalcular; senão, limpar
                        if (temCamposOrganogramaResposta) {
                            e.setRestrito(false);
                            Long chosenR = null;
                            String tipoR = null;
                            // Ordem correta: diretoria primeiro
                            if (isPresent(r.diretoriaId())) { 
                                chosenR = r.diretoriaId(); 
                                tipoR = "DIRETORIA"; 
                            }
                            else if (isPresent(r.superintendenciaId())) { 
                                chosenR = r.superintendenciaId(); 
                                tipoR = "SUPERINTENDENCIA"; 
                            }
                            else if (isPresent(regionalNormalizado)) { 
                                chosenR = regionalNormalizado; 
                                tipoR = "REGIONAL"; 
                            }
                            else if (isPresent(r.setorOrganizationId())) { 
                                chosenR = r.setorOrganizationId(); 
                                tipoR = "SETOR"; 
                            }
                            else if (r.contratos() != null && !r.contratos().isEmpty()) { 
                                chosenR = r.contratos().get(r.contratos().size()-1); 
                                tipoR = "CONTRATO"; 
                            }
                            else if (isPresent(r.projetoId())) { 
                                chosenR = r.projetoId(); 
                                tipoR = "PROJETO"; 
                            }
                            if (chosenR != null) {
                                java.util.List<Long> valuesR = new ArrayList<>();
                                valuesR.add(chosenR); // Apenas um valor
                                e.setValor(valuesR);
                                e.setTipoValor(tipoR);
                            } else {
                                e.setRestrito(null);
                                e.setValor(null);
                                e.setTipoValor(null);
                            }
                        } else {
                            // Sem campos de organograma: limpar restrições
                            e.setRestrito(null);
                            e.setValor(null);
                            e.setTipoValor(null);
                        }
                    }
                } else {
                    // Se não veio filial, recalcular se há campos de organograma, ou limpar se estava restrito=true
                    if (temCamposOrganogramaResposta) {
                        // Recalcular com base nos campos enviados
                        e.setRestrito(false);
                        Long chosenR = null;
                        String tipoR = null;
                        // Ordem correta: diretoria primeiro
                        if (isPresent(r.diretoriaId())) { 
                            chosenR = r.diretoriaId(); 
                            tipoR = "DIRETORIA"; 
                        }
                        else if (isPresent(r.superintendenciaId())) { 
                            chosenR = r.superintendenciaId(); 
                            tipoR = "SUPERINTENDENCIA"; 
                        }
                        else if (isPresent(regionalNormalizado)) { 
                            chosenR = regionalNormalizado; 
                            tipoR = "REGIONAL"; 
                        }
                        else if (isPresent(r.setorOrganizationId())) { 
                            chosenR = r.setorOrganizationId(); 
                            tipoR = "SETOR"; 
                        }
                        else if (r.contratos() != null && !r.contratos().isEmpty()) { 
                            chosenR = r.contratos().get(r.contratos().size()-1); 
                            tipoR = "CONTRATO"; 
                        }
                        else if (isPresent(r.projetoId())) { 
                            chosenR = r.projetoId(); 
                            tipoR = "PROJETO"; 
                        }
                        if (chosenR != null) {
                            java.util.List<Long> valuesR = new ArrayList<>();
                            valuesR.add(chosenR); // Apenas um valor
                            e.setValor(valuesR);
                            e.setTipoValor(tipoR);
                        } else {
                            // Se nenhum campo válido foi encontrado, limpar
                            e.setValor(null);
                            e.setTipoValor(null);
                        }
                    } else {
                        // Se não há campos de organograma e não veio filial, limpar se estava restrito=true
                        if (respostaExistente != null && respostaExistente.getRestrito() != null && respostaExistente.getRestrito()) {
                            e.setRestrito(null);
                            e.setValor(null);
                            e.setTipoValor(null);
                        } else if (respostaExistente != null) {
                            // Preservar valores existentes se estava restrito=false
                            e.setRestrito(respostaExistente.getRestrito());
                            e.setValor(respostaExistente.getValor());
                            e.setTipoValor(respostaExistente.getTipoValor());
                        }
                    }
                }
                
                // Lógica para anexos: preservar existentes + adicionar novos + remover especificados
                List<AttachmentEntity> anexosResposta = new ArrayList<>();
                
                // Preservar anexos existentes se houver
                if (respostaExistente != null && respostaExistente.getAnexo() != null) {
                    anexosResposta.addAll(respostaExistente.getAnexo());
                }
                
                // Remover anexos especificados em anexosRemover
                if (r.anexosRemover() != null && !r.anexosRemover().isEmpty()) {
                    anexosResposta.removeIf(anexo -> r.anexosRemover().contains(anexo.getId()));
                }
                
                // Adicionar novos anexos se enviados
                if (r.anexo() != null && !r.anexo().isEmpty()) {
                    // Usar público da resposta ou fallback para "INTERNO" se não definido
                    String publicoParaAnexos = r.publico() != null ? r.publico() : "INTERNO";
                    List<AttachmentEntity> novosAnexos = createAttachmentFromMultipartFiles(r.anexo(), doc.getCategoria(), publicoParaAnexos);
                    anexosResposta.addAll(novosAnexos);
                }
                
                e.setAnexo(anexosResposta);
                
                respostasAtualizadas.add(e);
            }
            
            doc.setRespostas(respostasAtualizadas);
        }
        
        // Se não for edição, finalizar revisão e enviar para aprovação
        if (!isEdit) {
            doc.setStatus("PENDENTE");
            doc.setCurrentStep(2);
        }
        
        // Log da ação
        var step = new PerguntaMongo.StepLogEmbedded();
        step.setId(UUID.randomUUID().toString());
        step.setName(isEdit ? "Edição durante revisão" : "Revisão finalizada");
        step.setStep(isEdit ? 1 : 2);
        step.setCreated_at(new Date());
        step.setUser(userId);
        step.setObservation(isEdit ? "Edição das informações" : "Revisão finalizada e enviada para aprovação");
        
        List<PerguntaMongo.StepLogEmbedded> steps = doc.getStepLog();
        if (steps == null) steps = new ArrayList<>();
        steps.add(step);
        doc.setStepLog(steps);
    }
    
    // Método auxiliar para criar anexos a partir de MultipartFile
    private List<AttachmentEntity> createAttachmentFromMultipartFiles(List<MultipartFile> files, String categoria, String publico) {
        List<AttachmentEntity> result = new ArrayList<>();
        if (files == null) return result;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            
            var id = UUID.randomUUID().toString();
            var item = new AttachmentEntity();
            item.setNome(file.getOriginalFilename());
            item.setId(id);

            String filename = item.getId() + " - " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .toString().replace(":", "-");

            FileMetadata store;
            if ("FAQ".equalsIgnoreCase(categoria) && "EXTERNO".equalsIgnoreCase(publico)) {
                // Usar WhatsAppMedia para anexos acessíveis externamente
                String whatsappPath = whatsappMediaService.upload(file);
                store = new FileMetadata(whatsappPath, extractExtension(file.getOriginalFilename()),
                                       file.getContentType(), 1);
            } else {
                // Usar armazenamento padrão do sistema
                store = storeFile(file, "faq/anexos", filename, 1);
            }

            item.setFile(store);
            result.add(item);
        }
        return result;
    }

    // ========== MÉTODOS AUXILIARES ==========



    /**
     * Mapeia um documento MongoDB para PerguntaResumoDTO
     */
    private PerguntaResumoDTO mapToPerguntaResumoDTO(PerguntaMongo doc, 
                                                     Map<Long, String> nomeSetorPorId, 
                                                     Map<Long, String> nomeTemaPorId) {
        String nomeSetor = doc.getSetorId() == null ? null : nomeSetorPorId.get(doc.getSetorId());
        String nomeTema = doc.getTemaId() == null ? null : nomeTemaPorId.get(doc.getTemaId());
        int total = doc.getRespostas() == null ? 0 : doc.getRespostas().size();
        
        // Resolve regionalName a partir do regionalId, quando possível
        String regionalName = null;
        if (doc.getRegionalId() != null) {
            regionalName = regionalRepository.findById(doc.getRegionalId())
                    .map(Regional::getRegional)
                    .orElse(null);
        }

        String contratoNome = null;
        if (doc.getContratoId() != null) {
            contratoNome = contractProjectRepository.findWithFilialByRateio(doc.getContratoId().intValue())
                    .map(ContractProject::getCostCenterName)
                    .orElse(null);
        }

        // Determina a etapa atual: usar currentStep quando disponível; senão fallback
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

        return new PerguntaResumoDTO(
            doc.getCodigoSequencial(),
            nomeSetor,
            nomeTema,
            doc.getTitulo(),
            total,
            doc.getStatus(),
            doc.getCategoria(),
            regionalName,
            etapaAtual,
            doc.getDataCriacao(),
            doc.getDataFim(),
            doc.getSituacao(),
            doc.getId()
        );
    }
} 