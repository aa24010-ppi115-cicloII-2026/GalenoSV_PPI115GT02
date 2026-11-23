import { expect } from "chai";

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
});
