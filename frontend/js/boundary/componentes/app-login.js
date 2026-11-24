import { Vista, definirElemento } from "../base.js";
import { nombreCompleto, nombreCorto } from "../../comun/texto.js";
import { cargarTodo } from "../../control/repositorio.js";
import { personaDeDocumento, empleadoDePersona } from "../../control/personas.js";
import { errorDeFormatoDocumento, indicacionesDocumento, tiposDocumentoActivos, tipoDocumentoElegido } from "../../control/documentos.js";
import { guardarSesion } from "../../control/sesion.js";
import "./ui-autocompletar.js";

/** Asignaciones de un empleado convertidas en los roles y clínicas que guarda la sesión. */
function rolesYClinicas(asignaciones) {
  const roles = asignaciones.map(({ personaRol, rol, clinica }) => ({
    idPersonaRol: personaRol.idPersonaRol,
    idRol: rol?.idRol,
    nombreRol: rol?.nombre ?? "Sin rol",
    idClinica: clinica?.idClinica ?? personaRol.idClinica,
    nombreClinica: clinica?.nombre ?? "Sin clínica",
  }));
  const clinicas = [];
  for (const rol of roles) {
    if (!clinicas.some((clinica) => String(clinica.idClinica) === String(rol.idClinica))) {
      clinicas.push({ idClinica: rol.idClinica, nombre: rol.nombreClinica });
    }
  }
  return { roles, clinicas };
}

/** HU2 · Login de empleados con tipo y número de documento. */
export default class AppLogin extends Vista {
  static plantilla = `
    <div class="marco">
      <section class="tarjeta">
        <form id="formulario" novalidate aria-labelledby="titulo-login">
          <div class="titulo">
            <h2 id="titulo-login">Iniciar sesión</h2>
            <p class="aviso">Acceso exclusivo para el personal de la clínica.</p>
          </div>
          <ui-autocompletar id="tipo" name="tipo" etiqueta="Tipo de documento" required></ui-autocompletar>
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
  `;

  #datos = null;

  connectedCallback() {
    this.montarPlantilla();
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

  cargar() {
    const tipo = this.q("#tipo");
    return cargarTodo()
      .then((datos) => {
        this.#datos = datos;
        if (!this.isConnected) return;
        const tipos = tiposDocumentoActivos(this.#datos);
        tipo.opciones = tipos.map((item) => ({ valor: item.idTipoDocumento, texto: item.nombre }));
      })
      .catch(() => {
        if (!this.isConnected) return;
        tipo.mensaje = "No se pudo cargar";
        this.mostrarError("No se pudo conectar con el servicio. Intente de nuevo.");
      });
  }

  tipoElegido() {
    return tipoDocumentoElegido(this.#datos, this.q("#tipo").value);
  }

  mostrarFormato() {
    const formato = indicacionesDocumento(this.tipoElegido());
    this.q("#formato").textContent = formato.texto;
    this.q("#numero").placeholder = formato.ejemplo;
  }

  mostrarError(mensaje) {
    const error = this.q("#error");
    error.textContent = mensaje;
    error.hidden = !mensaje;
  }

  entrar() {
    this.mostrarError("");
    const idTipoDocumento = this.q("#tipo").value;
    const numero = this.q("#numero").value.trim();
    if (!idTipoDocumento || !numero) {
      this.mostrarError("Seleccione el tipo de documento e ingrese el número.");
      return Promise.resolve();
    }
    const boton = this.q("#entrar");
    boton.disabled = true;
    boton.textContent = "Validando…";
    return (this.#datos ? Promise.resolve(this.#datos) : cargarTodo())
      .then((datos) => {
        this.#datos = datos;
        if (!this.isConnected) return;
        const tipo = this.tipoElegido();
        const errorFormato = errorDeFormatoDocumento(tipo, numero);
        if (errorFormato) {
          this.mostrarError(errorFormato);
          return;
        }
        const persona = personaDeDocumento(this.#datos, idTipoDocumento, numero);
        const asignaciones = persona ? empleadoDePersona(this.#datos, persona.idPersona) : [];
        // Mismo mensaje si el documento no existe o no es de un empleado: no se revela quién está registrado.
        if (asignaciones.length === 0) {
          this.mostrarError("No hay un empleado registrado con ese documento. Verifique el tipo y el número.");
          return;
        }
        const { roles, clinicas } = rolesYClinicas(asignaciones);
        const idClinica = clinicas.length === 1 ? clinicas[0].idClinica : "";
        const rolesEnClinica = roles.filter((rol) => String(rol.idClinica) === String(idClinica));
        guardarSesion({
          idPersona: persona.idPersona,
          nombre: nombreCompleto(persona),
          nombreCorto: nombreCorto(persona),
          documento: { tipo: tipo?.nombre ?? "", valor: numero },
          roles,
          clinicas,
          idClinica,
          idRol: rolesEnClinica.length === 1 ? rolesEnClinica[0].idRol : "",
        });
        this.notificar(`Bienvenido, ${persona.nombres}.`, "ok");
        this.navegar("dashboard");
      })
      .catch(() => this.mostrarError("No se pudo validar el acceso. Intente de nuevo."))
      .finally(() => {
        boton.disabled = false;
        boton.textContent = "Iniciar sesión";
      });
  }
}

definirElemento("app-login", AppLogin);
