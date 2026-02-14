# Security Audit Report - Task Management System

## Nalezené zranitelnosti a demonstrace

Tento dokument popisuje nalezené bezpečnostní zranitelnosti, jak je demonstrovat (PŘED opravou),
a jaké opravy byly implementovány.

---

## CRITICAL - Kritické zranitelnosti

### 1. H2 Console - přímý přístup k databázi bez autentizace

**Stav:** OPRAVENO

**Popis:** H2 konzole byla zapnutá a přidaná do `PUBLIC_PATHS` - kdokoli mohl přistoupit
k databázi bez jakékoli autentizace.

**Demonstrace (před opravou):**
```bash
# Otevřít v prohlížeči:
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:file:./backend/data/taskdb
# User: sa, Password: (prázdné)
# Spustit: SELECT * FROM USERS
```

**Oprava:**
- `spring.h2.console.enabled=false` v `application.properties`
- Odstraněn `/h2-console/` z `PUBLIC_PATHS` v `SecurityConfig.java`
- Přidáno heslo pro H2 databázi (`H2_PASSWORD` env variable)
- Přidány security headers: `X-Frame-Options: DENY`, `X-Content-Type-Options`, `HSTS`

---

### 2. Hardcoded admin credentials (admin/admin)

**Stav:** OPRAVENO

**Popis:** Výchozí admin účet s heslem `admin`, přihlašovací údaje logované do konzole.

**Demonstrace (před opravou):**
```bash
# Přihlášení jako admin s výchozím heslem
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
# Vrátí JWT token - plný admin přístup
```

**Oprava:**
- Heslo admina je nyní konfigurovatelné přes `admin.default-password` property
- Pokud není nastaveno, vygeneruje se náhodné UUID heslo
- Credentials se již nelogují do konzole (pouze upozornění, že bylo vygenerováno)
- Kontrola, zda admin už existuje (neduplikuje se)

---

### 3. JWT secret se generuje při každém startu

**Stav:** OPRAVENO

**Popis:** `Keys.secretKeyFor(HS256)` se volal při startu - klíč nebyl persistentní.

**Demonstrace (před opravou):**
```bash
# 1. Přihlásit se, získat token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.accessToken')

# 2. Restartovat backend

# 3. Použít token - selže, protože klíč je jiný
curl -s http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
# Vrátí 401 Unauthorized
```

**Oprava:**
- JWT secret je konfigurovatelný přes `jwt.secret` property (Base64-encoded)
- Pokud není nastaven, vygeneruje se náhodný (s warningem v logu)
- Pro produkci: `JWT_SECRET=<base64-klíč>` jako environment variable

**Generování klíče:**
```bash
openssl rand -base64 32
```

---

## HIGH - Závažné zranitelnosti

### 4. Broken Authorization - horizontální privilege escalation

**Stav:** OPRAVENO

**Popis:** Chyběly autorizační kontroly na většině endpointů.

**Demonstrace (před opravou):**
```bash
# 1. Přihlásit se jako běžný USER
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"TestPass1"}' | jq -r '.accessToken')

# 2. PRIVILEGE ESCALATION - USER si změní roli na ADMIN
curl -s -X PUT http://localhost:8080/api/users/2 \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}'
# Před opravou: Úspěch - uživatel je nyní ADMIN!

# 3. ENUMERACE - USER vidí všechny uživatele
curl -s http://localhost:8080/api/users \
  -H "Authorization: Bearer $USER_TOKEN"
# Před opravou: Vrátí seznam všech uživatelů včetně jejich údajů

# 4. MODIFIKACE CIZÍCH DAT - USER upraví cizí projekt
curl -s -X PUT http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"HACKED PROJECT"}'
# Před opravou: Úspěch - cizí projekt upraven!

# 5. VYTVOŘENÍ UŽIVATELE - USER si vytvoří dalšího admina
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"hacker","email":"h@h.com","password":"TestPass1","role":"ADMIN","firstName":"H","lastName":"H"}'
# Před opravou: Úspěch - nový admin vytvořen!
```

**Oprava:**
- **UserController:**
  - `GET /api/users` - pouze ADMIN
  - `GET /api/users/{id}` - vlastní profil nebo ADMIN
  - `POST /api/users` - pouze ADMIN
  - `PUT /api/users/{id}` - vlastní profil nebo ADMIN, změna role pouze ADMIN
- **ProjectController:**
  - `POST /api/projects` - pouze pro sebe (ownerId musí být ID aktuálního uživatele) nebo ADMIN
  - `PUT /api/projects/{id}` - vlastník projektu nebo ADMIN
  - `DELETE /api/projects/{id}` - vlastník projektu nebo ADMIN
- **TaskController:**
  - `POST /api/tasks` - pouze v projektech, které vlastním, nebo ADMIN
  - `PUT /api/tasks/{id}` - assignee, vlastník projektu, nebo ADMIN
  - `DELETE /api/tasks/{id}` - vlastník projektu nebo ADMIN

---

### 5. JWT tokeny v localStorage

**Stav:** ZDOKUMENTOVÁNO (vyžaduje větší refaktoring pro httpOnly cookies)

**Popis:** Access i refresh tokeny jsou uloženy v `localStorage`.

**Demonstrace:**
```javascript
// V JS konzoli prohlížeče na stránce aplikace:
console.log('Access Token:', localStorage.getItem('access_token'));
console.log('Refresh Token:', localStorage.getItem('refresh_token'));
// Oba tokeny jsou čitelné jakýmkoli JS na stránce
```

**Doporučení pro budoucí opravu:**
- Přesunout refresh token do httpOnly cookie
- Access token ponechat v paměti (ne localStorage)
- Implementovat CSRF ochranu pro cookie-based auth

---

### 6. Chybí CSRF ochrana

**Stav:** AKCEPTOVÁNO (JWT-based stateless API)

**Popis:** CSRF je vypnutý, ale to je standardní praxe pro stateless JWT API.
CSRF ochrana je relevantní pro cookie-based session autentizaci.

---

## MEDIUM - Střední zranitelnosti

### 7. Rate limiting na login

**Stav:** OPRAVENO

**Popis:** Neomezený počet pokusů o přihlášení.

**Demonstrace (před opravou):**
```bash
# Brute-force útok - zkouší hesla bez omezení
for i in $(seq 1 100); do
  RESULT=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"admin\",\"password\":\"guess$i\"}")
  echo "Attempt $i: HTTP $RESULT"
done
# Před opravou: Všech 100 pokusů projde (HTTP 401)

# Po opravě: Po 5 neúspěšných pokusech vrací HTTP 429 (Too Many Requests)
```

**Oprava:**
- `LoginRateLimiter` - max 5 pokusů za 5 minut per IP+username
- Po překročení limitu vrací HTTP 429 Too Many Requests
- Úspěšné přihlášení resetuje počítadlo

---

### 8. Silnější validace hesel

**Stav:** OPRAVENO

**Popis:** Povolovala se hesla jako "123456".

**Demonstrace (před opravou):**
```bash
# Vytvoření uživatele se slabým heslem (jako admin)
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"weak","email":"w@w.com","password":"123456","role":"USER"}'
# Před opravou: Úspěch - slabé heslo přijato!
```

**Oprava:**
- Minimální délka hesla zvýšena na 8 znaků
- Vyžadováno alespoň 1 velké písmeno, 1 malé písmeno, 1 číslice
- Regex: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$`

---

### 10. Security headers

**Stav:** OPRAVENO

**Oprava:**
- `X-Frame-Options: DENY` (dříve vypnutý)
- `X-Content-Type-Options: nosniff`
- `Strict-Transport-Security` (HSTS) s max-age 1 rok

---

### 11. Demo credentials ve frontend kódu

**Stav:** OPRAVENO

**Popis:** Login stránka zobrazovala `admin/admin` přímo uživatelům.

**Oprava:** Odstraněn demo text z `Login.svelte`, nahrazen kontaktní informací.

---

## Shrnutí změněných souborů

| Soubor | Změna |
|--------|-------|
| `application.properties` | H2 console off, H2 password, JWT secret config |
| `SecurityConfig.java` | Odstraněn H2/rehash z PUBLIC_PATHS, security headers |
| `DataInitializer.java` | Konfigurovatelné admin heslo, bez logování credentials |
| `JwtUtil.java` | Persistentní JWT secret z konfigurace |
| `JwtAuthenticationFilter.java` | AuthenticatedUser principal s userId |
| `AuthenticatedUser.java` | NOVÝ - principal s username, userId, role |
| `SecurityUtils.java` | NOVÝ - utility pro autorizační kontroly |
| `LoginRateLimiter.java` | NOVÝ - rate limiting pro login |
| `UserController.java` | @PreAuthorize + ownership checks |
| `ProjectController.java` | Ownership checks na CRUD operace |
| `TaskController.java` | Ownership/assignee checks na CRUD operace |
| `UserCreateDTO.java` | Silnější password validace |
| `UserUpdateDTO.java` | Silnější password validace |
| `Login.svelte` | Odstraněny demo credentials |
