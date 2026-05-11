package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.cbo.CourseSuperior;
import com.indux.core.domain.model.cbo.CourseTechnical;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import com.indux.modules.training.application.dto.TrainingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryFuncao {
    @JsonProperty("idCbo")
    private String cboId;

    @JsonProperty("idHcm")
    private String hcmId;

    @JsonProperty("nomeHCM")
    private String nameHcm;

    @JsonProperty("filialHCM")
    private List<Long> filialHCM;

    @JsonProperty("tipo")
    private String type;

    @JsonProperty("incidencia")
    private Boolean incidence;

    @JsonProperty("cota")
    private Boolean quota;

    @Transient
    @JsonProperty("anexo")
    private List<MultipartFile> anexo;

    @JsonProperty("anexos")
    private List<AttachmentEntity> anexos;

    @JsonProperty("anexo1")
    private String anexo1;

    @JsonProperty("descricaoResumida")
    private String descResume;

    @JsonProperty("atividadesComuns")
    private String activityCommon;

    @JsonProperty("descricaoTarefas")
    private String descWork;

    @JsonProperty("educacao")
    private String education;

    @JsonProperty("nivelEducacao")
    private String educationLevel;

    @JsonProperty("cursoSuperior")
    private List<CourseSuperior> courseSuperior;

    @JsonProperty("cursoSuperiorId")
    private List<String> courseSuperiorId;

    @JsonProperty("cursoTecnico")
    private List<CourseTechnical> courseTechnical;

    @JsonProperty("cursoId")
    private List<String> courseTechnicalId;

    @JsonProperty("treinamentos")
    private List<TrainingResponse> trainings;

    @JsonProperty("treinamentosExigidos")
    private List<RequiredTrainingStraring> requiredTrainings;

    @JsonProperty("treinamentosId")
    private List<String> trainingsId;

    @JsonProperty("obrigatorioId")
    private List<String> obrigatoryId;

    @JsonProperty("obrigatorio")
    private List<PerguntaMongo> obrigatory;

    @JsonProperty("proativoId")
    private List<String> proactiveId;

    @JsonProperty("proativo")
    private List<PerguntaMongo> proactive;

    @JsonProperty("habilidades")
    private List<String> skills;

    @JsonProperty("experiencia")
    private List<ExperienceDTO> experience;

    @JsonProperty("cargoId")
    private List<String> positionId;

    @JsonProperty("atitudes")
    private List<String> attitudes;

    @JsonProperty("informatica")
    private List<String> computing;

    @JsonProperty("sistemas")
    private List<String> systems;

    @JsonProperty("recursosEspecificos")
    private String specificResorces;

    @JsonProperty("dadosLog")
    private List<DataLog> dataLog;

    @JsonProperty("justificativa")
    private String justification;

    @JsonProperty("nomeCBO")
    private String nameCBOChildren;

    @JsonProperty("codCBO")
    private String codCBOChildren;

    @JsonProperty("dataDescontinuada")
    private LocalDateTime discontinuationDate;
}
