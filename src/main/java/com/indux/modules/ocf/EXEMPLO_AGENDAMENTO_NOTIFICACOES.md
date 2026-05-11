# Exemplo Prático - Sistema de Agendamento de Notificações

## 🎯 Cenário: Configurar Agendamentos de Disparo por Canal, Etapa e Múltiplos Usuários

### ✨ Nova Funcionalidade: Suporte a Múltiplos User IDs

Agora é possível configurar agendamentos para **múltiplos usuários** simultaneamente! Isso significa que você pode:

- ✅ **Enviar notificações para vários usuários** com uma única configuração
- ✅ **Reduzir a duplicação** de agendamentos similares
- ✅ **Facilitar a gestão** de notificações para equipes
- ✅ **Manter compatibilidade** com usuários únicos (usando array com um elemento)

**Exemplo de uso:**
```json
{
  "canais": ["EMAIL", "WHATSAPP"],
  "tempoLimiteHoras": 24.5,
  "etapa": 1,
  "userIds": ["user123", "user456", "user789"],  // ← Múltiplos usuários!
  "textoNotificacao": "Lembrete: Ocorrência aguardando sua análise há mais de 24 horas. Por favor, verifique o sistema."  // ← Texto personalizado!
}
```

**✨ Nova Funcionalidade: Suporte a Frações de Horas**

Agora é possível configurar agendamentos com **frações de horas** para maior precisão! Isso significa que você pode:

- ✅ **Configurar notificações em minutos**: 0.5 horas = 30 minutos, 1.5 horas = 1h30min
- ✅ **Maior flexibilidade**: 0.1 horas = 6 minutos, 2.25 horas = 2h15min
- ✅ **Precisão granular**: Até 0.0167 horas (1 minuto) como tempo mínimo
- ✅ **Compatibilidade**: Números inteiros continuam funcionando normalmente

**Exemplos de frações de horas:**
- `0.0167` = 1 minuto (tempo mínimo)
- `0.1` = 6 minutos
- `0.5` = 30 minutos  
- `1.5` = 1 hora e 30 minutos
- `2.25` = 2 horas e 15 minutos
- `12.75` = 12 horas e 45 minutos

**✨ Nova Funcionalidade: Texto Personalizado de Notificação**

Agora é possível configurar **texto personalizado** para cada agendamento! Isso significa que você pode:

- ✅ **Mensagens específicas**: Cada agendamento pode ter sua própria mensagem
- ✅ **Contexto personalizado**: Adaptar o texto para cada etapa ou situação
- ✅ **Flexibilidade total**: Até 1000 caracteres para mensagens detalhadas
- ✅ **Múltiplos canais**: O mesmo texto é enviado por todos os canais configurados

**Exemplos de textos personalizados:**
- `"Lembrete: Ocorrência aguardando análise há mais de 24h. Verifique o sistema."`
- `"URGENTE: Ocorrência na etapa 2 há mais de 2 horas. Ação necessária!"`
- `"Notificação automática: Ocorrência pendente há mais de 1 hora na sua fila."`
- `"Lembrete de prazo: Ocorrência aguardando sua aprovação há mais de 4 horas."`

### Passo 1: Criar Agendamento para Email e WhatsApp - Etapa 1 - Múltiplos Usuários (com fração de horas e texto personalizado)

```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL", "WHATSAPP"],
    "tempoLimiteHoras": 24.5,
    "etapa": 1,
    "userIds": ["user123", "user456", "user789"],
    "textoNotificacao": "Lembrete: Ocorrência aguardando sua análise há mais de 24 horas. Por favor, verifique o sistema."
  }'
```

**Resposta:**
```json
{
  "id": "65a1b2c3d4e5f6789012345e",
  "canais": ["EMAIL", "WHATSAPP"],
  "tempoLimiteHoras": 24.5,
  "etapa": 1,
  "userIds": ["user123", "user456", "user789"],
  "textoNotificacao": "Lembrete: Ocorrência aguardando sua análise há mais de 24 horas. Por favor, verifique o sistema.",
  "ativo": true,
  "criadoEm": "2025-01-15T11:00:00",
  "atualizadoEm": "2025-01-15T11:00:00",
  "criadoPor": "admin456",
  "atualizadoPor": "admin456"
}
```

### Passo 2: Criar Agendamento para Teams e WebSocket - Etapa 2 - Usuário Único

```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["TEAMS", "WEBSOCKET"],
    "tempoLimiteHoras": 48,
    "etapa": 2,
    "userIds": ["user456"],
    "textoNotificacao": "URGENTE: Ocorrência na etapa 2 há mais de 48 horas. Ação necessária imediatamente!"
  }'
```

### Passo 3: Criar Agendamento para Todos os Canais - Etapa 3 - Múltiplos Usuários

```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL", "WHATSAPP", "TEAMS", "WEBSOCKET"],
    "tempoLimiteHoras": 72,
    "etapa": 3,
    "userIds": ["user789", "user999", "user111", "user222"],
    "textoNotificacao": "Notificação automática: Ocorrência pendente há mais de 72 horas na etapa 3. Verificação necessária."
  }'
```

### Passo 4: Criar Agendamento para Email - Etapa 1 - Usuário Único (com fração de horas)

```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 0.5,
    "etapa": 1,
    "userIds": ["user999"],
    "textoNotificacao": "Lembrete rápido: Ocorrência aguardando análise há 30 minutos."
  }'
```

### Passo 5: Criar Agendamento com Fração de Horas - Notificação Rápida

```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["WHATSAPP"],
    "tempoLimiteHoras": 0.25,
    "etapa": 2,
    "userIds": ["user111"],
    "textoNotificacao": "Alerta rápido: Ocorrência na etapa 2 há 15 minutos. Verifique urgentemente!"
  }'
```

**Nota:** Este exemplo cria um agendamento que dispara após apenas 15 minutos (0.25 horas)!

### Passo 6: Criar Agendamento com Tempo Mínimo - Notificação Instantânea

```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["WHATSAPP"],
    "tempoLimiteHoras": 0.0167,
    "etapa": 1,
    "userIds": ["user222"],
    "textoNotificacao": "Alerta instantâneo: Ocorrência aguardando análise há 1 minuto. Verifique imediatamente!"
  }'
```

**Nota:** Este exemplo cria um agendamento que dispara após apenas 1 minuto (0.0167 horas) - o tempo mínimo permitido!

## 📋 Verificar Agendamentos Criados

### Listar Todos os Agendamentos
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
[
  {
    "id": "65a1b2c3d4e5f6789012345e",
    "canais": ["EMAIL", "WHATSAPP"],
    "tempoLimiteHoras": 24,
    "etapa": 1,
    "userIds": ["user123", "user456", "user789"],
    "ativo": true,
    "criadoEm": "2025-01-15T11:00:00",
    "criadoPor": "admin456"
  },
  {
    "id": "65a1b2c3d4e5f6789012345f",
    "canais": ["TEAMS", "WEBSOCKET"],
    "tempoLimiteHoras": 48,
    "etapa": 2,
    "userIds": ["user456"],
    "ativo": true,
    "criadoEm": "2025-01-15T11:05:00",
    "criadoPor": "admin456"
  },
  {
    "id": "65a1b2c3d4e5f6789012345g",
    "canais": ["EMAIL", "WHATSAPP", "TEAMS", "WEBSOCKET"],
    "tempoLimiteHoras": 72,
    "etapa": 3,
    "userIds": ["user789", "user999", "user111", "user222"],
    "ativo": true,
    "criadoEm": "2025-01-15T11:10:00",
    "criadoPor": "admin456"
  },
  {
    "id": "65a1b2c3d4e5f6789012345h",
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 12,
    "etapa": 1,
    "userIds": ["user999"],
    "ativo": true,
    "criadoEm": "2025-01-15T11:15:00",
    "criadoPor": "admin456"
  }
]
```

### Buscar Agendamento Específico
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/canal/EMAIL/etapa/1/user/user123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
{
  "id": "65a1b2c3d4e5f6789012345e",
  "canais": ["EMAIL", "WHATSAPP"],
  "tempoLimiteHoras": 24,
  "etapa": 1,
  "userIds": ["user123", "user456", "user789"],
  "ativo": true,
  "criadoEm": "2025-01-15T11:00:00",
  "criadoPor": "admin456"
}
```

## 🔄 Atualizar Agendamento

### Alterar Tempo Limite e Canais
```bash
curl -X PUT "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/65a1b2c3d4e5f6789012345e" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL", "WHATSAPP", "TEAMS"],
    "tempoLimiteHoras": 12,
    "etapa": 1,
    "userIds": ["user123", "user456", "user789"],
    "ativo": true
  }'
```

## 🚫 Desativar Agendamento

```bash
curl -X PUT "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/65a1b2c3d4e5f6789012345e/deactivate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
{
  "message": "Agendamento desativado com sucesso.",
  "status": 200
}
```

## 🗑️ Deletar Agendamento Permanentemente

```bash
curl -X DELETE "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/65a1b2c3d4e5f6789012345e"
```

**Resposta:**
```json
{
  "message": "Agendamento removido com sucesso.",
  "status": 200
}
```

**⚠️ Atenção:** Esta operação remove o agendamento permanentemente do banco de dados. Use com cuidado!

## 🔄 Reativar Agendamento

```bash
curl -X PUT "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/65a1b2c3d4e5f6789012345e/activate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
{
  "message": "Agendamento ativado com sucesso.",
  "status": 200
}
```

## 🚫 Controle de Notificações Duplicadas por Agendamento

O sistema possui um mecanismo automático para **evitar notificar a mesma ocorrência múltiplas vezes pelo MESMO agendamento**:

### Como Funciona:
- ✅ **Histórico independente**: Cada agendamento mantém seu próprio histórico de ocorrências notificadas
- ✅ **Primeira notificação**: A ocorrência é notificada normalmente pelo agendamento
- ✅ **Registro automático**: O ID da ocorrência é salvo no campo `ocorrenciasNotificadas` do agendamento específico
- ✅ **Verificação subsequente**: O sistema verifica se a ocorrência já foi notificada por AQUELE agendamento
- ✅ **Pulando duplicatas**: Se já foi notificada por aquele agendamento, a ocorrência é pulada

### Exemplo de Cenário:
```
Agendamento A (1 minuto) → Notifica ocorrência X para usuário A
Agendamento B (5 minutos) → Pode notificar a MESMA ocorrência X para usuário B
Agendamento A (próximo ciclo) → NÃO notifica ocorrência X novamente (já foi notificada)
```

### Exemplo de Log:
```
📧 Processando notificação para ocorrência 68b849d20a689158d8b1717a (protocolo: 136)
⏭️ Ocorrência 68b849d20a689158d8b1717a já foi notificada pelo agendamento 68b8421559ccb1314ac94ae6. Pulando...
```

### Benefícios:
- 🚫 **Evita spam**: Não envia notificações repetidas pelo mesmo agendamento
- ⚡ **Performance**: Melhora a performance do sistema
- 📊 **Controle granular**: Cada agendamento mantém seu próprio histórico
- 🔄 **Flexibilidade**: Diferentes agendamentos podem notificar a mesma ocorrência
- 👥 **Multi-usuário**: Permite notificar a mesma ocorrência para diferentes usuários em tempos diferentes

### Casos de Uso:
- **Escalação**: Notificar gestor em 1 minuto, diretor em 5 minutos
- **Diferentes canais**: Email em 1 minuto, WhatsApp em 3 minutos
- **Diferentes usuários**: Usuário A em 1 minuto, Usuário B em 5 minutos
- **Diferentes etapas**: Etapa 1 em 1 minuto, Etapa 2 em 5 minutos

### Exemplo Prático - Escalação de Notificações:

**Agendamento 1 - Notificação Imediata (1 minuto):**
```json
{
  "canais": ["EMAIL", "WHATSAPP"],
  "tempoLimiteHoras": 0.0167,
  "etapa": 2,
  "usuarios": ["user-gestor-id"],
  "textoNotificacao": "Alerta: Ocorrência aguardando análise há 1 minuto"
}
```

**Agendamento 2 - Escalação (5 minutos):**
```json
{
  "canais": ["EMAIL", "WHATSAPP", "TEAMS"],
  "tempoLimiteHoras": 0.0833,
  "etapa": 2,
  "usuarios": ["user-diretor-id"],
  "textoNotificacao": "Escalação: Ocorrência aguardando análise há 5 minutos"
}
```

**Resultado:**
- ✅ **1 minuto**: Gestor recebe notificação
- ✅ **5 minutos**: Diretor recebe notificação (mesma ocorrência)
- ❌ **Próximo ciclo**: Gestor NÃO recebe novamente (já foi notificado)
- ❌ **Próximo ciclo**: Diretor NÃO recebe novamente (já foi notificado)

## 🔍 Consultas Úteis

### Buscar Agendamentos por Canal
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/canal/EMAIL" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Buscar Agendamentos por Etapa
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/etapa/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Buscar Agendamentos por User ID
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/user/user123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## ⚠️ Casos de Erro

### Tentar Criar Agendamento Duplicado
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 24,
    "etapa": 1,
    "userIds": ["user123"]
  }'
```

**Resposta de Erro:**
```json
{
  "message": "Já existe um agendamento ativo para canal EMAIL - etapa 1 - userId user123",
  "status": 400
}
```

### Tempo Limite Muito Alto
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 200.5,
    "etapa": 2,
    "userIds": ["user456"]
  }'
```

**Resposta de Erro:**
```json
{
  "message": "Tempo limite não pode ser superior a 168 horas (7 dias)",
  "status": 400
}
```

### Tempo Limite Muito Baixo (Frações)
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 0.01,
    "etapa": 1,
    "userIds": ["user123"],
    "textoNotificacao": "Texto de exemplo"
  }'
```

**Resposta de Erro:**
```json
{
  "message": "Tempo limite deve ser pelo menos 0.0167 horas (1 minuto)",
  "status": 400
}
```

### Canal Inválido
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["INVALIDO"],
    "tempoLimiteHoras": 24,
    "etapa": 1,
    "userIds": ["user123"],
    "textoNotificacao": "Texto de exemplo"
  }'
```

**Resposta de Erro:**
```json
{
  "message": "Canal inválido: INVALIDO. Use: EMAIL, WHATSAPP, TEAMS ou WEBSOCKET",
  "status": 400
}
```

### Tentar Deletar Agendamento Inexistente
```bash
curl -X DELETE "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/agendamento_inexistente"
```

**Resposta de Erro:**
```json
{
  "message": "Agendamento não encontrado com ID: agendamento_inexistente",
  "status": 404
}
```

### Texto de Notificação Vazio
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 24,
    "etapa": 1,
    "userIds": ["user123"],
    "textoNotificacao": ""
  }'
```

**Resposta de Erro:**
```json
{
  "message": "Texto da notificação é obrigatório e não pode estar vazio",
  "status": 400
}
```

### Texto de Notificação Muito Longo
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/notification-schedule" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "canais": ["EMAIL"],
    "tempoLimiteHoras": 24,
    "etapa": 1,
    "userIds": ["user123"],
    "textoNotificacao": "Este é um texto muito longo que excede o limite de 1000 caracteres permitidos para o campo textoNotificacao. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incididunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"
  }'
```

**Resposta de Erro:**
```json
{
  "message": "Texto da notificação não pode ter mais de 1000 caracteres",
  "status": 400
}
```

## 🎯 Como o Sistema Usa os Agendamentos

### ⚙️ Processamento Automático

O sistema possui um **scheduler automático** (`NotificationScheduleScheduler`) que executa **a cada 1 minuto** para processar todos os agendamentos ativos:

1. **🔄 Execução**: A cada minuto, o scheduler verifica todos os agendamentos ativos
2. **📋 Busca**: Localiza ocorrências na etapa especificada que excederam o tempo limite
3. **📧 Envio**: Envia notificações pelos canais configurados para todos os usuários
4. **📝 Logs**: Registra todas as operações no terminal para monitoramento

### Exemplo: Verificação de Tempo Limite

1. **Sistema verifica**: Ocorrência na etapa 1
2. **Busca agendamentos**: Para etapa 1
3. **Encontra**: Agendamento para EMAIL + WHATSAPP + [user123, user456, user789] + 24 horas
4. **Calcula**: Tempo decorrido desde que a ocorrência entrou na etapa 1
5. **Se exceder**: Envia notificação por EMAIL E WHATSAPP para todos os usuários: user123, user456 e user789

### 📊 Monitoramento e Logs

O sistema gera logs detalhados no terminal para facilitar o monitoramento:

```
🔄 Iniciando processamento de agendamentos de notificação...
📋 Encontrados 3 agendamentos ativos para processar
🔍 Processando agendamento ID: 68b8421559ccb1314ac94ae6 - Etapa: 2 - Tempo: 0.0167h - Canais: [WHATSAPP, EMAIL] - Usuários: [954435f9-fa3f-4ab6-9986-9dd5ceeea788]
📊 Encontradas 1 ocorrências na etapa 2 para o agendamento 68b8421559ccb1314ac94ae6
📧 Processando notificação para ocorrência 507f1f77bcf86cd799439011 (protocolo: 12345)
📤 Enviando notificação via WHATSAPP para ocorrência 507f1f77bcf86cd799439011 - Texto: 'Alerta instantâneo: Ocorrência aguardando análise há 1 minuto. Verifique imediatamente!'
👤 Enviando notificação para usuário: 954435f9-fa3f-4ab6-9986-9dd5ceeea788
✅ Notificação WHATSAPP enviada com sucesso para usuário 954435f9-fa3f-4ab6-9986-9dd5ceeea788 - Ocorrência 507f1f77bcf86cd799439011
📤 Enviando notificação via EMAIL para ocorrência 507f1f77bcf86cd799439011 - Texto: 'Alerta instantâneo: Ocorrência aguardando análise há 1 minuto. Verifique imediatamente!'
👤 Enviando notificação para usuário: 954435f9-fa3f-4ab6-9986-9dd5ceeea788
✅ Notificação EMAIL enviada com sucesso para usuário 954435f9-fa3f-4ab6-9986-9dd5ceeea788 - Ocorrência 507f1f77bcf86cd799439011
✅ Notificações enviadas com sucesso para ocorrência 507f1f77bcf86cd799439011
✅ Processamento de agendamentos concluído com sucesso!
```

### Exemplo: Lógica de Disparo

```java
// Pseudocódigo da lógica de verificação
public void verificarAgendamentos() {
    List<NotificationSchedule> agendamentos = repository.findByAtivoTrue();
    
    for (NotificationSchedule agendamento : agendamentos) {
        // Buscar ocorrências que estão na etapa especificada
        List<OcorrenciaFF> ocorrencias = buscarOcorrenciasPorEtapa(agendamento.getEtapa());
        
        for (OcorrenciaFF ocorrencia : ocorrencias) {
            LocalDateTime tempoLimite = calcularTempoLimite(ocorrencia, agendamento.getTempoLimiteHoras());
            
            if (LocalDateTime.now().isAfter(tempoLimite)) {
                // Enviar notificação por todos os canais especificados para todos os usuários
                for (String canal : agendamento.getCanais()) {
                    for (String userId : agendamento.getUserIds()) {
                        enviarNotificacaoAgendada(ocorrencia, agendamento, canal, userId);
                    }
                }
            }
        }
    }
}
```

## 🎉 Resultado Final

Com esses agendamentos, o sistema agora:

- ✅ **EMAIL + WHATSAPP - Etapa 1 - [user123, user456, user789]**: Dispara após 24.5 horas (24h30min)
- ✅ **TEAMS + WEBSOCKET - Etapa 2 - [user456]**: Dispara após 48 horas  
- ✅ **EMAIL + WHATSAPP + TEAMS + WEBSOCKET - Etapa 3 - [user789, user999, user111, user222]**: Dispara após 72 horas
- ✅ **EMAIL - Etapa 1 - [user999]**: Dispara após 0.5 horas (30 minutos)
- ✅ **WHATSAPP - Etapa 2 - [user111]**: Dispara após 0.25 horas (15 minutos)
- ✅ **WHATSAPP - Etapa 1 - [user222]**: Dispara após 0.0167 horas (1 minuto)

O sistema de agendamento permite controle granular sobre:
- **Múltiplos canais de notificação** (EMAIL, WHATSAPP, TEAMS, WEBSOCKET)
- **Tempo limite com frações** (de 0.0167 horas = 1 minuto até 168 horas = 7 dias)
- **Etapa específica** da ocorrência
- **Múltiplos usuários** que receberão a notificação
- **Precisão em minutos** para notificações rápidas ou escalonadas
- **Texto personalizado** para cada agendamento (até 1000 caracteres)
- **Mensagens contextuais** adaptadas para cada situação e etapa

O sistema está totalmente configurável e permite controle preciso sobre quando e como enviar lembretes automáticos! 🚀

## 👥 Nova Funcionalidade: Informações Completas dos Usuários

Agora você pode obter **informações completas dos usuários** (nome, email, telefone, etc.) em vez de apenas os IDs! 

### 🔍 Endpoints com Informações dos Usuários

Todos os endpoints existentes agora têm versões com sufixo `/with-users` que retornam dados completos:

#### Buscar Agendamento com Informações dos Usuários
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/65a1b2c3d4e5f6789012345e/with-users" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
{
  "id": "65a1b2c3d4e5f6789012345e",
  "canais": ["EMAIL", "WHATSAPP"],
  "tempoLimiteHoras": 24,
  "etapa": 1,
  "usuarios": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "nome": "João Silva",
      "email": "joao.silva@empresa.com",
      "telefone": "11999999999",
      "imagemUrl": "https://storage.com/fotos/joao.jpg",
      "desativado": false,
      "permissao": "ANALISTA",
      "criadoEm": "2024-01-15T10:00:00Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001", 
      "nome": "Maria Santos",
      "email": "maria.santos@empresa.com",
      "telefone": "11888888888",
      "imagemUrl": "https://storage.com/fotos/maria.jpg",
      "desativado": false,
      "permissao": "GERENTE",
      "criadoEm": "2024-01-16T14:30:00Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "nome": "Pedro Costa", 
      "email": "pedro.costa@empresa.com",
      "telefone": "11777777777",
      "imagemUrl": null,
      "desativado": false,
      "permissao": "SUPERVISOR",
      "criadoEm": "2024-01-17T09:15:00Z"
    }
  ],
  "ativo": true,
  "criadoEm": "2025-01-15T11:00:00",
  "criadoPor": "admin456"
}
```

#### Listar Todos os Agendamentos com Informações dos Usuários
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/with-users" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Buscar por Canal com Informações dos Usuários
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/canal/EMAIL/with-users" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Buscar por Etapa com Informações dos Usuários
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/etapa/1/with-users" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Buscar por Usuário com Informações Completas
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/notification-schedule/user/550e8400-e29b-41d4-a716-446655440000/with-users" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 📊 Comparação: Endpoints Tradicionais vs. Com Informações dos Usuários

| Funcionalidade | Endpoint Tradicional | Endpoint com Usuários |
|---|---|---|
| **Buscar por ID** | `/notification-schedule/{id}` | `/notification-schedule/{id}/with-users` |
| **Listar todos** | `/notification-schedule` | `/notification-schedule/with-users` |
| **Por canal** | `/notification-schedule/canal/{canal}` | `/notification-schedule/canal/{canal}/with-users` |
| **Por etapa** | `/notification-schedule/etapa/{etapa}` | `/notification-schedule/etapa/{etapa}/with-users` |
| **Por usuário** | `/notification-schedule/user/{userId}` | `/notification-schedule/user/{userId}/with-users` |

### 🎯 Vantagens dos Novos Endpoints

- ✅ **Informações completas**: Nome, email, telefone, foto, permissões
- ✅ **Melhor UX**: Interface pode mostrar nomes em vez de IDs
- ✅ **Menos requisições**: Uma única chamada retorna tudo
- ✅ **Compatibilidade**: Endpoints tradicionais continuam funcionando
- ✅ **Flexibilidade**: Escolha o formato que melhor atende sua necessidade

### 💡 Casos de Uso

1. **Dashboard administrativo**: Mostrar nomes dos usuários nos agendamentos
2. **Relatórios**: Gerar listas com informações completas
3. **Interface de usuário**: Exibir avatares e nomes em vez de IDs
4. **Auditoria**: Rastrear quem criou/modificou agendamentos
5. **Integração**: APIs externas podem obter dados completos

## 🔧 Correção Implementada: Cálculo de Tempo Parado

### ❌ Problema Anterior
O sistema estava usando o campo `created_at` da ocorrência para calcular o tempo limite, o que não refletia o tempo real que a ocorrência estava parada na etapa atual.

### ✅ Solução Implementada
Agora o sistema calcula corretamente o tempo parado baseado no `stepLog`:

1. **Busca o último stepLog da etapa atual**
2. **Usa `final_at` se disponível, senão `created_at`**
3. **Calcula o tempo decorrido desde a última atualização na etapa**

### 📊 Exemplo Prático

**Dados da Ocorrência:**
```json
{
  "id": "ocorrencia-123",
  "currentStep": 3,
  "created_at": "2025-09-05T10:00:00.000+0000",
  "stepLog": [
    {
      "step": 1,
      "name": "Qualificação",
      "created_at": "2025-09-05T10:00:00.000+0000",
      "final_at": "2025-09-05T10:30:00.000+0000"
    },
    {
      "step": 2,
      "name": "Análise",
      "created_at": "2025-09-05T10:30:00.000+0000",
      "final_at": "2025-09-05T11:00:00.000+0000"
    },
    {
      "step": 3,
      "name": "Aprovação",
      "created_at": "2025-09-05T11:00:00.000+0000",
      "final_at": "2025-09-05T11:00:00.000+0000"
    }
  ]
}
```

**Cálculo do Tempo Parado:**
- **Etapa atual**: 3
- **Última atualização na etapa 3**: 2025-09-05T11:00:00.000+0000
- **Tempo parado**: Desde 11:00 até agora (não desde 10:00 da criação)

### 🎯 Benefícios da Correção

- ✅ **Precisão**: Tempo real parado na etapa atual
- ✅ **Flexibilidade**: Funciona com qualquer etapa
- ✅ **Robustez**: Fallback para `created_at` se stepLog estiver vazio
- ✅ **Logs detalhados**: Debug completo do cálculo

### 🔍 Logs de Debug

O sistema agora gera logs detalhados:
```
📊 Ocorrência ocorrencia-123 - Etapa atual: 3, Última atualização: 2025-09-05T11:00:00, Minutos parado: 120
⏱️ Ocorrência ocorrencia-123 - Tempo parado: 120 minutos, Limite: 60 minutos, Excede: true
```

---

# 📝 Cadastro de Listas de Strings

## 🎯 Visão Geral

O sistema permite cadastrar e gerenciar listas de strings para classificação de ocorrências em duas categorias:
- **PERTINENTE**: Strings que indicam ocorrências relevantes
- **NÃO PERTINENTE**: Strings que indicam ocorrências irrelevantes

## 🔧 APIs Disponíveis

### 1. OccurrenceStringController
**Base URL**: `/api/ocf/occurrence-strings`

#### 📝 Cadastrar Strings Pertinentes
```bash
curl -X POST "http://localhost:8080/api/ocf/occurrence-strings/pertinentes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "strings": [
      "desconto indevido",
      "valor incorreto",
      "falta de pagamento",
      "erro de cálculo"
    ]
  }'
```

**Resposta:**
```json
{
  "message": "Salvas 4 strings de ocorrências pertinentes com sucesso.",
  "status": 201
}
```

#### 📝 Cadastrar Strings Não Pertinentes
```bash
curl -X POST "http://localhost:8080/api/ocf/occurrence-strings/nao-pertinentes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "strings": [
      "consulta geral",
      "informação",
      "dúvida",
      "esclarecimento"
    ]
  }'
```

**Resposta:**
```json
{
  "message": "Salvas 4 strings de ocorrências não pertinentes com sucesso.",
  "status": 201
}
```

#### 📋 Listar Strings Pertinentes
```bash
curl -X GET "http://localhost:8080/api/ocf/occurrence-strings/pertinentes" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
[
  "desconto indevido",
  "valor incorreto",
  "falta de pagamento",
  "erro de cálculo"
]
```

#### 📋 Listar Strings Não Pertinentes
```bash
curl -X GET "http://localhost:8080/api/ocf/occurrence-strings/nao-pertinentes" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 📋 Listar Todas as Strings
```bash
curl -X GET "http://localhost:8080/api/ocf/occurrence-strings/all" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 🗑️ Limpar Strings Pertinentes
```bash
curl -X DELETE "http://localhost:8080/api/ocf/occurrence-strings/pertinentes" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 🗑️ Limpar Strings Não Pertinentes
```bash
curl -X DELETE "http://localhost:8080/api/ocf/occurrence-strings/nao-pertinentes" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2. StringApprovalController
**Base URL**: `/api/solicitacoes/ocf/string-approvals`

#### ✅ Aprovar Lista de Strings
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/string-approvals?aprovado=true" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "strings": [
      "string aprovada 1",
      "string aprovada 2",
      "string aprovada 3"
    ]
  }'
```

**Resposta:**
```json
{
  "message": "Strings aprovadas salvas com sucesso",
  "status": 200
}
```

#### ❌ Rejeitar Lista de Strings
```bash
curl -X POST "http://localhost:8080/api/solicitacoes/ocf/string-approvals?aprovado=false" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "strings": [
      "string rejeitada 1",
      "string rejeitada 2",
      "string rejeitada 3"
    ]
  }'
```

**Resposta:**
```json
{
  "message": "Strings rejeitadas salvas com sucesso",
  "status": 200
}
```

#### 📋 Listar Todas as Aprovações
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/string-approvals" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Resposta:**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "aprovado": ["string aprovada 1", "string aprovada 2"],
    "rejeitado": ["string rejeitada 1", "string rejeitada 2"],
    "createdAt": "2025-01-09T10:30:00",
    "updatedAt": "2025-01-09T10:30:00",
    "createdBy": "user123",
    "updatedBy": "user123"
  }
]
```

#### ✅ Listar Apenas Strings Aprovadas
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/string-approvals/aprovadas" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### ❌ Listar Apenas Strings Rejeitadas
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/string-approvals/rejeitadas" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 🔍 Obter Registro Mais Recente
```bash
curl -X GET "http://localhost:8080/api/solicitacoes/ocf/string-approvals/latest" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 🗑️ Limpar Todas as Strings
```bash
curl -X DELETE "http://localhost:8080/api/solicitacoes/ocf/string-approvals" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 🗑️ Limpar Apenas Strings Aprovadas
```bash
curl -X DELETE "http://localhost:8080/api/solicitacoes/ocf/string-approvals/aprovadas" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 🗑️ Limpar Apenas Strings Rejeitadas
```bash
curl -X DELETE "http://localhost:8080/api/solicitacoes/ocf/string-approvals/rejeitadas" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 📊 Estrutura dos Dados

### OccurrenceStringRequest
```json
{
  "strings": ["string1", "string2", "string3"]
}
```

**Validações:**
- `strings`: Lista não pode estar vazia
- Máximo de 100 strings por requisição

### StringApprovalResponseDTO
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "aprovado": ["string aprovada 1", "string aprovada 2"],
  "rejeitado": ["string rejeitada 1", "string rejeitada 2"],
  "createdAt": "2025-01-09T10:30:00",
  "updatedAt": "2025-01-09T10:30:00",
  "createdBy": "user123",
  "updatedBy": "user123"
}
```

## 🎯 Casos de Uso

### 1. **Classificação Automática de Ocorrências**
- Cadastre strings pertinentes para identificar ocorrências relevantes
- Cadastre strings não pertinentes para filtrar ocorrências irrelevantes
- Use as listas para classificação automática no sistema

### 2. **Aprovação de Listas**
- Aprove listas de strings para uso oficial
- Rejeite listas que não atendem aos critérios
- Mantenha histórico de aprovações e rejeições

### 3. **Gestão de Vocabulário**
- Mantenha listas atualizadas de termos relevantes
- Limpe listas antigas quando necessário
- Consulte listas aprovadas para referência

## ⚠️ Regras Importantes

### **Validações:**
- ✅ Lista de strings não pode estar vazia
- ✅ Máximo de 100 strings por requisição
- ✅ Strings são automaticamente trimadas (espaços removidos)

### **Comportamento:**
- ✅ Strings duplicadas são permitidas
- ✅ Strings vazias são ignoradas
- ✅ Todas as operações são registradas com usuário e timestamp

### **Limpeza:**
- ⚠️ Operações de limpeza são **irreversíveis**
- ⚠️ Use com cuidado em ambiente de produção
- ⚠️ Sempre faça backup antes de limpar listas

## 🔄 Fluxo de Trabalho Recomendado

1. **Cadastro Inicial**: Cadastre strings pertinentes e não pertinentes
2. **Teste**: Teste a classificação com dados reais
3. **Aprovação**: Aprove as listas que funcionam bem
4. **Manutenção**: Atualize listas conforme necessário
5. **Limpeza**: Limpe listas antigas periodicamente

Agora você tem total flexibilidade para trabalhar com agendamentos de notificações e listas de strings! 🎉
