package com.indux.modules.ocf.domain.model;

import com.indux.core.domain.model.employee.ContractProject;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.employee.Regional;
import com.indux.modules.faq.domain.entities.Setor;
import com.indux.modules.ocf.application.dto.AtendenteDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tb_atendentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atendente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Transient
    private String matriculaRHlocal;
    
    @Transient
    private String matriculaRHmatriz;
    
    @Column(nullable = true)
    private Long setor;
    
    // Relacionamento com tb_setor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor", insertable = false, updatable = false)
    private Setor setorEntity;
    
    @Transient
    private String atendenteRHmatriz;
    
    @Transient
    private String atendenteRhlocal;
    
    @Column(nullable = true)
    private String senha;
    
    @Column(columnDefinition = "TEXT")
    private String regional; // Armazenado como JSON string
    
    @Transient
    private String contrato; // Não é mais persistido em banco
    
    // Identificador de conta associado ao atendente (se aplicável)
    @Column(name = "account_id", nullable = true)
    private Integer accountId;

    // Novo campo nome persistido em banco
    @Column(nullable = true)
    private String nome;

    // Nova coluna matricula
    @Column(nullable = true)
    private String matricula;

	// Campos derivados (não persistidos) para correlações de contrato/filial/regional
	@Transient
	private String contractNomeCentroCustos;

	@Transient
	private String filialNome;

    @Transient
	private Long projectId;

	@Transient
	private String regionalFromFilialNome;

    // Relacionamento com tb_funcionarios (sem foreign key)
    @Transient
    private Employee funcionario;
    
    // Relacionamento com tb_regional (através da lista de IDs)
    @Transient
    private List<Regional> regionais;
    
    // Relacionamento com tb_contratos (através da lista de IDs)
    @Transient // Não mapeia para coluna, apenas para consultas
    private List<ContractProject> contratos;
    
    // Getters e Setters para setorEntity
    public Setor getSetorEntity() {
        return setorEntity;
    }
    
    public void setSetorEntity(Setor setorEntity) {
        this.setorEntity = setorEntity;
    }
    
    public static Atendente fromDTO(AtendenteDTO dto) {
        Atendente atendente = new Atendente();
        atendente.setEmail(dto.email());
        atendente.setMatriculaRHlocal(dto.matriculaRHlocal());
        atendente.setMatriculaRHmatriz(dto.matriculaRHmatriz());
        atendente.setSetor(dto.setor());
        atendente.setAtendenteRHmatriz(dto.atendenteRHmatriz());
        atendente.setAtendenteRhlocal(dto.atendenteRhlocal());
        atendente.setSenha(dto.senha());
        // Converter lista de regionais para JSON string
        atendente.setRegional(convertListToJson(dto.regional()));
        // Converter lista de contratos para JSON string
        atendente.setContrato(convertListToJson(dto.contrato()));
        return atendente;
    }
    
    private static String convertListToJson(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[" + String.join(",", list.stream().map(String::valueOf).toList()) + "]";
    }
    
    public List<String> getRegionaisAsList() {
        if (regional == null || regional.isEmpty() || regional.equals("[]")) {
            return List.of();
        }
        
        // Verificar se a string tem pelo menos 2 caracteres para ter colchetes
        if (regional.length() < 2) {
            return List.of();
        }
        
        // Remover colchetes e dividir por vírgula
        String content = regional.substring(1, regional.length() - 1);
        if (content.isEmpty()) {
            return List.of();
        }
        
        String[] parts = content.split(",");
        return List.of(parts).stream()
                .map(String::trim)
                .map(s -> s.replace("\"", "")) // Remover aspas se houver
                .filter(s -> !s.isEmpty()) // Filtrar strings vazias
                .toList();
    }
    
    public List<Integer> getContratosAsList() {
        if (contrato == null || contrato.isEmpty() || contrato.equals("[]")) {
            return List.of();
        }
        
        // Verificar se a string tem pelo menos 2 caracteres para ter colchetes
        if (contrato.length() < 2) {
            return List.of();
        }
        
        // Remover colchetes e dividir por vírgula
        String content = contrato.substring(1, contrato.length() - 1);
        if (content.isEmpty()) {
            return List.of();
        }
        
        String[] parts = content.split(",");
        return List.of(parts).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty()) // Filtrar strings vazias
                .map(Integer::parseInt)
                .toList();
    }
} 