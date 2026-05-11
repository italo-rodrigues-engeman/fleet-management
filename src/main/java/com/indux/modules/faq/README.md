# 📚 Módulo FAQ - Sistema de Perguntas e Respostas

## 📋 Visão Geral

O módulo FAQ (Frequently Asked Questions) é um sistema completo para gerenciamento de perguntas e respostas organizadas hierarquicamente por setores, temas e perguntas. Permite criar, consultar, atualizar e excluir informações de forma estruturada.

## 🏗️ Arquitetura

### Estrutura de Dados
```
Setor → Tema → Pergunta → Resposta
```

### Hierarquia
- **Setor**: Área da empresa (RH, Financeiro, TI, etc.)
- **Tema**: Categoria dentro do setor (Benefícios, Folha de Pagamento, etc.)
- **Pergunta**: Questão específica do tema
- **Resposta**: Solução para a pergunta (vinculada a contratos)

## 🗄️ Banco de Dados

### Tabelas

#### `tb_setor`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGSERIAL | Chave primária |
| `nome` | VARCHAR(100) | Nome do setor |
| `descricao` | TEXT | Descrição do setor |
| `status` | BOOLEAN | Status ativo/inativo |

#### `tb_tema`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGSERIAL | Chave primária |
| `nome` | VARCHAR(100) | Nome do tema |
| `descricao` | TEXT | Descrição do tema |
| `status` | BOOLEAN | Status ativo/inativo |
| `setor_id` | BIGINT | FK para tb_setor |

#### `tb_pergunta`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGSERIAL | Chave primária |
| `nome` | VARCHAR(500) | Texto da pergunta |
| `tema_id` | BIGINT | FK para tb_tema |

#### `tb_resposta`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGSERIAL | Chave primária |
| `nome` | VARCHAR(1000) | Texto da resposta |
| `pergunta_id` | BIGINT | FK para tb_pergunta |
| `contrato` | INTEGER | Contrato vinculado |
| `status` | BOOLEAN | Status ativo/inativo |

#### `tb_sequence_faq_pergunta`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGSERIAL | Código sequencial gerado no Postgres |
| `document_id` | VARCHAR(100) | ID do documento salvo no MongoDB |

### Índices
- `idx_tema_setor_id` - Performance em consultas por setor
- `idx_pergunta_tema_id` - Performance em consultas por tema
- `idx_resposta_pergunta_id` - Performance em consultas por pergunta
- `idx_resposta_contrato` - Performance em consultas por contrato
- `idx_resposta_status` - Performance em consultas por status

## 🎯 Funcionalidades

### ✅ CRUD Completo
- **Create**: Criar setores, temas, perguntas e respostas
- **Read**: Consultar com filtros e relacionamentos
- **Update**: Atualizar informações existentes
- **Delete**: Excluir registros (com cascade)

### ✅ Validações
- Verificação de existência antes de criar
- Validação de relacionamentos
- Controle de duplicatas por nome

### ✅ Relacionamentos
- Hierarquia completa: Setor → Tema → Pergunta → Resposta
- Cascade delete para manter integridade
- Consultas com dados relacionados

## 📡 Endpoints da API

### 🏢 Setores

#### Criar Setor
```http
POST /api/faq/setores
Content-Type: application/json

{
    "nome": "Recursos Humanos",
    "descricao": "Assuntos relacionados ao RH",
    "status": true
}
```

#### Listar Todos os Setores
```http
GET /api/faq/setores
```

#### Buscar Setor por ID
```http
GET /api/faq/setores/{id}
```

#### Atualizar Setor
```http
PUT /api/faq/setores/{id}
Content-Type: application/json

{
    "nome": "Recursos Humanos",
    "descricao": "Assuntos corporativos de RH",
    "status": false
}
```

#### Excluir Setor
```http
DELETE /api/faq/setores/{id}
```

### 📋 Temas

#### Criar Tema
```http
POST /api/faq/temas
Content-Type: application/json

{
    "nome": "Benefícios",
    "descricao": "Benefícios corporativos",
    "status": true,
    "setorId": 1
}
```

#### Listar Todos os Temas
```http
GET /api/faq/temas
```

#### Listar Temas por Setor
```http
GET /api/faq/temas/setor/{setorId}
```

#### Buscar Tema por ID
```http
GET /api/faq/temas/{id}
```

#### Atualizar Tema
```http
PUT /api/faq/temas/{id}
Content-Type: application/json

{
    "nome": "Benefícios Corporativos",
    "descricao": "Temas de benefícios e auxílios",
    "status": true,
    "setorId": 1
}
```

#### Excluir Tema
```http
DELETE /api/faq/temas/{id}
```

### ❓ Perguntas (persistência em MongoDB com sequência em Postgres)

As perguntas passaram a ser armazenadas no MongoDB (coleção `faq_perguntas`), mas com um código sequencial gerado no Postgres através da tabela `tb_sequence_faq_pergunta`. A cada criação:
- É gerado um registro em `tb_sequence_faq_pergunta` (incrementando o `id`)
- A pergunta é salva no Mongo com `codigoSequencial = id`
- O `document_id` do Mongo é gravado de volta em `tb_sequence_faq_pergunta`

#### Estrutura da Pergunta (Mongo)
- `codigoSequencial` (Long)
- `setorId` (Long)
- `temaId` (Long)
- `categoria` (String)
- `tipo` (String)
- `dataCriacao` (ISO datetime)
- `titulo` (String)
- `regionalId` (Long)
- `contratoId` (Long)
- `publico` (String)
- `aprovador` (String)
- `observacoes` (String)
- `anexos` (Array[String])
- `respostas` (Array[Objeto]) com campos:
  - `conteudo` (String)
  - `regional` (Long)
  - `contrato` (Long)
  - `publico` (String)
  - `anexo` (Array[String])

#### Criar Pergunta
Endpoint aceita multipart/form-data (com anexos). Exemplo (curl):
```bash
curl -X POST \
  "https://seu-servidor/api/faq/perguntas" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "setorId=2" \
  -F "temaId=10" \
  -F "categoria=Benefícios" \
  -F "tipo=Informativo" \
  -F "dataCriacao=2025-08-14T13:45:00" \
  -F "titulo=Como solicitar vale refeição?" \
  -F "regionalId=5" \
  -F "contratoId=94" \
  -F "publico=SIM" \
  -F "aprovador=gestor.rh@empresa.com" \
  -F "observacoes=Cadastro inicial via portal" \
  -F "anexos=@/caminho/arquivo1.pdf" \
  -F "anexos=@/caminho/manual_vr.png" \
  -F "respostas[0].conteudo=Abrir chamado no portal (RH → VR)." \
  -F "respostas[0].regional=5" \
  -F "respostas[0].contrato=94" \
  -F "respostas[0].publico=SIM" \
  -F "respostas[0].anexo=@/caminho/passo_a_passo.pdf"
```

#### Listar Todas as Perguntas
```http
GET /api/faq/perguntas
```

#### Listar Perguntas por Tema
```http
GET /api/faq/perguntas/tema/{temaId}
```

#### Buscar Pergunta por ID
```http
GET /api/faq/perguntas/{id}
```


#### Buscar Pergunta (detalhe)
```http
GET /api/faq/perguntas/{id}
```
Onde `{id}` é o código sequencial (Postgres). Retorna o documento completo da pergunta (Mongo), incluindo `respostas`, `status` e `stepLog`.

#### Listar Perguntas (resumo com paginação)
```http
GET /api/faq/perguntas?page=0&size=10&sort=titulo,asc
```
Retorna `Page<PerguntaResumoDTO>` com os campos:
- **nome_setor**: resolvido a partir de `setorId` (tabela `tb_setor`)
- **nome_tema**: resolvido a partir de `temaId` (tabela `tb_tema`)
- **titulo**
- **total_respostas**
- **status** (ex.: PENDENTE)
- **categoria**

Exemplo de item:
```json
{
  "nome_setor": "Recursos Humanos",
  "nome_tema": "Benefícios",
  "titulo": "Como solicitar vale refeição?",
  "total_respostas": 2,
  "status": "PENDENTE",
  "categoria": "Informativo"
}
```

### Modelo de Dados da Pergunta (Mongo)
- codigoSequencial (Long)
- setorId (Long)
- temaId (Long)
- categoria (String)
- tipo (String)
- dataCriacao (ISO datetime)
- titulo (String)
- regionalId (Long)
- contratoId (Long)
- publico (String)
- aprovador (String)
- observacoes (String)
- anexos (Array[String])
- respostas (Array[Objeto])
  - conteudo (String)
  - regional (Long)
  - contrato (Long)
  - publico (String)
  - anexo (Array[String])
- status (String) — inicial: "PENDENTE"
- stepLog (Array[Objeto]) — logs de etapas (id, name, step, created_at, observation)

#### Excluir Pergunta
```http
DELETE /api/faq/perguntas/{id}
```

### 💬 Respostas

#### Criar Resposta
```http
POST /api/faq/respostas
Content-Type: application/json

{
    "nome": "Entre em contato com o RH através do email rh@empresa.com",
    "perguntaId": 1,
    "contrato": 1,
    "status": true
}
```

## ✏️ Edição de Perguntas com Múltiplos Anexos

### 🔄 Fluxo de Revisão e Edição

O sistema FAQ permite editar perguntas e suas respostas durante o processo de revisão, incluindo controle total sobre anexos (adicionar, remover, atualizar).

#### **1. 🚀 Enviar para Revisão**
```http
POST /api/faq/perguntas/{codigoSequencial}/revisao
Content-Type: multipart/form-data

motivoRevisao: Atualização dos procedimentos para 2024
```

#### **2. ✏️ Editar Durante Revisão**
```http
PUT /api/faq/perguntas/{codigoSequencial}/reviewed?edit=true
Content-Type: multipart/form-data
```

#### **3. ✅ Finalizar Revisão**
```http
PUT /api/faq/perguntas/{codigoSequencial}/reviewed?edit=false
Content-Type: multipart/form-data
```

### 🎯 Controle de Anexos

#### **Anexos da Pergunta Principal:**

##### **🗑️ Remover Anexos Específicos:**
```
anexosRemover: 123e4567-e89b-12d3-a456-426614174000
anexosRemover: 987fc654-3210-9876-5432-109876543210
```

##### **➕ Adicionar Novos Anexos:**
```
anexos[0].nome: Manual_Atualizado_2024
anexos[0].file: [arquivo PDF]

anexos[1].nome: Video_Tutorial
anexos[1].file: [arquivo MP4]
```

#### **Anexos das Respostas:**

##### **🔄 Atualizar Resposta com Novos Anexos:**
```
respostas[0].conteudo: Conteúdo atualizado da resposta...
respostas[0].regional: 3
respostas[0].contratos: 162
respostas[0].contratos: 163
respostas[0].publico: INTERNO

# Anexos existentes + novos
respostas[0].anexo[0].nome: Tutorial_Interativo
respostas[0].anexo[0].file: [arquivo HTML]

respostas[0].anexo[1].nome: Checklist_Interativo
respostas[0].anexo[1].file: [arquivo HTML novo]
```

##### **🗑️ Remover Anexos das Respostas:**
Para remover anexos específicos das respostas, use o campo `anexosRemover`:

```
# Resposta 1 - Remover anexos específicos por ID
respostas[0].conteudo: Para solicitar reembolso, acesse o sistema de RH...
respostas[0].regional: 3
respostas[0].contratos: 162,163
respostas[0].publico: INTERNO

# Remover anexos específicos
respostas[0].anexosRemover: 123e4567-e89b-12d3-a456-426614174000
respostas[0].anexosRemover: 987fc654-3210-9876-5432-109876543210

# Adicionar novos anexos (opcional)
respostas[0].anexo[0].nome: Novo_Manual_2024
respostas[0].anexo[0].file: [arquivo PDF]
```

**⚠️ IMPORTANTE**: O campo `anexosRemover` aceita uma lista de IDs dos anexos a serem removidos.

**⚠️ IMPORTANTE - Comportamento dos Anexos das Respostas:**
- **Se enviar `respostas[0].anexo`**: **ADICIONA** aos anexos existentes (não substitui)
- **Se NÃO enviar `respostas[0].anexo`**: **PRESERVA** anexos existentes
- **Se enviar `respostas[0].anexosRemover`**: **REMOVE** anexos específicos por ID
- **Para substituir completamente**: Envie todos os anexos desejados
- **Para adicionar apenas**: Envie apenas os novos anexos
- **Para remover específicos**: Use `anexosRemover` com IDs dos anexos

### 🔄 **Como Funciona o Sistema de Anexos das Respostas:**

**📋 Campos Disponíveis para Respostas:**
- `respostas[0].anexo` - **Adiciona** novos anexos aos existentes
- `respostas[0].anexosRemover` - **Remove** anexos específicos por ID
- `respostas[0].contratos` - **Atualiza** contratos da resposta
- `respostas[0].conteudo` - **Atualiza** conteúdo da resposta
- `respostas[0].regional` - **Atualiza** regional da resposta
- `respostas[0].publico` - **Atualiza** público da resposta

#### **Cenário 1: Adicionar Novo Anexo**
```
# Resposta existente tem 2 anexos: [Anexo1.pdf, Anexo2.pdf]
# Você envia apenas 1 novo anexo
respostas[0].anexo[0].nome: Novo_Anexo
respostas[0].anexo[0].file: [arquivo]

# Resultado: [Anexo1.pdf, Anexo2.pdf, Novo_Anexo] ✅
```

#### **Cenário 2: Preservar Anexos Existentes**
```
# Resposta existente tem 2 anexos: [Anexo1.pdf, Anexo2.pdf]
# Você NÃO envia o campo anexo

# Resultado: [Anexo1.pdf, Anexo2.pdf] ✅ (preservados)
```

#### **Cenário 3: Substituir Todos os Anexos**
```
# Resposta existente tem 2 anexos: [Anexo1.pdf, Anexo2.pdf]
# Você envia 3 novos anexos
respostas[0].anexo[0].nome: Novo1
respostas[0].anexo[0].file: [arquivo1]
respostas[0].anexo[1].nome: Novo2
respostas[0].anexo[1].file: [arquivo2]
respostas[0].anexo[2].nome: Novo3
respostas[0].anexo[2].file: [arquivo3]

# Resultado: [Anexo1.pdf, Anexo2.pdf, Novo1, Novo2, Novo3] ✅
```

#### **Cenário 4: Remover Anexos Específicos + Adicionar Novos**
```
# Resposta existente tem 3 anexos: [Anexo1.pdf, Anexo2.pdf, Anexo3.pdf]
# Você quer remover Anexo2.pdf e adicionar 1 novo

# Remover anexo específico
respostas[0].anexosRemover: id_anexo2

# Adicionar novo anexo
respostas[0].anexo[0].nome: Novo_Anexo
respostas[0].anexo[0].file: [arquivo]

# Resultado: [Anexo1.pdf, Anexo3.pdf, Novo_Anexo] ✅
```

### 📝 Exemplo Completo de Edição

#### **FormData para Editar Anexos das Respostas:**

```
# Campos básicos (opcionais - só atualiza se enviado)
setorId: 5
temaId: 12
categoria: TÉCNICO
tipo: PROCEDIMENTO
titulo: Como solicitar reembolso de despesas? (ATUALIZADO)
observacoes: Procedimento atualizado para solicitação de reembolso

# Anexos da pergunta principal
anexosRemover: 123e4567-e89b-12d3-a456-426614174000
anexosRemover: 987fc654-3210-9876-5432-109876543210

anexos[0].nome: Procedimento_Reembolso_2024
anexos[0].file: [arquivo PDF atualizado]

anexos[1].nome: Checklist_Documentos_2024
anexos[1].file: [arquivo Excel atualizado]

# Respostas com anexos
respostas[0].conteudo: Para solicitar reembolso, acesse o sistema de RH atualizado...
respostas[0].regional: 3
respostas[0].contratos: 162,163
respostas[0].publico: INTERNO

# Remover anexo antigo da resposta 1
respostas[0].anexosRemover: id_anexo_antigo_123

# Adicionar novos anexos à resposta 1
respostas[0].anexo[0].nome: Fluxo_Processo_2024
respostas[0].anexo[0].file: [arquivo PNG atualizado]
respostas[0].anexo[1].nome: Exemplo_Preenchimento_2024
respostas[0].anexo[1].file: [arquivo PDF atualizado]

respostas[1].conteudo: Documentos necessários atualizados...
respostas[1].regional: 3
respostas[1].contratos: 164
respostas[1].publico: INTERNO

# Remover anexo antigo da resposta 2
respostas[1].anexosRemover: id_anexo_antigo_456

# Adicionar novo anexo à resposta 2
respostas[1].anexo[0].nome: Lista_Documentos_2024
respostas[1].anexo[0].file: [arquivo PDF atualizado]

respostas[2].conteudo: Prazo para reembolso atualizado...
respostas[2].regional: 3
respostas[2].contratos: 162
respostas[2].publico: INTERNO

# Remover anexo antigo da resposta 3
respostas[2].anexosRemover: id_anexo_antigo_789

# Adicionar novo anexo à resposta 3
respostas[2].anexo[0].nome: Politica_Reembolso_2024
respostas[2].anexo[0].file: [arquivo PDF atualizado]

motivoRevisao: Atualização dos procedimentos de reembolso para 2024
```

### ⚠️ **Exemplo de Como NÃO Enviar Contratos (Evitar):**

```
# ❌ ERRADO - Isso NÃO limpará os contratos, mas também NÃO os atualizará!
contratos: []

# ❌ ERRADO - Isso NÃO limpará os contratos, mas também NÃO os atualizará!
respostas[0].contratos: []

# ⚠️ ATENÇÃO: Listas vazias são IGNORADAS pelo sistema
# Para limpar contratos, você deve enviar uma lista vazia explicitamente
# ou não enviar o campo

### 🔄 **Como Limpar Contratos (Se Necessário):**
```
# ✅ CORRETO - Para limpar TODOS os contratos da pergunta
contratos: 

# ✅ CORRETO - Para limpar TODOS os contratos de uma resposta
respostas[0].contratos: 

# ⚠️ NOTA: Deixar o campo vazio (sem valor) limpa os contratos
# Enviar "contratos: []" (array vazio) NÃO limpa - apenas ignora
```
```

### ✅ **Exemplo de Como Enviar Contratos Corretamente:**

#### **Opção 1: Campo Único com Vírgulas (RECOMENDADO)**
```
# ✅ CORRETO - Todos os contratos em um campo
contratos: 162,163,164

# ✅ CORRETO - Resposta com contratos específicos
respostas[0].contratos: 162,163
respostas[1].contratos: 164
```

#### **Opção 2: Campos Separados**
```
# ✅ CORRETO - Cada contrato em campo separado
contratos: 162
contratos: 163
contratos: 164

# ✅ CORRETO - Resposta com contratos específicos
respostas[0].contratos: 162
respostas[0].contratos: 163
respostas[1].contratos: 164
```

### 🔧 Configuração no Postman

#### **Headers:**
```
Authorization: Bearer {{jwt_token}}
Content-Type: multipart/form-data
```

#### **Body (form-data):**
- **Key**: `setorId`
- **Value**: `5`
- **Type**: `Text`

- **Key**: `anexos[0].file`
- **Value**: `[Selecionar arquivo]`
- **Type**: `File`

- **Key**: `anexosRemover`
- **Value**: `123e4567-e89b-12d3-a456-426614174000`
- **Type**: `Text`

### 💡 Dicas Importantes

#### **1. ✅ Regras de Revisão:**
- **Perguntas aprovadas**: ✅ Podem ser enviadas para revisão novamente
- **Perguntas em revisão**: ✅ Podem ser editadas durante revisão
- **Perguntas pendentes**: ✅ Podem ser enviadas para revisão
- **Limitação**: ❌ Não é possível voltar além da primeira etapa (exceto perguntas aprovadas)

#### **2. ✅ Anexos das Respostas:**
- **Para manter**: Inclua todos os anexos que deseja manter
- **Para remover**: Não inclua na lista
- **Para adicionar**: Adicione ao final da lista
- **⚠️ IMPORTANTE**: Anexos são **ADICIONADOS** aos existentes, não substituídos
- **Para substituir completamente**: Envie todos os anexos desejados em uma única requisição

#### **3. ✅ Contratos (Importante):**
- **Para manter**: Inclua todos os contratos que deseja manter
- **Para remover**: Inclua apenas os contratos que deseja manter (não envie array vazio)
- **Para adicionar**: Adicione aos contratos existentes
- **⚠️ Cuidado**: Se enviar `contratos: []` (array vazio), os contratos NÃO serão atualizados (preservados)

**Formas de Enviar Contratos:**
- **Opção 1 (Recomendado)**: `contratos: 162,163,164` (um campo com vírgulas)
- **Opção 2**: `contratos: 162`, `contratos: 163`, `contratos: 164` (campos separados)
- **Ambas funcionam** perfeitamente com o sistema

**Comportamento de Listas Vazias:**
- **`contratos: []`** → **NÃO atualiza** (preserva contratos existentes)
- **`contratos: 162,163`** → **Atualiza** para os contratos especificados
- **Campo não enviado** → **NÃO atualiza** (preserva contratos existentes)

#### **4. ✅ Anexos da Pergunta Principal:**
- **Para remover**: Use `anexosRemover` com IDs
- **Para adicionar**: Use `anexos` com novos arquivos

#### **5. ✅ Comportamento:**
- **`edit=true`**: Atualiza e mantém em revisão
- **`edit=false`**: Atualiza e envia para aprovação

#### **6. ✅ Validações:**
- Só edita perguntas em status "REVISAO"
- Mantém histórico completo no `stepLog`
- Gera IDs únicos para novos anexos

#### **7. ✅ Fluxo de Trabalho:**
```
1. Pergunta criada → Status: "PENDENTE"
2. Enviar para revisão → Status: "REVISAO"
3. Editar durante revisão → Status: "REVISAO" (mantém)
4. Finalizar revisão → Status: "PENDENTE" (volta para aprovação)
5. Aprovar → Status: "APROVADO"
6. ✅ Enviar para revisão novamente → Status: "REVISAO" (permitido)
```

### 🎯 Casos de Uso Comuns

#### **Atualização de Procedimentos:**
- Editar conteúdo das respostas
- Substituir anexos desatualizados
- Adicionar novos materiais de apoio

#### **Correção de Informações:**
- Corrigir dados incorretos
- Remover anexos obsoletos
- Atualizar contratos vinculados

#### **Expansão de Conteúdo:**
- Adicionar novas respostas
- Incluir mais anexos explicativos
- Vincular a novos contratos

## �� Respostas da API

### ✅ Sucesso
```json
{
    "message": "Setor criado com sucesso.",
    "status": 201
}
```

### ❌ Erro
```json
{
    "message": "Setor já existe com este nome.",
    "status": 400
}
```

### 📋 Dados
```json
[
    {
        "id": 1,
        "nome": "Recursos Humanos",
        "status": true
    }
]
```

## 🗂️ Estrutura de Arquivos

```
src/main/java/com/indux/modules/faq/
├── domain/
│   ├── entities/
│   │   ├── Setor.java
│   │   ├── Tema.java
│   │   ├── Pergunta.java
│   │   └── Resposta.java
│   └── repository/
│       ├── SetorRepository.java
│       ├── TemaRepository.java
│       ├── PerguntaRepository.java
│       └── RespostaRepository.java
├── application/
│   ├── dto/
│   │   ├── SetorDTO.java
│   │   ├── TemaDTO.java
│   │   ├── PerguntaDTO.java
│   │   ├── RespostaDTO.java
│   │   ├── CreateSetorDTO.java
│   │   ├── CreateTemaDTO.java
│   │   ├── CreatePerguntaDTO.java
│   │   └── CreateRespostaDTO.java
│   └── service/
│       └── FAQService.java
└── presentation/
    └── FAQController.java
```

## 🗄️ Script de Migração

### Arquivo: `V1_30__CREATE_FAQ_TABLES.sql`

Contém:
- Criação das 4 tabelas principais
- Definição de chaves estrangeiras
- Criação de índices para performance
- Dados de exemplo para teste

## 🚀 Como Usar

### 1. Executar Migração
O script de migração será executado automaticamente pelo Flyway na inicialização da aplicação.

### 2. Testar Endpoints
Use os endpoints listados acima para:
- Criar setores, temas, perguntas e respostas
- Consultar dados com filtros
- Atualizar informações
- Excluir registros

### 3. Exemplo de Fluxo
1. **Criar Setor**: `POST /api/faq/setores`
2. **Criar Tema**: `POST /api/faq/temas` (vinculado ao setor)
3. **Criar Pergunta**: `POST /api/faq/perguntas` (vinculada ao tema)
4. **Criar Resposta**: `POST /api/faq/respostas` (vinculada à pergunta)

## 🔧 Configurações

### Dependências
- Spring Boot 3.4.4
- Spring Data JPA
- PostgreSQL
- Flyway (migrações)

### Anotações Utilizadas
- `@Entity` - Mapeamento JPA
- `@Repository` - Repositórios Spring Data
- `@Service` - Camada de serviço
- `@RestController` - Controllers REST
- `@Transactional` - Controle de transações

## 📈 Performance

### Otimizações
- **Índices**: Criados para consultas frequentes
- **Lazy Loading**: Relacionamentos carregados sob demanda
- **Cascade**: Delete em cascata para manter integridade
- **DTOs**: Separação entre entidades e resposta da API

### Consultas Otimizadas
- Busca por setor com temas relacionados
- Busca por tema com perguntas relacionadas
- Busca por pergunta com respostas relacionadas
- Filtros por contrato e status

## 🛡️ Segurança

### Validações
- Verificação de existência antes de criar
- Validação de relacionamentos obrigatórios
- Controle de duplicatas por nome
- Status de ativo/inativo

### Integridade
- Chaves estrangeiras com CASCADE DELETE
- Transações para operações complexas
- Rollback automático em caso de erro

## 🔄 Fluxo de Dados

```
1. Request HTTP → Controller
2. Controller → Service
3. Service → Repository
4. Repository → Database
5. Database → Repository → Service → Controller → Response
```

## 📝 Logs e Monitoramento

### Logs de Operação
- Criação, atualização e exclusão de registros
- Validações e erros
- Performance de consultas

### Métricas
- Tempo de resposta dos endpoints
- Quantidade de registros por entidade
- Uso de memória e CPU

## 🧪 Testes

### Cenários de Teste
1. **CRUD Completo**: Criar, ler, atualizar e excluir cada entidade
2. **Relacionamentos**: Verificar integridade entre entidades
3. **Validações**: Testar regras de negócio
4. **Performance**: Consultas com grandes volumes de dados

### Dados de Teste
O script de migração inclui dados de exemplo para:
- 4 setores (RH, Financeiro, TI, Operacional)
- 8 temas distribuídos pelos setores
- 6 perguntas com respostas
- Respostas vinculadas ao contrato 1

## 🔮 Melhorias Futuras

### Funcionalidades Planejadas
- [ ] Busca por texto (full-text search)
- [ ] Paginação para listagens grandes
- [ ] Cache para consultas frequentes
- [ ] Versionamento de respostas
- [ ] Sistema de tags/categorias
- [ ] Relatórios de uso
- [ ] API de busca avançada

### Otimizações Técnicas
- [ ] Índices de texto completo
- [ ] Cache Redis
- [ ] Compressão de respostas
- [ ] API GraphQL
- [ ] Documentação Swagger

## 📞 Suporte

Para dúvidas ou problemas:
1. Verificar logs da aplicação
2. Consultar documentação da API
3. Testar endpoints individualmente
4. Verificar integridade do banco de dados

---

**Desenvolvido para o sistema Indux**  
**Versão**: 1.0  
**Data**: 2024 