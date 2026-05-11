package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.application.dto.AtendenteResumoDTO;
import com.indux.modules.ocf.application.dto.ContractSummaryDTO;
import com.indux.modules.ocf.application.dto.TimeSummaryDTO;
import com.indux.modules.ocf.application.dto.TimeWithMembersDTO;
import com.indux.modules.ocf.domain.entities.SquadAttendant;
import com.indux.modules.ocf.domain.entities.SquadContractLink;
import com.indux.modules.ocf.domain.repository.SquadAttendantRepository;
import com.indux.modules.ocf.domain.repository.SquadContractLinkRepository;
import com.indux.modules.organization_chart.application.services.ProjectService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TimeService {

    private final JdbcTemplate jdbcTemplate;
    private final SquadAttendantRepository repository;
    private final SquadContractLinkRepository contractLinkRepository;
    private final ProjectService projectService;

    public TimeService(JdbcTemplate jdbcTemplate, SquadAttendantRepository repository, SquadContractLinkRepository contractLinkRepository, ProjectService projectService) {
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
        this.contractLinkRepository = contractLinkRepository;
        this.projectService = projectService;
    }

    @Transactional
    public void atualizarMembrosDoTime(Long timeId, List<Long> projetos, List<Long> atendentes) {
        if (projetos != null) {
            contractLinkRepository.deleteByTimeId(timeId);
            if (!projetos.isEmpty()) {
                ArrayList<SquadContractLink> newLinks = new ArrayList<>();
                var projectList = projectService.findAllByIds(projetos);
                for (var projeto : projetos) {
                    Long contract = null;
                    if(projectList.stream().anyMatch((e) -> e.getId() == projeto)){
                        var uniqueProject = projectList.stream().filter((e) -> e.getId() == projeto).findFirst().orElse(null);
                        contract = uniqueProject.getContract().getId();
                    }
                    var link = SquadContractLink.builder()
                            .timeId(timeId)
                            .projeto(projeto)
                            .contrato(contract)
                            .build();
                    newLinks.add(link);
                }
                contractLinkRepository.saveAll(newLinks);
            }
        }

        if (atendentes != null) {
            repository.deleteBySquadId(timeId);
            if (!atendentes.isEmpty()) {
                ArrayList<SquadAttendant> newAttendants = new ArrayList<>();
                for (var attendant : atendentes) {
                    var newAttendant = SquadAttendant.builder()
                            .squadId(timeId)
                            .attendantId(attendant).build();
                    newAttendants.add(newAttendant);
                }
                repository.saveAll(newAttendants);
            }
        }
    }

    @Transactional
    public void atualizarProjetos(Long timeId, List<Long> projetos) {
        if (projetos != null) {
            contractLinkRepository.deleteByTimeId(timeId);

            if (!projetos.isEmpty()) {
                var links = new ArrayList<SquadContractLink>();
                for (var projeto : projetos) {
                    links.add(SquadContractLink.builder()
                            .timeId(timeId)
                            .projeto(projeto)
                            .build());
                }
                contractLinkRepository.saveAll(links);
            }
        }
    }

    @Transactional
    public void atualizarAtendentes(Long timeId, List<Long> atendentes) {
        if (atendentes != null) {
            repository.deleteBySquadId(timeId);

            if (!atendentes.isEmpty()) {
                var links = new ArrayList<SquadAttendant>();
                for (var attendant : atendentes) {
                    links.add(SquadAttendant.builder()
                            .squadId(timeId)
                            .attendantId(attendant)
                            .build());
                }
                repository.saveAll(links);
            }
        }
    }

    public List<TimeSummaryDTO> listarTimes() {
        return jdbcTemplate.query(
                "SELECT time_id, nome_tipe FROM tb_times ORDER BY nome_tipe",
                (rs, rowNum) -> new TimeSummaryDTO(
                        rs.getLong("time_id"),
                        rs.getString("nome_tipe")
                )
        );
    }

    public List<TimeWithMembersDTO> listarTimesComMembros() {
        String sql = """
            SELECT
                t.time_id,
                t.nome_tipe,

                tc.projeto AS projeto_id,
                pm.pro_st_descricao AS projeto_nome,

                tc.contrato_id AS contrato_id,
                tc.contrato AS contrato,
                COALESCE(oc.nome, oc.apelido, oc.os::text) AS contrato_nome,

                a.account_id,
                a.email,
                a.nome AS atendente_nome
            FROM tb_times t
            LEFT JOIN tb_times_contratos tc
                   ON tc.time_id = t.time_id

            LEFT JOIN tb_organograma_projeto op
                   ON op.id = tc.projeto

            LEFT JOIN tb_projetos_mega pm
                   ON pm.pro_in_reduzido = op.mega

            LEFT JOIN tb_organograma_contrato oc
                   ON oc.id = tc.contrato

            LEFT JOIN tb_times_atendentes ta
                   ON ta.time_id = t.time_id
            LEFT JOIN tb_atendentes a
                   ON a.account_id = ta.atendente_id

            ORDER BY
                t.nome_tipe,
                pm.pro_st_descricao NULLS LAST,
                COALESCE(oc.nome, oc.apelido, oc.os::text) NULLS LAST,
                a.nome NULLS LAST
            """;

        Map<Long, TimeWithMembersDTO> acc = new LinkedHashMap<>();
        Map<Long, Set<Long>> projetosPorTime = new HashMap<>();
        Map<Long, Set<Integer>> atendentesPorTime = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long timeId = rs.getLong("time_id");
            String timeNome = rs.getString("nome_tipe");

            Long projetoId = rs.getObject("projeto_id") != null ? rs.getLong("projeto_id") : null;
            String projetoNome = rs.getString("projeto_nome");

            Integer accountId = rs.getObject("account_id") != null ? rs.getInt("account_id") : null;
            String email = rs.getString("email");
            String atendenteNome = rs.getString("atendente_nome");

            TimeWithMembersDTO atual = acc.get(timeId);
            if (atual == null) {
                atual = new TimeWithMembersDTO(timeId, timeNome, new ArrayList<>(), new ArrayList<>());
                acc.put(timeId, atual);
                projetosPorTime.put(timeId, new HashSet<>());
                atendentesPorTime.put(timeId, new HashSet<>());
            }

            // Regra: vínculo é por PROJETO. Se projeto estiver null, ignora (dado legado/sujo).
            if (projetoId != null) {
                Set<Long> set = projetosPorTime.get(timeId);
                if (set.add(projetoId)) {
                    ((List<ContractSummaryDTO>) atual.contratos())
                            .add(new ContractSummaryDTO(projetoId, projetoNome));
                }
            }

            if (accountId != null) {
                Set<Integer> setA = atendentesPorTime.get(timeId);
                if (setA.add(accountId)) {
                    ((List<AtendenteResumoDTO>) atual.atendentes())
                            .add(new AtendenteResumoDTO(accountId, email, atendenteNome));
                }
            }
        });

        return new ArrayList<>(acc.values());
    }

    public Long findTimeIdByRateio(Integer projectId) {
        String sql = "SELECT time_id FROM tb_times_contratos WHERE projeto = ? LIMIT 1";
        try {
            return jdbcTemplate.query(sql, ps -> ps.setLong(1, projectId.longValue()), rs -> rs.next() ? rs.getLong(1) : null);
        } catch (Exception e) {
            return null;
        }
    }
}


