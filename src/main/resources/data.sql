-- ─────────────────────────────────────────────────────────────────────────────
-- Seed de categorias (PostgreSQL)
--
-- Para adicionar uma nova categoria aqui, siga o padrão com o próximo id:
--   INSERT INTO categorias (id, nome) VALUES (8, 'investment') ON CONFLICT (id) DO NOTHING;
--
-- O ON CONFLICT DO NOTHING garante idempotência: reexecutar não duplica dados.
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO categorias (id, nome) VALUES (1, 'transport') ON CONFLICT (id) DO NOTHING;
INSERT INTO categorias (id, nome) VALUES (2, 'entertainment') ON CONFLICT (id) DO NOTHING;
INSERT INTO categorias (id, nome) VALUES (3, 'utilities') ON CONFLICT (id) DO NOTHING;
INSERT INTO categorias (id, nome) VALUES (4, 'healthcare') ON CONFLICT (id) DO NOTHING;
INSERT INTO categorias (id, nome) VALUES (5, 'shopping') ON CONFLICT (id) DO NOTHING;
INSERT INTO categorias (id, nome) VALUES (6, 'salary') ON CONFLICT (id) DO NOTHING;
INSERT INTO categorias (id, nome) VALUES (7, 'freelance') ON CONFLICT (id) DO NOTHING;

-- Como os ids acima são fixos, o contador automático da coluna id não avança
-- sozinho. Sem esta linha, um INSERT sem id (direto no banco) tentaria usar o
-- id 1 e falharia por chave duplicada.
SELECT setval(pg_get_serial_sequence('categorias', 'id'), (SELECT MAX(id) FROM categorias));
