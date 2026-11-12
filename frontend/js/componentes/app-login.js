import { Vista, escapar } from "../base.js";
import { cargarTodo, personaDeDocumento, empleadoDePersona } from "../datos.js";
import { guardarSesion } from "../estado.js";

export class AppLogin extends Vista {
  get estilos() {
    return `
      .marco {
        display: grid;
        grid-template-columns: 1.1fr 1fr;
        gap: 1.5rem;
        align-items: stretch;
        margin-top: 1rem;
      }
      .intro {
        background: linear-gradient(150deg, #2c4a63, #0f7b6c);
        color: #f4f7fb;
        border-radius: 20px;
        padding: 1.6rem;
        display: flex;
        flex-direction: column;
        gap: 0.8rem;
        justify-content: center;
      }
      .intro h2 { font-size: 1.5rem; line-height: 1.2; }
      .intro p { opacity: 0.9; font-size: 0.92rem; }
      .intro ul { margin: 0.4rem 0 0; padding-left: 1.1rem; font-size: 0.88rem; opacity: 0.92; display: grid; gap: 0.3rem; }
      form { display: grid; gap: 1rem; }
      .titulo { display: grid; gap: 0.2rem; }
      .titulo h2 { font-size: 1.25rem; }
      .pie { font-size: 0.85rem; color: #587089; }
      .enlaces { display: flex; gap: 0.9rem; flex-wrap: wrap; }
      .legal {
        font-size: 0.72rem; color: #7a8ca0; line-height: 1.45;
        border-top: 1px solid #e2e8f0; padding-top: 0.6rem;
      }
      @media (max-width: 760px) { .marco { grid-template-columns: 1fr; } .intro { order: 2; } }
    `;
  }

  connectedCallback() {
    this.render(`
      <div class="marco">
        <section class="intro" aria-hidden="true">
          <span class="etiqueta" style="color:#9fe3d6">Acceso interno</span>
          <h2>Ingrese con su documento de empleado</h2>
          <p>GalenoSV valida su identidad con el tipo y número de documento registrado por la clínica. No existe registro público de cuentas.</p>
          <ul>
            <li>Consulte las citas del día según su rol y clínica.</li>
            <li>Registre notas, diagnóstico CIE-10 y plan de atención.</li>
            <li>Asigne exámenes y avance los pasos del procedimiento.</li>
          </ul>
        </section>
        <section class="tarjeta">
          <form id="formulario" novalidate>
            <div class="titulo">
              <span class="etiqueta">Empleados</span>
              <h2>Iniciar sesión</h2>
            </div>
            <div class="campo">
              <label for="tipo">Tipo de documento</label>
              <select id="tipo" required><option value="">Cargando…</option></select>
            </div>
            <div class="campo">
              <label for="numero">Número de documento</label>
              <input id="numero" name="numero" autocomplete="username" placeholder="Ej. 01234567-8" required />
            </div>
            <p class="error" id="error" hidden></p>
            <button type="submit" id="entrar">Iniciar sesión</button>
            <div class="enlaces">
              <button class="enlace" type="button" id="recuperar">¿Olvidó mi contraseña?</button>
              <button class="enlace" type="button" id="solicitar">Solicitar acceso</button>
            </div>
            <p class="pie">¿Es paciente? <button class="enlace" type="button" id="ir-buscar">Busque sus citas aquí</button></p>
            <p class="legal">Aviso legal: el acceso a GalenoSV está reservado al personal de salud acreditado por la clínica. El uso de esta aplicación registra la identidad del empleado y la trazabilidad de la atención brindada. Los datos clínicos son confidenciales y solo deben ser consultados para fines asistenciales.</p>
          </form>
        </section>
      </div>
    `);
    this.q("#ir-buscar").addEventListener("click", () => this.navegar("buscar"));
    this.q("#recuperar").addEventListener("click", () =>
      this.notificar("La recuperación de credenciales la realiza el administrador de la clínica.", "info")
    );
    this.q("#solicitar").addEventListener("click", () =>
      this.notificar("La solicitud de acceso debe realizarla el administrador de la clínica.", "info")
    );
    this.q("#formulario").addEventListener("submit", (evento) => {
      evento.preventDefault();
      this.entrar();
    });
    this.cargar();
  }

  async cargar() {
    const tipo = this.q("#tipo");
    try {
      const datos = await cargarTodo();
      const tipos = [...datos.tiposDocumento].sort((a, b) =>
        String(a.nombre).localeCompare(String(b.nombre))
      );
      tipo.innerHTML =
        `<option value="">Seleccione…</option>` +
        tipos
          .map((item) => `<option value="${escapar(item.idTipoDocumento)}">${escapar(item.nombre)}</option>`)
          .join("");
    } catch {
      tipo.innerHTML = `<option value="">No se pudo cargar</option>`;
      this.mostrarError("No se pudo conectar con el servicio. Intente de nuevo.");
    }
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
      const tipo = datos.tiposDocumento.find(
        (item) => String(item.idTipoDocumento) === String(idTipoDocumento)
      );
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
