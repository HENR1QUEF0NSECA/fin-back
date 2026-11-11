### Arquivo de testes HTTP para VSCode REST Client
### Pressione "Send Request" em cada bloco ou use a extensão REST Client
### Atualizado com RF06 (Orçamento) e RF10 (Notificação de Orçamento)

@baseUrl = http://localhost:8080/api
@token =

### ===== AUTENTICAÇÃO =====

### 1. Registrar novo usuário
POST {{baseUrl}}/users/register
Content-Type: application/json

{
"nome": "João Silva",
"email": "joao@example.com",
"senha": "senha123"
}

### 2. Login (gera token)
# Após executar, copie o token retornado na resposta para usar nas próximas requisições
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
"email": "joao@example.com",
"senha": "senha123"
}

### ===== USUÁRIOS =====

### 3. Obter dados do usuário autenticado
GET {{baseUrl}}/users/me
Authorization: Bearer {{token}}

### 4. Listar todos os usuários
GET {{baseUrl}}/users
Authorization: Bearer {{token}}

### 5. Buscar usuário por ID
GET {{baseUrl}}/users/1
Authorization: Bearer {{token}}

### ===== TRANSAÇÕES - CRUD =====

### 6. Criar nova transação (Despesa)
POST {{baseUrl}}/transaction
Authorization: Bearer {{token}}
Content-Type: application/json

{
"tipo": "D",
"valor": 150.50,
"idCategoria": 1,
"descricao": "Uber para o trabalho"
}

### 7. Criar transação (Receita)
POST {{baseUrl}}/transaction
Authorization: Bearer {{token}}
Content-Type: application/json

{
"tipo": "R",
"valor": 5000.00,
"idCategoria": 6,
"descricao": "Salário mensal"
}

### 8. Listar todas as transações
GET {{baseUrl}}/transaction
Authorization: Bearer {{token}}

### 9. Deletar transação por ID
DELETE {{baseUrl}}/transaction/1
Authorization: Bearer {{token}}

### ===== TRANSAÇÕES - FILTROS =====

### 10. Filtrar por tipo (Despesas)
GET {{baseUrl}}/transaction/tipo/D
Authorization: Bearer {{token}}

### 11. Filtrar por tipo (Receitas)
GET {{baseUrl}}/transaction/tipo/R
Authorization: Bearer {{token}}

### 12. Filtrar por mês e ano
GET {{baseUrl}}/transaction/mes?mes=2&ano=2025
Authorization: Bearer {{token}}

### 13. Filtrar por período de datas
GET {{baseUrl}}/transaction/periodo?inicio=2025-01-01&fim=2025-02-28
Authorization: Bearer {{token}}

### 14. Filtrar por categoria
GET {{baseUrl}}/transaction/categoria/1
Authorization: Bearer {{token}}

### ===== DASHBOARD =====

### 15. Resumo mensal (totais + breakdown por categoria)
GET {{baseUrl}}/transaction/dashboard/resumo?mes=2&ano=2025
Authorization: Bearer {{token}}

### 16. Evolução anual (mês a mês)
GET {{baseUrl}}/transaction/dashboard/evolucao?ano=2025
Authorization: Bearer {{token}}

### ===== ORÇAMENTOS (RF06 - Orçamento Mensal) =====

### 17. Criar orçamento mensal para uma categoria
# Define que o usuário quer gastar no máximo R$ 500 em "transport" em Fev/2025
POST {{baseUrl}}/orcamentos
Authorization: Bearer {{token}}
Content-Type: application/json

{
"idCategoria": 1,
"mes": 2,
"ano": 2025,
"limite": 500.00
}

### 18. Listar todos os orçamentos do usuário
GET {{baseUrl}}/orcamentos
Authorization: Bearer {{token}}

### 19. Buscar orçamento específico
GET {{baseUrl}}/orcamentos/1
Authorization: Bearer {{token}}

### 20. Listar orçamentos de um mês específico
GET {{baseUrl}}/orcamentos/mes?mes=2&ano=2025
Authorization: Bearer {{token}}

### 21. Buscar orçamento de uma categoria em um mês
GET {{baseUrl}}/orcamentos/categoria/1/mes?mes=2&ano=2025
Authorization: Bearer {{token}}

### 22. Atualizar limite de orçamento
PUT {{baseUrl}}/orcamentos/1
Authorization: Bearer {{token}}
Content-Type: application/json

{
"idCategoria": 1,
"mes": 2,
"ano": 2025,
"limite": 600.00
}

### 23. Deletar orçamento
DELETE {{baseUrl}}/orcamentos/1
Authorization: Bearer {{token}}

### ===== TESTE RF10: Notificação de Orçamento (80% do limite) =====

### 24. [SETUP] Criar orçamento de R$ 300 para transport em Fev/2025
POST {{baseUrl}}/orcamentos
Authorization: Bearer {{token}}
Content-Type: application/json

{
"idCategoria": 1,
"mes": 2,
"ano": 2025,
"limite": 300.00
}

### 25. [TESTE] Criar despesa de R$ 150 (50% do orçamento)
# Resposta: alertaOrcamento será FALSE (ainda é só 50%)
POST {{baseUrl}}/transaction
Authorization: Bearer {{token}}
Content-Type: application/json

{
"tipo": "D",
"valor": 150.00,
"idCategoria": 1,
"descricao": "Primeira despesa - sem alerta ainda"
}

### 26. [TESTE] Criar despesa de R$ 100 (total 250 = 83% do orçamento)
# Resposta: alertaOrcamento será TRUE (ultrapassou 80%!)
# ISSO ATIVA O ALERTA NO FRONTEND (RF10) ✅
POST {{baseUrl}}/transaction
Authorization: Bearer {{token}}
Content-Type: application/json

{
"tipo": "D",
"valor": 100.00,
"idCategoria": 1,
"descricao": "Segunda despesa - DEVE ativar alerta de orçamento"
}

### 27. [VERIFICAÇÃO] Consultar resumo do mês para ver o consumo
GET {{baseUrl}}/transaction/dashboard/resumo?mes=2&ano=2025
Authorization: Bearer {{token}}

### ===== TESTES DE ERRO =====

### 28. Teste: Login com email inválido
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
"email": "naoexiste@example.com",
"senha": "senha123"
}

### 29. Teste: Criar transação sem autenticação
POST {{baseUrl}}/transaction
Content-Type: application/json

{
"tipo": "D",
"valor": 100.00,
"idCategoria": 1
}

### 30. Teste: Token inválido (deve retornar 401)
GET {{baseUrl}}/transaction
Authorization: Bearer token_invalido_qualquer_coisa

### 31. Teste: Validação - valor negativo
POST {{baseUrl}}/transaction
Authorization: Bearer {{token}}
Content-Type: application/json

{
"tipo": "D",
"valor": -50.00,
"idCategoria": 1
}

### 32. Teste: Email duplicado no registro
POST {{baseUrl}}/users/register
Content-Type: application/json

{
"nome": "Outro Nome",
"email": "joao@example.com",
"senha": "senha456"
}

### 33. Teste: Orçamento duplicado (mesmo mês/categoria)
# Depois de criar um, tente criar outro igual
POST {{baseUrl}}/orcamentos
Authorization: Bearer {{token}}
Content-Type: application/json

{
"idCategoria": 1,
"mes": 2,
"ano": 2025,
"limite": 400.00
}

