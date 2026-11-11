# Finance App - Documentação do Backend

📊 Backend de gerenciamento financeiro pessoal desenvolvido em **Spring Boot 4.0.2** com autenticação JWT.

## 🎯 Início Rápido

> **Quer começar já?** Leia o [QUICK_START.md](./QUICK_START.md) (5 minutos)

## 📋 Índice

- [Início Rápido](#-início-rápido)
- [Docker Setup](#-docker-setup)
- [Integração Angular](#-integração-angular)
- [Visão Geral](#visão-geral)
- [Setup e Instalação](#setup-e-instalação)
- [Autenticação JWT](#autenticação-jwt)
- [Endpoints da API](#endpoints-da-api)
  - [Autenticação](#autenticação-authcontroller)
  - [Usuários](#usuários-usercontroller)
  - [Transações](#transações-transacaocontroller)
- [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
- [Estrutura de Dados](#estrutura-de-dados)
- [Códigos de Status HTTP](#códigos-de-status-http)
- [Exemplos de Integração Frontend](#exemplos-de-integração-frontend)

---

## 🐳 Docker Setup

### Para Deploy Completo com PostgreSQL

Veja a documentação completa em [DOCKER_SETUP.md](./DOCKER_SETUP.md)

```powershell
# Iniciar tudo com Docker Compose
docker-compose up --build

# Em background
docker-compose up -d --build

# Parar
docker-compose down
```

**Ou use o script PowerShell:**
```powershell
.\docker-start.ps1 up          # Inicia
.\docker-start.ps1 down        # Para
.\docker-start.ps1 logs        # Mostra logs
```

**Esperado:**
- PostgreSQL: `localhost:5432`
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
- **Dashboard com resumos e gráficos**

### Tecnologias Utilizadas

- **Java 21** com **Spring Boot 4.0.2**
- **PostgreSQL** como banco de dados
- **JWT (JJWT)** para autenticação
- **JPA/Hibernate** para ORM
- **Validation (Jakarta)** para validação de dados
- **Gradle** para build

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

### 2. Configurar application.yaml

Edite o arquivo `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance
    username: postgres
    password: sua_senha_postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    defer-datasource-initialization: true
  sql:
    init:
      mode: always

server:
  port: 8080

jwt:
  secret: fHkl980UGl8cOfVCyfu518RnI37PtZt4C0xbJj1vw4IqdCMWlaXwgv8zGsIY8zxE
  expiration: 86400000  # 24 horas em ms
```

### 3. Build e Execução

```bash
# Build do projeto
./gradlew build

# Executar
./gradlew bootRun

# Ou executar o JAR gerado
java -jar build/libs/finance-app-back-0.0.1-SNAPSHOT.jar
```

A API estará disponível em `http://localhost:8080`

---

## 🔐 Autenticação JWT

### Fluxo de Autenticação

1. **Registro**: Novo usuário cria conta via `/api/users/register`
2. **Login**: Usuário faz login via `/api/auth/login`
3. **Token**: Backend retorna token JWT
4. **Requisições Autenticadas**: Cliente inclui token no header `Authorization: Bearer <token>`

### Estrutura do Token JWT

O token contém:
- `userId`: ID do usuário
- `nome`: Nome do usuário
- `exp`: Data de expiração (24 horas por padrão)

### Exemplo de Token

```
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjgyNjAyMDAwfQ.signature...
```

### Rotas Públicas (sem autenticação)

- `POST /api/auth/login`
- `POST /api/users/register`

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

#### Response (201 Created)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjgyNjAyMDAwfQ...",
  "tipo": "Bearer",
  "userId": 1,
  "nome": "João Silva"
}
```

#### Validações

- `email`: obrigatório, deve ser um email válido
- `senha`: obrigatória, não vazia

#### Exemplos de Erro

**400 Bad Request** - Email ou senha inválidos

```json
{
  "timestamp": "2025-02-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email ou senha inválidos"
}
```

---

## 👤 Usuários (UserController)

### Registrar Novo Usuário

**POST** `/api/users/register`

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
- `email`: obrigatório, deve ser válido e único
- `senha`: obrigatória, não vazia

#### Exemplos de Erro

**400 Bad Request** - Email já cadastrado

```json
{
  "timestamp": "2025-02-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email já está registrado"
}
```

---

### Buscar Usuário por ID

**GET** `/api/users/{id}`

Retorna informações de um usuário específico (requer autenticação).

#### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| id | Long (path) | ID do usuário |

#### Response (200 OK)

```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@example.com",
  "dataInclusao": "2025-02-15T10:30:00"
}
```

---

### Listar Todos os Usuários

**GET** `/api/users`

Lista todos os usuários cadastrados (requer autenticação).

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "nome": "João Silva",
    "email": "joao@example.com",
    "dataInclusao": "2025-02-15T10:30:00"
  },
  {
    "id": 2,
    "nome": "Maria Santos",
    "email": "maria@example.com",
    "dataInclusao": "2025-02-14T09:15:00"
  }
]
```

---

### Obter Dados do Usuário Autenticado

**GET** `/api/users/me`

Retorna ID e nome do usuário autenticado (extraídos do token).

#### Response (200 OK)

```json
{
  "userId": 1,
  "nome": "João Silva",
  "mensagem": "Você está autenticado!"
}
```

---

## 💰 Transações (TransacaoController)

### Criar Transação

**POST** `/api/transaction`

Cria uma nova transação para o usuário autenticado.

#### Request Body

```json
{
  "tipo": "D",
  "valor": 150.00,
  "idCategoria": 1,
  "descricao": "Uber para o trabalho",
  "dataTransacao": "2025-02-15"
}
```

#### Response (201 Created)

```json
{
  "idTransacao": 5,
  "idUsuario": 1,
  "idCategoria": 1,
  "nomeCategoria": "transporte",
  "valor": 150.00,
  "dataHoraTransacao": "2025-02-15T10:30:00",
  "descricao": "Uber para o trabalho",
  "tipo": "D",
  "tipoDescricao": "Despesa"
}
```

#### Validações

| Campo | Regra |
|-------|-------|
| `tipo` | obrigatório, "R" ou "D" |
| `valor` | obrigatório, > 0, máx 10 dígitos inteiros + 2 decimais |
| `idCategoria` | obrigatório, deve existir |
| `descricao` | opcional, máx 255 caracteres |
| `dataTransacao` | obrigatório (formato: YYYY-MM-DD) |

**Importante:** O campo `dataTransacao` não está no DTO por padrão. Você precisa adicionar:

```java
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
private LocalDate dataTransacao;
```

---

### Deletar Transação

**DELETE** `/api/transaction/{id}`

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
  "timestamp": "2025-02-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Você não tem permissão para deletar esta transação"
}
```

---

### Listar Todas as Transações

**GET** `/api/transaction`

Retorna todas as transações do usuário autenticado.

#### Response (200 OK)

```json
[
  {
    "idTransacao": 1,
    "idUsuario": 1,
    "idCategoria": 1,
    "nomeCategoria": "transporte",
    "valor": 50.00,
    "dataHoraTransacao": "2025-02-14T09:00:00",
    "descricao": "Passagem de ônibus",
    "tipo": "D",
    "tipoDescricao": "Despesa"
  },
  {
    "idTransacao": 2,
    "idUsuario": 1,
    "idCategoria": 5,
    "nomeCategoria": "salário",
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

**GET** `/api/transaction/tipo/{tipo}`

Filtra transações por tipo (receita ou despesa).

#### Parâmetros

| Parâmetro | Tipo | Descrição | Valores |
|-----------|------|-----------|---------|
| tipo | String (path) | Tipo de transação | "R" (Receita) ou "D" (Despesa) |

#### Exemplos

```
GET /api/transaction/tipo/D
GET /api/transaction/tipo/R
```

#### Response (200 OK)

```json
[
  {
    "idTransacao": 1,
    "idUsuario": 1,
    "idCategoria": 1,
    "nomeCategoria": "transporte",
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

**GET** `/api/transaction/mes?mes=2&ano=2025`

Retorna transações de um mês específico.

#### Query Parameters

| Parâmetro | Tipo | Descrição | Obrigatório |
|-----------|------|-----------|-------------|
| mes | Integer | Mês (1-12) | Sim |
| ano | Integer | Ano (ex: 2025) | Sim |

#### Exemplos

```
GET /api/transaction/mes?mes=2&ano=2025
GET /api/transaction/mes?mes=12&ano=2024
```

#### Response (200 OK)

```json
[
  {
    "idTransacao": 1,
    "idUsuario": 1,
    "idCategoria": 1,
    "nomeCategoria": "transporte",
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

**GET** `/api/transaction/periodo?inicio=2025-01-01&fim=2025-02-28`

Retorna transações em um intervalo de datas.

#### Query Parameters

| Parâmetro | Tipo | Descrição | Formato | Obrigatório |
|-----------|------|-----------|---------|-------------|
| inicio | Date | Data inicial | YYYY-MM-DD | Sim |
| fim | Date | Data final | YYYY-MM-DD | Sim |

#### Exemplos

```
GET /api/transaction/periodo?inicio=2025-01-01&fim=2025-02-28
GET /api/transaction/periodo?inicio=2025-02-01&fim=2025-02-15
```

#### Response (200 OK)

```json
[
  {
    "idTransacao": 1,
    "idUsuario": 1,
    "idCategoria": 1,
    "nomeCategoria": "transporte",
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

**GET** `/api/transaction/categoria/{idCategoria}`

Retorna transações filtradas por uma categoria específica.

#### Parâmetros

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| idCategoria | Integer (path) | ID da categoria |

#### Exemplos

```
GET /api/transaction/categoria/1
GET /api/transaction/categoria/5
```

#### Response (200 OK)

```json
[
  {
    "idTransacao": 1,
    "idUsuario": 1,
    "idCategoria": 1,
    "nomeCategoria": "transporte",
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

**GET** `/api/transaction/dashboard/resumo?mes=2&ano=2025`

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
      "categoria": "transporte",
      "total": 150.00
    },
    {
      "categoria": "alimentação",
      "total": 450.50
    },
    {
      "categoria": "lazer",
      "total": 600.00
    }
  ],
  "receitasPorCategoria": [
    {
      "categoria": "salário",
      "total": 5000.00
    }
  ]
}
```

---

### Evolução Anual (Dashboard)

**GET** `/api/transaction/dashboard/evolucao?ano=2025`

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
  "userId": 1,
  "nome": "João Silva"
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| token | String | Token JWT para autenticação |
| tipo | String | Sempre "Bearer" |
| userId | Long | ID do usuário |
| nome | String | Nome do usuário |

---

### UserDTO

Utilizado no registro de novos usuários.

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
| email | String | obrigatório, email válido, único | E-mail |
| senha | String | obrigatória | Senha (será hasheada) |

---

### UserResponseDTO

Retornado nas requisições de usuário.

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

### TransacaoRequestDTO

Utilizado na criação de transações.

```json
{
  "tipo": "D",
  "valor": 150.00,
  "idCategoria": 1,
  "descricao": "Uber para o trabalho",
  "dataTransacao": "2025-02-15"
}
```

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| tipo | TipoTransacao | obrigatório | "R" (Receita) ou "D" (Despesa) |
| valor | BigDecimal | obrigatório, > 0 | Valor da transação |
| idCategoria | Integer | obrigatório | ID da categoria |
| descricao | String | opcional, até 255 caracteres | Descrição da transação |
| dataTransacao | LocalDate | - | Data da transação (formato YYYY-MM-DD) |

**Nota:** O campo `dataTransacao` pode não estar no DTO atual. Verifique se precisa ser adicionado.

---

### TransacaoResponseDTO

Retornado nas operações com transações.

```json
{
  "idTransacao": 5,
  "idUsuario": 1,
  "idCategoria": 1,
  "nomeCategoria": "transporte",
  "valor": 150.00,
  "dataHoraTransacao": "2025-02-15T10:30:00",
  "descricao": "Uber para o trabalho",
  "tipo": "D",
  "tipoDescricao": "Despesa"
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| idTransacao | Long | ID único da transação |
| idUsuario | Long | ID do usuário proprietário |
| idCategoria | Integer | ID da categoria |
| nomeCategoria | String | Nome da categoria |
| valor | BigDecimal | Valor da transação |
| dataHoraTransacao | LocalDateTime | Data/hora da criação |
| descricao | String | Descrição |
| tipo | String | "R" ou "D" |
| tipoDescricao | String | "Receita" ou "Despesa" |

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
    { "categoria": "transporte", "total": 150.00 }
  ],
  "receitasPorCategoria": [
    { "categoria": "salário", "total": 5000.00 }
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
  "categoria": "transporte",
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
-- Exemplo: alimentação, transporte, saúde, lazer, salário, etc.
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
| 400 | Bad Request | Dados inválidos ou incompletos |
| 401 | Unauthorized | Token ausente ou inválido |
| 403 | Forbidden | Sem permissão para acessar |
| 404 | Not Found | Recurso não encontrado |
| 500 | Internal Server Error | Erro no servidor |

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
    const response = await api.post('/users/register', {
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
    const { token, userId, nome } = response.data;
    
    // Salvar token no localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('userId', userId);
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
async function criarTransacao(tipo, valor, idCategoria, descricao) {
  try {
    const response = await api.post('/transaction', {
      tipo,
      valor,
      idCategoria,
      descricao
    });
    console.log('Transação criada:', response.data);
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
    const response = await api.get('/transaction');
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
    const response = await api.get('/transaction/mes', {
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
    const response = await api.get('/transaction/dashboard/resumo', {
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
    const response = await api.get('/transaction/dashboard/evolucao', {
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
async function deletarTransacao(idTransacao) {
  try {
    await api.delete(`/transaction/${idTransacao}`);
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
    throw new Error(error.message || 'Erro na requisição');
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
  localStorage.setItem('userId', data.userId);
  localStorage.setItem('userName', data.nome);
  
  console.log('Login bem-sucedido!');
}
```

**Listar Transações**

```javascript
async function listarTransacoes() {
  return fetchAPI('/transaction', { method: 'GET' });
}
```

---

### 3. Com cURL (Testes)

**Registro**

```bash
curl -X POST http://localhost:8080/api/users/register \
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
curl -X POST http://localhost:8080/api/transaction \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu_token_aqui" \
  -d '{
    "tipo": "D",
    "valor": 150.00,
    "idCategoria": 1,
    "descricao": "Uber para o trabalho"
  }'
```

**Listar Transações** (com token)

```bash
curl -X GET http://localhost:8080/api/transaction \
  -H "Authorization: Bearer seu_token_aqui"
```

**Resumo Mensal** (com token)

```bash
curl -X GET "http://localhost:8080/api/transaction/dashboard/resumo?mes=2&ano=2025" \
  -H "Authorization: Bearer seu_token_aqui"
```

---

## 🌍 Variáveis de Ambiente

Para facilitar testes em diferentes ambientes, você pode usar variáveis:

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

As categorias são inseridas via `data.sql` ao iniciar a aplicação. Exemplos:

| ID | Nome |
|:--:|------|
| 1 | transporte |
| 2 | alimentação |
| 3 | saúde |
| 4 | lazer |
| 5 | salário |
| ... | ... |

Para adicionar novas categorias, insira um novo registro diretamente no banco:

```sql
INSERT INTO categorias (nome) VALUES ('nova_categoria');
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

- **Senhas**: Sempre hasheadas com Spring Security Crypto
- **JWT**: Token com expiração de 24 horas
- **Rotas Protegidas**: Todas as rotas exceto login e registro requerem autenticação
- **CORS**: Configure conforme necessário no `SecurityConfig`
- **HTTPS**: Use HTTPS em produção

---

## 📞 Suporte e Dúvidas

Para dúvidas sobre integração ou problemas técnicos, verifique:

1. Se o servidor está rodando em `http://localhost:8080`
2. Se o banco de dados PostgreSQL está configurado corretamente
3. Se o token JWT está sendo enviado corretamente no header `Authorization`
4. Se as credenciais do usuário estão corretas

---

## 📄 Licença

Este projeto é proprietário e desenvolvido para fins educacionais/internos.

---

**Última atualização**: Fevereiro de 2025
**Versão da API**: 1.0.0

