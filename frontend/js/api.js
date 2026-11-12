import { encolar, estaConectado, sincronizar, listarPendientes } from "./pendientes.js";

export const API = "http://localhost:9080/GalenoSV-1.0-SNAPSHOT/api";

async function errorDe(respuesta) {
  let mensaje = `Error ${respuesta.status}`;
  const parametro = respuesta.headers.get("X-Parametro-Error");
  try {
    const cuerpo = await respuesta.json();
    mensaje = cuerpo.mensaje || cuerpo.message || mensaje;
  } catch {
    mensaje = `${mensaje} ${respuesta.statusText}`.trim();
  }
  if (parametro) mensaje = `${mensaje} (${parametro})`;
  const error = new Error(mensaje);
  error.status = respuesta.status;
  error.parametro = parametro;
  return error;
}

async function pedir(ruta, opciones = {}) {
  const cabeceras = { Accept: "application/json", "Content-Type": "application/json", ...(opciones.headers ?? {}) };
  const respuesta = await fetch(`${API}/${ruta}`, { ...opciones, headers: cabeceras });
  if (!respuesta.ok) throw await errorDe(respuesta);
  return respuesta;
}

/** Descarga un recurso binario, por ejemplo el ticket en PDF. */
export async function pedirBinario(ruta, nombreArchivo) {
  const respuesta = await pedir(ruta);
  const blob = await respuesta.blob();
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement("a");
  enlace.href = url;
  enlace.download = nombreArchivo;
  document.body.append(enlace);
  enlace.click();
  enlace.remove();
  setTimeout(() => URL.revokeObjectURL(url), 2000);
  return blob.size;
}

export async function listar(ruta, { first = 0, max = 20 } = {}) {
  const respuesta = await pedir(`${ruta}?first=${first}&max=${max}`);
  return {
    datos: await respuesta.json(),
    total: Number(respuesta.headers.get("X-Total-Count") ?? 0),
  };
}

export async function listarTodo(ruta) {
  const tamano = 100;
  let first = 0;
  let total = Infinity;
  const datos = [];
  while (first < total) {
    const pagina = await listar(ruta, { first, max: tamano });
    datos.push(...pagina.datos);
    total = pagina.total;
    first += pagina.datos.length;
    if (pagina.datos.length === 0) break;
  }
  return datos;
}

export async function obtener(ruta, id) {
  const respuesta = await pedir(`${ruta}/${id}`);
  return respuesta.json();
}

export async function crear(ruta, datos) {
  if (!estaConectado()) {
    encolar({ metodo: "POST", ruta, cuerpo: datos });
    return null;
  }
  const respuesta = await pedir(ruta, {
    method: "POST",
    body: JSON.stringify(datos),
  });
  return respuesta.headers.get("Location");
}

export async function crearConId(ruta, datos) {
  const ubicacion = await crear(ruta, datos);
  return ubicacion ? ubicacion.split("/").filter(Boolean).pop() : null;
}

export async function actualizar(ruta, id, datos) {
  if (!estaConectado()) {
    encolar({ metodo: "PUT", ruta: `${ruta}/${id}`, cuerpo: datos });
    return;
  }
  await pedir(`${ruta}/${id}`, {
    method: "PUT",
    body: JSON.stringify(datos),
  });
}

export async function eliminar(ruta, id) {
  if (!estaConectado()) {
    encolar({ metodo: "DELETE", ruta: `${ruta}/${id}`, cuerpo: null });
    return;
  }
  await pedir(`${ruta}/${id}`, { method: "DELETE" });
}

/**
 * Reenvia una operacion guardada en la cola sin conexion.
 * La ruta ya trae el identificador completo cuando el metodo lo requiere.
 */
export async function ejecutarPendiente(pendiente) {
  if (pendiente.metodo === "POST") {
    await pedir(pendiente.ruta, { method: "POST", body: JSON.stringify(pendiente.cuerpo ?? {}) });
    return;
  }
  if (pendiente.metodo === "PUT") {
    await pedir(pendiente.ruta, { method: "PUT", body: JSON.stringify(pendiente.cuerpo ?? {}) });
    return;
  }
  if (pendiente.metodo === "DELETE") {
    await pedir(pendiente.ruta, { method: "DELETE" });
  }
}

export { estaConectado, sincronizar, listarPendientes };
