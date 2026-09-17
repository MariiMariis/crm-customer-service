-- Dados de demonstracao carregados a cada start (H2 em memoria, create-drop)

INSERT INTO customers (id, name, email, phone, document, created_at) VALUES
  (1, 'Ana Souza', 'ana.souza@empresa1.com', '11988887777', '11122233344', CURRENT_TIMESTAMP),
  (2, 'Bruno Lima', 'bruno.lima@firma2.com', '21977776666', '22233344455', CURRENT_TIMESTAMP),
  (3, 'Carla Menezes', 'carla.menezes@industria3.com', '31966665555', '33344455566', CURRENT_TIMESTAMP);

INSERT INTO agents (id, name, email, department, active) VALUES
  (1, 'Diego Ferreira', 'diego.ferreira@pbcrm.com', 'Suporte Tecnico', TRUE),
  (2, 'Elisa Prado', 'elisa.prado@pbcrm.com', 'Financeiro', TRUE);

INSERT INTO tickets (id, subject, description, status, priority, customer_id, agent_id, created_at, updated_at) VALUES
  (1, 'Erro ao acessar o sistema', 'Cliente recebe erro 500 ao tentar logar na plataforma.', 'IN_PROGRESS', 'HIGH', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'Duvida sobre fatura', 'Cliente questiona cobranca duplicada na fatura de julho.', 'OPEN', 'MEDIUM', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Solicitacao de cancelamento', 'Cliente deseja cancelar o plano atual.', 'RESOLVED', 'LOW', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO interactions (id, ticket_id, author, message, created_at) VALUES
  (1, 1, 'Diego Ferreira', 'Identificamos o problema, ajuste ja esta em andamento.', CURRENT_TIMESTAMP),
  (2, 1, 'Ana Souza', 'Obrigada, aguardo o retorno.', CURRENT_TIMESTAMP),
  (3, 3, 'Elisa Prado', 'Cancelamento processado com sucesso.', CURRENT_TIMESTAMP);

ALTER TABLE customers ALTER COLUMN id RESTART WITH 100;
ALTER TABLE agents ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tickets ALTER COLUMN id RESTART WITH 100;
ALTER TABLE interactions ALTER COLUMN id RESTART WITH 100;
