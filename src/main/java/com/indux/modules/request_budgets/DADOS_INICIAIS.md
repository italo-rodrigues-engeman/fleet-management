# Dados Iniciais - Módulo Request Budgets

Este documento contém exemplos de dados iniciais que devem ser cadastrados antes de criar orçamentos.

## Tipos de Budget

### 1. Rotina-Continuado
```json
{
  "nome": "Rotina-Continuado",
  "descricao": "Serviços de rotina com execução continuada ao longo do contrato"
}
```

### 2. Rotina-Sob Demanda
```json
{
  "nome": "Rotina-Sob Demanda",
  "descricao": "Serviços de rotina executados sob demanda conforme necessidade"
}
```

### 3. Spot
```json
{
  "nome": "Spot",
  "descricao": "Serviços pontuais e específicos"
}
```

### 4. Parada de Manutenção
```json
{
  "nome": "Parada de Manutenção",
  "descricao": "Serviços relacionados a paradas programadas de manutenção"
}
```

### 5. Guarda Chuva
```json
{
  "nome": "Guarda Chuva",
  "descricao": "Contrato guarda-chuva com múltiplos serviços"
}
```

### 6. Outros
```json
{
  "nome": "Outros",
  "descricao": "Outros tipos de serviços não categorizados"
}
```

## Características de Budget

### A - Venda de Mão de Obra
```json
{
  "codigo": "A",
  "descricao": "Venda de Mão de Obra (Diária / Mensal)"
}
```

### B - Fornecimento de Serviços
```json
{
  "codigo": "B",
  "descricao": "Fornecimento de serviços"
}
```

### C - Revenda de Peças
```json
{
  "codigo": "C",
  "descricao": "Revenda de Peças e Sobressalentes"
}
```

### D - Facilities
```json
{
  "codigo": "D",
  "descricao": "Facilities"
}
```

### E - Outros
```json
{
  "codigo": "E",
  "descricao": "Outros"
}
```

## Script de Inicialização

### Criar Tipos (executar via POST /budgets/tipos)

```bash
# Tipo 1 - Rotina-Continuado
curl -X POST http://localhost:8080/budgets/tipos \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Rotina-Continuado",
    "descricao": "Serviços de rotina com execução continuada ao longo do contrato"
  }'

# Tipo 2 - Rotina-Sob Demanda
curl -X POST http://localhost:8080/budgets/tipos \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Rotina-Sob Demanda",
    "descricao": "Serviços de rotina executados sob demanda conforme necessidade"
  }'

# Tipo 3 - Spot
curl -X POST http://localhost:8080/budgets/tipos \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Spot",
    "descricao": "Serviços pontuais e específicos"
  }'

# Tipo 4 - Parada de Manutenção
curl -X POST http://localhost:8080/budgets/tipos \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Parada de Manutenção",
    "descricao": "Serviços relacionados a paradas programadas de manutenção"
  }'

# Tipo 5 - Guarda Chuva
curl -X POST http://localhost:8080/budgets/tipos \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Guarda Chuva",
    "descricao": "Contrato guarda-chuva com múltiplos serviços"
  }'

# Tipo 6 - Outros
curl -X POST http://localhost:8080/budgets/tipos \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Outros",
    "descricao": "Outros tipos de serviços não categorizados"
  }'
```

### Criar Características (executar via POST /budgets/caracteristicas)

```bash
# Característica A
curl -X POST http://localhost:8080/budgets/caracteristicas \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": "A",
    "descricao": "Venda de Mão de Obra (Diária / Mensal)"
  }'

# Característica B
curl -X POST http://localhost:8080/budgets/caracteristicas \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": "B",
    "descricao": "Fornecimento de serviços"
  }'

# Característica C
curl -X POST http://localhost:8080/budgets/caracteristicas \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": "C",
    "descricao": "Revenda de Peças e Sobressalentes"
  }'

# Característica D
curl -X POST http://localhost:8080/budgets/caracteristicas \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": "D",
    "descricao": "Facilities"
  }'

# Característica E
curl -X POST http://localhost:8080/budgets/caracteristicas \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": "E",
    "descricao": "Outros"
  }'
```

## Exemplo Completo de Budget

Após cadastrar os tipos e características acima, você pode criar um budget completo:

```json
{
  "clienteId": 1,
  "mercadoId": 1,
  "setorId": 1,
  "solicitantes": [
    {
      "nome": "João Silva",
      "funcaoCargo": "Gerente de Operações",
      "telefone1": "11999999999",
      "telefone2": "1133333333",
      "email1": "joao.silva@empresa.com",
      "email2": "joao@empresa.com",
      "localizacao": "São Paulo - SP",
      "observacao": "Contato principal para questões técnicas"
    },
    {
      "nome": "Maria Santos",
      "funcaoCargo": "Diretora Comercial",
      "telefone1": "11988888888",
      "email1": "maria.santos@empresa.com",
      "localizacao": "São Paulo - SP",
      "observacao": "Responsável por aprovações comerciais"
    }
  ],
  "numeroOportunidade": "OPP-2024-001",
  "descricaoOportunidade": "Fornecimento de serviços de manutenção preventiva e corretiva em equipamentos industriais",
  "tempoContrato": "24 meses",
  "porteEstimado": 1500000.00,
  "dataAbertura": "2024-01-15",
  "representanteComercialId": "550e8400-e29b-41d4-a716-446655440000",
  "comissao": 5.5,
  "detalhesGerais": "Projeto inclui manutenção em 3 unidades fabris com equipe dedicada. Previsão de início: Março/2024.",
  "documentos": [
    {
      "tipo": "MD",
      "maisInformacoes": "Primeira reunião realizada em 10/01/2024",
      "descricao": "Memória de diálogo da reunião inicial com o cliente",
      "anexos": [
        "https://storage.example.com/md-opp-2024-001.pdf"
      ]
    },
    {
      "tipo": "PPU",
      "maisInformacoes": "Planilha de preços unitários versão 1.0",
      "descricao": "PPU com detalhamento de todos os serviços",
      "anexos": [
        "https://storage.example.com/ppu-opp-2024-001.xlsx"
      ]
    },
    {
      "tipo": "Habilitações necessárias",
      "descricao": "Documentos de habilitação técnica exigidos no edital",
      "anexos": [
        "https://storage.example.com/cert-iso-9001.pdf",
        "https://storage.example.com/cert-iso-14001.pdf"
      ]
    }
  ],
  "modalidadeConcorrencia": "Menor Preço",
  "tipoOportunidade": "{id_do_tipo_rotina_continuado}",
  "caracteristicasOportunidade": [
    "{id_caracteristica_A}",
    "{id_caracteristica_B}"
  ]
}
```

## Notas Importantes

1. **Substituir IDs**: Ao criar um budget, substitua `{id_do_tipo_rotina_continuado}`, `{id_caracteristica_A}`, etc. pelos IDs reais retornados ao criar os tipos e características.

2. **Modalidades de Concorrência**: Os valores aceitos são:
   - Menor Preço
   - Leilão
   - Maior Desconto
   - Outros

3. **Tipos de Documento**: Os valores aceitos são:
   - MD
   - PPU
   - SMS
   - Gerais
   - Habilitações necessárias

4. **Múltiplas Características**: Um budget pode ter múltiplas características selecionadas através do array `caracteristicasOportunidade`.

5. **Solicitantes**: É possível adicionar múltiplos solicitantes para cada budget.

6. **Anexos**: Cada documento pode ter múltiplos anexos (URLs).

