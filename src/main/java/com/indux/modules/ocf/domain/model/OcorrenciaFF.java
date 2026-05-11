package com.indux.modules.ocf.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.BankDetailDTO;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.Form;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "ocorrencia_ff")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcorrenciaFF extends Form<OcorrenciaFF> { // Ocorrência de Folha Financeira
    // Dados da Ocorrência
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private UUID modulo_id;
    private Origem origem;
    private String descricao;
    private String responsavelAtual;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data_Ocorrencia;
    // Solicitante
    private SimpleUser solicitante;
    // Dados do Colaborador.
    private EmployeeDTO colaborador;
    // Dados do Banco
    private boolean bancoDadosErrado;
    private BankDetailDTO dadosCorretos;
    private BankDetailDTO dadosBancarios;
    //Anexos
    private FileMetadata extratoColaborador;
    private FileMetadata comprovantePagamento;
    private FileMetadata evidenciaAnalista;
    private FileMetadata anexoTicket;  // Novo campo para anexos de ticket
    private String causas;
    @Indexed(unique = true, sparse = true)
    private String numeroDoProtocolo;
    private String telefone;
    private String telefoneDeContato;
    private String tipoDeBeneficio;
    private String motivo;
    private String valorContestado;
    private String valorPagarDescontar;
    private String tipoFluxo;
    private Boolean aprovacaoGestor = false;
    private Boolean aprovacaoAnalista = false;
    private String observacaoGestor;
    private String observacaoAnalista;
    private String observacaoAnalista1;
    private String motivoAnalista;
    private Boolean aprovacaoAnalista1 = false;
    private String prioridade;
    private String tipoAtendimento;
    // Itens Reclamados
    private List<ItemReclamado> itemsreclamados;
    // Novos campos adicionados
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAtendimento;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFim;
    private String atendenteRHlocal;
    private String atendenteRHmatriz;
    private Long teamId;
    private String teamName;
    private String competencia;
    private String conversaChat;
    private String descricaoOcorrencia;
    private String respostaEmpregado;
    private String justificativaOcorrencia;
    
    @JsonProperty("meiosComunicacao")
    private MeiosComunicacao meiosComunicacao;
    
    private Boolean pertinente;
    private String temperatura;
    private String valorPagar;
    private String competenciaPagar;
    private String dataPagamento;
}
