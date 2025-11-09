\# Task Management System



Full-stack aplikace pro správu úkolů.

&nbsp;

\## 🚀 Technologie



\### Backend

\- \*\*Java 21\*\*

\- \*\*Spring Boot 3.3.5\*\*

&nbsp; - Spring Web (REST API)

&nbsp; - Spring Data JPA

&nbsp; - Spring Security

&nbsp; - Spring Validation

\- \*\*H2 Database\*\* (file-based)

\- \*\*Lombok\*\*

\- \*\*Swagger/OpenAPI\*\* (dokumentace API)



\### DevOps

\- \*\*Docker \& Docker Compose\*\*

\- \*\*Gradle\*\* (build tool)



\## 📋 Funkce



\- ✅ CRUD operace pro uživatele, projekty a úkoly

\- ✅ Přiřazování úkolů uživatelům

\- ✅ Organizace úkolů do projektů

\- ✅ Priority a statusy úkolů

\- ✅ Validace vstupů

\- ✅ Exception handling s globálním handlerem

\- ✅ Interaktivní API dokumentace (Swagger UI)



\## 🐳 Spuštění s Dockerem (doporučeno)



\### Prerekvizity

\- Docker Desktop nainstalovaný a spuštěný



\### Postup



1\. \*\*Naklonuj repozitář\*\*



git clone https://github.com/pk46/tasker

cd task-management-system



2\. \*\*Spusť aplikaci\*\*

docker-compose up --build



3\. \*\*Otevři v prohlížeči\*\*

\- Swagger UI: http://localhost:8080/swagger-ui.html

\- Health check: http://localhost:8080/api/health



4\. \*\*Zastav aplikaci\*\*



docker-compose down



\## 💻 Spuštění bez Dockeru (lokálně)



\### Prerekvizity

\- Java 17+

\- Gradle 8+



\### Postup



1\. \*\*Přejdi do složky backend\*\*



cd backend



2\. \*\*Spusť aplikaci\*\*



./gradlew bootRun



(Na Windows: `gradlew.bat bootRun`)



3\. \*\*Otevři v prohlížeči\*\*

\- Swagger UI: http://localhost:8080/swagger-ui.html



\## 📚 API Dokumentace



Po spuštění aplikace je dostupná interaktivní dokumentace:



\*\*Swagger UI:\*\* http://localhost:8080/swagger-ui.html



\### Hlavní endpointy



\#### Users

\- `GET /api/users` - Získat všechny uživatele

\- `GET /api/users/{id}` - Získat uživatele podle ID

\- `POST /api/users` - Vytvořit nového uživatele

\- `PUT /api/users/{id}` - Upravit uživatele

\- `DELETE /api/users/{id}` - Smazat uživatele



\#### Projects

\- `GET /api/projects` - Získat všechny projekty

\- `GET /api/projects/{id}` - Získat projekt podle ID

\- `GET /api/projects/owner/{ownerId}` - Získat projekty podle vlastníka

\- `POST /api/projects` - Vytvořit nový projekt

\- `PUT /api/projects/{id}` - Upravit projekt

\- `DELETE /api/projects/{id}` - Smazat projekt



\#### Tasks

\- `GET /api/tasks` - Získat všechny úkoly

\- `GET /api/tasks/{id}` - Získat úkol podle ID

\- `GET /api/tasks/project/{projectId}` - Získat úkoly projektu

\- `GET /api/tasks/assignee/{assigneeId}` - Získat úkoly přiřazené uživateli

\- `GET /api/tasks/status/{status}` - Získat úkoly podle statusu

\- `POST /api/tasks` - Vytvořit nový úkol

\- `PUT /api/tasks/{id}` - Upravit úkol

\- `DELETE /api/tasks/{id}` - Smazat úkol



\## 🗄️ Databáze



Aplikace používá H2 databázi uloženou do souboru:

\- \*\*Lokální spuštění:\*\* `./data/taskdb`

\- \*\*Docker:\*\* `/data/taskdb` (persistentní Docker volume)



Data zůstávají zachována i po restartu aplikace.



\## 👤 Autor



\*\*Pavel Kupčík\*\*



\- GitHub: https://github.com/pk46

\- Email: kupcik46@gmail.com



