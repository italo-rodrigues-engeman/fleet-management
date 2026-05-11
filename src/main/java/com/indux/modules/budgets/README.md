# Módulo Budgets (Simple)

Este módulo gerencia o cadastro e controle de orçamentos simples seguindo a arquitetura limpa.

**Nota:** Este módulo é diferente do módulo `request_budgets` que possui funcionalidades mais complexas como versionamento, filtros, etc.

## Estrutura

```
budgets/
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

### SimpleBudget
Entidade principal que representa um orçamento simples.

**Collection MongoDB:** `simple_budgets`

**Campos principais:**
- `id`: String (MongoDB ObjectId)
- `clienteId`: Long - ID do cliente no sistema
- `mercadoId`: Long - ID do mercado
- `setorId`: Long - ID do setor
- `solicitanteNome`: String - Nome do solicitante
- `solicitanteFuncaoCargo`: String - Função/cargo do solicitante
- `solicitanteTelefone1`: String - Telefone principal
- `solicitanteTelefone2`: String - Telefone secundário
- `solicitanteEmail1`: String - Email principal
- `solicitanteEmail2`: String - Email secundário
- `solicitanteLocalizacao`: String - Localização do solicitante
- `solicitanteObservacao`: String - Observações sobre o solicitante
- `nomeOportunidade`: String (único) - Identificador da oportunidade
- `descricaoOportunidade`: String - Descrição/escopo da oportunidade
- `tempoContrato`: String - Duração do contrato
- `porteEstimado`: BigDecimal - Valor estimado
- `dataAbertura`: LocalDate - Data de abertura da oportunidade
- `representanteComercialId`: UUID - ID do representante comercial
- `detalhesGerais`: String - Detalhes adicionais
- `status`: String - Status do orçamento
- `dataAcompanhamento`: LocalDateTime - Data de acompanhamento
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime
- `createdBy`: UUID
- `updatedBy`: UUID

## Endpoints

### Criar Budget
```http
POST /api/simple-budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "clienteId": 1,
  "clienteNome": "Empresa ABC",
  "setorId": 1,
  "acOs": "AC123",
  "setor": "Suprimentos",
  "orcamentista": "João Silva",
  "oportunidade": 1,
  "nomeOportunidade": "OPP-2024-001"
}
```

**Resposta (201):**
```json
{
  "message": "Orçamento criado com sucesso",
  "status": 201,
  "budgetId": "507f1f77bcf86cd799439014",
  "nomeOportunidade": "OPP-2024-001",
  "budgetStatus": "ABERTO"
}
```

**Nota:** O campo `status` do orçamento é sempre definido automaticamente como "ABERTO" na criação.

### Listar Todos os Budgets
```http
GET /api/simple-budgets
Authorization: Bearer {token}
```

### Buscar Budget por ID
```http
GET /api/simple-budgets/{id}
Authorization: Bearer {token}
```

### Aprovar Budget
```http
PATCH /api/simple-budgets/{id}/aprovar
Authorization: Bearer {token}
Content-Type: application/json

{
  "observation": "Orçamento aprovado pelo cliente após revisão técnica."
}
```

**Resposta (200):** Retorna o `SimpleBudgetResponseDTO` com status atualizado para "APROVADO" e o log de aprovação incluído.

## Validações

### CreateSimpleBudgetRequest
- `clienteId`: Obrigatório
- `nomeOportunidade`: Obrigatório e único
- `status`: Definido automaticamente como "ABERTO" (não aceito no request)

## Observações

1. O módulo utiliza MongoDB como banco de dados
2. O `nomeOportunidade` possui índice único no MongoDB
3. Todos os endpoints requerem autenticação via JWT
4. Os timestamps (createdAt/updatedAt) são gerenciados automaticamente pelo serviço
5. O endpoint de criação de orçamento (`POST /api/simple-budgets`) aceita apenas `application/json`
6. O campo `status` é sempre definido como "ABERTO" automaticamente na criação de um novo orçamento

## Integração com Outros Módulos

O módulo de budgets utiliza dados de:
- **Clientes**: clienteId deve existir no cadastro de clientes
- **Setor**: setorId pode ser usado para categorização (opcional)

