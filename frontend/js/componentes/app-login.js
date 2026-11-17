import { Vista } from "../base.js";
import { cargarTodo, personaDeDocumento, empleadoDePersona, cumpleFormato } from "../datos.js";
import { guardarSesion } from "../estado.js";
import "./ui-autocompletar.js";

/** HU2 · Login de empleados con tipo y número de documento. */
export class AppLogin extends Vista {
  get estilos() {
    return `
      .marco { max-width: 440px; margin: 0 auto; }
      .tarjeta { padding: 1.6rem 1.6rem 1.35rem; }
      form { display: grid; gap: 1.05rem; }
      .titulo { display: grid; gap: 0.3rem; }
      .titulo h2 { font-size: 1.35rem; letter-spacing: -0.015em; }
      .ayuda { font-size: 0.82rem; color: var(--texto-suave, #4d6378); min-height: 1.2em; }
      #entrar { width: 100%; min-height: 2.85rem; }
      .pie {
        font-size: 0.88rem; color: var(--texto-suave, #4d6378);
        padding-top: 1rem; border-top: 1px solid var(--borde, #d6dee7);
      }
      .legal { font-size: 0.75rem; color: var(--texto-suave, #4d6378); line-height: 1.5; }
      @media (max-width: 520px) { .tarjeta { padding: 1.25rem 1.1rem 1.1rem; } }
    `;
  }

  connectedCallback() {
    this.render(`
      <div class="marco">
        <section class="tarjeta">
          <form id="formulario" novalidate aria-labelledby="titulo-login">
            <div class="titulo">
              <h2 id="titulo-login">Iniciar sesión</h2>
              <p class="aviso">Acceso exclusivo para el personal de la clínica.</p>
            </div>
            <ui-autocompletar id="tipo" etiqueta="Tipo de documento"></ui-autocompletar>
            <div class="campo">
              <label for="numero">Número de documento</label>
              <input id="numero" name="numero" autocomplete="username" required aria-describedby="formato" />
              <small class="ayuda" id="formato"></small>
            </div>
            <p class="error" id="error" role="alert" hidden></p>
            <button type="submit" id="entrar">Iniciar sesión</button>
            <p class="pie">¿Es paciente? <button class="enlace" type="button" id="ir-buscar">Busque sus citas aquí</button></p>
            <p class="legal">El acceso a GalenoSV está reservado al personal de salud acreditado por la clínica. El uso de esta aplicación registra la identidad del empleado y la trazabilidad de la atención brindada. Los datos clínicos son confidenciales y solo deben consultarse con fines asistenciales.</p>
          </form>
        </section>
      </div>
    `);
    this.q("#ir-buscar").addEventListener("click", () => this.navegar("buscar"));
    this.q("#tipo").addEventListener("change", () => {
      this.mostrarFormato();
      this.mostrarError("");
    });
    // El error se refiere a lo escrito antes: al corregir el número deja de mostrarse.
    this.q("#numero").addEventListener("input", () => this.mostrarError(""));
    this.q("#formulario").addEventListener("submit", (evento) => {
      evento.preventDefault();
      this.entrar();
    });
    this.cargar();
  }

  async cargar() {
    const tipo = this.q("#tipo");
    try {
      this._datos = await cargarTodo();
      const tipos = this._datos.tiposDocumento
        .filter((item) => item.activo !== false)
        .sort((a, b) => String(a.nombre).localeCompare(String(b.nombre)));
      tipo.opciones = tipos.map((item) => ({ valor: item.idTipoDocumento, texto: item.nombre }));
    } catch {
      tipo.mensaje = "No se pudo cargar";
      this.mostrarError("No se pudo conectar con el servicio. Intente de nuevo.");
    }
  }

  tipoElegido() {
    const id = this.q("#tipo").value;
    return (this._datos?.tiposDocumento ?? []).find((item) => String(item.idTipoDocumento) === String(id)) ?? null;
  }

  mostrarFormato() {
    const tipo = this.tipoElegido();
    this.q("#formato").textContent = tipo?.indicaciones ?? "";
    this.q("#numero").placeholder = tipo?.nombre === "DUI" ? "Ej. 00000000-0" : "";
  }

  mostrarError(mensaje) {
    const error = this.q("#error");
    error.textContent = mensaje;
    error.hidden = !mensaje;
  }

  async entrar() {
    this.mostrarError("");
    const idTipoDocumento = this.q("#tipo").value;
    const numero = this.q("#numero").value.trim();
    if (!idTipoDocumento || !numero) {
      this.mostrarError("Seleccione el tipo de documento e ingrese el número.");
      return;
    }
    const boton = this.q("#entrar");
    boton.disabled = true;
    boton.textContent = "Validando…";
    try {
      const datos = this._datos ?? (this._datos = await cargarTodo());
      const tipo = this.tipoElegido();
      if (!cumpleFormato(tipo, numero)) {
        this.mostrarError(`El número no tiene el formato esperado: ${tipo.indicaciones || tipo.expresionRegular}.`);
        return;
      }
      const persona = personaDeDocumento(datos, idTipoDocumento, numero);
      const asignaciones = persona ? empleadoDePersona(datos, persona.idPersona) : [];
      // Mismo mensaje si el documento no existe o no es de un empleado: no se revela quién está registrado.
      if (asignaciones.length === 0) {
        this.mostrarError("No hay un empleado registrado con ese documento. Verifique el tipo y el número.");
        return;
      }
      const roles = asignaciones.map(({ personaRol, rol, clinica }) => ({
        idPersonaRol: personaRol.idPersonaRol,
        idRol: rol?.idRol,
        nombreRol: rol?.nombre ?? "Sin rol",
        idClinica: clinica?.idClinica ?? personaRol.idClinica,
        nombreClinica: clinica?.nombre ?? "Sin clínica",
      }));
      const clinicas = new Map(roles.map((rol) => [rol.idClinica, rol.nombreClinica]));
      guardarSesion({
        idPersona: persona.idPersona,
        nombre: `${persona.nombres} ${persona.apellidos}`.trim(),
        documento: { tipo: tipo?.nombre ?? "", valor: numero },
        roles,
        clinicas: [...clinicas].map(([idClinica, nombre]) => ({ idClinica, nombre })),
        idClinica: roles[0].idClinica,
        idRol: roles[0].idRol,
      });
      this.notificar(`Bienvenido, ${persona.nombres}.`, "ok");
      this.navegar("dashboard");
    } catch {
      this.mostrarError("No se pudo validar el acceso. Intente de nuevo.");
    } finally {
      boton.disabled = false;
      boton.textContent = "Iniciar sesión";
    }
  }
}

customElements.define("app-login", AppLogin);
