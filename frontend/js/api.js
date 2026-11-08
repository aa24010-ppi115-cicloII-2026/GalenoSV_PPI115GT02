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
  const respuesta = await fetch(`${API}/${ruta}`, {
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    ...opciones,
  });
  if (!respuesta.ok) throw await errorDe(respuesta);
  return respuesta;
}

export async function listar(ruta, { first = 0, max = 20 } = {}) {
  const respuesta = await pedir(`${ruta}?first=${first}&max=${max}`);
  return {
    datos: await respuesta.json(),
    total: Number(respuesta.headers.get("X-Total-Count") ?? 0),
  };
}

export async function obtener(ruta, id) {
  const respuesta = await pedir(`${ruta}/${id}`);
  return respuesta.json();
}

export async function crear(ruta, datos) {
  const respuesta = await pedir(ruta, {
    method: "POST",
    body: JSON.stringify(datos),
  });
  return respuesta.headers.get("Location");
}

export async function actualizar(ruta, id, datos) {
  await pedir(`${ruta}/${id}`, {
    method: "PUT",
    body: JSON.stringify(datos),
  });
}

export async function eliminar(ruta, id) {
  await pedir(`${ruta}/${id}`, { method: "DELETE" });
}
