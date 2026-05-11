---
trigger: always_on
---

# CODING_STANDARDS.md

Define regras para codificação

## Estilo geral
- Prefira código simples e legível; evitar over-engineering.
- Comentários no código apenas quando explicitado pela solicitação.
- Codificação sempre em inglês (nomes de variáveis, métodos, classes etc)

## Linguagens
- Java: usar eslint/prettier padrão, módulos ES

## Estrutura
- Nomes claros para variáveis e funções.
- Tratar erros explicitamente, mensagens de erro claras.
- Não adicionar bibliotecas externas sem consentimento

## Testes
- Cobrir caminho feliz, borda e erro.
- Nomear testes de forma descritiva.
- Evitar mocks excessivos; preferir testes de unidade coesos.

## Segurança
- Não expor segredos; usar variáveis de ambiente.
- Validar entradas externas.
- Não executar commits, push, pull ou qualquer operação git de versionamento

## Filosofia

- **Clean Architecture** com separação clara de camadas
- **Clean Code** com código autoexplicativo
- **SOLID** principles aplicados consistentemente
