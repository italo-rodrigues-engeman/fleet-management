# Módulo de Contratos

Este módulo implementa a gestão de contratos seguindo a arquitetura limpa (Clean Architecture).

## Estrutura

### Domain Layer
- **Model**: Entidade `Contract` que representa a tabela `tb_contratos`
- **Repository**: Interface `ContractRepository` com métodos de consulta
- **Custom Repository**: Interface `ContractCustomRepository` para operações personalizadas
- **Exception**: `ContractNotFoundException` para tratamento de erros

### Application Layer
- **DTOs**: `ContractDTO` e `ContractFilterDTO` para transferência de dados
- **Use Cases**: 
  - `ListContractsUseCase`: Lista contratos com filtros
  - `GetContractByIdUseCase`: Busca contrato por ID
- **Mapper**: `ContractMapper` para conversão entre entidade e DTO

### Presentation Layer
- **Controller**: `ContractController` com endpoints REST

### Infrastructure Layer
- **Exception Handler**: `ContractExceptionHandler` para tratamento de exceções
- **Repository Implementation**: `ContractRepositoryImpl` implementa `ContractCustomRepository` para tratamento de valores NaN do PostgreSQL

## Endpoints

### GET /api/contracts
Lista todos os contratos com paginação e filtros opcionais.

**Parâmetros de consulta:**
- `cliente`: Filtra por nome do cliente
- `nomeProjeto`: Filtra por nome do projeto
- `regional`: Filtra por regional
- `ativo`: Filtra por status ativo/inativo
- `os`: Filtra por número da OS
- `codSap`: Filtra por código SAP
- `gestorInterno`: Filtra por gestor interno
- `gestorCliente`: Filtra por gestor do cliente
- `page`: Número da página (padrão: 0)
- `size`: Tamanho da página (padrão: 10)
- `sort`: Campo para ordenação (padrão: id,asc)

**Exemplo de resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "os": "OS001",
      "cliente": "PETROBRAS",
      "nomeProjeto": "MANUTENÇÃO EM BOMBA DE LAMA",
      "regional": "RN",
      "ativo": false,
      "valorContrato": 100000.00,
      "dataAssinatura": "1986-12-31",
      "icj": "5900.0124075.23.2"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "empty": false
}
```

**Nota:** A paginação segue o padrão Spring Data Page, retornando diretamente a entidade `Contract` em vez de um DTO personalizado, seguindo a mesma abordagem utilizada no módulo OCF.

### GET /api/contracts/{id}
Busca um contrato específico por ID.

**Exemplo de resposta:**
```json
{
  "id": 1,
  "os": "OS001",
  "osMega": "MEGA001",
  "anoOs": "1986",
  "cliente": "PETROBRAS",
  "codSap": "-",
  "dataAssinatura": "1986-12-31",
  "dataFim": null,
  "porcentagemReajuste": null,
  "mesReajuste": null,
  "ativo": false,
  "tipoAditivo": null,
  "regional": "RN",
  "gestorInternoContrato": null,
  "gestorClienteContrato": null,
  "valorContrato": null,
  "nomeProjeto": "MANUTENÇÃO EM BOMBA DE LAMA",
  "dataConhecida": false,
  "megaId": null,
  "filialId": null,
  "rateioId": null,
  "nomeCentroCustos": null,
  "descricaoEscopo": null,
  "idDisciplina": null,
  "idCliente": null,
  "gestorInternoCustos": null,
  "coordenadorContrato": null,
  "emailGestor": null,
  "emailCoordenador": null,
  "contatoGestor": null,
  "contatoCoordenador": null,
  "dataInicio": null,
  "nome": null,
  "clienteId": null,
  "plataformaId": null,
  "icj": "5900.0124075.23.2"
}
```

## Implementação da Paginação

A paginação no módulo de contracts segue o mesmo padrão utilizado no módulo OCF, utilizando diretamente o `Page<Contract>` do Spring Data em vez de um DTO personalizado. Isso garante consistência com outros módulos do sistema e aproveita todas as funcionalidades nativas do Spring Data.

### Mudanças Implementadas
- **Controller**: Retorna `ResponseEntity<Page<Contract>>` em vez de `PageResponseDTO`
- **Use Case**: Retorna `Page<Contract>` diretamente do repository
- **Repository**: Mantém a implementação personalizada para tratamento de valores NaN
- **DTOs**: Removido `PageResponseDTO` desnecessário

## Tratamento de Valores NaN

O módulo inclui tratamento especial para valores "NaN" (Not a Number) que podem estar presentes na tabela `tb_contratos` do PostgreSQL. A implementação `ContractRepositoryImpl` trata automaticamente esses valores, convertendo-os para `null` antes de retornar os dados.

### Problemas Resolvidos
- **Erro Original**: `org.postgresql.util.PSQLException: Valor inválido para tipo BigDecimal : NaN`
- **Solução**: Implementação personalizada que filtra valores "NaN" e os converte para `null`
- **Erro de Query**: `org.postgresql.util.PSQLException: ERRO: coluna "c.id" deve aparecer na cláusula GROUP BY`
- **Solução**: Separação das queries de dados e contagem, removendo ORDER BY da query de contagem

## Campos da Tabela

A entidade `Contract` mapeia todos os campos da tabela `tb_contratos`:

- `id`: ID único do contrato
- `os`: Número da OS
- `osMega`: Número da OS no sistema Mega
- `anoOs`: Ano da OS
- `cliente`: Nome do cliente
- `codSap`: Código SAP
- `dataAssinatura`: Data de assinatura do contrato
- `dataFim`: Data de fim do contrato
- `porcentagemReajuste`: Percentual de reajuste
- `mesReajuste`: Mês do reajuste
- `ativo`: Status ativo/inativo
- `tipoAditivo`: Tipo de aditivo
- `regional`: Regional
- `gestorInternoContrato`: Gestor interno do contrato
- `gestorClienteContrato`: Gestor do cliente
- `valorContrato`: Valor do contrato
- `nomeProjeto`: Nome do projeto
- `dataConhecida`: Se a data é conhecida
- `megaId`: ID no sistema Mega
- `filialId`: ID da filial
- `rateioId`: ID do rateio
- `nomeCentroCustos`: Nome do centro de custos
- `descricaoEscopo`: Descrição do escopo
- `idDisciplina`: ID da disciplina
- `idCliente`: ID do cliente
- `gestorInternoCustos`: Gestor interno de custos
- `coordenadorContrato`: Coordenador do contrato
- `emailGestor`: Email do gestor
- `emailCoordenador`: Email do coordenador
- `contatoGestor`: Contato do gestor
- `contatoCoordenador`: Contato do coordenador
- `dataInicio`: Data de início
- `nome`: Nome do contrato
- `clienteId`: ID do cliente
- `plataformaId`: ID da plataforma
- `icj`: Campo ICJ (opcional, String) 