# 🚀 Quick Start - Finance App Backend

## Tl;dr - 5 minutos

### 1. Pré-requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop) instalado

### 2. Clonar e navegar
```powershell
cd caminho\para\meu-norte-back
```

### 3. Criar o `.env`
Senha do banco e segredo do JWT ficam no arquivo `.env` (fora do Git). Crie a partir do modelo:
```powershell
Copy-Item .env.example .env
```
Abra o `.env` e troque pelo menos:
- `DB_PASSWORD`: a senha que o Postgres vai usar
- `JWT_SECRET`: um segredo com **no mínimo 32 caracteres** (gere com `openssl rand -base64 48` no Git Bash)

> O `.\docker-start.ps1 up` cria o `.env` sozinho se ele não existir e pede para você revisar os valores.

### 4. Iniciar tudo com Docker
```powershell
docker-compose up --build
```

**Esperado:** Verá algo como:
```
app_1      | ... Started FinanceApplication in 8.234 seconds
postgres_1 | database system is ready to accept connections
```

### 5. API está rodando em:
```
http://localhost:8080/api
```

### 6. Testar rápido (em outro terminal)
```powershell
# Registrar usuário
curl -X POST http://localhost:8080/api/auth/registro `
  -H "Content-Type: application/json" `
  -d '{
    "nome":"Teste",
    "email":"teste@example.com",
    "senha":"senha123"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email":"teste@example.com",
    "senha":"senha123"
  }'
```

O login devolve `{ "token": "...", "tipo": "Bearer", "usuarioId": 1, "nome": "Teste" }`. Use o token no header `Authorization: Bearer <token>` nas outras rotas, por exemplo `GET /api/usuarios/me`, `GET /api/categorias` e `GET /api/transacoes`.

---

## 📚 Próximas etapas

- [ ] Ler [DOCKER_SETUP.md](./DOCKER_SETUP.md) para mais detalhes
- [ ] Testar endpoints em [requests.http](./requests.http) (com VSCode REST Client)
- [ ] Rodar os testes automatizados: `.\gradlew.bat test` (não precisa de Postgres nem Docker; veja o [README](./README.md#-testes-automatizados))
- [ ] Conectar seu frontend Angular em `localhost:4200` (CORS já está configurado ✅)

---

## 🛑 Parar
```powershell
# Em outro terminal, ou Ctrl+C no terminal onde rodou docker-compose
docker-compose down
```

---

## 🆘 Problemas?

| Problema | Solução |
|----------|---------|
| "required variable DB_PASSWORD is missing a value" | Falta o `.env` (ou o valor nele): veja o passo 3 |
| "Port 5432 already in use" | Defina `POSTGRES_PORT=5433` no `.env` |
| "password authentication failed for user" | O volume do Postgres foi criado com outra senha: use a mesma senha no `DB_PASSWORD` ou recrie o volume com `docker-compose down -v` (⚠️ apaga os dados) |
| API não responde | `docker-compose logs app` para ver erros (ex.: `JWT_SECRET` com menos de 32 caracteres impede a API de subir) |
| Token expirado | Faça login novamente |

Mais casos em [TROUBLESHOOTING.md](./TROUBLESHOOTING.md).

---

## 📝 Banco de Dados

**Conexão direta (se quiser usar DBeaver/pgAdmin):**
- Host: `localhost`
- Port: o valor de `POSTGRES_PORT` no `.env` (padrão `5432`)
- Database: `finance`
- User: o valor de `DB_USERNAME` no `.env` (padrão `postgres`)
- Password: o valor de `DB_PASSWORD` no seu `.env`

---

Bom desenvolvimento! 🎉

