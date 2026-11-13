# 🚀 Deploy em Railway (Opcional)

Se você quiser um link online para compartilhar com o professor, use **Railway** (grátis para projetos acadêmicos).

## ⚡ 5 Minutos no Railway

### 1. Pré-requisitos
- Conta no GitHub com seu repo `finance-app-back`
- Conta Railway (grátis)

### 2. Criar Deploy

1. Acesse: https://railway.app
2. Clique em **"Create New Project"**
3. Selecione **"Deploy from GitHub"**
4. Conecte sua conta GitHub
5. Selecione o repo `finance-app-back`
6. Clique em **Deploy**

**Pronto!** Railway vai:
- Detectar Spring Boot
- Fazer build automaticamente
- Rodar seu app

### 3. Configurar Banco de Dados

1. No painel do Railway, clique em **"Add Plugin"**
2. Selecione **"PostgreSQL"**
3. Railway vai criar DB automaticamente

### 4. Configurar Variáveis de Ambiente

A API lê a configuração de variáveis de ambiente (as mesmas do `.env.example`). O `.env` local **não vai para o GitHub** nem para a imagem, então elas precisam ser cadastradas no Railway.

1. Vá em **Variables** (no painel do serviço da API)
2. Adicione:
```
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<segredo-com-pelo-menos-32-caracteres>
JWT_EXPIRATION=86400000
```

**Atenção:**
- O `DB_URL` precisa começar com **`jdbc:postgresql://`**. A URL de conexão que o Railway mostra para o Postgres (`postgresql://usuario:senha@host:porta/banco`) não serve direto: monte o `DB_URL` só com host, porta e banco, e passe usuário e senha em `DB_USERNAME` e `DB_PASSWORD`. Esses dados aparecem nas variáveis do serviço PostgreSQL no painel.
- Gere um `JWT_SECRET` novo para produção (`openssl rand -base64 48`), diferente do que você usa localmente. Com menos de 32 caracteres a API não sobe.
- `DB_PASSWORD` e `JWT_SECRET` são obrigatórios; sem eles a API não sobe.

### 5. Pronto! 🎉

Sua API está online em:
```
https://seu-projeto-XXXXX.railway.app/api
```

---

## 🔗 Testar URL Online

```powershell
# Teste se está respondendo (sem token deve voltar 401 em JSON: a API está no ar)
curl https://seu-projeto-XXXXX.railway.app/api/categorias

# Cadastrar um usuário
curl -X POST https://seu-projeto-XXXXX.railway.app/api/auth/registro `
  -H "Content-Type: application/json" `
  -d '{"nome":"Teste","email":"teste@test.com","senha":"senha123"}'

# Login (devolve token, tipo, usuarioId e nome)
curl -X POST https://seu-projeto-XXXXX.railway.app/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"teste@test.com","senha":"senha123"}'
```

---

## 📝 Compartilhar URL com Professor

Copie o link e compartilhe:
```
https://seu-projeto-XXXXX.railway.app/api
```

Seu Angular pode chamar de qualquer lugar agora! 🌍

---

## 💡 Notas

- Railway oferece **free tier generoso** para estudiantes
- Se acabar crédito gratuito, você pode adicionar cartão de crédito
- Alternativamente, tente **Render** ou **Heroku**
- Lembre-se: dados no banco de dados persistem entre deploys

---

## 🆚 Docker Local vs Railway Online

| Aspecto | Docker Local | Railway |
|--------|-------------|---------|
| Setup | 5 min | 3 min |
| URL | localhost:8080 | https://... |
| Persistência | Local | Nuvem |
| Acesso | Apenas local | Qualquer lugar |
| Custo | 0 | 0 (free tier) |
| Ideal para | Desenvolvimento | Apresentação |

---

## 🚨 Se der problema no Railway

Verifique:
1. GitHub está conectado à Railway
2. PostgreSQL foi adicionado como plugin
3. Variáveis de ambiente estão configuradas (`DB_URL` com prefixo `jdbc:postgresql://`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` com 32+ caracteres, `JWT_EXPIRATION`)
4. Build logs: clique em **View Build Logs**
5. Deploy logs: `Could not resolve placeholder` indica variável faltando; `WeakKeyException` indica `JWT_SECRET` curto demais

Se tiver erro, consulte [TROUBLESHOOTING.md](./TROUBLESHOOTING.md).

