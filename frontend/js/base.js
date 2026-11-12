export const estilosBase = `
  :host {
    display: block;
    color: var(--texto, #14202b);
    font-family: system-ui, "Segoe UI", Roboto, sans-serif;
    line-height: 1.5;
  }
  * { box-sizing: border-box; }
  h1, h2, h3, p { margin: 0; }
  button, input, select, textarea { font: inherit; }
  button {
    border: 1px solid var(--mate, #2c4a63);
    background: var(--mate, #2c4a63);
    color: #fff;
    border-radius: 10px;
    padding: 0.6rem 1rem;
    cursor: pointer;
    transition: filter 0.15s ease, transform 0.05s ease;
  }
  button:hover { filter: brightness(1.08); }
  button:active { transform: translateY(1px); }
  button:disabled { opacity: 0.45; cursor: not-allowed; }
  button.secundario { background: transparent; color: var(--mate, #2c4a63); }
  button.peligro { background: transparent; border-color: #b42318; color: #b42318; }
  button.enlace {
    background: none;
    border: none;
    color: var(--acento, #0f7b6c);
    padding: 0.2rem;
    text-decoration: underline;
    font-size: 0.85rem;
  }
  .campo { display: grid; gap: 0.3rem; }
  .campo label { font-size: 0.8rem; font-weight: 600; color: var(--mate, #2c4a63); }
  .campo input, .campo select, .campo textarea {
    width: 100%;
    padding: 0.6rem 0.7rem;
    border: 1px solid var(--borde, #cbd5e1);
    border-radius: 10px;
    background: #fff;
    color: inherit;
  }
  .campo textarea { min-height: 88px; resize: vertical; }
  .campo input:focus, .campo select:focus, .campo textarea:focus {
    outline: 2px solid color-mix(in srgb, var(--acento, #0f7b6c) 45%, white);
    border-color: var(--acento, #0f7b6c);
  }
  .tarjeta {
    background: #fff;
    border: 1px solid var(--borde, #cbd5e1);
    border-radius: 16px;
    padding: 1rem 1.15rem;
    box-shadow: 0 8px 24px rgba(20, 32, 43, 0.07);
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
  .aviso { color: #587089; font-size: 0.9rem; }
  .error { color: #b42318; font-size: 0.85rem; }
  .rejilla { display: grid; gap: 0.85rem; }
  .rejilla.dos { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .rejilla.tres { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .fila { display: flex; flex-wrap: wrap; gap: 0.6rem; align-items: center; }
  .separador { flex: 1; }
  .vacio {
    border: 1px dashed var(--borde, #cbd5e1);
    border-radius: 14px;
    padding: 1.5rem;
    text-align: center;
    color: #587089;
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
