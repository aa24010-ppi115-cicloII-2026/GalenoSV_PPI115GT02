import { listar, crear, actualizar, eliminar } from "./api.js";
import { recursos, recursoPorClave } from "./recursos.js";

const navegacion = document.querySelector("#navegacion");
const panel = document.querySelector("#panel");
const dialogo = document.querySelector("#dialogo");
const formulario = document.querySelector("#formulario");
const campos = document.querySelector("#campos");
const tituloFormulario = document.querySelector("#titulo-formulario");
const notificacion = document.querySelector("#notificacion");

const estado = {
  recurso: recursos[0],
  first: 0,
  max: 10,
  datos: [],
  total: 0,
  editando: null,
};

const referencias = new Map();

function elemento(etiqueta, atributos = {}, texto = null) {
  const nodo = document.createElement(etiqueta);
  for (const [clave, valor] of Object.entries(atributos)) {
    if (clave === "clase") nodo.className = valor;
    else if (clave === "dataset") Object.assign(nodo.dataset, valor);
    else nodo.setAttribute(clave, valor);
  }
  if (texto !== null) nodo.textContent = texto;
  return nodo;
}

function notificar(mensaje, esError = false) {
  const aviso = elemento("div", { clase: esError ? "error" : "" }, mensaje);
  notificacion.append(aviso);
  setTimeout(() => aviso.remove(), 3500);
}

function construirNavegacion() {
  const grupos = new Map();
  for (const recurso of recursos) {
    if (!grupos.has(recurso.grupo)) grupos.set(recurso.grupo, []);
    grupos.get(recurso.grupo).push(recurso);
  }
  navegacion.replaceChildren();
  for (const [titulo, lista] of grupos) {
    navegacion.append(elemento("p", { clase: "grupo" }, titulo));
    for (const recurso of lista) {
      const boton = elemento("button", { type: "button", dataset: { clave: recurso.clave } }, recurso.titulo);
      boton.addEventListener("click", () => seleccionar(recurso.clave));
      navegacion.append(boton);
    }
  }
  marcarActivo();
}

function marcarActivo() {
  for (const boton of navegacion.querySelectorAll("button")) {
    boton.classList.toggle("activo", boton.dataset.clave === estado.recurso.clave);
  }
}

function seleccionar(clave) {
  estado.recurso = recursoPorClave(clave);
  estado.first = 0;
  marcarActivo();
  cargar();
}

async function cargarReferencias(recurso) {
  const rutas = [...new Set(recurso.campos.filter((c) => c.tipo === "select").map((c) => c.recurso))];
  await Promise.all(
    rutas.map(async (ruta) => {
      const referencia = recursoPorClave(ruta);
      const { datos } = await listar(referencia.ruta, { first: 0, max: 100 });
      const mapa = new Map(datos.map((registro) => [registro[referencia.id], referencia.etiqueta(registro)]));
      referencias.set(ruta, mapa);
    })
  );
}

async function cargar() {
  const { recurso } = estado;
  panel.replaceChildren(elemento("p", { clase: "aviso" }, "Cargando..."));
  try {
    await cargarReferencias(recurso);
    const { datos, total } = await listar(recurso.ruta, { first: estado.first, max: estado.max });
    estado.datos = datos;
    estado.total = total;
    renderizarPanel();
  } catch (error) {
    panel.replaceChildren(elemento("p", { clase: "aviso" }, `No se pudo cargar: ${error.message}`));
    notificar(error.message, true);
  }
}

function valorCelda(registro, campo) {
  const valor = registro[campo.campo];
  if (campo.tipo === "booleano") return valor ? "Sí" : "No";
  if (campo.tipo === "select") return referencias.get(campo.recurso)?.get(valor) ?? "—";
  if (valor === null || valor === undefined || valor === "") return "—";
  if (campo.tipo === "fechaHora") return String(valor).replace("T", " ").slice(0, 16);
  return String(valor);
}

function renderizarPanel() {
  const { recurso, datos, total, first, max } = estado;

  const barra = elemento("div", { clase: "barra" });
  const bloqueTitulo = elemento("div");
  bloqueTitulo.append(
    elemento("h2", {}, recurso.titulo),
    elemento("span", { clase: "contador" }, `${total} registro${total === 1 ? "" : "s"}`)
  );
  const botonNuevo = elemento("button", { type: "button", dataset: { accion: "nuevo" } }, "Nuevo");
  barra.append(bloqueTitulo, botonNuevo);

  const tabla = elemento("table");
  const cabecera = elemento("thead");
  const filaCabecera = elemento("tr");
  for (const campo of recurso.campos) filaCabecera.append(elemento("th", {}, campo.etiqueta));
  filaCabecera.append(elemento("th", {}, "Acciones"));
  cabecera.append(filaCabecera);

  const cuerpo = elemento("tbody");
  for (const registro of datos) {
    const fila = elemento("tr");
    for (const campo of recurso.campos) {
      const celda = elemento("td", campo.tipo === "textarea" ? { clase: "celda-larga" } : {}, valorCelda(registro, campo));
      if (campo.tipo === "textarea") celda.title = valorCelda(registro, campo);
      fila.append(celda);
    }
    const acciones = elemento("td", { clase: "acciones" });
    acciones.append(
      elemento("button", { type: "button", clase: "secundario", dataset: { accion: "editar", id: registro[recurso.id] } }, "Editar"),
      elemento("button", { type: "button", clase: "peligro", dataset: { accion: "eliminar", id: registro[recurso.id] } }, "Eliminar")
    );
    fila.append(acciones);
    cuerpo.append(fila);
  }
  if (datos.length === 0) {
    const fila = elemento("tr");
    const celda = elemento("td", { colspan: String(recurso.campos.length + 1) }, "No hay registros.");
    fila.append(celda);
    cuerpo.append(fila);
  }
  tabla.append(cabecera, cuerpo);

  const paginacion = elemento("div", { clase: "paginacion" });
  const anterior = elemento("button", { type: "button", clase: "secundario", dataset: { accion: "anterior" } }, "Anterior");
  const siguiente = elemento("button", { type: "button", clase: "secundario", dataset: { accion: "siguiente" } }, "Siguiente");
  anterior.disabled = first === 0;
  siguiente.disabled = first + max >= total;
  const desde = total === 0 ? 0 : first + 1;
  const hasta = Math.min(first + datos.length, total);
  paginacion.append(anterior, elemento("span", { clase: "contador" }, `${desde}-${hasta} de ${total}`), siguiente);

  panel.replaceChildren(barra, tabla, paginacion);
}

function abrirFormulario(registro = null) {
  const { recurso } = estado;
  estado.editando = registro;
  tituloFormulario.textContent = registro ? `Editar ${recurso.titulo}` : `Nuevo ${recurso.titulo}`;
  campos.replaceChildren();

  for (const campo of recurso.campos) {
    const contenedor = elemento("div", { clase: campo.tipo === "booleano" ? "campo booleano" : "campo" });
    const valor = registro ? registro[campo.campo] : campo.inicial;
    const idInput = `campo-${campo.campo}`;

    if (campo.tipo === "booleano") {
      const entrada = elemento("input", { type: "checkbox", id: idInput, name: campo.campo });
      entrada.checked = Boolean(valor ?? false);
      contenedor.append(entrada, elemento("label", { for: idInput }, campo.etiqueta));
      campos.append(contenedor);
      continue;
    }

    contenedor.append(elemento("label", { for: idInput }, campo.requerido ? `${campo.etiqueta} *` : campo.etiqueta));
    let entrada;
    if (campo.tipo === "textarea") {
      entrada = elemento("textarea", { id: idInput, name: campo.campo });
      entrada.value = valor ?? "";
    } else if (campo.tipo === "select") {
      entrada = elemento("select", { id: idInput, name: campo.campo });
      entrada.append(elemento("option", { value: "" }, "Seleccione..."));
      const opciones = referencias.get(campo.recurso) ?? new Map();
      for (const [id, etiqueta] of opciones) {
        const opcion = elemento("option", { value: id }, etiqueta);
        opcion.selected = id === valor;
        entrada.append(opcion);
      }
      if (valor && !opciones.has(valor)) {
        const opcion = elemento("option", { value: valor }, valor);
        opcion.selected = true;
        entrada.append(opcion);
      }
    } else {
      const tipo = campo.tipo === "fecha" ? "date" : campo.tipo === "fechaHora" ? "datetime-local" : "text";
      entrada = elemento("input", { type: tipo, id: idInput, name: campo.campo });
      if (valor) {
        entrada.value = campo.tipo === "fechaHora" ? String(valor).slice(0, 16) : String(valor).slice(0, 10);
      }
    }
    if (campo.requerido) entrada.required = true;
    contenedor.append(entrada);
    campos.append(contenedor);
  }
  dialogo.showModal();
}

function datosDelFormulario() {
  const datos = {};
  for (const campo of estado.recurso.campos) {
    const entrada = formulario.elements[campo.campo];
    if (campo.tipo === "booleano") {
      datos[campo.campo] = entrada.checked;
    } else {
      const valor = entrada.value.trim();
      datos[campo.campo] = valor === "" ? null : valor;
    }
  }
  return datos;
}

async function guardar(evento) {
  evento.preventDefault();
  const { recurso, editando } = estado;
  const datos = datosDelFormulario();
  const boton = document.querySelector("#guardar");
  boton.disabled = true;
  try {
    if (editando) {
      await actualizar(recurso.ruta, editando[recurso.id], datos);
      notificar("Registro actualizado.");
    } else {
      await crear(recurso.ruta, datos);
      notificar("Registro creado.");
    }
    dialogo.close();
    await cargar();
  } catch (error) {
    notificar(error.message, true);
  } finally {
    boton.disabled = false;
  }
}

async function borrar(registro) {
  const { recurso } = estado;
  const etiqueta = recurso.etiqueta(registro) || "este registro";
  if (!confirm(`¿Eliminar ${etiqueta}?`)) return;
  try {
    await eliminar(recurso.ruta, registro[recurso.id]);
    notificar("Registro eliminado.");
    if (estado.datos.length === 1 && estado.first > 0) estado.first = Math.max(0, estado.first - estado.max);
    await cargar();
  } catch (error) {
    notificar(error.message, true);
  }
}

panel.addEventListener("click", (evento) => {
  const boton = evento.target.closest("button[data-accion]");
  if (!boton) return;
  const accion = boton.dataset.accion;
  if (accion === "nuevo") return abrirFormulario();
  if (accion === "anterior") {
    estado.first = Math.max(0, estado.first - estado.max);
    return cargar();
  }
  if (accion === "siguiente") {
    if (estado.first + estado.max < estado.total) {
      estado.first += estado.max;
      cargar();
    }
    return;
  }
  const registro = estado.datos.find((fila) => String(fila[estado.recurso.id]) === boton.dataset.id);
  if (!registro) return;
  if (accion === "editar") abrirFormulario(registro);
  if (accion === "eliminar") borrar(registro);
});

document.querySelector("#cancelar").addEventListener("click", () => dialogo.close());
formulario.addEventListener("submit", guardar);

construirNavegacion();
seleccionar(estado.recurso.clave);
