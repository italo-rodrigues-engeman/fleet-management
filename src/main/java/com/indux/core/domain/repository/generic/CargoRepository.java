package com.indux.core.domain.repository.generic;

import com.indux.core.application.dto.cbo.FilterCBO;
import com.indux.core.domain.model.employee.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, String>, CargoRepositoryCustom {
    @Query(value = """
    WITH RECURSIVE
    -- 1) Base: conecta funcionário → cargo → projeto → (contrato opcional)
    base AS (
        SELECT
            f.id                            AS id_func,
            f.situacao                      AS situacao, -- adiciona a situacao
            ca.id_hcm                       AS id_hcm,
            ca.nome_cargo                   AS nome_hcm,
            ca.cod_cbo                      AS cod_cbo,
            op.id                           AS id_projeto,
            oc.id                           AS id_contrato,
            oc.nome                         AS nome_contrato,
            oc.os                           AS os,
            COALESCE(oc.subordinado, op.subordinado) AS id_org_start
        FROM tb_funcionarios f
        JOIN tb_cargos ca
          ON ca.id_hcm = f.cargo_id
        LEFT JOIN tb_organograma_projeto op
          ON op.hcm::varchar = f.centro_custos_id
        LEFT JOIN tb_organograma_contrato oc
          ON oc.id = op.contrato
         WHERE ca.dt_extincao = '1900-12-31'
    ),
    
    -- 2) Caminho: parte do nó inicial e sobe pelos tipo=3 até achar tipo=2
    caminho AS (
        SELECT
            b.id_func, b.id_hcm, b.nome_hcm, b.cod_cbo,
            b.id_projeto, b.id_contrato, b.nome_contrato,
            b.id_org_start AS id_org,
            0 AS depth
        FROM base b
        WHERE b.id_org_start IS NOT NULL
    
        UNION ALL
    
        SELECT
            c.id_func, c.id_hcm, c.nome_hcm, c.cod_cbo,
            c.id_projeto, c.id_contrato, c.nome_contrato,
            o.subordinado AS id_org,
            c.depth + 1 AS depth
        FROM caminho c
        JOIN tb_organograma o
          ON o.id = c.id_org
        WHERE o.tipo = 3
          AND c.depth < 25
    ),
    
    -- 3) Rotula cada passo com o tipo/cargo para permitir seleção do tipo 2
    marcada AS (
        SELECT
            c.*,
            o.tipo,
            o.cargo
        FROM caminho c
        JOIN tb_organograma o
          ON o.id = c.id_org
    ),
    
    -- 4) Regional: o primeiro nó tipo 2 encontrado no caminho (menor depth)
    regional_por_func AS (
        SELECT DISTINCT ON (id_func)
            id_func,
            cargo AS regional
        FROM marcada
        WHERE tipo = 2
        ORDER BY id_func, depth
    ),
    
    -- 5) Subselect: cria alias 'chave' para o DISTINCT ON
    base_final AS (
        SELECT
            (CASE WHEN :resume THEN b.id_hcm::text  || '|' || COALESCE(rp.regional, '')
                || '|' || COALESCE(b.nome_contrato, '') || '|' || COALESCE(b.nome_hcm, '') 
                || '|' || COALESCE(b.cod_cbo, '') 
                ELSE b.cod_cbo::text END)::text AS chave,
            b.*,
            rp.regional,
            p.id AS id_proj,
            pf.filial_id
        FROM base b
        LEFT JOIN regional_por_func rp ON rp.id_func = b.id_func
        LEFT JOIN tb_organograma_projeto p ON p.id = b.id_projeto
        LEFT JOIN tb_organograma_projeto_filial pf ON pf.project_id = p.id
    )
    
    -- 6) Resultado final com DISTINCT ON usando o alias
    SELECT DISTINCT ON (bf.chave)
        bf.id_hcm                 AS "idHcm",
        bf.nome_hcm               AS "nomeHcm",
        bf.cod_cbo                AS "codCbo",
        bf.regional               AS "regional",
        bf.os                     AS "os",
        bf.nome_contrato          AS "contrato",
    
        -- totais por grupo
        COUNT(*) OVER (
            PARTITION BY bf.id_hcm, bf.nome_hcm, bf.cod_cbo, bf.regional, bf.nome_contrato
        ) AS totalPessoas,
    
        -- total de ativos (situacao = 'Trabalhando')
        SUM(CASE WHEN bf.situacao = 'Trabalhando' THEN 1 ELSE 0 END)
            OVER (
                PARTITION BY bf.id_hcm, bf.nome_hcm, bf.cod_cbo, bf.regional, bf.nome_contrato
            ) AS totalAtivos,
    
        -- total de inativos (total - ativos)
        (
            COUNT(*) OVER (
                PARTITION BY bf.id_hcm, bf.nome_hcm, bf.cod_cbo, bf.regional, bf.nome_contrato
            )
            -
            SUM(CASE WHEN bf.situacao = 'Trabalhando' THEN 1 ELSE 0 END)
            OVER (
                PARTITION BY bf.id_hcm, bf.nome_hcm, bf.cod_cbo, bf.regional, bf.nome_contrato
            )
        ) AS totalInativos,

    COUNT(*) OVER (PARTITION BY bf.cod_cbo) AS totalCBO,
    COALESCE(filiais.filiaisHCM, ARRAY[]::integer[]) AS "filiaisHCM"
    
    FROM base_final bf
    LEFT JOIN LATERAL (
        SELECT array_agg(pf2.filial_id) AS filiaisHCM
        FROM tb_organograma_projeto_filial pf2
        WHERE pf2.project_id = bf.id_proj
    ) filiais ON TRUE 
    WHERE
        (COALESCE(:regional, ARRAY[]::varchar[]) = ARRAY[]::varchar[] OR bf.regional = ANY(:regional))
        AND (COALESCE(:contrato, ARRAY[]::varchar[]) = ARRAY[]::varchar[] OR bf.nome_contrato = ANY(:contrato))
        AND (COALESCE(:projetoId, ARRAY[]::varchar[]) = ARRAY[]::varchar[] OR bf.id_projeto::varchar = ANY(:projetoId))
        AND (COALESCE(:idHCM, ARRAY[]::varchar[]) = ARRAY[]::varchar[] OR bf.id_hcm::varchar = ANY(:idHCM))
        AND (COALESCE(:filialHCM, ARRAY[]::integer[]) = ARRAY[]::integer[] OR bf.filial_id = ANY(:filialHCM))
    
    ORDER BY
        bf.chave;
            
    """,
            nativeQuery = true)
        List<Map<String, Object>> findCargoRelationsSQL(
            @Param("codCBO") String[] codCBO,
            @Param("nomeCBO") String[] nomeCBO,
            @Param("regional") String[] regional,
            @Param("contrato") String[] contrato,
            @Param("projetoId") String[] projetoId,
            @Param("filialHCM") Integer[] filialHCM,
            @Param("idHCM") String[] idHCM,
            @Param("resume") Boolean resume
    );


    Optional<Cargo> findByIdHcm(String s);
    List<Cargo> findAllByIdHcmIn(List<String> idsHcm);
    Optional<List<Cargo>> findAllByNameTitleCbo(String s);
}