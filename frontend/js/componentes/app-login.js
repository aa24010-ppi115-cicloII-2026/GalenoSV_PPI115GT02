import { Vista } from "../base.js";
import { cargarTodo, personaDeDocumento, empleadoDePersona, cumpleFormato } from "../datos.js";
import { guardarSesion } from "../estado.js";
import "./ui-autocompletar.js";

/** HU2 · Login de empleados con tipo y número de documento. */
export class AppLogin extends Vista {
  get estilos() {
    return `
      .marco {
        display: grid;
        grid-template-columns: 1.05fr 1fr;
        gap: 1.5rem;
        align-items: stretch;
        max-width: 980px;
        margin: 0 auto;
      }
      .intro {
        background: linear-gradient(150deg, var(--mate-fuerte, #1f3a50), var(--mate, #2c4a63) 45%, var(--acento-fuerte, #0b6155));
        color: #f3f6f9;
        border-radius: var(--radio-grande, 14px);
        padding: 2rem 1.85rem;
        display: flex;
        flex-direction: column;
        gap: 0.9rem;
        justify-content: center;
      }
      .intro h2 { font-size: clamp(1.35rem, 1.1rem + 1vw, 1.7rem); line-height: 1.2; letter-spacing: -0.02em; }
      .intro p { color: #d3e0ea; font-size: 0.95rem; max-width: 46ch; }
      .intro ul { margin: 0.2rem 0 0; padding: 0; list-style: none; display: grid; gap: 0.55rem; font-size: 0.9rem; color: #e6eef4; }
      .intro li { display: flex; gap: 0.6rem; align-items: baseline; }
      .intro li::before {
        content: ""; flex: none; width: 0.45rem; height: 0.45rem; border-radius: 999px;
        background: #9fe3d6; transform: translateY(-0.1rem);
      }
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
      @media (max-width: 760px) {
        .marco { grid-template-columns: 1fr; }
        .intro { order: 2; padding: 1.4rem 1.25rem; }
        .tarjeta { padding: 1.25rem 1.1rem 1.1rem; }
      }
    `;
  }

  connectedCallback() {
    this.render(`
      <div class="marco">
        <section class="intro" aria-labelledby="titulo-intro">
          <h2 id="titulo-intro">Ingrese con su documento de empleado</h2>
          <p>GalenoSV valida su identidad con el tipo y número de documento registrado por la clínica. No existe registro público de cuentas.</p>
          <ul>
            <li>Consulte las citas del día según su rol y clínica.</li>
            <li>Registre notas, diagnóstico CIE-10 y plan de atención.</li>
            <li>Asigne exámenes y avance los pasos del procedimiento.</li>
          </ul>
        </section>
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
    this.q("#tipo").addEventListener("change", () => this.mostrarFormato());
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
    this.q("#numero").placeholder = tipo?.nombre === "DUI" ? "Ej. 01234567-8" : "";
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
      if (!persona) {
        this.mostrarError("No existe un empleado con ese documento. Verifique los datos.");
        return;
      }
      const asignaciones = empleadoDePersona(datos, persona.idPersona);
      if (asignaciones.length === 0) {
        this.mostrarError("El documento no tiene un rol de empleado activo en una clínica.");
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
