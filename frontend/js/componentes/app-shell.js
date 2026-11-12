import { Vista, escapar } from "../base.js";
import { leerSesion, cerrarSesion, alCambiarSesion } from "../estado.js";
import { estaConectado, sincronizar, listarPendientes } from "../pendientes.js";
import { ejecutarPendiente } from "../api.js";

const VISTAS = {
  buscar: "app-busqueda",
  login: "app-login",
  dashboard: "app-dashboard",
  consulta: "app-consulta",
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
        background: var(--fondo, #f4f7fb);
      }
      .contenedor { max-width: 1120px; margin: 0 auto; padding: 0 1rem; }
      header {
        background: linear-gradient(120deg, #14202b, #2c4a63 60%, #0f7b6c);
        color: #f4f7fb;
        box-shadow: 0 6px 24px rgba(20, 32, 43, 0.2);
      }
      .barra { display: flex; align-items: center; gap: 1rem; padding-block: 0.9rem; flex-wrap: wrap; }
      .marca { display: flex; align-items: center; gap: 0.7rem; margin-right: auto; }
      .logo {
        width: 40px; height: 40px; border-radius: 12px; display: grid; place-items: center;
        background: #0f7b6c; color: #fff; font-weight: 800; font-size: 1.1rem;
      }
      .marca h1 { font-size: 1.05rem; margin: 0; letter-spacing: 0.02em; }
      .marca p { margin: 0; font-size: 0.72rem; opacity: 0.72; }
      nav { display: flex; align-items: center; gap: 0.25rem; flex-wrap: wrap; }
      nav button.enlace {
        border: none; background: transparent; color: #dbe7f0; padding: 0.45rem 0.7rem;
        border-radius: 8px; font-size: 0.85rem; text-decoration: none;
      }
      nav button.enlace:hover { background: rgba(255,255,255,0.12); }
      nav button.enlace.activo { background: rgba(255,255,255,0.18); color: #fff; }
      .usuario { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; }
      .usuario .nombre { opacity: 0.9; max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .usuario button.enlace { color: #dbe7f0; font-size: 0.8rem; }
      main { flex: 1; padding-block: 1.5rem 2.5rem; }
      .saltar {
        position: absolute;
        left: -999px;
        top: 0.5rem;
        background: #fff;
        color: var(--mate);
        padding: 0.5rem 0.8rem;
        border-radius: 8px;
        z-index: 100;
      }
      .saltar:focus { left: 0.5rem; }
      .estado-conexion {
        display: flex; align-items: center; gap: 0.5rem; justify-content: center;
        background: #fff4e0; color: #8a5a00; font-size: 0.8rem; padding: 0.5rem 1rem;
        border-bottom: 1px solid #f2d9a8;
      }
      .estado-conexion[hidden] { display: none; }
      .estado-conexion button {
        background: transparent; border: 1px solid #8a5a00; color: #8a5a00;
        border-radius: 8px; padding: 0.15rem 0.6rem; font-size: 0.75rem;
      }
      footer { padding-block: 1rem 1.5rem; color: #587089; font-size: 0.75rem; text-align: center; }
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
            <div class="logo" aria-hidden="true">G</div>
            <div>
              <h1>GalenoSV</h1>
              <p>Gestión clínica odontológica</p>
            </div>
          </div>
          <nav aria-label="Navegación principal">
            <button class="enlace" data-ruta="buscar" type="button" aria-current="page">Buscar citas</button>
            <button class="enlace" data-ruta="acceso" type="button">Acceso empleados</button>
          </nav>
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
    if (estaConectado()) {
      aviso.hidden = true;
      aviso.innerHTML = "";
      return;
    }
    aviso.hidden = false;
    aviso.innerHTML = `<span>Sin conexión: los datos mostrados son los guardados en este dispositivo${
      pendientes > 0 ? ` y hay ${pendientes} cambio(s) pendiente(s) por enviar` : ""
    }.</span>`;
  }

  async sincronizarPendientes() {
    const resultado = await sincronizar((pendiente) => ejecutarPendiente(pendiente));
    this.actualizarConexion();
    if (resultado.enviadas > 0) {
      this.notificar(`Se enviaron ${resultado.enviadas} cambio(s) acumulado(s).`, "ok");
    }
    return resultado;
  }

  actualizarSesion() {
    const sesion = leerSesion();
    const usuario = this.q(".usuario");
    const botonAcceso = this.q('[data-ruta="acceso"]');
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
    const contenedor = this.q("#vista");
    const vista = document.createElement(VISTAS[nombre]);
    if (parametro) vista.setAttribute("id-consulta", parametro);
    contenedor.replaceChildren(vista);
    for (const boton of this.qa('nav button[data-ruta="buscar"], nav button[data-ruta="dashboard"]')) {
      const rutaBoton = boton.dataset.ruta;
      const activo = rutaBoton === nombre || (nombre === "consulta" && rutaBoton === "dashboard");
      boton.classList.toggle("activo", activo);
      if (activo) boton.setAttribute("aria-current", "page");
      else boton.removeAttribute("aria-current");
    }
  }
}

customElements.define("app-shell", AppShell);
