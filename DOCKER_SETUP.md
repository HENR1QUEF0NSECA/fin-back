# Finance App - Backend 🏦

Backend do Finance App desenvolvido em **Spring Boot 4.0.2** com **PostgreSQL**.

## 📋 Pré-requisitos

- **Docker & Docker Compose** ([instalar aqui](https://www.docker.com/products/docker-desktop))
- **Git**

## 🚀 Início Rápido com Docker Compose

### 1️⃣ Clone e navegue para o projeto
```bash
cd C:finance-app-back
```

### 2️⃣ Crie o arquivo `.env`
O `docker-compose.yml` lê a senha do banco e o segredo do JWT do arquivo `.env` na raiz do projeto. Ele fica fora do Git (`.gitignore`) e fora da imagem (`.dockerignore`). Crie a partir do modelo versionado:
```powershell
Copy-Item .env.example .env
```
```bash
# Git Bash / Linux / macOS
cp .env.example .env
```

Depois edite o `.env` e preencha pelo menos `DB_PASSWORD` e `JWT_SECRET` (veja a seção **Variáveis de Ambiente** mais abaixo). Sem eles o compose para com a mensagem `defina DB_PASSWORD no arquivo .env` (ou `JWT_SECRET`).

> O `.\docker-start.ps1 up` cria o `.env` a partir do `.env.example` se ele não existir e pede para você revisar os valores antes de subir.

### 3️⃣ Inicie os containers (API + PostgreSQL)
```powershell
docker-compose up --build
```

**Esperado:**
- PostgreSQL rodando em `localhost:5432` (ou na porta de `POSTGRES_PORT`)
- API rodando em `http://localhost:8080`
- Banco `finance` criado automaticamente
- Segmentação de dados com 7 categorias iniciais (Transporte, Salário etc.)

**Para rodar em background:**
```powershell
docker-compose up -d --build
```

### 4️⃣ Parar os containers
```powershell
docker-compose down
```

**Remover dados (limpar volumes):**
```powershell
docker-compose down -v
```

> ⚠️ **Já tinha o volume do Postgres de antes?** O Postgres só define a senha na **primeira** criação do volume. Se o volume foi criado com outra senha, use essa mesma senha no `DB_PASSWORD` do `.env`, ou recrie o volume com `docker-compose down -v` (**apaga os dados do banco**). Senão a API falha com `password authentication failed for user "postgres"`.

---

## 🔗 Integração com o Frontend (Next.js)

### URL Base da API:
```
http://localhost:8080/api
```

O frontend do projeto é o **meu-norte-front** (Next.js, em `http://localhost:3000`). Ele não chama a API direto do navegador: as rotas do próprio Next repassam as chamadas pelo servidor, com o JWT guardado num cookie `httpOnly`. O endereço da API vem da variável `BACKEND_URL` do front (padrão `http://localhost:8080/api`), então com o Docker rodando basta fazer `npm ci` e `npm run dev` no front.

Detalhes no [README](./README.md#-integração-com-o-frontend-nextjs).

### CORS Configurado para:
- `http://localhost:3000` (Next.js)
- `http://localhost:4200` (outro front local, se precisar)

O meu-norte-front não depende de CORS, porque as chamadas saem do servidor do Next. A liberação serve para um front que chame a API direto do navegador.

---

## 📚 Fluxo de Authenticação

### 1️⃣ Registrar Usuário
```http
POST /api/auth/registro
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "senha123"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@example.com",
  "dataInclusao": "2025-02-21T10:30:00"
}
```

### 2️⃣ Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "joao@example.com",
  "senha": "senha123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "usuarioId": 1,
  "nome": "João Silva"
}
```

**Guarde o `token`** para mandar nas próximas requisições. O meu-norte-front guarda num cookie `httpOnly`, fora do alcance do JavaScript da página (veja a seção [Integração com o Frontend](#-integração-com-o-frontend-nextjs)).

### 3️⃣ Usar Token em Requisições
```http
GET /api/usuarios/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@example.com",
  "dataInclusao": "2025-02-21T10:30:00"
}
```

Só login e registro dispensam o token. Não há rota para listar usuários: cada um só vê os próprios dados.

---

## 💰 Endpoints Principais

### 📊 Transações

#### Criar Transação
```http
POST /api/transacoes
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "tipo": "D",
  "valor": 150.50,
  "categoriaId": 1,
  "descricao": "Uber para o trabalho"
}
```

A data/hora é a do momento da criação. A resposta traz `id`, `usuarioId`, `categoriaId`, `nomeCategoria`, `valor`, `dataHoraTransacao`, `descricao`, `tipo`, `tipoDescricao` e, só no POST, `alertaOrcamento` (`true` quando a despesa leva o gasto da categoria no mês a 80% ou mais do orçamento).

#### Listar todas as transações
```http
GET /api/transacoes
Authorization: Bearer {JWT_TOKEN}
```

#### Filtrar por tipo (Receita ou Despesa)
```http
GET /api/transacoes/tipo/D
Authorization: Bearer {JWT_TOKEN}
```

#### Filtrar por mês/ano
```http
GET /api/transacoes/mes?mes=2&ano=2025
Authorization: Bearer {JWT_TOKEN}
```

#### Filtrar por período
```http
GET /api/transacoes/periodo?inicio=2025-01-01&fim=2025-02-28
Authorization: Bearer {JWT_TOKEN}
```

#### Filtrar por categoria
```http
GET /api/transacoes/categoria/1
Authorization: Bearer {JWT_TOKEN}
```

#### Deletar transação
```http
DELETE /api/transacoes/5
Authorization: Bearer {JWT_TOKEN}
```

### 🧾 Orçamentos

```http
POST /api/orcamentos
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "categoriaId": 1,
  "mes": 2,
  "ano": 2025,
  "limite": 500.00
}
```

Também: `GET /api/orcamentos`, `GET|PUT|DELETE /api/orcamentos/{id}`, `GET /api/orcamentos/mes?mes=2&ano=2025` e `GET /api/orcamentos/categoria/{categoriaId}/mes?mes=2&ano=2025`. Detalhes no [README](./README.md#-orçamentos-orcamentocontroller).

### 📈 Dashboard

#### Resumo do mês
```http
GET /api/transacoes/dashboard/resumo?mes=2&ano=2025
Authorization: Bearer {JWT_TOKEN}

Response:
{
  "mes": 2,
  "ano": 2025,
  "totalReceitas": 5000.00,
  "totalDespesas": 1850.50,
  "saldo": 3149.50,
  "despesasPorCategoria": [
    { "categoria": "Transporte", "total": 450.00 },
    { "categoria": "Lazer", "total": 230.50 }
  ],
  "receitasPorCategoria": [
    { "categoria": "Salário", "total": 5000.00 }
  ]
}
```

#### Evolução anual (mês a mês)
```http
GET /api/transacoes/dashboard/evolucao?ano=2025
Authorization: Bearer {JWT_TOKEN}

Response:
{
  "ano": 2025,
  "receitas": [
    { "mes": 1, "total": 5000.00 },
    { "mes": 2, "total": 5000.00 }
  ],
  "despesas": [
    { "mes": 1, "total": 1200.00 },
    { "mes": 2, "total": 1850.50 }
  ]
}
```

---

## 🗂️ Categorias Disponíveis

As categorias são pré-carregadas via `data.sql` e listadas em `GET /api/categorias` (com token), que devolve `[{ "id": 1, "nome": "Transporte" }, ...]` em ordem alfabética:

1. **Transporte** - Uber, ônibus, gasolina
2. **Lazer** - cinema, streaming
3. **Contas da casa** - luz, água, internet
4. **Saúde** - farmácia, médico
5. **Compras** - compras em geral
6. **Salário** - receita
7. **Freelance** - trabalho freelancer (receita)

**Para adicionar nova categoria (via SQL direto):**
```sql
INSERT INTO categorias (id, nome) VALUES (8, 'Investimentos') ON CONFLICT (id) DO NOTHING;
```

Ou via interface SQL (DBeaver, pgAdmin) conectando em `localhost:5432` (ou na porta de `POSTGRES_PORT`):
- **Database:** finance
- **User:** o valor de `DB_USERNAME` no `.env` (padrão `postgres`)
- **Password:** o valor de `DB_PASSWORD` no seu `.env`

---

## 🧪 Testar pelo PowerShell

Os exemplos usam `Invoke-RestMethod`, que já monta o JSON e lê a resposta. Funciona no Windows PowerShell 5.1 e no PowerShell 7. (No 5.1, `curl` é apelido do `Invoke-WebRequest` e não aceita `-X`/`-H`/`-d`; o curl de verdade é o `curl.exe`.)

### Registrar
```powershell
$body = @{
    nome = "João"
    email = "joao@test.com"
    senha = "senha123"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/registro `
  -ContentType "application/json; charset=utf-8" -Body $body
```

### Login
```powershell
$body = @{
    email = "joao@test.com"
    senha = "senha123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json; charset=utf-8" -Body $body

$token = $response.token
Write-Host "Token: $token"
```

### Listar transações (com token)
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/transacoes `
  -Headers @{ Authorization = "Bearer $token" }
```

---

## 📊 Estrutura do Banco de Dados

```
usuarios (id, nome, email, senha, data_inclusao)
  ↓
transacoes (id_transacao, id_usuario, id_categoria, valor, tipo, descricao, dh_transacao)
  ↓
categorias (id, nome)
```

**Tipos de Transação:**
- `R` = Receita (entrada)
- `D` = Despesa (saída)

---

## ⚙️ Variáveis de Ambiente (Configuração)

Toda a configuração fica no `.env` (copiado do `.env.example`). **Não coloque senhas no `docker-compose.yml` nem no `application.yaml`**: os dois só leem as variáveis.

```env
DB_USERNAME=postgres
DB_PASSWORD=<sua-senha-do-postgres>
# Usado só ao rodar a API fora do Docker (./gradlew bootRun)
DB_URL=jdbc:postgresql://localhost:5432/finance
POSTGRES_PORT=5432
JWT_SECRET=<segredo-com-pelo-menos-32-caracteres>
JWT_EXPIRATION=86400000
```

| Variável | Obrigatória | Para que serve |
|----------|:-----------:|----------------|
| `DB_PASSWORD` | **Sim** | Senha do Postgres (usada pelo container do banco e pela API) |
| `JWT_SECRET` | **Sim** | Segredo dos tokens, com **no mínimo 32 caracteres**; gere com `openssl rand -base64 48`. Mais curto, a API não sobe |
| `DB_USERNAME` | Não (padrão `postgres`) | Usuário do Postgres |
| `POSTGRES_PORT` | Não (padrão `5432`) | Porta do Postgres no seu computador |
| `JWT_EXPIRATION` | Não (padrão `86400000`) | Validade do token em ms (24 horas) |
| `DB_URL` | Não | Só para rodar a API fora do Docker. Dentro do Docker o compose já usa `jdbc:postgresql://postgres:5432/finance` |

Depois de mudar o `.env`, recrie os containers com `docker-compose up -d --build`. (Mudar o `DB_PASSWORD` não altera a senha de um volume que já existe; veja o aviso no passo 4️⃣.)

---

## 🐛 Troubleshooting

### "Postgres connection refused"
```powershell
# Verificar se o container está rodando
docker-compose ps

# Ver logs
docker-compose logs postgres
```

### "Port 5432 already in use"
Defina outra porta no `.env` (a API dentro do Docker não é afetada):
```env
POSTGRES_PORT=5433
```
Se também roda a API fora do Docker, ajuste a porta no `DB_URL` (`jdbc:postgresql://localhost:5433/finance`).

### "required variable DB_PASSWORD is missing a value"
O `.env` não existe ou está sem `DB_PASSWORD`/`JWT_SECRET`. Veja o passo 2️⃣.

### "password authentication failed for user"
O volume do Postgres foi criado com outra senha. Use essa senha no `DB_PASSWORD` ou recrie o volume com `docker-compose down -v` (apaga os dados).

### "API não responde"
```powershell
# Reiniciar
docker-compose restart app

# Ver logs da API
docker-compose logs -f app
```

### "Token expirado"
Tokens expiram em 24 horas. Faça login novamente para obter novo token.

---

## 📝 Notas

- Banco de dados **persiste** em volumes Docker (dados não são perdidos ao reiniciar)
- Para **limpar tudo**, use: `docker-compose down -v`
- JWT expira em **24h** (86400000 ms) — ajuste em `JWT_EXPIRATION` no `.env` se necessário
- CORS está configurado para `localhost:3000` (front Next.js) e `localhost:4200`; adicione outras origens conforme necessário em `CorsConfig.java`
- O `Dockerfile` gera a imagem com `-x test`; os testes automatizados rodam separados, com `.\gradlew.bat test` (veja o [README](./README.md#-testes-automatizados))

---

## 📞 Suporte

Dúvidas? Verifique:
1. Se Postgres está rodando: `docker-compose ps`
2. Logs: `docker-compose logs app` e `docker-compose logs postgres`
3. Testes com cURL (exemplos acima)

**Bom desenvolvimento!** 🚀

