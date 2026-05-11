package com.indux.core.domain.service.importers;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ExcelReader<T> {
    /**
     * Retorna uma lista completa de itens convertidos no Objeto requerido.
     * @param file Arquivo em tipo EXCEL.
     * @return Lista de objeto que representem os dados do Excel.
     */
    List<T> readerAllRows(MultipartFile file);

    /**
     * Retorna uma lista completa de itens convertidos no Objeto requerido via mapeamento dos headers.
     * Busca as informações do arquivo via HEADERS, podendo buscar uma informação em colunas diferentes.
     * @param file arquivo em formato EXCEL.
     * @param headerMapping mapeamento do cabeçalho do EXCEL.
     * @return Lista de objeto que representem os dados do Excel.
     */
    List<T> readerSpecificHeaders(MultipartFile file, Map<String, String> headerMapping);

    /**
     * Retorna uma lista completa de itens convertidos no Objeto requerido via leitura dos indexes fixos
     * das colunas. Busca os itens através do index, utilizado em EXCEL sem cabeçalho.
     * @param file arquivo em formato EXCEL.
     * @param columnIndexes indexes do arquivo do EXCEL.
     * @return Lista de objeto que representem os dados do Excel.
     */
    List<T> readerSpecificColumns(MultipartFile file, List<Integer> columnIndexes);

    /**
     * Retorna uma lista completa de itens de uma coluna pelo nome do cabeçalho.
     * @param file arquivo em formato EXCEL.
     * @param columnName nome do cabeçalho.
     * @return Lista de itens em String.
     */
    List<String> parseUniqueColumn(MultipartFile file, String columnName);

    /**
     * Retorna uma lista completa de itens de uma coluna pelo index da coluna.
     * @param file arquivo em formato EXCEL.
     * @param columnIndex index da coluna.
     * @return Lista de itens em String.
     */
    List<String> parseUniqueColumnByIndex(MultipartFile file, int columnIndex);
}
