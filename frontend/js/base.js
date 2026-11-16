export const estilosBase = `
  :host {
    --icono-alerta: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='black' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='9'/%3E%3Cpath d='M12 7.5v5.5M12 16.5h.01'/%3E%3C/svg%3E");
    display: block;
    color: var(--texto, #14202b);
    font-family: var(--fuente, system-ui, sans-serif);
    line-height: 1.5;
  }
  * { box-sizing: border-box; }
  /* Ningún display de componente debe ganarle al atributo hidden. */
  [hidden] { display: none !important; }
  ::selection { background: color-mix(in srgb, var(--acento, #0f7b6c) 22%, transparent); }
  h1, h2, h3, p { margin: 0; }
  h1, h2, h3 { text-wrap: balance; letter-spacing: -0.01em; }
  button, input, select, textarea { font: inherit; }
  button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.45rem;
    min-height: 2.6rem;
    border: 1px solid var(--mate, #2c4a63);
    background: var(--mate, #2c4a63);
    color: #fff;
    border-radius: var(--radio, 10px);
    padding: 0.55rem 1.05rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
  }
  button:hover { background: var(--mate-fuerte, #1f3a50); border-color: var(--mate-fuerte, #1f3a50); }
  button:active { transform: translateY(1px); }
  button:focus-visible { outline: none; box-shadow: var(--anillo); }
  button:disabled { opacity: 0.45; cursor: not-allowed; }
  button.secundario { background: var(--superficie, #fff); color: var(--mate, #2c4a63); border-color: var(--borde-fuerte, #b5c3d1); }
  button.secundario:hover { background: var(--mate-suave, #e9eff4); border-color: var(--mate, #2c4a63); }
  button.peligro { background: transparent; border-color: var(--peligro, #b42318); color: var(--peligro, #b42318); }
  button.peligro:hover { background: var(--peligro-suave, #fdeceb); }
  button.enlace {
    min-height: 0;
    background: none;
    border: none;
    color: var(--acento, #0f7b6c);
    padding: 0.2rem;
    font-weight: 500;
    text-decoration: underline;
    text-underline-offset: 0.18em;
    font-size: 0.85rem;
  }
  button.enlace:hover { background: none; color: var(--acento-fuerte, #0b6155); }
  .campo { display: grid; gap: 0.35rem; }
  .campo label { font-size: 0.82rem; font-weight: 600; color: var(--mate, #2c4a63); }
  .campo input, .campo select, .campo textarea {
    width: 100%;
    min-height: 2.75rem;
    padding: 0.6rem 0.75rem;
    border: 1px solid var(--borde-fuerte, #b5c3d1);
    border-radius: var(--radio, 10px);
    background: var(--superficie, #fff);
    color: inherit;
    caret-color: var(--acento, #0f7b6c);
    accent-color: var(--acento, #0f7b6c);
    transition: border-color 0.15s ease, box-shadow 0.15s ease;
  }
  .campo input::placeholder, .campo textarea::placeholder { color: var(--texto-tenue, #5f7387); }
  .campo input:hover, .campo select:hover, .campo textarea:hover { border-color: var(--mate, #2c4a63); }
  .campo textarea { min-height: 88px; resize: vertical; }
  .campo input:focus, .campo select:focus, .campo textarea:focus {
    outline: none;
    border-color: var(--acento, #0f7b6c);
    box-shadow: var(--anillo);
  }
  .tarjeta {
    background: var(--superficie, #fff);
    border: 1px solid var(--borde, #d6dee7);
    border-radius: var(--radio-grande, 14px);
    padding: 1.15rem 1.25rem;
    box-shadow: var(--sombra);
  }
  .etiqueta {
    display: inline-flex;
    align-items: center;
    gap: 0.35rem;
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.04em;
    text-transform: uppercase;
    color: var(--mate, #2c4a63);
  }
  .aviso { color: var(--texto-suave, #4d6378); font-size: 0.9rem; }
  .error { color: var(--peligro, #b42318); font-size: 0.85rem; }
  /* Aviso de error de formulario: recuadro con ícono, compartido por todas las vistas. */
  .error[role="alert"] {
    display: flex; align-items: flex-start; gap: 0.55rem;
    padding: 0.7rem 0.85rem; border-radius: var(--radio, 10px);
    background: var(--peligro-suave, #fdeceb); font-size: 0.88rem; font-weight: 500;
  }
  .error[role="alert"]::before {
    content: ""; flex: none; width: 1.1rem; height: 1.1rem; margin-top: 0.08rem; background: currentColor;
    -webkit-mask: var(--icono-alerta) center / contain no-repeat; mask: var(--icono-alerta) center / contain no-repeat;
  }
  .rejilla { display: grid; gap: 0.85rem; }
  .rejilla.dos { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .rejilla.tres { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .fila { display: flex; flex-wrap: wrap; gap: 0.6rem; align-items: center; }
  .separador { flex: 1; }
  .vacio {
    border: 1px dashed var(--borde-fuerte, #b5c3d1);
    border-radius: var(--radio-grande, 14px);
    padding: 1.5rem;
    text-align: center;
    color: var(--texto-suave, #4d6378);
    background: color-mix(in srgb, var(--superficie, #fff) 55%, transparent);
  }
  @media (max-width: 720px) {
    .rejilla.dos, .rejilla.tres { grid-template-columns: 1fr; }
  }
`;

export class Vista extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
  }

  get estilos() {
    return "";
  }

  render(contenido) {
    this.shadowRoot.innerHTML = `<style>${estilosBase}${this.estilos}</style>${contenido}`;
  }

  q(selector) {
    return this.shadowRoot.querySelector(selector);
  }

  qa(selector) {
    return [...this.shadowRoot.querySelectorAll(selector)];
  }

  notificar(mensaje, tipo = "info") {
    this.dispatchEvent(
      new CustomEvent("notificar", { bubbles: true, composed: true, detail: { mensaje, tipo } })
    );
  }

  navegar(ruta) {
    this.dispatchEvent(new CustomEvent("navegar", { bubbles: true, composed: true, detail: ruta }));
  }
}

export function escapar(texto) {
  return String(texto ?? "").replace(/[&<>"']/g, (caracter) => {
    const mapa = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };
    return mapa[caracter];
  });
}

export function fechaHora(valor) {
  if (!valor) return "—";
  const fecha = new Date(valor);
  if (Number.isNaN(fecha.getTime())) return "—";
  return fecha.toLocaleString("es-SV", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function fechaCorta(valor) {
  if (!valor) return "—";
  const fecha = new Date(valor);
  if (Number.isNaN(fecha.getTime())) return "—";
  return fecha.toLocaleDateString("es-SV", { day: "2-digit", month: "short", year: "numeric" });
}

/**
 * Fecha sin hora (nacimiento): la API la guarda a medianoche UTC, así que se
 * formatea en UTC para no mostrar el día anterior en zonas horarias negativas.
 */
export function fechaCalendario(valor) {
  if (!valor) return "—";
  const fecha = new Date(valor);
  if (Number.isNaN(fecha.getTime())) return "—";
  return fecha.toLocaleDateString("es-SV", { day: "2-digit", month: "short", year: "numeric", timeZone: "UTC" });
}

export function hoyISO() {
  const fecha = new Date();
  const mes = String(fecha.getMonth() + 1).padStart(2, "0");
  const dia = String(fecha.getDate()).padStart(2, "0");
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function ahoraISO() {
  const fecha = new Date();
  const zona = -fecha.getTimezoneOffset();
  const signo = zona >= 0 ? "+" : "-";
  const horas = String(Math.floor(Math.abs(zona) / 60)).padStart(2, "0");
  const minutos = String(Math.abs(zona) % 60).padStart(2, "0");
  const local = new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().slice(0, 19);
  return `${local}${signo}${horas}:${minutos}`;
}
