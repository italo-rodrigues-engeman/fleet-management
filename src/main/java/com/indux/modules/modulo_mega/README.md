# Módulo Mega - Documentação de Endpoints

Este documento descreve todos os endpoints disponíveis no módulo `modulo_mega` da aplicação Indux.

## Índice

- [Mega Item Controller](#mega-item-controller)
- [Mega Orders Controller](#mega-orders-controller)
- [ABC Report Controller](#abc-report-controller)

---

## Mega Item Controller

Base Path: `/api/mega-itens`

### 1. Autocomplete Genérico

**GET** `/api/mega-itens/{type}`

Retorna uma lista de resultados para autocomplete baseado no tipo especificado, limitada a 20 resultados.

**Parâmetros de Path:**
- `type` (obrigatório): Tipo de autocomplete desejado. Valores disponíveis:
  - `fornecedor`: Fornecedor
  - `grupo`: Grupo
  - `comprador`: Comprador
  - `regional`: Regional
  - `solicitante`: Solicitante
  - `projeto`: Projeto
  - `filial_mega`: Filial Mega
  - `contrato`: Contrato
  - `codigo_item`: Código do item
  - `nome_do_item`: Nome do item
  - `codigo_grupo`: Código do grupo

**Parâmetros de Query:**
- `term` (opcional): Termo de busca para filtrar resultados

**Resposta:**
- `200 OK`: Lista de DTOs apropriados com informações do tipo solicitado
- `400 Bad Request`: Tipo inválido ou vazio

**Exemplo:**
```
GET /api/mega-itens/fornecedor?term=fornecedor
GET /api/mega-itens/grupo?term=grupo
GET /api/mega-itens/comprador?term=comprador
```

---

### 2. Listar Tipos de Autocomplete Disponíveis

**GET** `/api/mega-itens/type/disponiveis`

Retorna uma lista de todos os tipos de autocomplete disponíveis com suas descrições.

**Parâmetros:** Nenhum

**Resposta:**
- `200 OK`: Lista de `AutocompleteTypeDTO` contendo:
  - `type`: Valor do tipo (usado no endpoint de autocomplete)
  - `label`: Descrição legível do tipo

**Exemplo:**
```
GET /api/mega-itens/type/disponiveis
```

**Resposta de Exemplo:**
```json
[
  {
    "type": "fornecedor",
    "label": "Fornecedor"
  },
  {
    "type": "grupo",
    "label": "Grupo"
  },
  {
    "type": "comprador",
    "label": "Comprador"
  }
]
```

---

### 3. Lista de Status de Itens

**GET** `/api/mega-itens/status_item`

Retorna uma lista de todos os status distintos de itens disponíveis no sistema.

**Parâmetros:** Nenhum

**Resposta:**
- `200 OK`: Lista de strings contendo os status disponíveis

**Exemplo:**
```
GET /api/mega-itens/status_item
```

---

### 4. Pesquisar Itens

**GET** `/api/mega-itens/pesquisar-itens`

Realiza uma pesquisa paginada de itens com base em filtros opcionais. Retorna itens agrupados.

**Parâmetros de Query (todos opcionais):**
- `codigoItem` (Integer): Código do item
- `codigoGrupo` (Integer): Código do grupo
- `codigoFornecedor` (Integer): Código do fornecedor
- `status` (String): Status do item
- `precoMedMin` (BigDecimal): Preço médio mínimo
- `precoMedMax` (BigDecimal): Preço médio máximo
- `qtdePedidosMin` (Integer): Quantidade mínima de pedidos
- `qtdePedidosMax` (Integer): Quantidade máxima de pedidos
- `qtdeComprasMin` (BigDecimal): Quantidade mínima de compras
- `qtdeComprasMax` (BigDecimal): Quantidade máxima de compras
- `cadastroStartDate` (LocalDate, formato ISO): Data inicial de cadastro
- `cadastroEndDate` (LocalDate, formato ISO): Data final de cadastro

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 50): Tamanho da página
- `sort` (opcional, padrão: "itemCode"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `ItemGroupedDTO` contendo os itens encontrados

**Nota:** Se `cadastroEndDate` não for informado, assume a data atual. Se `cadastroStartDate` não for informado, assume 1 ano antes da data final.

**Exemplo:**
```
GET /api/mega-itens/pesquisar-itens?codigoItem=123&status=ATIVO&page=0&size=50
```

---

### 5. Detalhes do Item

**GET** `/api/mega-itens/detalhes-item/{itemCode}`

Retorna informações detalhadas de um item específico, incluindo estatísticas agrupadas por regional e filial.

**Parâmetros de Path:**
- `itemCode` (Integer, obrigatório): Código do item

**Parâmetros de Query (opcionais):**
- `startDate` (LocalDate, formato ISO): Data inicial para cálculo das estatísticas
- `endDate` (LocalDate, formato ISO): Data final para cálculo das estatísticas

**Resposta:**
- `200 OK`: `ItemDetailedDTO` contendo informações detalhadas do item, incluindo:
  - Estatísticas base
  - Detalhes por regional
  - Detalhes por filial
  - Informações de estoque

**Nota:** Os parâmetros `startDate` e `endDate` são opcionais. Se não informados, o sistema utilizará valores padrão baseados na data atual.

**Exemplo:**
```
GET /api/mega-itens/detalhes-item/123?startDate=2024-01-01&endDate=2024-12-31
GET /api/mega-itens/detalhes-item/123
```

---

### 6. Histórico de Compras do Item

**GET** `/api/mega-itens/{itemCode}/historico-compras`

Retorna o histórico detalhado de compras de um item específico.

**Parâmetros de Path:**
- `itemCode` (Integer, obrigatório): Código do item

**Parâmetros de Query (opcionais):**
- `startDate` (LocalDate, formato ISO): Data inicial do período
- `endDate` (LocalDate, formato ISO): Data final do período

**Resposta:**
- `200 OK`: Lista de `ItemHistoryDetailedDTO` contendo o histórico de compras

**Nota:** Os parâmetros `startDate` e `endDate` são opcionais. Se não informados, o sistema retornará todos os registros disponíveis.

**Exemplo:**
```
GET /api/mega-itens/123/historico-compras?startDate=2024-01-01&endDate=2024-12-31
GET /api/mega-itens/123/historico-compras
```

---

### 7. Download do Histórico em PDF

**GET** `/api/mega-itens/{itemCode}/historico-compras/download-pdf`

Gera e retorna um arquivo PDF com o histórico de compras do item.

**Parâmetros de Path:**
- `itemCode` (Integer, obrigatório): Código do item

**Parâmetros de Query (opcionais):**
- `startDate` (LocalDate, formato ISO): Data inicial do período
- `endDate` (LocalDate, formato ISO): Data final do período

**Resposta:**
- `200 OK`: Arquivo PDF com o histórico de compras
- `204 No Content`: Quando não há dados para gerar o PDF
- `500 Internal Server Error`: Erro ao gerar o PDF

**Headers de Resposta:**
- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="historico_item_{itemCode}.pdf"`

**Exemplo:**
```
GET /api/mega-itens/123/historico-compras/download-pdf?startDate=2024-01-01&endDate=2024-12-31
```

---

### 8. Download do Histórico em Excel

**GET** `/api/mega-itens/{itemCode}/historico-compras/download-excel`

Gera e retorna um arquivo Excel (XLSX) com o histórico de compras do item.

**Parâmetros de Path:**
- `itemCode` (Integer, obrigatório): Código do item

**Parâmetros de Query (opcionais):**
- `startDate` (LocalDate, formato ISO): Data inicial do período
- `endDate` (LocalDate, formato ISO): Data final do período

**Resposta:**
- `200 OK`: Arquivo Excel com o histórico de compras

**Headers de Resposta:**
- `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition: attachment; filename="historico_item_{itemCode}.xlsx"`

**Exemplo:**
```
GET /api/mega-itens/123/historico-compras/download-excel?startDate=2024-01-01&endDate=2024-12-31
```

---

### 9. Buscar Itens com Complemento

**GET** `/api/mega-itens/itens-complemento`

Retorna uma lista paginada de itens com seus complementos, relacionando dados das tabelas `tb_notas_mega` e `tb_itens_mega`. Permite buscar pelo nome do item usando Criteria API.

**Parâmetros de Query (opcionais):**
- `nomeItem` (String): Nome do item para filtrar a busca (busca case-insensitive com LIKE)

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 50): Tamanho da página
- `sort` (opcional, padrão: "idItem"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `ItemComplementDTO` contendo:
  - `idItem` (Integer): Código do item (id_item)
  - `nomeItem` (String): Nome do item (item de tb_itens_mega)
  - `itemComplemento` (String): Complemento do item (item_complemento de tb_notas_mega)

**Nota:** 
- O endpoint realiza uma relação entre as tabelas `tb_notas_mega` e `tb_itens_mega` através da view `MegaItem`
- Retorna apenas combinações distintas de (id_item, item, item_complemento)
- Se `nomeItem` não for informado, retorna todos os itens disponíveis
- A busca pelo nome do item é case-insensitive e utiliza LIKE (busca parcial)

**Exemplo:**
```
GET /api/mega-itens/itens-complemento?nomeItem=cabo&page=0&size=50
GET /api/mega-itens/itens-complemento
```

**Resposta de Exemplo:**
```json
{
  "content": [
    {
      "idItem": 123,
      "nomeItem": "Cabo Elétrico",
      "itemComplemento": "Cabo 2.5mm"
    },
    {
      "idItem": 123,
      "nomeItem": "Cabo Elétrico",
      "itemComplemento": "Cabo 4.0mm"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalElements": 2,
  "totalPages": 1
}
```

---

### 10. Criar Solicitação de Item

**POST** `/api/mega-itens/solicitacao-item`

Cria uma nova solicitação de item no MongoDB. A solicitação é salva com status inicial "Pendente" e registra automaticamente informações de auditoria (usuário criador e data de criação).

**Corpo da Requisição (JSON):**
- `nomePrincipal` (String, obrigatório): Nome principal do item solicitado
- `urlReferencia` (String, opcional): URL de referência do item
- `descricao` (String, opcional): Descrição detalhada do item

**Resposta:**
- `201 Created`: `ItemSolicitacaoEntity` contendo:
  - `id` (String): ID único da solicitação gerado pelo MongoDB
  - `nomePrincipal` (String): Nome principal do item
  - `urlReferencia` (String): URL de referência (pode ser null)
  - `descricao` (String): Descrição do item (pode ser null)
  - `status` (ItemSolicitacaoStatus): Status da solicitação (padrão: `PENDENTE`)
  - `regional` (String): Regional do usuário que criou a solicitação (preenchido automaticamente baseado na filialHcm do usuário, pode ser null)
  - `createdBy` (String): Usuário que criou a solicitação (preenchido automaticamente)
  - `dataCriacao` (LocalDateTime): Data e hora de criação (preenchido automaticamente)
  - `dataAtualizacao` (LocalDateTime): Data e hora da última atualização (atualizado automaticamente)

**Status Disponíveis:**
- `PENDENTE`: Status inicial quando a solicitação é criada
- `EM_ANALISE`: Solicitação em análise
- `APROVADO`: Solicitação aprovada
- `REJEITADO`: Solicitação rejeitada
- `CANCELADO`: Solicitação cancelada
- `CADASTRADO`: Item foi cadastrado no sistema (status alterado quando o item é cadastrado)

**Nota:**
- O campo `nomePrincipal` é obrigatório e será validado
- O status é automaticamente definido como `PENDENTE` ao criar a solicitação
- O campo `regional` é preenchido automaticamente baseado na filialHcm do usuário autenticado através da relação entre `tb_funcionarios` e `tb_regional`
- Os campos de auditoria (`createdBy`, `dataCriacao`, `dataAtualizacao`) são preenchidos automaticamente pelo Spring Data MongoDB
- Se não houver usuário autenticado, o sistema usa `"system"` como criador
- Se não for possível determinar a regional do usuário, o campo `regional` será `null`

**Exemplo de Requisição:**
```json
POST /api/mega-itens/solicitacao-item
Content-Type: application/json

{
  "nomePrincipal": "Cabo Elétrico 2.5mm",
  "urlReferencia": "https://exemplo.com/produto/cabo-eletrico-2-5mm",
  "descricao": "Cabo elétrico flexível 2.5mm² para instalações elétricas residenciais"
}
```

**Resposta de Exemplo:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "nomePrincipal": "Cabo Elétrico 2.5mm",
  "urlReferencia": "https://exemplo.com/produto/cabo-eletrico-2-5mm",
  "descricao": "Cabo elétrico flexível 2.5mm² para instalações elétricas residenciais",
  "status": "PENDENTE",
  "regional": "Norte",
  "createdBy": "usuario@exemplo.com",
  "dataCriacao": "2025-12-05T10:30:00",
  "dataAtualizacao": "2025-12-05T10:30:00"
}
```

---

### 11. Listar Solicitações de Item

**GET** `/api/mega-itens/solicitacao-item`

Retorna uma lista paginada de todas as solicitações de item cadastradas no MongoDB, ordenadas por data de criação (mais recentes primeiro). Retorna apenas os dados básicos para listagem.

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 20): Tamanho da página
- `sort` (opcional, padrão: "dataCriacao"): Campo para ordenação
- `direction` (opcional, padrão: DESC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `ItemSolicitacaoListagemDTO` contendo apenas os dados básicos:
  - `id`: ID único da solicitação
  - `nomePrincipal`: Nome principal do item
  - `descricao`: Descrição do item
  - `status`: Status da solicitação (PENDENTE, EM_ANALISE, APROVADO, REJEITADO, CANCELADO, CADASTRADO)
  - `regional`: Regional do usuário que criou a solicitação (preenchido automaticamente baseado na filialHcm)
  - `solicitante`: ID do usuário que criou a solicitação (createdBy)
  - `dataCriacao`: Data e hora de criação

**Exemplo:**
```
GET /api/mega-itens/solicitacao-item?page=0&size=20
GET /api/mega-itens/solicitacao-item?page=0&size=50&sort=dataCriacao&direction=ASC
```

**Resposta de Exemplo:**
```json
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439011",
      "nomePrincipal": "Cabo Elétrico 2.5mm",
      "descricao": "Cabo elétrico flexível 2.5mm² para instalações elétricas residenciais",
      "status": "PENDENTE",
      "regional": "Norte",
      "solicitante": "445c92d9-43c7-4366-9ceb-6979de9ef432",
      "dataCriacao": "2025-12-05T10:30:00"
    },
    {
      "id": "507f1f77bcf86cd799439012",
      "nomePrincipal": "Parafuso 6x20",
      "descricao": "Parafuso autoatarraxante 6x20mm",
      "status": "EM_ANALISE",
      "regional": "Sul",
      "solicitante": "123e4567-e89b-12d3-a456-426614174000",
      "dataCriacao": "2025-12-05T09:15:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1
}
```

---

### 12. Detalhar Solicitação de Item

**GET** `/api/mega-itens/solicitacao-item/{id}`

Retorna todos os detalhes de uma solicitação de item específica, incluindo todos os campos e histórico de logs (stepLog).

**Parâmetros de Path:**
- `id` (String, obrigatório): ID da solicitação de item

**Resposta:**
- `200 OK`: `ItemSolicitacaoEntity` completa contendo todos os campos:
  - `id`: ID único da solicitação
  - `nomePrincipal`: Nome principal do item
  - `urlReferencia`: URL de referência (pode ser null)
  - `descricao`: Descrição do item (pode ser null)
  - `regional`: Regional do usuário que criou a solicitação
  - `codigoItem`: Código do item cadastrado (pode ser null se ainda não foi cadastrado)
  - `grupoItem`: Código do grupo do item (pode ser null se ainda não foi cadastrado)
  - `nomeItem`: Nome do item cadastrado (pode ser null se ainda não foi cadastrado)
  - `observacaoCadastro`: Observações sobre o cadastro (pode ser null se ainda não foi cadastrado)
  - `status`: Status da solicitação (PENDENTE, EM_ANALISE, APROVADO, REJEITADO, CANCELADO, CADASTRADO)
  - `createdBy`: ID do usuário que criou a solicitação
  - `dataCriacao`: Data e hora de criação
  - `dataAtualizacao`: Data e hora da última atualização
  - `stepLog`: Lista de logs de ações realizadas na solicitação (cadastro, rejeição, etc.)
- `404 Not Found`: Quando a solicitação não for encontrada

**Exemplo de Requisição:**
```
GET /api/mega-itens/solicitacao-item/507f1f77bcf86cd799439011
```

**Resposta de Exemplo:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "nomePrincipal": "Cabo Elétrico 2.5mm",
  "urlReferencia": "https://exemplo.com/produto/cabo-eletrico-2-5mm",
  "descricao": "Cabo elétrico flexível 2.5mm² para instalações elétricas residenciais",
  "regional": "Norte",
  "codigoItem": 12345,
  "grupoItem": 15,
  "nomeItem": "Cabo Elétrico 2.5mm Flexível",
  "observacaoCadastro": "Item cadastrado no sistema Mega com sucesso",
  "status": "CADASTRADO",
  "createdBy": "445c92d9-43c7-4366-9ceb-6979de9ef432",
  "dataCriacao": "2025-12-05T10:30:00",
  "dataAtualizacao": "2025-12-05T11:45:00",
  "stepLog": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Item Cadastrado",
      "usuario": "445c92d9-43c7-4366-9ceb-6979de9ef432",
      "criado_em": "2025-12-05T11:45:00",
      "finalizado_em": "2025-12-05T11:45:00",
      "etapa": 1,
      "observacao": "Item cadastrado no sistema Mega com sucesso",
      "grupo": null
    }
  ]
}
```

---

### 13. Cadastrar Item da Solicitação

**PUT** `/api/mega-itens/solicitacao-item/{id}/cadastrar`

Cadastra o item de uma solicitação, adicionando informações do item cadastrado e alterando o status para "Cadastrado".

**Parâmetros de Path:**
- `id` (String, obrigatório): ID da solicitação de item a ser cadastrada

**Corpo da Requisição (JSON):**
- `codigoItem` (Integer, obrigatório): Código do item cadastrado
- `grupoItem` (Integer, obrigatório): Código do grupo do item
- `nomeItem` (String, obrigatório): Nome do item cadastrado
- `observacaoCadastro` (String, opcional): Observações sobre o cadastro do item

**Resposta:**
- `200 OK`: `ItemSolicitacaoEntity` atualizada contendo todos os campos, incluindo os novos campos de cadastro
- `404 Not Found`: Quando a solicitação não for encontrada

**Nota:**
- Ao cadastrar o item, o status é automaticamente alterado para `CADASTRADO`
- Os campos `codigoItem`, `grupoItem`, `nomeItem` e `observacaoCadastro` são preenchidos com os valores fornecidos
- O campo `dataAtualizacao` é atualizado automaticamente pelo Spring Data MongoDB

**Exemplo de Requisição:**
```json
PUT /api/mega-itens/solicitacao-item/507f1f77bcf86cd799439011/cadastrar
Content-Type: application/json

{
  "codigoItem": 12345,
  "grupoItem": 15,
  "nomeItem": "Cabo Elétrico 2.5mm Flexível",
  "observacaoCadastro": "Item cadastrado no sistema Mega com sucesso"
}
```

**Resposta de Exemplo:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "nomePrincipal": "Cabo Elétrico 2.5mm",
  "urlReferencia": "https://exemplo.com/produto/cabo-eletrico-2-5mm",
  "descricao": "Cabo elétrico flexível 2.5mm² para instalações elétricas residenciais",
  "status": "CADASTRADO",
  "regional": "Norte",
  "codigoItem": 12345,
  "grupoItem": 15,
  "nomeItem": "Cabo Elétrico 2.5mm Flexível",
  "observacaoCadastro": "Item cadastrado no sistema Mega com sucesso",
  "createdBy": "usuario@exemplo.com",
  "dataCriacao": "2025-12-05T10:30:00",
  "dataAtualizacao": "2025-12-05T11:45:00"
}
```

---

### 14. Rejeitar Solicitação de Item

**PUT** `/api/mega-itens/solicitacao-item/{id}/rejeitar`

Rejeita uma solicitação de item, alterando o status para "Rejeitado".

**Parâmetros de Path:**
- `id` (String, obrigatório): ID da solicitação de item a ser rejeitada

**Corpo da Requisição:**
- Nenhum (endpoint não requer body)

**Resposta:**
- `200 OK`: `ItemSolicitacaoEntity` atualizada com status `REJEITADO`
- `404 Not Found`: Quando a solicitação não for encontrada

**Nota:**
- Ao rejeitar a solicitação, o status é automaticamente alterado para `REJEITADO`
- O campo `dataAtualizacao` é atualizado automaticamente pelo Spring Data MongoDB

**Exemplo de Requisição:**
```
PUT /api/mega-itens/solicitacao-item/507f1f77bcf86cd799439011/rejeitar
```

**Resposta de Exemplo:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "nomePrincipal": "Cabo Elétrico 2.5mm",
  "urlReferencia": "https://exemplo.com/produto/cabo-eletrico-2-5mm",
  "descricao": "Cabo elétrico flexível 2.5mm² para instalações elétricas residenciais",
  "status": "REJEITADO",
  "regional": "Norte",
  "codigoItem": null,
  "grupoItem": null,
  "nomeItem": null,
  "observacaoCadastro": null,
  "createdBy": "usuario@exemplo.com",
  "dataCriacao": "2025-12-05T10:30:00",
  "dataAtualizacao": "2025-12-05T12:00:00"
}
```

---

### 15. Listar Códigos de Aplicação

**GET** `/api/mega-itens/codigo-aplicacao`

Retorna uma lista paginada de códigos de aplicação do sistema Mega, com suporte a filtros opcionais.

**Parâmetros de Query (opcionais):**
- `descAplicacao` (String): Descrição da aplicação para filtrar (busca case-insensitive com LIKE)
- `codAplicacao` (Integer): Código da aplicação para filtrar

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 20): Tamanho da página
- `sort` (opcional, padrão: "codAplicacao"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `CodigoAplicacaoMegaEntity` contendo:
  - `codAplicacao` (Long): Código da aplicação
  - `descAplicacao` (String): Descrição da aplicação

**Exemplo:**
```
GET /api/mega-itens/codigo-aplicacao?descAplicacao=construção&page=0&size=20
GET /api/mega-itens/codigo-aplicacao?codAplicacao=1
```

---

### 16. Listar Códigos de Serviço

**GET** `/api/mega-itens/codigo-servico`

Retorna uma lista paginada de códigos de serviço do sistema Mega, com suporte a filtros opcionais.

**Parâmetros de Query (opcionais):**
- `descServico` (String): Descrição do serviço para filtrar (busca case-insensitive com LIKE)
- `codServico` (Integer): Código do serviço para filtrar

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 20): Tamanho da página
- `sort` (opcional, padrão: "codServico"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `CodigoServicoMegaEntity` contendo:
  - `codServico` (Long): Código do serviço
  - `descServico` (String): Descrição do serviço

**Exemplo:**
```
GET /api/mega-itens/codigo-servico?descServico=manutenção&page=0&size=20
GET /api/mega-itens/codigo-servico?codServico=1
```

---

### 17. Listar Códigos ICMS

**GET** `/api/mega-itens/codigo-icms`

Retorna uma lista completa de todos os códigos ICMS do sistema Mega.

**Parâmetros:** Nenhum

**Resposta:**
- `200 OK`: Lista de `CodigoIcmsMegaEntity` contendo:
  - `codIcms` (Long): Código ICMS
  - `descIcms` (String): Descrição do código ICMS

**Exemplo:**
```
GET /api/mega-itens/codigo-icms
```

---

### 18. Listar Códigos PIS/COFINS

**GET** `/api/mega-itens/codigo-pis-cofins`

Retorna uma lista paginada de códigos PIS/COFINS do sistema Mega.

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 20): Tamanho da página
- `sort` (opcional, padrão: "codPisCofins"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `CodigoPisCofinsMegaEntity` contendo:
  - `codPisCofins` (Long): Código PIS/COFINS
  - `descPisCofins` (String): Descrição do código PIS/COFINS

**Exemplo:**
```
GET /api/mega-itens/codigo-pis-cofins?page=0&size=20
```

---

### 19. Listar Códigos de Situação Tributária

**GET** `/api/mega-itens/codigo-situacao-tributaria`

Retorna uma lista paginada de códigos de situação tributária do sistema Mega.

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 20): Tamanho da página
- `sort` (opcional, padrão: "codSitTributaria"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `CodigoSituacaoTributariaMegaEntity` contendo:
  - `codSitTributaria` (Long): Código de situação tributária
  - `descSitTributaria` (String): Descrição da situação tributária

**Exemplo:**
```
GET /api/mega-itens/codigo-situacao-tributaria?page=0&size=20
```

---

### 20. Listar Códigos SPED Fiscal

**GET** `/api/mega-itens/codigo-sped-fiscal`

Retorna uma lista paginada de códigos SPED Fiscal do sistema Mega.

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 20): Tamanho da página
- `sort` (opcional, padrão: "codSpedFiscal"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `CodigoSpedFiscalMegaEntity` contendo:
  - `codSpedFiscal` (Long): Código SPED Fiscal
  - `descSpedFiscal` (String): Descrição do código SPED Fiscal

**Exemplo:**
```
GET /api/mega-itens/codigo-sped-fiscal?page=0&size=20
```

---

### 21. Listar Unidades de Medida

**GET** `/api/mega-itens/unidade-medida`

Retorna uma lista completa de todas as unidades de medida cadastradas no sistema Mega.

**Parâmetros:** Nenhum

**Resposta:**
- `200 OK`: Lista de `UnidadeMedidaMegaEntity` contendo:
  - `uniMedItem` (String): Código da unidade de medida (chave primária)
  - `descMedItem` (String): Descrição da unidade de medida

**Exemplo:**
```
GET /api/mega-itens/unidade-medida
```

**Resposta de Exemplo:**
```json
[
  {
    "uniMedItem": "BD",
    "descMedItem": "Balde"
  },
  {
    "uniMedItem": "BL",
    "descMedItem": "Bloco"
  },
  {
    "uniMedItem": "BR",
    "descMedItem": "Barra"
  }
]
```

---

## Mega Orders Controller

Base Path: `/api/mega-orders`

### 1. Autocomplete de Fornecedores

**GET** `/api/mega-orders/fornecedor`

Retorna uma lista de fornecedores para autocomplete, limitada a 20 resultados, específica para pedidos.

**Parâmetros:**
- `term` (opcional, query parameter): Termo de busca para filtrar fornecedores

**Resposta:**
- `200 OK`: Lista de `AutocompleteOrdersDTO` com informações de fornecedores

**Exemplo:**
```
GET /api/mega-orders/fornecedor?term=fornecedor
```

---

### 2. Lista de Status de Pedidos

**GET** `/api/mega-orders/status_pedidos`

Retorna uma lista de todos os status distintos de pedidos disponíveis no sistema.

**Parâmetros:** Nenhum

**Resposta:**
- `200 OK`: Lista de strings contendo os status disponíveis

**Exemplo:**
```
GET /api/mega-orders/status_pedidos
```

---

### 3. Pesquisar Pedidos

**GET** `/api/mega-orders/pesquisar-pedidos`

Realiza uma pesquisa paginada de pedidos com base em filtros opcionais. Retorna pedidos agrupados.

**Parâmetros de Query (todos opcionais):**

**Filtros de Pedido:**
- `numeroPedido` (Integer): Número do pedido
- `tipoPedido` (String): Tipo do pedido
- `statusPedido` (String): Status do pedido
- `pedidoStartDate` (LocalDate, formato ISO): Data inicial do pedido
- `pedidoEndDate` (LocalDate, formato ISO): Data final do pedido

**Filtros de Item:**
- `codigoItem` (Integer): Código do item
- `nomeItem` (String): Nome do item
- `codigoGrupo` (Integer): Código do grupo
- `nomeGrupo` (String): Nome do grupo
- `statusItem` (String): Status do item

**Filtros de Fornecedor:**
- `codigoFornecedor` (Integer): Código do fornecedor
- `nomeFornecedor` (String): Nome do fornecedor

**Filtros de Pessoas:**
- `comprador` (String): Nome do comprador
- `solicitante` (String): Nome do solicitante

**Filtros de Organograma:**
- `diretoria` (String): Nome da diretoria
- `superintendencia` (String): Nome da superintendência
- `regional` (String): Nome da regional
- `contrato` (String): Nome do contrato
- `projeto` (String): Nome do projeto
- `filial` (String): Nome da filial

**Filtros de Valores:**
- `precoMedMin` (BigDecimal): Preço médio mínimo
- `precoMedMax` (BigDecimal): Preço médio máximo
- `qtdePedidosMin` (Integer): Quantidade mínima de pedidos
- `qtdePedidosMax` (Integer): Quantidade máxima de pedidos
- `qtdeComprasMin` (BigDecimal): Quantidade mínima de compras
- `qtdeComprasMax` (BigDecimal): Quantidade máxima de compras

**Filtros de Data de Cadastro:**
- `cadastroStartDate` (LocalDate, formato ISO): Data inicial de cadastro
- `cadastroEndDate` (LocalDate, formato ISO): Data final de cadastro

**Parâmetros de Paginação:**
- `page` (opcional, padrão: 0): Número da página
- `size` (opcional, padrão: 200): Tamanho da página
- `sort` (opcional, padrão: "orderNumber"): Campo para ordenação
- `direction` (opcional, padrão: ASC): Direção da ordenação (ASC/DESC)

**Resposta:**
- `200 OK`: Página de `OrderResponseDTO` contendo os pedidos encontrados agrupados

**Notas:**
- Se nenhum filtro for informado, retorna todos os pedidos
- Se `cadastroEndDate` não for informado, assume a data atual
- Se `cadastroStartDate` não for informado, assume 1 ano antes da data final de cadastro
- Se `pedidoEndDate` não for informado, assume a data atual
- Se `pedidoStartDate` não for informado, assume 1 ano antes da data final do pedido

**Exemplo:**
```
GET /api/mega-orders/pesquisar-pedidos?numeroPedido=123&statusPedido=ATIVO&codigoFornecedor=456&page=0&size=200
GET /api/mega-orders/pesquisar-pedidos?pedidoStartDate=2024-01-01&pedidoEndDate=2024-12-31&regional=Norte
GET /api/mega-orders/pesquisar-pedidos?codigoItem=789&nomeFornecedor=Fornecedor%20XYZ&precoMedMin=100.00
```

---

## ABC Report Controller

Base Path: `/api/curva-abc`

### 1. Obter Dados da Curva ABC

**GET** `/api/curva-abc`

Calcula e retorna os dados da curva ABC com base nos filtros fornecidos.

**Parâmetros de Query (todos opcionais):**
- `startDate` (LocalDate): Data inicial do período de análise
- `endDate` (LocalDate): Data final do período de análise
- `criteria` (AbcClassificationCriteria): Critério de classificação ABC
  - Valores possíveis (em português): `VALOR_TOTAL`, `QUANTIDADE`, `PRECO_MEDIO`
  - Valores possíveis (em inglês): `TOTAL_VALUE`, `QUANTITY`, `AVERAGE_PRICE`
  - Se não informado, assume `VALOR_TOTAL` como padrão

**Resposta:**
- `200 OK`: Lista de `AbcCurveGroupDTO` contendo os dados da curva ABC agrupados

**Nota:** 
- Se `endDate` não for informado, assume a data atual
- Se `startDate` não for informado, assume 1 ano antes da data final

**Exemplo:**
```
GET /api/curva-abc?startDate=2024-01-01&endDate=2024-12-31&criteria=VALOR_TOTAL
```

---

### 2. Download do Relatório ABC em PDF

**GET** `/api/curva-abc/download-pdf`

Gera e retorna um arquivo PDF com o relatório da curva ABC.

**Parâmetros de Query (todos opcionais):**
- `startDate` (LocalDate): Data inicial do período de análise
- `endDate` (LocalDate): Data final do período de análise
- `criteria` (AbcClassificationCriteria): Critério de classificação ABC
  - Valores possíveis (em português): `VALOR_TOTAL`, `QUANTIDADE`, `PRECO_MEDIO`
  - Valores possíveis (em inglês): `TOTAL_VALUE`, `QUANTITY`, `AVERAGE_PRICE`

**Resposta:**
- `200 OK`: Arquivo PDF com o relatório da curva ABC
- `204 No Content`: Quando não há dados para gerar o PDF
- `500 Internal Server Error`: Erro ao gerar o PDF

**Headers de Resposta:**
- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="relatorio_curva_abc.pdf"`

**Exemplo:**
```
GET /api/curva-abc/download-pdf?startDate=2024-01-01&endDate=2024-12-31&criteria=VALOR_TOTAL
```

---

### 3. Download do Relatório ABC em Excel

**GET** `/api/curva-abc/download-excel`

Gera e retorna um arquivo Excel (XLSX) com o relatório da curva ABC.

**Parâmetros de Query (todos opcionais):**
- `startDate` (LocalDate): Data inicial do período de análise
- `endDate` (LocalDate): Data final do período de análise
- `criteria` (AbcClassificationCriteria): Critério de classificação ABC
  - Valores possíveis (em português): `VALOR_TOTAL`, `QUANTIDADE`, `PRECO_MEDIO`
  - Valores possíveis (em inglês): `TOTAL_VALUE`, `QUANTITY`, `AVERAGE_PRICE`

**Resposta:**
- `200 OK`: Arquivo Excel com o relatório da curva ABC

**Headers de Resposta:**
- `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition: attachment; filename="curva_abc.xlsx"`

**Exemplo:**
```
GET /api/curva-abc/download-excel?startDate=2024-01-01&endDate=2024-12-31&criteria=VALOR_TOTAL
```

---

### 4. Listar Critérios de Classificação ABC

**GET** `/api/curva-abc/abc-criterios`

Retorna a lista de todos os critérios de classificação ABC disponíveis com suas descrições.

**Parâmetros:** Nenhum

**Resposta:**
- `200 OK`: Lista de `CriteriaDto` contendo:
  - `name`: Nome do critério (enum)
  - `description`: Descrição do critério

**Exemplo:**
```
GET /api/curva-abc/abc-criterios
```

**Resposta de Exemplo:**
```json
[
  {
    "name": "TOTAL_VALUE",
    "description": "Valor total"
  },
  {
    "name": "QUANTITY",
    "description": "Quantidade"
  },
  {
    "name": "AVERAGE_PRICE",
    "description": "Preço médio"
  }
]
```

---

## Observações Gerais

### Formato de Datas

Todos os parâmetros de data devem ser enviados no formato ISO (YYYY-MM-DD), por exemplo: `2024-01-01`

### Paginação

Os endpoints que suportam paginação utilizam os parâmetros padrão do Spring Data:
- `page`: Número da página (começando em 0)
- `size`: Tamanho da página
- `sort`: Campo para ordenação
- `direction`: Direção da ordenação (ASC ou DESC)

### Autenticação

Todos os endpoints requerem autenticação. Consulte a configuração de segurança da aplicação para mais detalhes.

### Limites de Autocomplete

Os endpoints de autocomplete retornam no máximo 20 resultados por padrão. O endpoint genérico `/api/mega-itens/{type}` substitui os endpoints específicos de autocomplete e oferece suporte a múltiplos tipos de busca. Use o endpoint `/api/mega-itens/type/disponiveis` para obter a lista completa de tipos disponíveis.

### Armazenamento de Dados

O módulo utiliza dois tipos de banco de dados:
- **PostgreSQL**: Para dados principais do sistema Mega (itens, pedidos, etc.) através de views JPA
- **MongoDB**: Para solicitações de item (`ItemSolicitacaoEntity`), permitindo flexibilidade no armazenamento de dados não estruturados e auditoria automática

### Status de Solicitações de Item

Os status disponíveis para solicitações de item são:
- `PENDENTE`: Status inicial quando a solicitação é criada
- `EM_ANALISE`: Solicitação em análise
- `APROVADO`: Solicitação aprovada
- `REJEITADO`: Solicitação rejeitada
- `CANCELADO`: Solicitação cancelada
- `CADASTRADO`: Item foi cadastrado no sistema (status alterado quando o item é cadastrado)

---

## Estrutura do Módulo

```
modulo_mega/
├── application/
│   ├── dto/          # Data Transfer Objects
│   └── mapper/       # Mappers para conversão de entidades
├── domain/
│   ├── enums/        # Enumeradores (AbcClassificationCriteria, ItemSolicitacaoStatus)
│   ├── entities/
│   │   └── mongo/    # Entidades MongoDB
│   │       └── ItemSolicitacaoEntity.java
│   ├── repository/   # Repositórios e especificações
│   │   ├── mongo/    # Repositórios MongoDB
│   │   │   └── ItemSolicitacaoRepository.java
│   │   ├── projection/  # Projeções para queries
│   │   └── specs/    # Specifications para queries dinâmicas
│   ├── persistence/
│   │   └── view/     # Views JPA
│   │       ├── MegaItem.java    # Entidade JPA para itens Mega
│   │       └── MegaOrders.java  # Entidade JPA para pedidos Mega
├── infra/
│   └── config/       # Configurações de infraestrutura
│       └── MegaDatabaseConfig.java
├── presentation/
│   └── controller/   # Controllers REST
│       ├── MegaItemController.java
│       ├── MegaOrdersController.java
│       └── AbcReportController.java
└── service/          # Serviços de negócio
    ├── MegaItemService.java
    ├── AbcCurveService.java
    ├── PdfGeneratorService.java
    └── ExcelGeneratorService.java
```

---

## Entidades Principais

### MegaItem
Entidade JPA que representa os itens do sistema Mega. Mapeada para a view `mega_items_v` no banco de dados. Contém informações sobre itens, fornecedores, grupos, preços, quantidades e datas relacionadas.

### MegaOrders
Entidade JPA que representa os pedidos do sistema Mega. Mapeada para a view `mega_orders_v` no banco de dados. Contém informações sobre pedidos, fornecedores, status e demais dados relacionados a pedidos.

### ItemSolicitacaoEntity
Entidade MongoDB que representa uma solicitação de item no sistema. Armazenada na coleção `item_solicitacao_collection` do MongoDB. Contém informações sobre solicitações de novos itens, incluindo dados de cadastro, status, regional e auditoria automática.

---

## DTOs Principais

### ItemGroupedDTO
DTO usado para retornar itens agrupados na pesquisa, contendo informações consolidadas como:
- Código e descrição do item
- Código e nome do grupo
- Status do item
- Quantidades e preços agregados
- Número de fornecedores distintos

### ItemDetailedDTO
DTO usado para retornar detalhes completos de um item, incluindo:
- Estatísticas base
- Detalhes agrupados por regional
- Detalhes agrupados por filial
- Informações de estoque

### ItemHistoryDetailedDTO
DTO usado para retornar o histórico detalhado de compras de um item, contendo todas as informações de cada transação.

### ItemComplementDTO
DTO usado para retornar itens com seus complementos, relacionando dados das tabelas `tb_notas_mega` e `tb_itens_mega`, contendo:
- `idItem`: Código do item (id_item)
- `nomeItem`: Nome do item (item de tb_itens_mega)
- `itemComplemento`: Complemento do item (item_complemento de tb_notas_mega)

### ItemSolicitacaoDTO
DTO usado para criar solicitações de item, contendo:
- `nomePrincipal`: Nome principal do item (obrigatório)
- `urlReferencia`: URL de referência do item (opcional)
- `descricao`: Descrição detalhada do item (opcional)

### CadastrarItemSolicitacaoDTO
DTO usado para cadastrar um item de uma solicitação, contendo:
- `codigoItem`: Código do item (obrigatório)
- `grupoItem`: Código do grupo do item (obrigatório)
- `nomeItem`: Nome do item (obrigatório)
- `observacaoCadastro`: Observações sobre o cadastro (opcional)

### ItemSolicitacaoListagemDTO
DTO usado para listar solicitações de item, contendo apenas os dados básicos:
- `id`: ID único da solicitação
- `nomePrincipal`: Nome principal do item
- `descricao`: Descrição do item
- `status`: Status da solicitação
- `regional`: Regional do usuário que criou a solicitação
- `solicitante`: ID do usuário que criou a solicitação
- `dataCriacao`: Data e hora de criação

### ItemSolicitacaoEntity
Entidade MongoDB que representa uma solicitação de item, contendo:
- `id`: ID único gerado pelo MongoDB
- `nomePrincipal`: Nome principal do item
- `urlReferencia`: URL de referência
- `descricao`: Descrição do item
- `regional`: Regional do usuário que criou a solicitação (baseada na filialHcm)
- `status`: Status da solicitação (enum ItemSolicitacaoStatus, padrão: PENDENTE)
- `codigoItem`: Código do item cadastrado (preenchido quando o item é cadastrado)
- `grupoItem`: Código do grupo do item (preenchido quando o item é cadastrado)
- `nomeItem`: Nome do item cadastrado (preenchido quando o item é cadastrado)
- `observacaoCadastro`: Observações sobre o cadastro (preenchido quando o item é cadastrado)
- `createdBy`: Usuário que criou a solicitação (preenchido automaticamente)
- `dataCriacao`: Data e hora de criação (preenchido automaticamente)
- `dataAtualizacao`: Data e hora da última atualização (atualizado automaticamente)

### CadastrarItemSolicitacaoDTO
DTO usado para cadastrar um item de uma solicitação, contendo:
- `codigoItem`: Código do item (obrigatório)
- `grupoItem`: Código do grupo do item (obrigatório)
- `nomeItem`: Nome do item (obrigatório)
- `observacaoCadastro`: Observações sobre o cadastro (opcional)

### OrdersGroupedDTO
DTO usado para retornar pedidos agrupados, contendo informações consolidadas dos pedidos.

### AbcCurveGroupDTO
DTO usado para retornar os dados da curva ABC, contendo a classificação dos itens em categorias A, B e C.

### AutocompleteTypeDTO
DTO usado para retornar informações sobre os tipos de autocomplete disponíveis, contendo:
- `type`: Valor do tipo (usado no endpoint de autocomplete)
- `label`: Descrição legível do tipo

### CriteriaDto
DTO usado para retornar informações sobre os critérios de classificação ABC, contendo:
- `name`: Nome do critério (enum)
- `description`: Descrição do critério

---

**Última atualização:** Janeiro 2025

---

## Changelog

### Janeiro 2025
- Adicionado endpoint genérico de autocomplete `/api/mega-itens/{type}` com suporte a múltiplos tipos
- Adicionado endpoint `/api/mega-itens/type/disponiveis` para listar tipos de autocomplete disponíveis
- Tipos de autocomplete suportados: fornecedor, grupo, comprador, regional, solicitante, projeto, filial_mega, contrato, codigo_item, nome_do_item, codigo_grupo
- Adicionado endpoint `/api/mega-itens/itens-complemento` para buscar itens com complementos, relacionando tb_notas_mega e tb_itens_mega com busca por nome do item usando Criteria API
- Adicionado endpoint `POST /api/mega-itens/solicitacao-item` para criar solicitações de item no MongoDB com status inicial "Pendente" e auditoria automática
- Adicionado endpoint `GET /api/mega-itens/solicitacao-item` para listar solicitações de item com paginação, incluindo regional do usuário baseada na filialHcm
- Adicionado endpoint `GET /api/mega-itens/solicitacao-item/{id}` para detalhar uma solicitação de item específica com todos os campos e histórico de logs
- Adicionado endpoint `PUT /api/mega-itens/solicitacao-item/{id}/cadastrar` para cadastrar o item de uma solicitação, adicionando código do item, grupo, nome e observações, e alterando o status para "Cadastrado"
- Adicionado endpoint `PUT /api/mega-itens/solicitacao-item/{id}/rejeitar` para rejeitar uma solicitação de item, alterando o status para "Rejeitado"
- Implementado sistema de logs com StepLog para registrar ações de cadastro e rejeição de solicitações
- Adicionado endpoint `GET /api/mega-itens/unidade-medida` para listar todas as unidades de medida cadastradas na tabela `tb_unidade_medida_mega`
- Documentados endpoints de códigos auxiliares: codigo-aplicacao, codigo-servico, codigo-icms, codigo-pis-cofins, codigo-situacao-tributaria, codigo-sped-fiscal