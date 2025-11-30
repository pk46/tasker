# Task Management System

## Tech Stack
- **Backend:** Spring Boot 3.5.7, Java 21
- **Frontend:** Svelte 5, TypeScript, Vite 7
- **Node.js:** 24.11.1
- **Database:** H2
- **DevOps:** Docker Compose

## Prerequisites

### For Docker (recommended):
- Docker 27+

### For local development:
- Node.js 24.11.1+ (use `nvm` for version management)
- Java 21
- Gradle 8

## Quick Start (Docker)
```bash
# Clone repository
git clone https://github.com/pk46/tasker.git
cd tasker

# Start all services
docker-compose up --build

# Access:
# - Frontend: http://localhost:5173
# - Backend: http://localhost:8080
# - H2 Console: http://localhost:8080/h2-console
# - Swagger UI: http://localhost:8080/swagger-ui/index.html
```

## Local Development (without Docker)

### Backend
```bash
cd backend
gradlew.bat bootRun
```

### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

## Node.js Version Management

This project uses Node.js 24.11.1. I recommend using `nvm`:
```bash
# Install specific version
nvm install 24.11.1

# Use it
nvm use 24.11.1

## Demo Credentials
- **Admin:** admin / admin

