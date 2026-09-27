INSERT INTO role (id, tipo, descricao)
VALUES
    (gen_random_uuid(), 'ADMIN', 'Administrador'),
    (gen_random_uuid(), 'PROFISSIONAL', 'Profissional'),
    (gen_random_uuid(), 'ASSISTENTE', 'Assistente')
ON CONFLICT (tipo) DO NOTHING;
