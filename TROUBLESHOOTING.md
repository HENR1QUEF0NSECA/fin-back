# 🆘 Troubleshooting & FAQ

## Problemas Comuns

### 1. Docker / Docker Compose

#### ❌ "docker-compose: command not found"
```
Docker Desktop não está instalado ou não está no PATH
```
**Solução:**
- Instale Docker Desktop: https://www.docker.com/products/docker-desktop
- Reinicie o terminal após instalar
- Verifique: `docker --version` e `docker-compose --version`

---

#### ❌ "Port 5432 already in use"
```
Outro serviço PostgreSQL já está rodando na porta 5432
```
**Soluções:**

A) Usar outra porta - defina no `.env` (o `docker-compose.yml` lê esse valor):
```env
POSTGRES_PORT=5433
```
A API dentro do Docker continua usando a porta interna 5432. Se você roda a API fora do Docker, ajuste também o `DB_URL` (`jdbc:postgresql://localhost:5433/finance`).

B) Parar o serviço existente:
```powershell
# Se foi outro Docker:
docker-compose down

# Se foi PostgreSQL instalado localmente:
# Windows: Services > PostgreSQL > Stop
```

---

#### ❌ "Port 8080 already in use"
```
Outra aplicação Java está usando a porta 8080
```
**Solução:**
- Edite `docker-compose.yml`:
```yaml
services:
  app:
    ports:
      - "8081:8080"  # Mude para 8081
```

Então acesse: `http://localhost:8081/api`

---

#### ❌ "required variable DB_PASSWORD is missing a value"
```
error while interpolating ...: required variable DB_PASSWORD is missing a value: defina DB_PASSWORD no arquivo .env
```
**Causa:** o `.env` não existe na raiz do projeto, ou está sem `DB_PASSWORD`/`JWT_SECRET` (os dois são obrigatórios no `docker-compose.yml`).

**Solução:**
```powershell
Copy-Item .env.example .env
# Edite o .env e preencha DB_PASSWORD e JWT_SECRET
```
O `.\docker-start.ps1 up` faz essa cópia sozinho quando o `.env` não existe.

---

#### ❌ "password authentication failed for user "postgres""
```
A API não consegue logar no banco
```
**Causa:** o Postgres só define a senha na **primeira** criação do volume. Se o volume `postgres_data` foi criado com outra senha (por exemplo, a de uma versão antiga do projeto), mudar o `DB_PASSWORD` no `.env` não altera a senha do banco.

**Soluções:**
- Use no `DB_PASSWORD` a mesma senha com que o volume foi criado; **ou**
- Recrie o volume (⚠️ **apaga todos os dados do banco**):
```powershell
docker-compose down -v
docker-compose up --build
```

---

#### ❌ API não sobe: "Could not resolve placeholder 'JWT_SECRET'" ou "WeakKeyException"
```
Faltou variável de ambiente, ou o segredo do JWT é curto demais
```
**Soluções:**
- `Could not resolve placeholder 'JWT_SECRET'` (ou `'DB_PASSWORD'`): a variável não está definida. Ao rodar com `.\gradlew.bat bootRun`, confira se o `.env` está na **raiz do projeto** e se você está rodando o comando a partir dela.
- `WeakKeyException` / `is not secure enough`: o `JWT_SECRET` tem menos de 32 caracteres. Gere um novo com `openssl rand -base64 48` (no Git Bash) e atualize o `.env`.

---

#### ❌ "Postgres connection refused"
```
Banco de dados não está respondendo
```
**Debug:**
```powershell
# Ver status dos containers
docker-compose ps

# Ver logs do PostgreSQL
docker-compose logs postgres

# Reiniciar
docker-compose restart postgres
```

---

#### ❌ "Build failed" ou "Gradle build error"
```
Erro durante a compilação do JAR
```
**Soluções:**
```powershell
# Limpar cache e tentar novamente
docker-compose down -v
docker-compose up --build

# Ou build manual primeiro
.\gradlew.bat clean build -x test
docker-compose up
```

---

### 2. Banco de Dados

#### ❌ "Erro: Database 'finance' does not exist"
```
O banco não foi criado ou foi deletado
```
**Solução:**
```powershell
# Remover volumes e recriar
docker-compose down -v
docker-compose up
```

O banco será criado automaticamente na primeira execução.

---

#### ❌ Conectar ao PostgreSQL direto (DBeaver, pgAdmin)

**Detalhes de conexão** (valores do seu `.env`):
```
Host: localhost
Port: POSTGRES_PORT (padrão 5432)
Database: finance
User: DB_USERNAME (padrão postgres)
Password: DB_PASSWORD
```

**Teste a conexão:**
```powershell
# Instale psql (cliente PostgreSQL)
# Windows: https://www.postgresql.org/download/windows/

# Conecte:
psql -h localhost -U postgres -d finance

# Ou via Docker:
docker exec -it finance-app-db psql -U postgres -d finance
```

---

### 3. API / Endpoints

#### ❌ "Connection refused" ao chamar API
```
API não está rodando
```
**Verificação:**
```powershell
# Checar se container está rodando
docker-compose ps

# Testar se API responde (sem token, deve voltar 401 em JSON: a API está no ar)
curl http://localhost:8080/api/categorias
```

**Se não responder:** `docker-compose logs app`

---

#### ❌ "401 Unauthorized"
```
Token ausente, inválido ou expirado
```
**Soluções:**

1. **Token não enviado:**
   ```
   Authorization: Bearer seu_token_aqui
   ```

2. **Token expirado (24h):**
   - Faça login novamente para obter novo token

3. **Token inválido:**
   - Verifique se (copiou corretamente - sem espaços extras)
   - Tokens gerados com outro `JWT_SECRET` deixam de valer: se o segredo do `.env` mudou, faça login de novo

Só `POST /api/auth/login` e `POST /api/auth/registro` funcionam sem token.

---

#### ❌ "CORS error" no Angular
```
Angular não consegue chamar a API
```
**Mensagem típica:**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/...'
from origin 'http://localhost:4200' has been blocked by CORS policy
```

**Soluções:**

A) **Verificar se CORS está ativado:**
- Arquivo: `src/main/java/com/ufu/finance/config/CorsConfig.java`
- Deve ter `@Configuration` e métodos de CORS

B) **Adicionar mais origens** (se necessário):
```java
config.setAllowedOrigins(List.of(
        "http://localhost:4200",
        "http://localhost:3000",
        "http://127.0.0.1:4200",
        "http://127.0.0.1:3000",
        "http://seu-dominio.com"  // Adicione aqui
));
```

C) **Rebuildar a API:**
```powershell
docker-compose down
docker-compose up --build
```

D) **Verificar headers de resposta:**
```powershell
curl -i http://localhost:8080/api/categorias -H "Origin: http://localhost:4200"
# Procure por: Access-Control-Allow-...
```

---

#### ❌ "400 Bad Request" com validação

Exemplo de erro:
```json
{
  "status": 400,
  "erro": "Dados inválidos",
  "campos": {
    "email": "Email deve ser válido",
    "nome": "Nome deve ter entre 3 e 100 caracteres"
  }
}
```

**Soluções:**
- Verifique os campos obrigatórios
- Valide os tipos de dados
- Consulte a documentação de DTOs em [README.md](./README.md#-data-transfer-objects-dtos)

---

### 4. Frontend Angular

#### ❌ "CORS Headers não aparecem"

Teste manualmente:
```powershell
curl -X OPTIONS http://localhost:8080/api/transacoes \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: GET" \
  -v
```

Deve retornar headers como:
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
```

---

#### ❌ "Token não persiste" no Angular

Verifique localStorage:
```typescript
// No console do navegador (F12)
localStorage.getItem('token')
```

Se estiver nulo, significa:
- Token não foi salvo após login
- Verifique seu serviço Angular: `localStorage.setItem('token', response.data.token)`

---

### 5. Performance / Lentidão

#### ⚠️ API lenta para responder

**Diagnóstico:**
```powershell
# Ver logs em tempo real
docker-compose logs -f app

# Ver recursos dos containers
docker stats
```

**Possíveis causas:**
- BD ainda inicializando na primeira execução
- Falta de memória/CPU
- Query de banco pesada

**Soluções:**
```yaml
# Aumentar limites de memória no docker-compose.yml.
# O ENTRYPOINT do Dockerfile não lê JAVA_OPTS; use JAVA_TOOL_OPTIONS, que a JVM lê sozinha:
services:
  app:
    environment:
      JAVA_TOOL_OPTIONS: "-Xmx1g -Xms512m"
```

Para ver no log o SQL que o Hibernate gera (desligado por padrão), rode a API fora do Docker com o perfil `dev`:
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

---

## ❓ FAQ

### P: Como adicionar uma nova categoria?

**R:** Via SQL direto no banco:
```sql
INSERT INTO categorias (id, nome) VALUES (8, 'Investimentos') ON CONFLICT (id) DO NOTHING;
```

Ou edite `src/main/resources/data.sql` (inclua `(8, 'Investimentos')` seguindo o padrão das outras) e reinicie. Ela passa a aparecer em `GET /api/categorias`.

---

### P: Como mudar a senha de um usuário?

**R:** Atualmente não há endpoint. Opções:

1. **SQL direto:**
   ```sql
   UPDATE usuarios SET senha = 'nova_hash' WHERE id = 1;
   ```

2. **Adicionar endpoint:**
   - Criar, por exemplo, `PUT /api/usuarios/me/senha` (sempre para o dono do token, nunca por ID)
   - Solicitar senha antiga, validar, atualizar com BCrypt

---

### P: Como fazer backup do banco?

**R:**
```powershell
# Backup
docker exec finance-app-db pg_dump -U postgres finance > backup.sql

# Restaurar
docker exec -i finance-app-db psql -U postgres finance < backup.sql
```

---

### P: Posso usar outro BD que não PostgreSQL?

**R:** Sim, mas é necessário:
1. Adicionar driver no `build.gradle`
2. Definir `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no `.env` (o `application.yaml` não precisa ser editado; o Hibernate detecta o dialeto pela conexão)
3. Revisar o `data.sql`, que usa sintaxe do PostgreSQL (`ON CONFLICT`, `setval`, `pg_get_serial_sequence`)
4. Testar as consultas

**H2 (em memória) já é usado nos testes automatizados:** veja `src/test/resources/application-test.yaml` (perfil `test`, com `MODE=PostgreSQL` e `spring.sql.init.mode: never`, porque o `data.sql` é específico do Postgres). O H2 está no `build.gradle` só como dependência de teste (`testRuntimeOnly`).

---

### P: O token JWT expira muito rápido/lento. Como mudar?

**R:** Edite `JWT_EXPIRATION` no `.env` (vale tanto para o Docker quanto para o `bootRun`):
```env
# 1 hora (em ms)
JWT_EXPIRATION=3600000
# 86400000 = 24 horas (padrão)
# 604800000 = 7 dias
```

Depois reinicie a API (no Docker: `docker-compose up -d`).

---

### P: Como colocar a API em produção?

**R:** Arquitetura recomendada:
1. **Dockerfile** já pronto em `./Dockerfile`
2. Deploy para:
   - **Railway.app** (mais simples para acadêmico)
   - **Heroku** (pode custar)
   - **AWS ECS** / **Google Cloud Run** / **Azure Container Instances**
   - **VPS com Docker**

**Para Railway (5 min):**
- Push no GitHub
- Conecte repo em railway.app
- Railway detecta o projeto + cria deploy
- Defina as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `JWT_EXPIRATION` (veja [RAILWAY_DEPLOY.md](./RAILWAY_DEPLOY.md))
- Pronto! URL pública na sequência

---

### P: Posso usar sem Docker?

**R:** Sim:
1. Instale PostgreSQL 12+
2. Crie banco: `CREATE DATABASE finance;`
3. Copie o modelo (`Copy-Item .env.example .env`) e preencha no `.env` as credenciais locais (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) e o `JWT_SECRET`
4. Execute, a partir da raiz do projeto: `.\gradlew.bat bootRun` (ou com o perfil `dev` para ver o SQL no log: `.\gradlew.bat bootRun --args='--spring.profiles.active=dev'`)

---

### P: Preciso de documentação Swagger?

**R:** Sim! Pode adiciona:

1. Adicione ao `build.gradle`:
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.0'
```

2. Acesse: `http://localhost:8080/swagger-ui.html`

---

### P: Como rodar e adicionar testes?

**R:** O projeto já tem 37 testes automatizados em `src/test/java`. Para rodar:
```powershell
.\gradlew.bat test
```
Eles **não precisam de PostgreSQL, Docker nem `.env`**: usam o perfil `test`, com H2 em memória (`src/test/resources/application-test.yaml`). Sem JDK instalado, rode com Docker:
```powershell
docker run --rm -v "${PWD}:/app" -w /app gradle:8-jdk21-alpine gradle test --no-daemon
```

Para escrever um teste novo, use os existentes como modelo:
- **Ponta a ponta (HTTP + banco H2):** `com/ufu/finance/ApiIntegracaoTest.java` (`@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`)
- **Unitário de service com Mockito:** `com/ufu/finance/service/UsuarioServiceTest.java`, `OrcamentoServiceTest.java`, `TransacaoServiceTest.java` e `service/auth/AuthServiceTest.java`
- **JWT:** `com/ufu/finance/service/auth/JwtServiceTest.java`
- **Validação dos DTOs:** `com/ufu/finance/dto/ValidacaoDtoTest.java`

Detalhes no [README](./README.md#-testes-automatizados).

---

## 📞 Ainda com dúvidas?

1. Consulte [DOCKER_SETUP.md](./DOCKER_SETUP.md)
2. Consulte [QUICK_START.md](./QUICK_START.md)
3. Consulte [README.md](./README.md)
4. Verifique logs: `docker-compose logs -f`

**Bom debugging! 🔍**

