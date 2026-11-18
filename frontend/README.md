# GalenoSV · PPI115 2026 (GT02)

Aplicación **PWA / SPA** para la gestión de citas y atenciones odontológicas de GalenoSV.
El frontend está escrito **exclusivamente en Ecmascript y Web Components**, sin frameworks,
librerías, estilos ni componentes de terceros, y consume la API REST del backend Jakarta EE
que se encuentra en el mismo repositorio (`src/main/java`).

---

## 1. Estructura del proyecto

```
frontend/
├── index.html            punto de entrada (solo contiene Web Components)
├── manifest.json         manifiesto de la PWA
├── service-worker.js     service worker (precache y modo sin conexión)
├── css/principal.css     estilos globales de la aplicación
├── js/
│   ├── app.js            registro de los Web Components
│   ├── api.js            cliente de la API REST (fetch)
│   ├── base.js           clase base de componentes y utilidades de vista
│   ├── cie10.js          catálogo local de códigos CIE-10 y buscador
│   ├── datos.js          composición de los datos de la API (consultas, pasos, examenes…)
│   ├── estado.js         sesión del empleado (sessionStorage)
│   ├── pdf.js            generador de PDF en JavaScript estándar
│   ├── pendientes.js     cola de cambios para trabajar sin conexión
│   ├── pwa.js            registro del service worker
│   └── componentes/      Web Components de la aplicación
│       ├── app-shell.js       enrutado y estructura general
│       ├── app-busqueda.js    HU1 · búsqueda de citas
│       ├── app-login.js       HU2 · acceso de empleados
│       ├── app-dashboard.js   HU3 · agenda del día
│       ├── app-consulta.js   HU4 · registro de atención
│       ├── cita-card.js       card de cita
│       ├── ticket-cita.js     ticket imprimible y descarga de PDF
│       ├── ui-toast.js        notificaciones
│       └── pwa-instalar.js    botón de instalación de la PWA
└── test/                 pruebas unitarias (Mocha + Chai + Sinon + jsdom)
```

## 2. Requisitos

- Java 21 (para el backend Jakarta EE sobre Open Liberty)
- Node.js 18 o superior (solo para servir el frontend y ejecutar las pruebas)
- Docker con PostgreSQL 17 para la base de datos

## 3. Levantar el proyecto

### 3.1 Base de datos

PostgreSQL 17 en Docker: contenedor `pg_container` con la base `basedatos_pg`.

```bash
docker start pg_container
```

La base de datos es la del proyecto original y **no se modifica**: el frontend solo
consulta la API REST existente. La conexión (`jdbc/GalenoSV`) la define el servidor
Open Liberty configurado en cada equipo.

### 3.2 Backend (API REST)

```bash
mvn clean package liberty:run
```

Use siempre `clean`: así el WAR incluye OpenPDF y el ticket PDF se descarga del backend.
La API queda disponible en `http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api`.

### 3.3 Frontend

En otra terminal:

```bash
cd frontend
npm install
npm start                  # BrowserSync en http://localhost:3000
```

> La PWA (service worker e instalación) requiere `localhost` o HTTPS.
> En Chrome o Edge aparece abajo a la derecha un botón redondo de descarga; al pulsarlo se
> despliega la opción **Instalar**.

## 4. Pruebas unitarias

```bash
cd frontend
npm test                   # todas las pruebas
npm run coverage           # cobertura (resumen en consola y detalle en coverage/index.html)
```

Cada comportamiento observable de la aplicación tiene su prueba: API, composición de datos,
CIE-10, sesión, cola sin conexión, generador de PDF y cada Web Component. Las pruebas de
componentes están agrupadas por historia de usuario (HU1 a HU4) y por el shell de la PWA.

## 5. Rutas de la aplicación

| Ruta              | Pantalla                                                  |
| ----------------- | --------------------------------------------------------- |
| `#/buscar`        | HU1 · búsqueda de citas del paciente                       |
| `#/login`         | HU2 · acceso de empleados                                  |
| `#/dashboard`     | HU3 · agenda del día por clínica y rol                     |
| `#/consulta/:id`  | HU4 · registro de atención de una consulta                 |

## 6. API REST

Endpoints nuevos que usa el frontend:

| Metodo | Ruta | Descripcion |
| ------ | ---- | ----------- |
| GET | `/tickets/consultas/{id}` | Comprobante de la cita en PDF generado por el backend con OpenPDF |

El resto de la API (20 recursos CRUD con paginacion `first/max`) no se modifico.

## 7. Acceso de empleados

El acceso de empleados se hace con **tipo y número de documento** (sin contraseña, tal como
requiere el enunciado). Entra cualquier persona registrada en la base con un rol distinto de
Paciente; quien solo es paciente consulta sus citas desde «Buscar citas». El repositorio no
incluye datos: se usan los registros de la base de datos de la clínica.

## 8. Notas de implementación

- La **base de datos no se toca**: no se crean tablas ni columnas. Todo lo que el frontend
  necesita y el backend no expone se resuelve en el cliente:
  - **Identidad**: el acceso del empleado se valida con el tipo y numero de documento
    contra los registros existentes (no hay autoregistro).
  - **CIE-10**: catalogo local en `js/cie10.js` con busqueda por codigo o descripcion.
  - **Notas por paso**: se guardan en las observaciones del procedimiento, indexadas por paso.
  - **PDF**: el comprobante lo genera el backend con OpenPDF; si no hay conexion se genera
    en el dispositivo con `js/pdf.js` para no perder la descarga.
  - **Sin conexion**: las lecturas se sirven desde la cache del service worker y las
    escrituras se encolan en el dispositivo para enviarse al recuperar la red.
