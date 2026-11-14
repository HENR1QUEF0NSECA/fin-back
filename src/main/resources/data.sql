INSERT INTO categorias (id, nome) VALUES
    (1, 'Transporte'),
    (2, 'Lazer'),
    (3, 'Contas da casa'),
    (4, 'Saúde'),
    (5, 'Compras'),
    (6, 'Salário'),
    (7, 'Freelance')
ON CONFLICT (id) DO UPDATE SET nome = EXCLUDED.nome;

SELECT setval(pg_get_serial_sequence('categorias', 'id'), (SELECT MAX(id) FROM categorias));
