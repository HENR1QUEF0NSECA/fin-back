-- ─────────────────────────────────────────────────────────────────────────────
-- Seed de categorias (PostgreSQL)
--
-- Roda a cada inicialização. O ON CONFLICT (id) DO UPDATE garante idempotência
-- (reexecutar não duplica nada) e também atualiza o nome de quem já existe,
-- como as categorias que antes estavam em inglês.
--
-- Para adicionar uma nova categoria, siga o padrão com o próximo id:
--   (8, 'Investimentos')
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO categorias (id, nome) VALUES
    (1, 'Transporte'),
    (2, 'Lazer'),
    (3, 'Contas da casa'),
    (4, 'Saúde'),
    (5, 'Compras'),
    (6, 'Salário'),
    (7, 'Freelance')
ON CONFLICT (id) DO UPDATE SET nome = EXCLUDED.nome;

-- Como os ids acima são fixos, o contador automático da coluna id não avança
-- sozinho. Sem esta linha, um INSERT sem id (direto no banco) tentaria usar o
-- id 1 e falharia por chave duplicada.
SELECT setval(pg_get_serial_sequence('categorias', 'id'), (SELECT MAX(id) FROM categorias));
