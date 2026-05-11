package com.indux.modules.ppu.domain.services.bm.audit.mio;

import com.indux.modules.ppu.domain.entities.rdo.audit.MioDivergence;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
/**
 * Serviço de auditoria de embarque MIO responsável por comparar a presença de colaboradores
 * registrada no sistema MIO com a presença registrada nos RDOs de uma BM.
 */
public interface AuditMio {

    /**
     * Executa a auditoria entre o sistema MIO e os RDOs associados a uma BM.
     *
     * <p>Fluxo resumido:</p>
     * <ul>
     *     <li>Carrega BM, PPU e projeto associados ao {@code bmID};</li>
     *     <li>Carrega RDOs aprovados do período de medição da BM, filtrando por plataformas da PPU;</li>
     *     <li>Busca colaboradores embarcados no MIO dentro do mesmo intervalo;</li>
     *     <li>Aplica as regras de filtragem por filial HCM e plataforma;</li>
     *     <li>Expande presença diária de colaboradores do MIO e agrupa por data;</li>
     *     <li>Agrupa presença de colaboradores dos RDOs por data;</li>
     *     <li>Compara matrículas dia a dia e gera {@code MioDivergence} para cada inconsistência;</li>
     *     <li>Registra o resultado em {@code BMMioLog} quando a BM não estiver finalizada;</li>
     *     <li>Retorna a lista de divergências encontradas.</li>
     * </ul>
     *
     * @param bmID  identificador da BM a ser auditada
     * @param token usuário autenticado, utilizado para registro de log
     * @return lista de divergências de presença entre MIO e RDOs
     * @throws IOException              em caso de falhas de comunicação com o MIO ou na ETL
     * @throws ExecutionException       em caso de falhas em operações assíncronas internas
     * @throws InterruptedException     se a thread for interrompida durante o processamento assíncrono
     */
    List<MioDivergence> call(String bmID, JwtAuthenticationToken token) throws IOException, ExecutionException, InterruptedException;
}
