# Finance App - Documentação do Backend

📊 Backend de gerenciamento financeiro pessoal desenvolvido em **Spring Boot 4.0.2** com autenticação JWT.

## 🎯 Início Rápido

> **Quer começar já?** Leia o [QUICK_START.md](./QUICK_START.md) (5 minutos)

## 📋 Índice

- [Início Rápido](#-início-rápido)
- [Docker Setup](#-docker-setup)
- [Integração Angular](#-integração-angular)
- [Visão Geral](#-visão-geral)
- [Setup e Instalação](#-setup-e-instalação) (configuração pelo `.env`)
- [Testes Automatizados](#-testes-automatizados)
- [Autenticação JWT](#-autenticação-jwt)
- [Endpoints da API](#-endpoints-da-api)
  - [Autenticação](#-autenticação-authcontroller)
  - [Usuários](#-usuários-usuariocontroller)
  - [Categorias](#-categorias-categoriacontroller)
  - [Transações](#-transações-transacaocontroller)
  - [Orçamentos](#-orçamentos-orcamentocontroller)
- [Data Transfer Objects (DTOs)](#-data-transfer-objects-dtos)
- [Estrutura de Dados](#-estrutura-de-dados)
- [Códigos de Status HTTP](#-códigos-de-status-http)
- [Exemplos de Integração Frontend](#-exemplos-de-integração-frontend)

---

## 🐳 Docker Setup

### Para Deploy Completo com PostgreSQL

Veja a documentação completa em [DOCKER_SETUP.md](./DOCKER_SETUP.md)

```powershell
# Antes da primeira vez: criar o .env e preencher DB_PASSWORD e JWT_SECRET
Copy-Item .env.example .env

# Iniciar tudo com Docker Compose
docker-compose up --build

# Em background
docker-compose up -d --build

# Parar
docker-compose down
```

**Ou use o script PowerShell:**
```powershell
.\docker-start.ps1 up          # Inicia (cria o .env a partir do .env.example se ele não existir)
.\docker-start.ps1 down        # Para
.\docker-start.ps1 logs        # Mostra logs
```

**Esperado:**
- PostgreSQL: `localhost:5432` (ou a porta definida em `POSTGRES_PORT` no `.env`)
- API: `http://localhost:8080`

### Deploy Online (Opcional)

Quer compartilhar um link com seu professor? Veja [RAILWAY_DEPLOY.md](./RAILWAY_DEPLOY.md) para deploy gratuito na nuvem em 5 minutos.

---

## 🅰️ Integração Angular

### CORS já configurado ✅

O projeto já possui configuração de CORS para Angular local em `localhost:4200`.

### Teste Rápido de Endpoints

Use o arquivo [requests.http](./requests.http) com **VSCode REST Client**:
- Instale a extensão: REST Client
- Abra `requests.http`
- Clique em "Send Request" em cada teste

---

---

## 🎯 Visão Geral

O backend fornece uma API RESTful para gerenciar:
- **Autenticação de usuários** com JWT
- **Cadastro e perfil de usuários**
- **Transações financeiras** (receitas e despesas)
- **Categorias de transações** (pré-configuradas)
- **Orçamentos mensais por categoria**, com alerta ao atingir 80% do limite
- **Dashboard com resumos e gráficos**

### Tecnologias Utilizadas

- **Java 21** com **Spring Boot 4.0.2**
- **PostgreSQL** como banco de dados
- **JWT (JJWT)** para autenticação
- **JPA/Hibernate** para ORM
- **Validation (Jakarta)** para validação de dados
- **Gradle** para build
- **JUnit 5 + Mockito + H2** para os testes automatizados

### Requisitos

- Java 21+
- PostgreSQL 12+
- Gradle (ou usar `./gradlew`)

---

## 🔧 Setup e Instalação

### 1. Configurar Banco de Dados

```sql
-- Criar banco de dados
CREATE DATABASE finance;

-- Conectar ao banco
\c finance
```

### 2. Configurar as variáveis de ambiente (`.env`)

O `src/main/resources/application.yaml` **não guarda nenhum segredo** e não precisa ser editado. Senha do banco e segredo do JWT vêm de variáveis de ambiente, que a API lê do arquivo `.env` na raiz do projeto (`spring.config.import: optional:file:.env[.properties]`). O mesmo `.env` é usado pelo `docker-compose.yml`.

Crie o seu `.env` a partir do modelo versionado:

```bash
cp .env.example .env
```

```powershell
# PowerShell
Copy-Item .env.example .env
```

Depois preencha os valores:

| Variável | Obrigatória | Padrão | Descrição |
|----------|:-----------:|--------|-----------|
| `DB_URL` | Não | `jdbc:postgresql://localhost:5432/finance` | URL JDBC do banco (usada ao rodar a API fora do Docker) |
| `DB_USERNAME` | Não | `postgres` | Usuário do PostgreSQL |
| `DB_PASSWORD` | **Sim** | - | Senha do PostgreSQL |
| `POSTGRES_PORT` | Não | `5432` | Porta do Postgres do Docker no seu computador |
| `JWT_SECRET` | **Sim** | - | Segredo para assinar os tokens, com **no mínimo 32 caracteres** |
| `JWT_EXPIRATION` | Não | `86400000` | Validade do token em ms (24 horas) |

Para gerar um `JWT_SECRET`:

```bash
openssl rand -base64 48
```

```powershell
# PowerShell, sem openssl
$b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

> ⚠️ O `.env` está no `.gitignore` e no `.dockerignore`: **nunca faça commit dele**. Só o `.env.example`, com valores de exemplo, vai para o Git.
>
> Se o `JWT_SECRET` tiver menos de 32 caracteres, a API **não sobe** (o jjwt recusa a chave como fraca). Se `DB_PASSWORD` ou `JWT_SECRET` estiverem faltando, ela também não sobe.
>
> Escreva os valores sem aspas: ao rodar com `./gradlew bootRun`, o `.env` é lido como arquivo `.properties`, e aspas passariam a fazer parte do valor.

### 3. Build e Execução

Rode os comandos a partir da raiz do projeto (é lá que a API procura o `.env`):

```bash
# Build do projeto (também roda os testes automatizados)
./gradlew build

# Executar
./gradlew bootRun

# Ou executar o JAR gerado
java -jar build/libs/finance-app-0.0.1-SNAPSHOT.jar
```

No Windows, use `.\gradlew.bat` no lugar de `./gradlew`.

A API estará disponível em `http://localhost:8080`

### 4. Perfil `dev` (log de SQL)

Por padrão o SQL gerado pelo Hibernate não aparece no log (`show-sql: false`). Para vê-lo durante o desenvolvimento, ative o perfil `dev` (`src/main/resources/application-dev.yaml`):

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## 🧪 Testes Automatizados

```bash
./gradlew test
```

```powershell
# Windows
.\gradlew.bat test
```

São **37 testes**, e eles **não precisam de PostgreSQL nem de Docker**: rodam com o perfil `test`, que usa um banco H2 em memória (`src/test/resources/application-test.yaml`). O `.env` também não é necessário para os testes.

| Classe (`src/test/java`) | O que cobre |
|--------------------------|-------------|
| `ApiIntegracaoTest` | Ponta a ponta com MockMvc: cadastro, login e `/api/usuarios/me`; respostas 401, 403, 404 e 409; validação do body e dos parâmetros; alerta de orçamento em 80% |
| `UsuarioServiceTest` | Unitário (Mockito): e-mail normalizado e duplicado, senha com hash, usuário do token que não existe mais (401) |
| `AuthServiceTest` | Unitário (Mockito): login correto, senha errada e e-mail inexistente com a mesma mensagem |
| `JwtServiceTest` | Token válido, assinado com outro segredo, expirado, e segredo curto que impede a API de subir |
| `OrcamentoServiceTest` | Unitário (Mockito): orçamento repetido (409), de outro usuário (403), inexistente (404) e alerta a partir de 80% |
| `TransacaoServiceTest` | Unitário (Mockito): alerta só em despesa, categoria inexistente (400), período invertido (400), exclusão por outro usuário (403) |
| `ValidacaoDtoTest` | Anotações `@Mes`/`@Ano`, limite do orçamento, senha curta e valor com 3 casas decimais |

**Sem JDK instalado?** Dá para rodar os testes com Docker:

```powershell
docker run --rm -v "${PWD}:/app" -w /app gradle:8-jdk21-alpine gradle test --no-daemon
```

O `Dockerfile` continua gerando a imagem com `-x test` (os testes rodam separados, com o comando acima).

---

## 🔐 Autenticação JWT

### Fluxo de Autenticação

1. **Registro**: Novo usuário cria conta via `POST /api/auth/registro`
2. **Login**: Usuário faz login via `POST /api/auth/login`
3. **Token**: Backend retorna token JWT
4. **Requisições Autenticadas**: Cliente inclui token no header `Authorization: Bearer <token>`

### Estrutura do Token JWT

O token contém:
- `userId`: ID do usuário (nome interno da claim; na resposta do login o campo se chama `usuarioId`)
- `nome`: Nome do usuário
- `exp`: Data de expiração (24 horas por padrão, configurável em `JWT_EXPIRATION`)

### Exemplo de Token

```
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjgyNjAyMDAwfQ.signature...
```

### Rotas Públicas (sem autenticação)

- `POST /api/auth/login`
- `POST /api/auth/registro`

### Rotas Protegidas (requerem token)

Todas as outras rotas sob `/api/` requerem o header:
```
Authorization: Bearer seu_token_jwt
```

---

## 📡 Endpoints da API

### Base URL

```
http://localhost:8080
```

### Resumo das rotas

Todas recebem e devolvem JSON. Só login e registro dispensam o token.

| Método | Rota | Token | Descrição |
|--------|------|:-----:|-----------|
| POST | `/api/auth/login` | Não | Login, devolve o token JWT |
| POST | `/api/auth/registro` | Não | Cadastro de usuário |
| GET | `/api/usuarios/me` | Sim | Dados do usuário logado |
| GET | `/api/categorias` | Sim | Lista as categorias |
| POST | `/api/transacoes` | Sim | Cria transação |
| GET | `/api/transacoes` | Sim | Lista as transações do usuário |
| DELETE | `/api/transacoes/{id}` | Sim | Remove transação |
| GET | `/api/transacoes/tipo/{tipo}` | Sim | Filtra por tipo (`R` ou `D`) |
| GET | `/api/transacoes/mes?mes=&ano=` | Sim | Filtra por mês/ano |
| GET | `/api/transacoes/periodo?inicio=&fim=` | Sim | Filtra por intervalo de datas |
| GET | `/api/transacoes/categoria/{categoriaId}` | Sim | Filtra por categoria |
| GET | `/api/transacoes/dashboard/resumo?mes=&ano=` | Sim | Resumo do mês |
| GET | `/api/transacoes/dashboard/evolucao?ano=` | Sim | Evolução mês a mês no ano |
| POST | `/api/orcamentos` | Sim | Cria orçamento |
| GET | `/api/orcamentos` | Sim | Lista os orçamentos do usuário |
| GET | `/api/orcamentos/{id}` | Sim | Busca orçamento |
| PUT | `/api/orcamentos/{id}` | Sim | Atualiza orçamento |
| DELETE | `/api/orcamentos/{id}` | Sim | Remove orçamento |
| GET | `/api/orcamentos/mes?mes=&ano=` | Sim | Orçamentos de um mês |
| GET | `/api/orcamentos/categoria/{categoriaId}/mes?mes=&ano=` | Sim | Orçamento de uma categoria em um mês |

---

## 🔑 Autenticação (AuthController)

### Login

**POST** `/api/auth/login`

Realiza autenticação e retorna token JWT.

#### Request Body

```json
{
  "email": "usuario@example.com",
  "senha": "senha123"
}
```

#### Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjgyNjAyMDAwfQ...",
  "tipo": "Bearer",
  "usuarioId": 1,
  "nome": "João Silva"
}
```

#### Validações

- `email`: obrigatório, deve ser um email válido
- `senha`: obrigatória, não vazia

#### Exemplos de Erro

**401 Unauthorized** - Email ou senha inválidos (a mensagem não revela qual dos dois errou)

```json
{
  "status": 401,
  "erro": "Email ou senha inválidos",
  "timestamp": "2025-02-15T10:30:00"
}
```

---

### Registrar Novo Usuário

**POST** `/api/auth/registro`

Cadastra um novo usuário (rota pública).

#### Request Body

```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "senha123"
}
```

#### Response (201 Created)

```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@example.com",
  "dataInclusao": "2025-02-15T10:30:00"
}
```

#### Validações

- `nome`: obrigatório, 3-100 caracteres
- `email`: obrigatório, válido, até 150 caracteres e único (sem diferenciar maiúsculas: é gravado em minúsculas)
- `senha`: obrigatória, 6-72 caracteres (o BCrypt só considera os primeiros 72 bytes)

#### Exemplos de Erro

**400 Bad Request** - Campos inválidos

```json
{
  "status": 400,
  "erro": "Dados inválidos",
  "campos": {
    "senha": "Senha deve ter entre 6 e 72 caracteres"
  },
  "timestamp": "2025-02-15T10:30:00"
}
```

**409 Conflict** - Email já cadastrado

```json
{
  "status": 409,
  "erro": "Email já está em uso",
  "timestamp": "2025-02-15T10:30:00"
}
```

---

## 👤 Usuários (UsuarioController)

Não existe rota para listar usuários nem para consultar outro usuário por ID: cada usuário só enxerga os próprios dados, pelo `/me`.

### Obter Dados do Usuário Autenticado

**GET** `/api/usuarios/me`

Retorna os dados do dono do token. O `id` vem do token, mas os dados são lidos do banco.

#### Response (200 OK)

```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@example.com",
  "dataInclusao": "2025-02-15T10:30:00"
}
```

#### Exemplos de Erro

**401 Unauthorized** - Token ausente/inválido, ou o usuário do token não existe mais

```json
{
  "status": 401,
  "erro": "Usuário não encontrado",
  "timestamp": "2025-02-15T10:30:00"
}
```

---

## 🏷️ Categorias (CategoriaController)

### Listar Categorias

**GET** `/api/categorias`

Lista as categorias disponíveis, em ordem alfabética. Elas são compartilhadas entre todos os usuários (veja [Categorias Pré-carregadas](#-categorias-pré-carregadas)).

#### Response (200 OK)

```json
[
  { "id": 5, "nome": "Compras" },
  { "id": 3, "nome": "Contas da casa" },
  { "id": 7, "nome": "Freelance" },
  ...
]
```

---

## 💰 Transações (TransacaoController)

### Criar Transação

**POST** `/api/transacoes`

Cria uma nova transação para o usuário autenticado.

#### Request Body

```json
{
  "tipo": "D",
  "valor": 150.00,
  "categoriaId": 1,
  "descricao": "Uber para o trabalho"
}
```

#### Response (201 Created)

```json
{
  "id": 5,
  "usuarioId": 1,
  "categoriaId": 1,
  "nomeCategoria": "Transporte",
  "valor": 150.00,
  "dataHoraTransacao": "2025-02-15T10:30:00",
  "descricao": "Uber para o trabalho",
  "tipo": "D",
  "tipoDescricao": "Despesa",
  "alertaOrcamento": false
}
```

`alertaOrcamento` só aparece na resposta do POST (nas listagens ele é omitido):
- `true` quando a despesa fez o gasto da categoria no mês atingir **80% ou mais** do [orçamento](#-orçamentos-orcamentocontroller) daquele mês;
- `false` se não há orçamento para a categoria/mês, se ainda não chegou a 80%, ou se a transação é uma receita.

#### Validações

| Campo | Regra |
|-------|-------|
| `tipo` | obrigatório, "R" ou "D" |
| `valor` | obrigatório, > 0, máx 10 dígitos inteiros + 2 decimais |
| `categoriaId` | obrigatório, deve existir (senão 400 "Categoria não encontrada") |
| `descricao` | opcional, máx 255 caracteres |

A data/hora não é enviada: o back grava o momento da criação (`dataHoraTransacao`). Por isso o alerta de orçamento sempre se refere ao mês atual.

Um valor com tipo errado (ex.: `"tipo": "X"` ou `"valor": "abc"`) também volta como erro de campo:

```json
{
  "status": 400,
  "erro": "Dados inválidos",
  "campos": { "tipo": "Valor inválido. Valores aceitos: R, D" },
  "timestamp": "2025-02-15T10:30:00"
}
```

---

### Deletar Transação

**DELETE** `/api/transacoes/{id}`

Remove uma transação (apenas o dono pode deletar).

#### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| id | Long (path) | ID da transação |

#### Response (204 No Content)

Sem body - apenas indica sucesso.

#### Exemplos de Erro

**403 Forbidden** - Transação não pertence ao usuário

```json
{
  "status": 403,
  "erro": "Acesso negado: você não pode deletar esta transação",
  "timestamp": "2025-02-15T10:30:00"
}
```

**404 Not Found** - Transação não existe (`"erro": "Transação não encontrada"`)

---

### Listar Todas as Transações

**GET** `/api/transacoes`

Retorna todas as transações do usuário autenticado, da mais recente para a mais antiga.

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "categoriaId": 1,
    "nomeCategoria": "Transporte",
    "valor": 50.00,
    "dataHoraTransacao": "2025-02-14T09:00:00",
    "descricao": "Passagem de ônibus",
    "tipo": "D",
    "tipoDescricao": "Despesa"
  },
  {
    "id": 2,
    "usuarioId": 1,
    "categoriaId": 6,
    "nomeCategoria": "Salário",
    "valor": 5000.00,
    "dataHoraTransacao": "2025-02-01T08:00:00",
    "descricao": "Salário fevereiro",
    "tipo": "R",
    "tipoDescricao": "Receita"
  }
]
```

---

### Filtrar por Tipo

**GET** `/api/transacoes/tipo/{tipo}`

Filtra transações por tipo (receita ou despesa).

#### Parâmetros

| Parâmetro | Tipo | Descrição | Valores |
|-----------|------|-----------|---------|
| tipo | String (path) | Tipo de transação | "R" (Receita) ou "D" (Despesa) |

#### Exemplos

```
GET /api/transacoes/tipo/D
GET /api/transacoes/tipo/R
```

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "categoriaId": 1,
    "nomeCategoria": "Transporte",
    "valor": 50.00,
    "dataHoraTransacao": "2025-02-14T09:00:00",
    "descricao": "Passagem de ônibus",
    "tipo": "D",
    "tipoDescricao": "Despesa"
  }
]
```

---

### Filtrar por Mês e Ano

**GET** `/api/transacoes/mes?mes=2&ano=2025`

Retorna transações de um mês específico.

#### Query Parameters

| Parâmetro | Tipo | Descrição | Obrigatório |
|-----------|------|-----------|-------------|
| mes | Integer | Mês (1-12) | Sim |
| ano | Integer | Ano (ex: 2025) | Sim |

#### Exemplos

```
GET /api/transacoes/mes?mes=2&ano=2025
GET /api/transacoes/mes?mes=12&ano=2024
```

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "categoriaId": 1,
    "nomeCategoria": "Transporte",
    "valor": 150.00,
    "dataHoraTransacao": "2025-02-15T10:30:00",
    "descricao": "Uber para o trabalho",
    "tipo": "D",
    "tipoDescricao": "Despesa"
  }
]
```

---

### Filtrar por Período

**GET** `/api/transacoes/periodo?inicio=2025-01-01&fim=2025-02-28`

Retorna transações em um intervalo de datas.

#### Query Parameters

| Parâmetro | Tipo | Descrição | Formato | Obrigatório |
|-----------|------|-----------|---------|-------------|
| inicio | Date | Data inicial | YYYY-MM-DD | Sim |
| fim | Date | Data final | YYYY-MM-DD | Sim |

#### Exemplos

```
GET /api/transacoes/periodo?inicio=2025-01-01&fim=2025-02-28
GET /api/transacoes/periodo?inicio=2025-02-01&fim=2025-02-15
```

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "categoriaId": 1,
    "nomeCategoria": "Transporte",
    "valor": 150.00,
    "dataHoraTransacao": "2025-02-15T10:30:00",
    "descricao": "Uber para o trabalho",
    "tipo": "D",
    "tipoDescricao": "Despesa"
  }
]
```

---

### Filtrar por Categoria

**GET** `/api/transacoes/categoria/{categoriaId}`

Retorna transações filtradas por uma categoria específica (404 se a categoria não existe).

#### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| categoriaId | Integer (path) | ID da categoria |

#### Exemplos

```
GET /api/transacoes/categoria/1
GET /api/transacoes/categoria/6
```

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "categoriaId": 1,
    "nomeCategoria": "Transporte",
    "valor": 50.00,
    "dataHoraTransacao": "2025-02-14T09:00:00",
    "descricao": "Passagem de ônibus",
    "tipo": "D",
    "tipoDescricao": "Despesa"
  }
]
```

---

### Resumo Mensal (Dashboard)

**GET** `/api/transacoes/dashboard/resumo?mes=2&ano=2025`

Retorna resumo financeiro do mês com totais e breakdown por categoria. Ideal para cards e gráfico de pizza.

#### Query Parameters

| Parâmetro | Tipo | Descrição | Obrigatório |
|-----------|------|-----------|-------------|
| mes | Integer | Mês (1-12) | Sim |
| ano | Integer | Ano | Sim |

#### Response (200 OK)

```json
{
  "mes": 2,
  "ano": 2025,
  "totalReceitas": 5000.00,
  "totalDespesas": 1200.50,
  "saldo": 3799.50,
  "despesasPorCategoria": [
    {
      "categoria": "Transporte",
      "total": 150.00
    },
    {
      "categoria": "Compras",
      "total": 450.50
    },
    {
      "categoria": "Lazer",
      "total": 600.00
    }
  ],
  "receitasPorCategoria": [
    {
      "categoria": "Salário",
      "total": 5000.00
    }
  ]
}
```

---

### Evolução Anual (Dashboard)

**GET** `/api/transacoes/dashboard/evolucao?ano=2025`

Retorna totais mês a mês do ano. Ideal para gráfico de linha ou barras.

#### Query Parameters

| Parâmetro | Tipo | Descrição | Obrigatório |
|-----------|------|-----------|-------------|
| ano | Integer | Ano | Sim |

#### Response (200 OK)

```json
{
  "ano": 2025,
  "receitas": [
    { "mes": 1, "total": 5000.00 },
    { "mes": 2, "total": 5000.00 },
    { "mes": 3, "total": 5500.00 }
  ],
  "despesas": [
    { "mes": 1, "total": 800.00 },
    { "mes": 2, "total": 1200.50 },
    { "mes": 3, "total": 950.00 }
  ]
}
```

---

## 🧾 Orçamentos (OrcamentoController)

Limite mensal de gasto por categoria. Cada usuário pode ter **um orçamento por categoria em cada mês/ano**. Todas as rotas exigem token, e só o dono acessa o próprio orçamento.

| Método | Rota | Resposta |
|--------|------|----------|
| POST | `/api/orcamentos` | 201 com o orçamento criado |
| GET | `/api/orcamentos` | 200 com a lista do usuário (mais recentes primeiro) |
| GET | `/api/orcamentos/{id}` | 200 |
| PUT | `/api/orcamentos/{id}` | 200 (atualiza categoria, mês, ano e limite) |
| DELETE | `/api/orcamentos/{id}` | 204 (as transações continuam; só o limite some) |
| GET | `/api/orcamentos/mes?mes=2&ano=2025` | 200 com os orçamentos do mês |
| GET | `/api/orcamentos/categoria/{categoriaId}/mes?mes=2&ano=2025` | 200, ou 404 se não houver orçamento para a categoria no mês |

### Criar Orçamento

**POST** `/api/orcamentos` (o PUT usa o mesmo body)

#### Request Body

```json
{
  "categoriaId": 1,
  "mes": 2,
  "ano": 2025,
  "limite": 300.00
}
```

#### Response (201 Created)

```json
{
  "id": 1,
  "usuarioId": 1,
  "categoriaId": 1,
  "nomeCategoria": "Transporte",
  "mes": 2,
  "ano": 2025,
  "limite": 300.00,
  "consumido": 250.00,
  "percentualUtilizado": 83.0,
  "saldo": 50.00,
  "alertaAtivo": true,
  "dataCriacao": "2025-02-15T10:30:00",
  "dataAtualizacao": "2025-02-15T10:30:00"
}
```

- `consumido`: soma das **despesas** do usuário na categoria naquele mês/ano.
- `percentualUtilizado`: `consumido / limite`, em porcentagem.
- `saldo`: `limite - consumido` (nunca negativo).
- `alertaAtivo`: `true` a partir de 80% do limite.

#### Erros comuns

| Status | Quando |
|--------|--------|
| 400 | Campo inválido (`mes` fora de 1-12, `ano` fora de 2000-2100, `limite` <= 0) ou categoria inexistente |
| 403 | O orçamento pertence a outro usuário |
| 404 | Orçamento não encontrado |
| 409 | Já existe orçamento para a categoria no mês/ano informado |

---

## 📦 Data Transfer Objects (DTOs)

### LoginDTO

Utilizado no login de usuários.

```json
{
  "email": "usuario@example.com",
  "senha": "senha123"
}
```

| Campo | Tipo | Validação | Descricao |
|-------|------|-----------|-----------|
| email | String | obrigatório, email válido | E-mail do usuário |
| senha | String | obrigatória, não vazia | Senha do usuário |

---

### AuthResponseDTO

Retornado após login bem-sucedido.

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "usuarioId": 1,
  "nome": "João Silva"
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| token | String | Token JWT para autenticação |
| tipo | String | Sempre "Bearer" |
| usuarioId | Long | ID do usuário |
| nome | String | Nome do usuário |

---

### UsuarioRequestDTO

Utilizado no registro de novos usuários (`POST /api/auth/registro`).

```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "senha123"
}
```

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| nome | String | obrigatório, 3-100 caracteres | Nome completo |
| email | String | obrigatório, email válido, máx 150, único | E-mail (gravado em minúsculas) |
| senha | String | obrigatória, 6-72 caracteres | Senha (será hasheada) |

---

### UsuarioResponseDTO

Retornado no registro e em `GET /api/usuarios/me`. Nunca inclui a senha.

```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@example.com",
  "dataInclusao": "2025-02-15T10:30:00"
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | ID único do usuário |
| nome | String | Nome do usuário |
| email | String | E-mail do usuário |
| dataInclusao | LocalDateTime | Data/hora de criação |

---

### CategoriaResponseDTO

Retornado em `GET /api/categorias`.

```json
{ "id": 1, "nome": "Transporte" }
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Integer | ID da categoria |
| nome | String | Nome da categoria |

---

### TransacaoRequestDTO

Utilizado na criação de transações.

```json
{
  "tipo": "D",
  "valor": 150.00,
  "categoriaId": 1,
  "descricao": "Uber para o trabalho"
}
```

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| tipo | TipoTransacao | obrigatório | "R" (Receita) ou "D" (Despesa) |
| valor | BigDecimal | obrigatório, > 0, máx 10 dígitos inteiros + 2 decimais | Valor da transação |
| categoriaId | Integer | obrigatório | ID da categoria |
| descricao | String | opcional, até 255 caracteres | Descrição da transação |

Não há campo de data: a transação é gravada com a data/hora do momento da criação.

---

### TransacaoResponseDTO

Retornado nas operações com transações.

```json
{
  "id": 5,
  "usuarioId": 1,
  "categoriaId": 1,
  "nomeCategoria": "Transporte",
  "valor": 150.00,
  "dataHoraTransacao": "2025-02-15T10:30:00",
  "descricao": "Uber para o trabalho",
  "tipo": "D",
  "tipoDescricao": "Despesa",
  "alertaOrcamento": false
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | ID único da transação |
| usuarioId | Long | ID do usuário proprietário |
| categoriaId | Integer | ID da categoria |
| nomeCategoria | String | Nome da categoria |
| valor | BigDecimal | Valor da transação |
| dataHoraTransacao | LocalDateTime | Data/hora da criação |
| descricao | String | Descrição |
| tipo | String | "R" ou "D" |
| tipoDescricao | String | "Receita" ou "Despesa" |
| alertaOrcamento | Boolean | Só no POST: `true` se a despesa levou o gasto da categoria no mês a 80% ou mais do orçamento |

---

### OrcamentoRequestDTO

Utilizado na criação e na atualização de orçamentos.

```json
{
  "categoriaId": 1,
  "mes": 2,
  "ano": 2025,
  "limite": 500.00
}
```

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| categoriaId | Integer | obrigatório, deve existir | ID da categoria |
| mes | Integer | obrigatório, `@Mes` (1 a 12) | Mês do orçamento |
| ano | Integer | obrigatório, `@Ano` (2000 a 2100) | Ano do orçamento |
| limite | BigDecimal | obrigatório, > 0, máx 10 dígitos inteiros + 2 decimais | Limite de gasto no mês |

---

### OrcamentoResponseDTO

Retornado nas operações com orçamentos (exemplo completo em [Orçamentos](#-orçamentos-orcamentocontroller)).

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | ID do orçamento |
| usuarioId | Long | ID do usuário dono |
| categoriaId | Integer | ID da categoria |
| nomeCategoria | String | Nome da categoria |
| mes | Integer | Mês (1-12) |
| ano | Integer | Ano |
| limite | BigDecimal | Limite definido |
| consumido | BigDecimal | Total de despesas na categoria no mês |
| percentualUtilizado | Double | Percentual do limite já utilizado |
| saldo | BigDecimal | Quanto ainda pode ser gasto (mínimo 0) |
| alertaAtivo | Boolean | `true` a partir de 80% do limite |
| dataCriacao | LocalDateTime | Data/hora de criação |
| dataAtualizacao | LocalDateTime | Data/hora da última atualização |

---

### ResumoMensalDTO

Resumo mensal para dashboard.

```json
{
  "mes": 2,
  "ano": 2025,
  "totalReceitas": 5000.00,
  "totalDespesas": 1200.50,
  "saldo": 3799.50,
  "despesasPorCategoria": [
    { "categoria": "Transporte", "total": 150.00 }
  ],
  "receitasPorCategoria": [
    { "categoria": "Salário", "total": 5000.00 }
  ]
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| mes | Integer | Mês (1-12) |
| ano | Integer | Ano |
| totalReceitas | BigDecimal | Total de receitas do mês |
| totalDespesas | BigDecimal | Total de despesas do mês |
| saldo | BigDecimal | Saldo (receitas - despesas) |
| despesasPorCategoria | List<GraficoCategoriaDTO> | Breakdown de despesas |
| receitasPorCategoria | List<GraficoCategoriaDTO> | Breakdown de receitas |

---

### EvolucaoMensalDTO

Evolução anual para gráficos de linha/barras.

```json
{
  "ano": 2025,
  "receitas": [
    { "mes": 1, "total": 5000.00 },
    { "mes": 2, "total": 5000.00 }
  ],
  "despesas": [
    { "mes": 1, "total": 800.00 },
    { "mes": 2, "total": 1200.50 }
  ]
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| ano | Integer | Ano |
| receitas | List<PontoMensalDTO> | Lista de receitas por mês |
| despesas | List<PontoMensalDTO> | Lista de despesas por mês |

#### PontoMensalDTO (inner class)

```json
{ "mes": 1, "total": 5000.00 }
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| mes | Integer | Mês (1-12) |
| total | BigDecimal | Total do mês |

---

### GraficoCategoriaDTO

Representa um slice do gráfico (categoria + total).

```json
{
  "categoria": "Transporte",
  "total": 150.00
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| categoria | String | Nome da categoria |
| total | BigDecimal | Total acumulado |

---

## 📊 Estrutura de Dados

### Entidades do Banco de Dados

#### usuarios

```sql
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    data_inclusao TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### categorias

```sql
CREATE TABLE categorias (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Categorias pré-carregadas via data.sql
-- Transporte, Lazer, Contas da casa, Saúde, Compras, Salário, Freelance
```

#### transacoes

```sql
CREATE TABLE transacoes (
    id_transacao BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_categoria INT NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    dh_transacao TIMESTAMP NOT NULL,
    descricao VARCHAR(255),
    tipo VARCHAR(1) NOT NULL,  -- 'R' ou 'D'
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_categoria) REFERENCES categorias(id)
);
```

### Enum TipoTransacao

```java
public enum TipoTransacao {
    R("Receita"),    // Renda, receitas
    D("Despesa")     // Despesas
}
```

Salvo no banco como `VARCHAR(1)` com valores 'R' ou 'D'.

---

## 📝 Códigos de Status HTTP

| Código | Significado | Descrição |
|--------|-----------|-----------|
| 200 | OK | Requisição bem-sucedida |
| 201 | Created | Recurso criado com sucesso |
| 204 | No Content | Sucesso, sem conteúdo (deletar) |
| 400 | Bad Request | Dados inválidos, incompletos ou JSON mal formado |
| 401 | Unauthorized | Token ausente/inválido ou login com email/senha errados |
| 403 | Forbidden | O recurso pertence a outro usuário |
| 404 | Not Found | Recurso ou rota não encontrados |
| 405 | Method Not Allowed | Método HTTP não aceito pela rota (header `Allow` lista os aceitos) |
| 409 | Conflict | Dado duplicado (email já usado, orçamento repetido no mês) |
| 415 | Unsupported Media Type | Corpo enviado sem `Content-Type: application/json` |
| 500 | Internal Server Error | Erro inesperado no servidor (detalhe só no log) |

### Formato das respostas de erro

Todo erro, inclusive o 401 do filtro JWT, sai do `GlobalExceptionHandler` no mesmo formato:

```json
{
  "status": 400,
  "erro": "Dados inválidos",
  "campos": {
    "mes": "Mês deve ser entre 1 e 12",
    "ano": "Ano deve ser entre 2000 e 2100"
  },
  "timestamp": "2025-02-15T10:30:00"
}
```

- `erro`: mensagem em português, pronta para mostrar ao usuário.
- `campos`: só em erro de validação, com uma mensagem por campo (do body ou da query).
- Nunca expõe SQL, nomes de classes Java ou stack trace.

Os services lançam exceções próprias (pacote `exception`), e o status vem do tipo da exceção:

| Exceção | Status |
|---------|--------|
| `RequisicaoInvalidaException` | 400 |
| `NaoAutenticadoException` | 401 |
| `AcessoNegadoException` | 403 |
| `RecursoNaoEncontradoException` | 404 |
| `ConflitoException` | 409 |

Mês e ano são validados pelas anotações `@Mes` (1 a 12) e `@Ano` (2000 a 2100), do pacote
`validation`, tanto no body dos orçamentos quanto nos parâmetros `?mes=` e `?ano=`.

---

## 🔌 Exemplos de Integração Frontend

### 1. Com Axios (JavaScript/React)

#### Instalação

```bash
npm install axios
```

#### Configuração

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para adicionar token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

#### Exemplos de Uso

**Registro**

```javascript
async function registrar(nome, email, senha) {
  try {
    const response = await api.post('/auth/registro', {
      nome,
      email,
      senha
    });
    console.log('Usuário criado:', response.data);
    return response.data;
  } catch (error) {
    console.error('Erro ao registrar:', error.response.data);
  }
}
```

**Login**

```javascript
async function login(email, senha) {
  try {
    const response = await api.post('/auth/login', {
      email,
      senha
    });
    const { token, usuarioId, nome } = response.data;
    
    // Salvar token no localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('usuarioId', usuarioId);
    localStorage.setItem('userName', nome);
    
    console.log('Login bem-sucedido!');
    return response.data;
  } catch (error) {
    console.error('Erro ao fazer login:', error.response.data);
  }
}
```

**Criar Transação**

```javascript
async function criarTransacao(tipo, valor, categoriaId, descricao) {
  try {
    const response = await api.post('/transacoes', {
      tipo,
      valor,
      categoriaId,
      descricao
    });
    console.log('Transação criada:', response.data);
    if (response.data.alertaOrcamento) {
      console.warn('Atenção: você já usou 80% ou mais do orçamento desta categoria no mês');
    }
    return response.data;
  } catch (error) {
    console.error('Erro ao criar transação:', error.response.data);
  }
}
```

**Listar Transações**

```javascript
async function listarTransacoes() {
  try {
    const response = await api.get('/transacoes');
    console.log('Transações:', response.data);
    return response.data;
  } catch (error) {
    console.error('Erro ao listar transações:', error.response.data);
  }
}
```

**Filtrar por Mês**

```javascript
async function listarTransacoesMes(mes, ano) {
  try {
    const response = await api.get('/transacoes/mes', {
      params: { mes, ano }
    });
    console.log(`Transações de ${mes}/${ano}:`, response.data);
    return response.data;
  } catch (error) {
    console.error('Erro ao listar transações:', error.response.data);
  }
}
```

**Resumo Mensal**

```javascript
async function obterResumoMensal(mes, ano) {
  try {
    const response = await api.get('/transacoes/dashboard/resumo', {
      params: { mes, ano }
    });
    console.log('Resumo:', response.data);
    return response.data;
  } catch (error) {
    console.error('Erro ao obter resumo:', error.response.data);
  }
}
```

**Evolução Anual**

```javascript
async function obterEvolucaoAnual(ano) {
  try {
    const response = await api.get('/transacoes/dashboard/evolucao', {
      params: { ano }
    });
    console.log('Evolução:', response.data);
    return response.data;
  } catch (error) {
    console.error('Erro ao obter evolução:', error.response.data);
  }
}
```

**Deletar Transação**

```javascript
async function deletarTransacao(id) {
  try {
    await api.delete(`/transacoes/${id}`);
    console.log('Transação deletada!');
  } catch (error) {
    console.error('Erro ao deletar transação:', error.response.data);
  }
}
```

---

### 2. Com Fetch API (JavaScript Vanilla)

#### Helpers

```javascript
const API_BASE_URL = 'http://localhost:8080/api';

async function fetchAPI(endpoint, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    const error = await response.json();
    // Formato padrão de erro da API: { status, erro, campos?, timestamp }
    throw new Error(error.erro || 'Erro na requisição');
  }

  return response.status === 204 ? null : response.json();
}
```

#### Exemplos

**Login**

```javascript
async function login(email, senha) {
  const data = await fetchAPI('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, senha })
  });

  localStorage.setItem('token', data.token);
  localStorage.setItem('usuarioId', data.usuarioId);
  localStorage.setItem('userName', data.nome);
  
  console.log('Login bem-sucedido!');
}
```

**Listar Transações**

```javascript
async function listarTransacoes() {
  return fetchAPI('/transacoes', { method: 'GET' });
}
```

---

### 3. Com cURL (Testes)

**Registro**

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@example.com",
    "senha": "senha123"
  }'
```

**Login**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "senha": "senha123"
  }'
```

**Criar Transação** (com token)

```bash
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu_token_aqui" \
  -d '{
    "tipo": "D",
    "valor": 150.00,
    "categoriaId": 1,
    "descricao": "Uber para o trabalho"
  }'
```

**Listar Transações** (com token)

```bash
curl -X GET http://localhost:8080/api/transacoes \
  -H "Authorization: Bearer seu_token_aqui"
```

**Resumo Mensal** (com token)

```bash
curl -X GET "http://localhost:8080/api/transacoes/dashboard/resumo?mes=2&ano=2025" \
  -H "Authorization: Bearer seu_token_aqui"
```

**Dados do usuário logado** (com token)

```bash
curl -X GET http://localhost:8080/api/usuarios/me \
  -H "Authorization: Bearer seu_token_aqui"
```

**Listar categorias** (com token)

```bash
curl -X GET http://localhost:8080/api/categorias \
  -H "Authorization: Bearer seu_token_aqui"
```

---

## 🌍 Variáveis de Ambiente do Frontend

As variáveis do **backend** ficam no `.env` (veja [Setup e Instalação](#-setup-e-instalação)). As abaixo são do **frontend**, para facilitar testes em diferentes ambientes:

### .env.development

```env
REACT_APP_API_URL=http://localhost:8080/api
```

### .env.production

```env
REACT_APP_API_URL=https://api.finance-app.com/api
```

### Uso no Frontend

```javascript
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

---

## 📋 Categorias Pré-carregadas

As categorias são inseridas via `src/main/resources/data.sql` ao iniciar a aplicação e podem ser consultadas em `GET /api/categorias`:

| ID | Nome |
|:--:|------|
| 1 | Transporte |
| 2 | Lazer |
| 3 | Contas da casa |
| 4 | Saúde |
| 5 | Compras |
| 6 | Salário |
| 7 | Freelance |

O `data.sql` roda a cada inicialização sem duplicar nada, e atualiza o nome das categorias que já existem (bancos antigos, com os nomes em inglês, passam a ver os nomes em português).

Para adicionar uma nova categoria, inclua uma linha no `data.sql` com o próximo id, por exemplo `(8, 'Investimentos')`, ou insira direto no banco:

```sql
INSERT INTO categorias (nome) VALUES ('Investimentos');
```

---

## 🚀 Boas Práticas de Integração

1. **Armazenar Token**: Guarde o token JWT no `localStorage` ou `sessionStorage`
2. **Remover Token**: Limpe o token ao fazer logout
3. **Validar Entrada**: Sempre valide dados no frontend antes de enviar
4. **Tratar Erros**: Implemente tratamento de erros adequado
5. **Interceptadores**: Use interceptadores para adicionar token automaticamente
6. **CORS**: Certifique-se de que o CORS está configurado corretamente
7. **Timeouts**: Implemente timeouts para requisições longas

---

## 🔒 Segurança

- **Senhas**: Sempre hasheadas com BCrypt (Spring Security Crypto)
- **JWT**: Token com expiração de 24 horas por padrão (`JWT_EXPIRATION`)
- **Segredos**: Senha do banco e `JWT_SECRET` ficam no `.env`, fora do Git e da imagem Docker; o `application.yaml` não guarda nenhum segredo
- **Rotas Protegidas**: Todas as rotas exceto login e registro requerem autenticação
- **Dados por usuário**: Cada usuário só acessa os próprios dados, transações e orçamentos
- **CORS**: Configure conforme necessário no `CorsConfig`
- **HTTPS**: Use HTTPS em produção

---

## 📞 Suporte e Dúvidas

Para dúvidas sobre integração ou problemas técnicos, verifique:

1. Se o servidor está rodando em `http://localhost:8080`
2. Se o `.env` existe na raiz do projeto, com `DB_PASSWORD` e `JWT_SECRET` (mínimo 32 caracteres) preenchidos
3. Se o banco de dados PostgreSQL está configurado corretamente
4. Se o token JWT está sendo enviado corretamente no header `Authorization`
5. Se as credenciais do usuário estão corretas

Veja também o [TROUBLESHOOTING.md](./TROUBLESHOOTING.md).

---

## 📄 Licença

Este projeto é proprietário e desenvolvido para fins educacionais/internos.

---

**Última atualização**: Novembro de 2026
**Versão da API**: 1.0.0

