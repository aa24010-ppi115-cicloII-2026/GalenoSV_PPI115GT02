# GalenoSV · PPI115 2026 (GT02)

Sistema de gestion clinica odontologica: backend Jakarta EE (Open Liberty) con API REST
y frontend PWA/SPA en Ecmascript y Web Components.

- **Frontend**: carpeta `frontend/` (detalles en `frontend/README.md`).
- **Backend**: `src/main/java` (Jakarta EE + Hibernate + API REST en `/api`).

El backend mantiene el esquema de base de datos original. Para el parcial solo se
agrego la generacion de los comprobantes en PDF con **OpenPDF**.

## Puesta en marcha rapida

```bash
# 1. Base de datos (con el esquema ya establecido)
docker start db17

# 2. Backend (API REST en http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api)
mvn clean package
mvn liberty:dev

# 3. Frontend (http://localhost:3000)
cd frontend && npm install && npm start
```

## Pruebas

```bash
mvn test                  # backend
cd frontend && npm test   # frontend (npm run coverage para el reporte)
```
