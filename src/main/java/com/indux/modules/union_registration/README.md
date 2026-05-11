
# Módulo Union Registration

Este módulo é responsável pelo cadastro e gerenciamento de sindicatos no sistema Indux.

## 🔄 Mudanças Recentes - Versão 1.0.34

### ✅ Sistema de Auditoria com StepLog

**Data:** 08/10/2025

**Alterações:**

#### **Campos de Auditoria Adicionados:**
- ✅ **Union (Sindicatos)**
  - `stepLog` (List<StepLog>) - Histórico completo de ações
  - `dataUltimaAtualizacao` (LocalDateTime) - Data da última modificação
  - `usuarioUltimaAtualizacao` (String) - ID/nome do usuário que fez a última atualização

- ✅ **LaborContract (ACT/CCT)**
  - `stepLog` (List<StepLog>) - Histórico completo de ações
  - `dataUltimaAtualizacao` (LocalDateTime) - Data da última modificação
  - `usuarioUltimaAtualizacao` (String) - ID/nome do usuário que fez a última atualização
  - `apelido` (String) - Apelido/nome curto para ACT/CCT

- ✅ **LaborContractAddendum (Aditivos)**
  - `stepLog` (List<StepLog>) - Histórico completo de ações
  - `dataUltimaAtualizacao` (LocalDateTime) - Data da última modificação
  - `usuarioUltimaAtualizacao` (String) - ID/nome do usuário que fez a última atualização
  - `apelido` (String) - Apelido/nome curto para aditivo

#### **Novos Campos em SalaryDTO:**
- ✅ `dataBase` (LocalDate) - Data base do reajuste salarial
- ✅ `porcentagemReajuste` (String) - Percentual de reajuste aplicado

#### **Novos Campos em BenefitsDTO:**
Os campos de benefícios foram atualizados para usar `BeneficioEstruturadoDTO` (objeto estruturado com aplicável, periodicidade, valor percentual, valor em reais, tipo de valor e observação):

- ✅ `almoco` (BeneficioEstruturadoDTO) - Almoço
- ✅ `lancheParada` (BeneficioEstruturadoDTO) - Lanche parada
- ✅ `valeAlimentacaoParada` (BeneficioEstruturadoDTO) - Vale alimentação parada
- ✅ `valeRefeicaoParada` (BeneficioEstruturadoDTO) - Vale refeição parada
- ✅ `cestaNatalina` (BeneficioEstruturadoDTO) - Cesta natalina
- ✅ `gratificacao` (BeneficioEstruturadoDTO) - Gratificação
- ✅ `gratificacaoAdcionalFerias` (BeneficioEstruturadoDTO) - Gratificação adicional de férias
- ✅ `gratificacaoAbonoParada` (BeneficioEstruturadoDTO) - Gratificação/Abono parada
- ✅ `plr` (BeneficioEstruturadoDTO) - PLR (Participação nos Lucros e Resultados)
- ✅ `plrParada` (BeneficioEstruturadoDTO) - PLR parada
- ✅ `ajudaDeCusto` (BeneficioEstruturadoDTO) - Ajuda de custo
- ✅ `auxilioCreche` (BeneficioEstruturadoDTO) - Auxílio creche
- ✅ `auxilioEducacao` (BeneficioEstruturadoDTO) - Auxílio educação

**Campos atualizados para BeneficioEstruturadoDTO:**
- `cafeDaManha` (BeneficioEstruturadoDTO) - Café da manhã
- `lanche` (BeneficioEstruturadoDTO) - Lanche
- `valeAlimentacao` (BeneficioEstruturadoDTO) - Vale alimentação
- `valeRefeicao` (BeneficioEstruturadoDTO) - Vale refeição
- `cestaBasica` (BeneficioEstruturadoDTO) - Cesta básica
- `flashVirtual` (BeneficioEstruturadoDTO) - Flash virtual
- `premioDesempenho` (BeneficioEstruturadoDTO) - Prêmio por desempenho
- `auxilioMoradia` (BeneficioEstruturadoDTO) - Auxílio moradia
- `reembolsoDespesaViagem` (BeneficioEstruturadoDTO) - Reembolso de despesa de viagem

#### **Novos Campos em AdditionalBenefitsDTO:**
Os campos de adicionais foram atualizados para usar `BeneficioEstruturadoDTO`:

- ✅ `adicionalNoturno` (BeneficioEstruturadoDTO) - Adicional noturno
- ✅ `adicionalInsalubridade` (BeneficioEstruturadoDTO) - Adicional de insalubridade
- ✅ `adicionalPericulosidade` (BeneficioEstruturadoDTO) - Adicional de periculosidade
- ✅ `adicionalSobreaviso` (BeneficioEstruturadoDTO) - Adicional de sobreaviso (20%)
- ✅ `adicionalProntidao` (BeneficioEstruturadoDTO) - Adicional de prontidão (33,33%)
- ✅ `adicionalHra` (BeneficioEstruturadoDTO) - Adicional HRA

**Estrutura do BeneficioEstruturadoDTO:**
```json
{
  "aplicavel": true,
  "periodicidade": "Mensal",
  "valorPercentual": 10.5,
  "valorTipo": "Salário base",
  "valorReais": 500.00,
  "observacao": "Observações sobre o benefício"
}
```

#### **Exemplos de Criação com os Novos Campos:**

**Exemplo 1: Criando ACT/CCT com novos campos de BenefitsDTO**

```json
{
  "tipoInstrumento": "ACT",
  "apelido": "ACT Tecnologia 2024",
  "numeroIdentificacaoInterno": "ACT-2024-001",
  "nomeInstrumento": "Acordo Coletivo de Trabalho 2024",
  "sindicatoTrabalhadoresId": "64f8b2c1234567890abcdef1",
  "dataInicioVigencia": "2024-01-01",
  "dataFimVigencia": "2024-12-31",
  "dataBase": "2024-01-01",
  "abrangenciaTerritorial": "Estadual",
  "ufPrincipal": ["SP", "RJ"],
  "categoriaPrincipalCBO": "CBO-1234",
  "laborRights": {
    "salary": {
      "pisoSalarial": "R$ 2.500,00",
      "tabelaSalarial": "Tabela 2024",
      "dataBase": "2024-01-01",
      "porcentagemReajuste": "5%"
    },
    "benefits": {
      "cafeDaManha": {
        "aplicavel": true,
        "periodicidade": "Diário",
        "valorReais": 15.00,
        "observacao": "Fornecido no refeitório da empresa"
      },
      "almoco": {
        "aplicavel": true,
        "periodicidade": "Diário",
        "valorReais": 25.00,
        "observacao": "Vale-refeição ou refeitório"
      },
      "lanche": {
        "aplicavel": true,
        "periodicidade": "Diário",
        "valorReais": 8.00,
        "observacao": "Lanche da tarde"
      },
      "lancheParada": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 12.00,
        "observacao": "Lanche durante paradas de trabalho"
      },
      "valeAlimentacao": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorReais": 500.00,
        "observacao": "Vale alimentação mensal"
      },
      "valeAlimentacaoParada": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 50.00,
        "observacao": "Vale alimentação para paradas"
      },
      "valeRefeicao": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorReais": 600.00,
        "observacao": "Vale refeição mensal"
      },
      "valeRefeicaoParada": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 30.00,
        "observacao": "Vale refeição para paradas"
      },
      "cestaBasica": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorReais": 200.00,
        "observacao": "Cesta básica mensal"
      },
      "cestaNatalina": {
        "aplicavel": true,
        "periodicidade": "Anual",
        "valorReais": 300.00,
        "observacao": "Cesta natalina no mês de dezembro"
      },
      "gratificacao": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 5.0,
        "valorTipo": "Salário base",
        "observacao": "Gratificação mensal de 5% sobre salário base"
      },
      "gratificacaoAdicionalFerias": {
        "aplicavel": true,
        "periodicidade": "Anual",
        "valorPercentual": 33.33,
        "valorTipo": "Salário base",
        "valorReais": 100.00,
        "observacao": "Gratificação adicional de 1/3 sobre férias"
      },
      "gratificacaoAbonoParada": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 100.00,
        "observacao": "Gratificação/abono para paradas"
      },
      "plr": {
        "aplicavel": true,
        "periodicidade": "Anual",
        "valorPercentual": 2.0,
        "valorTipo": "Salário base",
        "observacao": "PLR anual de 2 salários"
      },
      "plrParada": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 500.00,
        "observacao": "PLR para paradas"
      },
      "flashVirtual": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorReais": 150.00,
        "observacao": "Flash virtual mensal"
      },
      "premioDesempenho": {
        "aplicavel": true,
        "periodicidade": "Trimestral",
        "valorReais": 1000.00,
        "observacao": "Prêmio por desempenho trimestral"
      },
      "auxilioMoradia": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorReais": 800.00,
        "observacao": "Auxílio moradia mensal"
      },
      "reembolsoDespesaViagem": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 0,
        "observacao": "100% das despesas de viagem"
      },
      "ajudaDeCusto": {
        "aplicavel": true,
        "periodicidade": "Quando necessário",
        "valorReais": 500.00,
        "observacao": "Ajuda de custo para mudanças"
      },
      "auxilioCreche": {
        "aplicavel": true,
        "periodicidade": "Anual",
        "valorPercentual": 33.33,
        "valorTipo": "Salário base",
        "valorReais": 100.00,
        "observacao": "Auxílio creche mensal"
      },
      "auxilioEducacao": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 33.33,
        "valorTipo": "Salário base",
        "valorReais": 300.00,
        "observacao": "Auxílio educação mensal"
      }
    },
    "additionalBenefits": {
      "adicionalNoturno": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 20.0,
        "valorTipo": "Salário base",
        "observacao": "Adicional noturno de 20% sobre salário base"
      },
      "adicionalInsalubridade": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 30.0,
        "valorTipo": "Salário mínimo",
        "observacao": "Adicional de insalubridade grau médio (30%)"
      },
      "adicionalPericulosidade": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 30.0,
        "valorTipo": "Salário base",
        "observacao": "Adicional de periculosidade de 30%"
      },
      "adicionalSobreaviso": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 20.0,
        "valorTipo": "Salário base",
        "observacao": "Adicional de sobreaviso de 20%"
      },
      "adicionalProntidao": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 33.33,
        "valorTipo": "Salário base",
        "observacao": "Adicional de prontidão de 33,33%"
      },
      "adicionalHra": {
        "aplicavel": true,
        "periodicidade": "Mensal",
        "valorPercentual": 15.0,
        "valorTipo": "Salário base",
        "observacao": "Adicional HRA (Hora de Refeição e Alimentação) de 15% sobre salário base"
      }
    }
  }
}
```

**Exemplo 2: Benefício com valor percentual e valor em reais**

```json
{
  "gratificacao": {
    "aplicavel": true,
    "periodicidade": "Mensal",
    "valorPercentual": 10.5,
    "valorTipo": "Salário base",
    "valorReais": 500.00,
    "observacao": "Gratificação de 10,5% ou R$ 500,00, o que for maior"
  }
}
```

**Exemplo 3: Benefício não aplicável**

```json
{
  "cestaNatalina": {
    "aplicavel": false,
    "observacao": "Cesta natalina não prevista neste acordo"
  }
}
```

**Exemplo 4: Adicional com percentual sobre salário mínimo**

```json
{
  "adicionalInsalubridade": {
    "aplicavel": true,
    "periodicidade": "Mensal",
    "valorPercentual": 40.0,
    "valorTipo": "Salário mínimo",
    "observacao": "Adicional de insalubridade grau máximo (40% sobre salário mínimo)"
  }
}
```

**Exemplo 5: Adicional HRA com valor em reais (alternativa ao Exemplo 1)**

```json
{
  "adicionalHra": {
    "aplicavel": true,
    "periodicidade": "Mensal",
    "valorReais": 200.00,
    "observacao": "Adicional HRA fixo de R$ 200,00 mensais"
  }
}
```

**Nota importante:** Todos os campos de `AdditionalBenefitsDTO` (incluindo `adicionalNoturno`, `adicionalInsalubridade`, `adicionalPericulosidade`, `adicionalSobreaviso`, `adicionalProntidao` e `adicionalHra`) utilizam a mesma estrutura `BeneficioEstruturadoDTO`, podendo usar qualquer combinação dos campos disponíveis:
- `valorPercentual` + `valorTipo` (para percentuais sobre salário base ou salário mínimo)
- `valorReais` (para valores fixos em reais)
- Ou ambos (quando houver valor percentual E valor fixo)

#### **UseCases Atualizados:**
Todos os UseCases de criação e atualização agora registram automaticamente logs de auditoria:
- `CreateUnionUseCase` - Registra "CRIACAO_SINDICATO"
- `UpdateUnionUseCase` - Registra "ATUALIZACAO_SINDICATO"
- `CreateLaborContractUseCase` - Registra "CRIACAO_ACT" ou "CRIACAO_CCT"
- `UpdateLaborContractUseCase` - Registra "ATUALIZACAO_ACT" ou "ATUALIZACAO_CCT"
- `CreateLaborContractAddendumUseCase` - Registra "CRIACAO_ADITIVO_ACT" ou "CRIACAO_ADITIVO_CCT"
- `UpdateLaborContractAddendumUseCase` - Registra "ATUALIZACAO_ADITIVO_ACT" ou "ATUALIZACAO_ADITIVO_CCT"

#### **DTOs Atualizados com StepLog:**
Os seguintes DTOs de resposta agora incluem informações de auditoria:
- `UnionResponseDTO`
- `UnionDetailedResponseDTO`
- `LaborContractResponseDTO`
- `LaborContractAddendumResponseDTO`

#### **Estrutura do StepLog:**
Cada registro de log contém:
```json
{
  "id": "uuid",
  "name": "CRIACAO_SINDICATO",
  "user": "uuid-do-usuario",
  "created_at": "2025-10-08T10:30:00Z",
  "final_at": "2025-10-08T10:30:00Z",
  "step": 1,
  "observation": "Sindicato criado no sistema"
}
```

**Benefícios:**
- ✅ **Rastreabilidade Completa**: Histórico detalhado de quem criou/modificou cada registro
- ✅ **Auditoria Automática**: Logs gerados automaticamente em todas as operações
- ✅ **Transparência**: Informações de auditoria disponíveis em todas as APIs de consulta
- ✅ **Conformidade**: Atende requisitos de auditoria e compliance

**Impacto na API:**
- Todas as respostas de sindicatos, ACT/CCT e aditivos agora incluem `stepLog`, `dataUltimaAtualizacao` e `usuarioUltimaAtualizacao`
- Campo `apelido` disponível para facilitar identificação de ACT/CCT e aditivos
- Campos `dataBase` e `porcentagemReajuste` disponíveis em `laborRights.salary`
- Todos os campos de `BenefitsDTO` agora utilizam `BeneficioEstruturadoDTO` (estrutura com aplicável, periodicidade, valor percentual, valor em reais, tipo de valor e observação)
- Novos campos adicionados em `BenefitsDTO`: `almoco`, `lancheParada`, `valeAlimentacaoParada`, `valeRefeicaoParada`, `cestaNatalina`, `gratificacao`, `gratificacaoAdcionalFerias`, `gratificacaoAbonoParada`, `plr`, `plrParada`, `ajudaDeCusto`, `auxilioCreche`, `auxilioEducacao`
- Todos os campos de `AdditionalBenefitsDTO` agora utilizam `BeneficioEstruturadoDTO`, incluindo o novo campo `adicionalHra`

## 🔄 Mudanças Recentes - Versão 1.0.33

### ✅ Novos Endpoints para Detalhamento de ACTs/CCTs

**Data:** 02/10/2025

**Alterações:**
- **Novo endpoint**: `GET /api/unions/{id}/detailed-summary` - Retorna detalhes do sindicato com dados resumidos das ACTs/CCTs (apenas número registro, data início, data fim, status e tipo)
- **Novo endpoint**: `GET /api/unions/{unionId}/labor-contracts/{contractId}` - Retorna detalhes completos de uma ACT/CCT específica
- **DTO criado**: `LaborContractSummaryDTO` - Para dados resumidos das ACTs/CCTs
- **DTO criado**: `UnionDetailedWithSummaryDTO` - Para resposta detalhada do sindicato com resumo de contratos
- **UseCase criado**: `GetLaborContractDetailUseCase` - Para buscar detalhes de uma ACT/CCT específica
- **UseCase criado**: `GetLaborContractsSummaryUseCase` - Para buscar ACTs/CCTs resumidas de um sindicato
- **UseCase criado**: `GetUnionDetailedWithSummaryUseCase` - Para buscar sindicato com resumo de contratos

**Benefícios:**
- ✅ **Performance**: Endpoint de resumo mais rápido para listagens
- ✅ **Flexibilidade**: Dois níveis de detalhamento (resumo e completo)
- ✅ **Organização**: Separação clara entre dados do sindicato e contratos
- ✅ **Reutilização**: DTOs específicos para diferentes necessidades

## 🔄 Mudanças Recentes - Versão 1.0.32

### ✅ Conversão de String para Enum - TipoInstrumento

**Data:** 24/09/2025

**Alterações:**
- **Campo `tipoInstrumento`** nos contratos trabalhistas (ACT/CCT) agora usa enum `TipoInstrumento`
- **Campo `tipo`** nos aditivos de contratos trabalhistas agora usa enum `TipoInstrumento`
- **Valores aceitos:** `ACT` (Acordo Coletivo de Trabalho) e `CCT` (Convenção Coletiva de Trabalho)

### ✅ Validações com Bean Validation e @Validated

**Alterações:**
- Uso de `@Validated` nos casos de uso (services) e `@Valid` nos parâmetros dos métodos
- Validações básicas (obrigatoriedade, formato, consistência de datas) movidas para os DTOs com `@NotNull`, `@NotBlank`, `@Email`, `@AssertTrue`, etc.
- Casos de uso passaram a validar apenas regras de negócio (ex.: unicidade, estados válidos)

### ✅ Mapeamento com MapStruct

**Alterações:**
- `LaborContractMapper`, `LaborContractAddendumMapper` e `UnionMapper` migrados para MapStruct (`@Mapper(componentModel = "spring")`)
- Resolução de ambiguidades com `@Mapping(source=..., target=...)` e `imports = { LocalDateTime.class }`
- Métodos `default` apenas quando há necessidade de lógica adicional

### ✅ Direitos Trabalhistas Agrupados em DTOs Reutilizáveis

**Alterações:**
- Grande lista de campos de direitos trabalhistas foi dividida em DTOs menores e reutilizáveis
- Novo `LaborRightsDTO` agrega os grupos principais: salário/benefícios, jornada, folgas, saúde/segurança, transporte, condições de trabalho, disciplina, previdência, inclusão etc.
- Novo `LaborRightsMapper` centraliza o mapeamento entre entidades e o `LaborRightsDTO`

### ✅ Limpeza Geral

**Alterações:**
- Remoção de `System.out.println` e debugs
- Imports padronizados no formato Java (sem FQCN espalhado no código)

**Benefícios:**
- ✅ **Type Safety:** Apenas valores válidos são aceitos
- ✅ **Validação Automática:** Spring Boot valida automaticamente os valores
- ✅ **Código Mais Limpo:** Eliminação de validações manuais com `.trim()`
- ✅ **Melhor Performance:** Comparações de enum são mais rápidas que String

**Impacto na API:**
- **Antes:** `"tipoInstrumento": "ACT"` (String)
- **Depois:** `"tipoInstrumento": "ACT"` (Enum - mesmo formato JSON)
- **Validação:** Apenas `ACT` e `CCT` são aceitos (case-sensitive)

## Estrutura

O módulo segue a arquitetura limpa (Clean Architecture) com as seguintes camadas:

### Domain
- **model/Union.java**: Entidade principal que representa um sindicato no MongoDB
- **model/DatabaseSequenceUnion.java**: Entidade para controle de ID sequencial na tabela tb_sindicato_seq
- **model/LaborContract.java**: Entidade para contratos trabalhistas (ACT/CCT)
- **model/LaborContractAddendum.java**: Entidade para aditivos de contratos trabalhistas
- **enums/TipoInstrumento.java**: Enum para tipos de instrumento (ACT/CCT)
- **repository/UnionRepository.java**: Interface do repositório para operações de persistência
- **repository/DatabaseSequenceUnionRepository.java**: Repositório para controle de sequência
- **repository/LaborContractRepository.java**: Repositório para contratos trabalhistas
- **repository/LaborContractAddendumRepository.java**: Repositório para aditivos de contratos

### Application  
- **dto/CreateUnionRequestDTO.java**: DTO para requisições de criação de sindicato
- **dto/UnionResponseDTO.java**: DTO para respostas com dados do sindicato
- **dto/CreateLaborContractRequestDTO.java**: DTO para criação de contratos trabalhistas (usa enum TipoInstrumento)
- **dto/UpdateLaborContractRequestDTO.java**: DTO para atualização de contratos trabalhistas (usa enum TipoInstrumento)
- **dto/LaborContractResponseDTO.java**: DTO para respostas de contratos trabalhistas (usa enum TipoInstrumento)
- **dto/CreateLaborContractAddendumRequestDTO.java**: DTO para criação de aditivos (usa enum TipoInstrumento)
- **dto/LaborContractAddendumResponseDTO.java**: DTO para respostas de aditivos (usa enum TipoInstrumento)
- **dto/labor_rights/**: Grupo de DTOs de direitos trabalhistas reutilizáveis
  - `LaborRightsDTO`, `SalaryDTO`, `BenefitsDTO`, `WorkScheduleDTO`, `TimeOffBenefitsDTO`, `HealthSafetyDTO`, `TransportationDTO`, `WorkplaceConditionsDTO`, `DisciplinaryProceduresDTO`, `SocialSecurityDTO`, `InclusionPoliciesDTO`, `AdditionalBenefitsDTO`, `ContractTimeDTO`, `SeniorityBonusDTO`, `UnionContributionsDTO`, `WorkShiftTypesDTO`
- **mapper/UnionMapper.java**: Mapeamento entre entidades e DTOs (MapStruct)
- **mapper/LaborContractMapper.java**: Mapeamento para contratos trabalhistas (MapStruct)
- **mapper/LaborContractAddendumMapper.java**: Mapeamento para aditivos (MapStruct)
- **mapper/LaborRightsMapper.java**: Mapeamento entre entidades e `LaborRightsDTO`
- **service/CreateUnionUseCase.java**: Caso de uso para criação de sindicatos
- **service/CreateLaborContractUseCase.java**: Caso de uso para criação de contratos trabalhistas
- **service/UpdateLaborContractUseCase.java**: Caso de uso para atualização de contratos trabalhistas
- **service/CreateLaborContractAddendumUseCase.java**: Caso de uso para criação de aditivos

### Presentation
- **UnionController.java**: Controller REST com endpoints para sindicatos
- **LaborContractController.java**: Controller REST com endpoints para contratos trabalhistas

## Endpoints

### POST /api/unions

Cadastra um novo sindicato no sistema.

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Content-Type**: `multipart/form-data`

**Parâmetros**:

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| nomeCompletoSindicato | String(255) | Sim | Nome completo do sindicato |
| cnpj | String(18) | Sim | CNPJ do sindicato |
| codigoCnes | String(20) | Não | Código CNES |
| tipo | String | Não | Tipo do sindicato |
| categoriaRepresentada | String(255) | Não | Categoria representada |
| abrangenciaTerritorial | String | Não | Abrangência territorial |
| ufSede | List<String> | Não | UFs da sede (multi-select) |
| municipioSede | List<String> | Não | Municípios da sede (multi-select) |
| ufsAtendidas | List<String> | Não | UFs atendidas |
| municipiosAtendidos | List<String> | Não | Municípios atendidos |
| observacoesTerritoriais | Text | Não | Observações territoriais |
| logradouro | String(255) | Não | Logradouro do endereço |
| numero | String(20) | Não | Número do endereço |
| uf | String(2) | Não | UF do endereço |
| cep | String(10) | Não | CEP do endereço |
| cidade | String(100) | Não | Cidade do endereço |
| telefonePrincipal | String(20) | Não | Telefone principal |
| telefone2 | String(20) | Não | Telefone secundário |
| emailInstitucional | Email | Não | Email institucional |
| email2 | Email | Não | Email secundário |
| site | String(255) | Não | Site institucional |
| redeSocial | String(255) | Não | Rede social institucional |
| situacaoMte | String | Não | Situação no MTE |
| dataUltimaAtualizacaoMte | Date | Não | Data última atualização MTE |
| presidenteAtual | String(100) | Não | Presidente atual |
| mandatoInicio | Date | Não | Início do mandato |
| mandatoFim | Date | Não | Fim do mandato |
| documentosAnexos | List<MultipartFile> | Não | Documentos anexos |
| observacoes | Text | Não | Observações gerais |
| usuarioCriacao | String(100) | Não | Usuário que criou o registro |
| statusRegistro | String | Não | Status do registro |

**Resposta de Sucesso** (201):
```json
{
  "id": "64f8b2c1234567890abcdef1",
  "codeID": 12345,
  "nomeCompletoSindicato": "Sindicato dos Trabalhadores em Teste",
  "sigla": "STT",
  "cnpj": "12.345.678/0001-90",
  "statusRegistro": "ATIVO",
  "dataCriacao": "2023-09-18T10:30:00",
  ...
}
```

**Possíveis Erros**:
- 400: Dados inválidos ou CNPJ/sigla já cadastrados
- 401: Usuário não autenticado
- 403: Usuário sem permissão
- 500: Erro interno do servidor

### DELETE /api/unions/{id}

Exclui permanentemente um sindicato do sistema.

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Parâmetros**:
- `id` (path): ID do sindicato a ser excluído

**Resposta de Sucesso** (200):
```json
{
  "message": "Sindicato excluído com sucesso",
  "code": 200
}
```

**Possíveis Erros**:
- 400: Sindicato não encontrado ou já inativo
- 401: Usuário não autenticado
- 403: Usuário sem permissão
- 500: Erro interno do servidor

### PATCH /api/unions/{id}/deactivate

Desativa um sindicato (exclusão lógica - altera status para INATIVO).

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Parâmetros**:
- `id` (path): ID do sindicato a ser desativado

**Resposta de Sucesso** (200):
```json
{
  "message": "Sindicato desativado com sucesso",
  "code": 200
}
```

**Possíveis Erros**:
- 400: Sindicato não encontrado ou já inativo
- 401: Usuário não autenticado
- 403: Usuário sem permissão
- 500: Erro interno do servidor

### GET /api/unions

Lista sindicatos cadastrados com paginação e filtros opcionais.

**Autorização**: Requer role `ROLE_ADMINISTRADOR` ou `ROLE_USUARIO`

**Parâmetros de Query (todos opcionais)**:
- `nomeCompletoSindicato`: Filtro por nome do sindicato
- `cnpj`: Filtro por CNPJ
- `tipo`: Filtro por tipo
- `categoriaRepresentada`: Filtro por categoria representada
- `ufSede`: Filtro por UF da sede (busca em lista)
- `municipioSede`: Filtro por município da sede (busca em lista)
- `situacaoMte`: Filtro por situação no MTE
- `presidenteAtual`: Filtro por nome do presidente
- `statusRegistro`: Filtro por status (ATIVO/INATIVO)
- `cidade`: Filtro por cidade do endereço
- `uf`: Filtro por UF do endereço
- `ufAtendida`: Filtro por UF atendida (busca na lista `ufsAtendidas`)
- `page`: Número da página (padrão: 0)
- `size`: Tamanho da página (padrão: 20)
- `sort`: Ordenação (ex: `nomeCompletoSindicato,asc`)

**Resposta de Sucesso** (200):
```json
{
  "content": [
    {
      "id": "68cd2d492329c37e18b00944",
      "nomeCompletoSindicato": "Sindicato dos Programadores",
      "statusRegistro": "ATIVO",
      "mandatoInicio": "2022-01-01",
      "uf": "PE",
      "cidade": "Recife"
    }
  ],
  "pageable": {
    "sort": { "sorted": false, "unsorted": true },
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 20,
  "number": 0
}
```

**Possíveis Erros**:
- 401: Usuário não autenticado
- 403: Usuário sem permissão
- 500: Erro interno do servidor

**Exemplos de Uso**:
```bash
# Buscar sindicatos que atendem o estado de Pernambuco
curl -X GET "http://localhost:8080/api/unions?ufAtendida=PE"

# Buscar sindicatos que atendem São Paulo com paginação
curl -X GET "http://localhost:8080/api/unions?ufAtendida=SP&page=0&size=10"

# Combinar filtros: sindicatos de PE que atendem BA
curl -X GET "http://localhost:8080/api/unions?ufSede=PE&ufAtendida=BA"
```

**Observações**:
- ✅ **Resposta simplificada**: Retorna apenas os campos essenciais (id, nomeCompletoSindicato, statusRegistro, mandatoInicio, uf, cidade)
- ✅ **Para informações completas**: Use o endpoint `GET /api/unions/{id}/detailed`
- ✅ **Filtro ufAtendida**: Busca sindicatos que incluem o estado especificado na lista `ufsAtendidas`

### PATCH /api/unions/{id}

Atualiza um sindicato existente (atualização parcial - apenas campos fornecidos).

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Content-Type**: `multipart/form-data`

**Parâmetros**:
- `id` (path): ID do sindicato a ser atualizado
- Todos os campos do POST são opcionais (apenas os fornecidos serão atualizados)

**Exemplo de Atualização Parcial**:
```bash
curl -X PATCH "http://localhost:8080/api/unions/64f8b2c1234567890abcdef1" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: multipart/form-data" \
  -F "nomeCompletoSindicato=Sindicato dos Trabalhadores Atualizados" \
  -F "telefonePrincipal=(11) 9999-8888" \
  -F "telefone2=(11) 8888-7777" \
  -F "emailInstitucional=novo@email.com.br" \
  -F "email2=contato@sindicato.com.br" \
  -F "site=https://www.sindicato.com.br" \
  -F "redeSocial=https://www.facebook.com/sindicato"
```

**Exemplo via Postman/Insomnia**:
1. **Método**: PATCH
2. **URL**: `http://localhost:8080/api/unions/{id}`
3. **Headers**: `Authorization: Bearer {token}`
4. **Body (form-data)**:
   ```
   nomeCompletoSindicato: Sindicato Atualizado
   presidenteAtual: Novo Presidente
   telefonePrincipal: (11) 8888-7777
   telefone2: (11) 7777-6666
   emailInstitucional: novo@email.com.br
   email2: contato@sindicato.com.br
   site: https://www.sindicato.com.br
   redeSocial: https://www.facebook.com/sindicato
   documentosAnexos: [arquivo_adicional.pdf] (opcional)
   ```

**Resposta de Sucesso** (200):
```json
{
  "id": "64f8b2c1234567890abcdef1",
  "nomeCompletoSindicato": "Sindicato dos Trabalhadores Atualizados",
  "sigla": "STT", 
  "cnpj": "12.345.678/0001-90",
  "telefonePrincipal": "(11) 9999-8888",
  "telefone2": "(11) 8888-7777",
  "emailInstitucional": "novo@email.com.br",
  "email2": "contato@sindicato.com.br",
  "site": "https://www.sindicato.com.br",
  "redeSocial": "https://www.facebook.com/sindicato",
  "dataCriacao": "2023-09-18T14:30:00",
  "statusRegistro": "ATIVO",
  ...
}
```

**Possíveis Erros**:
- 400: Dados inválidos, sindicato não encontrado, ou CNPJ/sigla já cadastrados
- 401: Usuário não autenticado
- 403: Usuário sem permissão
- 404: Sindicato não encontrado
- 500: Erro interno do servidor

**Observações sobre Atualização**:
- ✅ **Atualização parcial**: Apenas campos fornecidos são alterados
- ✅ **Validação de unicidade**: CNPJ e sigla são validados se alterados
- ✅ **Documentos adicionais**: Novos documentos são ADICIONADOS aos existentes
- ✅ **Campos opcionais**: Todos os campos podem ser omitidos
- ✅ **Preservação de dados**: Campos não fornecidos mantêm valores originais

### GET /api/unions/{id}/detailed

Retorna todas as informações detalhadas de um sindicato específico, incluindo dados resumidos das suas ACTs/CCTs (apenas tipo, status, número registro, data início e data fim).

**Autorização**: Sem restrições de autorização

**Parâmetros**:
- `id` (path): ID do sindicato a ser consultado

**Exemplo de requisição**:
```bash
curl -X GET "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/detailed"
```

**Resposta de Sucesso** (200):
```json
{
  "id": "68cd2d492329c37e18b00944",
  "codeID": 123,
  "nomeCompletoSindicato": "Sindicato dos Programadores",
  "sigla": "SINTEC",
  "cnpj": "12.345.678/0001-90",
  "codigoCnes": "123456",
  "tipo": "Sindicato",
  "categoriaRepresentada": "Tecnologia",
  "abrangenciaTerritorial": "Nacional",
  "ufSede": ["SP", "RJ"],
  "municipioSede": ["São Paulo", "Rio de Janeiro"],
  "ufsAtendidas": ["SP", "RJ", "MG"],
  "municipiosAtendidos": ["São Paulo", "Rio de Janeiro"],
  "observacoesTerritoriais": "Atende todo o território nacional",
  "logradouro": "Rua das Flores, 123",
  "numero": "123",
  "uf": "SP",
  "cep": "01234-567",
  "cidade": "São Paulo",
  "telefonePrincipal": "(11) 99999-9999",
  "telefone2": "(11) 88888-8888",
  "emailInstitucional": "contato@sintectec.com.br",
  "email2": "presidencia@sintectec.com.br",
  "site": "https://www.sintectec.com.br",
  "redeSocial": "https://www.facebook.com/sintectec",
  "situacaoMte": "ATIVO",
  "dataUltimaAtualizacaoMte": "2023-01-15",
  "presidenteAtual": "João Silva",
  "mandatoInicio": "2022-01-01",
  "mandatoFim": "2025-12-31",
  "documentosAnexos": [
    {
      "id": "doc1",
      "fileName": "estatuto.pdf",
      "fileSize": 1024000,
      "contentType": "application/pdf",
      "uploadDate": "2023-01-01T10:00:00"
    }
  ],
  "observacoes": "Observações gerais sobre o sindicato",
  "dataCriacao": "2023-01-01T10:00:00",
  "usuarioCriacao": "admin",
  "statusRegistro": "ATIVO",
  "laborContracts": [
    {
      "id": "contract1",
      "numeroRegistro": "ACT001",
      "dataInicioVigencia": "2024-01-01",
      "dataFimVigencia": "2024-12-31",
      "status": "ATIVO",
      "tipoInstrumento": "ACT"
    },
    {
      "id": "contract2",
      "numeroRegistro": "CCT002",
      "dataInicioVigencia": "2024-06-01",
      "dataFimVigencia": "2025-05-31",
      "status": "ATIVO",
      "tipoInstrumento": "CCT"
    }
  ]
}
```

**Possíveis Erros**:
- 404: Sindicato não encontrado
- 500: Erro interno do servidor

**Observações sobre o Endpoint Detailed**:
- ✅ **Dados resumidos**: ACTs/CCTs retornam apenas campos essenciais (tipo, status, número registro, datas)
- ✅ **Performance**: Mais rápido que endpoint com dados completos
- ✅ **Para detalhes completos**: Use `GET /api/unions/{unionId}/labor-contracts/{contractId}`

### PATCH /api/unions/{id}

Atualiza um sindicato existente (atualização parcial - apenas campos fornecidos).

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Content-Type**: `multipart/form-data`

**Parâmetros**:
- `id` (path): ID do sindicato a ser atualizado
- Todos os campos do POST são opcionais (apenas os fornecidos serão atualizados)

**Exemplo de Atualização Parcial**:
      "linkAnexo": "https://exemplo.com/cct001.pdf",
      "resumoIA": "Resumo gerado por IA",
      "pisoSalarial": "R$ 6.000,00",
      "reajusteSalarial": "Anual baseado no INPC",
      "jornadaTrabalho": "40 horas semanais",
      "horasExtras": "50% para as 2 primeiras horas, 100% para as demais",
      "bancoHoras": "Permitido com acordo",
      "adicionalNoturno": "20% sobre o salário base",
      "intervalos": "1 hora para almoço, 15 min para lanche",
      "descansoSemanal": "1 dia por semana",
      "ferias": "30 dias corridos",
      "decimoTerceiroSalario": "Pago em dezembro",
      "valeTransporte": "6% do salário base",
      "valeAlimentacao": "R$ 600,00 mensais",
      "planoSaude": {
        "extensividade": "Extensiva aos dependentes",
        "cobertura": "Quarto individual",
        "abrangencia": "Nacional",
        "corteMaiorIdade": "24 anos"
      },
      "seguroVida": {
        "valorPremio": "R$ 75.000,00",
        "extensividade": "Extensiva aos dependentes"
      },
      "pprPlr": "Participação nos lucros conforme acordo",
      "estabilidade": "12 meses após contratação",
      "licencas": "Licença maternidade conforme CLT",
      "statusRegistro": "ATIVO",
      "addendums": [
        {
          "id": "addendum1",
          "contratoTrabalhistaId": "contract1",
          "tipo": "ACT",
          "titulo": "Aditivo Salarial 2024",
          "dataInclusao": "2024-06-01",
          "dataInicio": "2024-06-01",
          "dataFim": "2024-12-31",
          "link": "https://exemplo.com/aditivo_salarial_2024.pdf",
          "status": "ATIVO",
          "pisoSalarial": "R$ 5.500,00",
          "reajusteSalarial": "8% sobre o salário base",
          "valeAlimentacao": "R$ 600,00 mensais",
          "arquivosAnexos": [],
          "dataCriacao": "2024-06-01T10:00:00",
          "usuarioCriacao": "admin",
          "statusRegistro": "ATIVO"
        },
        {
          "id": "addendum2",
          "contratoTrabalhistaId": "contract1",
          "tipo": "CCT",
          "titulo": "Aditivo de Benefícios 2024",
          "dataInclusao": "2024-07-01",
          "dataInicio": "2024-07-01",
          "dataFim": "2024-12-31",
          "link": "https://exemplo.com/aditivo_beneficios_2024.pdf",
          "status": "ATIVO",
          "planoSaude": {
            "extensividade": "Extensiva aos dependentes",
            "cobertura": "Quarto individual",
            "abrangencia": "Nacional",
            "corteMaiorIdade": "24 anos"
          },
          "seguroVida": {
            "valorPremio": "R$ 75.000,00",
            "extensividade": "Extensiva aos dependentes"
          },
          "valeTransporte": "7% do salário base",
          "arquivosAnexos": [],
          "dataCriacao": "2024-07-01T14:30:00",
          "usuarioCriacao": "admin",
          "statusRegistro": "ATIVO"
        }
      ]
    }
  ]
}
```

**Possíveis Erros**:
- 404: Sindicato não encontrado
- 500: Erro interno do servidor

**Observações**:
- ✅ **Informações completas**: Retorna todos os dados do sindicato
- ✅ **ACTs/CCTs incluídas**: Lista todos os contratos trabalhistas do sindicato
- ✅ **Aditivos incluídos**: Cada contrato trabalhista inclui seus aditivos (ACTs/CCTs)
- ✅ **Sem paginação**: Retorna todos os contratos em uma única resposta
- ✅ **Dados aninhados**: Inclui informações detalhadas dos contratos trabalhistas e seus aditivos

## Exemplo de Cadastro

### Cadastro Completo via cURL

```bash
curl -X POST "http://localhost:8080/api/unions" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: multipart/form-data" \
  -F "nomeCompletoSindicato=Sindicato dos Trabalhadores em Tecnologia da Informação" \
  -F "cnpj=12.345.678/0001-90" \
  -F "codigoCnes=123456789" \
  -F "tipo=TRABALHADORES" \
  -F "categoriaRepresentada=Trabalhadores em Tecnologia da Informação" \
  -F "abrangenciaTerritorial=ESTADUAL" \
  -F "ufSede=SP" \
  -F "ufSede=RJ" \
  -F "municipioSede=São Paulo" \
  -F "municipioSede=Rio de Janeiro" \
  -F "ufsAtendidas=SP" \
  -F "ufsAtendidas=RJ" \
  -F "municipiosAtendidos=São Paulo" \
  -F "municipiosAtendidos=Rio de Janeiro" \
  -F "observacoesTerritoriais=Atende principalmente região Sudeste" \
  -F "logradouro=Rua da Tecnologia" \
  -F "numero=123" \
  -F "uf=SP" \
  -F "cep=01234-567" \
  -F "cidade=São Paulo" \
  -F "telefonePrincipal=(11) 1234-5678" \
  -F "telefone2=(11) 9876-5432" \
  -F "emailInstitucional=contato@stti.org.br" \
  -F "email2=presidencia@stti.org.br" \
  -F "site=https://www.stti.org.br" \
  -F "redeSocial=https://www.facebook.com/stti" \
  -F "situacaoMte=ATIVO" \
  -F "dataUltimaAtualizacaoMte=2023-09-18" \
  -F "presidenteAtual=João da Silva" \
  -F "mandatoInicio=2023-01-01" \
  -F "mandatoFim=2027-01-01" \
  -F "observacoes=Sindicato focado em direitos dos trabalhadores de TI" \
  -F "usuarioCriacao=admin" \
  -F "statusRegistro=ATIVO"
```

### Cadastro com Documentos Anexos

```bash
curl -X POST "http://localhost:8080/api/unions" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: multipart/form-data" \
  -F "nomeCompletoSindicato=Sindicato dos Metalúrgicos" \
  -F "sigla=SM" \
  -F "cnpj=98.765.432/0001-10" \
  -F "tipo=TRABALHADORES" \
  -F "categoriaRepresentada=Metalúrgicos" \
  -F "ufSede=SP" \
  -F "municipioSede=São Bernardo do Campo" \
  -F "logradouro=Av. Industrial" \
  -F "numero=456" \
  -F "uf=SP" \
  -F "cep=09876-543" \
  -F "cidade=São Bernardo do Campo" \
  -F "telefonePrincipal=(11) 9876-5432" \
  -F "telefone2=(11) 8765-4321" \
  -F "emailInstitucional=contato@metalurgicos.org.br" \
  -F "email2=secretaria@metalurgicos.org.br" \
  -F "site=https://www.metalurgicos.org.br" \
  -F "redeSocial=https://www.instagram.com/metalurgicos" \
  -F "presidenteAtual=Maria Santos" \
  -F "statusRegistro=ATIVO" \
  -F "documentosAnexos=@/caminho/para/estatuto.pdf" \
  -F "documentosAnexos=@/caminho/para/ata_fundacao.pdf"
```

### Cadastro Mínimo (Apenas Campos Obrigatórios)

```bash
curl -X POST "http://localhost:8080/api/unions" \
  -H "Authorization: Bearer {seu-token-jwt}" \
  -H "Content-Type: multipart/form-data" \
  -F "nomeCompletoSindicato=Sindicato Exemplo" \
  -F "sigla=SE" \
  -F "cnpj=11.222.333/0001-44"
```

### Exemplo via Postman/Insomnia

1. **Método**: POST
2. **URL**: `http://localhost:8080/api/unions`
3. **Headers**:
   - `Authorization: Bearer {seu-token-jwt}`
   - `Content-Type: multipart/form-data`
4. **Body (form-data)**:
   ```
   nomeCompletoSindicato: Sindicato dos Comerciários
   cnpj: 55.666.777/0001-88
   tipo: TRABALHADORES
   categoriaRepresentada: Comerciários
   ufSede: [RJ]
   municipioSede: [Rio de Janeiro]
   logradouro: Rua do Comércio
   numero: 789
   uf: RJ
   cep: 20000-000
   cidade: Rio de Janeiro
   telefonePrincipal: (21) 3333-4444
   telefone2: (21) 2222-3333
   emailInstitucional: contato@comerciarios.org.br
   email2: presidencia@comerciarios.org.br
   site: https://www.comerciarios.org.br
   redeSocial: https://www.facebook.com/comerciarios
   presidenteAtual: Carlos Oliveira
   statusRegistro: ATIVO
   ```

### Exemplo com Documentos Anexos via Postman/Insomnia

1. **Método**: POST
2. **URL**: `http://localhost:8080/api/unions`
3. **Headers**:
   - `Authorization: Bearer {seu-token-jwt}`
   - `Content-Type: multipart/form-data`
4. **Body (form-data)**:
   
   **Campos de Texto:**
   ```
   nomeCompletoSindicato: Sindicato dos Trabalhadores Rurais
   cnpj: 77.888.999/0001-55
   tipo: TRABALHADORES
   categoriaRepresentada: Trabalhadores Rurais
   ufSede: [MG]
   municipioSede: [Uberlândia]
   logradouro: Estrada Rural
   numero: 1000
   uf: MG
   cep: 38400-000
   cidade: Uberlândia
   telefonePrincipal: (34) 5555-6666
   telefone2: (34) 4444-5555
   emailInstitucional: contato@str.org.br
   email2: secretaria@str.org.br
   site: https://www.str.org.br
   redeSocial: https://www.instagram.com/str
   presidenteAtual: Ana Silva
   statusRegistro: ATIVO
   observacoes: Sindicato focado nos direitos dos trabalhadores rurais
   usuarioCriacao: admin
   ```

   **Documentos Anexos (Files):**
   
   Para cada documento, adicione uma nova linha no form-data:
   
   | Key | Type | Value |
   |-----|------|-------|
   | `documentosAnexos` | File | [Selecionar arquivo: estatuto.pdf] |
   | `documentosAnexos` | File | [Selecionar arquivo: ata_fundacao.pdf] |
   | `documentosAnexos` | File | [Selecionar arquivo: registro_mte.pdf] |

   **Passo a passo no Postman:**
   1. Na aba **Body**, selecione **form-data**
   2. Para campos de texto: 
      - Key: `nomeCompletoSindicato` | Type: Text | Value: `Sindicato dos Trabalhadores Rurais`
   3. Para documentos:
      - Key: `documentosAnexos` | Type: **File** | Value: [Clique em "Select Files" e escolha o arquivo]
      - Repita para cada documento (pode usar a mesma key `documentosAnexos` múltiplas vezes)

   **Passo a passo no Insomnia:**
   1. Na aba **Body**, selecione **Multipart Form**
   2. Clique em **+** para adicionar campos
   3. Para campos de texto: Nome: `nomeCompletoSindicato` | Valor: `Sindicato dos Trabalhadores Rurais`
   4. Para documentos: Nome: `documentosAnexos` | Tipo: **File** | Clique no ícone de pasta para selecionar o arquivo

### Estrutura Simplificada para Documentos

Os documentos agora são enviados diretamente como `MultipartFile`:

**Exemplo no Postman/Insomnia (Recomendado):**
```
Key: documentosAnexos | Type: File | Value: [estatuto.pdf]
Key: documentosAnexos | Type: File | Value: [ata_fundacao.pdf]
Key: documentosAnexos | Type: File | Value: [registro_mte.pdf]
```

**Observações:**
- Use a mesma key `documentosAnexos` para múltiplos arquivos
- O nome do documento será automaticamente o nome do arquivo enviado
- Tipos de arquivo aceitos: PDF, DOC, DOCX, JPG, PNG, etc.

### Resposta de Sucesso (201)

```json
{
  "id": "64f8b2c1234567890abcdef1",
  "nomeCompletoSindicato": "Sindicato dos Trabalhadores em Tecnologia da Informação",
  "cnpj": "12.345.678/0001-90",
  "codigoCnes": "123456789",
  "tipo": "TRABALHADORES",
  "categoriaRepresentada": "Trabalhadores em Tecnologia da Informação",
  "abrangenciaTerritorial": "ESTADUAL",
  "ufSede": ["SP", "RJ"],
  "municipioSede": ["São Paulo", "Rio de Janeiro"],
  "ufsAtendidas": ["SP", "RJ"],
  "municipiosAtendidos": ["São Paulo", "Rio de Janeiro"],
  "observacoesTerritoriais": "Atende principalmente região Sudeste",
  "logradouro": "Rua da Tecnologia",
  "numero": "123",
  "uf": "SP",
  "cep": "01234-567",
  "cidade": "São Paulo",
  "telefonePrincipal": "(11) 1234-5678",
  "telefone2": "(11) 9876-5432",
  "emailInstitucional": "contato@stti.org.br",
  "email2": "presidencia@stti.org.br",
  "site": "https://www.stti.org.br",
  "redeSocial": "https://www.facebook.com/stti",
  "situacaoMte": "ATIVO",
  "dataUltimaAtualizacaoMte": "2023-09-18",
  "presidenteAtual": "João da Silva",
  "mandatoInicio": "2023-01-01",
  "mandatoFim": "2027-01-01",
  "documentosAnexos": [
    {
      "id": "uuid-gerado-1",
      "nome": "estatuto.pdf",
      "file": {
        "path": "unions/documents/uuid-timestamp-1",
        "extension": "pdf",
        "mimeType": "application/pdf",
        "etapa": 1
      }
    }
  ],
  "observacoes": "Sindicato focado em direitos dos trabalhadores de TI",
  "dataCriacao": "2023-09-18T14:30:00",
  "usuarioCriacao": "admin",
  "statusRegistro": "ATIVO"
}
```

## Validações

- CNPJ deve ser único no sistema
- Sigla deve ser única no sistema (quando fornecida)
- Campos obrigatórios: nome completo, sigla e CNPJ
- Email deve ter formato válido
- Limites de caracteres conforme especificado na tabela

## Armazenamento

- **Dados**: Armazenados no MongoDB na collection "unions"
- **ID Sequencial**: Controlado pela tabela PostgreSQL "tb_sindicato_seq" 
- **Documentos**: Armazenados usando AttachmentEntity com FileMetadata em "unions/documents/"
- **Estrutura de Anexo**: Cada documento possui ID único, nome, e metadados do arquivo (path, extensão, mimeType)

## Controle de Sequência

Seguindo o padrão do módulo OCF, cada sindicato recebe:
- **ID único MongoDB**: String gerada automaticamente pelo MongoDB
- **ID sequencial**: Long gerado pela sequence "tb_sindicato_seq_seq" no PostgreSQL
- **Vínculo**: Tabela tb_sindicato_seq armazena a relação entre codeID (id) e documento_id (MongoDB ID)

## Testes

O módulo possui cobertura de testes unitários:

### Testes de Sindicatos
- **CreateUnionUseCaseTest**: Testes unitários do caso de uso de criação
- **DeleteUnionUseCaseTest**: Testes unitários do caso de uso de exclusão
- **UpdateUnionUseCaseTest**: Testes unitários do caso de uso de atualização
- **ListUnionsUseCaseTest**: Testes unitários do caso de uso de listagem

### Testes de Contratos Trabalhistas
- **CreateLaborContractUseCaseTest**: Testes unitários para criação de contratos
- **UpdateLaborContractUseCaseTest**: Testes unitários para atualização de contratos
- **CreateLaborContractAddendumUseCaseTest**: Testes unitários para criação de aditivos

### Testes do Enum TipoInstrumento
- **Validação automática**: Spring Boot valida valores do enum
- **Testes de integração**: Verificação de valores válidos/inválidos
- **Testes de serialização**: JSON serialization/deserialization

Para executar os testes:
```bash
# Todos os testes do módulo
mvn test -Dtest="*Union*Test,*LaborContract*Test"

# Testes específicos
mvn test -Dtest="CreateUnionUseCaseTest,DeleteUnionUseCaseTest,UpdateUnionUseCaseTest,ListUnionsUseCaseTest"
mvn test -Dtest="CreateLaborContractUseCaseTest,UpdateLaborContractUseCaseTest,CreateLaborContractAddendumUseCaseTest"
```

---

# Contratos Trabalhistas (ACT/CCT)

Este módulo também permite o cadastro de Acordos Coletivos de Trabalho (ACT) e Convenções Coletivas de Trabalho (CCT) associados aos sindicatos cadastrados.

## Estrutura dos Contratos Trabalhistas

### Domain
- **model/LaborContract.java**: Entidade principal que representa um contrato trabalhista no MongoDB
- **repository/LaborContractRepository.java**: Interface do repositório para operações de persistência

### Application  
- **dto/CreateLaborContractRequestDTO.java**: DTO para requisições de criação de contrato trabalhista
- **dto/LaborContractResponseDTO.java**: DTO para respostas com dados do contrato
- **mapper/LaborContractMapper.java**: Mapeamento entre entidades e DTOs
- **service/CreateLaborContractUseCase.java**: Caso de uso para criação de contratos trabalhistas

### Presentation
- **LaborContractController.java**: Controller REST com endpoint para cadastro de contratos

## Endpoints de Contratos Trabalhistas

### POST /api/unions/{unionId}/labor-contracts

Cadastra um novo contrato trabalhista (ACT ou CCT) para um sindicato específico.



**Content-Type**: `multipart/form-data`

**Path Parameters:**
- `unionId`: ID do sindicato (obrigatório)

**Campos Obrigatórios:**

| Campo | Tipo | Descrição | Validação |
|-------|------|-----------|-----------|
| tipoInstrumento | Enum | Tipo do instrumento | ACT ou CCT |
| dataInicioVigencia | Date | Data de início da vigência | Deve ser anterior à data de fim |
| dataFimVigencia | Date | Data de fim da vigência | Deve ser posterior à data de início |
| dataBase | Date | Data base | Deve ser anterior ou igual à data de início |
| abrangenciaTerritorial | String | Abrangência territorial | Dropdown (Nacional, Estadual, Municipal) |
| ufPrincipal | List<String> | UFs principais | Multi-select com UFs do Brasil |
| descricaoResumidaCategorias | String(255) | Descrição resumida das categorias | Não pode estar vazio |

**Campos Opcionais:**

| Campo | Tipo | Descrição | Observações |
|-------|------|-----------|-------------|
| apelido | String(100) | Apelido/nome curto | Nome curto para facilitar identificação |
| numeroRegistro | String(50) | Número do registro | Registro oficial no MTE |
| numeroSolicitacao | String(50) | Número da solicitação | Número da solicitação no MTE |
| numeroIdentificacaoInterno | String(50) | Número de identificação interno | Identificação interna do contrato |
| nomeInstrumento | String(255) | Nome do instrumento | Nome descritivo do contrato |
| empresasSignatarias | List<String> | Empresas signatárias | Multi-select com empresas |
| municipio | List<String> | Municípios | Multi-select com municípios |
| municipiosAbrangidos | List<String> | Municípios abrangidos | **Obrigatório se abrangência for Municipal** |
| estadosAdicionais | List<String> | Estados adicionais | Para contratos multi-estaduais |
| observacoesTerritoriais | Text | Observações territoriais | Texto livre |
| categoriaPrincipalCBO | String | Categoria principal CBO | Dropdown com códigos CBO |
| subcategoriaCBO | String | Subcategoria CBO | Subcategoria da categoria principal |
| funcoesEspecificas | List<String> | Funções específicas | Multi-select com funções |
| excecoesInclusoes | Text | Exceções/Inclusões | Texto livre |
| situacaoMTE | String | Situação MTE | Dropdown (Aprovado, Pendente, Rejeitado) |
| arquivosInstrumento | File Upload | Arquivos do instrumento | Múltiplos arquivos permitidos |
| linkAnexo | URL | Link anexo | URL válida |
| resumoIA | Text | Resumo da IA | Texto ilimitado |

**Direitos Trabalhistas (laborRights):**

Os direitos trabalhistas foram reorganizados em grupos mais específicos para facilitar reuso entre contratos e aditivos:

### 4. SalaryDTO - Salários
| Campo | Tipo | Descrição |
|-------|------|-----------|
| pisoSalarial | String | Piso salarial da categoria |
| tabelaSalarial | String | Tabela salarial aplicável |
| dataBase | LocalDate | Data base do reajuste salarial |
| porcentagemReajuste | String | Percentual de reajuste aplicado |

### 5. BenefitsDTO - Benefícios

**Tipo Alimentação e Refeição:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| cafeDaManha | BeneficioEstruturadoDTO | Café da manhã |
| almoco | BeneficioEstruturadoDTO | Almoço |
| lanche | BeneficioEstruturadoDTO | Lanche |
| lancheParada | BeneficioEstruturadoDTO | Lanche parada |
| valeAlimentacao | BeneficioEstruturadoDTO | Vale alimentação |
| valeAlimentacaoParada | BeneficioEstruturadoDTO | Vale alimentação parada |
| valeRefeicao | BeneficioEstruturadoDTO | Vale refeição |
| valeRefeicaoParada | BeneficioEstruturadoDTO | Vale refeição parada |
| cestaBasica | BeneficioEstruturadoDTO | Cesta básica |
| cestaNatalina | BeneficioEstruturadoDTO | Cesta natalina |

**Tipo Premiações:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| gratificacao | BeneficioEstruturadoDTO | Gratificação |
| gratificacaoAdcionalFerias | BeneficioEstruturadoDTO | Gratificação adicional de férias |
| gratificacaoAbonoParada | BeneficioEstruturadoDTO | Gratificação/Abono parada |
| plr | BeneficioEstruturadoDTO | PLR (Participação nos Lucros e Resultados) |
| plrParada | BeneficioEstruturadoDTO | PLR parada |
| flashVirtual | BeneficioEstruturadoDTO | Flash virtual |
| premioDesempenho | BeneficioEstruturadoDTO | Prêmio por desempenho |

**Tipo Outros Benefícios:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| auxilioMoradia | BeneficioEstruturadoDTO | Auxílio moradia |
| reembolsoDespesaViagem | BeneficioEstruturadoDTO | Reembolso de despesa de viagem |
| ajudaDeCusto | BeneficioEstruturadoDTO | Ajuda de custo |
| auxilioCreche | BeneficioEstruturadoDTO | Auxílio creche |
| auxilioEducacao | BeneficioEstruturadoDTO | Auxílio educação |
| planoSaude | PlanoSaudeDTO | Plano de saúde |
| seguroVida | SeguroVidaDTO | Seguro de vida |
| planoOdontologico | PlanoOdontologicoDTO | Plano odontológico |

#### BeneficioEstruturadoDTO - Estrutura de Benefício
| Campo | Tipo | Descrição |
|-------|------|-----------|
| aplicavel | Boolean | Se o benefício é aplicável |
| periodicidade | String | Periodicidade (Mensal/Anual/Diário/Quando necessário) |
| valorPercentual | BigDecimal | Valor percentual (ex: 10.5 para 10,5%) |
| valorTipo | String | Tipo de valor base (Salário base ou Salário Mínimo) |
| valorReais | BigDecimal | Valor em reais |
| observacao | String | Observações sobre o benefício |

#### PlanoSaudeDTO - Plano de Saúde
| Campo | Tipo | Descrição |
|-------|------|-----------|
| extensividade | String | Individual ou extensiva aos dependentes |
| cobertura | String | Quarto coletivo ou individual |
| abrangencia | String | Nacional ou regional |
| corteMaiorIdade | String | 21 ou 24 anos |

#### SeguroVidaDTO - Seguro de Vida
| Campo | Tipo | Descrição |
|-------|------|-----------|
| valorPremio | String | Valor do prêmio |
| extensividade | String | Individual ou extensiva aos dependentes |

#### PlanoOdontologicoDTO - Plano Odontológico
| Campo | Tipo | Descrição |
|-------|------|-----------|
| extensividade | String | Individual ou extensiva aos dependentes |
| abrangencia | String | Nacional ou regional |
| dataCorte | String | Data de corte |
| corteMaiorIdade | String | Idade limite para dependentes |

### 6. WorkScheduleDTO - Jornada de Trabalho
| Campo | Tipo | Descrição |
|-------|------|-----------|
| horasExtras | String | Regras para horas extras |
| bancoHoras | String | Regras do banco de horas |
| controlePonto | String | Sistema de controle de ponto |
| tiposContratacaoPermitidos | List<String> | Trabalho Intermitente, Estagiário, Jovem Aprendiz, Contrato por prazo indeterminado |

### 7. WorkShiftTypesDTO - Tipos de Escala
| Campo | Tipo | Descrição | Valores Aceitos |
|-------|------|-----------|-----------------|
| escala5x2 | String | Escala 5x2 | Texto livre |
| escala6x1 | String | Escala 6x1 | Texto livre |
| escala12x36 | String | Escala 12x36 | Texto livre |
| escala4x4 | String | Escala 4x4 | Texto livre |
| escala7x7 | String | Escala 7x7 | Texto livre |
| escalaL5811 | String | Escala Lei 5811/72 | Texto livre |
| outro | String | Outros tipos de escala | Texto livre |

### 8. ContractTimeDTO - Tempo de Contrato
| Campo | Tipo | Descrição | Valores Aceitos |
|-------|------|-----------|-----------------|
| tempo30x30 | String | Tempo de contrato 30x30 | Texto livre |
| tempo45x45 | String | Tempo de contrato 45x45 | Texto livre |
| tempo30NaoRenovaveis | String | Tempo de contrato 30 não renováveis | Texto livre |
| outro | String | Outros tempos de contrato | Texto livre |

### 8. TimeOffBenefitsDTO - Folgas e Licenças
| Campo | Tipo | Descrição |
|-------|------|-----------|
| ferias | String | Regras de férias |
| licencas | String | Tipos de licenças disponíveis |
| avisoPrevio | String | Período de aviso prévio |
| estabilidade | String | Regras de estabilidade |
| aleitamentoMaterno | String | Regras para aleitamento materno |

### 9. HealthSafetyDTO - Saúde e Segurança
| Campo | Tipo | Descrição |
|-------|------|-----------|
| equipamentosProtecao | String | Equipamentos de proteção individual |
| examesMedicos | String | Tipos de exames médicos obrigatórios |

### 10. AdditionalBenefitsDTO - Adicionais
| Campo | Tipo | Descrição |
|-------|------|-----------|
| adicionalNoturno | BeneficioEstruturadoDTO | Adicional noturno |
| adicionalInsalubridade | BeneficioEstruturadoDTO | Adicional de insalubridade |
| adicionalPericulosidade | BeneficioEstruturadoDTO | Adicional de periculosidade |
| adicionalSobreaviso | BeneficioEstruturadoDTO | Adicional de sobreaviso (20%) |
| adicionalProntidao | BeneficioEstruturadoDTO | Adicional de prontidão (33,33%) |
| adicionalHra | BeneficioEstruturadoDTO | Adicional HRA |

**Nota:** Todos os campos de `AdditionalBenefitsDTO` utilizam a estrutura `BeneficioEstruturadoDTO` que permite definir percentuais, valores em reais, periodicidade e observações de forma estruturada.

### 11. TransportationDTO - Transporte
| Campo | Tipo | Descrição | Valores Aceitos |
|-------|------|-----------|-----------------|
| valeTransporte | ValeTransporteDTO | Vale transporte (lei 7418 de 85) | Objeto com desconto e descrição |
| valeTransporte.descontoAplicado | String | Desconto a ser aplicado | Ex: "6% do salário", "R$ 50,00" |
| valeTransporte.descricaoValeTransporte | String | Descrição detalhada do vale transporte | Texto livre |
| auxilioTransporte | String | Auxílio transporte (definir valor) | Ex: "R$ 200,00", "1% do salário" |
| descontoAuxilioTransporte | String | Desconto a ser aplicado | Ex: "6% do salário", "R$ 30,00" |
| descricaoAuxilioTransporte | String | Descrição detalhada do auxílio transporte | Texto livre |
| fretado | String | Fretado | Sim, Não |
| descontoFretado | String | Desconto a ser aplicado | Ex: "6% do salário", "R$ 40,00" |
| descricaoFretado | String | Descrição detalhada do fretado | Texto livre |

### 12. SeniorityBonusDTO - Anuênio
| Campo | Tipo | Descrição | Valores Aceitos |
|-------|------|-----------|-----------------|
| aplicaAnuenio | String | Aplica anuênio | Sim, Não |
| valorAnuenio | String | Valor a ser pago | Ex: "R$ 500,00", "1% do salário" |
| descricaoAnuenio | String | Descrição detalhada do anuênio | Texto livre |

### 12. WorkplaceConditionsDTO - Condições de Trabalho
| Campo | Tipo | Descrição |
|-------|------|-----------|
| condicoesTrabalho | String | Condições gerais de trabalho |
| teletrabalhoHomeOffice | String | Regras para teletrabalho/home office |
| treinamentos | String | Capacitações e treinamentos |

### 13. DisciplinaryProceduresDTO - Procedimentos Disciplinares
| Campo | Tipo | Descrição |
|-------|------|-----------|
| multasPenalidades | String | Multas e penalidades |
| projecaoAviso | String | Projeção do aviso |
| trintidio | String | Trintídio (aplica projeção do aviso - sim ou não) |
| multaEncerramentoContratoTempoServico | String | Multa por encerramento de contrato de acordo com o tempo de serviço |
| multaEncerramentoContratoParada | String | Multa por encerramento de contrato de parada |

### 14. UnionContributionsDTO - Contribuições Sindicais
| Campo | Tipo | Descrição | Valores Aceitos |
|-------|------|-----------|-----------------|
| aplicaContribuicaoSindicalEmpregado | String | Aplica contribuição sindical do empregado | Sim, Não |
| valorContribuicaoSindicalEmpregado | String | Valor a ser descontado do empregado | Ex: "R$ 50,00", "1% do salário" |
| descricaoContribuicaoSindicalEmpregado | String | Descrição detalhada da contribuição do empregado | Texto livre |
| aplicaContribuicaoPatronal | String | Aplica contribuição patronal | Sim, Não |
| valorContribuicaoPatronal | String | Valor a ser pago pelo empregador | Ex: "R$ 200,00", "2% da folha" |
| descricaoContribuicaoPatronal | String | Descrição detalhada da contribuição patronal | Texto livre |
| aplicaContribuicaoPatronalEducativa | String | Aplica contribuição patronal educativa | Sim, Não |
| valorContribuicaoPatronalEducativa | String | Valor a ser pago para educação | Ex: "R$ 100,00", "0,5% da folha" |
| descricaoContribuicaoPatronalEducativa | String | Descrição detalhada da contribuição educativa | Texto livre |

**Observações:**
- Todos os campos são opcionais
- Os campos "aplica" aceitam valores "Sim" ou "Não"
- Os campos "valor" podem conter valores monetários ou percentuais
- Os campos "descrição" aceitam texto livre para detalhamento das contribuições
- As contribuições são aplicadas conforme legislação trabalhista vigente

**Exemplos de Valores:**
- Valores monetários: "R$ 50,00", "R$ 200,00", "R$ 100,00"
- Valores percentuais: "1% do salário", "2% da folha", "0,5% da folha"
- Valores mistos: "R$ 50,00 + 1% do salário"

**Exemplos de Descrições:**
- "Contribuição mensal obrigatória conforme estatuto do sindicato"
- "Taxa anual para manutenção das atividades sindicais"
- "Contribuição para programas de capacitação profissional"

**Aplicação das Contribuições:**
- **Contribuição Sindical do Empregado**: Descontada do salário do trabalhador
- **Contribuição Patronal**: Paga pelo empregador para o sindicato
- **Contribuição Patronal Educativa**: Paga pelo empregador para programas educacionais


```bash
# Salários
-F "laborRights.salary.pisoSalarial=R$ 1.500,00" \
-F "laborRights.salary.tabelaSalarial=Tabela 2024" \

# Benefícios - Alimentação e Refeição
-F "laborRights.benefits.valeAlimentacao=R$ 500,00" \
-F "laborRights.benefits.valeRefeicao=R$ 300,00" \
-F "laborRights.benefits.cestaBasica=R$ 200,00" \

# Benefícios - Premiações
-F "laborRights.benefits.flashVirtual=R$ 100,00" \
-F "laborRights.benefits.premioDesempenho=R$ 1.000,00" \

# Benefícios - Outros
-F "laborRights.benefits.auxilioMoradia=R$ 800,00" \
-F "laborRights.benefits.reembolsoDespesaViagem=100%" \

# Benefícios - Plano de Saúde
-F "laborRights.benefits.planoSaude.extensividade=Extensiva aos dependentes" \
-F "laborRights.benefits.planoSaude.cobertura=Quarto individual" \
-F "laborRights.benefits.planoSaude.abrangencia=Nacional" \
-F "laborRights.benefits.planoSaude.corteMaiorIdade=24 anos" \

# Benefícios - Seguro de Vida
-F "laborRights.benefits.seguroVida.valorPremio=R$ 50.000,00" \
-F "laborRights.benefits.seguroVida.extensividade=Extensiva aos dependentes" \

# Benefícios - Plano Odontológico
-F "laborRights.benefits.planoOdontologico.extensividade=Extensiva aos dependentes" \
-F "laborRights.benefits.planoOdontologico.abrangencia=Nacional" \
-F "laborRights.benefits.planoOdontologico.dataCorte=31/12" \
-F "laborRights.benefits.planoOdontologico.corteMaiorIdade=24 anos" \

# Jornada de Trabalho
-F "laborRights.workSchedule.horasExtras=50% sobre salário" \
-F "laborRights.workSchedule.bancoHoras=Acúmulo de horas extras" \
-F "laborRights.workSchedule.controlePonto=Registro eletrônico" \
-F "laborRights.workSchedule.tiposContratacaoPermitidos=Trabalho Intermitente" \
-F "laborRights.workSchedule.tiposContratacaoPermitidos=Contrato por prazo indeterminado" \

# Tipos de Escala
-F "laborRights.workShiftTypes.escala5x2=Aplicável" \
-F "laborRights.workShiftTypes.escala6x1=Aplicável" \
-F "laborRights.workShiftTypes.escala12x36=Não aplicável" \
-F "laborRights.workShiftTypes.escala4x4=Aplicável" \
-F "laborRights.workShiftTypes.escala7x7=Não aplicável" \
-F "laborRights.workShiftTypes.escalaL5811=Aplicável" \
-F "laborRights.workShiftTypes.outro=Escala 3x1 conforme acordo coletivo"

# Tempo de Contrato
-F "laborRights.contractTime.tempo30x30=Aplicável" \
-F "laborRights.contractTime.tempo45x45=Aplicável" \
-F "laborRights.contractTime.tempo30NaoRenovaveis=Não aplicável" \
-F "laborRights.contractTime.outro=Tempo de contrato 60x60 conforme acordo coletivo"

# Folgas e Licenças
-F "laborRights.timeOffBenefits.ferias=30 dias corridos" \
-F "laborRights.timeOffBenefits.licencas=Médica, maternidade, paternidade" \
-F "laborRights.timeOffBenefits.avisoPrevio=30 dias" \

# Saúde e Segurança
-F "laborRights.healthSafety.equipamentosProtecao=Fornecidos pela empresa" \
-F "laborRights.healthSafety.examesMedicos=Admissional, periódico, demissional" \

# Adicionais
-F "laborRights.additionalBenefits.adicionalPericulosidade=30%" \
-F "laborRights.additionalBenefits.adicionalInsalubridadePercentual=20%" \
-F "laborRights.additionalBenefits.adicionalInsalubridadeDescricao=Grau mínimo" \
-F "laborRights.additionalBenefits.adicionalSobreaviso=20%" \
-F "laborRights.additionalBenefits.adicionalNoturnoPercentual=20%" \
-F "laborRights.additionalBenefits.adicionalNoturnoDescricao=Trabalho das 22h às 6h" \

# Transporte
-F "laborRights.transportation.valeTransporte.descontoAplicado=6% do salário" \
-F "laborRights.transportation.valeTransporte.descricaoValeTransporte=Vale transporte conforme lei 7418/85" \
-F "laborRights.transportation.auxilioTransporte=R$ 200,00" \
-F "laborRights.transportation.descontoAuxilioTransporte=6% do salário" \
-F "laborRights.transportation.descricaoAuxilioTransporte=Auxílio transporte para deslocamento" \
-F "laborRights.transportation.fretado=Sim" \
-F "laborRights.transportation.descontoFretado=6% do salário" \
-F "laborRights.transportation.descricaoFretado=Transporte fretado pela empresa"

# Anuênio
-F "laborRights.seniorityBonus.aplicaAnuenio=Sim" \
-F "laborRights.seniorityBonus.valorAnuenio=R$ 500,00" \
-F "laborRights.seniorityBonus.descricaoAnuenio=Bonificação anual por tempo de serviço"

# Condições de Trabalho
-F "laborRights.workplaceConditions.condicoesTrabalho=Ambiente salubre" \
-F "laborRights.workplaceConditions.teletrabalhoHomeOffice=Regulamentado" \

# Procedimentos Disciplinares
-F "laborRights.disciplinaryProcedures.multasPenalidades=Conforme legislação" \
-F "laborRights.disciplinaryProcedures.projecaoAviso=30 dias" \
-F "laborRights.disciplinaryProcedures.trintidio=Sim" \

# Contribuições Sindicais
-F "laborRights.unionContributions.aplicaContribuicaoSindicalEmpregado=Sim" \
-F "laborRights.unionContributions.valorContribuicaoSindicalEmpregado=R$ 50,00" \
-F "laborRights.unionContributions.descricaoContribuicaoSindicalEmpregado=Contribuição mensal obrigatória conforme estatuto" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronal=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronal=R$ 200,00" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronal=Taxa anual para manutenção das atividades sindicais" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronalEducativa=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronalEducativa=R$ 100,00" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronalEducativa=Contribuição para programas de capacitação profissional"
```

**Exemplo de requisição:**

```bash
curl -X POST "http://localhost:8080/api/unions/64f8b2c1234567890abcdef1/labor-contracts" \
  -H "Authorization: Bearer {token}" \
  -F "tipoInstrumento=ACT" \
  -F "dataInicioVigencia=2024-01-01" \
  -F "dataFimVigencia=2024-12-31" \
  -F "dataBase=2023-12-31" \
  -F "abrangenciaTerritorial=Estadual" \
  -F "ufPrincipal=SP" \
  -F "ufPrincipal=RJ" \
  -F "municipio=São Paulo" \
  -F "municipio=Rio de Janeiro" \
  -F "descricaoResumidaCategorias=Profissionais da área de tecnologia" \
  -F "empresasSignatarias=Empresa ABC Ltda" \
  -F "empresasSignatarias=Empresa XYZ S.A." \
  -F "situacaoMTE=Aprovado" \
  -F "laborRights.salary.pisoSalarial=R$ 1.500,00" \
  -F "laborRights.salary.tabelaSalarial=Tabela 2024" \
  -F "laborRights.benefits.valeAlimentacao=R$ 500,00" \
  -F "laborRights.benefits.valeRefeicao=R$ 300,00" \
  -F "laborRights.benefits.cestaBasica=R$ 200,00" \
  -F "laborRights.benefits.flashVirtual=R$ 100,00" \
  -F "laborRights.benefits.premioDesempenho=R$ 1.000,00" \
  -F "laborRights.benefits.auxilioMoradia=R$ 800,00" \
  -F "laborRights.benefits.reembolsoDespesaViagem=100%" \
  -F "laborRights.benefits.planoSaude.extensividade=Extensiva aos dependentes" \
  -F "laborRights.benefits.planoSaude.cobertura=Quarto individual" \
  -F "laborRights.benefits.planoSaude.abrangencia=Nacional" \
  -F "laborRights.benefits.planoSaude.corteMaiorIdade=24 anos" \
  -F "laborRights.benefits.seguroVida.valorPremio=R$ 50.000,00" \
  -F "laborRights.benefits.seguroVida.extensividade=Extensiva aos dependentes" \
  -F "laborRights.benefits.planoOdontologico.extensividade=Extensiva aos dependentes" \
  -F "laborRights.benefits.planoOdontologico.abrangencia=Nacional" \
  -F "laborRights.benefits.planoOdontologico.dataCorte=31/12" \
  -F "laborRights.benefits.planoOdontologico.corteMaiorIdade=24 anos" \
  -F "laborRights.workSchedule.horasExtras=50% sobre salário" \
  -F "laborRights.workSchedule.bancoHoras=Acúmulo de horas extras" \
  -F "laborRights.workSchedule.controlePonto=Registro eletrônico" \
  -F "laborRights.workSchedule.tiposContratacaoPermitidos=Trabalho Intermitente" \
  -F "laborRights.workSchedule.tiposContratacaoPermitidos=Contrato por prazo indeterminado" \
  -F "laborRights.workShiftTypes.escala5x2=Aplicável" \
  -F "laborRights.workShiftTypes.escala6x1=Aplicável" \
  -F "laborRights.workShiftTypes.escala12x36=Não aplicável" \
  -F "laborRights.workShiftTypes.escala4x4=Aplicável" \
  -F "laborRights.workShiftTypes.escala7x7=Não aplicável" \
  -F "laborRights.workShiftTypes.escalaL5811=Aplicável" \
  -F "laborRights.workShiftTypes.outro=Escala 3x1 conforme acordo coletivo" \
  -F "laborRights.contractTime.tempo30x30=Aplicável" \
  -F "laborRights.contractTime.tempo45x45=Aplicável" \
  -F "laborRights.contractTime.tempo30NaoRenovaveis=Não aplicável" \
  -F "laborRights.contractTime.outro=Tempo de contrato 60x60 conforme acordo coletivo" \
  -F "laborRights.timeOffBenefits.ferias=30 dias corridos" \
  -F "laborRights.timeOffBenefits.licencas=Médica, maternidade, paternidade" \
  -F "laborRights.timeOffBenefits.avisoPrevio=30 dias" \
  -F "laborRights.timeOffBenefits.estabilidade=12 meses após admissão" \
  -F "laborRights.timeOffBenefits.aleitamentoMaterno=2 pausas de 30 min" \
  -F "laborRights.healthSafety.equipamentosProtecao=Fornecidos pela empresa" \
  -F "laborRights.healthSafety.examesMedicos=Admissional, periódico, demissional" \
  -F "laborRights.additionalBenefits.adicionalPericulosidade=30%" \
  -F "laborRights.additionalBenefits.adicionalInsalubridadePercentual=20%" \
  -F "laborRights.additionalBenefits.adicionalInsalubridadeDescricao=Grau mínimo" \
  -F "laborRights.additionalBenefits.adicionalSobreaviso=20%" \
  -F "laborRights.additionalBenefits.adicionalNoturnoPercentual=20%" \
  -F "laborRights.additionalBenefits.adicionalNoturnoDescricao=Trabalho das 22h às 6h" \
  -F "laborRights.transportation.valeTransporte.descontoAplicado=6% do salário" \
  -F "laborRights.transportation.valeTransporte.descricaoValeTransporte=Vale transporte conforme lei 7418/85" \
  -F "laborRights.transportation.auxilioTransporte=R$ 200,00" \
  -F "laborRights.transportation.descontoAuxilioTransporte=6% do salário" \
  -F "laborRights.transportation.descricaoAuxilioTransporte=Auxílio transporte para deslocamento" \
  -F "laborRights.transportation.fretado=Sim" \
  -F "laborRights.transportation.descontoFretado=6% do salário" \
  -F "laborRights.transportation.descricaoFretado=Transporte fretado pela empresa" \
  -F "laborRights.seniorityBonus.aplicaAnuenio=Sim" \
  -F "laborRights.seniorityBonus.valorAnuenio=R$ 500,00" \
  -F "laborRights.seniorityBonus.descricaoAnuenio=Bonificação anual por tempo de serviço" \
  -F "laborRights.workplaceConditions.condicoesTrabalho=Ambiente salubre" \
  -F "laborRights.workplaceConditions.teletrabalhoHomeOffice=Regulamentado" \
  -F "laborRights.workplaceConditions.treinamentos=Capacitação obrigatória" \
  -F "laborRights.disciplinaryProcedures.multasPenalidades=Conforme legislação" \
  -F "laborRights.disciplinaryProcedures.projecaoAviso=30 dias" \
  -F "laborRights.disciplinaryProcedures.trintidio=Sim" \
  -F "laborRights.disciplinaryProcedures.multaEncerramentoContratoTempoServico=40% FGTS" \
  -F "laborRights.disciplinaryProcedures.multaEncerramentoContratoParada=Multa por parada" \
  -F "laborRights.unionContributions.aplicaContribuicaoSindicalEmpregado=Sim" \
  -F "laborRights.unionContributions.valorContribuicaoSindicalEmpregado=R$ 50,00" \
  -F "laborRights.unionContributions.descricaoContribuicaoSindicalEmpregado=Contribuição mensal obrigatória conforme estatuto" \
  -F "laborRights.unionContributions.aplicaContribuicaoPatronal=Sim" \
  -F "laborRights.unionContributions.valorContribuicaoPatronal=R$ 200,00" \
  -F "laborRights.unionContributions.descricaoContribuicaoPatronal=Taxa anual para manutenção das atividades sindicais" \
  -F "laborRights.unionContributions.aplicaContribuicaoPatronalEducativa=Sim" \
  -F "laborRights.unionContributions.valorContribuicaoPatronalEducativa=R$ 100,00" \
  -F "laborRights.unionContributions.descricaoContribuicaoPatronalEducativa=Contribuição para programas de capacitação profissional" \
  -F "arquivosInstrumento=@documento.pdf"
```

**Resposta de sucesso (201 Created):**

```json
{
  "id": "64f8b2c1234567890abcdef2",
  "tipoInstrumento": "ACT",
  "numeroRegistro": null,
  "numeroSolicitacao": null,
  "numeroIdentificacaoInterno": "ACT-2024-001",
  "nomeInstrumento": "Acordo Coletivo de Trabalho 2024",
  "sindicatoTrabalhadoresId": "64f8b2c1234567890abcdef1",
  "empresasSignatarias": ["Empresa ABC Ltda", "Empresa XYZ S.A."],
  "dataInicioVigencia": "2024-01-01",
  "dataFimVigencia": "2024-12-31",
  "dataBase": "2023-12-31",
  "abrangenciaTerritorial": "Estadual",
  "ufPrincipal": ["SP"],
  "municipio": ["São Paulo"],
  "municipiosAbrangidos": null,
  "estadosAdicionais": null,
  "observacoesTerritoriais": null,
  "categoriaPrincipalCBO": "1114",
  "subcategoriaCBO": null,
  "funcoesEspecificas": null,
  "descricaoResumidaCategorias": "Profissionais da área de tecnologia",
  "excecoesInclusoes": null,
  "situacaoMTE": "Aprovado",
  "arquivosInstrumento": [
    {
      "id": "uuid-do-arquivo",
      "nome": "documento.pdf",
      "file": {
        "path": "labor_contracts/documents/arquivo.pdf",
        "extension": "pdf",
        "mimeType": "application/pdf",
        "etapa": 1
      }
    }
  ],
  "linkAnexo": null,
  "resumoIA": null,
  "dataCriacao": "2024-01-15T10:30:00",
  "usuarioCriacao": "admin",
  "statusRegistro": "ATIVO",
  "nomeSindicato": "Sindicato dos Trabalhadores em Tecnologia",
  "siglaSindicato": "STT"
}
```

**Validações dos Contratos:**

1. O sindicato deve existir no sistema
2. **Tipo de instrumento deve ser ACT ou CCT** (validado automaticamente pelo enum TipoInstrumento)
3. Data de início deve ser anterior à data de fim
4. Data base deve ser anterior ou igual à data de início
5. Se abrangência for Municipal, municípios abrangidos são obrigatórios
6. **Validação de enum:** Apenas valores `ACT` e `CCT` são aceitos (case-sensitive)

**Estrutura de arquivos dos contratos:**
- Os arquivos são armazenados em `labor_contracts/documents/`
- Cada arquivo recebe um nome único baseado em UUID e timestamp

### DELETE /api/unions/{unionId}/labor-contracts/{contractId}

Exclui permanentemente um contrato trabalhista.

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Path Parameters:**
- `unionId`: ID do sindicato
- `contractId`: ID do contrato trabalhista

**Exemplo de requisição:**

```bash
curl -X DELETE "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts/contrato123" \
  -H "Authorization: Bearer {token}"
```

**Resposta de sucesso (200 OK):**

```json
{
  "message": "Contrato trabalhista excluído com sucesso",
  "status": 200
}
```

### PATCH /api/unions/{unionId}/labor-contracts/{contractId}/deactivate

Desativa um contrato trabalhista (soft delete).

**Autorização**: Requer role `ROLE_ADMINISTRADOR`

**Path Parameters:**
- `unionId`: ID do sindicato
- `contractId`: ID do contrato trabalhista

**Exemplo de requisição:**

```bash
curl -X PATCH "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts/contrato123/deactivate" \
  -H "Authorization: Bearer {token}"
```

**Resposta de sucesso (200 OK):**

```json
{
  "message": "Contrato trabalhista desativado com sucesso",
  "status": 200
}
```

**Validações dos endpoints de delete:**

1. O contrato deve existir no sistema
2. O contrato não pode estar já inativo (para soft delete)
3. Apenas administradores podem excluir/desativar contratos

### GET /api/unions/{unionId}/labor-contracts

Lista os contratos trabalhistas de um sindicato específico com paginação.

**Autorização**: Requer role `ROLE_ADMINISTRADOR` ou `ROLE_USUARIO`

**Path Parameters:**
- `unionId`: ID do sindicato

**Query Parameters:**
- `tipoInstrumento` (opcional): Filtrar por tipo (ACT ou CCT)
- `page` (opcional): Número da página (padrão: 0)
- `size` (opcional): Tamanho da página (padrão: 20)
- `sort` (opcional): Campo para ordenação

**Exemplo de requisição:**

```bash
# Listar todos os contratos
curl -X GET "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts" \
  -H "Authorization: Bearer {token}"

# Listar apenas ACTs
curl -X GET "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts?tipoInstrumento=ACT" \
  -H "Authorization: Bearer {token}"

# Listar com paginação
curl -X GET "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

**Resposta de sucesso (200 OK):**

```json
{
  "content": [
    {
      "id": "contrato123",
      "tipoInstrumento": "ACT",
      "numeroIdentificacaoInterno": "ACT-2024-001",
      "nomeInstrumento": "Acordo Coletivo de Trabalho 2024",
      "sindicatoTrabalhadoresId": "68cd2d492329c37e18b00944",
      "nomeSindicato": "Sindicato dos Trabalhadores em Tecnologia",
      "siglaSindicato": "STT",
      "dataInicioVigencia": "2024-01-01",
      "dataFimVigencia": "2024-12-31",
      "statusRegistro": "ATIVO",
      // ... outros campos
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

### GET /api/unions/{unionId}/labor-contracts/all

Lista todos os contratos trabalhistas de um sindicato específico sem paginação.

**Autorização**: Requer role `ROLE_ADMINISTRADOR` ou `ROLE_USUARIO`

**Path Parameters:**
- `unionId`: ID do sindicato

**Exemplo de requisição:**

```bash
curl -X GET "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts/all" \
  -H "Authorization: Bearer {token}"
```

**Resposta de sucesso (200 OK):**

```json
[
  {
    "id": "contrato123",
    "tipoInstrumento": "ACT",
    "numeroIdentificacaoInterno": "ACT-2024-001",
    "nomeInstrumento": "Acordo Coletivo de Trabalho 2024",
    "sindicatoTrabalhadoresId": "68cd2d492329c37e18b00944",
    "nomeSindicato": "Sindicato dos Trabalhadores em Tecnologia",
    "siglaSindicato": "STT",
    "dataInicioVigencia": "2024-01-01",
    "dataFimVigencia": "2024-12-31",
    "statusRegistro": "ATIVO",
    // ... outros campos
  }
]
```

### GET /api/unions/{unionId}/labor-contracts/{contractId}

Retorna os detalhes completos de uma ACT/CCT específica de um sindicato.

**Autorização**: Requer role `ADMIN` ou `USER`

**Path Parameters:**
- `unionId`: ID do sindicato
- `contractId`: ID do contrato trabalhista (ACT/CCT)

**Exemplo de requisição:**

```bash
curl -X GET "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts/contrato123" \
  -H "Authorization: Bearer {token}"
```

**Resposta de sucesso (200 OK):**

```json
{
  "id": "contrato123",
  "tipoInstrumento": "ACT",
  "numeroRegistro": "ACT-2024-001",
  "numeroSolicitacao": "SOL-2024-001",
  "numeroIdentificacaoInterno": "ACT-2024-001",
  "nomeInstrumento": "Acordo Coletivo de Trabalho 2024",
  "sindicatoTrabalhadoresId": "68cd2d492329c37e18b00944",
  "empresasSignatarias": ["Empresa A", "Empresa B"],
  "dataInicioVigencia": "2024-01-01",
  "dataFimVigencia": "2024-12-31",
  "dataBase": "2024-01-01",
  "abrangenciaTerritorial": "Nacional",
  "ufPrincipal": ["SP", "RJ"],
  "municipio": ["São Paulo", "Rio de Janeiro"],
  "municipiosAbrangidos": ["São Paulo", "Rio de Janeiro"],
  "estadosAdicionais": ["MG", "RJ"],
  "observacoesTerritoriais": "Abrangência nacional",
  "categoriaPrincipalCBO": "Desenvolvedores",
  "subcategoriaCBO": "Desenvolvedor Full Stack",
  "funcoesEspecificas": ["Desenvolvedor", "Analista", "Arquiteto"],
  "descricaoResumidaCategorias": "Profissionais de desenvolvimento de software",
  "excecoesInclusoes": "Nenhuma exceção",
  "situacaoMTE": "ATIVO",
  "arquivosInstrumento": [],
  "linkAnexo": "https://exemplo.com/act001.pdf",
  "resumoIA": "Resumo gerado por IA",
  "laborRights": {
    "salary": {
      "pisoSalarial": "R$ 5.000,00",
      "tabelaSalarial": "Conforme tabela anexa"
    },
    "benefits": {
      "valeAlimentacao": "R$ 500,00",
      "valeRefeicao": "R$ 300,00",
      "cestaBasica": "R$ 200,00",
      "cafeDaManha": "Fornecido pela empresa",
      "lanche": "Fornecido pela empresa",
      "flashVirtual": "R$ 100,00",
      "premioDesempenho": "Até 2 salários",
      "auxilioMoradia": "R$ 800,00",
      "reembolsoDespesaViagem": "Conforme política da empresa",
      "planoSaude": {
        "extensividade": "Individual",
        "cobertura": "Quarto individual",
        "abrangencia": "Nacional",
        "corteMaiorIdade": "65 anos"
      },
      "seguroVida": {
        "valorPremio": "R$ 50.000,00",
        "extensividade": "Individual"
      },
      "planoOdontologico": {
        "extensividade": "Individual",
        "abrangencia": "Nacional",
        "dataCorte": "2024-12-31",
        "corteMaiorIdade": "65 anos"
      }
    },
    "workSchedule": {
      "horasExtras": "50% para as 2 primeiras horas, 100% para as demais",
      "bancoHoras": "Permitido com acordo",
      "controlePonto": "Eletrônico",
      "tiposContratacaoPermitidos": ["CLT", "PJ"]
    },
    "workShiftTypes": {
      "escala5x2": "Segunda a sexta",
      "escala6x1": "Segunda a sábado",
      "escala12x36": "12 horas trabalhadas, 36 horas de descanso",
      "escala4x4": "4 dias trabalhados, 4 dias de folga",
      "escala7x7": "7 dias trabalhados, 7 dias de folga",
      "escalaL5811": "Conforme Lei 5.811",
      "outro": "Escala especial conforme acordo"
    },
    "contractTime": {
      "tempo30x30": "30 dias trabalhados, 30 dias de folga",
      "tempo45x45": "45 dias trabalhados, 45 dias de folga",
      "tempo30NaoRenovaveis": "30 dias não renováveis",
      "outro": "Tempo especial conforme acordo"
    },
    "timeOffBenefits": {
      "ferias": "30 dias corridos",
      "licencas": "Conforme legislação",
      "avisoPrevio": "30 dias",
      "estabilidade": "Conforme legislação",
      "aleitamentoMaterno": "Conforme legislação"
    },
    "healthSafety": {
      "equipamentosProtecao": "Fornecidos pela empresa",
      "examesMedicos": "Admissionais, periódicos e demissionais"
    },
    "additionalBenefits": {
      "adicionalPericulosidade": "30% sobre salário",
      "adicionalInsalubridadePercentual": "20%",
      "adicionalInsalubridadeDescricao": "Adicional conforme NR-15",
      "adicionalSobreaviso": "1/3 sobre salário",
      "adicionalProntidao": "1/3 sobre salário",
      "adicionalNoturnoPercentual": "20%",
      "adicionalNoturnoDescricao": "Adicional noturno conforme legislação"
    },
    "transportation": {
      "valeTransporte": {
        "descontoAplicado": "6% do salário",
        "descricaoValeTransporte": "Vale transporte conforme lei 7418/85"
      },
      "auxilioTransporte": "R$ 200,00",
      "descontoAuxilioTransporte": "Não há desconto",
      "descricaoAuxilioTransporte": "Auxílio transporte para funcionários",
      "fretado": "Sim",
      "descontoFretado": "Não há desconto",
      "descricaoFretado": "Transporte fretado fornecido pela empresa"
    },
    "seniorityBonus": {
      "aplicaAnuenio": "Sim",
      "valorAnuenio": "R$ 500,00",
      "descricaoAnuenio": "Anuênio anual para funcionários com mais de 1 ano"
    },
    "workplaceConditions": {
      "condicoesTrabalho": "Ambiente climatizado",
      "teletrabalhoHomeOffice": "Permitido",
      "treinamentos": "Fornecidos pela empresa"
    },
    "disciplinaryProcedures": {
      "multasPenalidades": "Conforme legislação",
      "projecaoAviso": "Conforme legislação",
      "trintidio": "Conforme legislação",
      "multaEncerramentoContratoTempoServico": "Conforme legislação",
      "multaEncerramentoContratoParada": "Conforme legislação"
    },
    "unionContributions": {
      "aplicaContribuicaoSindicalEmpregado": "Sim",
      "valorContribuicaoSindicalEmpregado": "R$ 50,00",
      "descricaoContribuicaoSindicalEmpregado": "Contribuição sindical do empregado",
      "aplicaContribuicaoPatronal": "Sim",
      "valorContribuicaoPatronal": "R$ 100,00",
      "descricaoContribuicaoPatronal": "Contribuição patronal",
      "aplicaContribuicaoPatronalEducativa": "Sim",
      "valorContribuicaoPatronalEducativa": "R$ 75,00",
      "descricaoContribuicaoPatronalEducativa": "Contribuição patronal educativa"
    }
  },
  "statusRegistro": "ATIVO",
  "dataCriacao": "2024-01-01T10:00:00",
  "usuarioCriacao": "admin"
}
```

**Possíveis Erros:**
- 404: ACT/CCT não encontrada
- 500: Erro interno do servidor

**Validações dos endpoints de listagem:**

1. O sindicato deve existir no sistema
2. Usuários e administradores podem listar contratos
3. Filtro por tipo é opcional e aceita ACT ou CCT

---

## 8. Atualização de Contratos Trabalhistas (ACT/CCT)

### 8.1 Atualizar Contrato Trabalhista

**Endpoint:** `PATCH /api/unions/{unionId}/labor-contracts/{contractId}`

**Descrição:** Atualiza um contrato trabalhista existente. Apenas os campos fornecidos serão atualizados.

**Autorização:** `ROLE_ADMINISTRADOR`

**Parâmetros da URL:**
- `unionId` (String): ID do sindicato
- `contractId` (String): ID do contrato trabalhista

**Corpo da Requisição (multipart/form-data):**
```bash
# Campos básicos (opcionais - apenas os fornecidos serão atualizados)
-F "tipoInstrumento=ACT"
-F "numeroRegistro=123456"
-F "numeroSolicitacao=SOL-2024-001"
-F "numeroIdentificacaoInterno=ACT-2024-002"
-F "nomeInstrumento=Acordo Coletivo de Trabalho Atualizado"
-F "dataInicioVigencia=2024-02-01"
-F "dataFimVigencia=2024-12-31"
-F "dataBase=2024-01-01"
-F "abrangenciaTerritorial=Estadual"
-F "ufPrincipal=SP"
-F "ufPrincipal=RJ"
-F "municipio=São Paulo"
-F "municipio=Rio de Janeiro"
-F "categoriaPrincipalCBO=1114"
-F "descricaoResumidaCategorias=Profissionais da área de tecnologia"
-F "situacaoMTE=Aprovado"

# Campos de direitos trabalhistas (opcionais)
-F "pisoSalarial=R$ 1.600,00"
-F "reajusteSalarial=6% anual"
-F "horasExtras=60% sobre salário"
-F "valeAlimentacao=R$ 600,00"
-F "pprPlr=Participação nos lucros e resultados"

# Anexos (opcionais)
-F "arquivosInstrumento=@documento_atualizado.pdf"
```

**Resposta de Sucesso (200 OK):**
```json
{
  "id": "contrato123",
  "tipoInstrumento": "ACT",
  "numeroIdentificacaoInterno": "ACT-2024-002",
  "nomeInstrumento": "Acordo Coletivo de Trabalho Atualizado",
  "sindicatoTrabalhadoresId": "68cd2d492329c37e18b00944",
  "nomeSindicato": "Sindicato dos Trabalhadores em Tecnologia",
  "siglaSindicato": "STT",
  "dataInicioVigencia": "2024-02-01",
  "dataFimVigencia": "2024-12-31",
  "statusRegistro": "ATIVO",
  "pisoSalarial": "R$ 1.600,00",
  "reajusteSalarial": "6% anual",
  "horasExtras": "60% sobre salário",
  "valeAlimentacao": "R$ 600,00",
  "pprPlr": "Participação nos lucros e resultados"
}
```

**Respostas de Erro:**

- **400 Bad Request:** Dados inválidos ou contrato não encontrado
- **403 Forbidden:** Usuário sem permissão
- **500 Internal Server Error:** Erro interno do servidor

**Validações do endpoint de atualização:**

1. O contrato deve existir no sistema
2. Apenas contratos ativos podem ser editados
3. Se um novo número de identificação interno for fornecido, deve ser único
4. Apenas os campos fornecidos serão atualizados
5. Arquivos anexos substituem completamente os anexos anteriores

**⚠️ Importante sobre direitos trabalhistas (laborRights):**

O sistema realiza um **merge profundo** dos direitos trabalhistas, preservando todos os campos não enviados:

- Se você enviar apenas `pisoSalarial`, todos os outros campos de `laborRights` (como `valeAlimentacao`, `horasExtras`, etc.) **serão preservados**
- O merge funciona em múltiplos níveis: campos simples, DTOs aninhados e objetos dentro de DTOs
- Exemplo: ao atualizar apenas `planoSaude.cobertura`, os outros campos de `planoSaude` e todos os outros benefícios são mantidos
- Para remover um valor, envie explicitamente `null` ou string vazia para aquele campo específico

---

## 9. Cadastro de Aditivos em Contratos Trabalhistas (ACT/CCT)

### 9.1 Criar Aditivo de Contrato Trabalhista

**Endpoint:** `POST /api/unions/{unionId}/labor-contracts/{contractId}/addendums`

**Descrição:** Cria um aditivo para um contrato trabalhista existente. Os campos obrigatórios são tipo, data de inclusão, link e status. Todos os outros campos de direitos trabalhistas são opcionais. Além disso, o cadastro de aditivos aceita os **mesmos campos** do cadastro de ACT/CCT, utilizando os **mesmos nomes** e formatos.

**Autorização:** `ROLE_ADMINISTRADOR`

**Parâmetros da URL:**
- `unionId` (String): ID do sindicato
- `contractId` (String): ID do contrato trabalhista

**Corpo da Requisição (multipart/form-data):**

**Campos Obrigatórios:**

| Campo | Tipo | Descrição | Validação |
|-------|------|-----------|-----------|
| tipo | Enum | Tipo do instrumento | ACT ou CCT |
| titulo | String | Título do aditivo | Não pode estar vazio |
| apelido | String(100) | Apelido/nome curto (opcional) | Nome curto para facilitar identificação |
| dataInclusao | Date | Data de inclusão do aditivo | Data válida |
| dataInicio | Date | Data de início da vigência | Deve ser anterior à data de fim |
| dataFim | Date | Data de fim da vigência | Deve ser posterior à data de início |
| link | String | Link para o aditivo | URL válida |
| status | String | Status do aditivo | Dropdown (APROVADO, PENDENTE, REJEITADO) |
| arquivosAnexos | File Upload | Arquivo anexo | **Pelo menos um arquivo obrigatório** |

**Campos Adicionais (iguais ao ACT/CCT) — opcionais no aditivo:**

- `apelido` (String(100)) - Apelido/nome curto
- `tipoInstrumento` (Enum: ACT, CCT)
- `numeroRegistro` (String)
- `numeroSolicitacao` (String)
- `sindicatoTrabalhadoresId` (String)
- `empresasSignatarias` (List<String>)
- `dataInicioVigencia` (Date)
- `dataFimVigencia` (Date)
- `dataBase` (Date)
- `abrangenciaTerritorial` (String)
- `ufPrincipal` (List<String>)
- `municipio` (List<String>)
- `municipiosAbrangidos` (List<String>)
- `estadosAdicionais` (List<String>)
- `observacoesTerritoriais` (String)
- `subcategoriaCBO` (String)
- `funcoesEspecificas` (List<String>)
- `excecoesInclusoes` (String)
- `situacaoMTE` (String)
- `arquivosInstrumento` (List<File Upload>)
- `linkAnexo` (String/URL)
- `resumoIA` (String)

Exemplo (multipart/form-data) com campos adicionais do ACT/CCT:

```bash
-F "tipoInstrumento=ACT" \
-F "numeroRegistro=ACT-2024-001" \
-F "numeroSolicitacao=SOL-12345" \
-F "sindicatoTrabalhadoresId=68cd2d492329c37e18b00944" \
-F "empresasSignatarias=Empresa ABC Ltda" \
-F "empresasSignatarias=Empresa XYZ S.A." \
-F "dataInicioVigencia=2024-01-01" \
-F "dataFimVigencia=2024-12-31" \
-F "dataBase=2024-01-01" \
-F "abrangenciaTerritorial=Estadual" \
-F "ufPrincipal=SP" \
-F "municipio=Sao Paulo" \
-F "municipiosAbrangidos=Guarulhos" \
-F "estadosAdicionais=RJ" \
-F "observacoesTerritoriais=Observação exemplo" \
-F "subcategoriaCBO=1114" \
-F "funcoesEspecificas=Analista" \
-F "excecoesInclusoes=Isenções específicas" \
-F "situacaoMTE=Aprovado" \
-F "arquivosInstrumento=@instrumento.pdf" \
-F "linkAnexo=https://exemplo.com/anexo" \
-F "resumoIA=Resumo gerado pela IA"
```

**Campos Opcionais - Direitos Trabalhistas:**

### Exemplos de requisição (multipart/form-data)

Exemplo mínimo (somente campos obrigatórios):

```bash
curl -X POST "http://localhost:8080/api/unions/{unionId}/labor-contracts/{contractId}/addendums" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: multipart/form-data" \
  -F "tipo=ACT" \
  -F "titulo=Aditivo Salarial 2025" \
  -F "dataInclusao=2025-10-06" \
  -F "dataInicio=2025-10-06" \
  -F "dataFim=2025-10-31" \
  -F "link=https://exemplo.com/aditivos/123" \
  -F "status=APROVADO" \
  -F "arquivosAnexos=@C:/caminho/arquivo.pdf"
```

Exemplo com direitos trabalhistas via JSON (opcional) e campos adicionais do ACT/CCT (opcionais):

```bash
curl -X POST "http://localhost:8080/api/unions/{unionId}/labor-contracts/{contractId}/addendums" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: multipart/form-data" \
  -F "tipo=CCT" \
  -F "titulo=Aditivo de Jornada" \
  -F "dataInclusao=2025-10-20" \
  -F "dataInicio=2025-10-21" \
  -F "dataFim=2025-12-31" \
  -F "link=https://exemplo.com/aditivos/456" \
  -F "status=APROVADO" \
  -F "arquivosAnexos=@C:/caminho/aditivo.pdf" \
  -F 'laborRightsJson={
    "salaryBenefits": { "pisoSalarial": "R$ 1.800,00", "reajusteSalarial": "6% anual" },
    "workSchedule": { "horasExtras": "60% sobre salário" }
  }' \
  -F "numeroRegistro=564655646" \
  -F "numeroSolicitacao=5646546546" \
  -F "dataInicioVigencia=2025-10-06" \
  -F "dataFimVigencia=2025-10-31" \
  -F "dataBase=2025-10-20" \
  -F "abrangenciaTerritorial=Nacional" \
  -F "ufPrincipal=AC" -F "ufPrincipal=BA" \
  -F "municipio=ACRELÂNDIA" -F "municipio=ARAÇÁS" -F "municipio=BRUMADO" \
  -F "excecoesInclusoes=Texto livre" \
  -F "situacaoMTE=Ativo" \
  -F "linkAnexo=https://exemplo.com/act-cct/arquivo" \
  -F "arquivosInstrumento=@C:/caminho/instrumento.pdf"
```

Observações:
- Utilize o campo `tipo` (ACT ou CCT). O campo `tipoInstrumento` não é utilizado no aditivo.
- Envie datas no formato `YYYY-MM-DD`.
- Para listas, envie múltiplos `-F` com a mesma chave (ex.: `ufPrincipal`, `municipio`).
- Pelo menos um arquivo deve ser enviado em `arquivosAnexos`.

Todos os campos de direitos trabalhistas são opcionais nos aditivos. Utilize a mesma estrutura `laborRights` dos contratos principais:

```bash
# Salários e Benefícios
-F "laborRights.salaryBenefits.pisoSalarial=R$ 1.700,00" \
-F "laborRights.salaryBenefits.reajusteSalarial=7% anual" \
-F "laborRights.salaryBenefits.decimoTerceiroSalario=1/12 avos" \
-F "laborRights.salaryBenefits.pprPlr=Participação nos lucros e resultados" \
-F "laborRights.salaryBenefits.remuneracaoProdutividade=R$ 200,00/mês" \

# Jornada de Trabalho
-F "laborRights.workSchedule.jornadaTrabalho=8 horas diárias" \
-F "laborRights.workSchedule.horasExtras=60% sobre salário" \
-F "laborRights.workSchedule.bancoHoras=Acúmulo de horas extras" \
-F "laborRights.workSchedule.adicionalNoturno=20% sobre salário" \
-F "laborRights.workSchedule.intervalos=15 min manhã, 1h almoço" \
-F "laborRights.workSchedule.descansoSemanal=24h consecutivas" \
-F "laborRights.workSchedule.escalasRevezamento=6x1" \
-F "laborRights.workSchedule.trabalhoFinaisSemana=Adicional 100%" \
-F "laborRights.workSchedule.regimeSobreaviso=Regulamentado" \
-F "laborRights.workSchedule.trabalhoIntermitente=Não aplicável" \

# Folgas e Licenças
-F "laborRights.timeOffBenefits.ferias=30 dias corridos" \
-F "laborRights.timeOffBenefits.licencas=Médica, maternidade, paternidade" \
-F "laborRights.timeOffBenefits.avisoPrevio=30 dias" \
-F "laborRights.timeOffBenefits.estabilidade=12 meses após admissão" \
-F "laborRights.timeOffBenefits.aleitamentoMaterno=2 pausas de 30 min" \

# Benefícios - Plano de Saúde
-F "laborRights.benefits.planoSaude.extensividade=Extensiva aos dependentes" \
-F "laborRights.benefits.planoSaude.cobertura=Quarto individual" \
-F "laborRights.benefits.planoSaude.abrangencia=Nacional" \
-F "laborRights.benefits.planoSaude.corteMaiorIdade=24 anos" \

# Benefícios - Seguro de Vida
-F "laborRights.benefits.seguroVida.valorPremio=R$ 50.000,00" \
-F "laborRights.benefits.seguroVida.extensividade=Extensiva aos dependentes" \

# Benefícios - Plano Odontológico
-F "laborRights.benefits.planoOdontologico.extensividade=Extensiva aos dependentes" \
-F "laborRights.benefits.planoOdontologico.abrangencia=Nacional" \
-F "laborRights.benefits.planoOdontologico.dataCorte=31/12" \
-F "laborRights.benefits.planoOdontologico.corteMaiorIdade=24 anos" \

# Saúde e Segurança
-F "laborRights.healthSafety.equipamentosProtecao=Fornecidos pela empresa" \
-F "laborRights.healthSafety.examesMedicos=Admissional, periódico, demissional" \
-F "laborRights.healthSafety.adicionalPericulosidade=30%" \
-F "laborRights.healthSafety.adicionalInsalubridade=Grau máximo" \

# Transporte e Alimentação
-F "laborRights.transportationBenefits.valeTransporte=6% do salário" \
-F "laborRights.transportationBenefits.valeAlimentacao=R$ 700,00" \

# Condições de Trabalho
-F "laborRights.workplaceConditions.condicoesTrabalho=Ambiente salubre" \
-F "laborRights.workplaceConditions.controlePonto=Registro eletrônico" \
-F "laborRights.workplaceConditions.teletrabalhoHomeOffice=Regulamentado" \
-F "laborRights.workplaceConditions.treinamentos=Capacitação obrigatória" \
-F "laborRights.workplaceConditions.representacaoSindical=40h mensais" \

# Procedimentos Disciplinares
-F "laborRights.disciplinaryProcedures.procedimentosDisciplinares=Processo administrativo" \
-F "laborRights.disciplinaryProcedures.multasPenalidades=Conforme legislação" \
-F "laborRights.disciplinaryProcedures.projecaoAviso=30 dias" \
-F "laborRights.disciplinaryProcedures.trintidio=Sim" \
-F "laborRights.disciplinaryProcedures.multaRescisoria=40% FGTS" \

# Previdência Social
-F "laborRights.socialSecurity.previdenciaComplementar=Opcional" \
-F "laborRights.socialSecurity.encargosSociaisAdicionais=Conforme legislação" \
-F "laborRights.socialSecurity.programaSeguroEmprego=Incluso" \

# Políticas de Inclusão
-F "laborRights.inclusionPolicies.deficientesFisicos=Reserva de vagas" \
-F "laborRights.inclusionPolicies.planoCargosSalarios=Evolutivo"
```
-F "multasPenalidades=Por descumprimento"
-F "encargosSociaisAdicionais=Conforme legislação"
-F "regimeSobreaviso=Remuneração diferenciada"
-F "trabalhoIntermitente=Regulamentado"
-F "remuneracaoProdutividade=Por metas"
-F "programaSeguroEmprego=Estabilidade temporária"

# Anexos (opcionais)
-F "arquivosAnexos=@aditivo_documento.pdf"
```

**Resposta de Sucesso (201 CREATED):**
```json
{
  "id": "aditivo123",
  "contratoTrabalhistaId": "contrato123",
  "tipo": "ACT",
  "titulo": "Aditivo Salarial 2024",
  "dataInclusao": "2024-03-01",
  "dataInicio": "2024-03-01",
  "dataFim": "2024-12-31",
  "link": "https://exemplo.com",
  "status": "APROVADO",
  "laborRights": {
    "salaryBenefits": {
      "pisoSalarial": "R$ 1.700,00",
      "reajusteSalarial": "7% anual",
      "pprPlr": "Participação nos lucros e resultados"
    },
    "workSchedule": {
      "horasExtras": "60% sobre salário"
    },
    "salaryBenefitsExtra": {
      "valeAlimentacao": "R$ 700,00"
    }
  },
  "dataCriacao": "2024-01-15T10:30:00",
  "usuarioCriacao": "admin.usuario",
  "statusRegistro": "ATIVO"
}
```

**Respostas de Erro:**

- **400 Bad Request:** Dados inválidos ou contrato não encontrado
- **403 Forbidden:** Usuário sem permissão
- **500 Internal Server Error:** Erro interno do servidor

**Validações do endpoint de aditivos:**

1. O contrato trabalhista deve existir e estar ativo
2. **Tipo deve ser 'ACT' ou 'CCT'** (validado automaticamente pelo enum TipoInstrumento)
3. Título é obrigatório
4. Data de inclusão é obrigatória
5. Data de início da vigência é obrigatória
6. Data de fim da vigência é obrigatória
7. Data de início deve ser anterior à data de fim
8. Link é obrigatório
9. Status é obrigatório
10. Pelo menos um arquivo anexo é obrigatório
11. Não pode existir aditivo com mesmo tipo e data para o mesmo contrato
12. **Validação de enum:** Apenas valores `ACT` e `CCT` são aceitos (case-sensitive)
13. Apenas administradores podem criar aditivos

---

### 9.2 Atualizar Aditivo de Contrato Trabalhista

**Endpoint:** `PATCH /api/unions/{unionId}/labor-contracts/{contractId}/addendums/{addendumId}`

**Descrição:** Atualiza um aditivo de contrato trabalhista existente. Apenas os campos fornecidos serão atualizados, preservando os demais dados.

**Autorização:** `ROLE_ADMINISTRADOR`

**Parâmetros da URL:**
- `unionId` (String): ID do sindicato
- `contractId` (String): ID do contrato trabalhista
- `addendumId` (String): ID do aditivo

**Corpo da Requisição (multipart/form-data):**

Todos os campos são opcionais - envie apenas os que deseja atualizar:

```bash
curl -X PATCH "http://localhost:8080/api/unions/{unionId}/labor-contracts/{contractId}/addendums/{addendumId}" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: multipart/form-data" \
  -F "titulo=Aditivo Salarial 2024 - Atualizado" \
  -F "dataInicio=2024-04-01" \
  -F "dataFim=2024-12-31" \
  -F "status=APROVADO" \
  -F 'laborRightsJson={"salary":{"pisoSalarial":"R$ 1.900,00"}}'
```

**Campos Atualizáveis:**

Campos básicos do aditivo:
- `tipo` (Enum: ACT, CCT)
- `titulo` (String)
- `dataInclusao` (Date)
- `dataInicio` (Date)
- `dataFim` (Date)
- `link` (String/URL)
- `status` (String)

Campos adicionais (iguais ao ACT/CCT):
- `tipoInstrumento`, `numeroRegistro`, `numeroSolicitacao`, `sindicatoTrabalhadoresId`, `empresasSignatarias`, `dataInicioVigencia`, `dataFimVigencia`, `dataBase`, `abrangenciaTerritorial`, `ufPrincipal`, `municipio`, `municipiosAbrangidos`, `estadosAdicionais`, `observacoesTerritoriais`, `subcategoriaCBO`, `funcoesEspecificas`, `excecoesInclusoes`, `situacaoMTE`, `linkAnexo`, `resumoIA`

Direitos trabalhistas:
- `laborRightsJson` (JSON String) - Todos os campos de direitos trabalhistas

Anexos:
- `arquivosAnexos` (File Upload) - Substituem completamente os anexos anteriores se fornecidos

**Exemplo de atualização de múltiplos campos:**

```bash
curl -X PATCH "http://localhost:8080/api/unions/68cd2d492329c37e18b00944/labor-contracts/contrato123/addendums/addendum456" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: multipart/form-data" \
  -F "titulo=Aditivo Salarial 2024 - Revisão Final" \
  -F "dataFim=2025-03-31" \
  -F "status=APROVADO" \
  -F 'laborRightsJson={
    "salary": {
      "pisoSalarial": "R$ 2.000,00",
      "tabelaSalarial": "Tabela C"
    },
    "benefits": {
      "valeAlimentacao": "R$ 700,00"
    }
  }' \
  -F "arquivosAnexos=@aditivo_revisado.pdf"
```

**Resposta de Sucesso (200 OK):**
```json
{
  "id": "addendum456",
  "contratoTrabalhistaId": "contrato123",
  "tipo": "ACT",
  "titulo": "Aditivo Salarial 2024 - Revisão Final",
  "dataInclusao": "2024-03-01",
  "dataInicio": "2024-03-01",
  "dataFim": "2025-03-31",
  "link": "https://exemplo.com/aditivo",
  "status": "APROVADO",
  "laborRights": {
    "salary": {
      "pisoSalarial": "R$ 2.000,00",
      "tabelaSalarial": "Tabela C"
    },
    "benefits": {
      "valeAlimentacao": "R$ 700,00",
      "valeRefeicao": "R$ 35,00 por dia"
    }
  },
  "arquivosAnexos": [
    {
      "id": "file-uuid-123",
      "nome": "aditivo_revisado.pdf",
      "file": {
        "uri": "labor_contracts/addendums/file-uuid-123-1696428000000",
        "extension": "pdf",
        "mimeType": "application/pdf"
      }
    }
  ],
  "dataCriacao": "2024-03-01T10:00:00",
  "usuarioCriacao": "admin.usuario"
}
```

**Respostas de Erro:**

- **400 Bad Request:** Dados inválidos ou aditivo não encontrado
- **403 Forbidden:** Usuário sem permissão
- **500 Internal Server Error:** Erro interno do servidor

**Validações do endpoint de atualização:**

1. O aditivo deve existir no sistema
2. Se novas datas forem fornecidas, data de início deve ser anterior à data de fim
3. Apenas os campos fornecidos serão atualizados
4. Arquivos anexos substituem completamente os anexos anteriores se fornecidos

**⚠️ Importante sobre direitos trabalhistas (laborRights) em aditivos:**

O sistema realiza um **merge profundo** dos direitos trabalhistas, exatamente como na atualização de contratos:

- Se você enviar apenas `pisoSalarial`, todos os outros campos de `laborRights` (como `valeAlimentacao`, `horasExtras`, etc.) **serão preservados**
- O merge funciona em múltiplos níveis: campos simples, DTOs aninhados e objetos dentro de DTOs
- Exemplo: ao atualizar apenas `planoSaude.cobertura`, os outros campos de `planoSaude` e todos os outros benefícios são mantidos
- Para remover um valor, envie explicitamente `null` ou string vazia para aquele campo específico

---

## 📋 Enum TipoInstrumento

### Definição
```java
public enum TipoInstrumento {
    ACT("Acordo Coletivo de Trabalho"),
    CCT("Convenção Coletiva de Trabalho");
}
```

### Uso na API
- **Campo:** `tipoInstrumento` (contratos trabalhistas)
- **Campo:** `tipo` (aditivos de contratos)
- **Valores aceitos:** `ACT`, `CCT`
- **Validação:** Automática pelo Spring Boot
- **Case-sensitive:** Sim (deve ser exatamente `ACT` ou `CCT`)

### Exemplos de Uso
```json
// Contrato trabalhista
{
  "tipoInstrumento": "ACT",
  "numeroIdentificacaoInterno": "ACT-2024-001"
}

// Aditivo de contrato
{
  "tipo": "CCT",
  "titulo": "Aditivo Salarial 2024"
}
```

### Validação de Erro
```json
// Erro: valor inválido
{
  "error": "Validation failed",
  "message": "tipoInstrumento: must be one of [ACT, CCT]"
}
```

---

## 🔄 Guia de Migração

### Para Desenvolvedores Frontend

**Antes (String):**
```javascript
// Validação manual necessária
if (tipoInstrumento && tipoInstrumento.trim() !== 'ACT' && tipoInstrumento.trim() !== 'CCT') {
  throw new Error('Tipo inválido');
}

// Envio para API
const data = {
  tipoInstrumento: tipoInstrumento.trim().toUpperCase()
};
```

**Depois (Enum):**
```javascript
// Validação automática pelo Spring Boot
const data = {
  tipoInstrumento: 'ACT' // ou 'CCT' - case-sensitive
};

// Validação no frontend (opcional)
const validTypes = ['ACT', 'CCT'];
if (!validTypes.includes(tipoInstrumento)) {
  throw new Error('Tipo deve ser ACT ou CCT');
}
```

### Para Desenvolvedores Backend

**Antes (String):**
```java
// Validação manual
if (request.getTipoInstrumento() == null || request.getTipoInstrumento().trim().isEmpty()) {
    throw new RuntimeException("Tipo obrigatório");
}
if (!"ACT".equals(request.getTipoInstrumento().trim()) && !"CCT".equals(request.getTipoInstrumento().trim())) {
    throw new RuntimeException("Tipo inválido");
}
```

**Depois (Enum):**
```java
// Validação automática
@NotNull
private TipoInstrumento tipoInstrumento;

// Uso direto
if (request.getTipoInstrumento() == TipoInstrumento.ACT) {
    // Lógica para ACT
}
```

### Boas Práticas Introduzidas

- `@Validated` nas classes de serviço; `@Valid` nos parâmetros dos métodos
- DTOs com anotações de validação (`@NotNull`, `@NotBlank`, `@Email`, `@AssertTrue`)
- Mapeadores com MapStruct e `uses = { LaborRightsMapper }` para campos complexos
- Imports padronizados; remoção de `System.out.println` e debugs

### Breaking Changes

1. **Validação mais rigorosa**: Apenas `ACT` e `CCT` são aceitos
2. **Case-sensitive**: Deve ser exatamente `ACT` ou `CCT` (não `act` ou `Act`)
3. **Validação automática**: Spring Boot valida automaticamente
4. **Melhor performance**: Comparações de enum são mais rápidas

---

## 💰 Contribuições Sindicais - Guia Completo

### Visão Geral

O módulo Union Registration inclui campos específicos para gerenciar as contribuições sindicais nos contratos trabalhistas (ACT/CCT). Esses campos permitem definir se cada tipo de contribuição se aplica e qual o valor a ser pago.

### Tipos de Contribuições

#### 1. Contribuição Sindical do Empregado
- **Campo aplica**: `aplicaContribuicaoSindicalEmpregado`
- **Campo valor**: `valorContribuicaoSindicalEmpregado`
- **Campo descrição**: `descricaoContribuicaoSindicalEmpregado`
- **Descrição**: Contribuição descontada do salário do trabalhador
- **Valores aceitos**: "Sim" ou "Não" para aplicação
- **Exemplos de valor**: "R$ 50,00", "1% do salário", "R$ 50,00 + 1%"
- **Exemplos de descrição**: "Contribuição mensal obrigatória conforme estatuto"

#### 2. Contribuição Patronal
- **Campo aplica**: `aplicaContribuicaoPatronal`
- **Campo valor**: `valorContribuicaoPatronal`
- **Campo descrição**: `descricaoContribuicaoPatronal`
- **Descrição**: Contribuição paga pelo empregador para o sindicato
- **Valores aceitos**: "Sim" ou "Não" para aplicação
- **Exemplos de valor**: "R$ 200,00", "2% da folha", "R$ 200,00 + 2%"
- **Exemplos de descrição**: "Taxa anual para manutenção das atividades sindicais"

#### 3. Contribuição Patronal Educativa
- **Campo aplica**: `aplicaContribuicaoPatronalEducativa`
- **Campo valor**: `valorContribuicaoPatronalEducativa`
- **Campo descrição**: `descricaoContribuicaoPatronalEducativa`
- **Descrição**: Contribuição paga pelo empregador para programas educacionais
- **Valores aceitos**: "Sim" ou "Não" para aplicação
- **Exemplos de valor**: "R$ 100,00", "0,5% da folha", "R$ 100,00 + 0,5%"
- **Exemplos de descrição**: "Contribuição para programas de capacitação profissional"

### Exemplos de Uso

#### Exemplo 1: Todas as Contribuições Aplicáveis
```bash
-F "laborRights.unionContributions.aplicaContribuicaoSindicalEmpregado=Sim" \
-F "laborRights.unionContributions.valorContribuicaoSindicalEmpregado=R$ 50,00" \
-F "laborRights.unionContributions.descricaoContribuicaoSindicalEmpregado=Contribuição mensal obrigatória conforme estatuto" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronal=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronal=R$ 200,00" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronal=Taxa anual para manutenção das atividades sindicais" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronalEducativa=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronalEducativa=R$ 100,00" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronalEducativa=Contribuição para programas de capacitação profissional"
```

#### Exemplo 2: Apenas Contribuição Patronal
```bash
-F "laborRights.unionContributions.aplicaContribuicaoSindicalEmpregado=Não" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronal=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronal=2% da folha" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronal=Taxa anual para manutenção das atividades sindicais" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronalEducativa=Não"
```

#### Exemplo 3: Valores Percentuais
```bash
-F "laborRights.unionContributions.aplicaContribuicaoSindicalEmpregado=Sim" \
-F "laborRights.unionContributions.valorContribuicaoSindicalEmpregado=1% do salário" \
-F "laborRights.unionContributions.descricaoContribuicaoSindicalEmpregado=Contribuição mensal obrigatória conforme estatuto" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronal=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronal=2% da folha" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronal=Taxa anual para manutenção das atividades sindicais" \
-F "laborRights.unionContributions.aplicaContribuicaoPatronalEducativa=Sim" \
-F "laborRights.unionContributions.valorContribuicaoPatronalEducativa=0,5% da folha" \
-F "laborRights.unionContributions.descricaoContribuicaoPatronalEducativa=Contribuição para programas de capacitação profissional"
```

### Resposta JSON

```json
{
  "laborRights": {
    "unionContributions": {
      "aplicaContribuicaoSindicalEmpregado": "Sim",
      "valorContribuicaoSindicalEmpregado": "R$ 50,00",
      "descricaoContribuicaoSindicalEmpregado": "Contribuição mensal obrigatória conforme estatuto",
      "aplicaContribuicaoPatronal": "Sim",
      "valorContribuicaoPatronal": "R$ 200,00",
      "descricaoContribuicaoPatronal": "Taxa anual para manutenção das atividades sindicais",
      "aplicaContribuicaoPatronalEducativa": "Sim",
      "valorContribuicaoPatronalEducativa": "R$ 100,00",
      "descricaoContribuicaoPatronalEducativa": "Contribuição para programas de capacitação profissional"
    }
  }
}
```

### Validações

- Todos os campos são **opcionais**
- Campos "aplica" devem ser "Sim" ou "Não" (case-sensitive)
- Campos "valor" aceitam texto livre (valores monetários, percentuais ou mistos)
- Campos "descrição" aceitam texto livre para detalhamento das contribuições
- Não há validação de formato específico para os valores (flexibilidade para diferentes convenções)

### Integração com Contratos

As contribuições sindicais são integradas automaticamente em:
- ✅ **Contratos Trabalhistas** (ACT/CCT)
- ✅ **Aditivos de Contratos**
- ✅ **Respostas da API**
- ✅ **Documentação completa**

---

## 🎖️ Anuênio - Guia Completo

### Visão Geral

O módulo Union Registration inclui campos específicos para gerenciar o anuênio (bonificação por tempo de serviço) nos contratos trabalhistas (ACT/CCT). Esses campos permitem definir se o anuênio se aplica, qual o valor a ser pago e uma descrição detalhada.

### Campos do Anuênio

#### 1. Aplicação do Anuênio
- **Campo**: `aplicaAnuenio`
- **Descrição**: Define se o anuênio se aplica ao contrato
- **Valores aceitos**: "Sim" ou "Não"

#### 2. Valor do Anuênio
- **Campo**: `valorAnuenio`
- **Descrição**: Valor a ser pago como bonificação
- **Exemplos**: "R$ 500,00", "1% do salário", "R$ 500,00 + 1%"

#### 3. Descrição do Anuênio
- **Campo**: `descricaoAnuenio`
- **Descrição**: Detalhamento sobre o anuênio
- **Exemplos**: "Bonificação anual por tempo de serviço", "Prêmio por antiguidade"

### Exemplos de Uso

#### Exemplo 1: Anuênio Aplicável
```bash
-F "laborRights.seniorityBonus.aplicaAnuenio=Sim" \
-F "laborRights.seniorityBonus.valorAnuenio=R$ 500,00" \
-F "laborRights.seniorityBonus.descricaoAnuenio=Bonificação anual por tempo de serviço"
```

#### Exemplo 2: Anuênio Percentual
```bash
-F "laborRights.seniorityBonus.aplicaAnuenio=Sim" \
-F "laborRights.seniorityBonus.valorAnuenio=1% do salário" \
-F "laborRights.seniorityBonus.descricaoAnuenio=Prêmio por antiguidade conforme estatuto"
```

#### Exemplo 3: Anuênio Não Aplicável
```bash
-F "laborRights.seniorityBonus.aplicaAnuenio=Não"
```

### Resposta JSON

```json
{
  "laborRights": {
    "seniorityBonus": {
      "aplicaAnuenio": "Sim",
      "valorAnuenio": "R$ 500,00",
      "descricaoAnuenio": "Bonificação anual por tempo de serviço"
    }
  }
}
```

### Validações

- Todos os campos são **opcionais**
- Campo "aplica" deve ser "Sim" ou "Não" (case-sensitive)
- Campo "valor" aceita texto livre (valores monetários, percentuais ou mistos)
- Campo "descrição" aceita texto livre para detalhamento do anuênio

### Integração com Contratos

O anuênio é integrado automaticamente em:
- ✅ **Contratos Trabalhistas** (ACT/CCT)
- ✅ **Aditivos de Contratos**
- ✅ **Respostas da API**
- ✅ **Documentação completa**

---

## 🚌 Transporte - Guia Completo

### Visão Geral

O módulo Union Registration inclui campos específicos para gerenciar os benefícios de transporte nos contratos trabalhistas (ACT/CCT). Esses campos permitem definir diferentes tipos de transporte, seus valores, descontos e descrições detalhadas.

### Tipos de Transporte

#### 1. Vale Transporte (Lei 7418/85)
- **Campo desconto**: `valeTransporte.descontoAplicado`
- **Campo descrição**: `valeTransporte.descricaoValeTransporte`
- **Descrição**: Vale transporte conforme legislação específica
- **Exemplos de desconto**: "6% do salário", "R$ 50,00"
- **Exemplos de descrição**: "Vale transporte conforme lei 7418/85"

#### 2. Auxílio Transporte
- **Campo valor**: `auxilioTransporte`
- **Campo desconto**: `descontoAuxilioTransporte`
- **Campo descrição**: `descricaoAuxilioTransporte`
- **Descrição**: Auxílio transporte com valor definido
- **Exemplos de valor**: "R$ 200,00", "1% do salário"
- **Exemplos de desconto**: "6% do salário", "R$ 30,00"
- **Exemplos de descrição**: "Auxílio transporte para deslocamento"

#### 3. Fretado
- **Campo aplica**: `fretado`
- **Campo desconto**: `descontoFretado`
- **Campo descrição**: `descricaoFretado`
- **Descrição**: Transporte fretado pela empresa
- **Valores aceitos**: "Sim" ou "Não" para aplicação
- **Exemplos de desconto**: "6% do salário", "R$ 40,00"
- **Exemplos de descrição**: "Transporte fretado pela empresa"

### Exemplos de Uso

#### Exemplo 1: Todos os Tipos de Transporte
```bash
-F "laborRights.transportation.valeTransporte.descontoAplicado=6% do salário" \
-F "laborRights.transportation.valeTransporte.descricaoValeTransporte=Vale transporte conforme lei 7418/85" \
-F "laborRights.transportation.auxilioTransporte=R$ 200,00" \
-F "laborRights.transportation.descontoAuxilioTransporte=6% do salário" \
-F "laborRights.transportation.descricaoAuxilioTransporte=Auxílio transporte para deslocamento" \
-F "laborRights.transportation.fretado=Sim" \
-F "laborRights.transportation.descontoFretado=6% do salário" \
-F "laborRights.transportation.descricaoFretado=Transporte fretado pela empresa"
```

#### Exemplo 2: Apenas Vale Transporte
```bash
-F "laborRights.transportation.valeTransporte.descontoAplicado=6% do salário" \
-F "laborRights.transportation.valeTransporte.descricaoValeTransporte=Vale transporte conforme lei 7418/85"
```

#### Exemplo 3: Auxílio Transporte com Valores Percentuais
```bash
-F "laborRights.transportation.auxilioTransporte=1% do salário" \
-F "laborRights.transportation.descontoAuxilioTransporte=0,5% do salário" \
-F "laborRights.transportation.descricaoAuxilioTransporte=Auxílio transporte percentual"
```

#### Exemplo 4: Fretado Não Aplicável
```bash
-F "laborRights.transportation.fretado=Não"
```

### Resposta JSON

```json
{
  "laborRights": {
    "transportation": {
      "valeTransporte": {
        "descontoAplicado": "6% do salário",
        "descricaoValeTransporte": "Vale transporte conforme lei 7418/85"
      },
      "auxilioTransporte": "R$ 200,00",
      "descontoAuxilioTransporte": "6% do salário",
      "descricaoAuxilioTransporte": "Auxílio transporte para deslocamento",
      "fretado": "Sim",
      "descontoFretado": "6% do salário",
      "descricaoFretado": "Transporte fretado pela empresa"
    }
  }
}
```

### Validações

- Todos os campos são **opcionais**
- Campo "fretado" deve ser "Sim" ou "Não" (case-sensitive)
- Campos "valor" aceitam texto livre (valores monetários, percentuais ou mistos)
- Campos "desconto" aceitam texto livre (valores monetários, percentuais ou mistos)
- Campos "descrição" aceitam texto livre para detalhamento dos benefícios

### Integração com Contratos

Os benefícios de transporte são integrados automaticamente em:
- ✅ **Contratos Trabalhistas** (ACT/CCT)
- ✅ **Aditivos de Contratos**
- ✅ **Respostas da API**
- ✅ **Documentação completa**

---

## ⏰ Tipos de Escala - Guia Completo

### Visão Geral

O módulo Union Registration inclui campos específicos para gerenciar os tipos de escala de trabalho nos contratos trabalhistas (ACT/CCT). Esses campos permitem definir diferentes escalas de trabalho e incluir escalas personalizadas através do campo "outro".

### Tipos de Escala Disponíveis

#### 1. Escalas Padrão
- **escala5x2**: Escala 5 dias trabalhados, 2 dias de folga
- **escala6x1**: Escala 6 dias trabalhados, 1 dia de folga
- **escala12x36**: Escala 12 horas trabalhadas, 36 horas de descanso
- **escala4x4**: Escala 4 dias trabalhados, 4 dias de folga
- **escala7x7**: Escala 7 dias trabalhados, 7 dias de folga
- **escalaL5811**: Escala conforme Lei 5811/72

#### 2. Escala Personalizada
- **outro**: Campo para outros tipos de escala não padronizados
- **Descrição**: Permite definir escalas específicas conforme acordo coletivo

### Exemplos de Uso

#### Exemplo 1: Escalas Padrão
```bash
-F "laborRights.workShiftTypes.escala5x2=Aplicável" \
-F "laborRights.workShiftTypes.escala6x1=Aplicável" \
-F "laborRights.workShiftTypes.escala12x36=Não aplicável" \
-F "laborRights.workShiftTypes.escala4x4=Aplicável" \
-F "laborRights.workShiftTypes.escala7x7=Não aplicável" \
-F "laborRights.workShiftTypes.escalaL5811=Aplicável"
```

#### Exemplo 2: Escala Personalizada
```bash
-F "laborRights.workShiftTypes.outro=Escala 3x1 conforme acordo coletivo"
```

#### Exemplo 3: Múltiplas Escalas
```bash
-F "laborRights.workShiftTypes.escala5x2=Aplicável" \
-F "laborRights.workShiftTypes.escala6x1=Aplicável" \
-F "laborRights.workShiftTypes.outro=Escala 3x1 conforme acordo coletivo"
```

#### Exemplo 4: Apenas Escala Personalizada
```bash
-F "laborRights.workShiftTypes.outro=Escala especial 2x2 com folgas alternadas"
```

### Resposta JSON

```json
{
  "laborRights": {
    "workShiftTypes": {
      "escala5x2": "Aplicável",
      "escala6x1": "Aplicável",
      "escala12x36": "Não aplicável",
      "escala4x4": "Aplicável",
      "escala7x7": "Não aplicável",
      "escalaL5811": "Aplicável",
      "outro": "Escala 3x1 conforme acordo coletivo"
    }
  }
}
```

### Validações

- Todos os campos são **opcionais**
- Campos aceitam texto livre para flexibilidade
- Campo "outro" permite escalas personalizadas não padronizadas
- Não há validação de formato específico (flexibilidade para diferentes convenções)

### Integração com Contratos

Os tipos de escala são integrados automaticamente em:
- ✅ **Contratos Trabalhistas** (ACT/CCT)
- ✅ **Aditivos de Contratos**
- ✅ **Respostas da API**
- ✅ **Documentação completa**

---

## ⏱️ Tempo de Contrato - Guia Completo

### Visão Geral

O módulo Union Registration inclui campos específicos para gerenciar os tempos de contrato nos contratos trabalhistas (ACT/CCT). Esses campos permitem definir diferentes durações de contrato e incluir tempos personalizados através do campo "outro".

### Tipos de Tempo de Contrato Disponíveis

#### 1. Tempos Padrão
- **tempo30x30**: Tempo de contrato 30 dias trabalhados, 30 dias de descanso
- **tempo45x45**: Tempo de contrato 45 dias trabalhados, 45 dias de descanso
- **tempo30NaoRenovaveis**: Tempo de contrato 30 dias não renováveis

#### 2. Tempo Personalizado
- **outro**: Campo para outros tempos de contrato não padronizados
- **Descrição**: Permite definir tempos específicos conforme acordo coletivo

### Exemplos de Uso

#### Exemplo 1: Tempos Padrão
```bash
-F "laborRights.contractTime.tempo30x30=Aplicável" \
-F "laborRights.contractTime.tempo45x45=Aplicável" \
-F "laborRights.contractTime.tempo30NaoRenovaveis=Não aplicável"
```

#### Exemplo 2: Tempo Personalizado
```bash
-F "laborRights.contractTime.outro=Tempo de contrato 60x60 conforme acordo coletivo"
```

#### Exemplo 3: Múltiplos Tempos
```bash
-F "laborRights.contractTime.tempo30x30=Aplicável" \
-F "laborRights.contractTime.tempo45x45=Aplicável" \
-F "laborRights.contractTime.outro=Tempo de contrato 60x60 conforme acordo coletivo"
```

#### Exemplo 4: Apenas Tempo Personalizado
```bash
-F "laborRights.contractTime.outro=Tempo especial 90x30 com renovação automática"
```

### Resposta JSON

```json
{
  "laborRights": {
    "contractTime": {
      "tempo30x30": "Aplicável",
      "tempo45x45": "Aplicável",
      "tempo30NaoRenovaveis": "Não aplicável",
      "outro": "Tempo de contrato 60x60 conforme acordo coletivo"
    }
  }
}
```

### Validações

- Todos os campos são **opcionais**
- Campos aceitam texto livre para flexibilidade
- Campo "outro" permite tempos personalizados não padronizados
- Não há validação de formato específico (flexibilidade para diferentes convenções)

### Integração com Contratos

Os tempos de contrato são integrados automaticamente em:
- ✅ **Contratos Trabalhistas** (ACT/CCT)
- ✅ **Aditivos de Contratos**
- ✅ **Respostas da API**
- ✅ **Documentação completa**
