# Configuração de Períodos de Atividade para Notificações Agendadas

## 📋 Visão Geral

Foi implementada uma funcionalidade para permitir que o usuário configure dias da semana e horários específicos onde o despachador de notificações agendadas deve ser habilitado. As notificações só serão enviadas durante os períodos de atividade configurados.

## 🏗️ Arquitetura Implementada

### Entidades e Modelos

#### `NotificationScheduleConfig`
- **Localização**: `com.indux.modules.ocf.domain.model.NotificationScheduleConfig`
- **Funcionalidades**:
  - Configuração de dias da semana para atividade
  - Configuração de horário de início e fim para atividade
  - Status ativo/inativo
  - Auditoria (criado por, atualizado por, timestamps)

#### `NotificationScheduleConfigRepository`
- **Localização**: `com.indux.modules.ocf.domain.repository.NotificationScheduleConfigRepository`
- **Funcionalidades**:
  - Busca por configurações ativas
  - Busca por configurações para horário atual

### Serviços

#### `NotificationScheduleConfigService`
- **Localização**: `com.indux.modules.ocf.application.service.NotificationScheduleConfigService`
- **Funcionalidades**:
  - CRUD completo para configurações
  - Ativação/desativação de configurações
  - Verificação se notificações estão habilitadas no momento atual

### Controllers e DTOs

#### `NotificationScheduleConfigController`
- **Localização**: `com.indux.modules.ocf.presentation.NotificationScheduleConfigController`
- **Endpoints**:
  - `GET /api/ocf/notification-schedule-configs` - Listar todas as configurações
  - `GET /api/ocf/notification-schedule-configs/active` - Listar configurações ativas
  - `GET /api/ocf/notification-schedule-configs/{id}` - Buscar configuração por ID
  - `POST /api/ocf/notification-schedule-configs` - Criar nova configuração
  - `PUT /api/ocf/notification-schedule-configs/{id}` - Atualizar configuração
  - `DELETE /api/ocf/notification-schedule-configs/{id}` - Deletar configuração
  - `PUT /api/ocf/notification-schedule-configs/{id}/activate` - Ativar configuração
  - `PUT /api/ocf/notification-schedule-configs/{id}/deactivate` - Desativar configuração
  - `GET /api/ocf/notification-schedule-configs/check-enabled` - Verificar se notificações estão habilitadas

#### DTOs
- `NotificationScheduleConfigDTO` - DTO para resposta
- `CreateNotificationScheduleConfigDTO` - DTO para criação
- `UpdateNotificationScheduleConfigDTO` - DTO para atualização

### Modificações no Scheduler

#### `NotificationScheduleScheduler`
- **Modificação**: Adicionada verificação de configurações antes de processar notificações
- **Comportamento**: Se alguma configuração ativa estiver no período configurado, o scheduler processa notificações

## 🎯 Como Usar

### ⚠️ Informações Importantes
- **user-id**: Extraído automaticamente do token JWT (não é mais necessário enviar no header)
- **diaDaSemana**: Aceita apenas **um dia da semana** (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
- **Comportamento**: Quando não há configurações de dias e horários cadastradas, as notificações agendadas são **desabilitadas automaticamente**
- **Substituição**: Se uma configuração for enviada para um dia que já existe, ela **substitui** a configuração existente (não duplica)

### 1. Criar Configuração de Período de Atividade

```bash
POST /api/ocf/notification-schedule-configs
Content-Type: application/json
Authorization: Bearer {jwt-token}

{
    "diaDaSemana": "MONDAY",
    "horaInicio": "08:00:00",
    "horaFim": "18:00:00",
    "ativo": true
}
```

### 2. Listar Configurações

```bash
GET /api/ocf/notification-schedule-configs
```

**Resposta ordenada por dia da semana:**
- MONDAY (Segunda-feira)
- TUESDAY (Terça-feira)  
- WEDNESDAY (Quarta-feira)
- THURSDAY (Quinta-feira)
- FRIDAY (Sexta-feira)
- SATURDAY (Sábado)
- SUNDAY (Domingo)

**Exemplo de resposta:**
```json
[
    {
        "id": "507f1f77bcf86cd799439011",
        "diaDaSemana": "MONDAY",
        "horaInicio": "08:00:00",
        "horaFim": "18:00:00",
        "ativo": true
    },
    {
        "id": "507f1f77bcf86cd799439012",
        "diaDaSemana": "TUESDAY",
        "horaInicio": "09:00:00",
        "horaFim": "17:00:00",
        "ativo": true
    },
    {
        "id": "507f1f77bcf86cd799439013",
        "diaDaSemana": "FRIDAY",
        "horaInicio": "08:30:00",
        "horaFim": "17:30:00",
        "ativo": true
    }
]
```

### 3. Ativar/Desativar Configuração

```bash
PUT /api/ocf/notification-schedule-configs/{id}/activate
PUT /api/ocf/notification-schedule-configs/{id}/deactivate
```

### 4. Verificar Status Atual

```bash
GET /api/ocf/notification-schedule-configs/check-enabled
```

### 5. Remover Configurações Duplicadas

```bash
POST /api/ocf/notification-schedule-configs/remove-duplicates
```

**Funcionalidade:**
- Remove configurações duplicadas para o mesmo dia da semana
- Mantém apenas a configuração mais recente para cada dia
- Útil para limpar dados após migração ou problemas de sincronização

### 6. Comportamento Sem Configurações

**Quando não há configurações cadastradas:**
- O endpoint `/check-enabled` retorna `false`
- O scheduler **não processa** notificações agendadas
- É necessário criar pelo menos uma configuração ativa para habilitar as notificações

**Exemplo de resposta quando não há configurações:**
```json
false
```

### 7. Comportamento de Substituição

**Quando uma configuração é enviada para um dia que já existe:**
- A configuração existente é **atualizada** com os novos valores
- Não são criadas configurações duplicadas
- O ID da configuração original é mantido
- Os campos `atualizadoEm` e `atualizadoPor` são atualizados

**Exemplo:**
1. Primeira configuração para MONDAY:
```json
{
    "diaDaSemana": "MONDAY",
    "horaInicio": "08:00:00",
    "horaFim": "18:00:00",
    "ativo": true
}
```

2. Segunda configuração para o mesmo MONDAY:
```json
{
    "diaDaSemana": "MONDAY",
    "horaInicio": "09:00:00",
    "horaFim": "17:00:00",
    "ativo": true
}
```

**Resultado:** A primeira configuração é atualizada com os novos horários (09:00-17:00), mantendo o mesmo ID.

## 🔧 Exemplos de Configurações

### Segunda-feira
```json
{
    "diaDaSemana": "MONDAY",
    "horaInicio": "08:00:00",
    "horaFim": "18:00:00"
}
```

### Sábado
```json
{
    "diaDaSemana": "SATURDAY",
    "horaInicio": "09:00:00",
    "horaFim": "17:00:00"
}
```

### Domingo
```json
{
    "diaDaSemana": "SUNDAY",
    "horaInicio": "07:00:00",
    "horaFim": "20:00:00"
}
```

## 🧪 Testes

### Testes Unitários
- `NotificationScheduleConfigServiceTest` - Testa lógica de negócio do serviço
- `NotificationScheduleSchedulerTest` - Testa integração com o scheduler

### Testes de Integração
- `NotificationScheduleConfigControllerTest` - Testa endpoints da API

## ⚙️ Configuração do Sistema

### Dependências
- MongoDB (para persistência das configurações)
- Spring Boot (para injeção de dependências)
- Lombok (para redução de boilerplate)

### Variáveis de Ambiente
Não são necessárias variáveis de ambiente específicas. A funcionalidade utiliza as configurações existentes do MongoDB.

## 🔍 Monitoramento

### Logs
- O scheduler registra quando as notificações são habilitadas por configuração
- Erros são tratados silenciosamente para não afetar o funcionamento principal

### Métricas
- Endpoint `GET /api/ocf/notification-schedule-configs/check-enabled` pode ser usado para monitoramento
- Status das configurações pode ser verificado via API

## 🔄 Funcionalidade de Notificações Acumuladas

### 📋 Como Funciona

Quando um período de inatividade termina, o sistema automaticamente:

1. **Detecta o início do período**: Rastreia quando configurações de atividade são ativadas
2. **Identifica ocorrências pendentes**: Busca ocorrências que ficaram paradas durante períodos de inatividade
3. **Envia notificações acumuladas**: Notifica sobre atrasos que ocorreram durante períodos fora de atividade
4. **Calcula tempo total**: Inclui o período fora de atividade no cálculo do tempo de atraso

### 🎯 Características das Notificações Acumuladas

- **Identificação clara**: Assunto e mensagem indicam que é uma "notificação acumulada"
- **Informação do período**: Mostra quando o período de inatividade começou e terminou
- **Tempo total de atraso**: Calcula o tempo incluindo períodos fora de atividade
- **Evita spam**: Só notifica ocorrências que já estavam no limite antes do período fora de atividade

### 📝 Exemplo de Notificação Acumulada

```
🚨 ALERTA DE ATRASO ACUMULADO: 4 horas e 30 minutos 🚨

⚠️ ATENÇÃO: Esta notificação foi acumulada durante período de inatividade do sistema.

Período de inatividade: 23/09/2025 15:00 até 23/09/2025 17:00

Protocolo: #12345
Colaborador: João Silva - 123456
Tempo de atraso total: 4 horas e 30 minutos
```

## 🚀 Benefícios

1. **Controle Granular**: Permite configurar períodos específicos de atividade
2. **Flexibilidade**: Suporte a múltiplas configurações simultâneas
3. **Facilidade de Uso**: API REST simples para gerenciamento
4. **Auditoria**: Rastreamento de quem criou/atualizou cada configuração
5. **Testabilidade**: Cobertura completa de testes unitários e de integração
6. **Notificações Acumuladas**: Não perde alertas importantes durante períodos fora de atividade
7. **Transparência**: Informa claramente quando notificações foram acumuladas

## ⚠️ Considerações

- Configurações são aplicadas imediatamente (não requer reinicialização)
- Múltiplas configurações podem estar ativas simultaneamente
- O scheduler verifica as configurações a cada execução (a cada minuto)
- Configurações inativas não afetam o funcionamento do sistema
- **IMPORTANTE**: Se não há configurações ativas, as notificações agendadas são **desabilitadas automaticamente**
- É necessário criar pelo menos uma configuração ativa para que as notificações funcionem
