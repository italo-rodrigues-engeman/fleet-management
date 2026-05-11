package com.indux.core.domain.model.cbo;

import com.indux.core.application.dto.cbo.DataLog;
import com.indux.core.application.dto.cbo.ExperienceDTO;
import com.indux.core.application.dto.cbo.HistoryFuncao;
import com.indux.core.application.dto.cbo.RequiredTrainingStraring;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import com.indux.modules.training.domain.entity.TrainingEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "cbo_funcao")
public class FuncaoHCM {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private Long autoIncrementId;
    @Field("cbo_id")
    private String cboId;
    @Field("hcm_id")
    private String hcmId;
    @Field("nome_hcm")
    private String nameHcm;
    @Field("filial_hcm")
    private List<Long> filialHCM;
    @Field("tipo")
    private String type;
    @Field("incidencia")
    private Boolean incidence;
    @Field("cota")
    private Boolean quota;
    @Field("anexo")
    private List<AttachmentEntity> anexo;
    @Field("anexo1")
    private String anexo1;
    @Field("desc_resumido")
    private String descResume;
    @Field("atividade_comuns")
    private String activityCommon;
    @Field("desc_tarefas")
    private String descWork;
    @Field("educacao")
    private String education;
    @Field("nivel_educacao")
    private String educationLevel;
    @DBRef
    @Field("cursoSuperior")
    private List<CourseSuperior> courseSuperior;
    @DBRef
    @Field("cursoTecnico")
    private List<CourseTechnical> courseTechnical;
    @DBRef
    @Field("treinamentos")
    private List<TrainingEntity> trainings;
    @Field("treinamentosExigidos")
    private List<RequiredTrainingStraring> requiredTrainings;
    @Field("obrigatorio")
    private List<PerguntaMongo> obrigatory;
    @Field("proativo")
    private List<PerguntaMongo> proactive;
    @Field("habilidades")
    private List<String> skills;
    @Field("experiencia")
    private List<ExperienceDTO> experience;
    @Field("atitudes")
    private List<String> attitudes;
    @Field("informatica")
    private List<String> computing;
    @Field("sistemas")
    private List<String> systems;
    @Field("recursos_especificos")
    private String specificResorces;
    @Field("data_log")
    private List<DataLog> dataLog;
    @Field("justificativa")
    private String justification;
    @Field("nomeCBOFilho")
    private String nameCBOChildren;
    @Field("codCBOFilho")
    private String codCBOChildren;
    @Transient
    private List<String> branch;
    @Transient
    private List<String> contract;
    @Field("historico")
    private List<HistoryFuncao> history;
}
