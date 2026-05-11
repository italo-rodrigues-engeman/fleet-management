package com.indux.modules.ppu.domain.services.bm.audit.samc;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;
/**
 * Serviço de auditoria SAMC responsável por comparar os registros da planilha SAMC
 * com os RDOs e a BM de um mesmo período de medição.
 */
public interface AuditSAMC<T> {

    /**
     * Executa a auditoria SAMC de um RDO específico com base em um arquivo SAMC.
     *
     * <p>Fluxo resumido:</p>
     * <ul>
     *     <li>Carrega o RDO pelo identificador informado;</li>
     *     <li>Carrega a PPU associada ao RDO;</li>
     *     <li>Lê e filtra o arquivo SAMC conforme as regras de cabeçalho e status;</li>
     *     <li>Monta o contexto de auditoria e delega ao {@code AuditEngine};</li>
     *     <li>Registra um log no RDO com a quantidade de divergências encontradas;</li>
     *     <li>Retorna a lista de divergências convertida em DTOs de resposta.</li>
     * </ul>
     *
     * @param entityID identificador do RDO a ser auditado
     * @param archive  arquivo SAMC (planilha Excel) a ser utilizado na auditoria
     * @param token usuário autenticado, utilizado para registro de log
     * @return lista de divergências encontradas na auditoria
     * @throws ParseException caso ocorram erros de parsing de datas ou campos do arquivo SAMC
     */
    List<T> audit(String entityID, MultipartFile archive, JwtAuthenticationToken token) throws ParseException;

    /**
     * Executa a auditoria SAMC em lote para uma BM, cruzando todos os RDOs do período
     * de medição com o arquivo SAMC informado.
     *
     * <p>Fluxo resumido:</p>
     * <ul>
     *     <li>Carrega a BM pelo identificador informado;</li>
     *     <li>Carrega a PPU associada à BM;</li>
     *     <li>Valida se existem RDOs faltantes no período usando {@code GetMissingRDOsUseCase};
     *     em caso positivo, lança {@code ModuleFailure} com o detalhamento das faltas;</li>
     *     <li>Lê e filtra o arquivo SAMC conforme as regras de cabeçalho e status;</li>
     *     <li>Carrega todos os RDOs do período por plataforma da PPU;</li>
     *     <li>Monta o {@code AuditBatchContext} e delega ao {@code AuditEngine};</li>
     *     <li>Registra logs em cada RDO auditado com o resumo da auditoria geral;</li>
     *     <li>Se a BM não estiver finalizada:
     *         <ul>
     *             <li>adiciona um {@code BMSamcLog} com o resultado da auditoria;</li>
     *             <li>atualiza {@code updatedAt};</li>
     *             <li>marca {@code auditSAMCChecked = true} quando não há divergências.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param entityID  identificador da BM a ser auditada
     * @param archive  arquivo SAMC (planilha Excel) a ser utilizado na auditoria em lote
     * @param token usuário autenticado, utilizado para registro de log
     * @return lista de divergências encontradas em todos os RDOs do período
     * @throws ParseException caso ocorram erros de parsing de datas ou campos do arquivo SAMC
     */
    List<T> auditBatch(String entityID, MultipartFile archive, JwtAuthenticationToken token) throws ParseException;

    /**
     * Executa a auditoria SAMC para um conjunto de RDOs no mesmo contexto de medição.
     *
     * <p>Regras esperadas:</p>
     * <ul>
     *     <li>Todos os RDOs devem existir;</li>
     *     <li>Todos devem possuir a mesma data;</li>
     *     <li>Todos devem pertencer ao mesmo contrato;</li>
     *     <li>A plataforma pode variar entre os RDOs informados.</li>
     * </ul>
     *
     * @param rdoIDs identificadores dos RDOs a serem auditados
     * @param archive arquivo SAMC (planilha Excel) a ser utilizado na auditoria
     * @param token usuário autenticado, utilizado para registro de log
     * @return lista de divergências encontradas em todos os RDOs informados
     * @throws ParseException caso ocorram erros de parsing de datas ou campos do arquivo SAMC
     */
    List<T> auditMultipleRDOs(List<String> rdoIDs, MultipartFile archive, JwtAuthenticationToken token) throws ParseException;
}
