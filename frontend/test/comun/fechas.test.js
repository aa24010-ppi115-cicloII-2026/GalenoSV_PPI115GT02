import { expect } from "chai";
import sinon from "sinon";

describe("fechas", () => {
  it("fechaCalendario muestra una fecha sin hora guardada en UTC con el día correcto", async () => {
    const { fechaCalendario } = await import("../../js/comun/fechas.js");
    expect(fechaCalendario("1992-09-30T00:00:00Z")).to.match(/^30 /);
    expect(fechaCalendario("1992-09-30")).to.match(/^30 /);
    expect(fechaCalendario(null)).to.equal("—");
    expect(fechaCalendario("no es fecha")).to.equal("—");
  });

  it("las fechas se muestran sin cero inicial en el día, como «5 nov 2026»", async () => {
    const { fechaCalendario, fechaCorta, fechaHora } = await import("../../js/comun/fechas.js");
    expect(fechaCalendario("2026-11-05")).to.match(/^5 nov/);
    expect(fechaCorta("2026-11-05T12:00:00-06:00")).to.match(/^5 nov/);
    expect(fechaHora("2026-11-05T14:00:00-06:00")).to.match(/^5 nov/);
  });

  it("formatea igual que toLocaleString, con formateadores ya creados en vez de uno por fecha", async () => {
    const { fechaCalendario, fechaCorta, fechaHora } = await import("../../js/comun/fechas.js");
    const corta = { day: "numeric", month: "short", year: "numeric" };
    const fechas = Array.from({ length: 60 }, (_, i) => new Date(Date.UTC(2026, i % 12, 1 + (i % 28), (i * 7) % 24, (i * 13) % 60)));
    const esperado = fechas.map((fecha) => [
      fecha.toLocaleString("es-SV", { ...corta, hour: "numeric", minute: "2-digit" }),
      fecha.toLocaleDateString("es-SV", corta),
      fecha.toLocaleDateString("es-SV", { ...corta, timeZone: "UTC" }),
    ]);
    // toLocaleString crea un Intl.DateTimeFormat en cada llamada: MDN recomienda reutilizar uno.
    const porFecha = [sinon.spy(Date.prototype, "toLocaleString"), sinon.spy(Date.prototype, "toLocaleDateString")];
    try {
      const obtenido = fechas.map((fecha) => [fechaHora(fecha), fechaCorta(fecha), fechaCalendario(fecha)]);
      expect(obtenido).to.deep.equal(esperado);
      expect(porFecha.some((espia) => espia.called)).to.be.false;
    } finally {
      porFecha.forEach((espia) => espia.restore());
    }
  });
});
