# AGENTS.md

- Hay dos proyectos independientes: backend Jakarta EE 11 / Java 21 (`pom.xml`, `src/`) y PWA ES modules sin framework (`frontend/`). `src/main/webapp/index.html` es solo un placeholder; la app real arranca en `frontend/index.html` → `frontend/js/app.js`.
- La documentación canónica está en `frontend/README.md` (cubre también el backend); no hay README en la raíz.
- Backend: `mvn -o test` (offline) y `mvn -o test -Dtest=InicioBeanTest` para una clase. Las pruebas de errores imprimen stack traces y logs `GRAVE`/`ADVERTENCIA` esperados: juzga el resultado por el resumen de Surefire, no por la salida. Cada test está en el mismo paquete que la clase probada.
- Frontend: `cd frontend && npm install && npm test`; un archivo con `npx mocha test/control/citas.test.js`, cobertura con `npm run coverage`. No hay lockfile: `package-lock.json` está ignorado.
- `npm test` solo corre `*.test.js`. `npm run test:integracion` corre aparte los `*.it.js`, sin mocks y solo lectura, contra la URL fija `http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api` definida en `frontend/js/control/api.js`.
- Los tests espejean la estructura de `js/`; `frontend/test/ayudas.js` da `datosBase()` y `stubApi()`. Los tests de Web Components usan `usarDom()` de `test/boundary/entorno-dom.js`: hay una sola ventana jsdom compartida porque los custom elements no pueden redefinirse.
- El plugin Liberty lee `installDirectory` desde una propiedad cuyo default sigue siendo `/home/mrxto/openliberty/wlp`; en esta máquina usa `mvn -o liberty:* -DinstallDirectory=/home/duran/wlp` sin cambiar el default, para no afectar la presentación del otro equipo. `serverName=miServidor`; el README usa ese mismo nombre.
- La BD es PostgreSQL 17 con esquema previo y datasource `jdbc/GalenoSV` (`persistence.xml`); no hay migraciones ni DDL en el repo y las instrucciones del proyecto prohíben crear tablas o columnas.
