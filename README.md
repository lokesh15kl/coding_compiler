# CodeForge Compiler Platform

A production-oriented online coding compiler built with React, Spring Boot, MySQL, and Docker.

## Project Structure

- `frontend/` - React + Vite + Tailwind + Monaco Editor
- `backend/` - Spring Boot 3, Java 21, Spring Security, JWT, JPA
- `docker/` - Docker compose and runtime images
- `sql/` - Database schema

## Features

- JWT authentication with BCrypt password hashing
- Monaco-powered multi-language code editor
- Docker-isolated execution for Java, Python, C, C++, and JavaScript
- Save, share, download, and inspect program history
- Dashboard and admin analytics
- Responsive glassmorphism UI with dark mode styling

## Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

## Backend Setup

```bash
cd backend
mvn spring-boot:run
```

## Docker and MySQL

```bash
cd docker
docker compose up -d
```

## API Highlights

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/compiler/run`
- `POST /api/program/save`
- `GET /api/program/my`
- `GET /api/history`
- `DELETE /api/program/{id}`

## Security Notes

- All execution runs in temporary Docker containers.
- Memory, CPU, network, and runtime are constrained.
- JWT protects all authenticated endpoints.
- The schema uses explicit foreign keys and unique share IDs.

## Notes

- Update the JWT secret and MySQL credentials before production use.
- The frontend expects the backend at `http://localhost:8080` by default.
- Swagger UI is available from the backend once it is running.
