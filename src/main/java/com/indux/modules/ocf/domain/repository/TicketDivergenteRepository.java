package com.indux.modules.ocf.domain.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TicketDivergenteRepository {
    private final JdbcTemplate jdbcTemplate;

    public TicketDivergenteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> find(Long id, String matricula, Integer limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM public.tb_ticket_divergente_alodp WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (id != null) {
            sql.append(" AND id = ?");
            params.add(id);
        }
        if (matricula != null && !matricula.isBlank()) {
            sql.append(" AND matricula = ?");
            params.add(matricula);
        }
        sql.append(" ORDER BY id DESC");
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public int updateFields(Long id,
                            Boolean contato,
                            Boolean alteracao,
                            String observacao,
                            Boolean pertinente,
                            java.time.LocalDate dataAlteracao,
                            java.time.LocalDate dataContato) {
        StringBuilder sql = new StringBuilder("UPDATE public.tb_ticket_divergente_alodp SET status = 'Concluido'");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (contato != null) { sql.append(", contato = ?"); params.add(contato); }
        if (alteracao != null) { sql.append(", alteracao = ?"); params.add(alteracao); }
        if (observacao != null) { sql.append(", observacao = ?"); params.add(observacao); }
        if (pertinente != null) { sql.append(", pertinente = ?"); params.add(pertinente); }
        if (dataAlteracao != null) { sql.append(", data_alteracao = ?"); params.add(java.sql.Date.valueOf(dataAlteracao)); }
        if (dataContato != null) { sql.append(", data_contato = ?"); params.add(java.sql.Date.valueOf(dataContato)); }

        sql.append(" WHERE id = ?");
        params.add(id);

        return jdbcTemplate.update(sql.toString(), params.toArray());
    }
}


