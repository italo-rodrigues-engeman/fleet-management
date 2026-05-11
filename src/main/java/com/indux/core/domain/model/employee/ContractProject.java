package com.indux.core.domain.model.employee;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Table(name = "tb_contratos")
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractProject {
    @Id
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "rateio_id")
    private Integer rateio;
    
    @Column(name = "nome_centro_custos")
    private String costCenterName;
    
    @Column(name = "mega_id")
    private Integer megaId;
    
    @Column(name = "nome_projeto")
    private String projectName;
    
    @Column(name = "gestor_interno_contrato")
    private String contractManager;
    
    @Column(name = "cliente")
    private String client;

    @Column(name = "id_cliente")
    private Long idCliente;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private List<ContractCoordinator> coordinators = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "filial_id",
            referencedColumnName = "codigo_filial"
    )
    private Filial filial;
    
    @Column(name= "cod_sap")
    private String codeSap;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private List<Platform> platforms = new ArrayList<>();

    @Column(name = "data_assinatura")
    private LocalDate dataAssinatura;

    @Column(name = "data_fim")
    private java.time.LocalDate dataFim;

    @Column(name = "ativo")
    private Boolean ativo;

    public List<String> getContractCoordinators() {
        return coordinators.stream()
                .map(ContractCoordinator::getName)
                .collect(Collectors.toList());
    }

    public Map<String, Object> toDTO() {
        Map<String, Object> base = new HashMap<>();
        base.put("id", id);
        base.put("centro_custo_nome", costCenterName);
        base.put("mega_id", megaId);
        base.put("nome_projeto", projectName);
        base.put("rateio_id", rateio);
        base.put("gestor_interno_contrato", contractManager);
        base.put("coordenadores_contrato", getContractCoordinators());
        base.put("cliente", idCliente);
        base.put("clientName", client);

        if (filial != null) {
            Map<String, Object> filialMap = new HashMap<>();
            filialMap.put("codigo_filial", filial.getBranchId());
            filialMap.put("nome_filial", filial.getBranchName());
            filialMap.put("razao_social", filial.getCorporateName());
            base.put("filial", filialMap);
        }

        return base;
    }

    public ContractProject(Integer id,
                           Integer rateio,
                           String costCenterName,
                           Integer megaId,
                           String projectName) {
        this.id = id;
        this.rateio = rateio;
        this.costCenterName = costCenterName;
        this.megaId = megaId;
        this.projectName = projectName;
    }

    public ContractProject(Integer id,
                           Integer rateio,
                           String costCenterName,
                           Integer megaId,
                           String projectName,
                           String contractManager,
                           String client) {
        this.id = id;
        this.rateio = rateio;
        this.costCenterName = costCenterName;
        this.megaId = megaId;
        this.projectName = projectName;
        this.contractManager = contractManager;
        this.client = client;
    }
}