# 🚀 Quick Start - Finance App Backend

## Tl;dr - 5 minutos

### 1. Pré-requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop) instalado

### 2. Clonar e navegar
```powershell
cd C:\Users\Lucas\Documents\finance-app-back
```

### 3. Iniciar tudo com Docker
```powershell
docker-compose up --build
```

**Esperado:** Verá algo como:
```
app_1      | ... Started FinanceApplication in 8.234 seconds
postgres_1 | database system is ready to accept connections
```

### 4. API está rodando em:
```
http://localhost:8080/api
```

### 5. Testar rápido (em outro terminal)
```powershell
# Registrar usuário
curl -X POST http://localhost:8080/api/users/register `
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

---

## 📚 Próximas etapas

- [ ] Ler [DOCKER_SETUP.md](./DOCKER_SETUP.md) para mais detalhes
- [ ] Testar endpoints em [requests.http](./requests.http) (com VSCode REST Client)
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
| "Port 5432 already in use" | `docker-compose down -v` para limpar |
| API não responde | `docker-compose logs app` para ver erros |
| Token expirado | Faça login novamente |

---

## 📝 Banco de Dados

**Conexão direta (se quiser usar DBeaver/pgAdmin):**
- Host: `localhost`
- Port: `5432`
- Database: `finance`
- User: `postgres`
- Password: `postgres123`

---

Bom desenvolvimento! 🎉

