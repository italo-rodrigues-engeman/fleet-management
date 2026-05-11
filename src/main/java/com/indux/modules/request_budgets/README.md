## 📚 Módulo Request Budgets

### 📋 Visão Geral
O módulo de Orçamentos gerencia o ciclo completo de uma oportunidade comercial: criação do orçamento base, versionamento com documentos e seções (Comercial, Engenharia, SMS, Operações), atualizações de filtros (etapas de aprovação), geração de OS, esclarecimentos, circulares, premissas e proposta final.

- Base path: `/api/budgets`
- Autenticação: JWT (`Authorization: Bearer <token>`)
- Uploads: 
  - Endpoints com anexos (Comercial, Engenharia, SMS, Operações, Filtros, OS, Esclarecimentos, Circulares, Premissas, Propostas) aceitam `multipart/form-data`
  - Os anexos devem ser enviados como `AttachmentEntity` com metadados já processados (id, nome, file com uri, extensão, mimeType)
  - O endpoint de criação de orçamento (`POST /api/budgets`) aceita apenas `application/json`

### 🧭 Fluxo resumido (etapas)
1) Criar Orçamento base → retorna `budgetId`
2) Criar Versão do Orçamento → retorna `versionId`
3) (Opcional) Adicionar seções: Comercial, Engenharia, SMS, Operações
4) Atualizar Filtro 1 (aprovação inicial)
5) Atualizar Filtro 2 (aprovação final)
6) (Opcional) Criar OS para o orçamento
7) (Opcional) Registrar Esclarecimentos
8) (Opcional) Registrar Circulares
9) (Opcional) Registrar Premissas
10) Criar Proposta final

---

## 📡 Endpoints e Campos

### 🔀 Regras de Transição de Etapas
- Após aprovar a versão do orçamento, a etapa do fluxo avança para **Filtro 1**.
  - Observação: a aprovação de versão é uma regra de negócio do processo; a etapa Filtro 1 é realizada pelos endpoints de atualização de filtro descritos abaixo.

### 1) Criar Orçamento
- Método: `POST /api/budgets`
- Consumes: `application/json`
- Body (CreateBudgetRequest):
  - **clienteId** (Long) [obrigatório]
  - **mercadoId** (Long) [obrigatório]
  - **setorId** (Long) [obrigatório]
  - **solicitanteNome** (String)
  - **solicitanteFuncaoCargo** (String)
  - **solicitanteTelefone1** (String)
  - **solicitanteTelefone2** (String)
  - **solicitanteEmail1** (String)
  - **solicitanteEmail2** (String)
  - **solicitanteLocalizacao** (String)
  - **solicitanteObservacao** (String)
  - **nomeOportunidade** (String) [obrigatório e único: valida duplicidade]
  - **descricaoOportunidade** (String)
  - **tempoContrato** (String)
  - **porteEstimado** (BigDecimal)
  - **dataAbertura** (Date `yyyy-MM-dd`)
  - **representanteComercialId** (UUID) [obrigatório]
  - **detalhesGerais** (String)
  - **status** (String)
  - **dataAcompanhamento** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)

Resposta (CreateBudgetResponseDTO):
- message (String), budgetId (String), nomeOportunidade (String)

Observações de negócio:
- O serviço valida se já existe orçamento com o mesmo `nomeOportunidade`.
- O orçamento é criado com `createdBy/updatedBy` do usuário do token, e é registrado um `stepLog` inicial (etapa: "filtro1").

---

### 2) Obter Orçamento por ID
- Método: `GET /api/budgets/{id}`
- Retorno: `BudgetResponseDTO`

### 3) Listar Orçamentos
- Método: `GET /api/budgets`
- Retorno: `List<BudgetResponseDTO>`

---

## 🔑 Palavras-chave (Keywords)

### 3.1) Cadastrar palavras-chave (array)
- Método: `POST /api/budgets/keywords`
- Content-Type: `application/json`
- Body (CreateKeywordsRequest):
  - **keywords** (List<String>) [obrigatório] — palavras a cadastrar
- Comportamento:
  - Normaliza (trim), remove vazias e duplicadas
  - Ignora as que já existirem (índice único por valor)
  - Cria somente as novas e retorna a lista completa encontrada/criada, ordenada por valor (case-insensitive)
- Retorno: `201 Created` com `List<KeywordResponseDTO>`

### 3.2) Listar palavras-chave
- Método: `GET /api/budgets/keywords`
- Retorno: `List<KeywordResponseDTO>` (id, value, createdAt) ordenada por valor

---

## 📦 Versões do Orçamento

### 4) Criar Versão
- Método: `POST /api/budgets/{budgetId}/versions`
- Consumes: `multipart/form-data` ou `application/json`
- Body (CreateBudgetVersionRequest):
  - Documentos da oportunidade (todos opcionais):
    - **anexoMd** (List<AttachmentEntity>), **mdInfo** (String)
    - **anexoPpu** (List<AttachmentEntity>), **ppuInfo** (String)
    - **anexoSms** (List<AttachmentEntity>), **smsInfo** (String)
    - **anexoGerais** (List<AttachmentEntity>), **geraisInfo** (String)
    - **habilitacaoAnexo** (List<AttachmentEntity>), **habilitacaoInfo** (String)
  -Detalhes da oportunidade 
    - **modalidadeConcorrencia** (String) - Modalidade de concorrência (ex.: "Menor Preço", "Leilão", "Maior Desconto", "Outros")
    - **tipoOportunidade** (String) - ID do tipo de oportunidade
    - **caracteristicasOportunidade** (String)
    - **manutencao**
    - **operacao**
    - **atividadesDiversas**
    - **construcaoMontagem**
    - **fabricacao**
    - **projetos**
    - **diversos**
    - **detalhes**
    - **outros**
  - Proposta e entrega:
    - **tipoProposta** (String)
    - **dataEntrega** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
    - **metodoEntrega** (String)
    - **propostaLocal** (Boolean)
    - **cidades** (Lista<String>), **estados** (Lista<String>)
  
  - Outros campos:
    - **outroEmail** (String)
    - **portal** (String)
    - **acessoInfo** (String)

Resposta: `BudgetVersionResponseDTO` com os dados persistidos.

**Observações:**
- As seções Comercial, Engenharia, SMS e Operações devem ser adicionadas através de endpoints separados (ver seções abaixo).
- **Atualização automática de status:** Após salvar a versão, o sistema calcula automaticamente a pontuação baseada nos dados do orçamento e da versão. Se a pontuação calculada for maior ou igual à `pontuacaoReferencia` configurada no sistema de scoring, o documento é automaticamente movido para o **Filtro 2** (stepLog: `filtro2`, step: `3`), pulando o Filtro 1, sem necessidade de aprovação manual.

### 5) Listar Versões do Orçamento
- Método: `GET /api/budgets/{budgetId}/versions`

### 6) Obter Versão por ID
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}`

### 6.1) Aprovar Versão (mover para etapa 2 / Filtro 1)
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/approve`
- Efeito: atualiza o `status` do orçamento para `ETAPA_2` e registra transição no `stepLog` (nome `filtro1`, step `2`).
- Retorno: `BudgetVersionResponseDTO` da versão aprovada

---

## 📋 Seções da Versão

As seções Comercial, Engenharia, SMS e Operações são adicionadas separadamente através de endpoints específicos. Cada seção salva automaticamente o log do usuário (`createdBy`, `updatedBy`) e a data/hora (`createdAt`, `updatedAt`).

### 6.2) Adicionar Seção Comercial
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/comercial`
- Content-Type: `multipart/form-data`
- Body (CreateBudgetComercialRequest):
  - **comentariosComercial** (String)
  - **anexoComercial** (List<AttachmentEntity>) - Anexos com metadados já processados
  - **dataHoraComercial** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelComercial** (String)
- Retorno: `BudgetVersionResponseDTO` com a versão atualizada (incluindo a nova entrada na lista `comercial`)

### 6.3) Adicionar Seção Engenharia
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/engenharia`
- Content-Type: `multipart/form-data`
- Body (CreateBudgetEngenhariaRequest):
  - **comentariosEngenharia** (String)
  - **anexoEngenharia** (List<AttachmentEntity>) - Anexos com metadados já processados
  - **responsavelEngenharia** (String)
- Retorno: `BudgetVersionResponseDTO` com a versão atualizada (incluindo a nova entrada na lista `engenharia`)

### 6.4) Adicionar Seção SMS
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/sms`
- Content-Type: `multipart/form-data`
- Body (CreateBudgetSmsRequest):
  - **comentariosSms** (String)
  - **anexoSms** (List<AttachmentEntity>) - Anexos com metadados já processados
  - **datahoraSms** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelSms** (String)
- Retorno: `BudgetVersionResponseDTO` com a versão atualizada (incluindo a nova entrada na lista `sms`)

### 6.5) Adicionar Seção Operações
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/operacoes`
- Content-Type: `multipart/form-data`
- Body (CreateBudgetOperacaoRequest):
  - **comentariosOperacao** (String)
  - **anexoOperacao** (List<AttachmentEntity>) - Anexos com metadados já processados
  - **datahoraOperacao** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelOperacao** (String)
- Retorno: `BudgetVersionResponseDTO` com a versão atualizada (incluindo a nova entrada na lista `operacoes`)

**Observações:**
- Cada chamada adiciona uma nova entrada na lista correspondente da versão
- Todas as entradas incluem automaticamente `createdBy`, `updatedBy`, `createdAt` e `updatedAt`
- Os anexos devem ser enviados como `AttachmentEntity` com metadados já processados

---

## ✅ Filtros (Etapas de Aprovação)

### Semântica de criação/atualização
- Não existe endpoint separado para “criar” filtros. A primeira chamada aos endpoints de Filtro 1 e Filtro 2 com `PUT` realiza a criação dos dados do filtro na versão. Chamadas subsequentes fazem atualização.
- Formatos aceitos: `multipart/form-data` (para anexos) ou `application/json`.
- Dica: se você precisa de uma semântica explícita de criação (POST), é possível adicionar endpoints `POST /filtro1` e `POST /filtro2` no futuro, mas funcionalmente o comportamento atual já cobre criação/atualização via `PUT`.

### 7.0) Criar Filtro 1 (semântica explícita)
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/filtro1`
- Consumes: `multipart/form-data` ou `application/json`
- Body: mesmo payload de `Filtro1RequestDTO` (ver seção 7)
- Retorno: `201 Created` com `BudgetVersionResponseDTO`

### 7) Atualizar Filtro 1
- Método: `PUT /api/budgets/{budgetId}/versions/{versionId}/filtro1`
- Consumes: `multipart/form-data` ou `application/json`
- Body (Filtro1RequestDTO):
  - **filtro1** (String)
  - **datahoraFiltro1** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelFiltro1** (String)
  - **anexoFiltro1** (List<AttachmentEntity>)
  - **motivoFiltro1** (String)
  - **responsavelFilrtro1** (String)
  - Campos adicionais:
    - **numeroAc** (String)
    - **oracamentista** (String)
    - **dataDesignacao** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
    - **justificativaEngeman** (String)
    - **justificativaSolicitante** (String)

### 8) Atualizar Filtro 2
- Método: `PUT /api/budgets/{budgetId}/versions/{versionId}/filtro2`
- Consumes: `multipart/form-data` ou `application/json`
- Body (Filtro2RequestDTO):
  - **filtro2** (String)
  - **datahoraFiltro2** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelFiltro2** (String)
  - **justificativaFiltro2** (String)
  - **motivoFiltro2** (String)
  - **anexoFiltro2** (List<AttachmentEntity>)
  - **dataFiltro2** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **justificativaSolicitanteFiltro2** (String)

### 8.1) Criar Filtro 2 (semântica explícita)
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/filtro2`
- Consumes: `multipart/form-data` ou `application/json`
- Body: mesmo payload de `Filtro2RequestDTO` (ver seção 8)
- Retorno: `201 Created` com `BudgetVersionResponseDTO`

---

## 🧾 OS (Ordem de Serviço)

---

## 🧮 Scoring (Pontuação)

### 0) Conceito
Permite atribuir pontuações às seguintes categorias para priorização/ranqueamento de oportunidades:
- Palavras‑chave (cadastradas previamente)
- Mercado (Público, Privado)
- Setor (ex.: Siderurgia e Metalurgia, Mineração, Papel e Celulose, Óleo e Gás, etc.)
- Tempo de contrato (ex.: <12, 12–24, 24–36, >36 meses)
- Porte estimado (Pequeno, Médio, Grande)
- Modalidade de concorrência (Menor Preço, Leilão, Maior Desconto, Outros)
- Tipo (Rotina-Continuado, Rotina-Sob Demanda, Spot, Parada de Manutenção, Guarda Chuva, Outros)
- Característica (Mão de Obra, Serviços, Revenda de Peças, Facilities, Outros)
- Estados (todas as UFs do Brasil)

### 1) Upsert de pontuações (merge por categoria)
- Método: `POST /api/budgets/scoring`
- Content-Type: `application/json`
- Body (UpsertScoringRequest - enviar apenas as categorias que deseja atualizar; as não enviadas são preservadas):
  - **pontuacaoReferencia** (Integer, opcional) - Pontuação de referência para cálculo/comparação de scores. Valor usado como base para comparação ou cálculo de pontuações relativas.
```json
{
  "keywords": [{ "value": "siderurgia", "score": 10 }, { "value": "manutenção", "score": 8 }],
  "mercado": [{ "value": "Público", "score": 5 }, { "value": "Privado", "score": 3 }],
  "setor": [{ "value": "Siderurgia e Metalurgica", "score": 7 }],
  "tempoContrato": [
    { "value": "Menor que 12 meses", "score": 1 },
    { "value": "Entre 12 meses e 24 meses", "score": 3 },
    { "value": "Entre 24 meses e 36 meses", "score": 5 },
    { "value": "Maior que 36 meses", "score": 8 }
  ],
  "porteEstimado": [
    { "value": "Pequeno", "score": 1 },
    { "value": "Médio", "score": 3 },
    { "value": "Grande", "score": 6 }
  ],
  "modalidadeConcorrencia": [
    { "value": "Menor Preço", "score": 5 },
    { "value": "Leilão", "score": 3 },
    { "value": "Maior Desconto", "score": 4 },
    { "value": "Outros", "score": 1 }
  ],
  "tipo": [
    { "value": "Rotina-Continuado", "score": 3 },
    { "value": "Rotina-Sob Demanda", "score": 2 },
    { "value": "Spot", "score": 1 },
    { "value": "Parada de Manutenção", "score": 5 },
    { "value": "Guarda Chuva", "score": 2 },
    { "value": "Outros", "score": 1 }
  ],
  "caracteristica": [
    { "value": "Venda de Mão de Obra (Diária / Mensal)", "score": 4 },
    { "value": "Fornecimento de serviços", "score": 3 },
    { "value": "Revenda de Peças e Sobressalentes", "score": 2 },
    { "value": "Facilities", "score": 3 },
    { "value": "Outros", "score": 1 }
  ],
  "estados": [{ "value": "SP", "score": 5 }, { "value": "RJ", "score": 4 }],
  "pontuacaoReferencia": 100
}
```
- Comportamento:
  - Faz merge por chave (value) em cada categoria enviada (overwrite do score para valores existentes; cria se não existir).
  - Não apaga valores omitidos; apenas atualiza/adiciona os informados.
  - O campo `pontuacaoReferencia` é atualizado se fornecido, caso contrário mantém o valor anterior.
- Retorno: `200 OK` com `BudgetScoringConfigResponseDTO` (todas as categorias consolidadas, incluindo `pontuacaoReferencia`).

### 2) Obter pontuações atuais
- Método: `GET /api/budgets/scoring`
- Retorno: `BudgetScoringConfigResponseDTO` (maps value→score por categoria, incluindo `pontuacaoReferencia`)

### 3) Listar campos disponíveis para pontuação
- Método: `GET /api/budgets/scoring/fields`
- Retorno: `Map<String, List<String>>` - Todos os campos disponíveis para receber pontuação, agrupados por categoria
- Exemplo de resposta:
```json
{
  "mercado": ["Público", "Privado"],
  "setor": ["Siderurgia e Metalurgica", "Mineração", "Papel e Celulose", ...],
  "tempoContrato": ["Menor que 12 meses", "Entre 12 meses e 24 meses", ...],
  "porteEstimado": ["Pequeno", "Médio", "Grande"],
  "modalidadeConcorrencia": ["Menor Preço", "Leilão", "Maior Desconto", "Outros"],
  "tipo": ["Rotina-Continuado", "Rotina-Sob Demanda", "Spot", ...],
  "caracteristica": ["Venda de Mão de Obra (Diária / Mensal)", ...],
  "estados": ["AC", "AL", "AP", "AM", ..., "TO"]
}
```

### 9) Criar OS
- Método: `POST /api/budgets/{budgetId}/os`
- Consumes: `multipart/form-data` ou `application/json`
- Body (CreateBudgetOSRequest):
  - **numeroOs** (String) [obrigatório]
  - **status** (String)
  - **anexoOs** (List<AttachmentEntity>)
  - **cnpjDueDiligence** (String)
  - **inscEstadual** (String)
  - **cpfduediligence** (String)

### 10) Listar OS do Orçamento
- Método: `GET /api/budgets/{budgetId}/os`

### 11) Obter OS por ID
- Método: `GET /api/budgets/{budgetId}/os/{osId}`

---

## 💬 Esclarecimentos

### 12) Criar Esclarecimento
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/esclarecimentos`
- Consumes: `multipart/form-data` ou `application/json`
- Body (CreateBudgetEsclarecimentoRequest):
  - **perguntas** (String)
  - **anexoEsclarecimento** (List<AttachmentEntity>)
  - **descricaoEsclarecimento** (String)
  - **dataHoraEsclarecimento** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelEsclarecimento** (String)

### 13) Listar Esclarecimentos da Versão
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/esclarecimentos`

### 14) Obter Esclarecimento por ID
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/esclarecimentos/{esclarecimentoId}`

---

## 📨 Circulares

### 15) Criar Circulares
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/circulares`
- Consumes: `multipart/form-data` ou `application/json`
- Body (CreateBudgetCircularesRequest):
  - **identificacaoCirculares** (String)
  - **anexoCirculares** (List<AttachmentEntity>)
  - **descricaoCirculares** (String)
  - **dataHoraCirculares** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **responsavelCirculares** (String)

### 16) Listar Circulares da Versão
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/circulares`

### 17) Obter Circulares por ID
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/circulares/{circularesId}`

---

## 📑 Premissas

### 18) Criar Premissa
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/premissas`
- Consumes: `multipart/form-data` ou `application/json`
- Body (CreateBudgetPremissaRequest):
  - **anexoTipo** (String)
  - **anexoPremissa** (List<AttachmentEntity>)

### 19) Listar Premissas da Versão
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/premissas`

### 20) Obter Premissa por ID
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/premissas/{premissaId}`

---

## 📄 Proposta

### 21) Criar Proposta Final
- Método: `POST /api/budgets/{budgetId}/versions/{versionId}/propostas`
- Consumes: `multipart/form-data` ou `application/json`
- Body (CreateBudgetPropostaRequest):
  - **dataHoraEntrega** (DateTime `yyyy-MM-dd'T'HH:mm:ss`)
  - **valorFinalTotal** (BigDecimal)
  - **comprovanteEntrega** (String)
  - **anexos** (Lista de objetos AnexoPropostaDTO) [se aplicável]
    - anexoTipo (String), anexoProposta (List<AttachmentEntity>)

### 22) Obter Proposta por Versão
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/propostas`

### 23) Obter Proposta por ID
- Método: `GET /api/budgets/{budgetId}/versions/{versionId}/propostas/{propostaId}`

---

## 🧾 Observações importantes
- Para campos de data/hora, respeite os formatos especificados (`yyyy-MM-dd` e `yyyy-MM-dd'T'HH:mm:ss`).
- **Endpoints com anexos (Comercial, Engenharia, SMS, Operações, Filtros, OS, Esclarecimentos, Circulares, Premissas, Propostas):**
  - Content-Type: `multipart/form-data`
  - Os anexos devem ser enviados como `AttachmentEntity` com metadados já processados, contendo:
    - `id` (String): Identificador único do anexo
    - `nome` (String): Nome original do arquivo
    - `file` (FileMetadata): Metadados do arquivo contendo:
      - `uri` (String): Caminho/URI do arquivo armazenado
      - `ext` (String): Extensão do arquivo
      - `mimeType` (String): Tipo MIME do arquivo
      - `etapa` (Integer): Etapa do processo (geralmente 1)
- **Endpoints sem anexos:**
  - Content-Type: `application/json`
  - Exemplo: `POST /api/budgets` (criação de orçamento)
- A autenticação é obrigatória em todos os endpoints do controlador.

---

## 🔁 Exemplo de fluxo mínimo (end-to-end)
1. `POST /api/budgets` → cria orçamento e obtém `budgetId`
2. `POST /api/budgets/{budgetId}/versions` → cria versão e obtém `versionId`
3. `PUT /api/budgets/{budgetId}/versions/{versionId}/filtro1` → aprova etapa 1
4. `PUT /api/budgets/{budgetId}/versions/{versionId}/filtro2` → aprova etapa 2
5. (Opcional) `POST /api/budgets/{budgetId}/os` → cria OS
6. (Opcional) `POST /api/budgets/{budgetId}/versions/{versionId}/esclarecimentos` → registra esclarecimento
7. (Opcional) `POST /api/budgets/{budgetId}/versions/{versionId}/circulares` → registra circulares
8. (Opcional) `POST /api/budgets/{budgetId}/versions/{versionId}/premissas` → registra premissas
9. `POST /api/budgets/{budgetId}/versions/{versionId}/propostas` → registra proposta final

---

### 🛠️ Tecnologias e Padrões
- Spring Boot 3 / Spring Web / Spring Security (JWT)
- Anexos tratados como `AttachmentEntity` com metadados já processados
- DTOs separados por contexto (criação, versão, OS, proposta etc.)

---

Qualquer dúvida sobre campos específicos ou payloads aninhados, consulte os DTOs no pacote `com.indux.modules.request_budgets.application.dto` e os serviços em `com.indux.modules.request_budgets.application.service`.

# Módulo de Request Budgets

Este módulo gerencia o cadastro e controle de orçamentos/oportunidades comerciais seguindo a arquitetura limpa.

## Estrutura

```
request_budgets/
├── application/
│   ├── dto/                    # Data Transfer Objects
│   ├── mapper/                 # Mapeadores de entidades
│   └── service/                # Serviços de aplicação
├── domain/
│   ├── model/                  # Entidades de domínio
│   └── repository/             # Interfaces de repositório
└── presentation/               # Controllers REST
```

## Entidades

### Budget
Entidade principal que representa um orçamento/oportunidade comercial.

**Collection MongoDB:** `budgets`

**Campos principais:**
- `id`: String (MongoDB ObjectId)
- `clienteId`: Long - ID do cliente no sistema
- `mercadoId`: Long - ID do mercado
- `setorId`: Long - ID do setor
- `solicitantes`: List<BudgetSolicitante> - Dados dos solicitantes (não aceito na criação, apenas no domínio)
- `nomeOportunidade`: String (único) - Identificador da oportunidade
- `descricaoOportunidade`: String - Descrição/escopo da oportunidade
- `tempoContrato`: String - Duração do contrato
- `porteEstimado`: BigDecimal - Valor estimado
- `dataAbertura`: LocalDate - Data de abertura da oportunidade
- `representanteComercialId`: UUID - ID do representante comercial
- `detalhesGerais`: String - Detalhes adicionais
- `documentos`: List<BudgetDocumento> - Documentos da oportunidade (não aceito na criação, apenas no domínio)
- `status`: String - Status do orçamento
- `dataAcompanhamento`: LocalDateTime - Data de acompanhamento
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime
- `createdBy`: UUID
- `updatedBy`: UUID

### BudgetSolicitante
Dados dos solicitantes (embedded no Budget).

**Campos:**
- `nome`: String
- `funcaoCargo`: String
- `telefone1`: String
- `telefone2`: String
- `email1`: String
- `email2`: String
- `localizacao`: String
- `observacao`: String

### BudgetDocumento
Documentos da oportunidade (embedded no Budget).

**Tipos de documentos:**
- MD (Memória de Diálogo)
- PPU
- SMS
- Gerais
- Habilitações necessárias

**Campos:**
- `tipo`: String - Tipo do documento
- `maisInformacoes`: String - Informações adicionais
- `descricao`: String - Descrição
- `anexos`: List<String> - URLs dos anexos

### BudgetTipo
Tipos de orçamento (collection separada).

**Collection MongoDB:** `budget_tipo`

**Campos:**
- `id`: String
- `nome`: String (único) - Nome do tipo
- `descricao`: String

**Valores exemplo:**
- Rotina-Continuado
- Rotina-Sob Demanda
- Spot
- Parada de Manutenção
- Guarda Chuva
- Outros

### BudgetCaracteristica
Características do orçamento (collection separada).

**Collection MongoDB:** `budget_caracteristica`

**Campos:**
- `id`: String
- `codigo`: String - Código da característica (A, B, C, D, E)
- `descricao`: String

**Valores exemplo:**
- A: Venda de Mão de Obra (Diária / Mensal)
- B: Fornecimento de serviços
- C: Revenda de Peças e Sobressalentes
- D: Facilities
- E: Outros

## Endpoints

### Budgets (Orçamentos)

#### Criar Budget
```http
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "clienteId": 1,
  "mercadoId": 1,
  "setorId": 1,
  "solicitanteNome": "João Silva",
  "solicitanteFuncaoCargo": "Gerente",
  "solicitanteTelefone1": "11999999999",
  "solicitanteEmail1": "joao@email.com",
  "solicitanteLocalizacao": "São Paulo",
  "solicitanteObservacao": "Contato principal",
  "nomeOportunidade": "OPP-2024-001",
  "descricaoOportunidade": "Fornecimento de serviços de manutenção",
  "tempoContrato": "12 meses",
  "porteEstimado": 500000.00,
  "dataAbertura": "2024-01-15",
  "representanteComercialId": "550e8400-e29b-41d4-a716-446655440000",
  "detalhesGerais": "Detalhes adicionais da oportunidade",
  "status": "ABERTO"
}
```

**Resposta (201):**
```json
{
  "message": "Orçamento criado com sucesso",
  "status": 201,
  "budgetId": "507f1f77bcf86cd799439014",
  "nomeOportunidade": "OPP-2024-001"
}
```

#### Listar Todos os Budgets
```http
GET /budgets
Authorization: Bearer {token}
```

#### Buscar Budget por ID
```http
GET /budgets/{id}
Authorization: Bearer {token}
```

### Tipos de Budget

#### Criar Tipo
```http
POST /budgets/tipos
Authorization: Bearer {token}
Content-Type: application/json

{
  "nome": "Rotina-Continuado",
  "descricao": "Serviços de rotina com execução continuada"
}
```

#### Listar Tipos
```http
GET /budgets/tipos
Authorization: Bearer {token}
```

#### Buscar Tipo por ID
```http
GET /budgets/tipos/{id}
Authorization: Bearer {token}
```

#### Remover Tipo
```http
DELETE /budgets/tipos/{id}
Authorization: Bearer {token}
```

### Características

#### Criar Característica
```http
POST /budgets/caracteristicas
Authorization: Bearer {token}
Content-Type: application/json

{
  "codigo": "A",
  "descricao": "Venda de Mão de Obra (Diária / Mensal)"
}
```

#### Listar Características
```http
GET /budgets/caracteristicas
Authorization: Bearer {token}
```

#### Buscar Característica por ID
```http
GET /budgets/caracteristicas/{id}
Authorization: Bearer {token}
```

#### Remover Característica
```http
DELETE /budgets/caracteristicas/{id}
Authorization: Bearer {token}
```

## Validações

### CreateBudgetRequest
- `clienteId`: Obrigatório
- `mercadoId`: Obrigatório
- `setorId`: Obrigatório
- `nomeOportunidade`: Obrigatório e único
- `representanteComercialId`: Obrigatório

## Estrutura de Dados Relacionadas

### Modalidades de Concorrência
- Menor Preço
- Leilão
- Maior Desconto
- Outros

### Tipos de Documento
- **MD**: Memória de Diálogo
- **PPU**: Documentos PPU
- **SMS**: Documentos SMS
- **Gerais**: Documentos gerais
- **Habilitações necessárias**: Documentos de habilitação

## Observações

1. O módulo utiliza MongoDB como banco de dados
2. As entidades `BudgetSolicitante` e `BudgetDocumento` são embedded no documento principal `Budget`, mas não são aceitas na criação de orçamentos (apenas campos individuais do solicitante)
3. `BudgetTipo` é referenciado via DBRef
4. As características são armazenadas como lista de IDs para melhor performance
5. Todos os endpoints requerem autenticação via JWT
6. Os timestamps (createdAt/updatedAt) são gerenciados automaticamente pelo serviço
7. O `nomeOportunidade` possui índice único no MongoDB
8. O endpoint de criação de orçamento (`POST /api/budgets`) aceita apenas `application/json`

## Fluxo de Cadastro

1. Cadastrar Tipos de Budget (se necessário)
2. Cadastrar Características (se necessário)
3. Criar Budget com todas as informações:
   - Dados básicos (cliente, mercado, setor)
   - Dados do solicitante (campos individuais)
   - Informações da oportunidade
   - Detalhes gerais

## Integração com Outros Módulos

O módulo de request_budgets utiliza dados de:
- **Clientes**: clienteId deve existir no cadastro de clientes
- **Mercado**: mercadoId deve existir na tabela de mercado
- **Setor**: setorId deve existir na tabela de setor
- **Usuários**: representanteComercialId deve ser um usuário válido

