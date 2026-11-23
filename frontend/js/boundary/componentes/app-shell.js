import { Vista, definirElemento, fragmento } from "../base.js";
import { html, cantidad } from "../../comun/texto.js";
import { leerSesion, cerrarSesion, alCambiarSesion } from "../../control/sesion.js";
import { estaConectado, sincronizar, listarPendientes } from "../../control/pendientes.js";
import { comprobarServicio, ejecutarPendiente, servicioNoResponde } from "../../control/api.js";
import { limpiarCache } from "../../control/repositorio.js";
import { temaActual, aplicarTema, alternarTema, alCambiarTema } from "../../control/tema.js";

/** Un solo catálogo define el elemento y el título que corresponden a cada ruta. */
const RUTAS = {
  buscar: { elemento: "app-busqueda", titulo: "Buscar mis citas" },
  login: { elemento: "app-login", titulo: "Acceso de empleados" },
  dashboard: { elemento: "app-dashboard", titulo: "Agenda de consultas" },
  consulta: { elemento: "app-consulta", titulo: "Registro de atención" },
};

function rutaActual() {
  const hash = window.location.hash.replace(/^#\/?/, "");
  const [nombre, parametro] = hash.split("/");
  return { nombre: RUTAS[nombre] ? nombre : "buscar", parametro: parametro ?? null };
}

class AppShell extends Vista {
  static plantilla = `
    <button type="button" class="saltar">Ir al contenido principal</button>
    <header>
      <div class="ancho barra">
        <div class="marca">
          <div class="logo" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M7.5 3.5c-2.6 0-4 2-4 4.6 0 2.2.9 3.6 1.5 5.4.6 1.9.8 4.1 1.4 5.8.4 1.1 1 1.7 1.7 1.7 1 0 1.4-.9 1.7-2.4l.6-3c.2-1 .8-1.6 1.6-1.6s1.4.6 1.6 1.6l.6 3c.3 1.5.7 2.4 1.7 2.4.7 0 1.3-.6 1.7-1.7.6-1.7.8-3.9 1.4-5.8.6-1.8 1.5-3.2 1.5-5.4 0-2.6-1.4-4.6-4-4.6-1.8 0-2.7 1-4.5 1s-2.7-1-4.5-1Z"/>
            </svg>
          </div>
          <div>
            <h1>GalenoSV</h1>
            <p>Gestión clínica odontológica</p>
          </div>
        </div>
        <nav aria-label="Navegación principal">
          <button class="enlace" data-ruta="buscar" type="button" aria-current="page">Buscar citas</button>
          <button class="enlace" id="acceso" data-ruta="acceso" type="button">Acceso empleados</button>
        </nav>
        <div class="usuario"></div>
        <button type="button" class="tema" id="tema" aria-pressed="false" aria-label="Tema oscuro" title="Tema oscuro">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M20.5 14.2A8.5 8.5 0 1 1 9.8 3.5a6.8 6.8 0 0 0 10.7 10.7Z"/>
          </svg>
        </button>
      </div>
    </header>
    <div class="estado-conexion" id="estado-conexion" hidden role="status"></div>
    <main class="contenedor" id="vista" tabindex="-1"></main>
    <footer class="contenedor">PPI115 2026 · GalenoSV · Aplicación web progresiva</footer>
    <pwa-instalar></pwa-instalar>
    <ui-toast></ui-toast>
  `;

  #iniciado = false;
  #sincronizando = false;
  #identidadVisible;
  #quitarEscuchaSesion = null;
  #quitarEscuchaTema = null;

  connectedCallback() {
    if (this.#iniciado) return;
    this.#iniciado = true;
    this.montarPlantilla();

    this.escuchar(this.shadowRoot, "click", (evento) => {
      const boton = evento.target.closest("button[data-ruta]");
      if (!boton) return;
      let destino = boton.dataset.ruta;
      if (destino === "acceso") destino = leerSesion() ? "dashboard" : "login";
      this.navegar(destino);
    });
    // Las rutas usan el hash: el salto es un botón que mueve el foco, no un enlace que cambiaría la dirección.
    this.q(".saltar").addEventListener("click", () => this.q("#vista").focus());
    this.escuchar(this, "navegar", (evento) => {
      window.location.hash = `#/${evento.detail}`;
    });
    this.escuchar(this.q("#estado-conexion"), "click", (evento) => {
      const boton = evento.target.closest("#reintentar-servicio");
      if (!boton) return;
      boton.disabled = true;
      boton.textContent = "Comprobando…";
      comprobarServicio().then(() => this.actualizarConexion());
    });
    this.escuchar(window, "hashchange", () => this.montar());
    const alCambiarConexion = () => {
      this.actualizarConexion();
      if (estaConectado() && !servicioNoResponde() && listarPendientes().length > 0) {
        this.sincronizarPendientes();
      }
    };
    for (const tipo of ["online", "offline", "pendientes-cambio", "servicio-cambio"]) {
      this.escuchar(window, tipo, alCambiarConexion);
    }
    this.actualizarConexion();
    this.#quitarEscuchaSesion = alCambiarSesion(() => this.actualizarSesion());
    this.actualizarSesion();
    this.q("#tema").addEventListener("click", () => this.mostrarTema(alternarTema()));
    this.#quitarEscuchaTema = alCambiarTema((tema) => this.mostrarTema(tema));
    const tema = temaActual();
    aplicarTema(tema);
    this.mostrarTema(tema);
    this.montar();
    if (estaConectado() && listarPendientes().length > 0) this.sincronizarPendientes();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.#quitarEscuchaSesion?.();
    this.#quitarEscuchaSesion = null;
    this.#quitarEscuchaTema?.();
    this.#quitarEscuchaTema = null;
    this.#iniciado = false;
  }

  /** Interruptor «Tema oscuro»: el nombre no cambia; aria-pressed dice si está activo. */
  mostrarTema(tema) {
    this.q("#tema").setAttribute("aria-pressed", String(tema === "oscuro"));
  }

  actualizarConexion() {
    const aviso = this.q("#estado-conexion");
    if (!aviso) return;
    const pendientes = listarPendientes().length;
    const sinRed = !estaConectado();
    if (!sinRed && !servicioNoResponde()) {
      aviso.hidden = true;
      aviso.replaceChildren();
      return;
    }
    const cola = pendientes > 0 ? ` y hay ${cantidad(pendientes, "cambio pendiente", "cambios pendientes")} por enviar` : "";
    aviso.hidden = false;
    aviso.classList.toggle("sin-red", sinRed);
    aviso.classList.toggle("servidor-caido", !sinRed);
    aviso.setAttribute("role", sinRed ? "status" : "alert");
    aviso.replaceChildren(
      fragmento(
        sinRed
          ? html`<span class="icono" aria-hidden="true">!</span><span class="texto"><strong>Sin conexión</strong><span class="detalle">Los datos mostrados son los guardados en este dispositivo${cola}.</span></span>`
          : html`<span class="icono" aria-hidden="true">!</span><span class="texto"><strong>Servicio no disponible</strong><span class="detalle">El servidor de GalenoSV no responde. Está viendo datos guardados en este dispositivo${cola}; los cambios se enviarán cuando vuelva.</span></span><button type="button" id="reintentar-servicio" aria-label="Reintentar conexión con el servidor">Reintentar</button>`
      )
    );
  }

  sincronizarPendientes() {
    if (this.#sincronizando) return Promise.resolve();
    this.#sincronizando = true;
    return sincronizar((pendiente) => ejecutarPendiente(pendiente))
      .then((resultado) => {
        this.actualizarConexion();
        if (resultado.enviadas > 0) {
          // Lo guardado en memoria no incluye lo recién enviado: la próxima vista lo pide al servidor.
          limpiarCache();
          window.dispatchEvent(new Event("datos-sincronizados"));
          const enviadas = cantidad(resultado.enviadas, "cambio acumulado", "cambios acumulados");
          this.notificar(`Se ${resultado.enviadas === 1 ? "envió" : "enviaron"} ${enviadas}.`, "ok");
        }
        return resultado;
      })
      .finally(() => {
        this.#sincronizando = false;
      });
  }

  actualizarSesion() {
    const sesion = leerSesion();
    const usuario = this.q(".usuario");
    // Por id: data-ruta cambia entre "acceso" y "dashboard" según haya sesión.
    const botonAcceso = this.q("#acceso");
    if (!usuario || !botonAcceso) return;
    const identidad = sesion
      ? `${sesion.idPersona ?? ""}|${sesion.nombre ?? ""}|${sesion.nombreCorto ?? ""}`
      : "";
    if (identidad === this.#identidadVisible) return;
    this.#identidadVisible = identidad;
    if (sesion) {
      botonAcceso.textContent = "Mi panel";
      botonAcceso.dataset.ruta = "dashboard";
      usuario.replaceChildren(
        fragmento(html`<span class="nombre" title="${sesion.nombre}">${sesion.nombreCorto ?? sesion.nombre}</span><button class="enlace" type="button" id="salir">Salir</button>`)
      );
      this.q("#salir").addEventListener("click", () => {
        cerrarSesion();
        this.navegar("login");
      });
    } else {
      botonAcceso.textContent = "Acceso empleados";
      botonAcceso.dataset.ruta = "acceso";
      usuario.replaceChildren();
    }
  }

  montar() {
    const { nombre, parametro } = rutaActual();
    const sesion = leerSesion();
    if ((nombre === "dashboard" || nombre === "consulta") && !sesion) {
      window.location.hash = "#/login";
      return;
    }
    if (nombre === "login" && sesion) {
      window.location.hash = "#/dashboard";
      return;
    }
    document.title = `${RUTAS[nombre].titulo} · GalenoSV`;
    const contenedor = this.q("#vista");
    const vista = document.createElement(RUTAS[nombre].elemento);
    if (parametro) vista.setAttribute("id-consulta", parametro);
    contenedor.replaceChildren(vista);
    for (const boton of this.qa("nav button[data-ruta]")) {
      const rutaBoton = boton.dataset.ruta;
      const activo =
        rutaBoton === nombre ||
        (nombre === "consulta" && rutaBoton === "dashboard") ||
        (nombre === "login" && rutaBoton === "acceso");
      boton.classList.toggle("activo", activo);
      if (activo) boton.setAttribute("aria-current", "page");
      else boton.removeAttribute("aria-current");
    }
  }
}

definirElemento("app-shell", AppShell);
