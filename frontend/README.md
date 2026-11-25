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
├── css/                  estilos globales y una hoja por componente
├── js/                   organizado por capas, igual que el backend (BCE)
│   ├── app.js                 entrada: registra los Web Components
│   ├── configuracion-estilos.js  dónde están las hojas de cada componente
│   ├── pwa.js                 registro del service worker
│   ├── boundary/              lo que ve y usa la persona
│   │   ├── base.js                 clase Vista, plantillas y foco de los diálogos
│   │   ├── presentacion.js         textos de pantalla (estado de un paso, motivo…)
│   │   ├── presentacion-consulta.js  cómo se pinta el registro de atención (HU4)
│   │   └── componentes/            Web Components
│   │       ├── app-shell.js        enrutado y estructura general
│   │       ├── app-busqueda.js     HU1 · búsqueda de citas (con asistente-citas.js)
│   │       ├── app-login.js        HU2 · acceso de empleados
│   │       ├── app-dashboard.js    HU3 · agenda por clínica y rol
│   │       ├── app-consulta.js     HU4 · registro de atención
│   │       ├── cita-card.js · ticket-cita.js · linea-pasos.js
│   │       ├── ui-autocompletar.js · ui-confirmar.js · ui-toast.js · pwa-instalar.js
│   │       └── reutilizables.js    punto de entrada de los controles reutilizables
│   ├── control/               lógica y acceso a datos
│   │   ├── api.js                  transporte HTTP: cabeceras, errores y cola sin conexión
│   │   ├── default-service.js      clase padre de acceso a un recurso REST (findRange, findAll…)
│   │   ├── servicios.js            un DefaultService por recurso de la API
│   │   ├── repositorio.js          descarga y caché de las tablas de la API
│   │   ├── documentos.js · personas.js · citas.js   consultas sobre esas tablas
│   │   ├── tablas.js               búsqueda por identificador
│   │   ├── pendientes.js           cola de cambios para trabajar sin conexión
│   │   ├── sesion.js               sesión del empleado (sessionStorage)
│   │   └── tema.js                 tema claro u oscuro que elige la persona
│   ├── entity/                forma de los datos
│   │   ├── recursos.js             recursos de la API: ruta REST y campo id de cada uno
│   │   ├── default-response.js     respuesta paginada: datos y total
│   │   ├── default-error.js        error con que se rechazan las promesas de los servicios
│   │   ├── observaciones.js        formato JSON de las notas de cada paso
│   │   └── cie10.js                catálogo local de códigos CIE-10
│   └── comun/                 utilidades sin dependencias: texto.js · fechas.js
└── test/                 pruebas unitarias con la misma estructura que js/
    └── integracion/           pruebas de integración contra la API real (*.it.js)
```

## 2. Requisitos

- Java 21 (para el backend Jakarta EE sobre Open Liberty)
- Node.js 18 o superior (solo para servir el frontend y ejecutar las pruebas)
- Docker con PostgreSQL 17 para la base de datos

## 3. Levantar el proyecto

### 3.1 Base de datos

```bash
docker run --name db17 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=GalenoSV -p 5432:5432 -d postgres:17.5-alpine
```

La base de datos es la del proyecto original y **no se modifica**: el frontend solo
consulta la API REST existente.

### 3.2 Backend (API REST)

```bash
mvn clean package
mvn liberty:dev            # en esta máquina: -DinstallDirectory=/home/duran/wlp
                           # o bien: <LIBERTY>/bin/server start miServidor
```

El default de `installDirectory` del `pom.xml` se conserva para el equipo de presentación; se sobrescribe solo con `-D`. La API queda disponible en `http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api`.

### 3.3 Frontend

```bash
cd frontend
npm install
npm start                  # BrowserSync en http://localhost:3000
```

> La PWA (service worker e instalación) requiere `localhost` o HTTPS.
> En Chrome o Edge aparecerá el botón **Instalar aplicación**.

## 4. Pruebas unitarias

```bash
cd frontend
npm test
```

Cada comportamiento observable de la aplicación tiene su prueba. Hay un archivo de pruebas por
módulo, en la misma carpeta que en `js/` (por ejemplo `js/control/citas.js` se prueba en
`test/control/citas.test.js`), con Mocha + Chai + Sinon y jsdom para los Web Components.
Para correr un solo archivo: `npx mocha test/boundary/componentes/app-consulta.test.js`.

Las pruebas de integración (`test/integracion/*.it.js`) usan la API real sin mocks y solo
leen datos. Necesitan el backend levantado y se corren aparte:

```bash
npm run test:integracion
```

### 4.1 Pruebas del backend

```bash
mvn -o test                          # todas
mvn -o test -Dtest=InicioBeanTest    # una clase
```

Patrón de las pruebas de unidad: JUnit 5 con Mockito. Hay una clase de prueba por clase, en el
mismo paquete que la clase que prueba (`control` para los DAO, `boundary` y `boundary/jsf` para
los beans y modelos, `boundary/rest` para los recursos). Las dependencias se reemplazan con
mocks (`EntityManager` y `TypedQuery` en los DAO, los DAO y `FacesContext` en los modelos), y cada
prueba sigue tres pasos: preparar lo que devuelven los mocks, ejecutar el método y comprobar el
resultado o la interacción con `verify`.

## 5. Rutas de la aplicación

| Ruta              | Pantalla                                                  |
| ----------------- | --------------------------------------------------------- |
| `#/buscar`        | HU1 · búsqueda de citas del paciente                       |
| `#/login`         | HU2 · acceso de empleados                                  |
| `#/dashboard`     | HU3 · agenda del día por clínica y rol                     |
| `#/consulta/:id`  | HU4 · registro de atención de una consulta                 |

## 6. API REST

Endpoints nuevos que usa el frontend:

| Método | Ruta | Descripción |
| ------ | ---- | ----------- |
| GET | `/tickets/consultas/{id}` | Comprobante de la cita en PDF generado por el backend con OpenPDF |

El resto de la API (20 recursos CRUD con paginación `first/max`) no se modificó.

## 7. Datos de demostración

El acceso de empleados se hace con **tipo y número de documento** (sin contraseña, tal como
requiere el enunciado). Datos de ejemplo cargados en la base:

| Documento    | Persona        | Rol           |
| ------------ | -------------- | ------------- |
| `01234567-8` | José Torres    | Odontólogo    |
| `02345678-9` | María López    | Paciente      |
| `03456789-0` | Ana Ramírez    | Recepcionista |

## 8. Notas de implementación

- La **base de datos no se toca**: no se crean tablas ni columnas. Todo lo que el frontend
  necesita y el backend no expone se resuelve en el cliente:
  - **Identidad**: el acceso del empleado se valida con el tipo y número de documento
    contra los registros existentes (no hay autoregistro).
  - **CIE-10**: catálogo local en `js/entity/cie10.js` con búsqueda por código o descripción.
  - **Notas por paso**: se guardan en las observaciones del procedimiento, indexadas por paso.
  - **Comprobante**: el ticket se imprime desde el navegador con un marco oculto, también sin
    conexión; el backend ofrece además el PDF con OpenPDF en `/tickets/consultas/{id}`.
  - **Sin conexión**: las lecturas se sirven desde la caché del service worker y las
    escrituras se encolan en el dispositivo para enviarse al recuperar la red.
- **Acceso a datos**: cada recurso de la API es un `DefaultService` (`js/control/servicios.js`)
  con los mismos métodos que `DefaultDAO` en el backend: `findRange(first, max)`, `findAll()`,
  `crear`, `modificar` y `eliminar`. Las lecturas se resuelven con un `DefaultResponse`
  (`datos` y `total`) y todas las promesas se rechazan con un `DefaultError` (`message`, el
  `status` HTTP y el `error` original).
- **Web Components**: cada componente clona una plantilla preparada una sola vez y comparte
  sus hojas de estilo con las demás copias (`adoptedStyleSheets`); la aplicación las descarga
  antes de registrar los componentes para que se pinten con estilo desde el inicio. Los datos
  entran por propiedades (`.cita`, `.opciones`), la configuración por atributos que se pueden
  cambiar en cualquier momento, y cada pantalla agrega sus acciones a `cita-card` por un `<slot>`.
- **Tema oscuro**: lo elige la persona con el botón del encabezado y se recuerda en el dispositivo.
