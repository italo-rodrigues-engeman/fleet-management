-- Script para corrigir o checksum do Flyway para a migração V14.0
-- Execute este script diretamente no seu banco de dados PostgreSQL

-- Atualizar o checksum da migração V14.0 na tabela flyway_schema_history
UPDATE flyway_schema_history 
SET checksum = 245264558 
WHERE version = '14.0';

-- Verificar se a atualização foi bem-sucedida
SELECT version, checksum, description 
FROM flyway_schema_history 
WHERE version = '14.0'; 