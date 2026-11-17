import { Vista, escapar, cantidad } from "../base.js";
import { leerSesion, cerrarSesion, alCambiarSesion } from "../estado.js";
import { estaConectado, sincronizar, listarPendientes } from "../pendientes.js";
import { ejecutarPendiente, servicioNoResponde } from "../api.js";
import { limpiarCache } from "../datos.js";

const VISTAS = {
  buscar: "app-busqueda",
  login: "app-login",
  dashboard: "app-dashboard",
  consulta: "app-consulta",
};

/** Título de la pestaña de cada vista: el lector de pantalla y el historial distinguen las páginas. */
const TITULOS = {
  buscar: "Buscar mis citas",
  login: "Acceso de empleados",
  dashboard: "Agenda de consultas",
  consulta: "Registro de atención",
};

function rutaActual() {
  const hash = window.location.hash.replace(/^#\/?/, "");
  const [nombre, parametro] = hash.split("/");
  return { nombre: VISTAS[nombre] ? nombre : "buscar", parametro: parametro ?? null };
}

export class AppShell extends Vista {
  get estilos() {
    return `
        :host {
        display: flex;
        flex-direction: column;
        min-height: 100vh;
        background: var(--fondo, #f3f6f9);
      }
      .contenedor { width: 100%; max-width: 1120px; margin: 0 auto; padding: 0 1rem; }
      header {
        background: linear-gradient(110deg, #14202b 0%, var(--mate-fuerte, #1f3a50) 55%, #134f4a 100%);
        color: #f3f6f9;
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
      }
      .barra { display: flex; align-items: center; gap: 1rem; padding-block: 0.8rem; flex-wrap: wrap; }
      .marca { display: flex; align-items: center; gap: 0.7rem; margin-right: auto; }
      .logo {
        width: 38px; height: 38px; border-radius: 11px; display: grid; place-items: center;
        background: var(--acento, #0f7b6c); color: #fff;
        box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.14);
      }
      .logo svg { width: 22px; height: 22px; }
      .marca h1 { font-size: 1.05rem; margin: 0; letter-spacing: 0.01em; font-weight: 700; }
      .marca p { margin: 0; font-size: 0.72rem; color: #b7c7d5; }
      nav { display: flex; align-items: center; gap: 0.25rem; flex-wrap: wrap; }
      nav button.enlace {
        min-height: 2.25rem; border: none; background: transparent; color: #d3e0ea; padding: 0.4rem 0.8rem;
        border-radius: 8px; font-size: 0.86rem; font-weight: 500; text-decoration: none;
      }
      nav button.enlace:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }
      nav button.enlace.activo { background: rgba(255, 255, 255, 0.16); color: #fff; font-weight: 600; }
      nav button.enlace:focus-visible, .usuario button.enlace:focus-visible { box-shadow: 0 0 0 2px #9fe3d6; }
      .usuario { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; }
      .usuario .nombre { opacity: 0.9; max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .usuario button.enlace { color: #d3e0ea; font-size: 0.8rem; }
      .usuario button.enlace:hover { color: #fff; }
      main.contenedor { flex: 1; padding-block: 2rem 3rem; }
      main:focus { outline: none; }
      .saltar {
        position: absolute;
        left: -999px;
        top: 0.5rem;
        background: var(--superficie, #fff);
        color: var(--mate, #2c4a63);
        padding: 0.5rem 0.8rem;
        border-radius: 8px;
        z-index: 100;
      }
      .saltar:focus { left: 0.5rem; }
      .estado-conexion {
        display: flex; align-items: center; gap: 0.5rem; justify-content: center;
        background: var(--alerta-suave, #fdf3e1); color: var(--alerta, #85570a); font-size: 0.8rem; padding: 0.5rem 1rem;
        border-bottom: 1px solid #f0d9ad;
      }
      .estado-conexion[hidden] { display: none; }
      .estado-conexion button {
        background: transparent; border: 1px solid var(--alerta, #85570a); color: var(--alerta, #85570a);
        border-radius: 8px; padding: 0.15rem 0.6rem; font-size: 0.75rem;
      }
      footer { padding-block: 1.25rem 1.5rem; color: var(--texto-tenue, #5f7387); font-size: 0.75rem; text-align: center; border-top: 1px solid var(--borde, #d6dee7); }
      @media (max-width: 620px) {
        .barra { gap: 0.4rem 0.5rem; }
        .contenedor { padding: 0 0.75rem; }
        .marca p { display: none; }
        .marca { margin-right: 0; }
        .usuario { margin-left: auto; }
        .usuario .nombre { display: none; }
        nav { order: 3; width: 100%; justify-content: flex-start; }
        nav button.enlace { font-size: 0.8rem; padding: 0.4rem 0.55rem; }
      }
    `;
  }

  connectedCallback() {
    if (this._iniciado) return;
    this._iniciado = true;
    this.render(`
      <a class="saltar" href="#vista">Ir al contenido principal</a>
      <header>
        <div class="contenedor barra">
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
          <pwa-instalar></pwa-instalar>
          <div class="usuario"></div>
        </div>
      </header>
      <div class="estado-conexion" id="estado-conexion" hidden role="status"></div>
      <main class="contenedor" id="vista" tabindex="-1"></main>
      <footer class="contenedor">PPI115 2026 · GalenoSV · Aplicación web progresiva</footer>
      <ui-toast></ui-toast>
    `);

    this.shadowRoot.addEventListener("click", (evento) => {
      const boton = evento.target.closest("button[data-ruta]");
      if (!boton) return;
      const ruta = boton.dataset.ruta;
      if (ruta === "buscar") this.navegar("buscar");
      else if (ruta === "dashboard") this.navegar("dashboard");
      else this.navegar(leerSesion() ? "dashboard" : "login");
    });

    this.addEventListener("navegar", (evento) => {
      const destino = evento.detail;
      window.location.hash = `#/${destino}`;
    });

    window.addEventListener("hashchange", () => this.montar());
    window.addEventListener("online", () => {
      this.actualizarConexion();
      this.sincronizarPendientes();
    });
    window.addEventListener("offline", () => this.actualizarConexion());
    window.addEventListener("pendientes-cambio", () => this.actualizarConexion());
    window.addEventListener("servicio-cambio", () => this.actualizarConexion());
    this.actualizarConexion();
    this._quitarEscucha = alCambiarSesion(() => this.actualizarSesion());
    this.actualizarSesion();
    this.montar();
  }

  navegar(destino) {
    window.location.hash = `#/${destino}`;
  }

  actualizarConexion() {
    const aviso = this.q("#estado-conexion");
    if (!aviso) return;
    const pendientes = listarPendientes().length;
    const sinRed = !estaConectado();
    if (!sinRed && !servicioNoResponde()) {
      aviso.hidden = true;
      aviso.innerHTML = "";
      return;
    }
    const motivo = sinRed ? "Sin conexión" : "El servidor de GalenoSV no responde";
    aviso.hidden = false;
    aviso.innerHTML = `<span>${motivo}: los datos mostrados son los guardados en este dispositivo${
      pendientes > 0 ? ` y hay ${cantidad(pendientes, "cambio pendiente", "cambios pendientes")} por enviar` : ""
    }.</span>`;
  }

  async sincronizarPendientes() {
    const resultado = await sincronizar((pendiente) => ejecutarPendiente(pendiente));
    this.actualizarConexion();
    if (resultado.enviadas > 0) {
      // Lo guardado en memoria no incluye lo recién enviado: la próxima vista lo pide al servidor.
      limpiarCache();
      const enviadas = cantidad(resultado.enviadas, "cambio acumulado", "cambios acumulados");
      this.notificar(`Se ${resultado.enviadas === 1 ? "envió" : "enviaron"} ${enviadas}.`, "ok");
    }
    return resultado;
  }

  actualizarSesion() {
    const sesion = leerSesion();
    const usuario = this.q(".usuario");
    // Por id: data-ruta cambia entre "acceso" y "dashboard" según haya sesión.
    const botonAcceso = this.q("#acceso");
    if (!usuario || !botonAcceso) return;
    if (sesion) {
      botonAcceso.textContent = "Mi panel";
      botonAcceso.dataset.ruta = "dashboard";
      usuario.innerHTML = `<span class="nombre">${escapar(sesion.nombre)}</span><button class="enlace" type="button" id="salir">Salir</button>`;
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
    document.title = `${TITULOS[nombre]} · GalenoSV`;
    const contenedor = this.q("#vista");
    const vista = document.createElement(VISTAS[nombre]);
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

customElements.define("app-shell", AppShell);
